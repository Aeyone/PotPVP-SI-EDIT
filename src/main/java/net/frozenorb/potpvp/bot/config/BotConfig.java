package net.frozenorb.potpvp.bot.config;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import net.frozenorb.potpvp.PotPvPSI;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.libs.com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class BotConfig {

    private static final String CONFIG_FILE_NAME = "botConfig.json";
    private static final Type BOT_PROFILES_TYPE = new TypeToken<LinkedHashMap<String, BotProfile>>() {}.getType();
    private static final List<String> DEFAULT_BOT_NAMES = Arrays.asList(
        "DefeatBoy",
        "idiol",
        "GANGMEMBERHOW2",
        "ZIBLACKINGGG",
        "Zeynah",
        "Glory",
        "Verzide",
        "BCZ",
        "Marcel",
        "Stimpay",
        "Dreamer_420",
        "Tryhard",
        "Zefew",
        "DANTEH",
        "Zi_Min",
        "Xetha",
        "Jewdah",
        "Airbus",
        "Fearless_420",
        "DrummerReviews",
        "PotFast",
        "clare",
        "itsjhalt",
        "Vious",
        "Kevstah",
        "Latenci",
        "MeeZoid",
        "DaGoldBrick",
        "Apexay",
        "Tylarzz",
        "Topu",
        "iSparkton",
        "Reboting",
        "ImHacking",
        "Miami",
        "Hydrize",
        "Demolishing"
    );
    private static final List<ParameterDefinition> DEFAULT_PARAMETERS = Arrays.asList(
        new ParameterDefinition("horizontalAimSpeed", "Horizontal Aim Speed", 0.4D, 0.6D, false),
        new ParameterDefinition("verticalAimSpeed", "Vertical Aim Speed", 0.4D, 0.6D, false),
        new ParameterDefinition("horizontalAimAccuracy", "Horizontal Aim Accuracy", 0.4D, 0.6D, false),
        new ParameterDefinition("verticalAimAccuracy", "Vertical Aim Accuracy", 0.4D, 0.6D, false),
        new ParameterDefinition("horizontalErraticness", "Horizontal Erraticness", 0.2D, 0.4D, false),
        new ParameterDefinition("verticalErraticness", "Vertical Erraticness", 0.2D, 0.4D, false),
        new ParameterDefinition("averageCps", "Average CPS", 6.0D, 10.0D, false),
        new ParameterDefinition("sprintResetAccuracy", "Sprint Reset Accuracy", 0.4D, 0.6D, false),
        new ParameterDefinition("hitSelectAccuracy", "Hit Select Accuracy(No Effect)", 0.0D, 0.0D, false),
        new ParameterDefinition("reach", "Reach", 2.5D, 3.0D, false),
        new ParameterDefinition("jumpProbability", "Jump Probability", 0.05D, 0.2D, false),
        new ParameterDefinition("wtapProbability", "W-Tap Probability", 0.4D, 1.0D, false),
        new ParameterDefinition("latency", "Latency", 20D, 20D, true),
        new ParameterDefinition("targetSearchRange", "Target Search Range", 256D, 256D, true),
        new ParameterDefinition("pearlCooldown", "Pearl Cooldown", 24D, 24D, true)
    );

    private final Map<String, BotProfile> profiles = new LinkedHashMap<>();

    private BotConfig(Map<String, BotProfile> profiles) {
        if (profiles != null) {
            this.profiles.putAll(profiles);
        }
        ensureDefaults();
    }

    public static BotConfig load() {
        File file = getConfigFile();
        Map<String, BotProfile> profiles = null;

        if (file.exists()) {
            try (Reader reader = Files.newReader(file, Charsets.UTF_8)) {
                profiles = PotPvPSI.getGson().fromJson(reader, BOT_PROFILES_TYPE);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        BotConfig config = new BotConfig(profiles);
        config.save();
        return config;
    }

    public void save() {
        File file = getConfigFile();
        File parent = file.getParentFile();

        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try {
            Files.write(PotPvPSI.getGson().toJson(profiles, BOT_PROFILES_TYPE), file, Charsets.UTF_8);
        } catch (Exception ex) {
            Bukkit.getLogger().warning("[BotConfig] Failed to save bot config.");
            ex.printStackTrace();
        }
    }

    public List<String> getBotIds() {
        ensureDefaults();
        return new ArrayList<>(profiles.keySet());
    }

    public BotProfile getBot(String id) {
        ensureDefaults();
        String storedId = getStoredId(id);
        return storedId == null ? null : profiles.get(storedId);
    }

    public boolean addBot(String id) {
        ensureDefaults();

        String normalizedId = normalizeBotId(id);
        if (normalizedId == null || getStoredId(normalizedId) != null) {
            return false;
        }

        profiles.put(normalizedId, BotProfile.createDefault(normalizedId));
        save();
        return true;
    }

    public boolean removeBot(String id) {
        ensureDefaults();

        String storedId = getStoredId(id);
        if (storedId == null) {
            return false;
        }

        profiles.remove(storedId);
        save();
        return true;
    }

    private void ensureDefaults() {
        if (profiles.isEmpty()) {
            for (String defaultName : DEFAULT_BOT_NAMES) {
                profiles.put(defaultName, BotProfile.createDefault(defaultName));
            }
        }

        Map<String, BotProfile> loadedProfiles = new LinkedHashMap<>(profiles);
        profiles.clear();

        for (Map.Entry<String, BotProfile> entry : loadedProfiles.entrySet()) {
            String id = normalizeBotId(entry.getKey());
            BotProfile profile = entry.getValue();

            if (id == null || getStoredId(id) != null) {
                continue;
            }

            if (profile == null) {
                profile = BotProfile.createDefault(id);
            } else {
                profile.ensureDefaults(id);
            }

            profiles.put(id, profile);
        }
    }

    private String getStoredId(String id) {
        String normalizedId = normalizeBotId(id);
        if (normalizedId == null) {
            return null;
        }

        if (profiles.containsKey(normalizedId)) {
            return normalizedId;
        }

        for (String existingId : profiles.keySet()) {
            if (existingId.equalsIgnoreCase(normalizedId)) {
                return existingId;
            }
        }

        return null;
    }

    static Map<String, ParameterRange> createParameterRanges(Map<String, ParameterRange> loadedParameters) {
        Map<String, ParameterRange> normalizedParameters = new LinkedHashMap<>();
        Map<String, ParameterRange> source = loadedParameters == null ? new LinkedHashMap<>() : loadedParameters;

        for (ParameterDefinition definition : DEFAULT_PARAMETERS) {
            ParameterRange range = source.get(definition.id);
            if (range == null) {
                range = new ParameterRange(definition.showName, definition.min, definition.max, definition.integer);
            } else {
                range.setShowName(definition.showName);
                range.setInteger(definition.integer);
            }

            range.normalize();
            normalizedParameters.put(definition.id, range);
        }

        return normalizedParameters;
    }

    static String normalizeBotId(String id) {
        if (id == null) {
            return null;
        }
        String normalizedId = id.trim();
        return normalizedId.isEmpty() ? null : normalizedId;
    }

    private static File getConfigFile() {
        return new File(PotPvPSI.getInstance().getDataFolder(), CONFIG_FILE_NAME);
    }

    private static final class ParameterDefinition {

        private final String id;
        private final String showName;
        private final double min;
        private final double max;
        private final boolean integer;

        private ParameterDefinition(String id, String showName, double min, double max, boolean integer) {
            this.id = id;
            this.showName = showName;
            this.min = min;
            this.max = max;
            this.integer = integer;
        }

    }

}
