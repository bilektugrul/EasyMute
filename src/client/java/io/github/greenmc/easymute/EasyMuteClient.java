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
import org.lwjgl.glfw.GLFW;

public class EasyMuteClient implements ClientModInitializer {

	private static KeyMapping muteKey;

	private float savedVolume = 0.5F;
	private boolean muted;

	@Override
	public void onInitializeClient() {
		muteKey = KeyMappingHelper.registerKeyMapping(
				new KeyMapping("key.easymute.mute",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_M, KeyMapping.Category.register(Identifier.parse("category.easymute.mod"))));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (muteKey.isDown()) {

				if (!muted) mute(client, client.options);
				else unmute(client, client.options);

			}
		});
	}

	private void mute(Minecraft client, Options options) {
		float currentVolume = options.getSoundSourceVolume(SoundSource.MASTER);
		if (currentVolume == 0.0F) {
			unmute(client, options);
			return;
		}

		this.savedVolume = currentVolume;
		this.muted = true;
		this.sendToast(client, "text.easymute.muted");

		options.getSoundSourceOptionInstance(SoundSource.MASTER).set(0D);
	}

	private void unmute(Minecraft client, Options options) {
		options.getSoundSourceOptionInstance(SoundSource.MASTER).set((savedVolume == 0.0D ? 0.5D : savedVolume));

		this.muted = false;
		this.sendToast(client, "text.easymute.unmuted");
	}

	private void sendToast(Minecraft client, String desc) {
		client.getToastManager().addToast(new SystemToast(
				SystemToast.SystemToastId.NARRATOR_TOGGLE,
				Component.literal("EasyMute"),
				Component.translatable(desc)));
	}

}