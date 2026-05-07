package net.frozenorb.potpvp.bot.menu.extra;

import com.google.common.collect.ImmutableList;
import net.frozenorb.potpvp.bot.menu.BotMenuUtils;
import net.frozenorb.qlib.menu.Button;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.List;

final class AddExtraProfileButton extends Button {

    @Override
    public String getName(Player player) {
        return ChatColor.GREEN.toString() + ChatColor.BOLD + "Add Extra Profile";
    }

    @Override
    public List<String> getDescription(Player player) {
        return ImmutableList.of(
            "",
            ChatColor.YELLOW + "Click to enter a new extra profile id."
        );
    }

    @Override
    public Material getMaterial(Player player) {
        return Material.EMERALD;
    }

    @Override
    public void clicked(Player player, int slot, ClickType clickType) {
        Button.playNeutral(player);
        BotMenuUtils.startAddExtraProfileConversation(player);
    }

}
