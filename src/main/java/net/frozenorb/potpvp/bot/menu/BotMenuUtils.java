package net.frozenorb.potpvp.bot.menu;

import net.frozenorb.potpvp.PotPvPSI;
import net.frozenorb.potpvp.bot.config.BotConfig;
import net.frozenorb.potpvp.bot.config.BotProfile;
import net.frozenorb.potpvp.bot.config.ParameterRange;
import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;

final class BotMenuUtils {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");
    private static final int MAX_BOT_ID_LENGTH = 32;

    private BotMenuUtils() {
    }

    static void startAddConversation(Player player) {
        ConversationFactory factory = new ConversationFactory(PotPvPSI.getInstance()).withFirstPrompt(new StringPrompt() {

            @Override
            public String getPromptText(ConversationContext context) {
                return ChatColor.YELLOW + "Enter the bot id now, or type " + ChatColor.RED + "cancel" + ChatColor.YELLOW + ".";
            }

            @Override
            public Prompt acceptInput(ConversationContext ctx, String input) {
                String botId = input == null ? "" : input.trim();

                if (botId.equalsIgnoreCase("cancel")) {
                    ctx.getForWhom().sendRawMessage(ChatColor.RED + "Bot creation cancelled.");
                    reopenBotList(player);
                    return Prompt.END_OF_CONVERSATION;
                }

                if (!isValidBotId(botId)) {
                    ctx.getForWhom().sendRawMessage(ChatColor.RED + "Bot id must be 1-" + MAX_BOT_ID_LENGTH + " characters and cannot contain spaces.");
                    return this;
                }

                BotConfig config = PotPvPSI.getInstance().getBotConfig();
                if (!config.addBot(botId)) {
                    ctx.getForWhom().sendRawMessage(ChatColor.RED + "A bot with that id already exists.");
                    return this;
                }

                ctx.getForWhom().sendRawMessage(ChatColor.GREEN + "Added bot " + ChatColor.AQUA + botId + ChatColor.GREEN + ".");
                if (player.isOnline()) {
                    new BotProfileMenu(botId).openMenu(player);
                }
                return Prompt.END_OF_CONVERSATION;
            }

        }).withLocalEcho(false);

        player.closeInventory();
        player.beginConversation(factory.buildConversation(player));
    }

    static void startRangeConversation(Player player, String botId, String parameter, boolean editMin) {
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
                    reopenBotProfile(player, botId);
                    return Prompt.END_OF_CONVERSATION;
                }

                BotConfig config = PotPvPSI.getInstance().getBotConfig();
                BotProfile profile = config.getBot(botId);
                if (profile == null) {
                    ctx.getForWhom().sendRawMessage(ChatColor.RED + "That bot no longer exists.");
                    reopenBotList(player);
                    return Prompt.END_OF_CONVERSATION;
                }

                ParameterRange range = profile.getParameters().get(parameter);
                if (range == null) {
                    ctx.getForWhom().sendRawMessage(ChatColor.RED + "That parameter no longer exists.");
                    reopenBotProfile(player, botId);
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

                ctx.getForWhom().sendRawMessage(ChatColor.GREEN + "Set " + ChatColor.AQUA + profile.getId() + ChatColor.GREEN + " " + parameter + " " + (editMin ? "min" : "max") + " to " + format(range, editMin ? range.getMin() : range.getMax()) + ".");
                reopenBotProfile(player, profile.getId());
                return Prompt.END_OF_CONVERSATION;
            }

        }).withLocalEcho(false);

        player.closeInventory();
        player.beginConversation(factory.buildConversation(player));
    }

    static String format(ParameterRange range, double value) {
        return range.isInteger() ? String.valueOf((int) Math.round(value)) : DECIMAL_FORMAT.format(value);
    }

    static void reopenBotList(Player player) {
        if (player.isOnline()) {
            new BotConfigMenu().openMenu(player);
        }
    }

    private static void reopenBotProfile(Player player, String botId) {
        if (player.isOnline()) {
            new BotProfileMenu(botId).openMenu(player);
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

    private static boolean isValidBotId(String botId) {
        return botId != null && !botId.isEmpty() && botId.length() <= MAX_BOT_ID_LENGTH && !containsWhitespace(botId);
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
