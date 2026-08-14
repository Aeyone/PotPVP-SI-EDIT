package net.frozenorb.potpvp.match;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import net.frozenorb.potpvp.bot.BotManager;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.libs.com.google.gson.JsonObject;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;
import org.spigotmc.SpigotConfig;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import lombok.Getter;
import lombok.Setter;
import net.frozenorb.potpvp.PotPvPSI;
import net.frozenorb.potpvp.arena.Arena;
import net.frozenorb.potpvp.elo.EloCalculator;
import net.frozenorb.potpvp.kittype.KitType;
import net.frozenorb.potpvp.lobby.LobbyHandler;
import net.frozenorb.potpvp.match.event.MatchCountdownStartEvent;
import net.frozenorb.potpvp.match.event.MatchEndEvent;
import net.frozenorb.potpvp.match.event.MatchSpectatorJoinEvent;
import net.frozenorb.potpvp.match.event.MatchSpectatorLeaveEvent;
import net.frozenorb.potpvp.match.event.MatchStartEvent;
import net.frozenorb.potpvp.match.event.MatchTerminateEvent;
import net.frozenorb.potpvp.match.replay.ReplayableAction;
import net.frozenorb.potpvp.postmatchinv.PostMatchPlayer;
import net.frozenorb.potpvp.setting.Setting;
import net.frozenorb.potpvp.setting.SettingHandler;
import net.frozenorb.potpvp.util.InventoryUtils;
import net.frozenorb.potpvp.util.ItemListener;
import net.frozenorb.potpvp.util.MongoUtils;
import net.frozenorb.potpvp.util.PatchedPlayerUtils;
import net.frozenorb.potpvp.util.VisibilityUtils;
import net.frozenorb.qlib.nametag.FrozenNametagHandler;
import net.frozenorb.qlib.util.UUIDUtils;
import net.hylist.profile.KnockbackProfile;
import net.hylist.profile.PotionProfile;

public final class Match {
    
    private static final int MATCH_END_DELAY_SECONDS = 3;
    private static final int SUMO_ROUNDS_TO_WIN = 3;
    private static final int SUMO_COUNTDOWN_SECONDS = 3;
    
    @Getter
    private final String _id = UUID.randomUUID().toString().substring(0, 7);
    
    @Getter
    private final KitType kitType;
    @Getter
    private final Arena arena;
    @Getter
    private final List<MatchTeam> teams; // immutable so @Getter is ok
    private final Map<UUID, PostMatchPlayer> postMatchPlayers = new HashMap<>();
    private final Set<UUID> spectators = new HashSet<>();
    private final transient Map<MatchTeam, Integer> sumoRoundWins = new HashMap<>();
    private final transient Set<UUID> sumoRoundSpectators = new HashSet<>();
    private final transient Set<UUID> sumoWithdrawnPlayers = new HashSet<>();
    private transient BukkitRunnable countdownTask;
    
    @Getter
    private MatchTeam winner;
    @Getter
    private MatchEndReason endReason;
    @Getter
    private MatchState state;
    @Getter
    private Date startedAt;
    @Getter
    private Date endedAt;
    @Getter
    private boolean ranked;
    
    // we track if matches should give a rematch diamond manually. previouly
    // we just checked if both teams had 1 player on them, but this wasn't
    // always accurate. Scenarios like a team split of a 3 man team (with one
    // sitting out) would get treated as a 1v1 when calculating rematches.
    // https://github.com/FrozenOrb/PotPvP-SI/issues/19
    // this will also be set to false for ranked matches (which don't allow
    // rematches)
    @Getter
    private boolean allowRematches;
    @Getter
    @Setter
    private EloCalculator.Result eloChange;
    
    // this will keep track of blocks placed by players during this match.
    // it'll only be populated if the KitType allows building in the first place.
    private final Set<BlockVector> placedBlocks = new HashSet<>();
    
    // we only spectators generate one message (either a join or a leave)
    // per match, to prevent spam. This tracks who has used their one message
    private final transient Set<UUID> spectatorMessagesUsed = new HashSet<>();
    
    @Getter
    private Map<UUID, UUID> lastHit = Maps.newHashMap();
    @Getter
    private Map<UUID, Integer> combos = Maps.newHashMap();
    @Getter
    private Map<UUID, Integer> totalHits = Maps.newHashMap();
    @Getter
    private Map<UUID, Integer> blockedHits = Maps.newHashMap();
    @Getter
    private Map<UUID, Integer> longestCombo = Maps.newHashMap();
    @Getter
    private Map<UUID, Integer> missedPots = Maps.newHashMap();

    @Getter
    private Map<UUID, Double> thrownHp = Maps.newHashMap();
    @Getter
    private Map<UUID, Double> missedHp = Maps.newHashMap();
    @Getter
    private Map<UUID, Double> thrownDebuffs = Maps.newHashMap();
    @Getter
    private Map<UUID, Double> missedDebuffs = Maps.newHashMap();


