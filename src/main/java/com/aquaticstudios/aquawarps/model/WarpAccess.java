package com.aquaticstudios.aquawarps.model;

import java.util.Locale;

public enum WarpAccess {

    PUBLIC,
    PASSWORD,
    PRIVATE;

    public WarpAccess next() {
        return values()[(ordinal() + 1) % values().length];
    }

    public static WarpAccess fromString(String value) {
        if (value == null) {
            return PUBLIC;
        }
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return PUBLIC;
        }
    }
}
