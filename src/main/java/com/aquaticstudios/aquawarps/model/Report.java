package com.aquaticstudios.aquawarps.model;

import java.util.UUID;

public final class Report {

    private final String id;
    private final String warpId;
    private final UUID reporter;
    private final String reporterName;
    private final String reason;
    private final long created;
    private boolean handled;

    public Report(String id, String warpId, UUID reporter, String reporterName, String reason,
                  long created, boolean handled) {
        this.id = id;
        this.warpId = warpId;
        this.reporter = reporter;
        this.reporterName = reporterName;
        this.reason = reason;
        this.created = created;
        this.handled = handled;
    }

    public String id() {
        return id;
    }

    public String warpId() {
        return warpId;
    }

    public UUID reporter() {
        return reporter;
    }

    public String reporterName() {
        return reporterName == null ? "Unknown" : reporterName;
    }

    public String reason() {
        return reason == null ? "-" : reason;
    }

    public long created() {
        return created;
    }

    public boolean handled() {
        return handled;
    }

    public void handled(boolean handled) {
        this.handled = handled;
    }
}
