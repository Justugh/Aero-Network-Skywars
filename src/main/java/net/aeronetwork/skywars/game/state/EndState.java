package net.aeronetwork.skywars.game.state;

import club.encast.enflow.Enflow;
import club.encast.enflow.game.Game;
import club.encast.enflow.game.state.GameState;
import club.encast.enflow.version.packet.out.title.PacketTitle;
import net.aeronetwork.core.AeroCore;
import net.aeronetwork.core.redis.listener.ListenerComponent;
import net.aeronetwork.core.server.AeroServer;
import net.aeronetwork.skywars.SkyWarsCore;
import net.aeronetwork.skywars.game.SkyWars;
import net.aeronetwork.skywars.game.player.SkyWarsPlayer;
import net.aeronetwork.skywars.game.state.listener.LobbyListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.TimeUnit;

public class EndState extends GameState {

    private LobbyListener lobbyListener;

    public EndState() {
        super("End", 10, TimeUnit.SECONDS, false);

    }

    @Override
    public void onStateStart(Game game) {
        AeroCore.SERVER_MANAGER.getServerSettings().setJoinState(AeroServer.JoinState.NOT_JOINABLE);
        Bukkit.getServer().getPluginManager().registerEvents(lobbyListener = new LobbyListener(), SkyWarsCore.INSTANCE);

        SkyWars skyWars = (SkyWars) game;
        SkyWarsPlayer winner = skyWars.getSkyWarsPlayers().stream().filter(player -> player.isAlive()).findFirst().orElse(null);
        winner.setEarnedCoins(winner.getEarnedCoins() + 500);
        winner.setEarnedExp(winner.getEarnedExp() + 250);
        Bukkit.getOnlinePlayers().forEach(player -> {
            Enflow.OPERATION_LIBRARY.getOperation(PacketTitle.class).send(player, "§c" + winner.getPlayer().getName(), "§ewon the game!", 0, 60, 10);
            player.sendMessage("§8§m--------------------");
            player.sendMessage("§c§lGame Stats");
            player.sendMessage("");
            player.sendMessage("§eWinner: §c" + winner.getPlayer().getName());
            player.sendMessage("");
            player.sendMessage("§eKills: §c" + (skyWars.getSWPlayerByUUID(player.getUniqueId()) != null ? skyWars.getSWPlayerByUUID(player.getUniqueId()).getKills() + "" : "0"));
            player.sendMessage("§eEarned Coins: §c" + (skyWars.getSWPlayerByUUID(player.getUniqueId()) != null ? skyWars.getSWPlayerByUUID(player.getUniqueId()).getEarnedCoins() + "" : "0"));
            player.sendMessage("§eEarned Exp: §c" + (skyWars.getSWPlayerByUUID(player.getUniqueId()) != null ? skyWars.getSWPlayerByUUID(player.getUniqueId()).getEarnedExp() + "" : "0"));
            player.sendMessage("");
            player.sendMessage("§8§m--------------------");
        });
        AeroCore.EXPERIENCE_MANAGER.addExperience(AeroCore.PLAYER_MANAGER.getPlayer(winner.getUuid()), winner.getEarnedExp());
        AeroCore.PLAYER_MANAGER.getPlayer(winner.getUuid()).updateCoins(AeroCore.PLAYER_MANAGER.getPlayer(winner.getUuid()).getCoins() + winner.getEarnedCoins());
    }

    @Override
    public void onStateTick(Game game) {
    }

    @Override
    public void onStateEnd(Game game) {
        Bukkit.getOnlinePlayers().forEach(player -> AeroCore.REDIS_MANAGER.sendListenerMessage(new ListenerComponent("connect", player.getUniqueId() + " id arcade_lounge")));
        new BukkitRunnable() {
            @Override
            public void run() {
                HandlerList.unregisterAll(lobbyListener);
                Bukkit.shutdown();
            }
        }.runTaskLater(SkyWarsCore.INSTANCE, 20 * 5);
    }

    @Override
    public void onJoin(Player player) {
        player.kickPlayer("§cThis game has already ended.");
    }

    @Override
    public void onLeave(Player player) {

    }
}
