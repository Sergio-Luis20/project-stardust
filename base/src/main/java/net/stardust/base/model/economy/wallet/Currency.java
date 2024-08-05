package net.stardust.base.model.economy.wallet;

import java.util.Comparator;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.stardust.base.model.economy.MonetaryEntity;
import net.stardust.base.utils.database.lang.Translation;

/**
 * Enum of all currencies allowed in Stardust server economy system.
 * 
 * @see MonetaryEntity
 * 
 * @author Sergio Luis
 */
public enum Currency {
    
    /**
     * Bronze currency. It has the smallest value.
     */
    BRONZE((byte) 0, "§6", "<gold>", NamedTextColor.GOLD, 'B', "Bronze"),
            
    /**
     * Silver currency. It has medium value.
     */
    SILVER((byte) 1, "§7", "<gray>", NamedTextColor.GRAY, 'S', "Prata"),
                    
    /**
     * Gold currency. It has the biggest value.
     */
    GOLD((byte) 2, "§e", "<yellow>", NamedTextColor.YELLOW, 'G', "Ouro");

    /**
     * The default comparator for currencies values.
     */
    public static final Comparator<Currency> COMPARATOR = (c1, c2) -> c1.getOrder() - c2.getOrder();

    private byte order;
    private String color, tagColor;
    private TextColor textColor;
    private char defaultSymbol;
    private String symbolTranslationKey;
    private String consoleName;

    Currency(byte order, String color, String tagColor, TextColor textColor, char defaultSymbol, String consoleName) {
        this.order = order;
        this.color = color;
        this.tagColor = tagColor;
        this.textColor = textColor;
        this.defaultSymbol = defaultSymbol;
        this.consoleName = consoleName;
        symbolTranslationKey = "currency." + toString().toLowerCase() + "symbol";
    }

    /**
     * Returns the order of this currency.
     * The order of a currency is just an integer for value comparation.
     * This is the number used in {@link Currency#COMPARATOR}.
     * 
     * @return
     */
    public byte getOrder() {
        return order;
    }

    /**
     * Returns the Minecraft default hexadecimal color in section syntax
     * for this currency.
     * 
     * @return the Minecraft section syntax color
     */
    public String getColor() {
        return color;
    }

    /**
     * Returns the color of this currency in tag format for being used in {@link MiniMessage}.
     * 
     * @see MiniMessage
     * @return the tag color
     */
    public String getTagColor() {
        return tagColor;
    }

    /**
     * Returns the color of this entity in a {@link TextColor} instance for being
     * used in {@link Component}s.
     * 
     * @see TextColor
     * @return the text color
     */
    public TextColor getTextColor() {
        return textColor;
    }

    /**
     * Returns the default symbol of this currency. The symbol is
     * just an abreviation of the currency name.
     * 
     * @return the default symbol
     */
    public char getDefaultSymbol() {
        return defaultSymbol;
    }

    /**
     * Returns the symbol translated to the language of the specified
     * locale.
     * 
     * @see #getDefaultSymbol()
     * @param locale the locale to translate the default symbol
     * @return the translated symbol
     */
    public char getSymbol(Locale locale) {
        return Translation.string(locale, symbolTranslationKey).charAt(0);
    }

    /**
     * Returns the default symbol wrapped in a {@link Component} with
     * the {@link TextColor} returned by {@link #getTextColor()}.
     * 
     * @see Component
     * @see TextColor
     * @see #getTextColor()
     * @see #getDefaultSymbol()
     * @return the default symbol as {@link Component}
     */
    public Component getSymbolAsComponent() {
        return Component.text(defaultSymbol, textColor);
    }

    /**
     * Returns the translated symbol wrapped in a {@link Component} with
     * the {@link TextColor} returned by {@link #getTextColor()}. This
     * can be used in a {@link PlainTextComponentSerializer} to get the
     * translated text.
     * 
     * @see Component
     * @see TextColor
     * @see TranslatableComponent
     * @see PlainTextComponentSerializer
     * @see #getTextColor()
     * @see #getSymbol(Locale)
     * @see #getSymbolAsTranslatableComponent()
     * @param locale the locale to translate the symbol
     * @return the translated symbol as {@link Component}
     */
    public Component getSymbolAsComponent(Locale locale) {
        return Translation.get(locale, symbolTranslationKey).color(textColor);
    }

