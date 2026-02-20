package net.frozenorb.potpvp.lobby.menu.matchhistory;


import net.frozenorb.qlib.menu.Button;
import com.google.common.collect.Lists;
import net.frozenorb.qlib.util.UUIDUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.List;
import java.util.UUID;

public class MatchHistoryMenuButton extends Button {

    private UUID target;

    public MatchHistoryMenuButton(UUID target) {
        this.target = target;
    }

    @Override
    public String getName(Player player) {
        return ChatColor.GOLD.toString() + ChatColor.BOLD + "Match History";
    }

    @Override
    public List<String> getDescription(Player player) {
        List<String> description = Lists.newArrayList();
        boolean isSelf = target.equals(player.getUniqueId());
        description.add(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "----------------------");
        description.add(ChatColor.GRAY + "View in-depth information");
        description.add(ChatColor.GRAY + "about " + (isSelf ? "your " : UUIDUtils.name(target) + "'s ") + "past fights.");
        description.add("");
        description.add(ChatColor.GOLD + "Click to View!");
        description.add(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "----------------------");

        return description;
    }

    @Override
    public Material getMaterial(Player player) {
        return Material.BOOK;
    }

    @Override
    public void clicked(Player player, int slot, ClickType clickType) {
        Button.playNeutral(player);
        new MatchHistoryMenu(target).openMenu(player);
    }
}
