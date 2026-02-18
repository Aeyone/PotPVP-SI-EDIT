package net.frozenorb.potpvp.lobby.menu.statistics;

import net.frozenorb.qlib.command.Command;
import net.frozenorb.qlib.command.Param;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class StatisticsCommand {

    @Command(names = {"statistics", "stats"}, permission = "")
    public static void StatisticsCommand(Player sender, @Param(name = "target", defaultValue = "self") OfflinePlayer target) {
        new StatisticsMenu(target.getUniqueId()).openMenu(sender);
    }
}
