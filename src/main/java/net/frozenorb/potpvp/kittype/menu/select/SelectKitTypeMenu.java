package net.frozenorb.potpvp.kittype.menu.select;

import com.google.common.base.Preconditions;

import net.frozenorb.potpvp.PotPvPSI;
import net.frozenorb.potpvp.kittype.KitType;
import net.frozenorb.potpvp.party.Party;
import net.frozenorb.potpvp.util.InventoryUtils;
import net.frozenorb.qlib.menu.Button;
import net.frozenorb.qlib.menu.Menu;
import net.frozenorb.qlib.util.Callback;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public final class SelectKitTypeMenu extends Menu {

    private final boolean reset;
    private final Callback<KitType> callback;

    public SelectKitTypeMenu(Callback<KitType> callback, String title) {
        this(callback, true, title);
    }

    public SelectKitTypeMenu(Callback<KitType> callback, boolean reset, String title) {
        super(ChatColor.BLUE.toString() + ChatColor.BOLD + title);

        this.callback = Preconditions.checkNotNull(callback, "callback");
        this.reset = reset;
    }

    @Override
    public void onClose(Player player) {
        if (reset) {
            InventoryUtils.resetInventoryDelayed(player);
        }
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        int index = 0;

        for (KitType kitType : KitType.getAllTypes()) {
            if (!player.isOp() && kitType.isHidden()) {
                continue;
            }

            buttons.put(index ++, new KitTypeButton(kitType, callback));
        }

        Party party = PotPvPSI.getInstance().getPartyHandler().getParty(player);
        if (party != null) {
            int line = (index + 8) / 9;
            for (int i = line * 9; i < line * 9 + 9; i ++) {
                buttons.put(i, Button.placeholder(Material.STAINED_GLASS_PANE, (byte)15));
            }
            buttons.put((line + 1) * 9, new KitTypeButton(KitType.teamFight, callback));
        }

        return buttons;
    }

}