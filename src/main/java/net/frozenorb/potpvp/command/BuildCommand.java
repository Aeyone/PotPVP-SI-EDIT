package net.frozenorb.potpvp.command;

import net.frozenorb.potpvp.PotPvPSI;
import net.frozenorb.potpvp.util.InventoryUtils;
import net.frozenorb.potpvp.util.PatchedPlayerUtils;
import net.frozenorb.qlib.command.Command;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

public final class BuildCommand {

    @Command(names = {"build"}, permission = "op")
    public static void silent(Player sender) {
        if (sender.hasMetadata("Build")) {
            sender.removeMetadata("Build", PotPvPSI.getInstance());
            sender.sendMessage(ChatColor.RED + "Build mode disabled.");
            if (PotPvPSI.getInstance().getLobbyHandler().isInLobby(sender)) {
                PatchedPlayerUtils.resetInventory(sender, GameMode.SURVIVAL, true);
                InventoryUtils.resetInventoryDelayed(sender);
            }
            sender.setGameMode(GameMode.SURVIVAL);
        } else {
            sender.setMetadata("Build", new FixedMetadataValue(PotPvPSI.getInstance(), true));
            sender.sendMessage(ChatColor.GREEN + "Build mode enabled.");
            if (PotPvPSI.getInstance().getLobbyHandler().isInLobby(sender)) {
                sender.getInventory().clear();
            }
            sender.setGameMode(GameMode.CREATIVE);
        }
    }

}