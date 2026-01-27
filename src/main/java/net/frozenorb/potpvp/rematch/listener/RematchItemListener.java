package net.frozenorb.potpvp.rematch.listener;

import net.frozenorb.potpvp.PotPvPSI;
import net.frozenorb.potpvp.arena.ArenaSchematic;
import net.frozenorb.potpvp.arena.menu.select.SelectArenaMenu;
import net.frozenorb.potpvp.duel.command.AcceptCommand;
import net.frozenorb.potpvp.duel.command.DuelCommand;
import net.frozenorb.potpvp.rematch.RematchData;
import net.frozenorb.potpvp.rematch.RematchHandler;
import net.frozenorb.potpvp.rematch.RematchItems;
import net.frozenorb.potpvp.util.InventoryUtils;
import net.frozenorb.potpvp.util.ItemListener;
import net.frozenorb.potpvp.setting.Setting;
import net.frozenorb.potpvp.setting.SettingHandler;

import net.frozenorb.qlib.qLib;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableSet;

import java.util.ArrayList;

public final class RematchItemListener extends ItemListener {

    public RematchItemListener(RematchHandler rematchHandler) {
        addHandler(RematchItems.REQUEST_REMATCH_ITEM, player -> {
            RematchData rematchData = rematchHandler.getRematchData(player);

            if (rematchData != null) {
                Player target = Bukkit.getPlayer(rematchData.getTarget());
                SettingHandler settingHandler = PotPvPSI.getInstance().getSettingHandler();

                if (settingHandler.getSetting(player, Setting.SELECT_MAP)) {
                    DuelCommand.selectArena(player, target, rematchData.getKitType());
                } else {
                    DuelCommand.duel(player, target, rematchData.getKitType());
                }

                InventoryUtils.resetInventoryDelayed(player);
                InventoryUtils.resetInventoryDelayed(target);
            }
        });

        addHandler(RematchItems.SENT_REMATCH_ITEM, p -> p.sendMessage(ChatColor.RED + "You have already sent a rematch request."));

        addHandler(RematchItems.ACCEPT_REMATCH_ITEM, player -> {
            RematchData rematchData = rematchHandler.getRematchData(player);

            if (rematchData != null) {
                Player target = Bukkit.getPlayer(rematchData.getTarget());
                AcceptCommand.accept(player, target);
            }
        });
    }

}