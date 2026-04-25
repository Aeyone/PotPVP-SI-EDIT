package net.frozenorb.potpvp.lobby.menu.matchhistory;

import com.google.common.collect.Maps;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import net.frozenorb.potpvp.PotPvPSI;
import net.frozenorb.potpvp.match.Match;
import net.frozenorb.potpvp.match.MatchHandler;
import net.frozenorb.potpvp.match.event.MatchTerminateEvent;
import net.frozenorb.potpvp.util.MongoUtils;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class MatchHistoryHandler implements Listener {
    private static MongoCollection<Document> COLLECTION = MongoUtils.getCollection(MatchHandler.MONGO_COLLECTION_NAME);;
    private Map<UUID, List<String>> playerMatchList = Maps.newConcurrentMap();

    public MatchHistoryHandler() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(PotPvPSI.getInstance(), ()->{
             long start = System.currentTimeMillis();
             int size = playerMatchList.size();
             for (UUID uuid : playerMatchList.keySet()) {
                 if (Bukkit.getPlayer(uuid) == null) {
                     unloadMatchList(uuid);
                 }
             }
             if (size - playerMatchList.size() > 0) {
                 Bukkit.getLogger().info("[MongoDB] Removed " + (size - playerMatchList.size()) + " match record cache in " + (System.currentTimeMillis() - start) + "ms.");
             }
        },60 * 20, 60 * 20);
    }

    public void loadMatchList(UUID uuid) {
        FindIterable<Document> result = COLLECTION.find(Filters.eq("allPlayers", uuid.toString())); // ascending order
        List<String> idList = new ArrayList<>();
        for (Document doc : result) {
            idList.add(doc.getString("_id"));
        }
        playerMatchList.put(uuid, idList);
    }

    public void unloadMatchList(UUID uuid) {
        playerMatchList.remove(uuid);
    }

    public void addMatch(UUID uuid, String id) {
        playerMatchList.get(uuid).add(id);
    }

    public List<String> getMatchList(UUID uuid) {
        return playerMatchList.get(uuid);
    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(PotPvPSI.getInstance(), ()->{
            loadMatchList(event.getPlayer().getUniqueId());
        });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(PotPvPSI.getInstance(), () -> {
            unloadMatchList(event.getPlayer().getUniqueId());
        });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onMatchEnd(MatchTerminateEvent event) {// if cancel match end delay, maybe create null
        Match match = event.getMatch();

        if (match == null) {
            return;
        }

        String id = match.get_id();
        Set<UUID> historyPlayers = new HashSet<>();

        if (match.getWinningPlayers() != null) {
            historyPlayers.addAll(match.getWinningPlayers());
        }

        if (match.getLosingPlayers() != null) {
            historyPlayers.addAll(match.getLosingPlayers());
        }

        if (historyPlayers.isEmpty() && match.getAllPlayers() != null) {
            historyPlayers.addAll(match.getAllPlayers());
        }

        for (UUID uuid : historyPlayers) {
            if (uuid != null && getMatchList(uuid) != null) {
                addMatch(uuid, id);
            }
        }
    }
}
