package net.frozenorb.potpvp.lobby.menu.matchhistory.button;

import net.frozenorb.potpvp.kittype.KitType;
import net.frozenorb.potpvp.postmatchinv.menu.PostMatchMenu;
import net.frozenorb.qlib.menu.Button;
import com.google.common.collect.Lists;
import net.frozenorb.qlib.util.UUIDUtils;
import org.bson.Document;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.text.SimpleDateFormat;
import java.util.*;

public class MatchHistoryButton extends Button {

    private UUID target;
    private String id;
    private KitType kitType;
    private boolean ranked;
    private String arena;
    private Date startedAt;
    private Date endedAt;
    private List<String> winningPlayerUuids = new ArrayList<>();
    private List<String> losingPlayerUuids = new ArrayList<>();
    private Map<String, String> nameCache;

    public MatchHistoryButton(Document doc, UUID target, Map<String, String> nameCache) {
        this.target = target;
        this.nameCache = nameCache;
        this.id = doc.getString("_id");
        this.kitType = KitType.byId(doc.getString("kitType"));
        this.ranked = Boolean.TRUE.equals(doc.getBoolean("ranked"));
        this.arena = doc.getString("arena");
        this.startedAt = doc.getDate("startedAt");
        this.endedAt = doc.getDate("endedAt");
        addPlayerUuids(doc.getList("winningPlayers", String.class), this.winningPlayerUuids);
        addPlayerUuids(doc.getList("losingPlayers", String.class), this.losingPlayerUuids);
    }

    private void addPlayerUuids(List<String> playerUuids, List<String> target) {
        if (playerUuids == null) {
            return;
        }

        playerUuids.stream()
            .filter(Objects::nonNull)
            .forEach(target::add);
    }

    private String formatPlayers(List<String> playerUuids, String fallback) {
        if (playerUuids.isEmpty()) {
            return fallback;
        }

        List<String> playerNames = new ArrayList<>();
        for (String uuidString : playerUuids) {
            playerNames.add(nameCache.computeIfAbsent(uuidString, key -> UUIDUtils.name(UUID.fromString(key))));
        }

        return String.join(", ", playerNames);
    }

    private String formatDate(Date date) {
        return date == null ? "Unknown" : new SimpleDateFormat("MMM dd yyyy EEE hh:mm:ss a", Locale.ENGLISH).format(date);
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
        description.add(ChatColor.AQUA + "Arena: " + ChatColor.WHITE + (arena == null ? "unknown" : arena) + ChatColor.GRAY + " (#" + (id == null ? "unknown" : id) + ")");
        description.add("");
        description.add(ChatColor.GREEN + "Winner: " + ChatColor.YELLOW + formatPlayers(winningPlayerUuids, "None"));
        description.add(ChatColor.RED + "Loser: " + ChatColor.YELLOW + formatPlayers(losingPlayerUuids, "None"));
        description.add("");
        description.add(ChatColor.LIGHT_PURPLE + "Started at: " + ChatColor.WHITE + formatDate(startedAt));
        description.add(ChatColor.LIGHT_PURPLE + "Ended at: " + ChatColor.WHITE + formatDate(endedAt));
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
        new PostMatchMenu(this.id, this.target).openMenu(player);
    }

}
