package net.frozenorb.potpvp.bot;

import net.frozenorb.potpvp.bot.BotManager.RedisPacket;
import net.minecraft.util.com.google.gson.Gson;
import org.bukkit.Bukkit;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class RedisManager {

    private final JedisPool jedisPool;
    private final BlockingQueue<String> queue = new LinkedBlockingQueue<>(10000);
    private final Gson gson = new Gson();

    private volatile boolean running = true;

    private final String channel;

    public RedisManager(JedisPool jedisPool, String channel) {
        this.jedisPool = jedisPool;
        this.channel = channel;
    }

    public void send(RedisPacket packet) {
        String msg = gson.toJson(packet);

        boolean success = queue.offer(msg);

        if (!success) {
            Bukkit.getLogger().info("[RedisManager] Queue FULL, dropping packet");
        }
    }

    public void start() {
        Thread worker = new Thread(this::runWorker, "Redis-Worker");
        worker.setDaemon(true);
        worker.start();
    }

    public void shutdown() {
        running = false;
    }

    private void runWorker() {
        while (running) {
            try (Jedis jedis = jedisPool.getResource()) {

                while (running) {
                    String msg = queue.poll(1, TimeUnit.SECONDS);
                    if (msg == null) continue;

                    try {
                        jedis.publish(channel, msg);
                    } catch (Exception e) {
                        Bukkit.getLogger().info("[RedisManager] Publish failed, requeue");
                        queue.offer(msg);
                        break;
                    }
                }

            } catch (Exception e) {
                Bukkit.getLogger().info("[RedisManager] Redis connection failed, retrying...");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }
            }
        }
    }
}
