package net.frozenorb.potpvp.postmatchinv.menu;

import com.google.common.base.Preconditions;

import com.google.common.collect.ImmutableList;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import net.frozenorb.potpvp.PotPvPLang;
import net.frozenorb.potpvp.PotPvPSI;
import net.frozenorb.potpvp.kittype.HealingMethod;
import net.frozenorb.potpvp.lobby.menu.matchhistory.MatchHistoryMenu;
import net.frozenorb.potpvp.lobby.menu.matchhistory.MatchHistoryMenuButton;
import net.frozenorb.potpvp.lobby.menu.statistics.StatisticsMenu;
import net.frozenorb.potpvp.match.MatchHandler;
import net.frozenorb.potpvp.postmatchinv.PostMatchInvHandler;
import net.frozenorb.potpvp.postmatchinv.PostMatchPlayer;
import net.frozenorb.potpvp.util.InventoryUtils;
import net.frozenorb.potpvp.util.MongoUtils;
import net.frozenorb.qlib.menu.Button;
import net.frozenorb.qlib.menu.Menu;
import net.frozenorb.qlib.qLib;
import net.frozenorb.qlib.util.UUIDUtils;

import org.bson.Document;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public final class PostMatchMenu extends Menu {


    private final PostMatchPlayer target;
    private List<PostMatchPlayer> postMatchPlayerlist = new ArrayList<>();
    private String matchId;
    private UUID uuid;

    public PostMatchMenu(PostMatchPlayer target, List<PostMatchPlayer> postMatchPlayerlist, String matchId) {
        this.target = Preconditions.checkNotNull(target, "target");
        this.postMatchPlayerlist = postMatchPlayerlist;
        this.matchId = matchId;
    }

    public PostMatchMenu(String matchId, UUID uuid) {
        this.matchId = matchId;
        List<PostMatchPlayer> postMatchPlayerlist = new ArrayList<>();

        MongoCollection<Document> collection = MongoUtils.getCollection(MatchHandler.MONGO_COLLECTION_NAME);
        Document matchDoc = collection.find(Filters.eq("_id", matchId)).first();
        if (matchDoc != null) {
            Document postMatchPlayersDoc = matchDoc.get("postMatchPlayers", Document.class);

            for (String uuidStr : postMatchPlayersDoc.keySet()) {
                Document playerDoc = postMatchPlayersDoc.get(uuidStr, Document.class);
                postMatchPlayerlist.add(qLib.PLAIN_GSON.fromJson(playerDoc.toJson(), PostMatchPlayer.class));
                if (uuidStr.equals(uuid.toString())) {
                    Collections.swap(postMatchPlayerlist, 0, postMatchPlayerlist.size() - 1);
                }
            }
        }

        this.target = postMatchPlayerlist.get(0);
        this.postMatchPlayerlist = postMatchPlayerlist;

    }
    @Override
    public String getTitle(Player player) {
        return "Inventory of " + UUIDUtils.name(target.getPlayerUuid());
    }

    @Override
    public void openMenu(Player player) {
        if (this.postMatchPlayerlist.size() == 0) {
            player.sendMessage(ChatColor.RED + "Data for " + UUIDUtils.name(this.uuid) + " in " + this.matchId + " not found.");
            return;
        }
        super.openMenu(player);
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        int x = 0;
        int y = 0;

        List<ItemStack> targetInv = new ArrayList<>(Arrays.asList(target.getInventory()));

        // we want the hotbar (the first 9 items) to be at the bottom (end),
        // not the top (start) of the list, so we rotate them.
        for (int i = 0; i < 9; i++) {
            targetInv.add(targetInv.remove(0));
        }

        for (ItemStack inventoryItem : targetInv) {
            buttons.put(getSlot(x, y), Button.fromItem(inventoryItem));

            if (x++ > 7) {
                x = 0;
                y++;
            }
        }

        x = 3; // start armor backwards, helm first

        for (ItemStack armorItem : target.getArmor()) {
            buttons.put(getSlot(x--, y), Button.fromItem(armorItem));
        }

        y++; // advance line for status buttons

        int position = 0;
        buttons.put(getSlot(position++, y), new PostMatchHealthButton(target.getHealth()));
        buttons.put(getSlot(position++, y), new PostMatchFoodLevelButton(target.getHunger()));
        buttons.put(getSlot(position++, y), new PostMatchPotionEffectsButton(target.getPotionEffects()));

        HealingMethod healingMethod = target.getKitType() == null ? null : target.getKitType().getHealingMethod();

        if (healingMethod != null) {
            int count = healingMethod.count(targetInv.toArray(new ItemStack[targetInv.size()]));
            buttons.put(
                    getSlot(position++, y),
                    new PostMatchHealsLeftButton(
                        target.getPlayerUuid(),
                        healingMethod,
                        count,
                        target.getMissedPots()
                    )
            );
        }

        buttons.put(
                getSlot(position++, y),
                new PostMatchStatisticsButton(
                        target.getKitType(),
                        target.getKitType() == null ? null : target.getKitType().getHealingMethod(),
                        target.getTotalHits(),
                        target.getBlockedHits(),
                        target.getLongestCombo(),
                        target.getThrownHp(),
                        target.getMissedHp(),
                        target.getThrownDebuffs(),
                        target.getMissedDebuffs(),
                        target.getPlayerUuid()
                )
        );
        int index = 0;
        while (postMatchPlayerlist.get(index) != target) {
            index ++;
        }
        PostMatchPlayer otherPlayer = postMatchPlayerlist.get((index + 1) % postMatchPlayerlist.size());

        buttons.put(getSlot(8, y), new PostMatchSwapTargetButton(otherPlayer, postMatchPlayerlist, matchId));
        buttons.put(getSlot(position, y), new MatchHistoryMenuButton(target.getPlayerUuid()) {
            @Override
            public String getName(Player player) {
                return  ChatColor.GREEN + "View " + target.getLastUsername() + "'s Profile";
            }
            @Override
            public List<String> getDescription(Player player) {
                return ImmutableList.of(
                    "",
                    ChatColor.AQUA + "Left-Click" + ChatColor.YELLOW + " To View Match History",
                    ChatColor.AQUA + "Shift-Click" + ChatColor.YELLOW + " To View Total Stats"
                );
            }
            @Override
            public void clicked(Player player, int slot, ClickType clickType) {
                if (clickType.isShiftClick()){
                    Button.playNeutral(player);
                    new StatisticsMenu(target.getPlayerUuid(), matchId).openMenu(player);
                } else if (clickType.isLeftClick()){
                    Button.playNeutral(player);
                    new MatchHistoryMenu(target.getPlayerUuid(), matchId).openMenu(player);
                }
            }
        });

        return buttons;
    }

    @Override
    public void onClose(Player player) {
        InventoryUtils.resetInventoryDelayed(player);
    }

}