package net.frozenorb.potpvp.bot.menu;

import com.google.common.collect.ImmutableList;
import net.frozenorb.potpvp.PotPvPSI;
import net.frozenorb.potpvp.bot.config.BotProfile;
import net.frozenorb.potpvp.bot.config.ParameterRange;
import net.frozenorb.qlib.menu.Button;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.List;

final class BotParameterButton extends Button {

    private final String botId;
    private final String parameter;

    BotParameterButton(String botId, String parameter) {
        this.botId = botId;
        this.parameter = parameter;
    }

    @Override
    public String getName(Player player) {
        ParameterRange range = getRange();
        return ChatColor.YELLOW + (range == null || range.getShowName() == null ? parameter : range.getShowName());
    }

    @Override
    public List<String> getDescription(Player player) {
        ParameterRange range = getRange();

        if (range == null) {
            return ImmutableList.of(ChatColor.RED + "Parameter not found.");
        }

        return ImmutableList.of(
            ChatColor.GRAY + "Current range: " + ChatColor.WHITE + BotMenuUtils.format(range, range.getMin()) + " - " + BotMenuUtils.format(range, range.getMax()),
            "",
            ChatColor.GREEN + "Left-Click to edit minimum",
            ChatColor.RED + "Right-Click to edit maximum"
        );
    }

    @Override
    public Material getMaterial(Player player) {
        return Material.PAPER;
    }

    @Override
    public void clicked(Player player, int slot, ClickType clickType) {
        if (getRange() == null) {
            Button.playFail(player);
            return;
        }

        Button.playNeutral(player);
        BotMenuUtils.startRangeConversation(player, botId, parameter, !clickType.isRightClick());
    }

    private ParameterRange getRange() {
        BotProfile profile = PotPvPSI.getInstance().getBotConfig().getBot(botId);
        return profile == null ? null : profile.getParameters().get(parameter);
    }

}
