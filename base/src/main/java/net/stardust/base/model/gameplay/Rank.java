package net.stardust.base.model.gameplay;

import java.util.Comparator;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public enum Rank {

    E(0, "§c", "<red>", NamedTextColor.RED),
    D(1, "§6", "<gold>", NamedTextColor.GOLD),
    C(2, "§e", "<yellow>", NamedTextColor.YELLOW),
    B(3, "§a", "<green>", NamedTextColor.GREEN),
    A(4, "§2", "<dark_green>", NamedTextColor.DARK_GREEN),
    S(5, "§9", "<blue>", NamedTextColor.BLUE);

    public static final Comparator<Rank> COMPARATOR = (r1, r2) -> r1.order - r2.order;

    private int order;
    private String color, tagColor;
    private TextColor textColor;

    Rank(int order, String color, String tagColor, TextColor textColor) {
        this.order = order;
        this.color = color;
        this.tagColor = tagColor;
        this.textColor = textColor;
    }

    public int order() {
        return order;
    }

    public String getColor() {
        return color;
    }

    public String getTagColor() {
        return tagColor;
    }

    public TextColor getTextColor() {
        return textColor;
    }

    public Component getAsComponent() {
        return Component.text(toString(), textColor);
    }

    public char toChar() {
        return toString().charAt(0);
    }

    public Rank fromChar(char c) {
        return switch(c) {
            case 'E', 'e' -> E;
            case 'D', 'd' -> D;
            case 'C', 'c' -> C;
            case 'B', 'b' -> B;
            case 'A', 'a' -> A;
            case 'S', 's' -> S;
            default -> null;
        };
    }

    public Rank fromOrder(int order) {
        return switch(order) {
            case 0 -> E;
            case 1 -> D;
            case 2 -> C;
            case 3 -> B;
            case 4 -> A;
            case 5 -> S;
            default -> null;
        };
    }

    public Rank fromColor(String color) {
        return switch(color.toLowerCase()) {
            case "§c" -> E;
            case "§6" -> D;
            case "§e" -> C;
            case "§a" -> B;
            case "§2" -> A;
            case "§9" -> S;
            default -> null;
        };
    }

    public Rank fromTagColor(String color) {
        return switch(color.toLowerCase()) {
            case "<red>" -> E;
            case "<gold>" -> D;
            case "<yellow>" -> C;
            case "<green>" -> B;
            case "<dark_green>" -> A;
            case "<blue>" -> S;
            default -> null;
        };
    }

    public Rank fromTextColor(TextColor color) {
        return switch(color.value()) {
            case 0xff5555 -> E;
            case 0xffaa00 -> D;
            case 0xffff55 -> C;
            case 0x55ff55 -> B;
            case 0x00aa00 -> A;
            case 0x5555ff -> S;
            default -> null;
        };
    }

}
