package net.frozenorb.potpvp.arena.menu.manageschematics;

import net.frozenorb.potpvp.PotPvPSI;
import net.frozenorb.potpvp.arena.ArenaHandler;
import net.frozenorb.potpvp.arena.ArenaSchematic;
import net.frozenorb.potpvp.command.ManageCommand;
import net.frozenorb.potpvp.util.menu.MenuBackButton;
import net.frozenorb.qlib.menu.Button;

import net.frozenorb.qlib.menu.pagination.PaginatedMenu;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public final class ManageSchematicsMenu extends PaginatedMenu {

    public ManageSchematicsMenu() {
        setPlaceholder(true);
        setAutoUpdate(true);
    }

    @Override
    public String getPrePaginatedTitle(Player player) {
        return "Manage Schematics";
    }

    public Map<Integer, Button> getGlobalButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        buttons.put(4, new MenuBackButton(p -> new ManageCommand.ManageMenu().openMenu(p)));
        return buttons;
    }

    @Override
    public Map<Integer, Button> getAllPagesButtons(Player player) {
        ArenaHandler arenaHandler = PotPvPSI.getInstance().getArenaHandler();
        Map<Integer, Button> buttons = new HashMap<>();
        int index = 0;

        for (ArenaSchematic schematic : arenaHandler.getSchematics()) {
            buttons.put(index++, new ManageSchematicButton(schematic));
        }

        return buttons;
    }

    @Override
    public int size(Map<Integer, Button> buttons) {
        return 9 * 6;
    }

    @Override
    public int getMaxItemsPerPage(Player player) {
        return 9 * 5; // top row is dedicated to switching
    }

}