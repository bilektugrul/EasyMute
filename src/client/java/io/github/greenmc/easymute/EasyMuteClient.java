package io.github.greenmc.easymute;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.util.InputUtil;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class EasyMuteClient implements ClientModInitializer {

	private static KeyBinding muteKey;

	private float savedVolume = 0.5F;
	private boolean muted;

	@Override
	public void onInitializeClient() {
		muteKey = KeyBindingHelper.registerKeyBinding(
				new KeyBinding("key.easymute.mute",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_M,
				"category.easymute.mod"));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (muteKey.wasPressed()) {

				if (!muted) mute(client, client.options);
				else unmute(client, client.options);

			}
		});
	}

	private void mute(MinecraftClient client, GameOptions options) {
		float currentVolume = options.getSoundVolume(SoundCategory.MASTER);
		if (currentVolume == 0.0F) {
			unmute(client, options);
			return;
		}

		this.savedVolume = currentVolume;
		this.muted = true;
		this.sendToast(client, "text.easymute.muted");

		options.getSoundVolumeOption(SoundCategory.MASTER).setValue(0D);
	}

	private void unmute(MinecraftClient client, GameOptions options) {
		options.getSoundVolumeOption(SoundCategory.MASTER).setValue((savedVolume == 0.0D ? 0.5D : savedVolume));

		this.muted = false;
		this.sendToast(client, "text.easymute.unmuted");
	}

	private void sendToast(MinecraftClient client, String desc) {
		client.getToastManager().add(new SystemToast(
				SystemToast.Type.NARRATOR_TOGGLE,
				Text.literal("EasyMute"),
				Text.translatable(desc)));
	}

}