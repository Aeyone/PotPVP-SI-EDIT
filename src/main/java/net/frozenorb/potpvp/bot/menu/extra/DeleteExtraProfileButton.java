package net.frozenorb.potpvp.bot.menu.extra;

import com.google.common.collect.ImmutableList;
import net.frozenorb.potpvp.PotPvPSI;
import net.frozenorb.potpvp.bot.config.BotConfig;
import net.frozenorb.potpvp.bot.menu.BotMenuUtils;
import net.frozenorb.qlib.menu.Button;
import net.frozenorb.qlib.menu.menus.ConfirmMenu;
import net.frozenorb.qlib.util.Callback;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.List;

final class DeleteExtraProfileButton extends Button {

    private final String profileName;

    DeleteExtraProfileButton(String profileName) {
        this.profileName = profileName;
    }

    @Override
    public String getName(Player player) {
        return ChatColor.RED.toString() + ChatColor.BOLD + "Delete Extra Profile";
    }

    @Override
    public List<String> getDescription(Player player) {
        return ImmutableList.of(
            ChatColor.GRAY + "Extra Profile: " + ChatColor.WHITE + profileName,
            "",
            ChatColor.RED + "Click to open the confirm menu."
        );
    }

    @Override
    public Material getMaterial(Player player) {
        return Material.TNT;
    }

    @Override
    public void clicked(Player player, int slot, ClickType clickType) {
        if (PotPvPSI.getInstance().getBotConfig().getExtraProfile(profileName) == null) {
            Button.playFail(player);
            BotMenuUtils.reopenExtraProfiles(player);
            return;
        }

        Button.playNeutral(player);
        new ConfirmMenu("Delete " + profileName + "?", new Callback<Boolean>() {

            @Override
            public void callback(Boolean confirmed) {
                if (!confirmed) {
                    new ExtraProfileMenu(profileName).openMenu(player);
                    return;
                }

                BotConfig config = PotPvPSI.getInstance().getBotConfig();
                config.removeExtraProfile(profileName);
                player.sendMessage(ChatColor.RED + "Deleted extra profile " + ChatColor.WHITE + profileName + ChatColor.RED + ".");
                new ExtraProfilesMenu().openMenu(player);
            }

        }).openMenu(player);
    }

}
