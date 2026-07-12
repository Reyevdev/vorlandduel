package me.vorland.duel.commands;

import me.vorland.duel.VorlandDuel;
import me.vorland.duel.model.Arena;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DuelCommand implements CommandExecutor, TabCompleter {
    private final VorlandDuel plugin;
    public DuelCommand(VorlandDuel plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player p = (Player) sender;

        if (args.length == 0) {
            p.sendMessage("§e---------- §6Vorland Duel §e----------");
            p.sendMessage("§b/duel invite <isim>");
            p.sendMessage("§b/duel accept <isim>");
            p.sendMessage("§b/duel sandık §7- Kazanılan eşyaları gör");
            return true;
        }

        if (args[0].equalsIgnoreCase("sandık") || args[0].equalsIgnoreCase("chest")) {
            plugin.getLootManager().openLootMenu(p);
            return true;
        }

        if (args[0].equalsIgnoreCase("invite")) {
            if (args.length < 2) { p.sendMessage("§cİsim gir."); return true; }
            Player t = Bukkit.getPlayer(args[1]);
            if (t == null || t.equals(p)) { p.sendMessage("§cOyuncu yok."); return true; }
            openSettings(p, t);
            return true;
        }

        if (args[0].equalsIgnoreCase("accept")) {
            if (args.length < 2) { p.sendMessage("§cİsim gir."); return true; }
            Player t = Bukkit.getPlayer(args[1]);
            if (t == null) { p.sendMessage("§cOyuncu yok."); return true; }
            plugin.getDuelManager().acceptInvite(p, t);
            return true;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) return Arrays.asList("invite", "accept", "sandık");
        return null; 
    }

    private void openSettings(Player p, Player t) {
        Inventory gui = Bukkit.createInventory(null, 45, "Düello Ayarları: " + t.getName());
        
        ItemStack glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta gm = glass.getItemMeta(); gm.setDisplayName(" "); glass.setItemMeta(gm);
        for(int i=0; i<45; i++) gui.setItem(i, glass);

        gui.setItem(11, item(Material.ELYTRA, "§6Elitra", "§cKAPALI"));
        gui.setItem(12, item(Material.STONE_BUTTON, "§6Buton", "§aAÇIK")); 
        gui.setItem(13, item(Material.LADDER, "§6Merdiven", "§aAÇIK"));
        gui.setItem(14, item(Material.STONE_SLAB, "§6Slab", "§aAÇIK"));
        gui.setItem(15, item(Material.COBWEB, "§6Örümcek Ağı", "§aAÇIK"));

        String map = "Müsait Yok";
        List<Arena> list = plugin.getArenaManager().getArenas().stream()
                .filter(a->!a.isBusy() && a.isReady()).collect(Collectors.toList());
        if(!list.isEmpty()) map = list.get(0).getName();
        
        gui.setItem(22, item(Material.MAP, "§eHarita: §f" + map, "§7Değiştir"));

        ItemStack send = new ItemStack(Material.LIME_DYE);
        ItemMeta sm = send.getItemMeta(); sm.setDisplayName("§a§lDAVETİ GÖNDER");
        send.setItemMeta(sm);
        gui.setItem(31, send); 

        p.openInventory(gui);
    }

    private ItemStack item(Material m, String n, String s) {
        ItemStack i = new ItemStack(m);
        ItemMeta meta = i.getItemMeta();
        meta.setDisplayName(n);
        meta.setLore(Arrays.asList("§7Durum: " + s, "§eTıkla Değiştir"));
        i.setItemMeta(meta);
        return i;
    }
}