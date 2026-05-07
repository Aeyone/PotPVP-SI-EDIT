package net.frozenorb.potpvp.duel.menu;

import net.frozenorb.potpvp.PotPvPSI;
import net.frozenorb.potpvp.kittype.KitType;
import net.frozenorb.potpvp.kittype.menu.select.SelectKitTypeMenu;
import net.frozenorb.potpvp.setting.Setting;
import org.bukkit.entity.Player;

import java.util.Set;

import static net.frozenorb.potpvp.duel.command.DuelCommand.getArenas;

final class BotDuelMenuUtils {

    private BotDuelMenuUtils() {
    }

    static void openKitSelection(Player player, String botName) {
        new SelectKitTypeMenu(
            kitType -> {
                player.closeInventory();
                if (PotPvPSI.getInstance().getSettingHandler().getSetting(player, Setting.SELECT_MAP)) {
                    getArenas(player, kitType, allArenas -> prepareDuel(player, botName, kitType, allArenas));
                } else {
                    prepareDuel(player, botName, kitType, null);
                }
            },
            "Select a kit type..."
        ).openMenu(player);
    }

    private static void prepareDuel(Player player, String botName, KitType kitType, Set<String> allArenas) {
        if (botName == null) {
            PotPvPSI.getInstance().getBotPendingManager().prepareDuel(player, kitType, allArenas);
        } else {
            PotPvPSI.getInstance().getBotPendingManager().prepareDuel(player, botName, kitType, allArenas);
        }
    }

}
