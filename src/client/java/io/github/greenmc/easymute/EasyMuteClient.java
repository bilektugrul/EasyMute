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

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public class EasyMuteClient implements ClientModInitializer {

	private final Map<SoundSource, Double> savedVolumes = new EnumMap<>(SoundSource.class);

	private KeyMapping muteKey;
	private boolean muted;

	@Override
	public void onInitializeClient() {
		EasyMuteConfig.load();

		EnumSet<SoundSource> mutedCategories = EnumSet.noneOf(SoundSource.class);
		for (SoundSource source : SoundSource.values()) {
			if (Minecraft.getInstance().options.getSoundSourceVolume(source) == 0.0F) {
				mutedCategories.add(source);
			}
		}

		this.muted = mutedCategories.size() == SoundSource.values().length;
		this.muteKey = KeyMappingHelper.registerKeyMapping(
				new KeyMapping("key.easymute.mute",
						InputConstants.Type.KEYBOARD,
						InputConstants.KEY_M, KeyMapping.Category.register(Identifier.parse("category.easymute.mod")))
		);

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (muteKey.isDown()) {

				if (!muted) mute(client, client.options);
				else unmute(client, client.options);

			}
		});
	}

	private void mute(Minecraft client, Options options) {
		this.savedVolumes.clear();

		for (SoundSource source : SoundSource.values()) {
			if (EasyMuteConfig.categoriesToMute.getOrDefault(source, false)) {
				double currentVolume = options.getSoundSourceVolume(source);
				if (currentVolume == 0.0F) {
					continue;
				}

				this.savedVolumes.put(source, currentVolume);
				options.getSoundSourceOptionInstance(source).set(0.0D);
			}
		}

		this.sendToast(client, "text.easymute.muted");
	}

	private void unmute(Minecraft client, Options options) {
		this.muted = false;

		for (SoundSource category : SoundSource.values()) {
			double volumeToRestore = savedVolumes.getOrDefault(category, 0.5D);
			options.getSoundSourceOptionInstance(category).set(volumeToRestore);
		}

		this.savedVolumes.clear();
		this.sendToast(client, "text.easymute.unmuted");
	}

	private void sendToast(Minecraft client, String desc) {
		client.gui.toastManager().addToast(new SystemToast(
				SystemToast.SystemToastId.NARRATOR_TOGGLE,
				Component.literal("EasyMute"),
				Component.translatable(desc)));
	}

}