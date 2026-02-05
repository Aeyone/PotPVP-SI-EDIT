package net.frozenorb.potpvp.postmatchinv.menu;

import com.google.common.collect.ImmutableList;

import net.frozenorb.potpvp.kittype.HealingMethod;
import net.frozenorb.potpvp.kittype.KitType;
import net.frozenorb.qlib.menu.Button;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

final class PostMatchStatisticsButton extends Button {

    private final KitType kitType;
    private final HealingMethod healingMethodUsed;

    private final int totalHits;
    private final int blockedHits;
    private final int longestCombo;

    private final double thrownHp;
    private final double missedHp;
    private final double thrownDebuffs;
    private final double missedDebuffs;

    PostMatchStatisticsButton(KitType kitType, HealingMethod healingMethodUsed, int totalHits, int blockedHits, int longestCombo, double thrownHp, double missedHp, double thrownDebuffs, double missedDebuffs) {
        this.kitType = kitType;
        this.healingMethodUsed = healingMethodUsed;
        this.totalHits = totalHits;
        this.blockedHits = blockedHits;
        this.longestCombo = longestCombo;
        this.thrownHp = thrownHp;
        this.missedHp = missedHp;
        this.thrownDebuffs = thrownDebuffs;
        this.missedDebuffs = missedDebuffs;
    }

    @Override
    public String getName(Player player) {
        return ChatColor.GREEN + "Match Stats";
    }

    @Override
    public List<String> getDescription(Player player) {
        if (healingMethodUsed != HealingMethod.POTIONS) {
            return ImmutableList.of(
                    ChatColor.LIGHT_PURPLE + "Hits:" + ChatColor.YELLOW.toString() + " " + this.totalHits,
                    ChatColor.LIGHT_PURPLE + "Blocked Hits:" + ChatColor.YELLOW.toString() + " " + this.blockedHits,
                    ChatColor.LIGHT_PURPLE + "Longest Combo:" + ChatColor.YELLOW.toString() + " " + this.longestCombo
            );
        }
        int heal = getAccuracy(thrownHp, missedHp);

        if (kitType.getId().equals("DEBUFF") || kitType.getId().equals("VANILLA")) {
            int debuff = getAccuracy(thrownDebuffs, missedDebuffs);
            return ImmutableList.of(
                    ChatColor.LIGHT_PURPLE + "Hits:" + ChatColor.YELLOW.toString() + " " + this.totalHits,
                    ChatColor.LIGHT_PURPLE + "Blocked Hits:" + ChatColor.YELLOW.toString() + " " + this.blockedHits,
                    ChatColor.LIGHT_PURPLE + "Longest Combo:" + ChatColor.YELLOW.toString() + " " + this.longestCombo,
                    ChatColor.LIGHT_PURPLE + "Potion Accuracy: " + ChatColor.YELLOW + (heal == -1 ? "N/A" : heal + "%"),
                    ChatColor.LIGHT_PURPLE + "Debuff Accuracy: " + ChatColor.YELLOW + (debuff == -1 ? "N/A" : debuff + "%")
            );
        }

        return ImmutableList.of(
                ChatColor.LIGHT_PURPLE + "Hits:" + ChatColor.YELLOW.toString() + " " + this.totalHits,
                ChatColor.LIGHT_PURPLE + "Blocked Hits:" + ChatColor.YELLOW.toString() + " " + this.blockedHits,
                ChatColor.LIGHT_PURPLE + "Longest Combo:" + ChatColor.YELLOW.toString() + " " + this.longestCombo,
                ChatColor.LIGHT_PURPLE + "Potion Accuracy: " + ChatColor.YELLOW + (heal == -1 ? "N/A" : heal + "%")
        );
    }

    @Override
    public Material getMaterial(Player player) {
        return Material.PAPER;
    }

    @Override
    public int getAmount(Player player) {
        return 1;
    }

    public int getAccuracy(double thrown, double miss) {
        if (thrown == 0) {
            return -1;
        }
        return (100 - (int) ((miss / thrown) * 100));
    }
}