package net.frozenorb.potpvp.duel.command;

import net.frozenorb.potpvp.duel.menu.BotDuelSelectMenu;
import net.frozenorb.qlib.command.parameter.PlayerParameterType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DuelTargetParameterType extends PlayerParameterType {

    public static final String BOT_MENU_SENTINEL = "__bot_duel_menu__";

    @Override
    public Player transform(CommandSender sender, String value) {
        if (BOT_MENU_SENTINEL.equals(value)) {
            if (sender instanceof Player) {
                new BotDuelSelectMenu().openMenu((Player) sender);
            }
            return null;
        }

        return super.transform(sender, value);
    }

}
