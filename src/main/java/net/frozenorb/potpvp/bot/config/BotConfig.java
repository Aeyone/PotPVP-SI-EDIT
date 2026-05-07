package net.frozenorb.potpvp.bot.config;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import net.frozenorb.potpvp.PotPvPSI;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.libs.com.google.gson.JsonElement;
import org.bukkit.craftbukkit.libs.com.google.gson.JsonObject;

import java.io.File;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public final class BotConfig {

    private static final String CONFIG_FILE_NAME = "botConfig.json";
    private static final String EXTRA_PROFILES_KEY = "extraProfiles";
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
        new ParameterDefinition("aimSpeed", "Aim Speed", 0.0D, 0.2D, false, "horizontalAimSpeed", "verticalAimSpeed"),
        new ParameterDefinition("aimAccuracy", "Aim Accuracy", 0.0D, 0.2D, false, "horizontalAimAccuracy", "verticalAimAccuracy"),
        new ParameterDefinition("erraticness", "Erraticness", 0.01D, 0.05D, false, "horizontalErraticness", "verticalErraticness"),
        new ParameterDefinition("averageCps", "Average CPS", 6.0D, 8.0D, false),
        new ParameterDefinition("sprintResetAccuracy", "Sprint Reset Accuracy", 0.0D, 0.2D, false),
        new ParameterDefinition("hitSelectAccuracy", "Hit Select Accuracy(No Effect)", 0.0D, 0.0D, false),
        new ParameterDefinition("reach", "Reach", 2.5D, 3.0D, false),
        new ParameterDefinition("jumpProbability", "Jump Probability", 0.00D, 0.05D, false),
        new ParameterDefinition("wtapProbability", "W-Tap Probability", 0.0D, 0.2D, false),
        new ParameterDefinition("latency", "Latency", 10D, 20D, true),
        new ParameterDefinition("targetSearchRange", "Target Search Range", 256D, 256D, true),
        new ParameterDefinition("pearlCooldown", "Pearl Cooldown", 24D, 24D, true)
    );
    private static final ParameterDefinition MAX_REACH_EXTRA_PARAMETER = new ParameterDefinition("maxReach", "Max Reach", 0D, 0D, false);

    private final Map<String, BotProfile> profiles = new LinkedHashMap<>();
    private final Map<String, Map<String, Double>> extraProfiles = new LinkedHashMap<>();

    private BotConfig(Map<String, BotProfile> profiles, Map<String, Map<String, Double>> extraProfiles) {
        if (profiles != null) {
            this.profiles.putAll(profiles);
        }
        if (extraProfiles != null) {
            this.extraProfiles.putAll(extraProfiles);
        }
        ensureDefaults();
    }

    public static BotConfig load() {
        File file = getConfigFile();
        Map<String, BotProfile> profiles = new LinkedHashMap<>();
        Map<String, Map<String, Double>> extraProfiles = new LinkedHashMap<>();

        if (file.exists()) {
            try (Reader reader = Files.newReader(file, Charsets.UTF_8)) {
                JsonObject root = PotPvPSI.getGson().fromJson(reader, JsonObject.class);

                if (root != null) {
                    for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
                        if (entry.getKey().equalsIgnoreCase(EXTRA_PROFILES_KEY)) {
                            loadExtraProfiles(entry.getValue(), extraProfiles);
                        } else {
                            profiles.put(entry.getKey(), PotPvPSI.getGson().fromJson(entry.getValue(), BotProfile.class));
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        BotConfig config = new BotConfig(profiles, extraProfiles);
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
            JsonObject root = new JsonObject();

            for (Map.Entry<String, BotProfile> entry : profiles.entrySet()) {
                root.add(entry.getKey(), PotPvPSI.getGson().toJsonTree(entry.getValue()));
            }

            JsonObject extraProfilesJson = new JsonObject();
            for (Map.Entry<String, Map<String, Double>> entry : extraProfiles.entrySet()) {
                extraProfilesJson.add(entry.getKey(), PotPvPSI.getGson().toJsonTree(entry.getValue()));
            }
            root.add(EXTRA_PROFILES_KEY, extraProfilesJson);

            Files.write(PotPvPSI.getGson().toJson(root), file, Charsets.UTF_8);
        } catch (Exception ex) {
            Bukkit.getLogger().warning("[BotConfig] Failed to save bot config.");
            ex.printStackTrace();
        }
    }

    public List<String> getBotNames() {
        ensureDefaults();
        return new ArrayList<>(profiles.keySet());
    }

    public BotProfile getBot(String name) {
        ensureDefaults();
        String storedName = getStoredBotName(name);
        return storedName == null ? null : profiles.get(storedName);
    }

    public boolean addBot(String name) {
        ensureDefaults();

        String normalizedName = normalizeBotName(name);
        if (normalizedName == null || normalizedName.equalsIgnoreCase(EXTRA_PROFILES_KEY) || getStoredBotName(normalizedName) != null) {
            return false;
        }

        profiles.put(normalizedName, BotProfile.createDefault(normalizedName));
        save();
        return true;
    }

    public boolean removeBot(String name) {
        ensureDefaults();

        String storedName = getStoredBotName(name);
        if (storedName == null) {
            return false;
        }

        profiles.remove(storedName);
        save();
        return true;
    }

    public List<String> getExtraProfileNames() {
        ensureDefaults();
        return new ArrayList<>(extraProfiles.keySet());
    }

    public Map<String, Double> getExtraProfile(String name) {
        ensureDefaults();
        String storedName = getStoredExtraProfileName(name);
        return storedName == null ? null : extraProfiles.get(storedName);
    }

    public boolean addExtraProfile(String name) {
        ensureDefaults();

        String normalizedName = normalizeName(name);
        if (normalizedName == null || getStoredExtraProfileName(normalizedName) != null) {
            return false;
        }

        extraProfiles.put(normalizedName, createExtraProfileValues(null));
        save();
        return true;
    }

    public boolean removeExtraProfile(String name) {
        ensureDefaults();

        String storedName = getStoredExtraProfileName(name);
        if (storedName == null) {
            return false;
        }

        extraProfiles.remove(storedName);
        for (BotProfile profile : profiles.values()) {
            profile.setExtraProfileEnabled(storedName, false);
        }

        save();
        return true;
    }

    public boolean setExtraProfileValue(String extraProfileName, String parameterId, double value) {
        ensureDefaults();

        String storedName = getStoredExtraProfileName(extraProfileName);
        ParameterDefinition definition = getAnyParameterDefinition(parameterId);
        if (storedName == null || definition == null) {
            return false;
        }

        extraProfiles.get(storedName).put(definition.id, normalizeExtraValue(definition, value));
        save();
        return true;
    }

    public boolean isExtraProfileEnabled(String botName, String extraProfileName) {
        BotProfile profile = getBot(botName);
        String storedExtraProfileName = getStoredExtraProfileName(extraProfileName);
        return profile != null && storedExtraProfileName != null && profile.isExtraProfileEnabled(storedExtraProfileName);
    }

    public boolean setExtraProfileEnabled(String botName, String extraProfileName, boolean enabled) {
        BotProfile profile = getBot(botName);
        String storedExtraProfileName = getStoredExtraProfileName(extraProfileName);
        if (profile == null || storedExtraProfileName == null) {
            return false;
        }

        profile.setExtraProfileEnabled(storedExtraProfileName, enabled);
        save();
        return true;
    }

    public List<String> getParameterIds() {
        List<String> ids = new ArrayList<>();
        for (ParameterDefinition definition : DEFAULT_PARAMETERS) {
            ids.add(definition.id);
        }
        return ids;
    }

    public List<String> getExtraProfileParameterIds() {
        List<String> ids = new ArrayList<>();
        for (ParameterDefinition definition : DEFAULT_PARAMETERS) {
            ids.add(definition.id);
            if (definition.id.equals("reach")) {
                ids.add(MAX_REACH_EXTRA_PARAMETER.id);
            }
        }
        return ids;
    }

    public String getParameterShowName(String parameterId) {
        ParameterDefinition definition = getAnyParameterDefinition(parameterId);
        return definition == null ? parameterId : definition.showName;
    }

    public boolean isIntegerParameter(String parameterId) {
        ParameterDefinition definition = getAnyParameterDefinition(parameterId);
        return definition != null && definition.integer;
    }

    public Map<String, Object> createRandomSettings(String botName) {
        BotProfile profile = getBot(botName);
        if (profile == null) {
            return new LinkedHashMap<>();
        }

        return profile.createRandomSettings(selectExtraProfile(profile));
    }

    private void ensureDefaults() {
        normalizeExtraProfiles();

        if (profiles.isEmpty()) {
            for (String defaultName : DEFAULT_BOT_NAMES) {
                profiles.put(defaultName, BotProfile.createDefault(defaultName));
            }
        }

        Map<String, BotProfile> loadedProfiles = new LinkedHashMap<>(profiles);
        profiles.clear();

        for (Map.Entry<String, BotProfile> entry : loadedProfiles.entrySet()) {
            String botName = normalizeBotName(entry.getKey());
            BotProfile profile = entry.getValue();

            if (botName == null || getStoredBotName(botName) != null) {
                continue;
            }

            if (profile == null) {
                profile = BotProfile.createDefault(botName);
            } else {
                profile.ensureDefaults(botName, extraProfiles.keySet());
            }

            profiles.put(botName, profile);
        }
    }

    private String getStoredBotName(String name) {
        String normalizedName = normalizeBotName(name);
        if (normalizedName == null) {
            return null;
        }

        if (profiles.containsKey(normalizedName)) {
            return normalizedName;
        }

        for (String existingName : profiles.keySet()) {
            if (existingName.equalsIgnoreCase(normalizedName)) {
                return existingName;
            }
        }

        return null;
    }

    private String getStoredExtraProfileName(String name) {
        String normalizedName = normalizeName(name);
        if (normalizedName == null) {
            return null;
        }

        if (extraProfiles.containsKey(normalizedName)) {
            return normalizedName;
        }

        for (String existingName : extraProfiles.keySet()) {
            if (existingName.equalsIgnoreCase(normalizedName)) {
                return existingName;
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
                range = getLegacyRange(source, definition);
            }

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

    static String[] getOutputParameterIds(String parameterId) {
        ParameterDefinition definition = getParameterDefinition(parameterId);
        return definition == null ? new String[] { parameterId } : definition.outputIds;
    }

    static double applyExtraProfileLimits(String parameterId, double value, Map<String, Double> extraProfile) {
        if (extraProfile == null || !parameterId.equals("reach")) {
            return value;
        }

        Double maxReach = extraProfile.get(MAX_REACH_EXTRA_PARAMETER.id);
        return maxReach != null && maxReach > 0D && value > maxReach ? maxReach : value;
    }

    private Map<String, Double> selectExtraProfile(BotProfile profile) {
        List<String> enabledProfiles = new ArrayList<>();
        for (String extraProfileName : profile.getEnabledExtraProfiles()) {
            if (extraProfiles.containsKey(extraProfileName)) {
                enabledProfiles.add(extraProfileName);
            }
        }

        if (enabledProfiles.isEmpty()) {
            return null;
        }

        return extraProfiles.get(enabledProfiles.get(ThreadLocalRandom.current().nextInt(enabledProfiles.size())));
    }

    private void normalizeExtraProfiles() {
        Map<String, Map<String, Double>> loadedExtraProfiles = new LinkedHashMap<>(extraProfiles);
        extraProfiles.clear();

        for (Map.Entry<String, Map<String, Double>> entry : loadedExtraProfiles.entrySet()) {
            String profileName = normalizeName(entry.getKey());
            if (profileName == null || getStoredExtraProfileName(profileName) != null) {
                continue;
            }

            extraProfiles.put(profileName, createExtraProfileValues(entry.getValue()));
        }
    }

    private static Map<String, Double> createExtraProfileValues(Map<String, Double> loadedValues) {
        Map<String, Double> values = new LinkedHashMap<>();
        Map<String, Double> source = loadedValues == null ? new LinkedHashMap<>() : loadedValues;

        for (ParameterDefinition definition : DEFAULT_PARAMETERS) {
            Double value = getConfiguredValue(source, definition);
            values.put(definition.id, value == null ? 0D : normalizeExtraValue(definition, value));
            if (definition.id.equals("reach")) {
                Double maxReach = getConfiguredValue(source, MAX_REACH_EXTRA_PARAMETER);
                values.put(MAX_REACH_EXTRA_PARAMETER.id, maxReach == null ? 0D : normalizeExtraValue(MAX_REACH_EXTRA_PARAMETER, maxReach));
            }
        }

        return values;
    }

    private static ParameterRange getLegacyRange(Map<String, ParameterRange> source, ParameterDefinition definition) {
        double min = 0D;
        double max = 0D;
        int found = 0;

        for (String outputId : definition.outputIds) {
            ParameterRange range = source.get(outputId);
            if (range != null) {
                range.normalize();
                min += range.getMin();
                max += range.getMax();
                found++;
            }
        }

        return found == 0 ? null : new ParameterRange(definition.showName, min / found, max / found, definition.integer);
    }

    private static Double getConfiguredValue(Map<String, Double> source, ParameterDefinition definition) {
        Double value = source.get(definition.id);
        if (value != null) {
            return value;
        }

        double total = 0D;
        int found = 0;
        for (String outputId : definition.outputIds) {
            value = source.get(outputId);
            if (value != null) {
                total += value;
                found++;
            }
        }

        return found == 0 ? null : total / found;
    }

    private static void loadExtraProfiles(JsonElement element, Map<String, Map<String, Double>> extraProfiles) {
        if (element == null || !element.isJsonObject()) {
            return;
        }

        JsonObject object = element.getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            Map<String, Double> values = new LinkedHashMap<>();

            if (entry.getValue() != null && entry.getValue().isJsonObject()) {
                for (Map.Entry<String, JsonElement> valueEntry : entry.getValue().getAsJsonObject().entrySet()) {
                    if (valueEntry.getValue() != null && valueEntry.getValue().isJsonPrimitive()) {
                        values.put(valueEntry.getKey(), valueEntry.getValue().getAsDouble());
                    }
                }
            }

            extraProfiles.put(entry.getKey(), values);
        }
    }

    private static double normalizeExtraValue(ParameterDefinition definition, double value) {
        if (definition.id.equals(MAX_REACH_EXTRA_PARAMETER.id)) {
            return Math.max(0D, value);
        }

        return definition.integer ? Math.round(value) : value;
    }

    private static ParameterDefinition getParameterDefinition(String parameterId) {
        for (ParameterDefinition definition : DEFAULT_PARAMETERS) {
            if (definition.id.equals(parameterId)) {
                return definition;
            }
        }

        return null;
    }

    private static ParameterDefinition getAnyParameterDefinition(String parameterId) {
        ParameterDefinition definition = getParameterDefinition(parameterId);
        if (definition != null) {
            return definition;
        }

        return MAX_REACH_EXTRA_PARAMETER.id.equals(parameterId) ? MAX_REACH_EXTRA_PARAMETER : null;
    }

    static String normalizeBotName(String name) {
        return normalizeName(name);
    }

    private static String normalizeName(String name) {
        if (name == null) {
            return null;
        }
        String normalizedName = name.trim();
        return normalizedName.isEmpty() ? null : normalizedName;
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
        private final String[] outputIds;

        private ParameterDefinition(String id, String showName, double min, double max, boolean integer, String... outputIds) {
            this.id = id;
            this.showName = showName;
            this.min = min;
            this.max = max;
            this.integer = integer;
            this.outputIds = outputIds == null || outputIds.length == 0 ? new String[] { id } : outputIds;
        }

    }

}
