package net.frozenorb.potpvp.command;

import net.frozenorb.potpvp.PotPvPSI;
import net.frozenorb.qlib.command.Command;

import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class LobbyCommand {
    @Command(names = {"hub", "lobby"}, permission = "")
    public static void lobby(Player sender) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DataOutputStream data = new DataOutputStream(out);

            data.writeUTF("Connect");
            data.writeUTF("Lobby");

            sender.sendPluginMessage(PotPvPSI.getInstance(), "BungeeCord", out.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
