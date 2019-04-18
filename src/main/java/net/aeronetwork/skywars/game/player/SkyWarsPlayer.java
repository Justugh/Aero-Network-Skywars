package net.aeronetwork.skywars.game.player;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

@Getter @Setter
public class SkyWarsPlayer {

    @Setter(AccessLevel.NONE)
    private UUID uuid;
    private long earnedCoins;
    private long earnedExp;
    private long kills;
    private boolean alive = true;
    private Location spawn;
    private boolean hasOpenedChest = false;

    public SkyWarsPlayer(UUID uuid) {
        this.uuid = uuid;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

}
