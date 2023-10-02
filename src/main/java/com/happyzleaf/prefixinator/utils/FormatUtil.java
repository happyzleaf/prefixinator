package com.happyzleaf.prefixinator.utils;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Color;

import java.util.EnumSet;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * EssentialsX @ <a href="https://github.com/EssentialsX/Essentials">GitHub</a>
 */
public final class FormatUtil {
    private static final Pattern REPLACE_ALL_PATTERN = Pattern.compile("(&)?&([0-9a-fk-orA-FK-OR])");
    private static final Pattern REPLACE_ALL_RGB_PATTERN = Pattern.compile("(&)?&#([0-9a-fA-F]{6})");
    private static final EnumSet<ChatColor> ALL = EnumSet.allOf(ChatColor.class);

    private FormatUtil() {
    }

    //This is the general permission sensitive message format function
    public static BaseComponent[] format(final String input) {
        if (input == null) {
            return null;
        }
        return TextComponent.fromLegacyText(replaceColor(input));
    }

    private static String replaceColor(final String input) {
        final StringBuffer legacyBuilder = new StringBuffer();
        final Matcher legacyMatcher = REPLACE_ALL_PATTERN.matcher(input);
        legacyLoop:
        while (legacyMatcher.find()) {
            final boolean isEscaped = legacyMatcher.group(1) != null;
            if (!isEscaped) {
                final char code = legacyMatcher.group(2).toLowerCase(Locale.ROOT).charAt(0);
                for (final ChatColor color : ALL) {
                    if (color.getChar() == code) {
                        legacyMatcher.appendReplacement(legacyBuilder, ChatColor.COLOR_CHAR + "$2");
                        continue legacyLoop;
                    }
                }
            }
            // Don't change & to section sign (or replace two &'s with one)
            legacyMatcher.appendReplacement(legacyBuilder, "&$2");
        }
        legacyMatcher.appendTail(legacyBuilder);

        final StringBuffer rgbBuilder = new StringBuffer();
        final Matcher rgbMatcher = REPLACE_ALL_RGB_PATTERN.matcher(legacyBuilder.toString());
        while (rgbMatcher.find()) {
            final boolean isEscaped = rgbMatcher.group(1) != null;
            if (!isEscaped) {
                try {
                    final String hexCode = rgbMatcher.group(2);
                    rgbMatcher.appendReplacement(rgbBuilder, parseHexColor(hexCode));
                    continue;
                } catch (final NumberFormatException ignored) {
                }
            }
            rgbMatcher.appendReplacement(rgbBuilder, "&#$2");
        }
        rgbMatcher.appendTail(rgbBuilder);
        return rgbBuilder.toString();
    }

    /**
     * @throws NumberFormatException If the provided hex color code is invalid or if version is lower than 1.16.
     */
    public static String parseHexColor(String hexColor) throws NumberFormatException {
        if (hexColor.startsWith("#")) {
            hexColor = hexColor.substring(1); //fuck you im reassigning this.
        }
        if (hexColor.length() != 6) {
            throw new NumberFormatException("Invalid hex length");
        }

        //noinspection ResultOfMethodCallIgnored
        Color.fromRGB(Integer.decode("#" + hexColor));
        final StringBuilder assembledColorCode = new StringBuilder();
        assembledColorCode.append(ChatColor.COLOR_CHAR + "x");
        for (final char curChar : hexColor.toCharArray()) {
            assembledColorCode.append(ChatColor.COLOR_CHAR).append(curChar);
        }
        return assembledColorCode.toString();
    }
}