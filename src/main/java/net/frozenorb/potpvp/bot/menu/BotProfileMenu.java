package net.frozenorb.potpvp.bot.menu;

import net.frozenorb.potpvp.PotPvPSI;
import net.frozenorb.potpvp.bot.config.BotProfile;
import net.frozenorb.potpvp.bot.config.ParameterRange;
import net.frozenorb.potpvp.util.menu.MenuBackButton;
import net.frozenorb.qlib.menu.Button;
import net.frozenorb.qlib.menu.pagination.PaginatedMenu;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class BotProfileMenu extends PaginatedMenu {

    private final String botId;

    public BotProfileMenu(String botId) {
        this.botId = botId;
        setPlaceholder(true);
    }

    @Override
    public String getPrePaginatedTitle(Player player) {
        return botId;
    }

    @Override
    public Map<Integer, Button> getGlobalButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        buttons.put(4, new MenuBackButton(p -> new BotConfigMenu().openMenu(p)));
        buttons.put(6, new DeleteBotButton(botId));
        return buttons;
    }

    @Override
    public Map<Integer, Button> getAllPagesButtons(Player player) {
        BotProfile profile = PotPvPSI.getInstance().getBotConfig().getBot(botId);
        Map<Integer, Button> buttons = new HashMap<>();
        int index = 0;

        if (profile == null) {
            return buttons;
        }

        for (Map.Entry<String, ParameterRange> entry : profile.getParameters().entrySet()) {
            buttons.put(index++, new BotParameterButton(profile.getId(), entry.getKey()));
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
