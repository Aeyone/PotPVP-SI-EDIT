package net.frozenorb.potpvp.tab;

import java.util.function.BiConsumer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import net.frozenorb.qlib.tab.TabLayout;
import net.frozenorb.qlib.util.PlayerUtils;

import net.frozenorb.potpvp.PotPvPSI;
import net.frozenorb.potpvp.match.MatchHandler;

final class HeaderLayoutProvider implements BiConsumer<Player, TabLayout> {

    @Override
    public void accept(Player player, TabLayout tabLayout) {
        MatchHandler matchHandler = PotPvPSI.getInstance().getMatchHandler();
        header: {
            tabLayout.set(1, 0, ChatColor.GOLD.toString() + ChatColor.BOLD + "Practice");
        }
        status: {
            tabLayout.set(0, 1, ChatColor.GRAY + "Online: " + Bukkit.getOnlinePlayers().size());
            tabLayout.set(1, 1, ChatColor.GRAY + "Your Connection", PlayerUtils.getPing(player));
            tabLayout.set(2, 1, ChatColor.GRAY + "In Fights: " + matchHandler.countPlayersPlayingInProgressMatches());
        }
        /*
        status: {
            tabLayout.set(1, 1, ChatColor.GRAY + "Your Connection", Math.max(((PlayerUtils.getPing(player) + 5) / 10) * 10, 1));
        }
        */
    }
}
