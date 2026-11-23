package io.github.greenmc.easymute;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.*;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;

import java.util.Map;

public class EasyMuteClient implements ClientModInitializer {

	private KeyMapping muteKey;
	private boolean muted;

	@Override
	public void onInitializeClient() {
		EasyMuteConfig.load();

		this.muted = !EasyMuteConfig.savedVolumes.isEmpty();
		this.muteKey = KeyMappingHelper.registerKeyMapping(
				new KeyMapping("key.easymute.mute",
						InputConstants.Type.KEYBOARD,
						InputConstants.KEY_M, KeyMapping.Category.register(Identifier.parse("category.easymute.mod")))
		);

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (muteKey.consumeClick()) {
				if (!muted) mute(client, client.options);
				else unmute(client, client.options);
			}

			updateBackgroundMute(client, client.options);
		});
	}

	private void updateBackgroundMute(Minecraft client, Options options) {
		boolean inBackground = !client.getWindow().isFocused() || client.getWindow().isIconified();
		boolean backgroundMuted = !EasyMuteConfig.backgroundVolumes.isEmpty();

		if (inBackground && EasyMuteConfig.muteInBackground && !backgroundMuted) {
			for (SoundSource source : SoundSource.values()) {
				boolean shouldMute = EasyMuteConfig.backgroundUsesCategories
						? EasyMuteConfig.categoriesToMute.getOrDefault(source, false)
						: source == SoundSource.MASTER;
				double currentVolume = options.getSoundSourceVolume(source);

				if (shouldMute && currentVolume != 0.0D) {
					EasyMuteConfig.backgroundVolumes.put(source, currentVolume);
					options.getSoundSourceOptionInstance(source).set(0.0D);
				}
			}

			if (!EasyMuteConfig.backgroundVolumes.isEmpty()) {
				EasyMuteConfig.save();
			}
		} else if (!inBackground && backgroundMuted) {
			for (Map.Entry<SoundSource, Double> entry : EasyMuteConfig.backgroundVolumes.entrySet()) {
				if (options.getSoundSourceVolume(entry.getKey()) == 0.0D) {
					options.getSoundSourceOptionInstance(entry.getKey()).set(entry.getValue());
				}
			}

			EasyMuteConfig.backgroundVolumes.clear();
			EasyMuteConfig.save();
		}
	}

	private void mute(Minecraft client, Options options) {
		EasyMuteConfig.savedVolumes.clear();

		for (SoundSource source : SoundSource.values()) {
			if (EasyMuteConfig.categoriesToMute.getOrDefault(source, false)) {
				double currentVolume = options.getSoundSourceVolume(source);
				if (currentVolume == 0.0D) {
					continue;
				}

				EasyMuteConfig.savedVolumes.put(source, currentVolume);
				options.getSoundSourceOptionInstance(source).set(0.0D);
			}
		}

		this.muted = true;
		EasyMuteConfig.save();
		this.sendToast(client, "text.easymute.muted");
	}

	private void unmute(Minecraft client, Options options) {
		this.muted = false;

		for (Map.Entry<SoundSource, Double> entry : EasyMuteConfig.savedVolumes.entrySet()) {
			if (options.getSoundSourceVolume(entry.getKey()) == 0.0D) {
				options.getSoundSourceOptionInstance(entry.getKey()).set(entry.getValue());
			}
		}

		EasyMuteConfig.savedVolumes.clear();
		EasyMuteConfig.save();
		this.sendToast(client, "text.easymute.unmuted");
	}

	private void sendToast(Minecraft client, String desc) {
		client.gui.toastManager().addToast(new SystemToast(
				SystemToast.SystemToastId.NARRATOR_TOGGLE,
				Component.literal("EasyMute"),
				Component.translatable(desc)));
	}

}
