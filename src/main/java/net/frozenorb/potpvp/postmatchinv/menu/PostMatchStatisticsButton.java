package net.frozenorb.potpvp.postmatchinv.menu;


import com.google.common.collect.Lists;
import net.frozenorb.potpvp.kittype.HealingMethod;
import net.frozenorb.potpvp.kittype.KitType;
import net.frozenorb.potpvp.lobby.menu.statistics.StatisticsMenu;
import net.frozenorb.qlib.menu.Button;

import net.frozenorb.qlib.util.UUIDUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.List;
import java.util.UUID;

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
    private final UUID target;

    PostMatchStatisticsButton(KitType kitType, HealingMethod healingMethodUsed, int totalHits, int blockedHits, int longestCombo, double thrownHp, double missedHp, double thrownDebuffs, double missedDebuffs, UUID target) {
        this.kitType = kitType;
        this.healingMethodUsed = healingMethodUsed;
        this.totalHits = totalHits;
        this.blockedHits = blockedHits;
        this.longestCombo = longestCombo;
        this.thrownHp = thrownHp;
        this.missedHp = missedHp;
        this.thrownDebuffs = thrownDebuffs;
        this.missedDebuffs = missedDebuffs;
        this.target = target;
    }

    @Override
    public String getName(Player player) {
        return ChatColor.GREEN + "Match Stats";
    }

    @Override
    public List<String> getDescription(Player player) {
        List<String> description = Lists.newArrayList();

        description.add(ChatColor.LIGHT_PURPLE + "Hits:" + ChatColor.YELLOW + " " + this.totalHits);
        description.add(ChatColor.LIGHT_PURPLE + "Blocked Hits:" + ChatColor.YELLOW + " " + this.blockedHits);
        description.add(ChatColor.LIGHT_PURPLE + "Longest Combo:" + ChatColor.YELLOW + " " + this.longestCombo);

        if (healingMethodUsed == HealingMethod.POTIONS) {
            int heal = getAccuracy(thrownHp, missedHp);
            description.add(ChatColor.LIGHT_PURPLE + "Potion Accuracy: " + ChatColor.YELLOW + (heal == -1 ? "N/A" : heal + "%"));

            if (kitType.getId().equals("DEBUFF") || kitType.getId().equals("VANILLA")) {
                int debuff = getAccuracy(thrownDebuffs, missedDebuffs);
                description.add(ChatColor.LIGHT_PURPLE + "Debuff Accuracy: " + ChatColor.YELLOW + (debuff == -1 ? "N/A" : debuff + "%"));
            }
        }
        return description;
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
        return (int)(100.0D - (miss / thrown) * 100);
    }
}