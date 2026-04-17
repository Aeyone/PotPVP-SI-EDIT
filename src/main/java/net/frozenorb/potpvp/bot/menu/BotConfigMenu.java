package net.frozenorb.potpvp.bot.menu;

import net.frozenorb.potpvp.bot.command.BotCommand;
import net.frozenorb.qlib.menu.Button;
import net.frozenorb.qlib.menu.Menu;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Map;

public class BotConfigMenu extends Menu {

    public BotConfigMenu() {
        super("Bot Config");
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        return Collections.emptyMap();
    }
}