    @Getter
    private List<ReplayableAction> replayableActions = Lists.newArrayList();
    
    @Getter
    private Set<UUID> allPlayers = Sets.newHashSet();
    
    @Getter
    private Set<UUID> winningPlayers;
    @Getter
    private Set<UUID> losingPlayers;
    
    public Match(KitType kitType, Arena arena, List<MatchTeam> teams, boolean ranked, boolean allowRematches) {
        this.kitType = Preconditions.checkNotNull(kitType, "kitType");
        this.arena = Preconditions.checkNotNull(arena, "arena");
        this.teams = ImmutableList.copyOf(teams);
        this.ranked = ranked;
        this.allowRematches = allowRematches;
        
        saveState();
        applyConfig();
    }

    private void applyConfig(){
        PotionProfile potProfile = SpigotConfig.potionManager.getProfileByName(this.kitType.getId());

        for (MatchTeam team : teams) {
            for (UUID playerUuid : team.getAllMembers()) {
                Player player = Bukkit.getPlayer(playerUuid);
                if (player != null) {
                    player.setPotProfile(potProfile);
                }
            }
        }
    }

    private void applyKnockbackProfile() {
        KnockbackProfile kbProfile = SpigotConfig.knockbackManager.getProfileByName(this.kitType.getId());

        for (MatchTeam team : teams) {
            for (UUID playerUuid : team.getAliveMembers()) {
                if (!isControllingPlayer(playerUuid)) {
                    continue;
                }

                Player player = Bukkit.getPlayer(playerUuid);
                if (player != null) {
                    player.setKbProfile(kbProfile);
                }
            }
        }
    }

    private void saveState() {
        if (kitType.isBuildingAllowed())
            this.arena.takeSnapshot();
    }
    
    void startCountdown() {
        startCountdown(5, true);
    }

    private void startCountdown(int countdownSeconds, boolean initialCountdown) {
        cancelCountdownTask();
        state = MatchState.COUNTDOWN;

        Set<Player> updateVisiblity = new HashSet<>();

        for (MatchTeam team : this.getTeams()) {
            for (UUID playerUuid : team.getAllMembers()) {

                if (!team.isAlive(playerUuid))
                    continue;

                Player player = Bukkit.getPlayer(playerUuid);
                if (player == null) {
                    team.markDead(playerUuid);
                    if (isSumoBo5()) {
                        sumoWithdrawnPlayers.add(playerUuid);
                        releasePlayerOwnership(playerUuid);
                    }
                    continue;
                }

                if (!claimPlayingOwnership(playerUuid)) {
                    team.markDead(playerUuid);
                    if (isSumoBo5()) {
                        sumoWithdrawnPlayers.add(playerUuid);
                    }
                    continue;
                }

                Location spawn = (team == teams.get(0) ? arena.getTeam1Spawn() : arena.getTeam2Spawn()).clone();
                Vector oldDirection = spawn.getDirection();

                Block block = spawn.getBlock();
                while (block.getRelative(BlockFace.DOWN).getType() == Material.AIR) {
                    block = block.getRelative(BlockFace.DOWN);
                    if (block.getY() <= 0) {
                        block = spawn.getBlock();
                        break;
                    }
                }

                spawn = block.getLocation();
                spawn.setDirection(oldDirection);
                spawn.add(0.5, 0, 0.5);

                player.teleport(spawn);
                player.setVelocity(new Vector());
                player.setFallDistance(0F);
                player.getInventory().setHeldItemSlot(0);
                if (player.hasMetadata("Build")) {
                    player.removeMetadata("Build", PotPvPSI.getInstance());
                }

                FrozenNametagHandler.reloadPlayer(player);
                FrozenNametagHandler.reloadOthersFor(player);

                updateVisiblity.add(player);
                PatchedPlayerUtils.resetInventory(player, GameMode.SURVIVAL);
            }
        }

        if (finishSumoIfTeamUnavailable()) {
            return;
        }

        applyKnockbackProfile();

        // we wait to update visibility until everyone's been put in the player cache
        // then we update vis, otherwise the update code will see 'partial' views of the
        // match
        updateVisiblity.forEach(VisibilityUtils::updateVisibilityFlicker);
        sendFriendlyUuidsToBots();

        if (initialCountdown) {
            Bukkit.getPluginManager().callEvent(new MatchCountdownStartEvent(this));
        }

        BukkitRunnable task = new BukkitRunnable() {

            int countdownTimeRemaining = countdownSeconds;

            public void run() {
                if (Match.this.countdownTask != this) {
                    cancel();
                    return;
                }

                if (state != MatchState.COUNTDOWN) {
                    cancel();
                    Match.this.countdownTask = null;
                    return;
                }

                if (countdownTimeRemaining == 0) {
                    cancel();
                    Match.this.countdownTask = null;
                    playSoundAll(Sound.NOTE_PLING, 2F);
                    if (initialCountdown) {
                        startMatch();
                    } else {
                        resumeSumoRound();
                    }
                    return; // so we don't send '0...' message
                } else if (countdownTimeRemaining <= 3) {
                    playSoundAll(Sound.NOTE_PLING, 1F);
                }

                messageAll(ChatColor.YELLOW.toString() + countdownTimeRemaining + "...");
                countdownTimeRemaining--;
            }

        };

        countdownTask = task;
        task.runTaskTimer(PotPvPSI.getInstance(), 0L, 20L);
    }

