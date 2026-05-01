package net.frozenorb.potpvp.bot.listener;

import net.frozenorb.potpvp.PotPvPSI;
import net.frozenorb.potpvp.bot.BotPendingManager;
import net.frozenorb.potpvp.bot.BotPendingData;
import net.frozenorb.potpvp.kittype.KitType;
import net.frozenorb.potpvp.queue.MatchQueue;
import net.frozenorb.potpvp.queue.MatchQueueEntry;
import net.frozenorb.potpvp.queue.QueueHandler;
import net.frozenorb.potpvp.util.Skin;
import net.frozenorb.potpvp.util.SkinUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.Set;

import static net.frozenorb.potpvp.duel.command.AcceptCommand.accept;
import static net.frozenorb.potpvp.duel.command.DuelCommand.duel;
import static net.frozenorb.potpvp.duel.command.DuelCommand.getRandomArenaSchematic;

public class BotListener implements Listener {

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        if (!PotPvPSI.getInstance().getBotManager().getList().contains(player.getName())) {
            return;
        }

        Skin cached = Skin.getCachedSkin(player.getName());
        if (cached != null) {
            SkinUtils.setSkin(player, cached);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        BotPendingManager botPendingManager = PotPvPSI.getInstance().getBotPendingManager();
        BotPendingData botPendingData = botPendingManager.getPendingBot().get(player.getName());
        if (botPendingData == null) {
            return;
        }

        Player target = botPendingData.target;
        KitType kitType = botPendingData.kitType;
        Set<String> allArenas = botPendingData.allArenas;

        switch (botPendingData.pendingType) {
            case DUEL: {
                if (allArenas == null || allArenas.isEmpty()) {
                    duel(target, player, kitType);
                } else {
                    duel(target, player, kitType, getRandomArenaSchematic(allArenas), allArenas.size() == 1 ? "EXACT" : "RANDOM");
                }
                accept(player, target);
                break;
            }
            case QUEUE: {
                QueueHandler queueHandler = PotPvPSI.getInstance().getQueueHandler();
                MatchQueueEntry targetEntry = queueHandler.getQueueEntry(target.getUniqueId());
                if (targetEntry != null) {
                    MatchQueue queue = targetEntry.getQueue();
                    queueHandler.joinQueue(player, queue.getKitType(), false, target.getUniqueId());
                }
                break;
            }
            default:{}
        }
        botPendingManager.getPendingBot().remove(player.getName());

        Bukkit.getScheduler().runTaskLater( PotPvPSI.getInstance(), ()-> {
            if (!PotPvPSI.getInstance().getMatchHandler().isPlayingMatch(player)) {
                PotPvPSI.getInstance().getBotManager().delBot(player.getName());
            }
        }, 20L * 5);
    }
}
