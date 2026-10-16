package com.aquaticstudios.aquawarps.model;

import java.util.Locale;

public enum SortMode {

    TRENDING,
    RATING,
    VISITS,
    NEWEST,
    OLDEST,
    ALPHABETICAL,
    FAVORITES,
    LEVEL,
    RANDOM;

    public SortMode next() {
        return values()[(ordinal() + 1) % values().length];
    }

    public SortMode previous() {
        return values()[(ordinal() - 1 + values().length) % values().length];
    }

    public static SortMode fromString(String value, SortMode fallback) {
        if (value == null) {
            return fallback;
        }
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return fallback;
        }
    }
}
