package net.frozenorb.potpvp.fakechat.Command;

import net.frozenorb.potpvp.PotPvPSI;
import net.frozenorb.potpvp.fakechat.FakeChatGUI;
import net.frozenorb.qlib.qLib;
import net.frozenorb.qlib.command.Command;
import org.bukkit.entity.Player;

public final class FakeChatCommand {
    public FakeChatCommand() {
    }

    @Command(
            names = {"fakechat", "fc"},
            permission = ""
    )
    public static void fakechat(Player sender) {
        FakeChatGUI.openGUI(sender, PotPvPSI.getInstance());
    }
}
