package net.aeronetwork.skywars;

import club.encast.enflow.Enflow;
import club.encast.enflow.game.load.balancer.ConnectionLoadBalancer;
import club.encast.enflow.game.load.impl.JoinLoad;
import club.encast.enflow.game.load.impl.LeaveLoad;
import net.aeronetwork.skywars.game.SkyWars;
import org.bukkit.plugin.java.JavaPlugin;

public class SkyWarsCore extends JavaPlugin {

    public static SkyWarsCore INSTANCE;

    @Override
    public void onEnable() {
        INSTANCE = this;

        Enflow.GAME_MANAGER.registerGame(SkyWars.class);
        Enflow.GAME_MANAGER.startGame(SkyWars.class);

        Enflow.GAME_MANAGER.setLoadBalancer(new ConnectionLoadBalancer() {
            @Override
            public void onJoin(JoinLoad load) {
                load.setGame(Enflow.GAME_MANAGER.getGameHandlers().get(0).getGame());
            }

            @Override
            public void onLeave(LeaveLoad load) {

            }
        });
    }

    @Override
    public void onDisable() {

    }
}
