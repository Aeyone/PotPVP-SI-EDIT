package net.frozenorb.potpvp.bot;

import lombok.Getter;
import net.frozenorb.potpvp.PotPvPSI;
import net.frozenorb.potpvp.kittype.KitType;
import net.frozenorb.potpvp.util.Skin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class BotMatchManager {

    public static List<String> NAME = new ArrayList<>(
        Arrays.asList(
            "DefeatBoy",
            "idiol",
            "GANGMEMBERHOW2",
            "ZIBLACKINGGG",
            "Zeynah",
            "Glory",
            "Verzide",
            "BCZ",
            "Marcel",
            "Stimpay",
            "Dreamer_420",
            "Tryhard",
            "Zefew",
            "DANTEH",
            "Zi_Min",
            "Xetha",
            "Jewdah",
            "Airbus",
            "Fearless_420",
            "DrummerReviews",
            "PotFast",
            "clare",
            "itsjhalt",
            "Vious",
            "Kevstah",
            "Latenci",
            "MeeZoid",
            "DaGoldBrick",
            "Apexay",
            "Tylarzz",
            "Topu",
            "iSparkton",
            "Reboting",
            "ImHacking",
            "Miami",
            "Hydrize",
            "Demolishing"
        )
    );

    @Getter private final Map<String, BotPendingData> pendingBot = new ConcurrentHashMap<>();
    private final Set<String> loadingBotNames = ConcurrentHashMap.newKeySet();

    public BotMatchManager() {
        Skin.loadCache();
    }

    public void prepareDuel(Player player, KitType kitType, Set<String> allArenas) {
        prepareBot(player, kitType, allArenas, BotPendingType.DUEL);
    }

    public void prepareQueue(Player player, KitType kitType) {
        prepareBot(player, kitType, null, BotPendingType.QUEUE);
    }

    public void prepareManualAdd(Player player, String name) {
        if (player == null || !player.isOnline() || name == null || name.trim().isEmpty()) {
            return;
        }

        String botName = name.trim();
        if (Skin.getCachedSkin(botName) != null) {
            addManualBot(player, botName);
            return;
        }

        String botNameKey = nameKey(botName);
        if (!loadingBotNames.add(botNameKey)) {
            player.sendMessage(ChatColor.RED + botName + " is already loading skin data.");
            return;
        }

        player.sendMessage(ChatColor.YELLOW + "Loading skin for " + ChatColor.AQUA + botName + ChatColor.YELLOW + "...");
        Skin.getSkinByName(botName).whenComplete((skin, throwable) -> Bukkit.getScheduler().runTask(PotPvPSI.getInstance(), () -> {
            try {
                if (throwable != null) {
                    throwable.printStackTrace();
                    player.sendMessage(ChatColor.YELLOW + "Failed to load skin for " + botName + ", adding bot without cached skin.");
                } else if (skin == null || !skin.isComplete()) {
                    player.sendMessage(ChatColor.YELLOW + "Failed to load skin for " + botName + ", adding bot without cached skin.");
                }

                if (player.isOnline()) {
                    addManualBot(player, botName);
                }
            } finally {
                loadingBotNames.remove(botNameKey);
            }
        }));
    }

    private void prepareBot(Player player, KitType kitType, Set<String> allArenas, BotPendingType pendingType) {
        if (player == null || !player.isOnline()) {
            return;
        }

        String name = randomAvailableName();
        if (name == null) {
            return;
        }

        if (Skin.getCachedSkin(name) != null) {
            startBot(name, player, kitType, allArenas, pendingType);
            return;
        }

        String key = nameKey(name);
        if (!loadingBotNames.add(key)) {
            return;
        }

        Skin.getSkinByName(name).whenComplete((skin, throwable) -> Bukkit.getScheduler().runTask(PotPvPSI.getInstance(), () -> {
            try {
                if (throwable != null) {
                    throwable.printStackTrace();
                    Bukkit.getLogger().warning("Failed to load skin for bot " + name + ", spawning without cached skin.");
                } else if (skin == null || !skin.isComplete()) {
                    Bukkit.getLogger().warning("Failed to load skin for bot " + name + ".");
                }

                if (player.isOnline() && !isNameActive(name)) {
                    startBot(name, player, kitType, allArenas, pendingType);
                }
            } finally {
                loadingBotNames.remove(key);
            }
        }));
    }

    private void startBot(String name, Player player, KitType kitType, Set<String> allArenas, BotPendingType pendingType) {
        if (isNameActive(name)) {
            return;
        }

//        String name = PotPvPSI.getInstance().getFakeChatManager().getConfig().generateRandomNickname();
        BotManager botManager = PotPvPSI.getInstance().getBotManager();
        pendingBot.put(name, new BotPendingData(player, kitType, allArenas, pendingType));
        botManager.addBot(name, player);
    }

    private void addManualBot(Player player, String name) {
        if (PotPvPSI.getInstance().getBotManager().addBot(name, player)) {
            player.sendMessage(ChatColor.GREEN + "Successfully added " + ChatColor.AQUA + name + ChatColor.GREEN + " to active bots.");
        } else {
            player.sendMessage(ChatColor.RED + name + " already exists.");
        }
    }

    private String randomAvailableName() {
        if (NAME.isEmpty()) {
            return null;
        }

        int maxAttempts = NAME.size() * 2;
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            String name = NAME.get(ThreadLocalRandom.current().nextInt(NAME.size()));
            if (!isNameReserved(name)) {
                return name;
            }
        }

        for (String name : NAME) {
            if (!isNameReserved(name)) {
                return name;
            }
        }

        return null;
    }

    private boolean isNameReserved(String name) {
        return loadingBotNames.contains(nameKey(name)) || isNameActive(name);
    }

    private boolean isNameActive(String name) {
        BotManager botManager = PotPvPSI.getInstance().getBotManager();
        return Bukkit.getPlayer(name) != null || pendingBot.containsKey(name) || botManager.getList().contains(name);
    }

    private String nameKey(String name) {
        return name == null ? "" : name.toLowerCase(Locale.ROOT);
    }
}
