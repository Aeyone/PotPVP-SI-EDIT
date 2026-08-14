package net.frozenorb.potpvp.lobby;

import net.frozenorb.qlib.util.ItemUtils;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import lombok.experimental.UtilityClass;

import static net.frozenorb.potpvp.PotPvPLang.LEFT_ARROW;
import static net.frozenorb.potpvp.PotPvPLang.RIGHT_ARROW;
import static org.bukkit.ChatColor.*;

@UtilityClass
public final class LobbyItems {

    public static final ItemStack SPECTATE_RANDOM_ITEM = new ItemStack(Material.COMPASS);
    public static final ItemStack SPECTATE_MENU_ITEM = new ItemStack(Material.PAPER);
    public static final ItemStack ENABLE_SPEC_MODE_ITEM = new ItemStack(Material.REDSTONE_TORCH_ON);
    public static final ItemStack DISABLE_SPEC_MODE_ITEM = new ItemStack(Material.LEVER);
    public static final ItemStack MANAGE_ITEM = new ItemStack(Material.ANVIL);
    public static final ItemStack UNFOLLOW_ITEM = new ItemStack(Material.INK_SACK, 1, DyeColor.RED.getDyeData());
    public static final ItemStack PLAYER_STATISTICS = new ItemStack(Material.EMERALD, 1, (byte) 3);
    public static final ItemStack SETTINGS_ITEM = new ItemStack(Material.ITEM_FRAME);
    public static final ItemStack FIGHT_WITH_BOT_ITEM = new ItemStack(Material.GOLD_SWORD);

    static {
        ItemUtils.setDisplayName(
            SPECTATE_RANDOM_ITEM, 
            LEFT_ARROW + 
            YELLOW + BOLD + "Spectate Random Match" +
            RIGHT_ARROW
        );

        ItemUtils.setDisplayName(
            SPECTATE_MENU_ITEM, 
            LEFT_ARROW + 
            GREEN + BOLD + "Spectate Menu" +
            RIGHT_ARROW
        );

        ItemUtils.setDisplayName(
            ENABLE_SPEC_MODE_ITEM,
            LEFT_ARROW + 
            AQUA + BOLD + "Enable Spectator Mode" +
            RIGHT_ARROW
        );

        ItemUtils.setDisplayName(
            DISABLE_SPEC_MODE_ITEM, 
            LEFT_ARROW + 
            AQUA + BOLD + "Disable Spectator Mode" +
            RIGHT_ARROW
        );

        ItemUtils.setDisplayName(
            MANAGE_ITEM, 
            LEFT_ARROW + 
            GRAY + BOLD + "Manage PotPvP" +
            RIGHT_ARROW
        );

        ItemUtils.setDisplayName(
            UNFOLLOW_ITEM, 
            LEFT_ARROW + 
            RED + BOLD + "Stop Following" +
            RIGHT_ARROW
        );

        ItemUtils.setDisplayName(
            PLAYER_STATISTICS, 
            LEFT_ARROW + 
            LIGHT_PURPLE + BOLD + "Statistics" +
            RIGHT_ARROW
        );

        ItemUtils.setDisplayName(
            SETTINGS_ITEM,
            LEFT_ARROW +
            GOLD + BOLD + "Settings" +
            RIGHT_ARROW
        );

        ItemUtils.setDisplayName(
            FIGHT_WITH_BOT_ITEM,
            LEFT_ARROW +
            YELLOW + BOLD + "Fight With Bot" +
            RIGHT_ARROW
        );
    }

}