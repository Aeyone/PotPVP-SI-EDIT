package net.frozenorb.potpvp.bot;

import net.frozenorb.potpvp.kittype.KitType;
import org.bukkit.entity.Player;

import java.util.Set;

public class BotPendingData {
    public Player target;
    public KitType kitType;
    public Set<String> allArenas;
    public String pendingType;

    public BotPendingData(Player target, KitType kitType, Set<String> allArenas, String pendingType) {
        this.target = target;
        this.kitType = kitType;
        this.allArenas = allArenas;
        this.pendingType =  pendingType;
    }
}
