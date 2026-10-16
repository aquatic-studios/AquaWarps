package com.aquaticstudios.aquawarps.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public final class Formats {

    private static final DecimalFormatSymbols SYMBOLS = new DecimalFormatSymbols(Locale.US);
    private static final DecimalFormat MONEY = new DecimalFormat("#,##0.##", SYMBOLS);
    private static final DecimalFormat INTEGER = new DecimalFormat("#,##0", SYMBOLS);
    private static final DecimalFormat DECIMAL_1 = new DecimalFormat("0.0", SYMBOLS);
    private static final SimpleDateFormat DATE = new SimpleDateFormat("dd/MM/yyyy", Locale.ROOT);
    private static final SimpleDateFormat DATE_TIME = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ROOT);

    private static final String[] COMPACT = {"", "k", "M", "B", "T"};

    public static String money(double amount) {
        return MONEY.format(amount);
    }

    public static String number(long amount) {
        return INTEGER.format(amount);
    }

    public static String decimal(double value) {
        return DECIMAL_1.format(value);
    }

    public static String compact(long value) {
        if (value < 1000L) {
            return String.valueOf(value);
        }
        double working = value;
        int index = 0;
        while (working >= 1000d && index < COMPACT.length - 1) {
            working /= 1000d;
            index++;
        }
        return DECIMAL_1.format(working) + COMPACT[index];
    }

    public static String date(long millis) {
        return DATE.format(new Date(millis));
    }

    public static String dateTime(long millis) {
        return DATE_TIME.format(new Date(millis));
    }

    public static String duration(long millis) {
        if (millis <= 0L) {
            return "0s";
        }
        long days = TimeUnit.MILLISECONDS.toDays(millis);
        long hours = TimeUnit.MILLISECONDS.toHours(millis) % 24L;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60L;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60L;

        StringBuilder builder = new StringBuilder();
        if (days > 0L) {
            builder.append(days).append("d ");
        }
        if (hours > 0L) {
            builder.append(hours).append("h ");
        }
        if (minutes > 0L && days == 0L) {
            builder.append(minutes).append("m ");
        }
        if (seconds > 0L && days == 0L && hours == 0L) {
            builder.append(seconds).append("s");
        }
        String out = builder.toString().trim();
        return out.isEmpty() ? "0s" : out;
    }

    public static String ago(long millis) {
        long diff = System.currentTimeMillis() - millis;
        if (diff < 60_000L) {
            return "just now";
        }
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
        if (minutes < 60L) {
            return minutes + (minutes == 1L ? " minute ago" : " minutes ago");
        }
        long hours = TimeUnit.MILLISECONDS.toHours(diff);
        if (hours < 24L) {
            return hours + (hours == 1L ? " hour ago" : " hours ago");
        }
        long days = TimeUnit.MILLISECONDS.toDays(diff);
        if (days < 30L) {
            return days + (days == 1L ? " day ago" : " days ago");
        }
        long months = days / 30L;
        if (months < 12L) {
            return months + (months == 1L ? " month ago" : " months ago");
        }
        long years = days / 365L;
        return years + (years == 1L ? " year ago" : " years ago");
    }

    public static String bar(double ratio, int length, String full, String empty) {
        int filled = (int) Math.round(Math.max(0d, Math.min(1d, ratio)) * length);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            builder.append(i < filled ? full : empty);
        }
        return builder.toString();
    }

    public static String pretty(String raw) {
        if (raw == null || raw.isEmpty()) {
            return "";
        }
        String[] parts = raw.toLowerCase(Locale.ROOT).replace('_', ' ').split(" ");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return builder.toString();
    }
}
