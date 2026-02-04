package net.frozenorb.potpvp.duel;

import java.util.UUID;

import org.bukkit.entity.Player;

import net.frozenorb.potpvp.arena.ArenaSchematic;
import net.frozenorb.potpvp.kittype.KitType;

public final class PlayerDuelInvite extends DuelInvite<UUID> {

    public PlayerDuelInvite(Player sender, Player target, KitType kitType, ArenaSchematic arena) {
        super(sender.getUniqueId(), target.getUniqueId(), kitType, arena);
    }

}