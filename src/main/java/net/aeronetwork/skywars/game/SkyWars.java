package net.aeronetwork.skywars.game;

import club.encast.enflow.example.util.ServerPropertiesUtil;
import club.encast.enflow.game.Game;
import club.encast.enflow.game.handler.GameHandler;
import club.encast.enflow.game.settings.GameSettings;
import club.encast.enflow.spectate.SpectateManager;
import club.encast.enflow.spectate.impl.DefaultSpectateManager;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import net.aeronetwork.skywars.game.loot.GameLoot;
import net.aeronetwork.skywars.game.map.TestMap;
import net.aeronetwork.skywars.game.player.SkyWarsPlayer;
import net.aeronetwork.skywars.game.state.EndState;
import net.aeronetwork.skywars.game.state.LobbyState;
import net.aeronetwork.skywars.game.state.PermanentState;
import net.aeronetwork.skywars.game.state.PlayState;
import org.bukkit.Bukkit;

import java.util.List;
import java.util.UUID;

@Getter
public class SkyWars extends Game {

    private List<SkyWarsPlayer> skyWarsPlayers = Lists.newArrayList();

    @Setter
    private boolean refill = false;
    @Setter
    private boolean pvpEnabled = false;

    private GameLoot gameLoot;

    private SpectateManager spectateManager = new DefaultSpectateManager();

    public SkyWars(GameHandler handler) {
        super(handler);

        gameLoot = new GameLoot();

        setName("SkyWars");

        ServerPropertiesUtil.setServerProperty(
                ServerPropertiesUtil.ServerPropertyType.ANNOUNCE_PLAYER_ACHIEVEMENTS,
                false
        );

        Bukkit.getWorld("skywars_test").setGameRuleValue("doMobSpawning", "false");
        Bukkit.getWorld("skywars_test").setGameRuleValue("mobGriefing", "false");

        setSettings(new GameSettings());
        getSettings().setMaxPlayers(8);
        getSettings().setMinPlayers(2);

        // Add test map
        getMaps().add(new TestMap());

        getGameStates().add(LobbyState.class);
        getGameStates().add(PlayState.class);
        getGameStates().add(EndState.class);

        setPermanentState(new PermanentState());
    }

    public SkyWarsPlayer getSWPlayerByUUID(UUID uuid) {
        return getSkyWarsPlayers().stream().filter(player -> player.getUuid().equals(uuid)).findFirst().orElse(null);
    }

}
