package net.frozenorb.potpvp.postmatchinv.command;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import net.frozenorb.potpvp.PotPvPSI;
import net.frozenorb.potpvp.match.MatchHandler;
import net.frozenorb.potpvp.postmatchinv.PostMatchInvHandler;
import net.frozenorb.potpvp.postmatchinv.PostMatchPlayer;
import net.frozenorb.potpvp.postmatchinv.menu.PostMatchMenu;
import net.frozenorb.potpvp.util.MongoUtils;
import net.frozenorb.qlib.command.Command;
import net.frozenorb.qlib.command.Param;
import net.frozenorb.qlib.qLib;
import net.frozenorb.qlib.util.UUIDUtils;

import org.bson.Document;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;

public final class CheckPostMatchInvCommand {

    @Command(names = { "checkPostMatchInv", "_" }, permission = "")
    public static void checkPostMatchInv(Player sender, @Param(name = "id") String id, @Param(name = "target") UUID target) {
        List<PostMatchPlayer> postMatchPlayerlist = new ArrayList<>();
        PostMatchInvHandler postMatchInvHandler = PotPvPSI.getInstance().getPostMatchInvHandler();
        Map<UUID, PostMatchPlayer> players = postMatchInvHandler.getPostMatchData(sender.getUniqueId());

        for (PostMatchPlayer postMatchPlayer : players.values()) {
            postMatchPlayerlist.add(postMatchPlayer);
            if (postMatchPlayer.getPlayerUuid().toString().equals(target.toString())) {
                Collections.swap(postMatchPlayerlist, 0, postMatchPlayerlist.size() - 1);
            }
        }

        if (postMatchPlayerlist.size() > 0) {
            new PostMatchMenu(postMatchPlayerlist.get(0), postMatchPlayerlist, id).openMenu(sender);
        } else {
            new PostMatchMenu(id, target).openMenu(sender);
        }
    }

}