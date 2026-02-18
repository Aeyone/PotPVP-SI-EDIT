package net.frozenorb.potpvp.lobby.menu.statistics.button;

import java.util.List;
import java.util.UUID;

import net.frozenorb.potpvp.lobby.menu.statistics.StatisticsHandler;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.google.common.collect.Lists;

// import net.frozenorb.hydrogen.Hydrogen;
// import net.frozenorb.hydrogen.profile.Profile;
import net.frozenorb.potpvp.PotPvPSI;
import net.frozenorb.potpvp.elo.EloHandler;
import net.frozenorb.qlib.menu.Button;

public class PlayerButton extends Button {

    private static EloHandler eloHandler = PotPvPSI.getInstance().getEloHandler();
    private static StatisticsHandler statisticsHandler = PotPvPSI.getInstance().getStatisticsHandler();
    private UUID target;

    public PlayerButton(UUID target){
        this.target = target;
    }

    @Override
    public String getName(Player player) {
        return ChatColor.GOLD.toString() + ChatColor.BOLD + "Global Statistics";
    }

    @Override
    public List<String> getDescription(Player player) {
        List<String> description = Lists.newArrayList();


        description.add(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "----------------------");
        if (statisticsHandler.checkNull(target)) {
            statisticsHandler.loadStatistics(target);
        }
        if (target.equals(player.getUniqueId())) {
            int globalElo = eloHandler.getGlobalElo(target);
            description.add(ChatColor.YELLOW + "Global Elo: " + (globalElo >= 1000 ? ChatColor.GREEN : ChatColor.RED) + globalElo);
        }
        description.add(ChatColor.YELLOW + "Wins: " + ChatColor.GREEN + (int)statisticsHandler.getStat(target, StatisticsHandler.Statistic.WINS, "GLOBAL"));
        description.add(ChatColor.YELLOW + "Losses: " + ChatColor.RED + (int)statisticsHandler.getStat(target, StatisticsHandler.Statistic.LOSSES, "GLOBAL"));
        description.add(ChatColor.YELLOW + "W/L Ratio: " + ChatColor.AQUA +  Math.round(statisticsHandler.getStat(target, StatisticsHandler.Statistic.WLR, "GLOBAL") * 100.0) / 100.0);
        description.add(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "----------------------");

        return description;
    }

    @Override
    public Material getMaterial(Player player) {
        return Material.SKULL_ITEM;
    }

    @Override
    public byte getDamageValue(Player player) {
        return (byte) 3;
    }
}
