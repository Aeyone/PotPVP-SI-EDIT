package net.frozenorb.potpvp.arena.menu.select;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import net.frozenorb.potpvp.kittype.menu.select.SelectStyleButton;
import net.frozenorb.qlib.menu.pagination.PaginatedMenu;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.frozenorb.potpvp.PotPvPSI;
import net.frozenorb.potpvp.arena.ArenaSchematic;
import net.frozenorb.potpvp.kittype.KitType;
import net.frozenorb.potpvp.kittype.menu.select.SendDuelButton;
import net.frozenorb.potpvp.kittype.menu.select.ToggleAllButton;
import net.frozenorb.potpvp.match.MatchHandler;
import net.frozenorb.qlib.menu.Button;
import net.frozenorb.qlib.util.Callback;

public class SelectArenaMenu extends PaginatedMenu {

    private KitType kitType;
    private Callback<Set<String>> mapsCallback;
    private String title;
    Set<String> allMaps;
    Set<String> enabledSchematics = new HashSet<>();
    Set<String> kohiStyle = new HashSet<>();
    Set<String> potpvpStyle = new HashSet<>();
    Set<String> practiceStyle = new HashSet<>();
    
    public SelectArenaMenu(KitType kitType, Callback<Set<String>> mapsCallback, String title) {
        setPlaceholder(true);
        setAutoUpdate(true);

        this.title = title;
        this.kitType = kitType;
        this.mapsCallback = mapsCallback;
        PotPvPSI.getInstance().getArenaHandler().getStyleSchematics(kitType, kohiStyle, potpvpStyle, practiceStyle, enabledSchematics);

        this.allMaps = new TreeSet<>(this.enabledSchematics);

        //Set all closed
        enabledSchematics.clear();
    }

    @Override
    public String getPrePaginatedTitle(Player player) {
        return (ChatColor.BLUE.toString() + ChatColor.BOLD + title);
    }

    @Override
    public Map<Integer, Button> getGlobalButtons(Player player) {
        Map<Integer, Button> buttons = Maps.newHashMap();
        buttons.put(45, new ToggleAllButton(allMaps, enabledSchematics));
        buttons.put(46, new SelectStyleButton(kohiStyle, enabledSchematics, "Kohi"));
        buttons.put(47, new SelectStyleButton(potpvpStyle, enabledSchematics, "PotPvP"));
        buttons.put(48, new SelectStyleButton(practiceStyle, enabledSchematics, "Practice"));
        buttons.put(53, new SendDuelButton(enabledSchematics, mapsCallback));
        return buttons;
    }

    @Override
    public Map<Integer, Button> getAllPagesButtons(Player player) {
        Map<Integer, Button> buttons = Maps.newHashMap();

        int i = 0;
        for (String mapName : allMaps) {
            buttons.put(i++, new ArenaButton(mapName, enabledSchematics));
        }
        return buttons;
    }
    // we lock the size of this inventory at full, otherwise we'll have
    // issues if it 'grows' into the next line while it's open (say we open
    // the menu with 8 entries, then it grows to 11 [and onto the second row]
    // - this breaks things)
    @Override
    public int size(Map<Integer, Button> buttons) {
        return 9 * 6;
    }

    @Override
    public int getMaxItemsPerPage(Player player) {
        return 9 * 4; // top row is dedicated to switching
    }
}
