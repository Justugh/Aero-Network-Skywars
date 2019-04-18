package net.aeronetwork.skywars.game.state;

import club.encast.enflow.game.Game;
import club.encast.enflow.game.state.GameState;
import net.aeronetwork.skywars.game.SkyWars;
import net.aeronetwork.skywars.game.player.SkyWarsPlayer;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

public class PermanentState extends GameState {

    public PermanentState() {
        super("Permanent", 420, TimeUnit.DAYS, false);
    }

    @Override
    public void onStateStart(Game game) {

    }

    @Override
    public void onStateTick(Game game) {
        SkyWars skyWars = (SkyWars) game;

        if(skyWars.getSkyWarsPlayers().stream().filter(SkyWarsPlayer::isAlive).count() <= 1 && game.getTimer().getCurrentState().getName().equalsIgnoreCase("Playing")) {
            skyWars.getTimer().switchState(game.getGameStates().size() - 1);
        }
    }

    @Override
    public void onStateEnd(Game game) {

    }

    @Override
    public void onJoin(Player player) {

    }

    @Override
    public void onLeave(Player player) {

    }
}
