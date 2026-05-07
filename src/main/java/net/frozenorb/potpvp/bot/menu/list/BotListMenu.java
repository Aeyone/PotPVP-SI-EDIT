package net.frozenorb.potpvp.bot.menu.list;

import net.frozenorb.potpvp.PotPvPSI;
import net.frozenorb.qlib.menu.Button;
import net.frozenorb.qlib.menu.pagination.PaginatedMenu;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BotListMenu extends PaginatedMenu {

    public BotListMenu() {
        setPlaceholder(true);
    }

    @Override
    public String getPrePaginatedTitle(Player player) {
        PotPvPSI plugin = PotPvPSI.getInstance();
        return "Bot List (" + plugin.getBotManager().getList().size() + "/" + plugin.getBotConfig().getBotNames().size() + ")";
    }

    @Override
    public Map<Integer, Button> getAllPagesButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        List<String> botNames = new ArrayList<>(PotPvPSI.getInstance().getBotManager().getList());
        int index = 0;

        Collections.sort(botNames, String.CASE_INSENSITIVE_ORDER);
        for (String botName : botNames) {
            buttons.put(index++, new BotListButton(botName));
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
