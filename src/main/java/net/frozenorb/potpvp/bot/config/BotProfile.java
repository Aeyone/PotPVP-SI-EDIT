package net.frozenorb.potpvp.bot.config;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

public final class BotProfile {

    @Getter @Setter private transient String id;
    @Getter private Map<String, ParameterRange> parameters = new LinkedHashMap<>();

    public BotProfile() {
    }

    static BotProfile createDefault(String id) {
        BotProfile profile = new BotProfile();
        profile.id = id;
        profile.parameters = BotConfig.createParameterRanges(null);
        return profile;
    }

    void ensureDefaults(String fallbackId) {
        String normalizedId = BotConfig.normalizeBotId(id);
        id = normalizedId == null ? fallbackId : normalizedId;
        parameters = BotConfig.createParameterRanges(parameters);
    }

    public Map<String, Object> createRandomSettings() {
        ensureDefaults(id);

        Map<String, Object> settings = new LinkedHashMap<>();
        for (Map.Entry<String, ParameterRange> entry : parameters.entrySet()) {
            settings.put(entry.getKey(), entry.getValue().randomValue());
        }

        return settings;
    }

}
