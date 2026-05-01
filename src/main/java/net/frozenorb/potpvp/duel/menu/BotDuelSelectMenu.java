package net.frozenorb.potpvp.duel.menu;

import net.frozenorb.potpvp.PotPvPSI;
import net.frozenorb.qlib.menu.Button;
import net.frozenorb.qlib.menu.pagination.PaginatedMenu;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BotDuelSelectMenu extends PaginatedMenu {

    public BotDuelSelectMenu() {
        setPlaceholder(true);
    }

    @Override
    public String getPrePaginatedTitle(Player player) {
        return "Select Player";
    }

    @Override
    public Map<Integer, Button> getGlobalButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        buttons.put(4, new RandomBotDuelButton());
        return buttons;
    }

    @Override
    public Map<Integer, Button> getAllPagesButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        List<String> botIds = PotPvPSI.getInstance().getBotConfig().getBotIds();
        int index = 0;

        Collections.sort(botIds, String.CASE_INSENSITIVE_ORDER);
        for (String botId : botIds) {
            buttons.put(index++, new BotDuelButton(botId));
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