    /**
     * Returns the symbol as {@link TranslatableComponent}, using the default
     * translation internal translation key. This is different from the {@link Component}
     * returned by {@link #getSymbolAsComponent(Locale)}, because as this is translatable,
     * not translated, so it will differ the text depending on who are you sending it. For
     * that reason, it can't be used in {@link PlainTextComponentSerializer} as it hasn't
     * a default text to return.
     * 
     * @see Component
     * @see TranslatableComponent
     * @see PlainTextComponentSerializer
     * @see #getSymbolAsComponent(Locale)
     * @see #getSymbolTranslationKey()
     * @return the symbol as {@link TranslatableComponent}
     */
    public TranslatableComponent getSymbolAsTranslatableComponent() {
        return Component.translatable(symbolTranslationKey);
    }

    /**
     * Returns the symbol translation key, which can be used in {@link Component#translatable()}.
     * 
     * @see Component#translatable()
     * @return the symbol translation key
     */
    public String getSymbolTranslationKey() {
        return symbolTranslationKey;
    }

    /**
     * Returns the name of this currency to be used in a context of console, meaning
     * that it is in a language that the developer team will understand.
     * 
     * @return the name of this currency in developer's language
     */
    public String getConsoleName() {
        return consoleName;
    }

    /**
     * Returns the currency based on the default symbol passed as parameter.
     * If the default symbol does not correspond on one of the presets,
     * then this method will return null.
     * 
     * @see #getDefaultSymbol()
     * @see #getSymbol(Locale)
     * @param symbol the de fault symbol of the currency
     * @return the currency of the parameter symbol
     */
    public static Currency fromDefaultSymbol(char symbol) {
        return switch (symbol) {
            case 'B', 'b' -> BRONZE;
            case 'S', 's' -> SILVER;
            case 'G', 'g' -> GOLD;
            default -> null;
        };
    }

    /**
     * Returns a currrency based on the Minecraft section syntax color.
     * If the color does not corresponde to one of the currency instances,
     * this will return null.
     * 
     * @see #getColor()
     * @param color the Minecraft section syntax color
     * @return the currency of the color parameter
     */
    public static Currency fromColor(String color) {
        return switch (color.toLowerCase()) {
            case "§6" -> BRONZE;
            case "§7" -> SILVER;
            case "§e" -> GOLD;
            default -> null;
        };
    }

    /**
     * Returns a currency based on the {@link TextColor} value. If
     * the value does not corresponde to one of the currency instances,
     * this will return null.
     * 
     * @see #getTextColor()
     * @param color the {@link TextColor} with the requested value
     * @return the currency of the {@link TextColor} value
     */
    public static Currency fromTextColor(TextColor color) {
        return switch (color.value()) {
            case 0xffaa00 -> BRONZE;
            case 0xaaaaaa -> SILVER;
            case 0xffff55 -> GOLD;
            default -> null;
        };
    }

    /**
     * Returns a currency based on a tag color. If the color is not one
     * of the currency instances, this will return null.
     * 
     * @see #getTagColor()
     * @param color the tag color
     * @return the currency of the tag color
     */
    public static Currency fromTagColor(String color) {
        return switch (color.toLowerCase()) {
            case "<gold>" -> BRONZE;
            case "<gray>", "<grey>" -> SILVER;
            case "<yellow>" -> GOLD;
            default -> null;
        };
    }

    /**
     * Returns a currency based on its console name. This is case sensitive
     * and must be capitalized. If the passed console name is not one of the
     * currency instances, this will return null.
     * 
     * @see #getConsoleName()
     * @param name the console name of the currency
     * @return the currency of the console name
     */
    public static Currency fromConsoleName(String name) {
        return switch (StringUtils.capitalize(name.toLowerCase())) {
            case "Bronze" -> BRONZE;
            case "Prata" -> SILVER;
            case "Ouro" -> GOLD;
            default -> null;
        };
    }

    /**
     * Returns the currency based on its order.
     * 
     * @see #getOrder()
     * @param order the order
     * @return the currency of the order
     */
    public static Currency fromOrder(byte order) {
        return switch(order) {
            case 0 -> BRONZE;
            case 1 -> SILVER;
            case 2 -> GOLD;
            default -> null;
        };
    }

}
