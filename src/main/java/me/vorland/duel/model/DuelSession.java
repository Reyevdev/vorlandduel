package me.vorland.duel.model;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;

public class DuelSession {
    private final Player p1, p2;
    private final Arena arena;
    private boolean started = false;
    private boolean frozen = false;
    
    // Ayarlar
    private final boolean elytra;
    private final boolean stairs;
    private final boolean slabs;
    private final boolean webs;
    private final boolean buttons;
    
    private final List<Location> placedBlocks = new ArrayList<>();

    public DuelSession(Player p1, Player p2, Arena arena, boolean e, boolean st, boolean sl, boolean w, boolean b) {
        this.p1 = p1; this.p2 = p2; this.arena = arena;
        this.elytra = e; 
        this.stairs = st; 
        this.slabs = sl; 
        this.webs = w;
        this.buttons = b;
    }

    public void trackBlock(Location loc) { 
        if (!placedBlocks.contains(loc)) placedBlocks.add(loc); 
    }

    public boolean isPlacedBlock(Location loc) {
        return placedBlocks.contains(loc);
    }

    public void removePlacedBlock(Location loc) {
        placedBlocks.remove(loc);
    }
    
    public void cleanupBlocks() {
        for (Location loc : placedBlocks) { 
            if (loc != null && loc.getWorld() != null) {
                loc.getBlock().setType(Material.AIR); 
            }
        }
        placedBlocks.clear();
    }

    public Player getP1() { return p1; }
    public Player getP2() { return p2; }
    public Arena getArena() { return arena; }
    public Player getOpponent(Player p) { return p.equals(p1) ? p2 : p1; }
    
    public boolean isStarted() { return started; }
    public void setStarted(boolean started) { this.started = started; }
    public boolean isFrozen() { return frozen; }
    public void setFrozen(boolean frozen) { this.frozen = frozen; }

    public boolean isElytra() { return elytra; }
    public boolean isStairs() { return stairs; }
    public boolean isSlabs() { return slabs; }
    public boolean isWebs() { return webs; }
    public boolean isButtons() { return buttons; }
}