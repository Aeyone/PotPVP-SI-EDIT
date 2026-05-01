package net.frozenorb.potpvp.duel.menu;

import com.google.common.collect.ImmutableList;
import net.frozenorb.potpvp.PotPvPSI;
import net.frozenorb.qlib.menu.Button;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.List;

final class BotDuelButton extends Button {

    private final String botId;

    BotDuelButton(String botId) {
        this.botId = botId;
    }

    @Override
    public String getName(Player player) {
        return ChatColor.AQUA + botId;
    }

    @Override
    public List<String> getDescription(Player player) {
        return ImmutableList.of(
            "",
            ChatColor.YELLOW + "Click to duel."
        );
    }

    @Override
    public Material getMaterial(Player player) {
        return Material.SKULL_ITEM;
    }

    @Override
    public byte getDamageValue(Player player) {
        return 3;
    }

    @Override
    public void clicked(Player player, int slot, ClickType clickType) {
        if (PotPvPSI.getInstance().getBotPendingManager().isNameReserved(botId)) {
            Button.playFail(player);
            player.sendMessage(ChatColor.RED + botId + " is already active or loading.");
            return;
        }

        Button.playNeutral(player);
        BotDuelMenuUtils.openKitSelection(player, botId);
    }

}
