package net.frozenorb.potpvp.match.listener;

import java.util.*;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.projectiles.ProjectileSource;

import net.frozenorb.potpvp.PotPvPSI;
import net.frozenorb.potpvp.match.Match;

public class MatchStatsListener implements Listener {
    private static final Set<Short> HEALTH_POTS = new HashSet<>(Arrays.asList(
        (short) 16385, // splash health I
        (short) 16421  // splash health II
    ));

    private static final Set<Short> DEBUFF_POTS = new HashSet<>(Arrays.asList(
        (short) 16388, // splash harm I
        (short) 16426, // splash harm II
        (short) 16424, // weakness
        (short) 16428, // slowness
        (short) 16458, // poison
        (short) 16420  // wither / custom
    ));


    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) return;

        Player damager = (Player) event.getDamager();
        Player damaged = (Player) event.getEntity();

        Match damagerMatch = PotPvPSI.getInstance().getMatchHandler().getMatchPlaying(damager);
        if (damagerMatch == null) return;

        Map<UUID, UUID> lastHitMap = damagerMatch.getLastHit();
        Map<UUID, Integer> combos = damagerMatch.getCombos();
        Map<UUID, Integer> totalHits = damagerMatch.getTotalHits();
        Map<UUID, Integer> blockedHits = damagerMatch.getBlockedHits();
        Map<UUID, Integer> longestCombo = damagerMatch.getLongestCombo();

        UUID lastHit = lastHitMap.put(damager.getUniqueId(), damaged.getUniqueId());
        if (lastHit != null) {
            if (lastHit.equals(damaged.getUniqueId())) {
                combos.put(damager.getUniqueId(), combos.getOrDefault(damager.getUniqueId(), 0) + 1);
            } else {
                combos.put(damager.getUniqueId(), 1);
            }

            longestCombo.put(damager.getUniqueId(), Math.max(combos.get(damager.getUniqueId()), longestCombo.getOrDefault(damager.getUniqueId(), 1)));
        } else {
            combos.put(damager.getUniqueId(), 0);
        }

        totalHits.put(damager.getUniqueId(), totalHits.getOrDefault(damager.getUniqueId(), 0) + 1);
        if (damaged.isBlocking()) {
            blockedHits.put(damaged.getUniqueId(), blockedHits.getOrDefault(damaged.getUniqueId(), 0) + 1);
        }
        while (lastHitMap.values().remove(damager.getUniqueId()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPotionLaunch(ProjectileLaunchEvent event) {
        Projectile thrownEntity = event.getEntity();
        if (!(thrownEntity instanceof ThrownPotion)) return;

        ThrownPotion thrownPotion = (ThrownPotion) thrownEntity;

        ProjectileSource projectileSource = thrownPotion.getShooter();
        if (!(projectileSource instanceof Player)) return;

        Player player = (Player) projectileSource;
        Match match = PotPvPSI.getInstance().getMatchHandler().getMatchPlaying(player);

        if (match == null) return;

        short durability = thrownPotion.getItem().getDurability();
        UUID uuid = player.getUniqueId();

        // calculation for different types of potions
        // If splash, then simply apply it as 'miss'

        if (HEALTH_POTS.contains(durability)) {
            match.getMissedPots().put(uuid, match.getMissedPots().getOrDefault(uuid, 0) + 1);
            match.getThrownHp().put(uuid, match.getThrownHp().getOrDefault(uuid, 0.0D) + 1.0D);
            match.getMissedHp().put(uuid, match.getMissedHp().getOrDefault(uuid, 0.0D) + 1.0D);
        } else if(DEBUFF_POTS.contains(durability)){
            match.getThrownDebuffs().put(uuid, match.getThrownDebuffs().getOrDefault(uuid, 0.0D) + 1.0D);
            match.getMissedDebuffs().put(uuid, match.getMissedDebuffs().getOrDefault(uuid, 0.0D) + 1.0D);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSplash(PotionSplashEvent event) {
        ThrownPotion thrownPotion = event.getEntity();

//        if (thrownPotion.getItem().getDurability() != 16421) return; // now we know it's a health pot!

        ProjectileSource projectileSource = thrownPotion.getShooter();
        if (!(projectileSource instanceof Player)) return;

        Player shooter = (Player) projectileSource;
        UUID shooterUid = shooter.getUniqueId();
        short durability = thrownPotion.getItem().getDurability();

        Match match = PotPvPSI.getInstance().getMatchHandler().getMatchPlaying(shooter);

        if (match == null) return;

//        for (LivingEntity affectedEntity : event.getAffectedEntities()) {
//            if (!affectedEntity.getUniqueId().equals(shooter.getUniqueId())) continue;
//
//            if (event.getIntensity(affectedEntity) == 1.0D) {
//                match.getMissedPots().put(shooter.getUniqueId(), Math.max(match.getMissedPots().getOrDefault(shooter.getUniqueId(), 1) - 1, 0));
//            }
//        }

        if (HEALTH_POTS.contains(durability)) {
             if (event.getIntensity(shooter) > 0.6D) { // cancel miss
                match.getMissedPots().put(
                        shooterUid,
                        Math.max(match.getMissedPots().getOrDefault(shooterUid, 1) - 1, 0)
                );
             }
             match.getMissedHp().put( // calculation splash accuracy
                     shooterUid,
                     Math.max(match.getMissedHp().getOrDefault(shooterUid, 1.0D) - event.getIntensity(shooter), 0.0D)
             );
        } else if(DEBUFF_POTS.contains(durability)){
            for (LivingEntity affectedEntity : event.getAffectedEntities()) {
                for (UUID u : match.getTeam(shooterUid).getAliveMembers()) {
                     if (affectedEntity.getUniqueId() != u) {
                         match.getMissedDebuffs().put(
                                 shooterUid,
                                 Math.max(match.getMissedDebuffs().getOrDefault(shooterUid, 1.0D) - event.getIntensity((Player)affectedEntity), 0.0D)
                         );
                     }
                }
            }
          }
    }
/*
    @EventHandler(priority = EventPriority.MONITOR)
    public void onMatchEnd(MatchEndEvent event) {
        Match match = event.getMatch();
        match.getTeams().forEach(team -> {
            if (match.getWinner() == team) {
                team.getAllMembers().forEach(PotPvPSI.getInstance().getWinsMap()::incrementWins);
            } else {
                team.getAllMembers().forEach(PotPvPSI.getInstance().getLossMap()::incrementLosses);
            }
        });
    }
    */
}
