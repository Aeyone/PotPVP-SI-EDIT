package net.frozenorb.potpvp.kittype.menu.select;

import java.util.List;
import java.util.Set;
import java.beans.ConstructorProperties;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import com.google.common.collect.ImmutableList;

import lombok.AllArgsConstructor;
import net.frozenorb.qlib.menu.Button;
import net.frozenorb.qlib.util.Callback;

@AllArgsConstructor
public class SendDuelButton extends Button {
    
    private Set<String> maps;
    private Callback<Set<String>> mapsCallback;
    
    @Override
    public List<String> getDescription(Player arg0) {
        return ImmutableList.of();
    }

    @Override
    public Material getMaterial(Player arg0) {
        return Material.DIAMOND_SWORD;
    }
    
    @Override
    public String getName(Player player) {
        return ChatColor.GREEN + "Send duel";
    }
    
    @Override
    public void clicked(Player player, int slot, ClickType clickType) {
        Button.playNeutral(player);
        if (maps.size() < 1) {
            player.sendMessage(ChatColor.RED + "You must select at least one map.");
            return;
        }
        
        mapsCallback.callback(maps);
    }

//    @ConstructorProperties({"maps", "mapsCallback"})
//    public SendDuelButton(Set<String> maps, Callback<Set<String>> mapsCallback) {
//        this.maps = maps;
//        this.mapsCallback = mapsCallback;
//    }
}
