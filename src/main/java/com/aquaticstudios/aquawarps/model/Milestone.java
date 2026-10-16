package com.aquaticstudios.aquawarps.model;

import java.util.List;

public final class Milestone {

    private final int amount;
    private final double pearls;
    private final List<String> commands;

    public Milestone(int amount, double pearls, List<String> commands) {
        this.amount = amount;
        this.pearls = pearls;
        this.commands = commands;
    }

    public int amount() {
        return amount;
    }

    public double pearls() {
        return pearls;
    }

    public List<String> commands() {
        return commands;
    }
}
