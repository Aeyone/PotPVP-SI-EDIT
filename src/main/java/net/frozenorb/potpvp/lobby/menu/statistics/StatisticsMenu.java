package net.frozenorb.potpvp.lobby.menu.statistics;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.frozenorb.potpvp.PotPvPSI;
import net.frozenorb.potpvp.lobby.menu.matchhistory.MatchHistoryMenuButton;
import net.frozenorb.qlib.util.UUIDUtils;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import net.frozenorb.potpvp.kittype.KitType;
import net.frozenorb.potpvp.lobby.menu.statistics.button.KitButton;
import net.frozenorb.potpvp.lobby.menu.statistics.button.PlayerButton;
import net.frozenorb.qlib.menu.Button;
import net.frozenorb.qlib.menu.Menu;
import net.frozenorb.qlib.util.ItemBuilder;

public final class StatisticsMenu extends Menu {

    private static final Button BLACK_PANE = Button.fromItem(ItemBuilder.of(Material.STAINED_GLASS_PANE).data(DyeColor.BLACK.getData()).name(" ").build());
    private UUID target;

    public StatisticsMenu(UUID target) {
        this.target = target;
        setAutoUpdate(true);

        StatisticsHandler statisticsHandler = PotPvPSI.getInstance().getStatisticsHandler();
        if (statisticsHandler.checkNull(target)) {
            statisticsHandler.loadStatistics(target);
        }
    }

    @Override
    public String getTitle(Player player) {
        return ChatColor.GOLD.toString() + ChatColor.BOLD + UUIDUtils.name(target);
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        buttons.put(getSlot(1, 1), new PlayerButton(target));
        buttons.put(getSlot(1, 3), new MatchHistoryMenuButton(target));

        int y = 1;
        int x = 3;

        for (KitType kitType : KitType.getAllTypes()) {
            if (!kitType.isSupportsRanked()) continue;

            buttons.put(getSlot(x++, y), new KitButton(kitType, target));

            if (x == 8) {
                y++;
                x = 3;
            }
        }

        for (int i = 0; i < 45; i++) {
            buttons.putIfAbsent(i, BLACK_PANE);
        }

        return buttons;
    }

    @Override
    public int size(Map<Integer, Button> buttons) {
        return 9 * 5;
    }

}