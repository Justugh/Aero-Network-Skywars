package net.aeronetwork.skywars.game.state;

import club.encast.enflow.example.util.PlayerUtil;
import club.encast.enflow.example.util.Util;
import club.encast.enflow.game.Game;
import club.encast.enflow.game.state.GameState;
import club.encast.enflow.scoreboard.GScoreboard;
import club.encast.enflow.spectate.impl.DefaultSpectatePlayer;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.aeronetwork.core.util.FM;
import net.aeronetwork.skywars.SkyWarsCore;
import net.aeronetwork.skywars.game.SkyWars;
import net.aeronetwork.skywars.game.state.listener.GameListener;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PlayState extends GameState {

    private SkyWars game;
    private GameListener gameListener;

    private HashMap<UUID, GScoreboard> scoreboardHashMap = Maps.newHashMap(); // Sadly this is need because the 'kills'
    private List<Location> cageBlocks = Lists.newArrayList();

    public PlayState() {
        super("Playing", 10, TimeUnit.MINUTES, true);
    }

    public void setGameScoreboard(Player player) {
        GScoreboard gameBoard = new GScoreboard("     §c§lSkyWars     ")
                .addLine(" ")
                .addLine("§ePlayers Alive §8» §c0")
                .addLine("  ")
                .addLine("§eNext Refill §8» §c0:00")
                .addLine("   ")
                .addLine("§eKills §8» §c0")
                .addLine("    ")
                .addLine("§7play.aeronetwork.net");

        Team team = gameBoard.getScoreboard().registerNewTeam("color");
        team.setPrefix("§e");

        scoreboardHashMap.put(player.getUniqueId(), gameBoard);
        player.setScoreboard(gameBoard.getScoreboard());
    }

    @Override
    public void onStateStart(Game game) {
        this.game = (SkyWars) game;

        this.game.setPvpEnabled(false);
        Bukkit.broadcastMessage(FM.mainFormat("SkyWars", "The game has started!"));
        Bukkit.getOnlinePlayers().forEach(player -> {
            setGameScoreboard(player);
            player.playSound(player.getLocation(), Sound.LEVEL_UP, 1f, 1f);
        });

        for(GScoreboard scoreboard : scoreboardHashMap.values()) {
            for(Player player : Bukkit.getOnlinePlayers()) {
                scoreboard.getScoreboard().getTeam("color").addEntry(player.getName());
            }
        }

        // Cages
        this.game.getSkyWarsPlayers().forEach(player -> {
            Location spawn = player.getSpawn();
            for(int x = -1; x <= 1; x++) {
                for(int y = -1; y <= 4; y++) {
                    for(int z = -1; z <= 1; z++) {
                        if(!(x == 0 && y != -1 && z == 0)) {
                            Block block = spawn.getWorld().getBlockAt(spawn.clone().add(x, y, z));
                            block.setType(Material.STAINED_GLASS);
                            block.getState().setType(Material.STAINED_GLASS);
                            block.getState().update();
                            this.cageBlocks.add(block.getLocation());
                        }
                    }
                }
            }
            player.getPlayer().teleport(player.getSpawn());
        });

        Bukkit.broadcastMessage(FM.mainFormat("SkyWars", "Cages will open in 5 seconds!"));
        Bukkit.getServer().getPluginManager().registerEvents(gameListener = new GameListener(this.game), SkyWarsCore.INSTANCE);
    }

    @Override
    public void onStateTick(Game game) {
        // Update scoreboard data
        scoreboardHashMap.keySet().forEach(uuid -> {
            scoreboardHashMap.get(uuid).setLine(1, "§ePlayers Alive §8» §c" + this.game.getSkyWarsPlayers().stream().filter(player -> player.isAlive()).count());
            if(this.game.getSWPlayerByUUID(uuid) != null) {scoreboardHashMap.get(uuid).setLine(5, "§eKills §8» §c" + this.game.getSWPlayerByUUID(uuid).getKills());}
        });

        if(game.getTimer().getCurrentLengthSeconds() < 5) {
            Bukkit.broadcastMessage(FM.mainFormat("SkyWars", "Cages open in " +
                    (5 - game.getTimer().getCurrentLengthSeconds()) + " second(s)!"));
        }

        if(game.getTimer().getCurrentLengthSeconds() == 5) {
            this.cageBlocks.forEach(loc -> {
                Block block = loc.getWorld().getBlockAt(loc);
                block.setType(Material.AIR);
                block.getState().setType(Material.AIR);
                block.getState().update();
            });
            Bukkit.getOnlinePlayers().forEach(player -> Bukkit.getOnlinePlayers().forEach(target -> {
                if(target != player) {
                    player.hidePlayer(target);
                    player.showPlayer(target);
                }
            }));
            Bukkit.broadcastMessage(FM.mainFormat("SkyWars", "Cages have opened! Good luck!"));
        }

        // To prevent fall damage
        if(game.getTimer().getCurrentLengthSeconds() == 7)
            this.game.setPvpEnabled(true);

        if(game.getTimer().getRemainingTime(TimeUnit.SECONDS) == 300) {
            this.game.setRefill(true);
            Bukkit.broadcastMessage(FM.mainFormat("SkyWars", "Chests have been refilled!"));
            scoreboardHashMap.keySet().forEach(uuid -> scoreboardHashMap.get(uuid).setLine(3, "§eNext Refill §8» §cNo Refill"));
        }

        if(game.getTimer().getRemainingTime(TimeUnit.SECONDS) > 300) {
            scoreboardHashMap.keySet().forEach(uuid -> scoreboardHashMap.get(uuid).setLine(3, "§eNext Refill §8» §c" + Util.convertToMinuteSecond(game.getTimer().getRemainingTime(TimeUnit.SECONDS) / 2)));
        }
    }

    @Override
    public void onStateEnd(Game game) {
        HandlerList.unregisterAll(gameListener);
    }

    @Override
    public void onJoin(Player player) {
        setGameScoreboard(player);
        for(Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            scoreboardHashMap.get(player.getUniqueId()).getScoreboard().getTeam("color").addEntry(onlinePlayer.getName());
        }
        PlayerUtil.clearInventory(player);
        PlayerUtil.healPlayer(player);
        player.setGameMode(GameMode.SPECTATOR);
        game.getSpectateManager().addGlobalViewablePlayer(player.getUniqueId());
        game.getSpectateManager().addSpectator(new DefaultSpectatePlayer(player.getUniqueId()));
        player.sendMessage(FM.mainFormat("SkyWars", "This game is currently in progress. You're now a spectator."));
    }

    @Override
    public void onLeave(Player player) {
        if(this.game.getSWPlayerByUUID(player.getUniqueId()).isAlive()) {
            this.game.getSWPlayerByUUID(player.getUniqueId()).setAlive(false);
            for(ItemStack stack : player.getInventory().getContents()) {
                player.getWorld().dropItemNaturally(player.getLocation(), stack);
            }
            for(ItemStack stack : player.getInventory().getArmorContents()) {
                player.getWorld().dropItemNaturally(player.getLocation(), stack);
            }
        }
    }
}
