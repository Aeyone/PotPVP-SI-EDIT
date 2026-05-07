package net.frozenorb.potpvp.bot.menu.extra;

import com.google.common.collect.ImmutableList;
import net.frozenorb.potpvp.PotPvPSI;
import net.frozenorb.potpvp.bot.config.BotConfig;
import net.frozenorb.potpvp.bot.menu.BotMenuUtils;
import net.frozenorb.qlib.menu.Button;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.List;
import java.util.Map;

final class ExtraProfileParameterButton extends Button {

    private final String profileName;
    private final String parameterId;

    ExtraProfileParameterButton(String profileName, String parameterId) {
        this.profileName = profileName;
        this.parameterId = parameterId;
    }

    @Override
    public String getName(Player player) {
        return ChatColor.YELLOW + PotPvPSI.getInstance().getBotConfig().getParameterShowName(parameterId);
    }

    @Override
    public List<String> getDescription(Player player) {
        BotConfig config = PotPvPSI.getInstance().getBotConfig();
        Map<String, Double> profile = config.getExtraProfile(profileName);

        if (profile == null || !profile.containsKey(parameterId)) {
            return ImmutableList.of(ChatColor.RED + "Parameter not found.");
        }

        return ImmutableList.of(
            ChatColor.GRAY + "Current extra value: " + ChatColor.WHITE + BotMenuUtils.format(config.isIntegerParameter(parameterId), profile.get(parameterId)),
            "",
            ChatColor.YELLOW + "Click to edit this value."
        );
    }

    @Override
    public Material getMaterial(Player player) {
        return Material.PAPER;
    }

    @Override
    public void clicked(Player player, int slot, ClickType clickType) {
        if (PotPvPSI.getInstance().getBotConfig().getExtraProfile(profileName) == null) {
            Button.playFail(player);
            BotMenuUtils.reopenExtraProfiles(player);
            return;
        }

        Button.playNeutral(player);
        BotMenuUtils.startExtraProfileValueConversation(player, profileName, parameterId);
    }

}
