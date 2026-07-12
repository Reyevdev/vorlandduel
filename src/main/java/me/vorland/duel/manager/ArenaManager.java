package me.vorland.duel.manager;

import me.vorland.duel.VorlandDuel;
import me.vorland.duel.model.Arena;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import java.io.File;
import java.util.*;

public class ArenaManager {
    private final VorlandDuel plugin;
    private final Map<String, Arena> arenas = new HashMap<>();
    private final Map<UUID, Location[]> selections = new HashMap<>();

    public ArenaManager(VorlandDuel plugin) {
        this.plugin = plugin;
        loadAll();
    }

    public void setSelection(Player p, Location loc, int pos) {
        Location[] locs = selections.computeIfAbsent(p.getUniqueId(), k -> new Location[2]);
        locs[pos-1] = loc;
        p.sendMessage("§aNokta " + pos + " seçildi.");
    }

    public void createArena(String name, Player p) {
        Location[] locs = selections.get(p.getUniqueId());
        if (locs == null || locs[0] == null || locs[1] == null) {
            p.sendMessage("§cÖnce wand ile iki alan seçmelisin!");
            return;
        }
        Arena arena = new Arena(name);
        arena.setPos1(locs[0]);
        arena.setPos2(locs[1]);
        arenas.put(name, arena);
        p.sendMessage("§aArena '" + name + "' başarıyla oluşturuldu.");
    }

    public void saveAll() {
        File file = new File(plugin.getDataFolder(), "arenas.yml");
        YamlConfiguration yaml = new YamlConfiguration();
        arenas.forEach((name, arena) -> {
            String path = "arenas." + name;
            yaml.set(path + ".spawn1", arena.getSpawn1());
            yaml.set(path + ".spawn2", arena.getSpawn2());
            yaml.set(path + ".pos1", arena.getPos1());
            yaml.set(path + ".pos2", arena.getPos2());
        });
        try { yaml.save(file); } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadAll() {
        File file = new File(plugin.getDataFolder(), "arenas.yml");
        if (!file.exists()) return;
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        if (yaml.getConfigurationSection("arenas") == null) return;
        for (String key : yaml.getConfigurationSection("arenas").getKeys(false)) {
            Arena a = new Arena(key);
            a.setSpawn1(yaml.getLocation("arenas." + key + ".spawn1"));
            a.setSpawn2(yaml.getLocation("arenas." + key + ".spawn2"));
            a.setPos1(yaml.getLocation("arenas." + key + ".pos1"));
            a.setPos2(yaml.getLocation("arenas." + key + ".pos2"));
            arenas.put(key, a);
        }
    }

    public Arena getArena(String name) { return arenas.get(name); }
    public Collection<Arena> getArenas() { return arenas.values(); }
}