package dev.speedrun.obtain;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;

/** Shared drawing helpers: rainbow colours, scaled text, glows, panels and item renders. */
public final class RenderUtil {
	private RenderUtil() {}

	/** HSV to ARGB. h wraps around 0..1. */
	public static int hsv(float h, float s, float v, int alpha) {
		float hh = ((h % 1f) + 1f) % 1f * 6f;
		int i = (int) Math.floor(hh);
		float f = hh - i;
		float p = v * (1f - s);
		float q = v * (1f - s * f);
		float t = v * (1f - s * (1f - f));
		float r, g, b;
		switch (i % 6) {
			case 0 -> { r = v; g = t; b = p; }
			case 1 -> { r = q; g = v; b = p; }
			case 2 -> { r = p; g = v; b = t; }
			case 3 -> { r = p; g = q; b = v; }
			case 4 -> { r = t; g = p; b = v; }
			default -> { r = v; g = p; b = q; }
		}
		return ((alpha & 0xFF) << 24)
				| (((int) (r * 255f) & 0xFF) << 16)
				| (((int) (g * 255f) & 0xFF) << 8)
				| ((int) (b * 255f) & 0xFF);
	}

	public static int withAlpha(int argb, int alpha) {
		return ((alpha & 0xFF) << 24) | (argb & 0xFFFFFF);
	}

	public static String formatTime(long ms) {
		long minutes = ms / 60000L;
		long seconds = (ms / 1000L) % 60L;
		long millis = ms % 1000L;
		return String.format("%02d:%02d.%03d", minutes, seconds, millis);
	}

	/** Text drawn at a scale, optionally centred horizontally on x. */
	public static void drawText(DrawContext ctx, TextRenderer tr, String s, float x, float y,
								float scale, int color, boolean centered) {
		float x0 = centered ? x - tr.getWidth(s) * scale / 2f : x;
		var m = ctx.getMatrices();
		m.pushMatrix();
		m.translate(x0, y);
		m.scale(scale, scale);
		ctx.drawText(tr, s, 0, 0, color, true);
		m.popMatrix();
	}

	/** Each letter gets its own hue, sliding over time. Centred on cx. */
	public static void rainbowText(DrawContext ctx, TextRenderer tr, String s, float cx, float y,
								   float scale, int alpha, float t) {
		float x = cx - tr.getWidth(s) * scale / 2f;
		for (int i = 0; i < s.length(); i++) {
			String ch = String.valueOf(s.charAt(i));
			int color = hsv(t * 0.4f + i * 0.045f, 0.7f, 1f, alpha);
			drawText(ctx, tr, ch, x + tr.getWidth(s.substring(0, i)) * scale, y, scale, color, false);
		}
	}

	/** A 16x16 item icon drawn at a scale, centred on (cx, cy). */
	public static void bigItem(DrawContext ctx, ItemStack stack, float cx, float cy, float scale) {
		var m = ctx.getMatrices();
		m.pushMatrix();
		m.translate(cx, cy);
		m.scale(scale, scale);
		ctx.drawItem(stack, -8, -8);
		m.popMatrix();
	}

	/** Concentric translucent rainbow squares behind something. */
	public static void glow(DrawContext ctx, int cx, int cy, int radius, float t, int maxAlpha) {
		for (int k = 4; k >= 0; k--) {
			int r = radius + k * 5;
			int a = maxAlpha * (5 - k) / 5;
			ctx.fill(cx - r, cy - r, cx + r, cy + r, hsv(t * 0.3f + k * 0.07f, 0.8f, 1f, a));
		}
	}

	/** Solid panel with a 1px border. */
	public static void panel(DrawContext ctx, int x1, int y1, int x2, int y2, int bg, int border) {
		ctx.fill(x1, y1, x2, y2, bg);
		ctx.fill(x1, y1, x2, y1 + 1, border);
		ctx.fill(x1, y2 - 1, x2, y2, border);
		ctx.fill(x1, y1, x1 + 1, y2, border);
		ctx.fill(x2 - 1, y1, x2, y2, border);
	}

	/** A 2px border whose colour cycles around the rectangle. */
	public static void rainbowBorder(DrawContext ctx, int x1, int y1, int x2, int y2, float t, int alpha) {
		for (int x = x1; x < x2; x += 3) {
			int xe = Math.min(x + 3, x2);
			int c1 = hsv(t * 0.5f + x * 0.004f, 0.85f, 1f, alpha);
			int c2 = hsv(t * 0.5f + x * 0.004f + 0.5f, 0.85f, 1f, alpha);
			ctx.fill(x, y1, xe, Math.min(y1 + 2, y2), c1);
			ctx.fill(x, Math.max(y2 - 2, y1), xe, y2, c2);
		}
		for (int y = y1; y < y2; y += 3) {
			int ye = Math.min(y + 3, y2);
			int c1 = hsv(t * 0.5f + y * 0.004f + 0.25f, 0.85f, 1f, alpha);
			int c2 = hsv(t * 0.5f + y * 0.004f + 0.75f, 0.85f, 1f, alpha);
			ctx.fill(x1, y, Math.min(x1 + 2, x2), ye, c1);
			ctx.fill(Math.max(x2 - 2, x1), y, x2, ye, c2);
		}
	}
}
