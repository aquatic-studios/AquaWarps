package com.aquaticstudios.aquawarps.database;

import java.util.UUID;

public final class Visit {

    private final String warpId;
    private final UUID player;
    private final long time;

    public Visit(String warpId, UUID player, long time) {
        this.warpId = warpId;
        this.player = player;
        this.time = time;
    }

    public String warpId() {
        return warpId;
    }

    public UUID player() {
        return player;
    }

    public long time() {
        return time;
    }
}
