package net.frozenorb.potpvp.kittype.menu.select;

import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import com.google.common.collect.ImmutableList;

import lombok.AllArgsConstructor;
import net.frozenorb.qlib.menu.Button;

@AllArgsConstructor
public class SelectStyleButton extends Button {

    private Set<String> allMaps;
    private Set<String> maps;
    String style;

    @Override
    public List<String> getDescription(Player arg0) {
        return ImmutableList.of();
    }

    @Override
    public Material getMaterial(Player arg0) {
        return maps.containsAll(allMaps) ? Material.REDSTONE_TORCH_ON : Material.LEVER;
    }

    @Override
    public String getName(Player arg0) {
        return maps.containsAll(allMaps) ?
                ChatColor.RED + "Disable all " + ChatColor.AQUA + style + " Style " + ChatColor.RED + "maps" :
                ChatColor.GREEN + "Enable all " + ChatColor.AQUA + style + " Style " + ChatColor.GREEN + "maps";
    }

    @Override
    public void clicked(Player player, int slot, ClickType clickType) {
        Button.playNeutral(player);
        if (maps.containsAll(allMaps)) {
            maps.removeIf(name -> allMaps.contains(name));
        } else {
            maps.addAll(allMaps);
        }
    }

}
