package me.vorland.duel.commands;

import me.vorland.duel.VorlandDuel;
import me.vorland.duel.model.Arena;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.List;

public class AdminCommand implements CommandExecutor, TabCompleter {
    private final VorlandDuel plugin;
    public AdminCommand(VorlandDuel plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;
        if (!p.hasPermission("vorlandduel.admin")) return true;

        if (args.length == 1 && args[0].equalsIgnoreCase("wand")) {
            ItemStack wand = new ItemStack(Material.STICK);
            ItemMeta meta = wand.getItemMeta();
            meta.setDisplayName("§bVorlandDuel Wand");
            meta.setLore(List.of("§7Sol tık: §f1. Nokta", "§7Sağ tık: §f2. Nokta"));
            wand.setItemMeta(meta);
            p.getInventory().addItem(wand);
            p.sendMessage("§aWand verildi! Alanları seçmeye başla.");
            return true;
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            String name = args[1];
            Arena arena = plugin.getArenaManager().getArena(name);

            if (sub.equals("create")) {
                plugin.getArenaManager().createArena(name, p);
                return true;
            }

            if (arena == null) { p.sendMessage("§cArena bulunamadı!"); return true; }

            if (sub.equals("spawn1")) {
                arena.setSpawn1(p.getLocation());
                p.sendMessage("§aSpawn 1 ayarlandı.");
            } else if (sub.equals("spawn2")) {
                arena.setSpawn2(p.getLocation());
                p.sendMessage("§aSpawn 2 ayarlandı.");
            }
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender s, Command c, String a, String[] args) {
        if (args.length == 1) return List.of("wand", "create", "spawn1", "spawn2");
        return null;
    }
}