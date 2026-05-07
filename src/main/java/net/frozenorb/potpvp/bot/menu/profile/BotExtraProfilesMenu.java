package net.frozenorb.potpvp.bot.menu.profile;

import net.frozenorb.potpvp.PotPvPSI;
import net.frozenorb.potpvp.util.menu.MenuBackButton;
import net.frozenorb.qlib.menu.Button;
import net.frozenorb.qlib.menu.pagination.PaginatedMenu;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BotExtraProfilesMenu extends PaginatedMenu {

    private final String botName;

    public BotExtraProfilesMenu(String botName) {
        this.botName = botName;
        setPlaceholder(true);
    }

    @Override
    public String getPrePaginatedTitle(Player player) {
        return "Select Extra Profiles";
    }

    @Override
    public Map<Integer, Button> getGlobalButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        buttons.put(4, new MenuBackButton(p -> new BotProfileMenu(botName).openMenu(p)));
        return buttons;
    }

    @Override
    public Map<Integer, Button> getAllPagesButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        List<String> profileNames = PotPvPSI.getInstance().getBotConfig().getExtraProfileNames();
        int index = 0;

        for (String profileName : profileNames) {
            buttons.put(index++, new BotExtraProfileToggleButton(botName, profileName));
        }

        return buttons;
    }

    @Override
    public int size(Map<Integer, Button> buttons) {
        return 9 * 6;
    }

    @Override
    public int getMaxItemsPerPage(Player player) {
        return 9 * 5;
    }

}
