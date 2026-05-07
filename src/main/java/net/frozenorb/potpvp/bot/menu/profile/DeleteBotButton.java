package net.frozenorb.potpvp.bot.menu.profile;

import com.google.common.collect.ImmutableList;
import net.frozenorb.potpvp.PotPvPSI;
import net.frozenorb.potpvp.bot.config.BotConfig;
import net.frozenorb.potpvp.bot.config.BotProfile;
import net.frozenorb.potpvp.bot.menu.BotConfigMenu;
import net.frozenorb.potpvp.bot.menu.BotMenuUtils;
import net.frozenorb.qlib.menu.Button;
import net.frozenorb.qlib.menu.menus.ConfirmMenu;
import net.frozenorb.qlib.util.Callback;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.List;

final class DeleteBotButton extends Button {

    private final String botName;

    DeleteBotButton(String botName) {
        this.botName = botName;
    }

    @Override
    public String getName(Player player) {
        return ChatColor.RED.toString() + ChatColor.BOLD + "Delete Bot";
    }

    @Override
    public List<String> getDescription(Player player) {
        return ImmutableList.of(
            ChatColor.GRAY + "Bot: " + ChatColor.WHITE + botName,
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
        BotConfig config = PotPvPSI.getInstance().getBotConfig();
        BotProfile profile = config.getBot(botName);

        if (profile == null) {
            Button.playFail(player);
            BotMenuUtils.reopenBotList(player);
            return;
        }

        Button.playNeutral(player);
        new ConfirmMenu("Delete " + profile.getName() + "?", new Callback<Boolean>() {

            @Override
            public void callback(Boolean confirmed) {
                if (!confirmed) {
                    new BotProfileMenu(profile.getName()).openMenu(player);
                    return;
                }

                PotPvPSI.getInstance().getBotManager().delBot(profile.getName());
                config.removeBot(profile.getName());
                player.sendMessage(ChatColor.RED + "Deleted bot " + ChatColor.WHITE + profile.getName() + ChatColor.RED + ".");
                new BotConfigMenu().openMenu(player);
            }

        }).openMenu(player);
    }

}
