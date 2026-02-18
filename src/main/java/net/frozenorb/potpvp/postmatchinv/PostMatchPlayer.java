package net.frozenorb.potpvp.postmatchinv;

import com.google.common.collect.ImmutableList;

import net.frozenorb.potpvp.kittype.KitType;

import net.frozenorb.qlib.util.PlayerUtils;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.Getter;

public final class PostMatchPlayer {

    @Getter private UUID playerUuid;
    @Getter private String lastUsername;
    @Getter private ItemStack[] armor;
    @Getter private ItemStack[] inventory;
    @Getter private List<PotionEffect> potionEffects;
    @Getter private int hunger;
    @Getter private int health; // out of 10
    private String kitType;
    @Getter private int totalHits;
    @Getter private int blockedHits;
    @Getter private int longestCombo;
    @Getter private int missedPots;
    @Getter private double thrownHp;
    @Getter private double missedHp;
    @Getter private double thrownDebuffs;
    @Getter private double missedDebuffs;
    @Getter private int ping;

    public PostMatchPlayer() {
        this.playerUuid = null;
        this.lastUsername = null;
        this.armor = new ItemStack[0];
        this.inventory = new ItemStack[0];
        this.potionEffects = new ArrayList<>();
        this.hunger = 0;
        this.health = 0;
        this.kitType = null;
        this.totalHits = 0;
        this.blockedHits = 0;
        this.longestCombo = 0;
        this.missedPots = 0;
        this.thrownHp = 0;
        this.missedHp = 0;
        this.thrownDebuffs = 0;
        this.missedDebuffs = 0;
        this.ping = 0;
    }


    public PostMatchPlayer(Player player, KitType kitType, int totalHits, int blockedHits, int longestCombo, int missedPots, double thrownHp, double missedHp, double thrownDebuffs, double missedDebuffs) {
        this.playerUuid = player.getUniqueId();
        this.lastUsername = player.getName();
        this.armor = player.getInventory().getArmorContents();
        this.inventory = player.getInventory().getContents();
        this.potionEffects = ImmutableList.copyOf(player.getActivePotionEffects());
        this.hunger = player.getFoodLevel();
        this.health = (int) player.getHealth();
        this.kitType = kitType.getId();
        this.totalHits = totalHits;
        this.blockedHits = blockedHits;
        this.longestCombo = longestCombo;
        this.missedPots = missedPots;
        this.thrownHp = thrownHp;
        this.missedHp = missedHp;
        this.thrownDebuffs = thrownDebuffs;
        this.missedDebuffs = missedDebuffs;
        this.ping = PlayerUtils.getPing(player);
    }

    public KitType getKitType() {
        return KitType.byId(this.kitType);
    }
}