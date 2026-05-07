package net.frozenorb.potpvp.bot.menu;

import net.frozenorb.potpvp.PotPvPSI;
import net.frozenorb.potpvp.bot.config.BotProfile;
import net.frozenorb.potpvp.bot.config.ParameterRange;
import net.frozenorb.potpvp.bot.menu.profile.BotProfileMenu;
import net.frozenorb.qlib.menu.Button;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class BotButton extends Button {

    private final String botName;

    BotButton(String botName) {
        this.botName = botName;
    }

    @Override
    public String getName(Player player) {
        return ChatColor.YELLOW + botName;
    }

    @Override
    public List<String> getDescription(Player player) {
        List<String> list = new ArrayList<>();
        BotProfile profile = PotPvPSI.getInstance().getBotConfig().getBot(botName);

        if (profile == null) {
            return list;
        }

        list.add(ChatColor.GRAY + "Status: " + (Bukkit.getPlayer(botName) != null ? ChatColor.GREEN + "Online" : ChatColor.RED + "Offline"));
        list.add("");
        for (Map.Entry<String, ParameterRange> entry : profile.getParameters().entrySet()) {
            String name = entry.getValue().getShowName();
            ParameterRange range = entry.getValue();

            list.add(
                ChatColor.GREEN +
                name + ": " +
                ChatColor.WHITE +
                BotMenuUtils.format(range, range.getMin()) +
                " - " +
                BotMenuUtils.format(range, range.getMax())
            );
        }

        return list;
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
        Button.playNeutral(player);
        new BotProfileMenu(botName).openMenu(player);
    }

}
