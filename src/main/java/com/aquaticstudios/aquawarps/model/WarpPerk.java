package com.aquaticstudios.aquawarps.model;

import java.util.Locale;

public enum WarpPerk {

    DESCRIPTION_LINE,
    TAG_SLOT,
    CUSTOM_ICON,
    PARTICLE,
    GLOW,
    DISCOUNT;

    public static WarpPerk fromString(String value) {
        if (value == null) {
            return null;
        }
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
