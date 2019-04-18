package net.aeronetwork.skywars.game.state.listener;

import club.encast.enflow.spectate.impl.DefaultSpectatePlayer;
import com.google.common.collect.Lists;
import net.aeronetwork.core.util.FM;
import net.aeronetwork.skywars.SkyWarsCore;
import net.aeronetwork.skywars.game.SkyWars;
import net.aeronetwork.skywars.game.player.SkyWarsPlayer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.EnderPearl;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.List;
import java.util.Random;

public class GameListener implements Listener {

    private SkyWars game;

    private List<Location> placedChests = Lists.newArrayList();

    public GameListener(SkyWars game) {
        this.game = game;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if(game.getTimer().getCurrentLengthSeconds() <= 5)
            event.setCancelled(true);

        if(!event.isCancelled() && (event.getBlock().getType().equals(Material.CHEST) || event.getBlock().getType().equals(Material.TRAPPED_CHEST))) {
            placedChests.add(event.getBlock().getLocation());
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if(game.getTimer().getCurrentLengthSeconds() <= 5)
            event.setCancelled(true);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if(!game.isPvpEnabled())
            event.setCancelled(true);
    }

    @EventHandler
    public void onEnderPearlTeleport(EntityDamageByEntityEvent event) {
        if(event.getDamager() instanceof EnderPearl) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if(event.getTo().getY() < 0) {
            event.getPlayer().damage(event.getPlayer().getMaxHealth());
        }
    }

    @EventHandler
    public void onKill(PlayerDeathEvent event) {
        if(game.getSWPlayerByUUID(event.getEntity().getUniqueId()) != null) {
            game.getSWPlayerByUUID(event.getEntity().getUniqueId()).setAlive(false);

            if(event.getEntity().getKiller() != null && game.getSWPlayerByUUID(event.getEntity().getKiller().getUniqueId()) != null) {
                SkyWarsPlayer killer = game.getSWPlayerByUUID(event.getEntity().getKiller().getUniqueId());
                killer.setKills(killer.getKills() + 1);
                killer.setEarnedCoins(killer.getEarnedCoins() + 25);
                killer.setEarnedExp(killer.getEarnedExp() + 25);
                event.getEntity().getKiller().sendMessage("§e+§c25 §eExp for killing §c" + event.getEntity().getName());
                event.setDeathMessage(FM.mainFormat("Death", "§c" + event.getEntity().getName() + " §ewas murdered by §c" + event.getEntity().getKiller().getName() + "§e."));
            } else {
                // TODO: Add fancy messages for every single damage cause. lol
                event.setDeathMessage(FM.mainFormat("Death", "§c" + event.getEntity().getName() + " §ehas died!"));
            }

            Bukkit.getScheduler().scheduleSyncDelayedTask(SkyWarsCore.INSTANCE, () -> event.getEntity().spigot().respawn(), 1); // Auto respawn
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        if(game.getSWPlayerByUUID(event.getPlayer().getUniqueId()) != null && !game.getSWPlayerByUUID(event.getPlayer().getUniqueId()).isAlive()) {
            if(event.getPlayer().getKiller() != null) {
                event.setRespawnLocation(event.getPlayer().getKiller().getLocation());
            } else {
                event.setRespawnLocation(game.getSWPlayerByUUID(event.getPlayer().getUniqueId()).getSpawn());
            }
            event.getPlayer().setGameMode(GameMode.SPECTATOR);
            game.getSpectateManager().addSpectator(new DefaultSpectatePlayer(event.getPlayer().getUniqueId()));
        }
    }

    @EventHandler
    public void onChestOpen(PlayerInteractEvent event) {
        if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && (event.getClickedBlock().getType().equals(Material.CHEST)
                || event.getClickedBlock().getType().equals(Material.TRAPPED_CHEST))
                && !placedChests.contains(event.getClickedBlock().getLocation())) {
            if(!event.getClickedBlock().hasMetadata("opened") || game.isRefill()) {
                BlockState chestState = event.getClickedBlock().getState();
                if (chestState instanceof Chest) {
                    Chest chest = (Chest) chestState;
                    Inventory inventory = chest.getInventory();
                    generateLoot(inventory, game.getSWPlayerByUUID(event.getPlayer().getUniqueId()));
                    event.getClickedBlock().setMetadata("opened", new FixedMetadataValue(SkyWarsCore.INSTANCE, "true`"));
                }
            }
        }
    }

    // TODO: Improve this too, lol.
    public void generateLoot(Inventory inventory, SkyWarsPlayer player) {
        if(inventory.getHolder() instanceof DoubleChest) { // Fix double chests.
            DoubleChest doubleChest = (DoubleChest) inventory.getHolder();
            Inventory doubleInventory = doubleChest.getInventory();
            doubleInventory.clear();
            game.getGameLoot().getLoot().forEach(item -> {
                int slot = new Random().nextInt(doubleInventory.getSize() - 1);
                while(doubleInventory.getItem(slot) != null) {
                    slot = new Random().nextInt(doubleInventory.getSize() - 1);
                }
                if(!player.isHasOpenedChest()) {
                    player.setHasOpenedChest(true);
                    doubleInventory.setItem(slot, new ItemStack(Material.WOOD, 16));
                } else {
                    doubleInventory.setItem(slot, item);
                }
            });
        } else {
            inventory.clear();
            game.getGameLoot().getLoot().forEach(item -> {
                int slot = new Random().nextInt(inventory.getSize() - 1);
                while(inventory.getItem(slot) != null) {
                    slot = new Random().nextInt(inventory.getSize() - 1);
                }
                if(!player.isHasOpenedChest()) {
                    player.setHasOpenedChest(true);
                    inventory.setItem(slot, new ItemStack(Material.WOOD, 16));
                } else {
                    inventory.setItem(slot, item);
                }
            });
        }
    }

}
