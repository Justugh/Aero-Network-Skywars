package net.aeronetwork.skywars.game.map;

import club.encast.enflow.game.map.GameMap;
import com.google.common.collect.Lists;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class TestMap extends GameMap {

    @Getter
    private List<Location> spawns = Lists.newArrayList();

    public TestMap() {
        super("Sanctuary", Arrays.asList("Hushi"), null);
        loadSpawns();
    }

    public void loadSpawns() {
        spawns.add(new Location(Bukkit.getWorld("world"), -20.5, 66, -33.5));
        spawns.add(new Location(Bukkit.getWorld("world"), -36.5, 66, -15.5));
        spawns.add(new Location(Bukkit.getWorld("world"), -34.5, 66, 19.5));
        spawns.add(new Location(Bukkit.getWorld("world"), -20.5, 66, 32.5));
        spawns.add(new Location(Bukkit.getWorld("world"), 27.5, 66, 32));
        spawns.add(new Location(Bukkit.getWorld("world"), 43.5, 66, 14.5));
        spawns.add(new Location(Bukkit.getWorld("world"), 41.5, 66, -20.5));
        spawns.add(new Location(Bukkit.getWorld("world"), 27.5, 66, -34.5));
    }

    public Location getRandomSpawn() {
        Location spawn = spawns.get(new Random().nextInt(spawns.size()));
        spawns.remove(spawn);
        return spawn;
    }

    public Location getLobbySpawn() {
        return new Location(Bukkit.getWorld("world"), 0.5, 65, 0.5);
    }

}
