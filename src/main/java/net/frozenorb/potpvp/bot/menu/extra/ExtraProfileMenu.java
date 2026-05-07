package net.frozenorb.potpvp.bot.menu.extra;

import net.frozenorb.potpvp.PotPvPSI;
import net.frozenorb.potpvp.util.menu.MenuBackButton;
import net.frozenorb.qlib.menu.Button;
import net.frozenorb.qlib.menu.pagination.PaginatedMenu;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class ExtraProfileMenu extends PaginatedMenu {

    private final String profileName;

    public ExtraProfileMenu(String profileName) {
        this.profileName = profileName;
        setPlaceholder(true);
    }

    @Override
    public String getPrePaginatedTitle(Player player) {
        return profileName;
    }

    @Override
    public Map<Integer, Button> getGlobalButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        buttons.put(4, new MenuBackButton(p -> new ExtraProfilesMenu().openMenu(p)));
        buttons.put(53, new DeleteExtraProfileButton(profileName));
        return buttons;
    }

    @Override
    public Map<Integer, Button> getAllPagesButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        int index = 0;

        if (PotPvPSI.getInstance().getBotConfig().getExtraProfile(profileName) == null) {
            return buttons;
        }

        for (String parameterId : PotPvPSI.getInstance().getBotConfig().getExtraProfileParameterIds()) {
            buttons.put(index++, new ExtraProfileParameterButton(profileName, parameterId));
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
