package net.frozenorb.potpvp.kittype.menu.manage;

import com.google.common.collect.ImmutableList;

import net.frozenorb.potpvp.PotPvPSI;
import net.frozenorb.potpvp.kittype.KitType;
import net.frozenorb.potpvp.util.menu.MenuBackButton;
import net.frozenorb.qlib.menu.Button;
import net.frozenorb.qlib.menu.Menu;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class MisplaceSettingsMenu extends Menu {

    private final KitType type;

    MisplaceSettingsMenu(KitType type) {
        super("Misplace Settings");
        this.type = type;
        setUpdateAfterClick(false);
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        buttons.put(0, new SettingButton(Setting.ATTACK_WINDOW));
        buttons.put(1, new SettingButton(Setting.DAMAGE_WINDOW));
        buttons.put(2, new SettingButton(Setting.DELAY));
        buttons.put(3, new SettingButton(Setting.STEP));
        buttons.put(8, new MenuBackButton(p -> new ManageKitTypeMenu(type).openMenu(p)));

        return buttons;
    }

    private final class SettingButton extends Button {

        private final Setting setting;

        private SettingButton(Setting setting) {
            this.setting = setting;
        }

        @Override
        public String getName(Player player) {
            return ChatColor.GOLD + "Edit " + setting.displayName;
        }

        @Override
        public List<String> getDescription(Player player) {
            return ImmutableList.of(
                ChatColor.YELLOW + "Current: " + ChatColor.WHITE + setting.format(type),
                ChatColor.GRAY + setting.description,
                "",
                ChatColor.GREEN.toString() + ChatColor.BOLD + "Click and enter a value"
            );
        }

        @Override
        public Material getMaterial(Player player) {
            return setting.material;
        }

        @Override
        public void clicked(Player player, int slot, ClickType clickType) {
            ConversationFactory factory = new ConversationFactory(PotPvPSI.getInstance())
                .withFirstPrompt(new StringPrompt() {
                    @Override
                    public String getPromptText(ConversationContext context) {
                        return ChatColor.YELLOW + "Enter a new " + setting.displayName
                            + ChatColor.YELLOW + " value " + ChatColor.GRAY + "(" + setting.inputHint
                            + ", or type 'cancel'):";
                    }

                    @Override
                    public Prompt acceptInput(ConversationContext context, String input) {
                        if (input.equalsIgnoreCase("cancel")) {
                            context.getForWhom().sendRawMessage(ChatColor.YELLOW + "Misplace edit cancelled.");
                            new MisplaceSettingsMenu(type).openMenu(player);
                            return Prompt.END_OF_CONVERSATION;
                        }

                        String error = setting.apply(type, input);
                        if (error != null) {
                            context.getForWhom().sendRawMessage(ChatColor.RED + error);
                            return this;
                        }

                        type.saveAsync();
                        context.getForWhom().sendRawMessage(
                            ChatColor.GREEN + "Set " + setting.displayName + " to " + setting.format(type) + "."
                        );
                        new MisplaceSettingsMenu(type).openMenu(player);
                        return Prompt.END_OF_CONVERSATION;
                    }
                })
                .withLocalEcho(false);

            player.closeInventory();
            player.beginConversation(factory.buildConversation(player));
        }
    }

    private enum Setting {
        ATTACK_WINDOW(
            "Attack Window",
            "How long a successful hit keeps Misplace active.",
            "whole milliseconds, 0 or greater",
            Material.DIAMOND_SWORD
        ),
        DAMAGE_WINDOW(
            "Damage Window",
            "How long taking damage blocks Misplace.",
            "whole milliseconds, 0 or greater",
            Material.IRON_CHESTPLATE
        ),
        DELAY(
            "Delay",
            "Maximum delay in ticks; 1.0 equals 50ms.",
            "number, 0 or greater",
            Material.REDSTONE
        ),
        STEP(
            "Step",
            "Delay change per movement packet; 0.2 equals 10ms.",
            "number greater than 0",
            Material.SUGAR
        );

        private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.###");

        private final String displayName;
        private final String description;
        private final String inputHint;
        private final Material material;

        Setting(String displayName, String description, String inputHint, Material material) {
            this.displayName = displayName;
            this.description = description;
            this.inputHint = inputHint;
            this.material = material;
        }

        private String format(KitType type) {
            switch (this) {
                case ATTACK_WINDOW:
                    return type.getMisplaceAttackWindowMs() + "ms";
                case DAMAGE_WINDOW:
                    return type.getMisplaceDamageWindowMs() + "ms";
                case DELAY:
                    return DECIMAL_FORMAT.format(type.getMisplaceDelay()) + " ("
                        + DECIMAL_FORMAT.format(type.getMisplaceDelay() * 50.0D) + "ms)";
                case STEP:
                    return DECIMAL_FORMAT.format(type.getMisplaceStep()) + " ("
                        + DECIMAL_FORMAT.format(type.getMisplaceStep() * 50.0D) + "ms)";
                default:
                    throw new IllegalStateException("Unknown Misplace setting " + this);
            }
        }

        private String apply(KitType type, String input) {
            try {
                if (this == ATTACK_WINDOW || this == DAMAGE_WINDOW) {
                    long value = Long.parseLong(input);
                    if (value < 0L) {
                        return "Value must be 0 or greater.";
                    }

                    if (this == ATTACK_WINDOW) {
                        type.setMisplaceAttackWindowMs(value);
                    } else {
                        type.setMisplaceDamageWindowMs(value);
                    }
                    return null;
                }

                double value = Double.parseDouble(input);
                if (Double.isNaN(value) || Double.isInfinite(value)) {
                    return "Value must be a finite number.";
                }
                if (this == DELAY && value < 0.0D) {
                    return "Delay must be 0 or greater.";
                }
                if (this == STEP && value <= 0.0D) {
                    return "Step must be greater than 0.";
                }

                if (this == DELAY) {
                    type.setMisplaceDelay(value);
                } else {
                    type.setMisplaceStep(value);
                }
                return null;
            } catch (NumberFormatException ex) {
                return "Invalid number. Expected " + inputHint + ".";
            }
        }
    }
}
