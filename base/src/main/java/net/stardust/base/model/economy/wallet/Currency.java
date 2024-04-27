package net.stardust.base.model.economy.wallet;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.stardust.base.utils.database.lang.Translation;

public enum Currency {
    
    BRONZE(0, "§6", "<gold>", NamedTextColor.GOLD, 'B', "Bronze"),
    SILVER(1, "§7", "<gray>", NamedTextColor.GRAY, 'S', "Prata"),
    GOLD(2, "§e", "<yellow>", NamedTextColor.YELLOW, 'G', "Ouro");

    private int order;
    private String color, tagColor;
    private TextColor textColor;
    private char defaultSymbol;
    private String symbolTranslationKey;
    private String consoleName;

    Currency(int order, String color, String tagColor, TextColor textColor, char defaultSymbol, String consoleName) {
        this.order = order;
        this.color = color;
        this.tagColor = tagColor;
        this.textColor = textColor;
        this.defaultSymbol = defaultSymbol;
        this.consoleName = consoleName;
        symbolTranslationKey = "currency." + toString().toLowerCase() + "symbol";
    }

    public int getOrder() {
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

    public char getDefaultSymbol() {
        return defaultSymbol;
    }

    public char getSymbol(Locale locale) {
        return Translation.string(locale, symbolTranslationKey).charAt(0);
    }

    public Component getSymbolAsComponent() {
        return Component.text(defaultSymbol, textColor);
    }

    public Component getSymbolAsComponent(Locale locale) {
        return Translation.get(locale, symbolTranslationKey);
    }

    public TranslatableComponent getSymbolAsTranslatableComponent() {
        return Component.translatable(symbolTranslationKey);
    }

    public String getSymbolTranslationKey() {
        return symbolTranslationKey;
    }

    public String getConsoleName() {
        return consoleName;
    }

    public static Currency fromDefaultSymbol(char symbol) {
        return switch(symbol) {
            case 'B', 'b' -> BRONZE;
            case 'S', 's' -> SILVER;
            case 'G', 'g' -> GOLD;
            default -> null;
        };
    }

    public static Currency fromColor(String color) {
        return switch(color.toLowerCase()) {
            case "§6" -> BRONZE;
            case "§7" -> SILVER;
            case "§e" -> GOLD;
            default -> null;
        };
    }

    public static Currency fromTextColor(TextColor color) {
        return switch(color.value()) {
            case 0xffaa00 -> BRONZE;
            case 0xaaaaaa -> SILVER;
            case 0xffff55 -> GOLD;
            default -> null;
        };
    }

    public static Currency fromTagColor(String color) {
        return switch(color.toLowerCase()) {
            case "<gold>" -> BRONZE;
            case "<gray>", "<grey>" -> SILVER;
            case "<yellow>" -> GOLD;
            default -> null;
        };
    }

    public static Currency fromConsoleName(String name) {
        return switch(StringUtils.capitalize(name.toLowerCase())) {
            case "Bronze" -> BRONZE;
            case "Prata" -> SILVER;
            case "Ouro" -> GOLD;
            default -> null;
        };
    }

    public static Currency fromOrder(int order) {
        return switch(order) {
            case 0 -> BRONZE;
            case 1 -> SILVER;
            case 2 -> GOLD;
            default -> null;
        };
    }

}
