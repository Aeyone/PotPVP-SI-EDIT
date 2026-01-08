package net.frozenorb.potpvp.fakechat;

import net.frozenorb.potpvp.PotPvPSI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class FakeChatGUI implements Listener {

    private static final String GUI_TITLE = ChatColor.DARK_GRAY + "Fake Chat Settings";
    private static final String SPEED_GUI_TITLE = ChatColor.DARK_GRAY + "Select Chat Speed";

    public static void openGUI(Player player, PotPvPSI plugin) {
        Inventory inv = Bukkit.createInventory(null, 27, GUI_TITLE);
        FakeChatManager manager = plugin.getFakeChatManager();

        boolean active = manager.hasActiveSession(player);
        boolean famous = active && manager.getSession(player).famous;

        ItemStack toggleItem = createItem(
                active ? Material.REDSTONE_BLOCK : Material.EMERALD_BLOCK,
                active ? ChatColor.RED + "Stop Fake Chat" : ChatColor.GREEN + "Start Fake Chat",
                active ? ChatColor.GRAY + "Click to disable" : ChatColor.GRAY + "Click to enable"
        );
        inv.setItem(11, toggleItem);

        ItemStack speedItem = createItem(
                Material.WATCH,
                ChatColor.YELLOW + "Chat Speed",
                ChatColor.GRAY + "Adjust message frequency",
                "",
                ChatColor.WHITE + "Click to change"
        );
        inv.setItem(13, speedItem);

        ItemStack famousToggle = createItem(
                famous ? Material.DIAMOND : Material.COAL,
                famous ? ChatColor.YELLOW + "Famous Mode: ON" : ChatColor.GRAY + "Famous Mode: OFF",
                famous ? ChatColor.GRAY + "Players will mention you" : ChatColor.GRAY + "Normal fake chat",
                "",
                famous ? ChatColor.RED + "Click to turn off" : ChatColor.GREEN + "Click to turn on"
        );
        inv.setItem(15, famousToggle);

        ItemStack infoItem = createItem(
                Material.SIGN,
                ChatColor.GOLD + "Information",
                ChatColor.GRAY + "Only you can see these messages",
                ChatColor.GRAY + "Simulates real player chat",
                "",
                ChatColor.YELLOW + "Speed: 0.5x - 3.0x",
                ChatColor.YELLOW + "Default: 1.0x (Normal)"
        );
        inv.setItem(22, infoItem);

        player.openInventory(inv);
    }

    public static void openSpeedGUI(Player player, PotPvPSI plugin) {
        Inventory inv = Bukkit.createInventory(null, 54, SPEED_GUI_TITLE);

        ItemStack info = createItem(
                Material.PAPER,
                ChatColor.GOLD + "Speed Multiplier",
                ChatColor.GRAY + "Select message frequency",
                "",
                ChatColor.YELLOW + "Lower = Slower | Higher = Faster"
        );
        inv.setItem(4, info);

        String[] speeds = {"0.5x", "0.6x", "0.7x", "0.8x", "0.9x", "1.0x", "1.2x", "1.5x", "2.0x", "3.0x"};
        String[] descriptions = {
                "Very Slow", "Slow", "Slower", "Slightly Slow", "Bit Slow",
                "Normal", "Bit Fast", "Fast", "Very Fast", "Ultra Fast"
        };

        for (int i = 0; i < 10; i++) {
            int level = i + 1;
            Material material = Material.STAINED_CLAY;
            byte data;

            if (level <= 3) data = 11;
            else if (level <= 5) data = 9;
            else if (level == 6) data = 4;
            else if (level <= 8) data = 1;
            else data = 14;

            ItemStack item = new ItemStack(material, level, data);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.YELLOW + "Speed: " + speeds[i]);
            meta.setLore(Arrays.asList(
                    ChatColor.WHITE + descriptions[i],
                    "",
                    level == 10 ? ChatColor.GREEN + "Recommended" : ChatColor.GRAY + "Click to select"
            ));
            item.setItemMeta(meta);

            int slot = i < 5 ? 19 + i : 28 + (i - 5);
            inv.setItem(slot, item);
        }

        ItemStack back = createItem(
                Material.ARROW,
                ChatColor.RED + "Back",
                ChatColor.GRAY + "Return to main menu"
        );
        inv.setItem(49, back);

        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        String title = event.getInventory().getTitle();
        if (!title.equals(GUI_TITLE) && !title.equals(SPEED_GUI_TITLE)) {
            return;
        }

        event.setCancelled(true);

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
            return;
        }

        PotPvPSI plugin = PotPvPSI.getInstance();
        if (plugin == null) return;

        FakeChatManager manager = plugin.getFakeChatManager();
        ItemStack item = event.getCurrentItem();
        String itemName = item.hasItemMeta() ? item.getItemMeta().getDisplayName() : "";

        if (title.equals(GUI_TITLE)) {
            if (itemName.contains("Start") || itemName.contains("Stop")) {
                handleToggle(player, manager);
            } else if (itemName.contains("Speed")) {
                openSpeedGUI(player, plugin);
            } else if (itemName.contains("Famous")) {
                handleFamousToggle(player, manager, plugin);
            }
        } else {
            if (itemName.contains("Speed:")) {
                handleSpeedChange(player, manager, item.getAmount());
            } else if (itemName.contains("Back")) {
                openGUI(player, plugin);
            }
        }
    }

    private void handleToggle(Player player, FakeChatManager manager) {
        if (manager.hasActiveSession(player)) {
            manager.stopSession(player);
        } else {
            manager.startSession(player, 10, false);
        }
        player.closeInventory();
    }

    private void handleFamousToggle(Player player, FakeChatManager manager, PotPvPSI plugin) {
        int speed = 6;
        boolean currentFamous = false;

        if (manager.hasActiveSession(player)) {
            FakeChatManager.FakeChatSession session = manager.getSession(player);
            speed = session.speedLevel;
            currentFamous = session.famous;
            manager.stopSession(player);
        }

        boolean newFamous = !currentFamous;
        manager.startSession(player, speed, newFamous);

        String status = newFamous ? ChatColor.YELLOW + "enabled" : ChatColor.GRAY + "disabled";
        player.sendMessage(ChatColor.GRAY + "Famous Mode " + status);
        player.closeInventory();
        openGUI(player, plugin);
    }

    private void handleSpeedChange(Player player, FakeChatManager manager, int speed) {
        boolean wasFamous = false;

        if (manager.hasActiveSession(player)) {
            wasFamous = manager.getSession(player).famous;
            manager.stopSession(player);
        }

        manager.startSession(player, speed, wasFamous);
        double multiplier = FakeChatManager.getSpeedMultiplier(speed);
        player.sendMessage(ChatColor.GREEN + "Speed updated to " + ChatColor.YELLOW + multiplier + "x");
        player.closeInventory();
    }

    private static ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (lore.length > 0) {
            meta.setLore(Arrays.asList(lore));
        }
        item.setItemMeta(meta);
        return item;
    }
}