package net.frozenorb.potpvp.lobby.menu.matchhistory.button;

import net.frozenorb.potpvp.kittype.KitType;
import net.frozenorb.potpvp.postmatchinv.PostMatchPlayer;
import net.frozenorb.potpvp.postmatchinv.menu.PostMatchMenu;
import net.frozenorb.qlib.menu.Button;
import com.google.common.collect.Lists;
import net.frozenorb.qlib.qLib;
import net.frozenorb.qlib.util.UUIDUtils;
import org.bson.Document;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.text.SimpleDateFormat;
import java.util.*;

public class MatchHistoryButton extends Button {

    private Document doc;
    private UUID target;
    private String id;
    private KitType kitType;
    private boolean ranked;
    private String arena;
    private Date startedAt;
    private Date endedAt;
    private List<String> winningPlayers = new ArrayList<>();
    private List<String> losingPlayers = new ArrayList<>();
    private Map<UUID, PostMatchPlayer> postMatchPlayers = new HashMap<>();
    private List<PostMatchPlayer> postMatchPlayerlist = new ArrayList<>();

    public MatchHistoryButton(Document doc, UUID target) {
        this.doc = doc;
        this.target = target;
        this.id = doc.getString("_id");
        this.kitType = KitType.byId(doc.getString("kitType"));
        this.ranked = doc.getBoolean("ranked");
        this.arena = doc.getString("arena");
        this.startedAt = doc.getDate("startedAt");
        this.endedAt = doc.getDate("endedAt");
        doc.getList("winningPlayers", String.class).forEach((uuidString)->{ this.winningPlayers.add(UUIDUtils.name(UUID.fromString(uuidString))); });
        doc.getList("losingPlayers", String.class).forEach((uuidString)->{ this.losingPlayers.add(UUIDUtils.name(UUID.fromString(uuidString))); });

        Document postMatchPlayersDoc = doc.get("postMatchPlayers", Document.class);
        for (String uuidStr : postMatchPlayersDoc.keySet()) {
            Document playerDoc = postMatchPlayersDoc.get(uuidStr, Document.class);
            this.postMatchPlayerlist.add(qLib.PLAIN_GSON.fromJson(playerDoc.toJson(), PostMatchPlayer.class));
            if (uuidStr.equals(target.toString())) {
                Collections.swap(this.postMatchPlayerlist, 0, this.postMatchPlayerlist.size() - 1);
            }
        }
    }

    @Override
    public String getName(Player player) {
        if (kitType == null) {
            return ChatColor.RED.toString() + ChatColor.BOLD + "Unknown KitType";
        }
        return kitType.getDisplayColor().toString() + ChatColor.BOLD + kitType.getDisplayName() +
                (ranked ? (ChatColor.AQUA.toString() + ChatColor.BOLD + " (Ranked)") : (ChatColor.GRAY.toString() + ChatColor.BOLD + " (Unranked)"));
    }

    @Override
    public List<String> getDescription(Player player) {
        List<String> description = Lists.newArrayList();
        description.add(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "--------------------------------");
        description.add(ChatColor.AQUA + "Arena: " + ChatColor.WHITE + arena + ChatColor.GRAY + " (#" + (id == null ? "unknown" : id) + ")");
        description.add("");
        description.add(ChatColor.GREEN + "Winner: " + ChatColor.YELLOW + String.join(", ", winningPlayers));
        description.add(ChatColor.RED + "Loser: " + ChatColor.YELLOW + String.join(", ", losingPlayers));
        description.add("");
        description.add(ChatColor.LIGHT_PURPLE + "Started at: " + ChatColor.WHITE + new SimpleDateFormat("MMM dd yyyy EEE hh:mm:ss a", Locale.ENGLISH).format(startedAt));
        description.add(ChatColor.LIGHT_PURPLE + "Ended at: " + ChatColor.WHITE + new SimpleDateFormat("MMM dd yyyy EEE hh:mm:ss a", Locale.ENGLISH).format(endedAt));
        description.add("");
        description.add(ChatColor.YELLOW + "Click here to view Inventories.");
        description.add(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "--------------------------------");

        return description;
    }

    @Override
    public Material getMaterial(Player player) {
        return kitType == null ? Material.REDSTONE_BLOCK : kitType.getIcon().getItemType();
    }

    @Override
    public byte getDamageValue(Player player) {
        return kitType == null ? 0 : kitType.getIcon().getData();
    }

    @Override
    public void clicked(Player player, int slot, ClickType clickType) {
        Button.playNeutral(player);
        new PostMatchMenu(postMatchPlayerlist.get(0), postMatchPlayerlist).openMenu(player);
    }

}
