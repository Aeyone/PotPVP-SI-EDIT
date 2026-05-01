package net.frozenorb.potpvp.bot.menu;

import com.google.common.collect.ImmutableList;
import net.frozenorb.qlib.menu.Button;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.List;

final class AddBotButton extends Button {

    @Override
    public String getName(Player player) {
        return ChatColor.GREEN.toString() + ChatColor.BOLD + "Add Bot";
    }

    @Override
    public List<String> getDescription(Player player) {
        return ImmutableList.of(
            "",
            ChatColor.YELLOW + "Click to enter a new username."
        );
    }

    @Override
    public Material getMaterial(Player player) {
        return Material.EMERALD;
    }

    @Override
    public void clicked(Player player, int slot, ClickType clickType) {
        Button.playNeutral(player);
        BotMenuUtils.startAddConversation(player);
    }

}
