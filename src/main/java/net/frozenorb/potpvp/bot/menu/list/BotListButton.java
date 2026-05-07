package net.frozenorb.potpvp.bot.menu.list;

import net.frozenorb.potpvp.PotPvPSI;
import net.frozenorb.qlib.menu.Button;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class BotListButton extends Button {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");

    private final String botName;

    BotListButton(String botName) {
        this.botName = botName;
    }

    @Override
    public String getName(Player player) {
        return (isOnline() ? ChatColor.GREEN : ChatColor.RED) + botName;
    }

    @Override
    public List<String> getDescription(Player player) {
        List<String> description = new ArrayList<>();
        Map<String, Object> settings = PotPvPSI.getInstance().getBotManager().getSettings(botName);

        description.add(ChatColor.GRAY + "Status: " + (isOnline() ? ChatColor.GREEN + "Online" : ChatColor.RED + "Offline"));

        if (settings.isEmpty()) {
            description.add("");
            description.add(ChatColor.RED + "Runtime settings not found.");
            return description;
        }

        description.add("");
        for (Map.Entry<String, Object> entry : createDisplaySettings(settings).entrySet()) {
            description.add(
                ChatColor.GREEN +
                formatSettingName(entry.getKey()) +
                ": " +
                ChatColor.WHITE +
                formatSettingValue(entry.getValue())
            );
        }

        return description;
    }

    @Override
    public Material getMaterial(Player player) {
        return Material.SKULL_ITEM;
    }

    @Override
    public byte getDamageValue(Player player) {
        return 3;
    }

    private boolean isOnline() {
        return Bukkit.getPlayer(botName) != null;
    }

    private Map<String, Object> createDisplaySettings(Map<String, Object> settings) {
        Map<String, Object> displaySettings = new LinkedHashMap<>();

        for (Map.Entry<String, Object> entry : settings.entrySet()) {
            String name = entry.getKey();

            if (name.equals("horizontalAimSpeed")) {
                displaySettings.put("aimSpeed", average(entry.getValue(), settings.get("verticalAimSpeed")));
            } else if (name.equals("horizontalAimAccuracy")) {
                displaySettings.put("aimAccuracy", average(entry.getValue(), settings.get("verticalAimAccuracy")));
            } else if (name.equals("horizontalErraticness")) {
                displaySettings.put("erraticness", average(entry.getValue(), settings.get("verticalErraticness")));
            } else if (!name.equals("verticalAimSpeed") && !name.equals("verticalAimAccuracy") && !name.equals("verticalErraticness")) {
                displaySettings.put(name, entry.getValue());
            }
        }

        return displaySettings;
    }

    private Object average(Object first, Object second) {
        if (first instanceof Number && second instanceof Number) {
            return (((Number) first).doubleValue() + ((Number) second).doubleValue()) / 2D;
        }

        return first;
    }

    private String formatSettingName(String name) {
        if (name.equals("aimSpeed")) {
            return "Aim Speed";
        } else if (name.equals("aimAccuracy")) {
            return "Aim Accuracy";
        } else if (name.equals("erraticness")) {
            return "Erraticness";
        }

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < name.length(); i++) {
            char character = name.charAt(i);
            if (i == 0) {
                builder.append(Character.toUpperCase(character));
            } else if (Character.isUpperCase(character)) {
                builder.append(' ').append(character);
            } else {
                builder.append(character);
            }
        }

        return builder.toString();
    }

    private String formatSettingValue(Object value) {
        if (value instanceof Number) {
            return DECIMAL_FORMAT.format(((Number) value).doubleValue());
        }

        return String.valueOf(value);
    }

}
