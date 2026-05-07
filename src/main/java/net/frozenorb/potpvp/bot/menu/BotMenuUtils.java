package net.frozenorb.potpvp.bot.menu;

import net.frozenorb.potpvp.PotPvPSI;
import net.frozenorb.potpvp.bot.config.BotConfig;
import net.frozenorb.potpvp.bot.config.BotProfile;
import net.frozenorb.potpvp.bot.config.ParameterRange;
import net.frozenorb.potpvp.bot.menu.extra.ExtraProfileMenu;
import net.frozenorb.potpvp.bot.menu.extra.ExtraProfilesMenu;
import net.frozenorb.potpvp.bot.menu.profile.BotProfileMenu;
import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;

public final class BotMenuUtils {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");
    private static final int MAX_NAME_LENGTH = 32;

    private BotMenuUtils() {
    }

    public static void startAddConversation(Player player) {
        ConversationFactory factory = new ConversationFactory(PotPvPSI.getInstance()).withFirstPrompt(new StringPrompt() {

            @Override
            public String getPromptText(ConversationContext context) {
                return ChatColor.YELLOW + "Enter the bot name now, or type " + ChatColor.RED + "cancel" + ChatColor.YELLOW + ".";
            }

            @Override
            public Prompt acceptInput(ConversationContext ctx, String input) {
                String botName = input == null ? "" : input.trim();

                if (botName.equalsIgnoreCase("cancel")) {
                    ctx.getForWhom().sendRawMessage(ChatColor.RED + "Bot creation cancelled.");
                    reopenBotList(player);
                    return Prompt.END_OF_CONVERSATION;
                }

                if (!isValidName(botName)) {
                    ctx.getForWhom().sendRawMessage(ChatColor.RED + "Bot name must be 1-" + MAX_NAME_LENGTH + " characters and cannot contain spaces.");
                    return this;
                }

                BotConfig config = PotPvPSI.getInstance().getBotConfig();
                if (!config.addBot(botName)) {
                    ctx.getForWhom().sendRawMessage(ChatColor.RED + "A bot with that name already exists.");
                    return this;
                }

                ctx.getForWhom().sendRawMessage(ChatColor.GREEN + "Added bot " + ChatColor.AQUA + botName + ChatColor.GREEN + ".");
                if (player.isOnline()) {
                    new BotProfileMenu(botName).openMenu(player);
                }
                return Prompt.END_OF_CONVERSATION;
            }

        }).withLocalEcho(false);

        player.closeInventory();
        player.beginConversation(factory.buildConversation(player));
    }

    public static void startRangeConversation(Player player, String botName, String parameter, boolean editMin) {
        ConversationFactory factory = new ConversationFactory(PotPvPSI.getInstance()).withFirstPrompt(new StringPrompt() {

            @Override
            public String getPromptText(ConversationContext context) {
                String bound = editMin ? "min" : "max";
                return ChatColor.YELLOW + "Enter " + ChatColor.AQUA + parameter + ChatColor.YELLOW + " " + bound + " value, or type " + ChatColor.RED + "cancel" + ChatColor.YELLOW + ".";
            }

            @Override
            public Prompt acceptInput(ConversationContext ctx, String input) {
                String rawValue = input == null ? "" : input.trim();

                if (rawValue.equalsIgnoreCase("cancel")) {
                    ctx.getForWhom().sendRawMessage(ChatColor.RED + "Edit cancelled.");
                    reopenBotProfile(player, botName);
                    return Prompt.END_OF_CONVERSATION;
                }

                BotConfig config = PotPvPSI.getInstance().getBotConfig();
                BotProfile profile = config.getBot(botName);
                if (profile == null) {
                    ctx.getForWhom().sendRawMessage(ChatColor.RED + "That bot no longer exists.");
                    reopenBotList(player);
                    return Prompt.END_OF_CONVERSATION;
                }

                ParameterRange range = profile.getParameters().get(parameter);
                if (range == null) {
                    ctx.getForWhom().sendRawMessage(ChatColor.RED + "That parameter no longer exists.");
                    reopenBotProfile(player, botName);
                    return Prompt.END_OF_CONVERSATION;
                }

                Double parsedValue = parseRangeValue(rawValue, range.isInteger());
                if (parsedValue == null) {
                    ctx.getForWhom().sendRawMessage(ChatColor.RED + "Please enter a valid " + (range.isInteger() ? "integer." : "number."));
                    return this;
                }

                if (parsedValue < 0D) {
                    ctx.getForWhom().sendRawMessage(ChatColor.RED + "Value cannot be negative.");
                    return this;
                }

                setRangeBound(range, parsedValue, editMin);
                config.save();

                ctx.getForWhom().sendRawMessage(ChatColor.GREEN + "Set " + ChatColor.AQUA + profile.getName() + ChatColor.GREEN + " " + parameter + " " + (editMin ? "min" : "max") + " to " + format(range, editMin ? range.getMin() : range.getMax()) + ".");
                reopenBotProfile(player, profile.getName());
                return Prompt.END_OF_CONVERSATION;
            }

        }).withLocalEcho(false);

        player.closeInventory();
        player.beginConversation(factory.buildConversation(player));
    }