    private void cancelCountdownTask() {
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }
    }

    private void resumeSumoRound() {
        state = MatchState.IN_PROGRESS;
    }
    
    private void startMatch() {
        state = MatchState.IN_PROGRESS;
        startedAt = new Date();
        
        messageAll(ChatColor.GREEN + "Match started.");
        Bukkit.getPluginManager().callEvent(new MatchStartEvent(this));
    }

    private void sendFriendlyUuidsToBots() {
        BotManager botManager = PotPvPSI.getInstance().getBotManager();

        for (MatchTeam team : teams) {
            for (UUID playerUuid : team.getAliveMembers()) {
                Player player = Bukkit.getPlayer(playerUuid);

                if (player != null && botManager.getList().contains(player.getName())) {
                    botManager.applyFriendlyUuids(player, team.getAliveMembers());
                }
            }
        }
    }
    
    public void endMatch(MatchEndReason reason) {
        // prevent duplicate endings
        if (state == MatchState.ENDING || state == MatchState.TERMINATED) {
            return;
        }

        cancelCountdownTask();
        state = MatchState.ENDING;
        endedAt = new Date();
        endReason = reason;
        
        try {
            for (MatchTeam matchTeam : this.getTeams()) {
                for (UUID playerUuid : matchTeam.getAllMembers()) {
                    allPlayers.add(playerUuid);
                    if (!matchTeam.isAlive(playerUuid))
                        continue;
                    Player player = Bukkit.getPlayer(playerUuid);

                    if (player == null || !isControllingPlayer(playerUuid)) {
                        matchTeam.markDead(playerUuid);
                        sumoWithdrawnPlayers.add(playerUuid);
                        releasePlayerOwnership(playerUuid);
                        continue;
                    }

                    postMatchPlayers.computeIfAbsent(
                            playerUuid,
                            v -> new PostMatchPlayer(
                                player,
                                kitType,
                                totalHits.getOrDefault(player.getUniqueId(), 0),
                                blockedHits.getOrDefault(player.getUniqueId(),0),
                                longestCombo.getOrDefault(player.getUniqueId(), 0),
                                missedPots.getOrDefault(player.getUniqueId(), 0),
                                thrownHp.getOrDefault(player.getUniqueId(), 0.0D),
                                missedHp.getOrDefault(player.getUniqueId(), 0.0D),
                                thrownDebuffs.getOrDefault(player.getUniqueId(), 0.0D),
                                missedDebuffs.getOrDefault(player.getUniqueId(), 0.0D)
                            )
                    );
                }
            }
            
            Bukkit.getPluginManager().callEvent(new MatchEndEvent(this));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        int delayTicks = MATCH_END_DELAY_SECONDS * 20;
        if (JavaPlugin.getProvidingPlugin(this.getClass()).isEnabled()) {
            // cancel match end delay
            Bukkit.getScheduler().runTaskLater(PotPvPSI.getInstance(), this::preparTerminate, 1);
            Bukkit.getScheduler().runTaskLater(PotPvPSI.getInstance(), this::terminateMatch, delayTicks);
        } else {
            this.preparTerminate();
            this.terminateMatch();
        }
    }

    private void preparTerminate() {
        // if the match ends before the countdown ends
        // we have to set this to avoid a NPE in Date#from
        if (startedAt == null) {
            startedAt = new Date();
        }

        // if endedAt wasn't set before (if terminateMatch was called directly)
        // we want to make sure we set an ending time. Otherwise we keep the
        // technically more accurate time set in endMatch
        if (endedAt == null) {
            endedAt = new Date();
        }

        if (winner == null) {
            // Force-ended matches may not have a winner at all.
            this.winningPlayers = Sets.newHashSet();
            this.losingPlayers = Sets.newHashSet();
        } else {
            this.winningPlayers = Sets.newHashSet(winner.getAllMembers());
            this.losingPlayers = teams.stream()
                .filter(team -> team != winner)
                .flatMap(team -> team.getAllMembers().stream())
                .collect(Collectors.toSet());
        }

        Bukkit.getPluginManager().callEvent(new MatchTerminateEvent(this));
    }
    
    private void terminateMatch() {
        // prevent double terminations
        if (state == MatchState.TERMINATED) {
            return;
        }

        state = MatchState.TERMINATED;

        // we have to make a few edits to the document so we use Gson (which has
        // adapters
        // for things like Locations) and then edit it
        JsonObject document = PotPvPSI.getGson().toJsonTree(this).getAsJsonObject();
        
        document.addProperty("winner", winner == null ? -1 : teams.indexOf(winner)); // replace the full team with their index in the full list
        document.addProperty("arena", arena.getSchematic()); // replace the full arena with its schematic (website doesn't care which copy we
                                                             // used)
        
        Bukkit.getScheduler().runTaskAsynchronously(PotPvPSI.getInstance(), () -> {
            // The Document#parse call really sucks. It generates literally thousands of
            // objects per call.
            // Hopefully we'll be moving to just posting to a web service soon enough (and
            // then we don't have to run
            // Mongo's stupid JSON parser)
            Document parsedDocument = Document.parse(document.toString());
            parsedDocument.put("startedAt", startedAt);
            parsedDocument.put("endedAt", endedAt);
            MongoUtils.getCollection(MatchHandler.MONGO_COLLECTION_NAME).insertOne(parsedDocument);
        });
        
        MatchHandler matchHandler = PotPvPSI.getInstance().getMatchHandler();
        LobbyHandler lobbyHandler = PotPvPSI.getInstance().getLobbyHandler();
        
        Map<UUID, Match> playingCache = matchHandler.getPlayingMatchCache();
        Map<UUID, Match> spectateCache = matchHandler.getSpectatingMatchCache();
        
        if (kitType.isBuildingAllowed())
            arena.restore();
        PotPvPSI.getInstance().getArenaHandler().releaseArena(arena);
        matchHandler.removeMatch(this);

        BotManager botManager = PotPvPSI.getInstance().getBotManager();
        getTeams().forEach(team -> {
            team.getAllMembers().forEach(player -> {
                if (team.isAlive(player)) {
                    boolean ownedByThisMatch = playingCache.remove(player, this);
                    ownedByThisMatch |= spectateCache.remove(player, this);

                    if (!ownedByThisMatch) {
                        return;
                    }

                    if (botManager.getList().contains(UUIDUtils.name(player))) {
                        botManager.delBot(UUIDUtils.name(player));
                    } else {
                        Player onlinePlayer = Bukkit.getPlayer(player);
                        if (onlinePlayer != null) {
                            lobbyHandler.returnToLobby(onlinePlayer);
                        }
                    }
                }
            });
        });
        
        spectators.forEach(player -> {
            Player onlinePlayer = Bukkit.getPlayer(player);
            boolean ownedByThisMatch = playingCache.remove(player, this);
            ownedByThisMatch |= spectateCache.remove(player, this);

            if (onlinePlayer != null && ownedByThisMatch) {
                if (botManager.getList().contains(UUIDUtils.name(player))) {
                    botManager.delBot(UUIDUtils.name(player));
                } else {
                    lobbyHandler.returnToLobby(onlinePlayer);
                }
            }
        });

        sumoRoundSpectators.clear();
        sumoWithdrawnPlayers.clear();
        spectators.clear();
    }
    
    public Set<UUID> getSpectators() {
        return ImmutableSet.copyOf(spectators);
    }
    
    public Map<UUID, PostMatchPlayer> getPostMatchPlayers() {
        return ImmutableMap.copyOf(postMatchPlayers);
    }

    public boolean handleSumoRoundLoss(Player loser) {
        if (!kitType.getId().equals("SUMO") || state != MatchState.IN_PROGRESS || teams.size() != 2) {
            return false;
        }

        MatchTeam losingTeam = getTeam(loser.getUniqueId());
        if (losingTeam == null) {
            return false;
        }

        markDead(loser, false);
        addSumoRoundSpectator(loser);

        if (!losingTeam.getAliveMembers().isEmpty()) {
            return true;
        }

        MatchTeam winningTeam = teams.get(0) == losingTeam ? teams.get(1) : teams.get(0);
        int wins = sumoRoundWins.getOrDefault(winningTeam, 0) + 1;
        sumoRoundWins.put(winningTeam, wins);

        if (wins >= SUMO_ROUNDS_TO_WIN) {
            winner = winningTeam;
            endMatch(MatchEndReason.ENEMIES_ELIMINATED);
            return true;
        }

        int roundsRemaining = SUMO_ROUNDS_TO_WIN - wins;
        String winnerName = UUIDUtils.name(winningTeam.getFirstAliveMember());

        for (MatchTeam team : teams) {
            for (UUID playerUuid : team.getAllMembers()) {
                if (!isControllingPlayer(playerUuid)) {
                    continue;
                }

                Player player = Bukkit.getPlayer(playerUuid);
                if (player == null) {
                    continue;
                }

                if (team == winningTeam) {
                    player.sendMessage(
                        ChatColor.YELLOW + "You have "+
                        ChatColor.GREEN + "won" +
                        ChatColor.YELLOW + " the round, you need " +
                        ChatColor.LIGHT_PURPLE + roundsRemaining +
                        ChatColor.YELLOW + " more to win."
                    );
                } else {
                    player.sendMessage(
                        ChatColor.WHITE + winnerName +
                        ChatColor.YELLOW + " has " +
                        ChatColor.GREEN + "won" +
                        ChatColor.YELLOW + " the round, they need " +
                        ChatColor.LIGHT_PURPLE + roundsRemaining +
                        ChatColor.YELLOW + " more to win."
                    );
                }
            }
        }

        for (UUID spectatorUuid : spectators) {
            if (!isControllingPlayer(spectatorUuid) || getPreviousTeam(spectatorUuid) != null) {
                continue;
            }

            Player spectator = Bukkit.getPlayer(spectatorUuid);
            if (spectator != null) {
                spectator.sendMessage(
                    ChatColor.WHITE + winnerName +
                    ChatColor.YELLOW + " has " +
                    ChatColor.GREEN + "won" +
                    ChatColor.YELLOW + " the round, they need " +
                    ChatColor.LIGHT_PURPLE + roundsRemaining +
                    ChatColor.YELLOW + " more to win."
                );
            }
        }

        for (MatchTeam team : teams) {
            Set<UUID> nextRoundMembers = new HashSet<>();

            for (UUID playerUuid : team.getAllMembers()) {
                if (sumoWithdrawnPlayers.contains(playerUuid)) {
                    continue;
                }

                Player player = Bukkit.getPlayer(playerUuid);
                if (player == null) {
                    sumoWithdrawnPlayers.add(playerUuid);
                    removeSumoRoundSpectator(playerUuid);
                    releasePlayerOwnership(playerUuid);
                    continue;
                }

                if (isOwnedByAnotherMatch(playerUuid)) {
                    sumoWithdrawnPlayers.add(playerUuid);
                    removeSumoRoundSpectator(playerUuid);
                    releasePlayerOwnership(playerUuid);
                    continue;
                }

                nextRoundMembers.add(playerUuid);
            }

            team.resetAliveMembers(nextRoundMembers);

            for (UUID playerUuid : nextRoundMembers) {
                Player player = Bukkit.getPlayer(playerUuid);
                if (player == null) {
                    team.markDead(playerUuid);
                    sumoWithdrawnPlayers.add(playerUuid);
                    releasePlayerOwnership(playerUuid);
                    continue;
                }

                postMatchPlayers.remove(playerUuid);
                removeSumoRoundSpectator(playerUuid);

                player.setFlying(false);
                player.setAllowFlight(false);
            }
        }

        if (finishSumoIfTeamUnavailable()) {
            return true;
        }

        startCountdown(SUMO_COUNTDOWN_SECONDS, false);
        return true;
    }
    
    private void checkEnded() {
        if (state == MatchState.ENDING || state == MatchState.TERMINATED) {
            return;
        }
        
        List<MatchTeam> teamsAlive = new ArrayList<>();
        
        for (MatchTeam team : teams) {
            if (!team.getAliveMembers().isEmpty()) {
                teamsAlive.add(team);
            }
        }
        
        if (teamsAlive.size() == 1) {
            this.winner = teamsAlive.get(0);
            endMatch(MatchEndReason.ENEMIES_ELIMINATED);
        }
    }
    
    public boolean isSpectator(UUID uuid) {
        return spectators.contains(uuid);
    }

    public boolean isSumoRoundSpectator(UUID uuid) {
        return sumoRoundSpectators.contains(uuid);
    }

    public boolean isControllingPlayer(UUID playerUuid) {
        MatchHandler matchHandler = PotPvPSI.getInstance().getMatchHandler();
        return matchHandler.getPlayingMatchCache().get(playerUuid) == this ||
            matchHandler.getSpectatingMatchCache().get(playerUuid) == this;
    }

    public boolean hasWithdrawnPlayers() {
        return !sumoWithdrawnPlayers.isEmpty();
    }

    public boolean withdrawSumoParticipant(UUID playerUuid, boolean returnToLobby) {
        Set<UUID> player = new HashSet<>();
        player.add(playerUuid);
        return withdrawSumoParticipants(player, returnToLobby);
    }

    public boolean withdrawSumoParticipants(Collection<UUID> playerUuids, boolean returnToLobby) {
        if (!isSumoBo5() || playerUuids.isEmpty()) {
            return false;
        }

        MatchHandler matchHandler = PotPvPSI.getInstance().getMatchHandler();
        List<Player> playersToReturn = new ArrayList<>();
        boolean changed = false;

        for (UUID playerUuid : new HashSet<>(playerUuids)) {
            MatchTeam team = getPreviousTeam(playerUuid);
            if (team == null || sumoWithdrawnPlayers.contains(playerUuid)) {
                continue;
            }

            boolean controlledByThisMatch = isControllingPlayer(playerUuid);
            Player player = Bukkit.getPlayer(playerUuid);

            sumoWithdrawnPlayers.add(playerUuid);
            changed = true;

            if (team.isAlive(playerUuid)) {
                team.markDead(playerUuid);
                if (controlledByThisMatch && player != null) {
                    capturePostMatchPlayer(player);
                }
            }

            sumoRoundSpectators.remove(playerUuid);
            spectators.remove(playerUuid);
            releasePlayerOwnership(playerUuid);

            if (
                returnToLobby &&
                controlledByThisMatch &&
                player != null &&
                !matchHandler.isPlayingOrSpectatingMatch(player)
            ) {
                playersToReturn.add(player);
            }
        }

        for (Player player : playersToReturn) {
            PotPvPSI.getInstance().getLobbyHandler().returnToLobby(player);
        }

        if (changed && (state == MatchState.COUNTDOWN || state == MatchState.IN_PROGRESS)) {
            finishSumoIfTeamUnavailable();
        }

        return changed;
    }

    private void addSumoRoundSpectator(Player player) {
        Map<UUID, Match> spectateCache = PotPvPSI.getInstance().getMatchHandler().getSpectatingMatchCache();

        if (isOwnedByAnotherMatch(player.getUniqueId())) {
            sumoWithdrawnPlayers.add(player.getUniqueId());
            return;
        }

        spectateCache.put(player.getUniqueId(), this);
        spectators.add(player.getUniqueId());
        sumoRoundSpectators.add(player.getUniqueId());

        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.getInventory().setHeldItemSlot(0);
        FrozenNametagHandler.reloadPlayer(player);
        FrozenNametagHandler.reloadOthersFor(player);
        VisibilityUtils.updateVisibility(player);
        PatchedPlayerUtils.resetInventory(player, GameMode.CREATIVE, true);
        player.setAllowFlight(true);
        player.setFlying(true);
    }

    private void removeSumoRoundSpectator(UUID playerUuid) {
        if (!sumoRoundSpectators.remove(playerUuid)) {
            return;
        }

        Map<UUID, Match> spectateCache = PotPvPSI.getInstance().getMatchHandler().getSpectatingMatchCache();
        spectateCache.remove(playerUuid, this);
        spectators.remove(playerUuid);
    }
    
    public void addSpectator(Player player, Player target) {
        addSpectator(player, target, false);
    }
    
    // fromMatch indicates if they were a player immediately before spectating.
    // we use this for things like teleporting and messages
    public void addSpectator(Player player, Player target, boolean fromMatch) {
        if (!fromMatch && state == MatchState.ENDING) {
            player.sendMessage(ChatColor.RED + "This match is no longer available for spectating.");
            return;
        }
        
        Map<UUID, Match> spectateCache = PotPvPSI.getInstance().getMatchHandler().getSpectatingMatchCache();

        if (isOwnedByAnotherMatch(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You are already spectating another match.");
            return;
        }

        spectateCache.put(player.getUniqueId(), this);
        spectators.add(player.getUniqueId());
        
        if (!fromMatch) {
            Location tpTo = arena.getSpectatorSpawn();
            
            if (target != null) {
                // we tp them a bit up so they're not inside of their target
                tpTo = target.getLocation().clone().add(0, 1.5, 0);
            }
            
            player.teleport(tpTo);
            player.sendMessage(ChatColor.YELLOW + "Now spectating " + ChatColor.AQUA + getSimpleDescription(true) + ChatColor.YELLOW + "...");
            sendSpectatorMessage(player, ChatColor.AQUA + player.getName() + ChatColor.YELLOW + " is now spectating.");
        } else {
            // so players don't accidentally click the item to stop spectating
            player.getInventory().setHeldItemSlot(0);
        }
        
        FrozenNametagHandler.reloadPlayer(player);
        FrozenNametagHandler.reloadOthersFor(player);
        
        VisibilityUtils.updateVisibility(player);
        PatchedPlayerUtils.resetInventory(player, GameMode.CREATIVE, true); // because we're about to reset their inv on a timer
        InventoryUtils.resetInventoryDelayed(player);
        player.setAllowFlight(true);
        player.setFlying(true); // called after PlayerUtils reset, make sure they don't fall out of the sky
        ItemListener.addButtonCooldown(player, 1_500);
        
        Bukkit.getPluginManager().callEvent(new MatchSpectatorJoinEvent(player, this));
    }
    
    public void removeSpectator(Player player) {
        removeSpectator(player, true);
    }
    
    public void removeSpectator(Player player, boolean returnToLobby) {
        if (isSumoRoundSpectator(player.getUniqueId())) {
            withdrawSumoParticipant(player.getUniqueId(), returnToLobby);
            return;
        }

        Map<UUID, Match> spectateCache = PotPvPSI.getInstance().getMatchHandler().getSpectatingMatchCache();

        if (!spectateCache.remove(player.getUniqueId(), this)) {
            spectators.remove(player.getUniqueId());
            return;
        }

        spectators.remove(player.getUniqueId());
        ItemListener.addButtonCooldown(player, 1_500);
        
        sendSpectatorMessage(player, ChatColor.AQUA + player.getName() + ChatColor.YELLOW + " is no longer spectating.");
        
        if (returnToLobby) {
            PotPvPSI.getInstance().getLobbyHandler().returnToLobby(player);
        }
        
        Bukkit.getPluginManager().callEvent(new MatchSpectatorLeaveEvent(player, this));
    }
    
    private void sendSpectatorMessage(Player spectator, String message) {
        // see comment on spectatorMessagesUsed field for more
        if (spectator.hasMetadata("ModMode") || !spectatorMessagesUsed.add(spectator.getUniqueId())) {
            return;
        }
        
        SettingHandler settingHandler = PotPvPSI.getInstance().getSettingHandler();
        
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online == spectator) {
                continue;
            }
            
            boolean sameMatch = isSpectator(online.getUniqueId()) || getTeam(online.getUniqueId()) != null;
            boolean spectatorMessagesEnabled = settingHandler.getSetting(online, Setting.SHOW_SPECTATOR_JOIN_MESSAGES);
            
            if (sameMatch && spectatorMessagesEnabled) {
                online.sendMessage(message);
            }
        }
    }
    
    public void markDead(Player player) {
        markDead(player, true);
    }

    private void markDead(Player player, boolean checkForEnd) {
        MatchTeam team = getTeam(player.getUniqueId());
        
        if (team == null) {
            return;
        }
        
        team.markDead(player.getUniqueId());
        PotPvPSI.getInstance().getMatchHandler().getPlayingMatchCache().remove(player.getUniqueId(), this);
        capturePostMatchPlayer(player);

        if (checkForEnd) {
            checkEnded();
        }
    }

    private void capturePostMatchPlayer(Player player) {
        postMatchPlayers.put(
                player.getUniqueId(),
                new PostMatchPlayer(
                        player,
                        kitType,
                        totalHits.getOrDefault(player.getUniqueId(), 0),
                        blockedHits.getOrDefault(player.getUniqueId(),0),
                        longestCombo.getOrDefault(player.getUniqueId(), 0),
                        missedPots.getOrDefault(player.getUniqueId(), 0),
                        thrownHp.getOrDefault(player.getUniqueId(), 0.0D),
                        missedHp.getOrDefault(player.getUniqueId(), 0.0D),
                        thrownDebuffs.getOrDefault(player.getUniqueId(), 0.0D),
                        missedDebuffs.getOrDefault(player.getUniqueId(), 0.0D)
                )
        );
    }

    private boolean isSumoBo5() {
        return kitType.getId().equals("SUMO") && teams.size() == 2;
    }

    private boolean claimPlayingOwnership(UUID playerUuid) {
        MatchHandler matchHandler = PotPvPSI.getInstance().getMatchHandler();
        Match playingMatch = matchHandler.getPlayingMatchCache().get(playerUuid);
        Match spectatingMatch = matchHandler.getSpectatingMatchCache().get(playerUuid);

        if ((playingMatch != null && playingMatch != this) || (spectatingMatch != null && spectatingMatch != this)) {
            return false;
        }

        matchHandler.getPlayingMatchCache().put(playerUuid, this);
        return true;
    }

    private boolean isOwnedByAnotherMatch(UUID playerUuid) {
        MatchHandler matchHandler = PotPvPSI.getInstance().getMatchHandler();
        Match playingMatch = matchHandler.getPlayingMatchCache().get(playerUuid);
        Match spectatingMatch = matchHandler.getSpectatingMatchCache().get(playerUuid);

        return (playingMatch != null && playingMatch != this) || (spectatingMatch != null && spectatingMatch != this);
    }

    private boolean releasePlayerOwnership(UUID playerUuid) {
        MatchHandler matchHandler = PotPvPSI.getInstance().getMatchHandler();
        boolean released = matchHandler.getPlayingMatchCache().remove(playerUuid, this);
        released |= matchHandler.getSpectatingMatchCache().remove(playerUuid, this);
        return released;
    }

    private boolean finishSumoIfTeamUnavailable() {
        if (!isSumoBo5() || state == MatchState.ENDING || state == MatchState.TERMINATED) {
            return state == MatchState.ENDING || state == MatchState.TERMINATED;
        }

        List<MatchTeam> teamsAlive = new ArrayList<>();
        for (MatchTeam team : teams) {
            if (!team.getAliveMembers().isEmpty()) {
                teamsAlive.add(team);
            }
        }

        if (teamsAlive.size() >= 2) {
            return false;
        }

        winner = teamsAlive.size() == 1 ? teamsAlive.get(0) : null;
        endMatch(MatchEndReason.ENEMIES_ELIMINATED);
        return true;
    }
    
    public MatchTeam getTeam(UUID playerUuid) {
        for (MatchTeam team : teams) {
            if (team.isAlive(playerUuid)) {
                return team;
            }
        }
        
        return null;
    }
    
    public MatchTeam getPreviousTeam(UUID playerUuid) {
        for (MatchTeam team : teams) {
            if (team.getAllMembers().contains(playerUuid)) {
                return team;
            }
        }
        
        return null;
    }
    
    /**
     * Creates a simple, one line description of this match This will include two
     * players (if a 1v1) or player counts and the kit type
     * 
     * @return A simple description of this match
     */
    public String getSimpleDescription(boolean includeRankedUnranked) {
        String players;
        
        if (teams.size() == 2) {
            MatchTeam teamA = teams.get(0);
            MatchTeam teamB = teams.get(1);
            
            if (teamA.getAliveMembers().size() == 1 && teamB.getAliveMembers().size() == 1) {
                String nameA = UUIDUtils.name(teamA.getFirstAliveMember());
                String nameB = UUIDUtils.name(teamB.getFirstAliveMember());
                
                players = nameA + " vs " + nameB;
            } else {
                players = teamA.getAliveMembers().size() + " vs " + teamB.getAliveMembers().size();
            }
        } else {
            int numTotalPlayers = 0;
            
            for (MatchTeam team : teams) {
                numTotalPlayers += team.getAliveMembers().size();
            }
            
            players = numTotalPlayers + " player fight";
        }
        
        if (includeRankedUnranked) {
            String rankedStr = ranked ? "Ranked" : "Unranked";
            return players + " (" + rankedStr + " " + kitType.getDisplayName() + ")";
        } else {
            return players;
        }
    }
    
    /**
     * Sends a basic chat message to all alive participants and spectators
     * 
     * @param message
     *            the message to send
     */
    public void messageAll(String message) {
        messageAlive(message);
        messageSpectators(message);
    }
    
    /**
     * Plays a sound for all alive participants and spectators
     * 
     * @param sound
     *            the Sound to play
     * @param pitch
     *            the pitch to play the provided sound at
     */
    public void playSoundAll(Sound sound, float pitch) {
        playSoundAlive(sound, pitch);
        playSoundSpectators(sound, pitch);
    }
    
    /**
     * Sends a basic chat message to all spectators
     * 
     * @param message
     *            the message to send
     */
    public void messageSpectators(String message) {
        for (UUID spectator : spectators) {
            if (!isControllingPlayer(spectator)) {
                continue;
            }

            Player spectatorBukkit = Bukkit.getPlayer(spectator);
            
            if (spectatorBukkit != null) {
                spectatorBukkit.sendMessage(message);
            }
        }
    }
    
    /**
     * Plays a sound for all spectators
     * 
     * @param sound
     *            the Sound to play
     * @param pitch
     *            the pitch to play the provided sound at
     */
    public void playSoundSpectators(Sound sound, float pitch) {
        for (UUID spectator : spectators) {
            if (!isControllingPlayer(spectator)) {
                continue;
            }

            Player spectatorBukkit = Bukkit.getPlayer(spectator);
            
            if (spectatorBukkit != null) {
                spectatorBukkit.playSound(spectatorBukkit.getEyeLocation(), sound, 10F, pitch);
            }
        }
    }
    
    /**
     * Sends a basic chat message to all alive participants
     * 
     * @see MatchTeam#messageAlive(String)
     * @param message
     *            the message to send
     */
    public void messageAlive(String message) {
        for (MatchTeam team : teams) {
            for (UUID playerUuid : team.getAliveMembers()) {
                if (!isControllingPlayer(playerUuid)) {
                    continue;
                }

                Player player = Bukkit.getPlayer(playerUuid);
                if (player != null) {
                    player.sendMessage(message);
                }
            }
        }
    }
    
    /**
     * Plays a sound for all alive participants
     * 
     * @param sound
     *            the Sound to play
     * @param pitch
     *            the pitch to play the provided sound at
     */
    public void playSoundAlive(Sound sound, float pitch) {
        for (MatchTeam team : teams) {
            for (UUID playerUuid : team.getAliveMembers()) {
                if (!isControllingPlayer(playerUuid)) {
                    continue;
                }

                Player player = Bukkit.getPlayer(playerUuid);
                if (player != null) {
                    player.playSound(player.getLocation(), sound, 10F, pitch);
                }
            }
        }
    }
    
    /**
     * Records a placed block during this match. Used to keep track of which blocks
     * can be broken.
     */
    public void recordPlacedBlock(Block block) {
        placedBlocks.add(block.getLocation().toVector().toBlockVector());
    }
    
    /**
     * Checks if a block can be broken in this match. Only used if the KitType
     * allows building.
     */
    public boolean canBeBroken(Block block) {
        return (kitType.getId().equals("SPLEEF") && (block.getType() == Material.SNOW_BLOCK || block.getType() == Material.GRASS || block.getType() == Material.DIRT)) || placedBlocks.contains(block.getLocation().toVector().toBlockVector());
    }

    public int getAccuracy(UUID uuid) {
        double thrown = thrownHp.getOrDefault(uuid, 0.0D) ;
        double miss = missedHp.getOrDefault(uuid, 0.0D);
        if (thrown == 0) {
            return -1;
        }
        return (int)(100.0D - (miss / thrown) * 100);
    }
}
