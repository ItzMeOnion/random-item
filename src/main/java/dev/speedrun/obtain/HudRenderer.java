package dev.speedrun.obtain;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.List;

/** Draws the spin, the "Obtain X" reveal, the top timer bar and the completion splash. */
public final class HudRenderer {
	private static final int SPIN_FRAMES = 90;
	private static final int GOLD = 0xFFFFD54A;
	private static final int WHITE = 0xFFFFFFFF;
	private static final int GREY = 0xFFB0B0C0;
	private static final int CYAN = 0xFF7FE8FF;

	private HudRenderer() {}

	public static void render(DrawContext ctx) {
		if (!RunManager.isActive()) return;

		MinecraftClient mc = MinecraftClient.getInstance();
		TextRenderer tr = mc.textRenderer;
		int sw = mc.getWindow().getScaledWidth();
		int sh = mc.getWindow().getScaledHeight();
		float t = System.currentTimeMillis() / 1000f;
		long elapsed = RunManager.phaseElapsedMs();

		switch (RunManager.phase()) {
			case SPIN -> renderSpin(ctx, tr, sw, sh, t, elapsed);
			case REVEAL -> renderReveal(ctx, tr, sw, sh, t, elapsed);
			case RUNNING, DONE -> renderTimerBar(ctx, tr, sw, t);
		}

		if (RunManager.phase() == RunManager.Phase.DONE && elapsed < RunManager.DONE_MS) {
			renderSplash(ctx, tr, sw, sh, t, elapsed);
		}
	}

	/** Slot-machine style flicker through the item pictures, slowing down until it lands. */
	private static void renderSpin(DrawContext ctx, TextRenderer tr, int sw, int sh, float t, long elapsed) {
		List<Item> pool = RunManager.spinPool();
		if (pool.isEmpty()) return;

		float p = Math.min(1f, elapsed / (float) RunManager.SPIN_MS);
		float eased = 1f - (float) Math.pow(1f - p, 3);
		int frame = (int) (eased * SPIN_FRAMES);
		ItemStack shown = p >= 1f
				? RunManager.targetStack()
				: new ItemStack(pool.get(frame % pool.size()));

		int cx = sw / 2;
		int cy = sh / 2 - 50;
		RenderUtil.glow(ctx, cx, cy, 50, t, 0x30);
		RenderUtil.rainbowBorder(ctx, cx - 52, cy - 52, cx + 52, cy + 52, t, 0xFF);
		ctx.fill(cx - 50, cy - 50, cx + 50, cy + 50, 0xD0080612);
		RenderUtil.bigItem(ctx, shown, cx, cy, 5f);

		String label = p >= 1f ? "LOCKED IN" : "ROLLING";
		RenderUtil.rainbowText(ctx, tr, label, cx, cy + 66, 2f, 0xFF, t);
		RenderUtil.drawText(ctx, tr, shown.getName().getString(), cx, cy + 96, 1f, GREY, true);
	}

	private static void renderReveal(DrawContext ctx, TextRenderer tr, int sw, int sh, float t, long elapsed) {
		int a = (int) (255f * Math.min(1f, elapsed / 400f));
		int cx = sw / 2;
		int cy = sh / 2 - 40;

		RenderUtil.glow(ctx, cx, cy, 52, t, a * 0x30 / 255);
		RenderUtil.bigItem(ctx, RunManager.targetStack(), cx, cy, 7f);
		RenderUtil.rainbowText(ctx, tr, "Obtain " + RunManager.targetName(), cx, sh / 2 + 36, 2.2f, a, t);

		long secondsLeft = (RunManager.REVEAL_MS - elapsed + 999L) / 1000L;
		RenderUtil.drawText(ctx, tr, "Timer starts in " + secondsLeft, cx, sh / 2 + 80, 1f,
				RenderUtil.withAlpha(GREY, a), true);
	}

	/** Compact panel at the top: item, name and live timer. Freezes and turns gold when done. */
	private static void renderTimerBar(DrawContext ctx, TextRenderer tr, int sw, float t) {
		boolean done = RunManager.phase() == RunManager.Phase.DONE;
		int w = 240;
		int h = 46;
		int x = sw / 2 - w / 2;
		int y = 6;

		ctx.fill(x, y, x + w, y + h, 0xC0080612);
		RenderUtil.rainbowBorder(ctx, x, y, x + w, y + h, t, 0xFF);
		ctx.drawItem(RunManager.targetStack(), x + 12, y + 15);

		String name = RunManager.targetName();
		if (tr.getWidth(name) > w - 50) {
			name = tr.trimToWidth(name, w - 60) + "...";
		}
		ctx.drawText(tr, name, x + 38, y + 6, GREY, true);
		RenderUtil.drawText(ctx, tr, RenderUtil.formatTime(RunManager.currentRunMs()),
				x + 38, y + 17, 1.7f, done ? GOLD : CYAN, false);
		if (done) {
			RenderUtil.drawText(ctx, tr, "DONE", x + w - 8, y + 6, 1f, GOLD, false);
		}
	}

	/** Big celebratory card after the item is obtained. Fades out at the end. */
	private static void renderSplash(DrawContext ctx, TextRenderer tr, int sw, int sh, float t, long elapsed) {
		int a = (int) (255f * Math.min(1f, (RunManager.DONE_MS - elapsed) / 1000f));
		int cx = sw / 2;
		int cy = sh / 2 - 30;

		RenderUtil.glow(ctx, cx, cy, 60, t, a * 0x40 / 255);
		RenderUtil.rainbowText(ctx, tr, "OBTAINED!", cx, cy - 98, 3f, a, t);
		RenderUtil.bigItem(ctx, RunManager.targetStack(), cx, cy, 6f);
		RenderUtil.drawText(ctx, tr, RenderUtil.formatTime(RunManager.currentRunMs()), cx, cy + 62, 2.6f,
				RenderUtil.withAlpha(GOLD, a), true);
		RenderUtil.drawText(ctx, tr, RunManager.targetName(), cx, cy + 102, 1.2f,
				RenderUtil.withAlpha(WHITE, a), true);
		RenderUtil.drawText(ctx, tr, "Saved to history - press H", cx, cy + 128, 1f,
				RenderUtil.withAlpha(GREY, a), true);
	}
}
