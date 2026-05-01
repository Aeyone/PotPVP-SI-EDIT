package net.frozenorb.potpvp.duel.menu;

import com.google.common.collect.ImmutableList;
import net.frozenorb.qlib.menu.Button;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.List;

final class RandomBotDuelButton extends Button {

    @Override
    public String getName(Player player) {
        return ChatColor.AQUA.toString() + ChatColor.BOLD + "Random";
    }

    @Override
    public List<String> getDescription(Player player) {
        return ImmutableList.of(
            "",
            ChatColor.YELLOW + "Click to duel a random player."
        );
    }

    @Override
    public Material getMaterial(Player player) {
        return Material.NETHER_STAR;
    }

    @Override
    public void clicked(Player player, int slot, ClickType clickType) {
        Button.playNeutral(player);
        BotDuelMenuUtils.openKitSelection(player, null);
    }

}
