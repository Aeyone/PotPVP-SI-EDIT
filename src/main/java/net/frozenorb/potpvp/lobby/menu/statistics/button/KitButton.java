package net.frozenorb.potpvp.lobby.menu.statistics.button;

import java.util.List;
import java.util.UUID;

import net.frozenorb.potpvp.lobby.menu.statistics.StatisticsHandler;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.google.common.collect.Lists;

import net.frozenorb.potpvp.PotPvPSI;
import net.frozenorb.potpvp.elo.EloHandler;
import net.frozenorb.potpvp.kittype.KitType;
import net.frozenorb.qlib.menu.Button;

public class KitButton extends Button {

    private static final EloHandler eloHandler = PotPvPSI.getInstance().getEloHandler();
    private static final StatisticsHandler statisticsHandler = PotPvPSI.getInstance().getStatisticsHandler();

    private KitType kitType;
    private UUID target;

    public KitButton(KitType kitType, UUID target) {
        this.kitType = kitType;
        this.target = target;
    }

    @Override
    public String getName(Player player) {
        return ChatColor.GREEN.toString() + ChatColor.BOLD + kitType.getDisplayName();
    }

    @Override
    public List<String> getDescription(Player player) {
        List<String> description = Lists.newArrayList();

        description.add(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "----------------");
        if (target.equals(player.getUniqueId())) {
            int elo = eloHandler.getElo(target, kitType);
            description.add(ChatColor.YELLOW + "Elo: " + (elo >= 1000 ? ChatColor.GREEN : ChatColor.RED) + elo);
        }
        if (statisticsHandler.checkNull(target)) {
            statisticsHandler.loadStatistics(target);
        }
        description.add(ChatColor.YELLOW + "Wins: " + ChatColor.GREEN + (int)statisticsHandler.getStat(target, StatisticsHandler.Statistic.WINS, kitType.getId()));
        description.add(ChatColor.YELLOW + "Losses: " + ChatColor.RED + (int)statisticsHandler.getStat(target, StatisticsHandler.Statistic.LOSSES, kitType.getId()));
        description.add(ChatColor.YELLOW + "W/L Ratio: " + ChatColor.AQUA +  Math.round(statisticsHandler.getStat(target, StatisticsHandler.Statistic.WLR, kitType.getId()) * 100.0) / 100.0);
        description.add(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "----------------");

        return description;
    }

    @Override
    public Material getMaterial(Player player) {
        return kitType.getIcon().getItemType();
    }

    @Override
    public byte getDamageValue(Player player) {
        return kitType.getIcon().getData();
    }
}
