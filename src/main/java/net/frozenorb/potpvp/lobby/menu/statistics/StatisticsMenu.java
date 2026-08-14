package net.frozenorb.potpvp.lobby.menu.statistics;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.frozenorb.potpvp.PotPvPSI;
import net.frozenorb.potpvp.lobby.menu.matchhistory.MatchHistoryMenuButton;
import net.frozenorb.potpvp.postmatchinv.menu.PostMatchMenu;
import net.frozenorb.potpvp.util.menu.MenuBackButton;
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
    private String matchId = null;
    private String targetName;

    public StatisticsMenu(UUID target) {
        this(target, null, null, true);
    }

    public StatisticsMenu(UUID target, String matchId) {
        this(target, matchId, null, true);
    }

    private StatisticsMenu(UUID target, String matchId, String targetName, boolean loadIfMissing) {
        this.matchId = matchId;
        this.targetName = targetName;
        setAutoUpdate(true);
        this.StatisticsMenuInit(target, loadIfMissing);
    }

    public static StatisticsMenu forLoadedStatistics(UUID target, String targetName) {
        return new StatisticsMenu(target, null, targetName, false);
    }

    public void StatisticsMenuInit(UUID target) {
        this.StatisticsMenuInit(target, true);
    }

    private void StatisticsMenuInit(UUID target, boolean loadIfMissing) {
        this.target = target;
        StatisticsHandler statisticsHandler = PotPvPSI.getInstance().getStatisticsHandler();
        if (loadIfMissing && statisticsHandler.checkNull(target)) {
            statisticsHandler.loadStatistics(target);
        }
    }

    @Override
    public String getTitle(Player player) {
        return ChatColor.GOLD.toString() + ChatColor.BOLD + (targetName == null ? UUIDUtils.name(target) : targetName);
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        buttons.put(getSlot(1, 1), new PlayerButton(target));
        if (matchId == null) {
            buttons.put(getSlot(1, 3), new MatchHistoryMenuButton(target));
        } else {
            buttons.put(getSlot(1, 3), new MenuBackButton(p -> {
                new PostMatchMenu(matchId, target).openMenu(p);
            }));
        }

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
