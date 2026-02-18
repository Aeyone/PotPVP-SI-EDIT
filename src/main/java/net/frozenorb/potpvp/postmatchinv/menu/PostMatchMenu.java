package net.frozenorb.potpvp.postmatchinv.menu;

import com.google.common.base.Preconditions;

import com.google.common.collect.ImmutableList;
import net.frozenorb.potpvp.PotPvPLang;
import net.frozenorb.potpvp.PotPvPSI;
import net.frozenorb.potpvp.kittype.HealingMethod;
import net.frozenorb.potpvp.lobby.menu.matchhistory.MatchHistoryMenu;
import net.frozenorb.potpvp.lobby.menu.matchhistory.MatchHistoryMenuButton;
import net.frozenorb.potpvp.lobby.menu.statistics.StatisticsMenu;
import net.frozenorb.potpvp.postmatchinv.PostMatchInvHandler;
import net.frozenorb.potpvp.postmatchinv.PostMatchPlayer;
import net.frozenorb.potpvp.util.InventoryUtils;
import net.frozenorb.qlib.menu.Button;
import net.frozenorb.qlib.menu.Menu;
import net.frozenorb.qlib.util.UUIDUtils;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public final class PostMatchMenu extends Menu {


    private final PostMatchPlayer target;
    private List<PostMatchPlayer> postMatchPlayerlist = new ArrayList<>();

    public PostMatchMenu(PostMatchPlayer target, List<PostMatchPlayer> postMatchPlayerlist) {
        super("Inventory of " + UUIDUtils.name(target.getPlayerUuid()));

        this.target = Preconditions.checkNotNull(target, "target");
        this.postMatchPlayerlist = postMatchPlayerlist;
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

        buttons.put(getSlot(8, y), new PostMatchSwapTargetButton(otherPlayer, postMatchPlayerlist));
        buttons.put(getSlot(position, y), new MatchHistoryMenuButton(target.getPlayerUuid()) {
            @Override
            public String getName(Player player) {
                return  ChatColor.GREEN + "View " + target.getLastUsername() + "'s Profile";
            }
            @Override
            public List<String> getDescription(Player player) {
                return ImmutableList.of(
                    "",
                    ChatColor.AQUA + "Click" + ChatColor.YELLOW + " To View Match History",
                    ChatColor.AQUA + "Shift-Click" + ChatColor.YELLOW + " To View Total Stats"
                );
            }
            @Override
            public void clicked(Player player, int slot, ClickType clickType) {
                Button.playNeutral(player);
                if (clickType.isShiftClick()){
                    new StatisticsMenu(target.getPlayerUuid()).openMenu(player);
                } else {
                    MatchHistoryMenu menu = new MatchHistoryMenu(target.getPlayerUuid());
                    menu.openMenuAsync(player);
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