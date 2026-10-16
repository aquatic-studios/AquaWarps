package com.aquaticstudios.aquawarps.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CC {

    private static final MiniMessage MINI = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.builder()
            .character('§')
            .hexCharacter('#')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    private static final Pattern HEX = Pattern.compile("&#([0-9a-fA-F]{6})");
    private static final Pattern BUNGEE_HEX =
            Pattern.compile("[&§]x([&§][0-9a-fA-F]){6}", Pattern.CASE_INSENSITIVE);

    private static final String[] NAMED = {
            "black", "dark_blue", "dark_green", "dark_aqua", "dark_red", "dark_purple",
            "gold", "gray", "dark_gray", "blue", "green", "aqua", "red", "light_purple",
            "yellow", "white"
    };
    private static final String CODES = "0123456789abcdef";
    private static final List<String> COLORS = Arrays.asList(NAMED);
    private static final Pattern TAG = Pattern.compile(
            "(?<!\\\\)</?!?([#a-zA-Z0-9_\\-]+)((?::(?:'[^']*'|\"[^\"]*\"|[^:<>'\"]*))*)>");

    public static Component parse(String input) {
        if (input == null) {
            return Component.empty();
        }
        return MINI.deserialize(toMiniMessage(input));
    }

    public static String toLegacy(String input) {
        return LEGACY.serialize(parse(input));
    }

    public static String format(String input) {
        return toLegacy(input);
    }

    public static String strip(String input) {
        return MINI.stripTags(toMiniMessage(input));
    }

    public static String escape(String input) {
        return MINI.escapeTags(input == null ? "" : input);
    }

    public static int visibleLength(String input) {
        return strip(input).length();
    }

    public static boolean hasInteractiveTags(String input) {
        String lower = toMiniMessage(input).toLowerCase();
        return lower.contains("<click") || lower.contains("<hover") || lower.contains("<insert");
    }

    public static Set<String> formats(String input) {
        Set<String> used = new LinkedHashSet<>();
        Matcher matcher = TAG.matcher(toMiniMessage(input));
        while (matcher.find()) {
            String name = matcher.group(1).toLowerCase(Locale.ROOT);
            if (name.equals("color") || name.equals("colour") || name.equals("c")) {
                String args = matcher.group(2);
                name = args.length() > 1
                        ? args.substring(1).split(":")[0].replace("'", "").replace("\"", "").toLowerCase(Locale.ROOT)
                        : "";
            }
            String format = formatOf(name);
            if (format != null) {
                used.add(format);
            }
        }
        return used;
    }

    private static String formatOf(String name) {
        if (name.isEmpty() || name.equals("reset") || name.equals("r")) {
            return null;
        }
        if (name.startsWith("#")) {
            return "hex";
        }
        if (COLORS.contains(name) || name.equals("grey") || name.equals("dark_grey")) {
            return "legacy";
        }
        switch (name) {
            case "bold":
            case "b":
                return "bold";
            case "italic":
            case "i":
            case "em":
                return "italic";
            case "underlined":
            case "u":
                return "underlined";
            case "strikethrough":
            case "st":
                return "strikethrough";
            case "obfuscated":
            case "obf":
                return "obfuscated";
            case "gradient":
            case "transition":
                return "gradient";
            case "rainbow":
                return "rainbow";
            default:
                return "minimessage";
        }
    }

    public static String sanitize(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        String work = input.replace('<', '(').replace('>', ')');
        return work.replaceAll("(?i)&(#[0-9a-f]{6}|[0-9a-fk-or])", "");
    }

    public static String toMiniMessage(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        String work = normalizeBungeeHex(input);
        work = HEX.matcher(work).replaceAll(m -> "<reset><#" + m.group(1) + ">");

        StringBuilder out = new StringBuilder(work.length() + 16);
        char[] chars = work.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if ((c == '&' || c == '§') && i + 1 < chars.length) {
                char code = Character.toLowerCase(chars[i + 1]);
                String tag = translate(code);
                if (tag != null) {
                    out.append(tag);
                    i++;
                    continue;
                }
            }
            out.append(c);
        }
        return out.toString();
    }

    private static String normalizeBungeeHex(String input) {
        Matcher matcher = BUNGEE_HEX.matcher(input);
        StringBuilder builder = new StringBuilder();
        while (matcher.find()) {
            String hex = matcher.group().replaceAll("[&§]", "").substring(1);
            matcher.appendReplacement(builder, Matcher.quoteReplacement("<reset><#" + hex + ">"));
        }
        matcher.appendTail(builder);
        return builder.toString();
    }

    private static String translate(char code) {
        int index = CODES.indexOf(code);
        if (index != -1) {
            return "<reset><" + NAMED[index] + ">";
        }
        switch (code) {
            case 'l':
                return "<bold>";
            case 'o':
                return "<italic>";
            case 'n':
                return "<underlined>";
            case 'm':
                return "<strikethrough>";
            case 'k':
                return "<obfuscated>";
            case 'r':
                return "<reset>";
            default:
                return null;
        }
    }
}
