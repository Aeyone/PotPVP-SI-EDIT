package net.frozenorb.potpvp.duel;

import net.frozenorb.potpvp.arena.ArenaSchematic;
import net.frozenorb.potpvp.kittype.KitType;
import net.frozenorb.potpvp.party.Party;

public final class PartyDuelInvite extends DuelInvite<Party> {

    public PartyDuelInvite(Party sender, Party target, KitType kitTypes, ArenaSchematic arena) {
        super(sender, target, kitTypes, arena);
    }

}