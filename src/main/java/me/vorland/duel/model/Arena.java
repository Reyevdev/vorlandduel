package me.vorland.duel.model;

import org.bukkit.Location;

public class Arena {
    private final String name;
    private Location spawn1, spawn2, pos1, pos2;
    private boolean busy = false;

    public Arena(String name) { this.name = name; }

    public String getName() { return name; }
    public Location getSpawn1() { return spawn1; }
    public void setSpawn1(Location spawn1) { this.spawn1 = spawn1; }
    public Location getSpawn2() { return spawn2; }
    public void setSpawn2(Location spawn2) { this.spawn2 = spawn2; }
    public Location getPos1() { return pos1; }
    public void setPos1(Location pos1) { this.pos1 = pos1; }
    public Location getPos2() { return pos2; }
    public void setPos2(Location pos2) { this.pos2 = pos2; }
    public boolean isBusy() { return busy; }
    public void setBusy(boolean busy) { this.busy = busy; }

    public boolean isReady() {
        return spawn1 != null && spawn2 != null && pos1 != null && pos2 != null;
    }
}