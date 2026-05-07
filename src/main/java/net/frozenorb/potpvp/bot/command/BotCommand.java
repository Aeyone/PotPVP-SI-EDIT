package net.frozenorb.potpvp.bot.command;

import net.frozenorb.potpvp.PotPvPSI;
import net.frozenorb.potpvp.bot.menu.list.BotListMenu;
import net.frozenorb.qlib.command.Command;
import net.frozenorb.qlib.command.Param;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public final class BotCommand {
    private static final PotPvPSI plugin = PotPvPSI.getInstance();

    @Command(names = {"bot list"}, permission = "op")
    public static void list(Player sender) {
        new BotListMenu().openMenu(sender);
    }

    @Command(names = {"bot add"}, permission = "op")
    public static void add(Player sender, @Param(name = "name") String name) {
        plugin.getBotPendingManager().prepareManualAdd(sender, name);
    }

    @Command(names = {"bot del"}, permission = "op")
    public static void del(Player sender, @Param(name = "player") String name) {
        if (plugin.getBotManager().delBot(name)) {
            sender.sendMessage(ChatColor.GREEN + "Successfully deleted bot " + ChatColor.AQUA + name + ChatColor.GREEN + ".");
        } else {
            sender.sendMessage(ChatColor.RED + "Not found bot " + name + ".");
        }
    }

}
