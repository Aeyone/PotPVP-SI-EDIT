package net.frozenorb.potpvp.postmatchinv.menu;

import com.google.common.base.Preconditions;

import net.frozenorb.potpvp.postmatchinv.PostMatchPlayer;
import net.frozenorb.qlib.menu.Button;
import net.frozenorb.qlib.util.UUIDUtils;
import net.minecraft.util.com.google.common.collect.ImmutableList;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.ArrayList;
import java.util.List;

final class PostMatchSwapTargetButton extends Button {

    private final PostMatchPlayer newTarget;
    private List<PostMatchPlayer> postMatchPlayerlist = new ArrayList<>();
    private String matchId;

    PostMatchSwapTargetButton(PostMatchPlayer newTarget, List<PostMatchPlayer> postMatchPlayerlist, String matchId) {
        this.newTarget = Preconditions.checkNotNull(newTarget, "newTarget");
        this.postMatchPlayerlist = postMatchPlayerlist;
        this.matchId = matchId;
    }

    @Override
    public String getName(Player player) {
        return ChatColor.GREEN + "View " + UUIDUtils.name(newTarget.getPlayerUuid()) + "'s inventory";
    }

    @Override
    public List<String> getDescription(Player player) {
        return ImmutableList.of(
            "",
            ChatColor.YELLOW + "Swap your view to " + UUIDUtils.name(newTarget.getPlayerUuid()) + "'s inventory"
        );
    }

    @Override
    public Material getMaterial(Player player) {
        return Material.LEVER;
    }

    @Override
    public void clicked(Player player, int i, ClickType clickType) {
        Button.playNeutral(player);
        new PostMatchMenu(newTarget, postMatchPlayerlist, matchId).openMenu(player);
    }

}