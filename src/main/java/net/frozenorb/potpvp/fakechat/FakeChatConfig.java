package net.frozenorb.potpvp.fakechat;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class FakeChatConfig {

    private final List<String> prefixes;
    private final List<String> suffixes;
    private final List<String> titles;
    private final List<String> messages;
    private final List<String> lolSpamMessages;
    private final List<String> conversationStarters;
    private final List<String> followUps;
    private final List<String> responses;
    private final Random random;

    public FakeChatConfig() {
        this.random = new Random();
        this.prefixes = new ArrayList<>();
        this.suffixes = new ArrayList<>();
        this.titles = new ArrayList<>();
        this.messages = new ArrayList<>();
        this.lolSpamMessages = new ArrayList<>();
        this.conversationStarters = new ArrayList<>();
        this.followUps = new ArrayList<>();
        this.responses = new ArrayList<>();

        initializeDefaults();
    }

    private void initializeDefaults() {
        prefixes.addAll(Arrays.asList(
                "Gro", "Hap", "Brand", "Daddy", "Wo", "Lopez",
                "Alixi", "Nes", "Not", "Real", "OG", "MC", "mc", "its",
                "not", "M0d", "Ver", "ver", "kari", "jer", "Jay",
                "isnotur", "pala", "horninx", "palod", "tayki", "suspec", "PVP_",
                "pvp_", "its_", "kayzer_", "Sne", "down", "z", "Sp", "i",
                "Swerv", "N0PU", "yung", "In", "To", "Bar", "Itz", "Smooth",
                "LAGEN", "Benjamin", "lulu", "cocole", "Twit", "Felix", "master_",
                "cell", "xMark", "MIT", "Lead", "Crixo", "Gigan", "xOMG", "enchant", "Megas",
                "jayki", "Manif", "Legis", "need", "palsc", "Dremin", "Daddy"
        ));

        suffixes.addAll(Arrays.asList(
                "ner", "on", "sense", "ced", "zar", "kc", "sak",
                "ock", "pol", "229", "101", "_420", "420", "69", "1192",
                "0012", "uned", "ed", "shed", "shie", "_", "__", "PvP",
                "_PVP", "_pvp", "_mc", "Latino", "0_o", "Luvs", "00",
                "Levz_", "mine", "Alee", "lix", "foo", "low", "Tnay",
                "HD", "row", "libra", "in", "P0T", "urmom", "hh",
                "rage", "_YT", "drake", "yaa", "blecraft", "BlaZ", "dzHD", "gof",
                "ops", "clues", "TheBed", "veon", "koen", "deadly"
        ));

        titles.addAll(Arrays.asList(
                ChatColor.GRAY + "[" + ChatColor.DARK_BLUE + "Developer" +ChatColor.GRAY + "] " + ChatColor.DARK_BLUE,
                ChatColor.GRAY + "[" + ChatColor.DARK_GREEN + "Mod" + ChatColor.GRAY + "] " + ChatColor.DARK_GREEN,
                ChatColor.GRAY + "[" + ChatColor.DARK_PURPLE + "Epic" + ChatColor.GRAY + "] " + ChatColor.DARK_PURPLE,
                ChatColor.GRAY + "[" + ChatColor.LIGHT_PURPLE + "Media" + ChatColor.GRAY + "] " + ChatColor.LIGHT_PURPLE,
                ChatColor.GRAY + "[" + ChatColor.GOLD + "Legend" + ChatColor.GRAY + "] " + ChatColor.GOLD,
                ChatColor.GRAY + "[" + ChatColor.GREEN + "Sponsor" + ChatColor.GRAY + "] " + ChatColor.GREEN,
                ChatColor.GRAY + "[" + ChatColor.AQUA + "Famous" + ChatColor.GRAY + "] " + ChatColor.AQUA,
                ChatColor.GRAY + "[" + ChatColor.RED + "Admin" + ChatColor.GRAY + "] " + ChatColor.RED,
                ""
        ));

        lolSpamMessages.addAll(Arrays.asList(
                "LOOOOL", "LMFAO", "WTF", "LLLLL", "oh my", "wtf",
                "Hahaha", "LL", "wTf", "LOOLOLOOLOLOLOLLOOLO", "o",
                "RIPP", "GG", "jesus", "LMA", "WTF LOL",
                "NO WAY", "XDD", "AHAHAHA", "LOOOOOOOO", "LMFAOOOOOOOOOOOOO",
                "L0L", "WTH???? HEY", "what the fuck", "wat t", "no way..", "bro LOL",
                "OMG", "wow", "ROFL", "INSANE", "CRAZY"
        ));

        messages.addAll(Arrays.asList(
                "that wtap", "q ranked axe", "nodebuff", "gf", "L", "Hahaha", "LL",
                "1v1 me bro", "WTF HAX", "this server lags", "queue ranked ndf",
                "Buena pelea", "No lo puedo creer, otra vez!", "Gané, eres muy fácil!",
                "a veges me trebo verdad", "ranked axe guys", "OK", "ok", "Ok",
                "ping fools win", "staff tp", "camping?", "jajajaja", "xd", "bro",
                "wat", "PEARL DIDNT WORK", "whyd u quickdropped :(", ":D", "kohi.lag",
                "calm down kiddo", "T_T", "any1 have skype", ".",
                ";v siempre te veo comentando en bidiozz",
                "do /msg me for HUGE party fights!", "duel?", "haha damn", "xd?",
                "any1 ranked archer", "wut", "1v1 ME IF UR GOOD",
                "i fuckking hate my life", "RAPE", "ass", "???",
                "youre so annoying bro", "logitech g600 sucks", "sure thingf",
                "---------------------- /msg " + generateRandomNickname() + " FOR HUGE TEAM FIGHTS -------------------",
                "HA", "gl", "does anyone know why my minecraft keeps crashing with shaders",
                "u smell like dead nans", "UNBANNED YES!!!!!!", "gggg",
                "this is insane", "need a better mouse", "any1 streaming?",
                "just got comboed rip", "looking for a clan to join", "1v1 for practice?",
                "fav kit for ranked?", "the lag is real", "gonna hit diamond today",
                "any tips 4 jitter clicking?", "got banned unfairly", "diamond sword or axe?",
                "need a teammate for 2v2 ranked", "how do i deal with bow spammers?",
                "this server's anticheat is SHIT", "may i get a answer from staff",
                "you are the most sweaty person", generateRandomNickname() + " this isnt badlion",
                "I didnt have it recorded yet", "Duel me warming up!", "wtf;",
                "stop ch eat plz", "this server is so retarded", "Im here",
                "shmoked", "3v2", "POT LAH", "tphacker", "wtg", "BYE CHEATer", "what do you expect from him",
                "someone 1500+elo duel me", "asd", "Leeroy__ giochi sul fazioni di qua?", generateRandomNickname() + " inv",
                "nah", "gf dude", "1 more?", "fd", "go", "ez tryhard", "msg me for redrover :P -=-=-=-=-=-=-=-=-=-=-=-=-=",
                "Itz_Recording i dropped my sword xd", "i hate having like 5ms", "SDFS", "round 2",
                "./team invite (me)", "jesus christ these nerds are wet", ": > )", "cherche team pvp FR et forte plese",
                "LINING", "LFT", "RECRUTE TEAM FR ME " + generateRandomNickname(), "fail", "i ca", "idem", "can we spectate on this", ";-;",
                "eZ 2v3", "i whill a good player 1v1", "WTF WHY SO LAGGY", "cry", "YA", "buruj", "stop hack pd",
                "ouuuuuuh", "GG!", "ping", "Qui Cherche Match De Team [FR]", "NO ARMOR", "7DUEL", "tt", "gg? fuck 2v1",
                "it was a joke.", "unban " + generateRandomNickname(), "Recording :d", "inviteeee", "brb b", "brb", "no shutup", "too laggy",
                "ads", "sorry", "gApple next", "squid dick", "some one duel me (warming up kinda)", "#" + generateRandomNickname(),
                "ahh fck off", "<33", "RAIDABLE!!!!!!!!", "Raidable lol", "MAS VALE", "dale?", "bro t", "debuff ranked",
                "ST&AFF", "g g", "frr", "send me playlist and Ill play it", "yo", "er", "yes much lag",
                "go laggers go!!", "the", "WTFS WRONG with my mouse i fucked my sensativityy", "fire me nertds",
                "Recruiting For Hydra/Minesides! /m me if interested", "si", "?", "omfffg", "r ohhhh", "Msg me for team invites ;>",
                "west wall", "ez bitch]", "k wea", "7duel " + generateRandomNickname(), "t", "wow ffs my map 16 elo is 795..", "lets go again",
                "minecraft is all ping", "Axe unranked", "and skill", "they just straightline", "im asian", "thought i had you",
                "puto lag ;v", "fuckboy", "k", "haxc", "no bow?", "u died and had reach", "5g", "ANYONE WANT TO HELP ME? PM ME", "+",
                "lookup in chat", "he did but he was telling me to make team", "AHHHH", "WICKED KB", "re invite", "Staff msg me",
                "MSG ME TO JOIN A BIG TEAM", "twitch.tv/" + generateRandomNickname(), "who join my team im recording",
                "again", "im better on 1.7 whatever", "lol rip", "J'y suis c bon", "I didnt heal", "join my team", "zs",
                "Mdrr y'a personne", "UnRanked gApple", "ez ban", "trigger much", "fucking beaner internet", "omg fucking potlag",
                "how?", "how", "HOW", "you just crit spammed and straightlined lel", "i have shitty mouse pad", "lolwtf", "jesus christ",
                "wow this pussy", "1v1 anybody?", "faggot pussy", "This kid is scared", "que?", "give me team", "dame team",
                "u hit", "but ok", "ok rm", "i will not do anything", "IM CLEANING MY KEYboard", "I hit with a pearl",
                "i need teaaaaaaaaaaaaaaaaaaaaaaaaaaaaaam", "gogogo map13", "u guys suck", "some1 good duel me", "#qclife",
                "#qdlife*", "i debut", "you back/", "duell us map16", "Salut !", "I guess", "Uuh", "stop tryhard fdp", "Im not feeling great again",
                "so thats why", "EALLEEE", "t for fdp", "NO GG", "imvette?", "stop sweating", ";)", "u", "RANEKD GAPPLE !", "your shit ffs",
                "reallt", "a", "really?", "Gg", "#GUCCISUC", "att", "in bound", "#GUCCISUCKS", "#GUCCI", "vien " + generateRandomNickname(), "totally legit..",
                "ja wacht", "Mdrr", "Someone GOOD duel me!", "lol ping 0ms", "Anyone recruiting for new map 24", "MY PING", "STOP",
                "kidd", "TOXIC M%AN", "Ranked soup", "wft ;)", "ITS AN UNRANKED KIKOO", "HAHAHHAHA Troll :D", "SOMEONE GOOD WARM ME UP", "ily",
                "fucking lagger", ".r ok", "bo je bent authist", "FAIL HUNTING", "lel", "kill me", "byebye", "someone good duel me must be 1400+ elo", "^^",
                "^", "rec", "dem..", "I'm happy I'm happy", "screen?", "for idk how long lmao", "is your pc fixed yet?", "RANKCED ARCHER <------", "" +
                        "is there any fairplay?", "oups", "NICE HAX", "e_z", "goo 2pots", "whoops", "v happy", "why do u think im still playing?", "Thank you antiskid!",
                "IM hosting a red rover msg me Must knowhow to play <<< 4 slots left", "F", "GF", "invite in my team :D", "www.twitch.tv/ureazytheg0d",
                "qui veut joindre ma partie", "vanilla", "eze ping", "1 sec lemme get a drink boys", "pm me", "west wall", "huh?", "huh",
                "fisl de pute ntm", "whats a red rover lol?", "FUCKER", "tha fire res", "vs ?", "i didnt have any :(", "splashed my heal :(",
                "bye <#", "my speed", "._.", "No one wants to face godping you loser", "RAWR", "Quien un PvP???? 1vs1 si db!!!",
                "y potlag", "pande", "maps", "te dijo unos truquillos", "mande", "./anon", "SORRY ALLIO", "ADMIN PLEASE BAN UJ", "buenas",
                "wtf vanilla is bad", "pot troll", "just lagger", "nice trigger", "https://www.youtube.com/watch?v=7494AJBTQCc",
                "https://www.youtube.com/watch?v=CiFPuxmheY0 LIKE", "https://www.youtube.com/watch?v=7aBxewXTnxY"
                ));

        conversationStarters.addAll(Arrays.asList(
                "yo anyone down for 2v2?",
                "who wants to party?",
                "bro who just killed me",
                "anyone wanna team?",
                "yo wait",
                "hold up",
                "guys",
                "bro wtf",
                "anyone here from EU?",
                "whos tryna run it",
                "yo who was that",
                "that was weird",
                "bruh moment",
                "wait what happened",
                "did yall see that",
                "yo that combo tho",
                "ngl that was clean",
                "who wants smoke",
                "any1 wanna practice",
                "ok so basically",
                "hear me out",
                "yo chat",
                "anyone good here?",
                "need a duo partner",
                "whos online rn",
                "yo im bored",
                "anyone tryna 1v1 me",
                "bro im lagging so bad",
                "just downloaded new client",
                "testing new sens"
        ));

        followUps.addAll(Arrays.asList(
                "like fr",
                "no cap",
                "im serious",
                "deadass",
                "bruh",
                "smh",
                "lmao",
                "nvm",
                "wait nvm",
                "actually",
                "u know what i mean",
                "right?",
                "ikr",
                "facts",
                "ong",
                "swear",
                ".",
                "..",
                "???",
                "hello?",
                "anyone?",
                "mb",
                "my b",
                "sry",
                "lol",
                "xd",
                "idk tho",
                "just saying",
                "js",
                "tbh"
        ));

        responses.addAll(Arrays.asList(
                "yea",
                "ye",
                "yep",
                "yup",
                "nah",
                "no",
                "nope",
                "idk",
                "idc",
                "ok",
                "k",
                "kk",
                "sure",
                "bet",
                "aight",
                "ight",
                "true",
                "fr",
                "facts",
                "cap",
                "no cap",
                "lol",
                "lmao",
                "same",
                "gg",
                "nice",
                "damn",
                "bro what",
                "wdym",
                "huh",
                "?",
                "who asked",
                "ratio",
                "L",
                "W",
                "based",
                "cringe",
                "real",
                "not real",
                "valid",
                "im down",
                "lets go",
                "less goo",
                "say less",
                "word",
                "thats crazy",
                "oh",
                "oh ok",
                "makes sense",
                "understandable"
        ));
    }

    public String getRandomPrefix() {
        return prefixes.isEmpty() ? "Player" : prefixes.get(random.nextInt(prefixes.size()));
    }

    public String getRandomSuffix() {
        return suffixes.isEmpty() ? "123" : suffixes.get(random.nextInt(suffixes.size()));
    }

    private String generateRandomNickname() {
        return getRandomPrefix() + getRandomSuffix();
    }

    public String getRandomTitle() {
        if (titles.isEmpty()) return ChatColor.WHITE + "";

        if (random.nextInt(100) < 85) {
            return ChatColor.WHITE + "";
        }

        List<String> nonEmptyTitles = new ArrayList<>();
        for (String title : titles) {
            if (!title.isEmpty() && !title.equals(ChatColor.WHITE + "")) {
                nonEmptyTitles.add(title);
            }
        }

        if (nonEmptyTitles.isEmpty()) return ChatColor.WHITE + "";
        return nonEmptyTitles.get(random.nextInt(nonEmptyTitles.size()));
    }


    public String getRandomMessage() {
        return messages.isEmpty() ? "..." : messages.get(random.nextInt(messages.size()));
    }

    public String getLolSpamMessage() {
        return lolSpamMessages.isEmpty() ? "..." : lolSpamMessages.get(random.nextInt(lolSpamMessages.size()));
    }

    public String getRandomConversationStarter() {
        return conversationStarters.isEmpty() ? getRandomMessage() : conversationStarters.get(random.nextInt(conversationStarters.size()));
    }

    public String getRandomFollowUp() {
        return followUps.isEmpty() ? "..." : followUps.get(random.nextInt(followUps.size()));
    }

    public String getRandomResponse() {
        return responses.isEmpty() ? "ok" : responses.get(random.nextInt(responses.size()));
    }
}
