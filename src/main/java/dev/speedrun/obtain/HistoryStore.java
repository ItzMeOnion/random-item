package dev.speedrun.obtain;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Saves completed runs to config/obtain-speedrun-history.json. */
public final class HistoryStore {
	public static final class Entry {
		public String item;
		public long timeMs;
		public long date;

		public Entry() {}

		public Entry(String item, long timeMs, long date) {
			this.item = item;
			this.timeMs = timeMs;
			this.date = date;
		}
	}

	private static final Path FILE = FabricLoader.getInstance()
			.getConfigDir()
			.resolve("obtain-speedrun-history.json");
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	/** Oldest first. */
	public static final List<Entry> ENTRIES = new ArrayList<>();

	private HistoryStore() {}

	public static void load() {
		if (!Files.exists(FILE)) return;
		try (Reader reader = Files.newBufferedReader(FILE)) {
			Entry[] loaded = GSON.fromJson(reader, Entry[].class);
			if (loaded != null) ENTRIES.addAll(Arrays.asList(loaded));
		} catch (IOException | JsonParseException e) {
			System.err.println("[ObtainSpeedrun] Could not read history: " + e.getMessage());
		}
	}

	public static void add(Item item, long timeMs) {
		ENTRIES.add(new Entry(Registries.ITEM.getId(item).toString(), timeMs, System.currentTimeMillis()));
		save();
	}

	public static Item itemOf(Entry entry) {
		Identifier id = Identifier.tryParse(entry.item);
		if (id == null) return Items.BARRIER;
		return Registries.ITEM.get(id);
	}

	private static void save() {
		try {
			Files.createDirectories(FILE.getParent());
			Files.writeString(FILE, GSON.toJson(ENTRIES));
		} catch (IOException e) {
			System.err.println("[ObtainSpeedrun] Could not save history: " + e.getMessage());
		}
	}
}
