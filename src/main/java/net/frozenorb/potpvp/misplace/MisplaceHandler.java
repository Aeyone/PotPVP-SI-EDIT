package net.frozenorb.potpvp.misplace;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;

import net.frozenorb.potpvp.PotPvPSI;
import net.frozenorb.potpvp.kittype.KitType;
import net.frozenorb.potpvp.match.Match;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public final class MisplaceHandler implements Listener {

    private static final List<PacketType> MOVE_PACKETS = Arrays.asList(
        PacketType.Play.Server.REL_ENTITY_MOVE,
        PacketType.Play.Server.ENTITY_MOVE_LOOK,
        PacketType.Play.Server.ENTITY_TELEPORT,
        PacketType.Play.Server.ENTITY_LOOK
    );

    private final PotPvPSI plugin;
    private final Map<UUID, Long> lastAttackTime = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastDamageTime = new ConcurrentHashMap<>();
    private final Map<UUID, Double> currentDelayMap = new ConcurrentHashMap<>();
    private final ScheduledExecutorService packetScheduler;
    private final PacketAdapter packetListener;
    private final BukkitTask cleanupTask;

    public MisplaceHandler(PotPvPSI plugin) {
        this.plugin = plugin;
        this.packetScheduler = Executors.newScheduledThreadPool(4, new ThreadFactory() {
            private final AtomicInteger threadNumber = new AtomicInteger();

            @Override
            public Thread newThread(Runnable runnable) {
                Thread thread = new Thread(runnable, "PotPvP-Misplace-" + threadNumber.incrementAndGet());
                thread.setDaemon(true);
                return thread;
            }
        });

        this.packetListener = new PacketAdapter(plugin, ListenerPriority.HIGHEST, MOVE_PACKETS) {
            @Override
            public void onPacketSending(PacketEvent event) {
                handleMovePacket(event);
            }
        };
        ProtocolLibrary.getProtocolManager().addPacketListener(packetListener);

        this.cleanupTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            lastAttackTime.keySet().removeIf(uuid -> Bukkit.getPlayer(uuid) == null);
            lastDamageTime.keySet().removeIf(uuid -> Bukkit.getPlayer(uuid) == null);
            currentDelayMap.keySet().removeIf(uuid -> Bukkit.getPlayer(uuid) == null);
        }, 1200L, 1200L);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCombat(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player attacker = (Player) event.getDamager();
            if (plugin.getMatchHandler().getMatchPlaying(attacker) != null) {
                lastAttackTime.put(attacker.getUniqueId(), System.currentTimeMillis());
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player damaged = (Player) event.getEntity();
            if (plugin.getMatchHandler().getMatchPlaying(damaged) != null) {
                lastDamageTime.put(damaged.getUniqueId(), System.currentTimeMillis());
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        lastAttackTime.remove(uuid);
        lastDamageTime.remove(uuid);
        currentDelayMap.remove(uuid);
    }

    public void shutdown() {
        ProtocolLibrary.getProtocolManager().removePacketListener(packetListener);
        cleanupTask.cancel();
        packetScheduler.shutdownNow();
        lastAttackTime.clear();
        lastDamageTime.clear();
        currentDelayMap.clear();
    }

    private void handleMovePacket(PacketEvent event) {
        final Player receiver = event.getPlayer();
        int entityId = event.getPacket().getIntegers().read(0);
        Entity movingEntity = findEntity(receiver, entityId);

        if (!(movingEntity instanceof Player) || movingEntity.getEntityId() == receiver.getEntityId()) {
            return;
        }

        Player attacker = (Player) movingEntity;
        UUID attackerUuid = attacker.getUniqueId();
        Match match = plugin.getMatchHandler().getMatchPlaying(attacker);

        if (match == null) {
            currentDelayMap.remove(attackerUuid);
            return;
        }

        KitType kitType = match.getKitType();
        double maxDelay = Math.max(0.0D, kitType.getMisplaceDelay());
        double step = kitType.getMisplaceStep();

        if (maxDelay <= 0.0D || step <= 0.0D
            || Double.isNaN(maxDelay) || Double.isNaN(step)
            || Double.isInfinite(maxDelay) || Double.isInfinite(step)) {
            currentDelayMap.remove(attackerUuid);
            return;
        }

        double targetDelay = isInComboState(attackerUuid, kitType) ? maxDelay : 0.0D;
        double currentDelay = currentDelayMap.getOrDefault(attackerUuid, 0.0D);

        if (currentDelay < targetDelay) {
            currentDelay = Math.min(currentDelay + step, targetDelay);
        } else if (currentDelay > targetDelay) {
            currentDelay = Math.max(currentDelay - step, targetDelay);
        }

        if (currentDelay <= 0.0D) {
            currentDelayMap.remove(attackerUuid);
            return;
        }

        currentDelayMap.put(attackerUuid, currentDelay);

        if (currentDelay <= 0.05D || event.isCancelled()) {
            return;
        }

        event.setCancelled(true);
        final PacketContainer packet = event.getPacket().deepClone();
        long delayMs = (long) (currentDelay * 50.0D);

        packetScheduler.schedule(() -> {
            if (!receiver.isOnline()) {
                return;
            }

            try {
                ProtocolLibrary.getProtocolManager().sendServerPacket(receiver, packet, false);
            } catch (InvocationTargetException e) {
                plugin.getLogger().log(Level.WARNING, "Error sending misplaced packet", e);
            }
        }, delayMs, TimeUnit.MILLISECONDS);
    }

    private Entity findEntity(Player receiver, int entityId) {
        try {
            return ProtocolLibrary.getProtocolManager().getEntityFromID(receiver.getWorld(), entityId);
        } catch (Exception ignored) {
            for (Entity entity : receiver.getWorld().getEntities()) {
                if (entity.getEntityId() == entityId) {
                    return entity;
                }
            }
            return null;
        }
    }

    private boolean isInComboState(UUID playerUuid, KitType kitType) {
        long now = System.currentTimeMillis();
        Long lastAttack = lastAttackTime.get(playerUuid);

        if (lastAttack == null || now - lastAttack > kitType.getMisplaceAttackWindowMs()) {
            return false;
        }

        Long lastDamage = lastDamageTime.get(playerUuid);
        return lastDamage == null || now - lastDamage > kitType.getMisplaceDamageWindowMs();
    }
}
