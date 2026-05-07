package net.frozenorb.potpvp.bot.config;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.ThreadLocalRandom;

public final class ParameterRange {

    @Getter @Setter private double min;
    @Getter @Setter private double max;
    @Getter @Setter private transient boolean integer;
    @Getter @Setter private transient String showName;

    public ParameterRange() {
    }

    ParameterRange(String showName, double min, double max, boolean integer) {
        this.showName = showName;
        this.min = min;
        this.max = max;
        this.integer = integer;
    }

    public Object randomValue() {
        normalize();

        if (integer) {
            int minInt = (int) Math.round(min);
            int maxInt = (int) Math.round(max);

            if (minInt == maxInt) {
                return minInt;
            }

            return ThreadLocalRandom.current().nextInt(minInt, maxInt + 1);
        }

        if (Double.compare(min, max) == 0) {
            return min;
        }

        return ThreadLocalRandom.current().nextDouble(min, max);
    }

    public void normalize() {
        min = Math.max(0D, min);
        max = Math.max(0D, max);

        if (max < min) {
            double oldMin = min;
            min = max;
            max = oldMin;
        }
    }

    public Object formatValue(double value) {
        return integer ? (int) Math.round(value) : value;
    }

}
