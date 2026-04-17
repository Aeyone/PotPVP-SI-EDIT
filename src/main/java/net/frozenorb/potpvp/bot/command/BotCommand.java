package net.frozenorb.potpvp.bot.command;

import net.frozenorb.potpvp.PotPvPSI;
import net.frozenorb.potpvp.kittype.menu.select.SelectKitTypeMenu;
import net.frozenorb.potpvp.setting.Setting;
import net.frozenorb.qlib.command.Command;
import net.frozenorb.qlib.command.Param;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import static net.frozenorb.potpvp.duel.command.DuelCommand.getArenas;

public final class BotCommand {
    private static final PotPvPSI plugin = PotPvPSI.getInstance();

    @Command(names = {"bot duel"}, permission = "")
    public static void duelBot(Player sender){
        new SelectKitTypeMenu(
            kitType -> {
                sender.closeInventory();
                if (PotPvPSI.getInstance().getSettingHandler().getSetting(sender, Setting.SELECT_MAP)) {
                    getArenas(sender, kitType, allArenas -> PotPvPSI.getInstance().getBotMatchManager().prepareDuel(sender, kitType, allArenas) );
                } else {
                    PotPvPSI.getInstance().getBotMatchManager().prepareDuel(sender, kitType, null);
                }
            },
            "Select a kit type..."
        ).openMenu(sender);
    }

    @Command(names = {"bot list"}, permission = "op")
    public static void list(Player sender) {
        String msg = null;
        for (String name : plugin.getBotManager().getList()) {
            String s = (Bukkit.getPlayer(name) != null ? ChatColor.GREEN : ChatColor.RED) + name;
            msg = (msg == null ? s : msg + "," + s);
        }
        sender.sendMessage("There are (" + plugin.getBotManager().getList().size() + "/100) bot active:");
        if (msg != null) {
            sender.sendMessage(msg);
        }
    }

    @Command(names = {"bot add"}, permission = "op")
    public static void add(Player sender, @Param(name = "name") String name) {
        if (plugin.getBotManager().addBot(name, sender)) {
            sender.sendMessage(ChatColor.GREEN + "Successfully added " + ChatColor.AQUA + name + ChatColor.GREEN + " to active bots.");
        } else {
            sender.sendMessage(ChatColor.RED + name + " already exists.");
        }
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
