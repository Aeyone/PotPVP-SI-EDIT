package net.frozenorb.potpvp.bot.menu.extra;

import net.frozenorb.potpvp.PotPvPSI;
import net.frozenorb.potpvp.bot.menu.BotConfigMenu;
import net.frozenorb.potpvp.util.menu.MenuBackButton;
import net.frozenorb.qlib.menu.Button;
import net.frozenorb.qlib.menu.pagination.PaginatedMenu;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExtraProfilesMenu extends PaginatedMenu {

    public ExtraProfilesMenu() {
        setPlaceholder(true);
    }

    @Override
    public String getPrePaginatedTitle(Player player) {
        return "Extra Profiles";
    }

    @Override
    public Map<Integer, Button> getGlobalButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        buttons.put(4, new MenuBackButton(p -> new BotConfigMenu().openMenu(p)));

        buttons.put(53, new AddExtraProfileButton());
        return buttons;
    }

    @Override
    public Map<Integer, Button> getAllPagesButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        List<String> profileNames = PotPvPSI.getInstance().getBotConfig().getExtraProfileNames();
        int index = 0;

        for (String profileName : profileNames) {
            buttons.put(index++, new ExtraProfileButton(profileName));
        }

        return buttons;
    }

    @Override
    public int size(Map<Integer, Button> buttons) {
        return 9 * 6;
    }

    @Override
    public int getMaxItemsPerPage(Player player) {
        return 9 * 4;
    }

}
