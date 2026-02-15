package ru.fkdev.donmarket.util;

import net.md_5.bungee.api.ChatColor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ColorUtil {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final Pattern GRADIENT_PATTERN = Pattern.compile("<gradient:(#[A-Fa-f0-9]{6}):(#[A-Fa-f0-9]{6})>(.*?)</gradient>");

    private ColorUtil() {}

    public static String colorize(String text) {
        if (text == null) return "";
        text = applyHexColors(text);
        text = ChatColor.translateAlternateColorCodes('&', text);
        return text;
    }

    public static String applyHexColors(String message) {
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String hex = matcher.group(1);
            StringBuilder replacement = new StringBuilder("§x");
            for (char c : hex.toCharArray()) {
                replacement.append('§').append(c);
            }
            matcher.appendReplacement(sb, replacement.toString());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public static String gradient(String text, int r1, int g1, int b1, int r2, int g2, int b2) {
        if (text == null || text.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        int len = text.length();
        for (int i = 0; i < len; i++) {
            float ratio = len == 1 ? 0 : (float) i / (len - 1);
            int r = (int) (r1 + (r2 - r1) * ratio);
            int g = (int) (g1 + (g2 - g1) * ratio);
            int b = (int) (b1 + (b2 - b1) * ratio);
            String hex = String.format("%02x%02x%02x", r, g, b);
            sb.append("§x");
            for (char c : hex.toCharArray()) {
                sb.append('§').append(c);
            }
            sb.append(text.charAt(i));
        }
        return sb.toString();
    }

    public static String gradientHex(String text, String hex1, String hex2) {
        int r1 = Integer.parseInt(hex1.substring(1, 3), 16);
        int g1 = Integer.parseInt(hex1.substring(3, 5), 16);
        int b1 = Integer.parseInt(hex1.substring(5, 7), 16);
        int r2 = Integer.parseInt(hex2.substring(1, 3), 16);
        int g2 = Integer.parseInt(hex2.substring(3, 5), 16);
        int b2 = Integer.parseInt(hex2.substring(5, 7), 16);
        return gradient(text, r1, g1, b1, r2, g2, b2);
    }

    public static String funtimeGradient() {
        return gradient("FunTime.su", 0xFF, 0x00, 0x00, 0xFF, 0x50, 0x00);
    }
}
