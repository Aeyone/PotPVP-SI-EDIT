package net.frozenorb.potpvp.bot.menu.extra;

import net.frozenorb.potpvp.PotPvPSI;
import net.frozenorb.potpvp.bot.config.BotConfig;
import net.frozenorb.potpvp.bot.menu.BotMenuUtils;
import net.frozenorb.qlib.menu.Button;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class ExtraProfileButton extends Button {

    private final String profileName;

    ExtraProfileButton(String profileName) {
        this.profileName = profileName;
    }

    @Override
    public String getName(Player player) {
        return ChatColor.YELLOW + profileName;
    }

    @Override
    public List<String> getDescription(Player player) {
        BotConfig config = PotPvPSI.getInstance().getBotConfig();
        Map<String, Double> profile = config.getExtraProfile(profileName);
        List<String> list = new ArrayList<>();

        if (profile == null) {
            return list;
        }

        list.add("");
        for (Map.Entry<String, Double> entry : profile.entrySet()) {
            list.add(ChatColor.GREEN + config.getParameterShowName(entry.getKey()) + ": " + ChatColor.WHITE + BotMenuUtils.format(config.isIntegerParameter(entry.getKey()), entry.getValue()));
        }

        list.add("");
        list.add(ChatColor.YELLOW + "Click to edit this extra profile.");
        return list;
    }

    @Override
    public Material getMaterial(Player player) {
        return Material.BOOK_AND_QUILL;
    }

    @Override
    public void clicked(Player player, int slot, ClickType clickType) {
        Button.playNeutral(player);
        new ExtraProfileMenu(profileName).openMenu(player);
    }

}
