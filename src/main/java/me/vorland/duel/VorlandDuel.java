package me.vorland.duel;

import me.vorland.duel.commands.*;
import me.vorland.duel.listeners.*;
import me.vorland.duel.manager.*;
import org.bukkit.plugin.java.JavaPlugin;

public class VorlandDuel extends JavaPlugin {
    private ArenaManager arenaManager;
    private DuelManager duelManager;
    private DuelLootManager lootManager;

    @Override
    public void onEnable() {
        this.arenaManager = new ArenaManager(this);
        this.duelManager = new DuelManager(this);
        this.lootManager = new DuelLootManager(this);

        getCommand("vorlandduel").setExecutor(new AdminCommand(this));
        getCommand("duel").setExecutor(new DuelCommand(this));
        
        getServer().getPluginManager().registerEvents(new DuelListener(this), this);
        
        getLogger().info("VorlandDuel v1.1 Aktif! (Sandık Sistemi Eklendi)");
    }

    @Override
    public void onDisable() {
        if (arenaManager != null) {
            arenaManager.saveAll();
        }
    }

    public ArenaManager getArenaManager() { return arenaManager; }
    public DuelManager getDuelManager() { return duelManager; }
    public DuelLootManager getLootManager() { return lootManager; }
}