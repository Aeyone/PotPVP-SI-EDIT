package net.frozenorb.potpvp.bot.menu.profile;

import com.google.common.collect.ImmutableList;
import net.frozenorb.potpvp.PotPvPSI;
import net.frozenorb.potpvp.bot.config.BotConfig;
import net.frozenorb.qlib.menu.Button;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.List;

final class BotExtraProfileToggleButton extends Button {

    private final String botName;
    private final String profileName;

    BotExtraProfileToggleButton(String botName, String profileName) {
        this.botName = botName;
        this.profileName = profileName;
    }

    @Override
    public String getName(Player player) {
        return (isEnabled() ? ChatColor.GREEN : ChatColor.RED) + profileName;
    }

    @Override
    public List<String> getDescription(Player player) {
        return ImmutableList.of(
            ChatColor.GRAY + "Status: " + (isEnabled() ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"),
            "",
            ChatColor.YELLOW + "Click to toggle this extra profile."
        );
    }

    @Override
    public Material getMaterial(Player player) {
        return Material.WOOL;
    }

    @Override
    public byte getDamageValue(Player player) {
        return (isEnabled() ? DyeColor.LIME : DyeColor.RED).getWoolData();
    }

    @Override
    public void clicked(Player player, int slot, ClickType clickType) {
        BotConfig config = PotPvPSI.getInstance().getBotConfig();
        boolean enabled = !config.isExtraProfileEnabled(botName, profileName);

        if (!config.setExtraProfileEnabled(botName, profileName, enabled)) {
            Button.playFail(player);
            return;
        }

        Button.playNeutral(player);
        new BotExtraProfilesMenu(botName).openMenu(player);
    }

    private boolean isEnabled() {
        return PotPvPSI.getInstance().getBotConfig().isExtraProfileEnabled(botName, profileName);
    }

}
