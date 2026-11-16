package dev.speedrun.obtain;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class ObtainSpeedrunClient implements ClientModInitializer {
	public static final KeyBinding.Category CATEGORY =
			KeyBinding.Category.create(Identifier.of("obtain-speedrun", "main"));

	public static final KeyBinding HISTORY_KEY = KeyBindingHelper.registerKeyBinding(
			new KeyBinding("key.obtain-speedrun.history", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_H, CATEGORY));

	@Override
	public void onInitializeClient() {
		HistoryStore.load();

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> RunManager.onJoin(client));
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> RunManager.onDisconnect());

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			RunManager.tick(client);
			while (HISTORY_KEY.wasPressed()) {
				if (client.currentScreen == null) {
					client.setScreen(new HistoryScreen(null));
				}
			}
		});

		HudRenderCallback.EVENT.register((context, tickCounter) -> HudRenderer.render(context));
	}
}
