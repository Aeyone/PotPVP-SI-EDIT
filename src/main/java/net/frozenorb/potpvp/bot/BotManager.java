package net.frozenorb.potpvp.bot;

import net.frozenorb.potpvp.PotPvPSI;
import net.frozenorb.potpvp.bot.config.BotProfile;
import net.frozenorb.qlib.util.UUIDUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BotManager {

    private final Set<String> bots = ConcurrentHashMap.newKeySet();
    private final Map<String, Map<String, Object>> botSettings = new ConcurrentHashMap<>();

    static class RedisPacket {
        String type;
        Map<String, Object> data;
        public RedisPacket(String type, Map<String, Object> map) {
            this.type = type;
            this.data = map;
        }
    }

    public BotManager() {

        Bukkit.getScheduler().runTaskTimer(PotPvPSI.getInstance(), () -> {
            for (String bot : bots) {
                if (Bukkit.getPlayer(bot) == null) {
                    delBot(bot);
                }
            }
        }, 60 * 20L, 60 * 20L);
    }

    public Set<String> getList() {
        return bots;
    }

    public Map<String, Object> getSettings(String name) {
        String storedName = getStoredBotName(name);
        return storedName == null ? Collections.emptyMap() : botSettings.getOrDefault(storedName, Collections.emptyMap());
    }

    public boolean isBot(String name) {
        return getStoredBotName(name) != null;
    }

    public Boolean addBot(String name, Player sender) {
        BotProfile profile = PotPvPSI.getInstance().getBotConfig().getBot(name);
        if (profile == null) {
            return false;
        }

        name = profile.getName();
        if (isBot(name)) {
            delBot(name);
        }
        bots.add(name);
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DataOutputStream data = new DataOutputStream(out);

            data.writeUTF("BotBypass");
            data.writeUTF(name);
            data.writeUTF("Practice");

            sender.sendPluginMessage(PotPvPSI.getInstance(), "BungeeCord", out.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }

        UUID uuid = UUIDUtils.uuid(name);
        Map<String, Object> config = new ConcurrentHashMap<>();
        Map<String, Object> settings = PotPvPSI.getInstance().getBotConfig().createRandomSettings(name);
        config.put("username", name);
        config.put("uuid", (uuid == null ? UUID.randomUUID() : uuid).toString());
        config.putAll(settings);
        botSettings.put(name, new LinkedHashMap<>(settings));

        send(new RedisPacket("add", config));
        scheduleJoinTimeout(name);
        return true;
    }

    public boolean delBot(String name) {
        String storedName = getStoredBotName(name);
        BotPendingManager pendingManager = PotPvPSI.getInstance().getBotPendingManager();
        boolean clearedReservation = false;
        if (pendingManager != null) {
            clearedReservation = pendingManager.clearReservation(name);
        }

        if (storedName == null) {
            return clearedReservation;
        }
        bots.remove(storedName);
        botSettings.remove(storedName);
        if (pendingManager != null) {
            pendingManager.clearReservation(storedName);
        }

        if (Bukkit.getPlayer(storedName) != null) {
            Bukkit.getPlayer(storedName).kickPlayer("delete fakeplayer.");
        }

        UUID uuid = UUIDUtils.uuid(storedName);
        Map<String, Object> config = new ConcurrentHashMap<>();

        config.put("username", storedName);
        config.put("uuid", (uuid == null ? UUID.randomUUID() : uuid).toString());

        send(new RedisPacket("del", config));
        return true;
    }

    public void applyFriendlyUuids(Player bot, Collection<UUID> friendlyUuids) {
        if (bot == null || !bots.contains(bot.getName())) {
            return;
        }

        List<String> friendlyUuidStrings = new ArrayList<>();
        for (UUID friendlyUuid : friendlyUuids) {
            if (friendlyUuid != null && !friendlyUuid.equals(bot.getUniqueId())) {
                friendlyUuidStrings.add(friendlyUuid.toString());
            }
        }

        Map<String, Object> config = new ConcurrentHashMap<>();
        config.put("username", bot.getName());
        config.put("uuid", bot.getUniqueId().toString());
        config.put("friendlyUUIDs", friendlyUuidStrings);

        send(new RedisPacket("apply", config));
    }

    private void send(RedisPacket packet) {
        PotPvPSI.getInstance().getRedisManager().send(packet);
    }

    private void scheduleJoinTimeout(String name) {
        Bukkit.getScheduler().runTaskLater(PotPvPSI.getInstance(), () -> {
            if (isBot(name) && Bukkit.getPlayer(name) == null) {
                delBot(name);
            }
        }, 20L * 5);
    }

    private String getStoredBotName(String name) {
        if (name == null) {
            return null;
        }

        if (bots.contains(name)) {
            return name;
        }

        for (String bot : bots) {
            if (bot.equalsIgnoreCase(name)) {
                return bot;
            }
        }

        return null;
    }

}
