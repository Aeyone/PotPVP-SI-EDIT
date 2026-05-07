package net.frozenorb.potpvp.bot.menu.profile;

import com.google.common.collect.ImmutableList;
import net.frozenorb.potpvp.PotPvPSI;
import net.frozenorb.qlib.menu.Button;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.List;

final class BotExtraProfilesButton extends Button {

    private final String botName;

    BotExtraProfilesButton(String botName) {
        this.botName = botName;
    }

    @Override
    public String getName(Player player) {
        return ChatColor.AQUA.toString() + ChatColor.BOLD + "Extra Profiles";
    }

    @Override
    public List<String> getDescription(Player player) {
        int enabled = PotPvPSI.getInstance().getBotConfig().getBot(botName) == null ? 0 : PotPvPSI.getInstance().getBotConfig().getBot(botName).getEnabledExtraProfiles().size();
        return ImmutableList.of(
            ChatColor.GRAY + "Enabled: " + ChatColor.WHITE + enabled,
            "",
            ChatColor.YELLOW + "Click to choose enabled extra profiles."
        );
    }

    @Override
    public Material getMaterial(Player player) {
        return Material.BOOK;
    }

    @Override
    public void clicked(Player player, int slot, ClickType clickType) {
        Button.playNeutral(player);
        new BotExtraProfilesMenu(botName).openMenu(player);
    }

}
