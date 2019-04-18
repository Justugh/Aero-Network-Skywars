package net.aeronetwork.skywars.game.state;

import club.encast.enflow.Enflow;
import club.encast.enflow.example.util.PlayerUtil;
import club.encast.enflow.game.Game;
import club.encast.enflow.game.state.GameState;
import club.encast.enflow.scoreboard.GScoreboard;
import club.encast.enflow.version.packet.out.playerlist.PacketPlayerList;
import club.encast.enflow.version.packet.out.title.PacketTitle;
import net.aeronetwork.core.AeroCore;
import net.aeronetwork.core.player.AeroPlayer;
import net.aeronetwork.core.util.FM;
import net.aeronetwork.skywars.SkyWarsCore;
import net.aeronetwork.skywars.game.SkyWars;
import net.aeronetwork.skywars.game.map.TestMap;
import net.aeronetwork.skywars.game.player.SkyWarsPlayer;
import net.aeronetwork.skywars.game.state.listener.LobbyListener;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.scoreboard.Team;

import java.util.concurrent.TimeUnit;

public class LobbyState extends GameState {

    private SkyWars game;
    private GScoreboard scoreboard;

    private LobbyListener lobbyListener;

    public LobbyState() {
        super("Lobby", 30, TimeUnit.SECONDS, true);

        scoreboard = new GScoreboard("§c§lSkyWars")
                .addLine(" ")
                .addLine("§ePlayers §8» §c0§8/§c0")
                .addLine("  ")
                .addLine("§eMap §8» §cNone")
                .addLine("   ")
                .addLine("§eStarting soon...")
                .addLine("    ");

        Team team = scoreboard.getScoreboard().registerNewTeam("color");
        team.setPrefix("§a");

        lobbyListener = new LobbyListener();
    }

    @Override
    public void onStateStart(Game game) {
        this.game = (SkyWars) game;

        scoreboard.setLine(3, "§eMap §8» §c" + game.getMaps().get(0).getMapName());

        Bukkit.getServer().getPluginManager().registerEvents(lobbyListener, SkyWarsCore.INSTANCE);
    }

    @Override
    public void onStateTick(Game game) {
        if(this.game.getSkyWarsPlayers().size() >= this.game.getSettings().getMinPlayers()) {
            if(isInfinite()) {
                setInfinite(false);
                this.game.getTimer().setCurrentLengthSeconds(0);
            }

            if(game.getTimer().getRemainingTime(TimeUnit.SECONDS) <= 10) {
                this.game.getSkyWarsPlayers().forEach(player -> Enflow.OPERATION_LIBRARY.getOperation(PacketTitle.class)
                        .send(player.getPlayer(), "", "§eStarting in §c" + game.getTimer().getRemainingTime(TimeUnit.SECONDS) + "§e seconds...", 0, 25, 10));
            }
        } else if(!isInfinite()) {
            setInfinite(true);
            setLength(30);
            Bukkit.broadcastMessage(FM.mainFormat("SkyWars", "Not enough players..."));
        }

        scoreboard.setLine(1, "§ePlayers §8» §c" + this.game.getSkyWarsPlayers().size() + "§8/§c" + this.game.getSettings().getMaxPlayers());
        scoreboard.setLine(5, isInfinite() ? "§eStarting soon..." : "§eStarting in §c" + game.getTimer().getRemainingTime(TimeUnit.SECONDS) + "s");
    }

    @Override
    public void onStateEnd(Game game) {
        HandlerList.unregisterAll(lobbyListener);
    }

    @Override
    public void onJoin(Player player) {
        AeroPlayer aeroPlayer = AeroCore.PLAYER_MANAGER.getPlayer(player.getUniqueId());
        TestMap map = (TestMap) game.getMaps().get(0); // TODO: Support multiple maps.

        game.getSkyWarsPlayers().add(new SkyWarsPlayer(player.getUniqueId()));
        game.getSWPlayerByUUID(player.getUniqueId()).setSpawn(map.getRandomSpawn());
        game.getSkyWarsPlayers().forEach(skyWarsPlayer ->
                skyWarsPlayer.getPlayer().sendMessage(
                        "§8(§c" + game.getSkyWarsPlayers().size() + "§8/§c" + game.getSettings().getMaxPlayers() + "§8) §8» " +
                                (aeroPlayer.isDisguised() ? aeroPlayer.getDisguiseData().getRank().getColor() : aeroPlayer.getRank().getColor()) + player.getName() + " has joined."));
        Enflow.OPERATION_LIBRARY.getOperation(PacketPlayerList.class)
                .send(player, "§c§lSkyWars \n ", " \n §eBuy fancy ranks at §cstore.aeronetwork.net");

        player.setGameMode(GameMode.SURVIVAL);
        PlayerUtil.healPlayer(player);
        PlayerUtil.clearInventory(player);

        player.teleport(map.getLobbySpawn());

        scoreboard.getScoreboard().getTeam("color").addEntry(player.getName());

        player.setScoreboard(scoreboard.getScoreboard());

        game.getSpectateManager().addGlobalViewablePlayer(player.getUniqueId());
    }

    @Override
    public void onLeave(Player player) {
        AeroPlayer aeroPlayer = AeroCore.PLAYER_MANAGER.getPlayer(player.getUniqueId());
        TestMap map = (TestMap) game.getMaps().get(0); // TODO: Support multiple maps.

        map.getSpawns().add(game.getSWPlayerByUUID(player.getUniqueId()).getSpawn());

        game.getSkyWarsPlayers().remove(game.getSWPlayerByUUID(player.getUniqueId()));
        game.getSkyWarsPlayers().forEach(skyWarsPlayer ->
                skyWarsPlayer.getPlayer().sendMessage(
                        "§8(§c" + game.getSkyWarsPlayers().size() + "§8/§c" + game.getSettings().getMaxPlayers() + "§8) §8» " +
                                (aeroPlayer.isDisguised() ? aeroPlayer.getDisguiseData().getRank().getColor() : aeroPlayer.getRank().getColor()) + player.getName() + " has quit."));
    }
}
