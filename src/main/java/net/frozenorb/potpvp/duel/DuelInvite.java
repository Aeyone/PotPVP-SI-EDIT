package net.frozenorb.potpvp.duel;

import com.google.common.base.Preconditions;

import net.frozenorb.potpvp.kittype.KitType;
import net.frozenorb.potpvp.arena.ArenaSchematic;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import lombok.Getter;

public abstract class DuelInvite<T> {

    @Getter private final T sender;
    @Getter private final T target;
    @Getter private final KitType kitType;
    @Getter private final ArenaSchematic arena;
    @Getter private final Instant timeSent;

    public DuelInvite(T sender, T target, KitType kitType) {
        this(sender, target, kitType, null);
    }

    public DuelInvite(T sender, T target, KitType kitType, ArenaSchematic arena) {
        this.sender = Preconditions.checkNotNull(sender, "sender");
        this.target = Preconditions.checkNotNull(target, "target");
        this.kitType = Preconditions.checkNotNull(kitType, "kitType");
        this.arena = arena;
        this.timeSent = Instant.now();
    }

    public boolean isExpired() {
        long sentAgo = ChronoUnit.SECONDS.between(timeSent, Instant.now());
        return sentAgo > DuelHandler.DUEL_INVITE_TIMEOUT_SECONDS;
    }
}