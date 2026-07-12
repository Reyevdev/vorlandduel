package me.vorland.duel.manager;

import me.vorland.duel.VorlandDuel;
import me.vorland.duel.model.Arena;
import me.vorland.duel.model.DuelSession;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.UUID;

public class DuelManager {
    private final VorlandDuel plugin;
    private final HashMap<UUID, DuelSession> activeSessions = new HashMap<>();
    private final HashMap<UUID, DuelInvite> pendingInvites = new HashMap<>();

    public DuelManager(VorlandDuel plugin) { this.plugin = plugin; }

    public void createInvite(Player sender, Player target, Arena arena, boolean e, boolean st, boolean sl, boolean w, boolean b) {
        if (target == null) return;
        if (pendingInvites.containsKey(target.getUniqueId())) pendingInvites.get(target.getUniqueId()).cancelTimeout();

        DuelInvite invite = new DuelInvite(sender.getUniqueId(), arena, e, st, sl, w, b);
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (pendingInvites.containsKey(target.getUniqueId()) && pendingInvites.get(target.getUniqueId()).equals(invite)) {
                pendingInvites.remove(target.getUniqueId());
                if (sender.isOnline()) sender.sendMessage("§c" + target.getName() + " daveti kabul etmedi.");
            }
        }, 15 * 20L);
        invite.setTask(task);
        pendingInvites.put(target.getUniqueId(), invite);

        target.sendMessage(" ");
        target.sendMessage("§6§lDÜELLO DAVETİ!");
        target.sendMessage("§fGönderen: §e" + sender.getName());
        target.sendMessage("§fHarita: §b" + (arena != null ? arena.getName() : "Rastgele"));
        target.sendMessage("§fAyarlar:");
        target.sendMessage(" §7> §fMerdiven: " + (st ? "§aAçık" : "§cKapalı"));
        target.sendMessage(" §7> §fSlab: " + (sl ? "§aAçık" : "§cKapalı"));
        target.sendMessage(" §7> §fAğ: " + (w ? "§aAçık" : "§cKapalı"));
        target.sendMessage(" §7> §fButon: " + (b ? "§aAçık" : "§cKapalı")); // YENİ
        target.sendMessage(" §7> §fElitra: " + (e ? "§aAçık" : "§cKapalı"));
        target.sendMessage(" §7> §7(Eşyalar her zaman düşer ve sandığa gider)");
        target.sendMessage(" ");
        target.sendMessage("§aKabul etmek için: §n/duel accept " + sender.getName());
        target.sendMessage(" ");
        
        target.playSound(target.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 2);
        sender.sendMessage("§aDavet gönderildi!");
    }

    public void acceptInvite(Player target, Player sender) {
        if (!pendingInvites.containsKey(target.getUniqueId())) {
            target.sendMessage("§cAktif davet yok.");
            return;
        }
        DuelInvite invite = pendingInvites.get(target.getUniqueId());
        if (!invite.senderId.equals(sender.getUniqueId())) {
            target.sendMessage("§cYanlış kişi.");
            return;
        }
        invite.cancelTimeout();
        pendingInvites.remove(target.getUniqueId());
        startDuel(sender, target, invite.arena, invite.elytra, invite.stairs, invite.slabs, invite.webs, invite.buttons);
    }

    public void startDuel(Player p1, Player p2, Arena arena, boolean e, boolean st, boolean sl, boolean w, boolean b) {
        if (arena == null || arena.isBusy()) {
            arena = plugin.getArenaManager().getArenas().stream()
                    .filter(a -> !a.isBusy() && a.isReady()).findFirst().orElse(null);
        }
        if (arena == null) {
            p1.sendMessage("§cBoş arena yok!"); p2.sendMessage("§cBoş arena yok!");
            return;
        }

        arena.setBusy(true);
        DuelSession session = new DuelSession(p1, p2, arena, e, st, sl, w, b);
        activeSessions.put(p1.getUniqueId(), session);
        activeSessions.put(p2.getUniqueId(), session);
        
        session.setFrozen(true);
        
        if (!e) {
            unequipElytra(p1);
            unequipElytra(p2);
        }

        p1.teleport(arena.getSpawn1());
        p2.teleport(arena.getSpawn2());

        Bukkit.broadcastMessage("§8[§6Duel§8] §e" + p1.getName() + " §7ve §e" + p2.getName() + " §7düellosu başladı!");

        new BukkitRunnable() {
            int count = 3;
            @Override
            public void run() {
                if (count > 0) {
                    String title = "§c" + count;
                    p1.sendTitle(title, "", 0, 20, 0);
                    p2.sendTitle(title, "", 0, 20, 0);
                    p1.playSound(p1.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                    p2.playSound(p2.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                    count--;
                } else {
                    p1.sendTitle("§a§lBAŞLA!", "", 5, 20, 5);
                    p2.sendTitle("§a§lBAŞLA!", "", 5, 20, 5);
                    p1.playSound(p1.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                    p2.playSound(p2.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                    session.setFrozen(false);
                    session.setStarted(true);
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void unequipElytra(Player p) {
        if (p.getInventory().getChestplate() != null && p.getInventory().getChestplate().getType() == Material.ELYTRA) {
            ItemStack elytra = p.getInventory().getChestplate();
            p.getInventory().setChestplate(null); 
            p.getInventory().addItem(elytra);
            p.sendMessage("§cElitra bu düelloda kapalı olduğu için çıkartıldı.");
        }
    }

    public void endDuel(DuelSession session, Player winner) {
        if (session == null) return;
        Player loser = session.getOpponent(winner);

        if (winner != null) {
            Bukkit.broadcastMessage("§8[§6Duel§8] §aKazanan: §e" + winner.getName());
            winner.sendTitle("§6§lKAZANDIN!", "§7Tebrikler!", 10, 60, 20);
            if (loser != null) loser.sendTitle("§c§lKAYBETTİN!", "§7Daha iyi şanslar...", 10, 60, 20);
        }

        session.cleanupBlocks();
        if (session.getArena() != null) session.getArena().setBusy(false);

        activeSessions.remove(session.getP1().getUniqueId());
        activeSessions.remove(session.getP2().getUniqueId());

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (winner != null && winner.isOnline()) winner.performCommand("spawn");
            if (loser != null && loser.isOnline()) loser.performCommand("spawn");
        }, 12 * 20L);
    }

    public DuelSession getSession(Player p) { return activeSessions.get(p.getUniqueId()); }

    public static class DuelInvite {
        UUID senderId; Arena arena; 
        boolean elytra, stairs, slabs, webs, buttons; 
        BukkitTask task;
        
        public DuelInvite(UUID s, Arena a, boolean e, boolean st, boolean sl, boolean w, boolean b) {
            this.senderId = s; this.arena = a;
            this.elytra = e; 
            this.stairs = st; this.slabs = sl; this.webs = w;
            this.buttons = b;
        }
        public void setTask(BukkitTask task) { this.task = task; }
        public void cancelTimeout() { if (task != null) task.cancel(); }
    }
}