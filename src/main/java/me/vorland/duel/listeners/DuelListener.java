package me.vorland.duel.listeners;

import me.vorland.duel.VorlandDuel;
import me.vorland.duel.model.Arena;
import me.vorland.duel.model.DuelSession;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.Inventory; 
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DuelListener implements Listener {
    private final VorlandDuel plugin;
    public DuelListener(VorlandDuel plugin) { this.plugin = plugin; }

    @EventHandler
    public void onWandInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        ItemStack item = e.getItem();

        if (item != null && item.getType() == Material.STICK && item.hasItemMeta() && "§bVorlandDuel Wand".equals(item.getItemMeta().getDisplayName())) {
            e.setCancelled(true);

            if (e.getClickedBlock() == null) return;
            Location loc = e.getClickedBlock().getLocation();

            if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
                plugin.getArenaManager().setSelection(p, loc, 1);
            } else if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                plugin.getArenaManager().setSelection(p, loc, 2);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(PlayerDeathEvent e) {
        Player loser = e.getEntity();
        DuelSession s = plugin.getDuelManager().getSession(loser);
        
        if (s != null) {
            Player winner = s.getOpponent(loser);
            
            List<ItemStack> itemsToSave = new ArrayList<>();
            for (ItemStack item : loser.getInventory().getContents()) {
                if (item != null && item.getType() != Material.AIR) itemsToSave.add(item);
            }

            if (winner != null && !itemsToSave.isEmpty()) {
                try {
                    plugin.getLootManager().saveLoot(winner, loser, itemsToSave);
                } catch (Exception ex) {
                    plugin.getLogger().warning("Loot kaydedilemedi: " + ex.getMessage());
                }
            }

            e.getDrops().clear();
            e.setDroppedExp(0);
            e.setKeepInventory(true); // Yere düşmemesi için
            
            loser.getInventory().clear();
            loser.getInventory().setArmorContents(null);
            loser.getInventory().setExtraContents(null);

            plugin.getDuelManager().endDuel(s, winner);
        }
    }

    //  OYUNDAN ÇIKINCA 
    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player loser = e.getPlayer();
        DuelSession s = plugin.getDuelManager().getSession(loser);
        
        if (s != null) {
            Player winner = s.getOpponent(loser);

            if (!loser.isDead() && winner != null) {
                List<ItemStack> itemsToSave = new ArrayList<>();
                for (ItemStack item : loser.getInventory().getContents()) {
                    if (item != null && item.getType() != Material.AIR) itemsToSave.add(item);
                }
                
                plugin.getLootManager().saveLoot(winner, loser, itemsToSave);
                loser.getInventory().clear(); 
            }
            
            plugin.getDuelManager().endDuel(s, winner);
        }
    }

    // MENÜ TIKLAMALARI 
    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player p = (Player) e.getWhoClicked();
        
        if (e.getView().getTitle().equals("§8Duel Sandık")) {
            e.setCancelled(true);
            if (e.getCurrentItem() == null || !e.getCurrentItem().hasItemMeta()) return;
            List<String> lore = e.getCurrentItem().getItemMeta().getLore();
            if (lore == null || lore.isEmpty()) return;
            String idLine = lore.get(lore.size() - 1);
            if (!idLine.startsWith("§0id:")) return;
            String id = idLine.replace("§0id:", "");
            if (e.isLeftClick()) plugin.getLootManager().claimLoot(p, id);
            else if (e.isRightClick()) plugin.getLootManager().openPreview(p, id);
            return;
        }
        else if (e.getView().getTitle().startsWith("§8İncele: ")) {
            e.setCancelled(true);
            if (e.getCurrentItem() != null && e.getCurrentItem().getType() == Material.ARROW) {
                plugin.getLootManager().openLootMenu(p);
            }
            return;
        }

        DuelSession s = plugin.getDuelManager().getSession(p);
        if (s != null && !s.isElytra()) {
            if (e.getSlotType() == InventoryType.SlotType.ARMOR) {
                if ((e.getCursor() != null && e.getCursor().getType() == Material.ELYTRA) || 
                    (e.getCurrentItem() != null && e.getCurrentItem().getType() == Material.ELYTRA)) {
                    e.setCancelled(true);
                    p.sendMessage("§cBu düelloda Elitra kapalı!");
                }
            }
            if (e.isShiftClick() && e.getCurrentItem() != null && e.getCurrentItem().getType() == Material.ELYTRA) {
                e.setCancelled(true);
                p.sendMessage("§cBu düelloda Elitra kapalı!");
            }
        }
    }

    //OYUNCU ETKİLEŞİMİ ELİTRA 
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        
        // Wand kontrolü yukarıdaki ayrı event'te yapılıyor, burası sadece düello içi etkileşimler
        DuelSession s = plugin.getDuelManager().getSession(p);
        
        if (s != null && !s.isElytra()) {
            if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (e.getItem() != null && e.getItem().getType() == Material.ELYTRA) {
                    e.setCancelled(true);
                    p.sendMessage("§cBu düelloda Elitra kapalı!");
                }
            }
        }
    }

    // MENÜ İŞLEMLERİ 
    @EventHandler
    public void onMenuClick(InventoryClickEvent e) {
        if (e.getView().getTitle().startsWith("Düello Ayarları:")) {
            e.setCancelled(true);
            ItemStack item = e.getCurrentItem();
            if (item == null || item.getType() == Material.AIR || item.getType() == Material.BLACK_STAINED_GLASS_PANE) return;

            Player s = (Player) e.getWhoClicked();
            int slot = e.getRawSlot();

            if (slot == 22) { 
                List<Arena> available = plugin.getArenaManager().getArenas().stream()
                        .filter(a -> !a.isBusy() && a.isReady()).collect(Collectors.toList());
                if (available.isEmpty()) { s.sendMessage("§cMüsait arena yok!"); return; }
                String current = ChatColor.stripColor(item.getItemMeta().getDisplayName().split(": ")[1]);
                int idx = 0;
                for(int i=0; i<available.size(); i++) if(available.get(i).getName().equals(current)) idx = i;
                idx = (idx + 1) % available.size();
                ItemMeta m = item.getItemMeta();
                m.setDisplayName("§eHarita: §f" + available.get(idx).getName());
                item.setItemMeta(m);
                s.playSound(s.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                return;
            }

            if (slot == 31) { 
                Player t = Bukkit.getPlayer(e.getView().getTitle().split(": ")[1]);
                if (t == null) { s.closeInventory(); return; }

                Inventory inv = e.getInventory();
                boolean ely = getStatus(inv.getItem(11));
                boolean butt = getStatus(inv.getItem(12)); 
                boolean lad = getStatus(inv.getItem(13));
                boolean slab = getStatus(inv.getItem(14));
                boolean web = getStatus(inv.getItem(15));
                
                String mapName = ChatColor.stripColor(inv.getItem(22).getItemMeta().getDisplayName().split(": ")[1]);
                Arena arena = plugin.getArenaManager().getArena(mapName);

                if (arena != null && arena.isBusy()) {
                    s.sendMessage("§cSeçilen arena doldu!"); s.closeInventory(); return;
                }

                plugin.getDuelManager().createInvite(s, t, arena, ely, lad, slab, web, butt);
                s.closeInventory();
                return;
            }

            ItemMeta m = item.getItemMeta();
            if (m != null && m.hasLore()) {
                List<String> l = m.getLore();
                if (l.get(0).contains("AÇIK")) l.set(0, "§7Durum: §cKAPALI");
                else l.set(0, "§7Durum: §aAÇIK");
                m.setLore(l);
                item.setItemMeta(m);
                s.playSound(s.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 2);
            }
        }
    }

    // BLOK KOYMA
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        DuelSession session = plugin.getDuelManager().getSession(p);
        if (session == null) return;

        if (e.isCancelled()) e.setCancelled(false);

        Material m = e.getBlockPlaced().getType();
        String name = m.name();
        boolean blocked = false;

        if (m == Material.COBWEB && !session.isWebs()) blocked = true;
        else if ((m == Material.LADDER || name.contains("STAIRS")) && !session.isStairs()) blocked = true;
        else if (name.contains("SLAB") && !session.isSlabs()) blocked = true;
        else if (name.contains("BUTTON") && !session.isButtons()) blocked = true;

        if (blocked) {
            e.setCancelled(true);
            p.sendMessage("§cBu bloğu kullanma ayarı kapalı!");
            return;
        }
        session.trackBlock(e.getBlockPlaced().getLocation());
    }

    //BLOK KIRMA
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        DuelSession session = plugin.getDuelManager().getSession(p);
        if (session == null) return;

        if (session.isPlacedBlock(e.getBlock().getLocation())) {
            e.setCancelled(false); 
            session.removePlacedBlock(e.getBlock().getLocation());
        } else {
            e.setCancelled(true);
            p.sendMessage("§cSadece sonradan koyulan blokları kırabilirsin!");
        }
    }

    // HAREKET VE KOMUT
    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        DuelSession s = plugin.getDuelManager().getSession(e.getPlayer());
        if (s != null && s.isFrozen()) {
            if (e.getFrom().getX() != e.getTo().getX() || e.getFrom().getZ() != e.getTo().getZ()) {
                e.setTo(e.getFrom());
            }
        }
    }

    @EventHandler
    public void onElytra(EntityToggleGlideEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        DuelSession s = plugin.getDuelManager().getSession((Player) e.getEntity());
        if (s != null && !s.isElytra()) e.setCancelled(true);
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
        DuelSession s = plugin.getDuelManager().getSession(e.getPlayer());
        if (s != null) {
            e.setCancelled(true);
            e.getPlayer().sendMessage("§cDüello sırasında komut kullanamazsın!");
        }
    }

    private boolean getStatus(ItemStack i) {
        return i != null && i.hasItemMeta() && i.getItemMeta().getLore().get(0).contains("AÇIK");
    }
}