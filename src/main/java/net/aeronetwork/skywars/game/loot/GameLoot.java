package net.aeronetwork.skywars.game.loot;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.IntStream;

@Getter
public class GameLoot {

    private HashMap<LootRarity, List<ItemStack>> lootMap = Maps.newHashMap();

    public GameLoot() {
        loadLoot();
    }

    private void loadLoot() {
        // TODO: Add more loot.

        // Common
        lootMap.put(LootRarity.COMMON,
                Arrays.asList(new ItemStack(Material.WOOD_SWORD), new ItemStack(Material.STICK, 2),
                        new ItemStack(Material.WOOD, 16), new ItemStack(Material.SNOW_BALL, 16),
                        new ItemStack(Material.LEATHER_HELMET),
                        new ItemStack(Material.COOKED_BEEF, 2), new ItemStack(Material.ARROW, 16),
                        new ItemStack(Material.STONE_PICKAXE), new ItemStack(Material.COBBLESTONE, 10)));

        // Uncommon
        lootMap.put(LootRarity.UNCOMMON, Arrays.asList(new ItemStack(Material.STONE_SWORD), new ItemStack(Material.LEATHER_CHESTPLATE),
                new ItemStack(Material.LEATHER_LEGGINGS), new ItemStack(Material.FLINT_AND_STEEL),
                new ItemStack(Material.CHAINMAIL_CHESTPLATE), new ItemStack(Material.CHAINMAIL_LEGGINGS), new ItemStack(Material.BOW),
                new ItemStack(Material.CHAINMAIL_HELMET), new ItemStack(Material.IRON_BOOTS), new ItemStack(Material.GOLD_LEGGINGS)));

        // Rare
        lootMap.put(LootRarity.RARE, Arrays.asList(new ItemStack(Material.IRON_SWORD), new ItemStack(Material.CHAINMAIL_BOOTS),
                new ItemStack(Material.GOLD_BOOTS), new ItemStack(Material.IRON_CHESTPLATE),
                new ItemStack(Material.IRON_HELMET), new ItemStack(Material.IRON_LEGGINGS), new ItemStack(Material.FISHING_ROD), new ItemStack(Material.TNT, 3)));

        // Epic
        lootMap.put(LootRarity.EPIC, Arrays.asList(new ItemStack(Material.DIAMOND), new ItemStack(Material.GOLD_CHESTPLATE), new ItemStack(Material.ENDER_PEARL, 2)));

        // Legendary
        lootMap.put(LootRarity.LEGENDARY, Arrays.asList(new ItemStack(Material.DIAMOND_SWORD),
                new ItemStack(Material.DIAMOND_CHESTPLATE), new ItemStack(Material.DIAMOND_BOOTS), new ItemStack(Material.DIAMOND_LEGGINGS), new ItemStack(Material.EXP_BOTTLE, 16),
                new ItemStack(Material.INK_SACK, 16, (short) 4)));
    }

    public int getRandomItemAmount() {
        int amount = new Random().nextInt(6);
        return amount < 2 ? 2 : amount;
    }

    public ItemStack getRandomLoot(LootRarity rarity) {
        return lootMap.get(rarity).get(new Random().nextInt(lootMap.get(rarity).size() - 1));
    }

    public List<ItemStack> getLoot() {
        int items = getRandomItemAmount();
        List<ItemStack> itemList = Lists.newArrayList();

        for(int i = 0; i < items; i++) {
            itemList.add(getRandomLoot(getRarity()));
        }

        return itemList;
    }

    public LootRarity getRarity() {
        List<LootRarity> rarities = Lists.newArrayList();
        for(LootRarity rarity : LootRarity.values()) {
            IntStream.range(0, rarity.getRarity()).forEach(i -> rarities.add(rarity));
        }

        Collections.shuffle(rarities);

        return rarities.get(0);
    }

    enum LootRarity {
        COMMON(200),
        UNCOMMON(100),
        RARE(50),
        EPIC(25),
        LEGENDARY(5);

        int rarity;

        LootRarity(int rarity) {
            this.rarity = rarity;
        }

        int getRarity() {
            return rarity;
        }
    }

}
