package net.frozenorb.potpvp.duel.command;

import jdk.nashorn.internal.runtime.regexp.joni.ast.StringNode;
import net.frozenorb.potpvp.PotPvPLang;
import net.frozenorb.potpvp.PotPvPSI;

import net.frozenorb.potpvp.arena.ArenaSchematic;
import net.frozenorb.potpvp.arena.menu.select.SelectArenaMenu;
import net.frozenorb.potpvp.duel.DuelHandler;
import net.frozenorb.potpvp.duel.DuelInvite;
import net.frozenorb.potpvp.duel.PartyDuelInvite;
import net.frozenorb.potpvp.duel.PlayerDuelInvite;

import net.frozenorb.potpvp.kittype.KitType;
import net.frozenorb.potpvp.kittype.menu.select.SelectKitTypeMenu;

import net.frozenorb.potpvp.lobby.LobbyHandler;

import net.frozenorb.potpvp.party.Party;
import net.frozenorb.potpvp.party.PartyHandler;

import net.frozenorb.potpvp.validation.PotPvPValidation;

import net.frozenorb.potpvp.setting.Setting;
import net.frozenorb.potpvp.setting.SettingHandler;

import net.frozenorb.qlib.command.Command;
import net.frozenorb.qlib.command.Param;
import net.frozenorb.qlib.qLib;
import net.frozenorb.qlib.util.UUIDUtils;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public final class DuelCommand {

    @Command(names = {"duel", "1v1"}, permission = "")
    public static void duel(Player sender, @Param(name = "player") Player target) {
        if (sender == target) {
            sender.sendMessage(ChatColor.RED + "You can't duel yourself!");
            return;
        }

        PartyHandler partyHandler = PotPvPSI.getInstance().getPartyHandler();
        LobbyHandler lobbyHandler = PotPvPSI.getInstance().getLobbyHandler();
        SettingHandler settingHandler = PotPvPSI.getInstance().getSettingHandler();

        Party senderParty = partyHandler.getParty(sender);
        Party targetParty = partyHandler.getParty(target);

        if (senderParty != null && targetParty != null) {
            // party dueling party (legal)
            if (!PotPvPValidation.canSendDuel(senderParty, targetParty, sender)) {
                return;
            }

            new SelectKitTypeMenu(kitType -> {
                sender.closeInventory();

                // reassign these fields so that any party changes
                // (kicks, etc) are reflectednow
                Party newSenderParty = partyHandler.getParty(sender);
                Party newTargetParty = partyHandler.getParty(target);

                if (newSenderParty != null && newTargetParty != null) {
                    if (newSenderParty.isLeader(sender.getUniqueId())) {
                        if(settingHandler.getSetting(sender, Setting.SELECT_MAP)){
                            selectArena(sender, newSenderParty, newTargetParty, kitType);
                        }else{
                            duel(sender, newSenderParty, newTargetParty, kitType);
                        }
                    } else {
                        sender.sendMessage(PotPvPLang.NOT_LEADER_OF_PARTY);
                    }
                }
            }, "Select a kit type...").openMenu(sender);
        } else if (senderParty == null && targetParty == null) {
            // player dueling player (legal)
            if (!PotPvPValidation.canSendDuel(sender, target)) {
                return;
            }

            if (target.hasPermission("potpvp.famous") && System.currentTimeMillis() - lobbyHandler.getLastLobbyTime(target) < 3_000) {
                sender.sendMessage(ChatColor.RED + target.getName() + " just returned to the lobby, please wait a moment.");
                return;
            }

            new SelectKitTypeMenu(
               kitType -> {
                  sender.closeInventory();
                  if(settingHandler.getSetting(sender, Setting.SELECT_MAP)){
                      selectArena(sender, target, kitType);
                  }else{
                      duel(sender, target, kitType);
                  }
               },
               "Select a kit type..."
            ).openMenu(sender);

        } else if (senderParty == null) {
            // player dueling party (illegal)
            sender.sendMessage(ChatColor.RED + "You must create a party to duel " + target.getName() + "'s party.");
        } else {
            // party dueling player (illegal)
            sender.sendMessage(ChatColor.RED + "You must leave your party to duel " + target.getName() + ".");
        }
    }
    public static void getArenas(
        Player sender,
        KitType kitType,
        Consumer<Set<String>> callback
    ) {
        new SelectArenaMenu(
            kitType,
            arenas -> {
                sender.closeInventory();
                callback.accept(arenas);
            },
            "Select an arena..."
        ).openMenu(sender);
    }

    public static ArenaSchematic getRandomArenaSchematic(Set<String> allArenas) { // randomly selected arena
        String arenaName = new ArrayList<>(allArenas).get(qLib.RANDOM.nextInt(allArenas.size()));

        for (ArenaSchematic schematic : PotPvPSI.getInstance().getArenaHandler().getSchematics()) {
           if(schematic.getName().equals(arenaName)){
              return schematic;
           }
        }
        return null;
    }

    public static void selectArena(Player sender, Player target, KitType kitType) {
        getArenas(sender, kitType, allArenas -> {
            duel(sender, target, kitType, getRandomArenaSchematic(allArenas), allArenas.size() == 1 ? "EXACT" : "RANDOM");
        });
    }

    public static void selectArena(Player sender, Party senderParty, Party targetParty, KitType kitType) {
        getArenas(sender, kitType, allArenas -> {
            duel(sender, senderParty, targetParty, kitType, getRandomArenaSchematic(allArenas), allArenas.size() == 1 ? "EXACT" : "RANDOM");
        });
    }

    public static void duel(Player sender, Player target, KitType kitType) {
        duel(sender, target, kitType, null, "RANDOM");
    }

    public static void duel(Player sender, Player target, KitType kitType, ArenaSchematic arena, String type) {
        if (!PotPvPValidation.canSendDuel(sender, target)) {
            return;
        }

        DuelHandler duelHandler = PotPvPSI.getInstance().getDuelHandler();
        DuelInvite alreadySentInvite = duelHandler.findInvite(sender, target);

        if (alreadySentInvite != null) {
            if (alreadySentInvite.getKitType() == kitType) {
                sender.sendMessage(
                    ChatColor.YELLOW + "You have already invited " +
                    ChatColor.AQUA + target.getName() +
                    ChatColor.YELLOW + " to a " +
                    kitType.getColoredDisplayName() +
                    ChatColor.YELLOW + " duel."
                );
                return;
            } else {
                // if an invite was already sent (with a different kit type)
                // just delete it (so /accept will accept the 'latest' invite)
                duelHandler.removeInvite(alreadySentInvite);
            }
        }

        String message = type.equals("RANDOM") ? ChatColor.YELLOW + "." : ChatColor.YELLOW + " on arena " + ChatColor.AQUA + arena.getName() + ".";

        target.sendMessage(
            ChatColor.AQUA + sender.getName() +
            ChatColor.YELLOW + " has sent you a " +
            kitType.getColoredDisplayName() +
            ChatColor.YELLOW + " duel" +
            message
        );
        target.spigot().sendMessage(createInviteNotification(sender.getName()));

        sender.sendMessage(
            ChatColor.YELLOW + "Successfully sent a " +
            kitType.getColoredDisplayName() +
            ChatColor.YELLOW + " duel invite to " +
            ChatColor.AQUA + target.getName() +
            message
        );
        duelHandler.insertInvite(new PlayerDuelInvite(sender, target, kitType, arena));

    }

    public static void duel(Player sender, Party senderParty, Party targetParty, KitType kitType) {
        duel(sender, senderParty, targetParty, kitType, null, "RANDOM");
    }

    public static void duel(Player sender, Party senderParty, Party targetParty, KitType kitType, ArenaSchematic arena, String type) {
        if (!PotPvPValidation.canSendDuel(senderParty, targetParty, sender)) {
            return;
        }

        DuelHandler duelHandler = PotPvPSI.getInstance().getDuelHandler();
        String targetPartyLeader = UUIDUtils.name(targetParty.getLeader());
        DuelInvite alreadySentInvite = duelHandler.findInvite(senderParty, targetParty);

        if (alreadySentInvite != null) {
            if (alreadySentInvite.getKitType() == kitType) {
                sender.sendMessage(ChatColor.YELLOW + "You have already invited " + ChatColor.AQUA + targetPartyLeader + "'s party" + ChatColor.YELLOW + " to a " + kitType.getColoredDisplayName() + ChatColor.YELLOW + " duel.");
                return;
            } else {
                // if an invite was already sent (with a different kit type)
                // just delete it (so /accept will accept the 'latest' invite)
                duelHandler.removeInvite(alreadySentInvite);
            }
        }

        String message = type.equals("RANDOM") ? ChatColor.YELLOW + "." : ChatColor.YELLOW + " on arena " + ChatColor.AQUA + arena.getName() + ".";

        targetParty.message(
            ChatColor.AQUA + sender.getName() + "'s Party (" + senderParty.getMembers().size() + ")" +
            ChatColor.YELLOW + " has sent you a " + kitType.getColoredDisplayName() +
            ChatColor.YELLOW + " duel" +
            message
        );

        Bukkit.getPlayer(targetParty.getLeader()).spigot().sendMessage(createInviteNotification(sender.getName()));

        sender.sendMessage(
            ChatColor.YELLOW + "Successfully sent a " + kitType.getColoredDisplayName() +
            ChatColor.YELLOW + " duel invite to " +
            ChatColor.AQUA + targetPartyLeader + "'s party" +
            message
        );
        duelHandler.insertInvite(new PartyDuelInvite(senderParty, targetParty, kitType, arena));
    }

    private static TextComponent[] createInviteNotification(String sender) {
        TextComponent firstPart = new TextComponent("Click here or type ");
        TextComponent commandPart = new TextComponent("/accept " + sender);
        TextComponent secondPart = new TextComponent(" to accept the invite");

        firstPart.setColor(net.md_5.bungee.api.ChatColor.GOLD);
        commandPart.setColor(net.md_5.bungee.api.ChatColor.AQUA);
        secondPart.setColor(net.md_5.bungee.api.ChatColor.GOLD);

        ClickEvent.Action runCommand = ClickEvent.Action.RUN_COMMAND;
        HoverEvent.Action showText = HoverEvent.Action.SHOW_TEXT;

        firstPart.setClickEvent(new ClickEvent(runCommand, "/accept " + sender));
        firstPart.setHoverEvent(new HoverEvent(showText, new BaseComponent[] { new TextComponent(ChatColor.GREEN + "Click here to accept") }));

        commandPart.setClickEvent(new ClickEvent(runCommand, "/accept " + sender));
        commandPart.setHoverEvent(new HoverEvent(showText, new BaseComponent[] { new TextComponent(ChatColor.GREEN + "Click here to accept") }));

        secondPart.setClickEvent(new ClickEvent(runCommand, "/accept " + sender));
        secondPart.setHoverEvent(new HoverEvent(showText, new BaseComponent[] { new TextComponent(ChatColor.GREEN + "Click here to accept") }));

        return new TextComponent[] { firstPart, commandPart, secondPart };
    }

}