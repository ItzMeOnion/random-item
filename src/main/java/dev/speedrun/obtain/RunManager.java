package dev.speedrun.obtain;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Owns the state of the current speedrun: spin -> reveal -> timer -> done. */
public final class RunManager {
	public enum Phase { SPIN, REVEAL, RUNNING, DONE }

	public static final long SPIN_MS = 5500L;
	public static final long REVEAL_MS = 4500L;
	public static final long DONE_MS = 6000L;

	private static final Random RNG = new Random();

	private static boolean pending;
	private static boolean active;
	private static Phase phase = Phase.SPIN;
	private static long phaseStartNs;
	private static long timerStartNs;
	private static long finalMs;
	private static Item target;
	private static ItemStack targetStack = ItemStack.EMPTY;
	private static List<Item> spinPool = new ArrayList<>();

	private RunManager() {}

	public static void onJoin(MinecraftClient client) {
		if (!client.isInSingleplayer()) return;
		pending = true;
		active = false;
	}

	public static void onDisconnect() {
		pending = false;
		active = false;
	}

	public static void tick(MinecraftClient client) {
		ClientPlayerEntity player = client.player;
		if (player == null || client.world == null) return;

		if (pending) {
			pending = false;
			startRun(player);
		}
		if (!active) return;

		long now = System.nanoTime();
		long elapsed = nsToMs(now - phaseStartNs);

		switch (phase) {
			case SPIN -> {
				if (elapsed >= SPIN_MS) setPhase(Phase.REVEAL, now);
			}
			case REVEAL -> {
				if (elapsed >= REVEAL_MS) {
					timerStartNs = now;
					setPhase(Phase.RUNNING, now);
				}
			}
			case RUNNING -> {
				if (ItemPool.playerHas(player, target)) {
					finalMs = nsToMs(now - timerStartNs);
					setPhase(Phase.DONE, now);
					HistoryStore.add(target, finalMs);
				}
			}
			case DONE -> { }
		}
	}

	private static void startRun(ClientPlayerEntity player) {
		List<Item> pool = ItemPool.obtainable();
		pool.removeIf(item -> ItemPool.playerHas(player, item)); // don't hand out something already owned
		if (pool.isEmpty()) return;

		target = pool.get(RNG.nextInt(pool.size()));
		targetStack = new ItemStack(target);
		spinPool = new ArrayList<>(pool);
		Collections.shuffle(spinPool, RNG);

		finalMs = 0L;
		timerStartNs = 0L;
		active = true;
		setPhase(Phase.SPIN, System.nanoTime());
	}

	private static void setPhase(Phase next, long nowNs) {
		phase = next;
		phaseStartNs = nowNs;
	}

	private static long nsToMs(long ns) {
		return ns / 1_000_000L;
	}

	public static boolean isActive() { return active; }

	public static Phase phase() { return phase; }

	public static long phaseElapsedMs() { return nsToMs(System.nanoTime() - phaseStartNs); }

	public static long currentRunMs() {
		if (phase == Phase.RUNNING) return nsToMs(System.nanoTime() - timerStartNs);
		return finalMs;
	}

	public static ItemStack targetStack() { return targetStack; }

	public static String targetName() { return targetStack.getName().getString(); }

	public static List<Item> spinPool() { return spinPool; }
}
