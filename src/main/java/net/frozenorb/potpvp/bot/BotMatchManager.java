package net.frozenorb.potpvp.bot;

import lombok.Getter;
import net.frozenorb.potpvp.PotPvPSI;
import net.frozenorb.potpvp.kittype.KitType;
import net.frozenorb.potpvp.util.Skin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
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
            "Hydrize"
        )
    );
    public BotMatchManager() {
        for (String name : NAME) {
            Skin.getSkinByName(name);
        }
    }

    @Getter private final Map<String, BotPendingData> pendingBot = new ConcurrentHashMap<>();

    public void prepareDuel(Player player, KitType kitType, Set<String> allArenas) {
        String name = NAME.get(ThreadLocalRandom.current().nextInt(NAME.size()));
        while (Bukkit.getPlayer(name) != null) {
            name = NAME.get(ThreadLocalRandom.current().nextInt(NAME.size()));
        }
//        String name = PotPvPSI.getInstance().getFakeChatManager().getConfig().generateRandomNickname();
        BotManager botManager =  PotPvPSI.getInstance().getBotManager();
        pendingBot.put(name, new BotPendingData(player, kitType, allArenas, "DUEL"));
        botManager.addBot(name, player);
    }

    public void prepareQueue(Player player, KitType kitType) {
        String name = NAME.get(ThreadLocalRandom.current().nextInt(NAME.size()));
        while (Bukkit.getPlayer(name) != null) {
            name = NAME.get(ThreadLocalRandom.current().nextInt(NAME.size()));
        }
//        String name = PotPvPSI.getInstance().getFakeChatManager().getConfig().generateRandomNickname();
        BotManager botManager =  PotPvPSI.getInstance().getBotManager();
        pendingBot.put(name, new BotPendingData(player, kitType, null, "QUEUE"));
        botManager.addBot(name, player);
    }
}
