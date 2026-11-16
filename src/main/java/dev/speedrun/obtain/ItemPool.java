package dev.speedrun.obtain;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class ItemPool {
	// Vanilla items that cannot be obtained in normal survival. Tune this list as you like.
	private static final Set<String> EXCLUDED = Set.of(
			"bedrock",
			"barrier",
			"command_block",
			"chain_command_block",
			"repeating_command_block",
			"structure_block",
			"structure_void",
			"jigsaw",
			"light",
			"debug_stick",
			"knowledge_book",
			"reinforced_deepslate",
			"budding_amethyst"
	);

	private ItemPool() {}

	/** Every vanilla item that a survival player can plausibly obtain. */
	public static List<Item> obtainable() {
		List<Item> out = new ArrayList<>();
		for (Item item : Registries.ITEM) {
			if (item == Items.AIR) continue;
			Identifier id = Registries.ITEM.getId(item);
			if (!"minecraft".equals(id.getNamespace())) continue;
			String path = id.getPath();
			if (EXCLUDED.contains(path) || path.contains("test")) continue;
			out.add(item);
		}
		return out;
	}

	public static boolean playerHas(ClientPlayerEntity player, Item item) {
		PlayerInventory inv = player.getInventory();
		for (int i = 0; i < inv.size(); i++) {
			if (inv.getStack(i).isOf(item)) return true;
		}
		return false;
	}
}
