package vv0ta3fa9.plugin.kapyMine.util;

import org.bukkit.ChatColor;

public final class ColorUtil {
    private ColorUtil() {}
    public static String colorize(String text) {
        if (text == null) return "";
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}

