package me.vorland.duel.manager;

import me.vorland.duel.VorlandDuel;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class DuelLootManager {

    private final VorlandDuel plugin;
    private File file;
    private FileConfiguration config;

    public DuelLootManager(VorlandDuel plugin) {
        this.plugin = plugin;
        createFile();
    }

    public void saveLoot(Player winner, Player loser, List<ItemStack> drops) {
        if (drops == null || drops.isEmpty()) return;

        String id = UUID.randomUUID().toString();
        String path = "loots." + winner.getUniqueId() + "." + id;

        config.set(path + ".victim", loser.getName());
        config.set(path + ".time", System.currentTimeMillis());
        
        config.set(path + ".itemsBase64", toBase64(drops)); 
        
        saveFile();

        winner.sendMessage("§a" + loser.getName() + " §7adlı oyuncunun eşyaları §6/duel sandık §7menüsüne eklendi.");
    }

    public void openLootMenu(Player p) {
        Inventory gui = Bukkit.createInventory(null, 54, "§8Duel Sandık");

        if (config.contains("loots." + p.getUniqueId())) {
            Set<String> keys = config.getConfigurationSection("loots." + p.getUniqueId()).getKeys(false);
            
            for (String id : keys) {
                String path = "loots." + p.getUniqueId() + "." + id;
                long time = config.getLong(path + ".time");

                if (System.currentTimeMillis() - time > 172800000) {
                    config.set(path, null);
                    continue;
                }

                String victim = config.getString(path + ".victim");
                
                ItemStack item = new ItemStack(Material.CHEST);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName("§e" + victim + " §7Envanteri");
                
                List<String> lore = new ArrayList<>();
                lore.add("§7");
                lore.add("§bSağ Tık §7-> §fİçeriği Gör");
                lore.add("§aSol Tık §7-> §fHepsini Al");
                lore.add("§7");
                lore.add("§c48 saat sonra silinir.");
                lore.add("§0id:" + id);
                
                meta.setLore(lore);
                item.setItemMeta(meta);
                
                gui.addItem(item);
            }
            saveFile();
        }
        p.openInventory(gui);
    }

    public void openPreview(Player p, String id) {
        String path = "loots." + p.getUniqueId() + "." + id;
        if (!config.contains(path)) return;

        Inventory preview = Bukkit.createInventory(null, 54, "§8İncele: " + id);

        List<ItemStack> items = getItemsFromConfig(path);

        if (items != null) {
            for (ItemStack item : items) {
                if (item != null) preview.addItem(item);
            }
        }

        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta meta = back.getItemMeta();
        meta.setDisplayName("§cGeri Dön");
        back.setItemMeta(meta);
        preview.setItem(49, back);

        p.openInventory(preview);
    }

    public void claimLoot(Player p, String id) {
        String path = "loots." + p.getUniqueId() + "." + id;
        
        if (!config.contains(path)) {
            p.sendMessage("§cBu sandık artık yok veya süresi dolmuş.");
            p.closeInventory();
            return;
        }

        List<ItemStack> items = getItemsFromConfig(path);

        if (items != null) {
            for (ItemStack item : items) {
                if (item != null) {
                    HashMap<Integer, ItemStack> left = p.getInventory().addItem(item);
                    for (ItemStack drop : left.values()) {
                        p.getWorld().dropItemNaturally(p.getLocation(), drop);
                    }
                }
            }
        }

        config.set(path, null);
        saveFile();
        
        p.sendMessage("§aEşyalar alındı! (Sığmayanlar yere düştü)");
        openLootMenu(p);
    }

    private List<ItemStack> getItemsFromConfig(String path) {
        if (config.contains(path + ".itemsBase64")) {
            return fromBase64(config.getString(path + ".itemsBase64"));
        } 
        else if (config.contains(path + ".items")) {
            List<?> list = config.getList(path + ".items");
            List<ItemStack> items = new ArrayList<>();
            if (list != null) {
                for (Object obj : list) {
                    if (obj instanceof ItemStack) items.add((ItemStack) obj);
                }
            }
            return items;
        }
        return new ArrayList<>();
    }

    private String toBase64(List<ItemStack> items) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            
            dataOutput.writeInt(items.size());
            for (ItemStack item : items) {
                dataOutput.writeObject(item);
            }
            
            dataOutput.close();
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private List<ItemStack> fromBase64(String data) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            
            int size = dataInput.readInt();
            List<ItemStack> items = new ArrayList<>();
            
            for (int i = 0; i < size; i++) {
                items.add((ItemStack) dataInput.readObject());
            }
            
            dataInput.close();
            return items;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private void createFile() {
        file = new File(plugin.getDataFolder(), "loot.yml");
        if (!file.exists()) {
            try { file.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    private void saveFile() {
        try { config.save(file); } catch (IOException e) { e.printStackTrace(); }
    }
}