    public static void startAddExtraProfileConversation(Player player) {
        ConversationFactory factory = new ConversationFactory(PotPvPSI.getInstance()).withFirstPrompt(new StringPrompt() {

            @Override
            public String getPromptText(ConversationContext context) {
                return ChatColor.YELLOW + "Enter the extra profile name now, or type " + ChatColor.RED + "cancel" + ChatColor.YELLOW + ".";
            }

            @Override
            public Prompt acceptInput(ConversationContext ctx, String input) {
                String profileName = input == null ? "" : input.trim();

                if (profileName.equalsIgnoreCase("cancel")) {
                    ctx.getForWhom().sendRawMessage(ChatColor.RED + "Extra profile creation cancelled.");
                    reopenExtraProfiles(player);
                    return Prompt.END_OF_CONVERSATION;
                }

                if (!isValidName(profileName)) {
                    ctx.getForWhom().sendRawMessage(ChatColor.RED + "Extra profile name must be 1-" + MAX_NAME_LENGTH + " characters and cannot contain spaces.");
                    return this;
                }

                BotConfig config = PotPvPSI.getInstance().getBotConfig();
                if (!config.addExtraProfile(profileName)) {
                    ctx.getForWhom().sendRawMessage(ChatColor.RED + "An extra profile with that name already exists.");
                    return this;
                }

                ctx.getForWhom().sendRawMessage(ChatColor.GREEN + "Added extra profile " + ChatColor.AQUA + profileName + ChatColor.GREEN + ".");
                reopenExtraProfile(player, profileName);
                return Prompt.END_OF_CONVERSATION;
            }

        }).withLocalEcho(false);

        player.closeInventory();
        player.beginConversation(factory.buildConversation(player));
    }

    public static void startExtraProfileValueConversation(Player player, String extraProfileName, String parameter) {
        ConversationFactory factory = new ConversationFactory(PotPvPSI.getInstance()).withFirstPrompt(new StringPrompt() {

            @Override
            public String getPromptText(ConversationContext context) {
                return ChatColor.YELLOW + "Enter " + ChatColor.AQUA + parameter + ChatColor.YELLOW + " extra value, or type " + ChatColor.RED + "cancel" + ChatColor.YELLOW + ".";
            }

            @Override
            public Prompt acceptInput(ConversationContext ctx, String input) {
                String rawValue = input == null ? "" : input.trim();

                if (rawValue.equalsIgnoreCase("cancel")) {
                    ctx.getForWhom().sendRawMessage(ChatColor.RED + "Edit cancelled.");
                    reopenExtraProfile(player, extraProfileName);
                    return Prompt.END_OF_CONVERSATION;
                }

                BotConfig config = PotPvPSI.getInstance().getBotConfig();
                if (config.getExtraProfile(extraProfileName) == null) {
                    ctx.getForWhom().sendRawMessage(ChatColor.RED + "That extra profile no longer exists.");
                    reopenExtraProfiles(player);
                    return Prompt.END_OF_CONVERSATION;
                }

                Double parsedValue = parseRangeValue(rawValue, config.isIntegerParameter(parameter));
                if (parsedValue == null) {
                    ctx.getForWhom().sendRawMessage(ChatColor.RED + "Please enter a valid " + (config.isIntegerParameter(parameter) ? "integer." : "number."));
                    return this;
                }

                if (!config.setExtraProfileValue(extraProfileName, parameter, parsedValue)) {
                    ctx.getForWhom().sendRawMessage(ChatColor.RED + "Could not update that extra profile.");
                    reopenExtraProfile(player, extraProfileName);
                    return Prompt.END_OF_CONVERSATION;
                }

                ctx.getForWhom().sendRawMessage(ChatColor.GREEN + "Set " + ChatColor.AQUA + extraProfileName + ChatColor.GREEN + " " + parameter + " to " + format(config.isIntegerParameter(parameter), parsedValue) + ".");
                reopenExtraProfile(player, extraProfileName);
                return Prompt.END_OF_CONVERSATION;
            }

        }).withLocalEcho(false);

        player.closeInventory();
        player.beginConversation(factory.buildConversation(player));
    }

    public static String format(ParameterRange range, double value) {
        return format(range.isInteger(), value);
    }

    public static String format(boolean integer, double value) {
        return integer ? String.valueOf((int) Math.round(value)) : DECIMAL_FORMAT.format(value);
    }

    public static void reopenBotList(Player player) {
        if (player.isOnline()) {
            new BotConfigMenu().openMenu(player);
        }
    }

    public static void reopenExtraProfiles(Player player) {
        if (player.isOnline()) {
            new ExtraProfilesMenu().openMenu(player);
        }
    }

    private static void reopenBotProfile(Player player, String botName) {
        if (player.isOnline()) {
            new BotProfileMenu(botName).openMenu(player);
        }
    }

    private static void reopenExtraProfile(Player player, String extraProfileName) {
        if (player.isOnline()) {
            new ExtraProfileMenu(extraProfileName).openMenu(player);
        }
    }

    private static Double parseRangeValue(String rawValue, boolean integer) {
        try {
            if (integer) {
                return (double) Integer.parseInt(rawValue);
            }

            return Double.parseDouble(rawValue);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static boolean isValidName(String name) {
        return name != null && !name.isEmpty() && name.length() <= MAX_NAME_LENGTH && !containsWhitespace(name);
    }

    private static boolean containsWhitespace(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (Character.isWhitespace(value.charAt(i))) {
                return true;
            }
        }

        return false;
    }

    private static void setRangeBound(ParameterRange range, double value, boolean min) {
        if (min) {
            range.setMin(value);
            if (range.getMin() > range.getMax()) {
                range.setMax(range.getMin());
            }
        } else {
            range.setMax(value);
            if (range.getMax() < range.getMin()) {
                range.setMin(range.getMax());
            }
        }

        range.normalize();
    }

}
