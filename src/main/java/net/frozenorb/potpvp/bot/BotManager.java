package net.frozenorb.potpvp.bot;

import net.frozenorb.potpvp.PotPvPSI;
import net.frozenorb.qlib.util.UUIDUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class BotManager {
    private static final Set<String> DOUBLE_TYPE = Collections.unmodifiableSet(
        new HashSet<>(Arrays.asList(
            "horizontalAimSpeed",
            "verticalAimSpeed",
            "horizontalAimAccuracy",
            "verticalAimAccuracy",
            "horizontalErraticness",
            "verticalErraticness",
            "averageCps",
            "sprintResetAccuracy",
            "hitSelectAccuracy",
            "reach",
            "jumpProbability",
            "wtapProbability"
        ))
    );
    private static final Set<String> INT_TYPE = Collections.unmodifiableSet(
        new HashSet<>(Arrays.asList(
            "latency",
            "targetSearchRange",
            "pearlCooldown"
        ))
    );

    private static final int[] PING_LEVELS = {5, 10, 15, 20, 25, 30, 50, 80, 120, 150};

    private final Set<String> bots = ConcurrentHashMap.newKeySet();
    private final Map<String, Object> upper = new ConcurrentHashMap<>();
    private final Map<String, Object> lower = new ConcurrentHashMap<>();

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

    public Boolean addBot(String name, Player sender) {
        if (bots.contains(name)) {
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

        ThreadLocalRandom random = ThreadLocalRandom.current();
        double difficulty = random.nextInt(0, 4);
        Bukkit.getLogger().info("difficulty: " + difficulty);
        UUID uuid = UUIDUtils.uuid(name);
        Map<String, Object> config = new ConcurrentHashMap<>();
        config.put("username", name);
        config.put("uuid", (uuid == null ? UUID.randomUUID() : uuid).toString());
        config.put("horizontalAimSpeed", random.nextDouble(0.4, 0.95) + difficulty * 2 / 10);
        config.put("verticalAimSpeed", random.nextDouble(0.4, 0.95) + difficulty * 2 / 10);
        config.put("horizontalAimAccuracy", random.nextDouble(0.3, 0.8) + difficulty * 2 / 10);
        config.put("verticalAimAccuracy", random.nextDouble(0.3, 0.8) + difficulty * 2 / 10);
        config.put("horizontalErraticness", Math.max(0.2, random.nextDouble(0.2, 0.8) - difficulty * 2 / 10));
        config.put("verticalErraticness", Math.max(0.2, random.nextDouble(0.2, 0.8) - difficulty * 2 / 10));
        config.put("averageCps", random.nextDouble(6.0, 12.0) + difficulty * 2);
        config.put("sprintResetAccuracy", random.nextDouble(0.4, 0.95) + difficulty * 2 / 10);
        config.put("hitSelectAccuracy", 0.6f);
        config.put("reach", Math.min(3.0, random.nextDouble(2.7, 3.0) + difficulty * 2 / 10));
        config.put("jumpProbability", random.nextDouble(0.06, 0.18));
        config.put("wtapProbability", random.nextDouble(0.4, 0.9) + difficulty * 2 / 10);
        config.put("latency", PING_LEVELS[random.nextInt(0, 10)]);
        config.put("targetSearchRange", 256);
        config.put("pearlCooldown", random.nextInt(20, 32));

        send(new RedisPacket("add", config));
        return true;
    }

    public boolean delBot(String name) {
        if (!bots.contains(name)) {
            return false;
        }
        bots.remove(name);
        if (Bukkit.getPlayer(name) != null) {
            Bukkit.getPlayer(name).kickPlayer("delete fakeplayer.");
        }

        UUID uuid = UUIDUtils.uuid(name);
        Map<String, Object> config = new ConcurrentHashMap<String, Object>() {{
                put("username", name);
                put("uuid", (uuid == null ? UUID.randomUUID() : uuid).toString());
            }
        };
        send(new RedisPacket("del", config));
        return true;
    }

    private void send(RedisPacket packet) {
        PotPvPSI.getInstance().getRedisManager().send(packet);
    }

}