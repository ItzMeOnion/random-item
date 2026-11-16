package dev.speedrun.obtain;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Scrollable list of finished runs, newest first. Best run is highlighted. */
public final class HistoryScreen extends Screen {
	private static final int ROW_H = 36;
	private static final int PANEL_W = 380;
	private static final int GOLD = 0xFFFFD54A;
	private static final int WHITE = 0xFFFFFFFF;
	private static final int GREY = 0xFFB0B0C0;
	private static final int DIM = 0xFF707080;
	private static final int CYAN = 0xFF7FE8FF;
	private static final DateTimeFormatter DATE =
			DateTimeFormatter.ofPattern("d MMM yyyy HH:mm").withZone(ZoneId.systemDefault());

	private final Screen parent;
	private double scroll;

	public HistoryScreen(Screen parent) {
		super(Text.literal("Run History"));
		this.parent = parent;
	}

	@Override
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
		TextRenderer tr = this.textRenderer;
		float t = System.currentTimeMillis() / 1000f;
		List<HistoryStore.Entry> entries = HistoryStore.ENTRIES;

		ctx.fill(0, 0, this.width, this.height, 0xE0070410);
		RenderUtil.rainbowText(ctx, tr, "RUN HISTORY", this.width / 2f, 12, 2.4f, 0xFF, t);

		long best = Long.MAX_VALUE;
		long total = 0L;
		for (HistoryStore.Entry e : entries) {
			best = Math.min(best, e.timeMs);
			total += e.timeMs;
		}
		String stats = entries.isEmpty()
				? "No runs yet"
				: "Runs: " + entries.size()
				+ "   Best: " + RenderUtil.formatTime(best)
				+ "   Avg: " + RenderUtil.formatTime(total / entries.size());
		RenderUtil.drawText(ctx, tr, stats, this.width / 2f, 46, 1f, GREY, true);
		RenderUtil.drawText(ctx, tr, "Scroll to browse  -  ESC to close", this.width / 2f, 60, 1f, DIM, true);

		int px = this.width / 2 - PANEL_W / 2;
		int top = 80;
		int bottom = this.height - 16;
		RenderUtil.panel(ctx, px - 2, top - 2, px + PANEL_W + 2, bottom + 2, 0x90101020, 0x66FFFFFF);

		if (entries.isEmpty()) {
			RenderUtil.drawText(ctx, tr, "Join a singleplayer world to start a run!",
					this.width / 2f, top + 20, 1f, GREY, true);
			return;
		}

		double maxScroll = Math.max(0, entries.size() * ROW_H - (bottom - top));
		scroll = Math.max(0, Math.min(scroll, maxScroll));

		ctx.enableScissor(px, top, px + PANEL_W, bottom);
		for (int i = 0; i < entries.size(); i++) {
			HistoryStore.Entry entry = entries.get(entries.size() - 1 - i);
			int y = top + i * ROW_H - (int) scroll;
			if (y + ROW_H < top || y > bottom) continue;

			boolean isBest = entry.timeMs == best;
			RenderUtil.panel(ctx, px, y, px + PANEL_W, y + ROW_H - 4, 0x90181028,
					isBest ? GOLD : 0x55FFFFFF);
			if (isBest) {
				RenderUtil.rainbowBorder(ctx, px, y, px + PANEL_W, y + ROW_H - 4, t, 0xFF);
			}

			ItemStack stack = new ItemStack(HistoryStore.itemOf(entry));
			RenderUtil.drawText(ctx, tr, "#" + (i + 1), px + 8, y + 12, 1f, GREY, false);
			ctx.drawItem(stack, px + 34, y + 8);
			ctx.drawText(tr, stack.getName().getString(), px + 60, y + 5, isBest ? GOLD : WHITE, true);
			ctx.drawText(tr, RenderUtil.formatTime(entry.timeMs), px + 60, y + 16, CYAN, true);

			String date = DATE.format(Instant.ofEpochMilli(entry.date));
			ctx.drawText(tr, date, px + PANEL_W - 8 - tr.getWidth(date), y + 16, DIM, true);
		}
		ctx.disableScissor();
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		scroll -= verticalAmount * 20.0;
		return true;
	}

	@Override
	public void close() {
		this.client.setScreen(parent);
	}
}
