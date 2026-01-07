package net.frozenorb.potpvp.postmatchinv;

import com.google.common.collect.ImmutableList;

import net.frozenorb.potpvp.kittype.HealingMethod;
import net.frozenorb.potpvp.kittype.KitType;

import net.frozenorb.qlib.util.PlayerUtils;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.List;
import java.util.UUID;

import lombok.Getter;

public final class PostMatchPlayer {

    @Getter private final UUID playerUuid;
    @Getter private final String lastUsername;
    @Getter private final ItemStack[] armor;
    @Getter private final ItemStack[] inventory;
    @Getter private final List<PotionEffect> potionEffects;
    @Getter private final int hunger;
    @Getter private final int health; // out of 10
    @Getter private final transient HealingMethod healingMethodUsed;
    @Getter  final KitType kitType;
    @Getter private final int totalHits;
    @Getter private final int longestCombo;
    @Getter private final int missedPots;
    @Getter private final double thrownHp;
    @Getter private final double missedHp;
    @Getter private final double thrownDebuffs;
    @Getter private final double missedDebuffs;
    @Getter private final int ping;

    public PostMatchPlayer(Player player, KitType kitType, HealingMethod healingMethodUsed, int totalHits, int longestCombo, int missedPots, double thrownHp, double missedHp, double thrownDebuffs, double missedDebuffs) {
        this.playerUuid = player.getUniqueId();
        this.lastUsername = player.getName();
        this.armor = player.getInventory().getArmorContents();
        this.inventory = player.getInventory().getContents();
        this.potionEffects = ImmutableList.copyOf(player.getActivePotionEffects());
        this.hunger = player.getFoodLevel();
        this.health = (int) player.getHealth();
        this.kitType = kitType;
        this.healingMethodUsed = healingMethodUsed;
        this.totalHits = totalHits;
        this.longestCombo = longestCombo;
        this.missedPots = missedPots;
        this.thrownHp = thrownHp;
        this.missedHp = missedHp;
        this.thrownDebuffs = thrownDebuffs;
        this.missedDebuffs = missedDebuffs;
        this.ping = PlayerUtils.getPing(player);
    }

}