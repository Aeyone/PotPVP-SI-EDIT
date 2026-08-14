package net.frozenorb.potpvp.lobby.menu.statistics;

import net.frozenorb.potpvp.PotPvPSI;
import net.frozenorb.qlib.command.Command;
import net.frozenorb.qlib.command.Param;
import net.frozenorb.qlib.uuid.FrozenUUIDCache;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class StatisticsCommand {

    private static final long LOOKUP_COOLDOWN_MILLIS = 1_000L;
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9_]{1,16}$");
    private static final Map<UUID, Long> LAST_LOOKUP = new ConcurrentHashMap<>();

    @Command(names = {"statistics", "stats"}, permission = "")
    public static void StatisticsCommand(Player sender, @Param(name = "target", defaultValue = "self") String targetName) {
        long now = System.currentTimeMillis();
        Long lastLookup = LAST_LOOKUP.put(sender.getUniqueId(), now);
        if (lastLookup != null && now - lastLookup < LOOKUP_COOLDOWN_MILLIS) {
            sender.sendMessage(ChatColor.RED + "Please wait before looking up another player.");
            return;
        }

        if (targetName.equalsIgnoreCase("self")) {
            openOnlineStatistics(sender, sender);
            return;
        }

        if (!USERNAME_PATTERN.matcher(targetName).matches()) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return;
        }

        Player onlineTarget = Bukkit.getPlayerExact(targetName);
        if (onlineTarget != null) {
            openOnlineStatistics(sender, onlineTarget);
            return;
        }

        resolveOfflineAndOpen(sender, targetName);
    }

    private static void openOnlineStatistics(Player sender, Player target) {
        StatisticsHandler statisticsHandler = PotPvPSI.getInstance().getStatisticsHandler();
        if (statisticsHandler.checkNull(target.getUniqueId())) {
            loadAndOpen(sender, target.getUniqueId(), target.getName(), true);
        } else {
            StatisticsMenu.forLoadedStatistics(target.getUniqueId(), target.getName()).openMenu(sender);
        }
    }

    private static void resolveOfflineAndOpen(Player sender, String requestedName) {
        UUID senderUuid = sender.getUniqueId();

        Bukkit.getScheduler().runTaskAsynchronously(PotPvPSI.getInstance(), () -> {
            UUID targetUuid;
            String targetName;
            boolean loaded;

            try {
                targetUuid = FrozenUUIDCache.uuid(requestedName);
                if (targetUuid == null) {
                    openLookupResult(senderUuid, null, requestedName, false, true);
                    return;
                }

                String cachedName = FrozenUUIDCache.name(targetUuid);
                targetName = cachedName == null ? requestedName : cachedName;
                StatisticsHandler statisticsHandler = PotPvPSI.getInstance().getStatisticsHandler();
                loaded = !statisticsHandler.checkNull(targetUuid) || statisticsHandler.loadStatisticsIfPresent(targetUuid);
            } catch (Exception exception) {
                PotPvPSI.getInstance().getLogger().log(Level.WARNING, "Failed to resolve statistics for " + requestedName, exception);
                openLookupResult(senderUuid, null, requestedName, false, false);
                return;
            }

            openLookupResult(senderUuid, targetUuid, targetName, loaded, false);
        });
    }

    private static void loadAndOpen(Player sender, UUID targetUuid, String targetName, boolean createIfMissing) {
        UUID senderUuid = sender.getUniqueId();

        Bukkit.getScheduler().runTaskAsynchronously(PotPvPSI.getInstance(), () -> {
            StatisticsHandler statisticsHandler = PotPvPSI.getInstance().getStatisticsHandler();
            boolean loaded;

            try {
                if (createIfMissing) {
                    statisticsHandler.loadStatistics(targetUuid, targetName);
                    loaded = true;
                } else {
                    loaded = statisticsHandler.loadStatisticsIfPresent(targetUuid);
                }
            } catch (Exception exception) {
                PotPvPSI.getInstance().getLogger().log(Level.WARNING, "Failed to load statistics for " + targetUuid, exception);
                loaded = false;
            }

            openLookupResult(senderUuid, targetUuid, targetName, loaded, false);
        });
    }

    private static void openLookupResult(UUID senderUuid, UUID targetUuid, String targetName, boolean loaded, boolean playerNotFound) {
        Bukkit.getScheduler().runTask(PotPvPSI.getInstance(), () -> {
            Player currentSender = Bukkit.getPlayer(senderUuid);
            if (currentSender == null) {
                return;
            }

            if (playerNotFound) {
                currentSender.sendMessage(ChatColor.RED + "Player not found.");
                return;
            }

            if (!loaded || targetUuid == null) {
                currentSender.sendMessage(ChatColor.RED + "No statistics were found for that player.");
                return;
            }

            StatisticsMenu.forLoadedStatistics(targetUuid, targetName).openMenu(currentSender);
        });
    }
}
