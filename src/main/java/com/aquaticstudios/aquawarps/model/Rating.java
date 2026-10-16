package com.aquaticstudios.aquawarps.model;

import java.util.UUID;

public final class Rating {

    private final String warpId;
    private final UUID player;
    private String playerName;
    private int stars;
    private String comment;
    private long created;

    public Rating(String warpId, UUID player, String playerName, int stars, String comment, long created) {
        this.warpId = warpId;
        this.player = player;
        this.playerName = playerName;
        this.stars = stars;
        this.comment = comment;
        this.created = created;
    }

    public String warpId() {
        return warpId;
    }

    public UUID player() {
        return player;
    }

    public String playerName() {
        return playerName == null ? "Unknown" : playerName;
    }

    public void playerName(String playerName) {
        this.playerName = playerName;
    }

    public int stars() {
        return stars;
    }

    public void stars(int stars) {
        this.stars = Math.max(1, Math.min(5, stars));
    }

    public String comment() {
        return comment == null ? "" : comment;
    }

    public boolean hasComment() {
        return comment != null && !comment.isEmpty();
    }

    public void comment(String comment) {
        this.comment = comment;
    }

    public long created() {
        return created;
    }

    public void created(long created) {
        this.created = created;
    }
}
