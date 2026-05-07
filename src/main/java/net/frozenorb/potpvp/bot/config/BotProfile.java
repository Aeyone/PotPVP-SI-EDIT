package net.frozenorb.potpvp.bot.config;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class BotProfile {

    @Getter @Setter private transient String name;
    @Getter private Map<String, ParameterRange> parameters = new LinkedHashMap<>();
    @Getter private Set<String> enabledExtraProfiles = new LinkedHashSet<>();

    public BotProfile() {
    }

    static BotProfile createDefault(String name) {
        BotProfile profile = new BotProfile();
        profile.name = name;
        profile.parameters = BotConfig.createParameterRanges(null);
        return profile;
    }

    void ensureDefaults(String fallbackName, Set<String> availableExtraProfiles) {
        String normalizedName = BotConfig.normalizeBotName(name);
        name = normalizedName == null ? fallbackName : normalizedName;
        parameters = BotConfig.createParameterRanges(parameters);
        enabledExtraProfiles = normalizeEnabledExtraProfiles(availableExtraProfiles);
    }

    public Map<String, Object> createRandomSettings(Map<String, Double> extraProfile) {
        parameters = BotConfig.createParameterRanges(parameters);

        Map<String, Object> settings = new LinkedHashMap<>();
        for (Map.Entry<String, ParameterRange> entry : parameters.entrySet()) {
            ParameterRange range = entry.getValue();
            double value = ((Number) range.randomValue()).doubleValue();

            if (extraProfile != null) {
                Double extraValue = extraProfile.get(entry.getKey());
                if (extraValue != null) {
                    value += extraValue;
                }
            }

            value = Math.max(0D, value);
            value = BotConfig.applyExtraProfileLimits(entry.getKey(), value, extraProfile);
            Object formattedValue = range.formatValue(value);
            for (String outputParameterId : BotConfig.getOutputParameterIds(entry.getKey())) {
                settings.put(outputParameterId, formattedValue);
            }
        }

        return settings;
    }

    boolean isExtraProfileEnabled(String extraProfileName) {
        return enabledExtraProfiles != null && enabledExtraProfiles.contains(extraProfileName);
    }

    void setExtraProfileEnabled(String extraProfileName, boolean enabled) {
        if (enabledExtraProfiles == null) {
            enabledExtraProfiles = new LinkedHashSet<>();
        }

        if (enabled) {
            enabledExtraProfiles.add(extraProfileName);
        } else {
            enabledExtraProfiles.remove(extraProfileName);
        }
    }

    private Set<String> normalizeEnabledExtraProfiles(Set<String> availableExtraProfiles) {
        Set<String> normalized = new LinkedHashSet<>();
        if (enabledExtraProfiles == null || availableExtraProfiles == null) {
            return normalized;
        }

        for (String enabledExtraProfile : enabledExtraProfiles) {
            for (String availableExtraProfile : availableExtraProfiles) {
                if (availableExtraProfile.equalsIgnoreCase(enabledExtraProfile)) {
                    normalized.add(availableExtraProfile);
                    break;
                }
            }
        }

        return normalized;
    }

}
