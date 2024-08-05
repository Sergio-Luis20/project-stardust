package net.stardust.base.model.economy.wallet;

import java.beans.ConstructorProperties;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

import org.bukkit.entity.Player;

import com.fasterxml.jackson.annotation.JsonCreator;

import net.kyori.adventure.text.Component;
import net.stardust.base.model.economy.MonetaryEntity;

/**
 * Represents a money quantity. This is the most common {@link MonetaryEntity}
 * implementation. This class only represents non-negative quantities.
 * 
 * @see ServerMoney
 * 
 * @author Sergio Luis
 */
public sealed class Money implements Serializable, Cloneable, MonetaryEntity permits ServerMoney {
    
    /**
     * <p>Regex used to validate an String representation of a {@link Money}
     * object. The String representation has the same syntax as returned
     * by {@link Money#toString()} and the one used in
     * {@link Money#valueOf(String)}.</p>
     * 
     * Read {@link Money#toString()} documentation to know how this syntax
     * works.
     * 
     * @see Money#toString()
     * @see Money#valueOf(String)
     * @see Money#PATTERN
     */
    public static final String REGEX = "\\A\\d+[BSGbsg]\\z";

    /**
     * A {@link Pattern} object for the {@link #REGEX} static value.
     * 
     * @see #REGEX
     */
    public static final Pattern PATTERN = Pattern.compile(REGEX);

    protected Currency currency;
    private BigInteger value;

    /**
     * Creates a new {@link Money} object with the currency passed as
     * parameter and {@code 0} as its value.
     * 
     * @param currency the currency
     * @throws NullPointerException if currency is null
     */
    public Money(Currency currency) {
        this(currency, 0);
    }

    /**
     * Creates a new {@link Money} object with the currency and the value
     * as long passed as parameters. If value is negative, {@code 0} will be used
     * instead.
     * 
     * @param currency the currency
     * @param value the numeric value
     * @throws NullPointerException if currency is null
     */
    public Money(Currency currency, long value) {
        this(currency, BigInteger.valueOf(value));
    }

    /**
     * Creates a new {@link Money} object with the currency and the value
     * as {@link BigInteger} passed as parameters. If value is negative or null,
     * {@code 0} will be used instead.
     * 
     * @param currency the currency
     * @param value the numeric value
     * @throws NullPointerException if currency is null
     */
    @JsonCreator
    @ConstructorProperties({"currency", "value"})
    public Money(Currency currency, BigInteger value) {
        this.currency = Objects.requireNonNull(currency, "currency");
        setValue(value == null ? BigInteger.ZERO : value);
    }

    /**
     * Adds a numeric quantity to the value. If the result is
     * negative, the value will become {@code 0}.
     * 
     * @param value the numeric quantity to add to the value
     * @throws NullPointerException if the numeric quantity is null
     */
    public void add(BigInteger value) {
        setValue(this.value.add(value));
    }

    /**
     * Subtracts a numeric quantity to the value. If the result is
     * negative, the value will become {@code 0}.
     * 
     * @param value the numeric quantity to subtract from the value
     * @throws NullPointerException if the numeric quantity is null
     */
    public void subtract(BigInteger value) {
        setValue(this.value.subtract(value));
    }

    /**
     * Sets the value of this {@link Money} object. If the parameter
     * is negative, the value will become {@code 0}.
     * 
     * @param value the new numeric value for this {@link Money} object
     * @throws NullPointerException if value is null
     */
    public void setValue(BigInteger value) {
        this.value = value.signum() >= 0 ? value : BigInteger.ZERO;
    }

    /**
     * Returns a shallow copy of this money object. If this
     * object it not an instance of {@link ServerMoney}, this
     * does not throws any exception. The {@code throws} in the
     * method signature is there only to allow subclass manipulation.
     * 
     * @return a shallow copy of this money object
     */
    @Override
    public Money clone() throws CloneNotSupportedException {
        return new Money(currency, value);
    }
    
    /**
     * <p>Returns a String representation of this {@link Money} object following
     * a specific but simple syntax. This syntax consists of a non-negative integer
     * followed by a letter (not case sensitive) which can be B for bronze, S for
     * silver and G for gold. This is also the same syntax that must be used in
     * {@link Money#valueOf(String)}.</p>
     * 
     * Examples:
     * <ul>
     * <li><b>800B</b> translates to <b>800 bronze coins</b></li>
     * <li><b>12345s</b> translates to <b>12345 silver coins</b></li>
     * <li><b>0g</b> translates to <b>0 gold coins</b></li>
     * </ul>
     * 
     * @see Money#valueOf(String)
     * @return a String representation of this {@link Money} object
     */
    @Override
    public String toString() {
        return value.toString() + currency.getDefaultSymbol();
    }

    /**
     * <p>Returns a String representation of this {@link Money} object following
     * the same syntax defined in {@link #toString()} documentation. The difference
     * is that the letter (representing the currency symbol) is translated to the
     * currency name initial letter of the locale passed as parameter.</p>
     * 
     * Read {@link Currency} documentation know about symbols and translations.
     * 
     * @see Currency
     * @see Currency#getSymbol(Locale)
     * @see #toString()
     * @param locale the locale to translate the currency symbol in the String representation
     * of this money object
     * @return the translated String representation of this money object
     */
    public String toString(Locale locale) {
        return value.toString() + currency.getSymbol(locale);
    }

    /**
     * Returns the String representation of this {@link Money} object translated
     * to the locale of the player parameter. Check {@link #toString(Locale)}
     * documentation for more details.
     * 
     * @see #toString(Locale)
     * @param player the player
     * @return the String representation of this money object translated to the
     * player locale
     * @throws NullPointerException if player is null
     */
    public String toString(Player player) {
        return toString(player.locale());
    }

    /**
     * Returns the {@link Component} representation of this {@link Money} with text
     * following the syntax described in {@link #toString()} documentation and
     * the color returned by {@link Currency#getTextColor()}.
     * 
     * @see #toString()
     * @see Component
     * @see Currency#getTextColor()
     * @return the {@link Component} representation of this money object
     */
    public Component toComponent() {
        return Component.text(toString(), currency.getTextColor());
    }

    /**
     * Returns the {@link Component} representation of this {@link Money} with
     * text translated as described in {@link #toString(Locale)} and the color
     * returned by {@link Currency#getTextColor()}.
     * 
     * @see Component
     * @see Currency#getTextColor()
     * @see #toString(Locale)
     * @param locale the locale to where translate the text of the component
     * @return the translated text component
     */
    public Component toComponent(Locale locale) {
        return Component.text(toString(locale), currency.getTextColor());
    }

    /**
     * Returns the {@link Component} representation of this {@link Money} using
     * the locale of the player passed as parameter to translate the text as
     * described in {@link #toComponent(Locale)}.
     * 
     * @see Component
     * @see #toComponent(Locale)
     * @param player the player whose locale will be used for translation
     * @return the translated text component
     */
    public Component toComponent(Player player) {
        return toComponent(player.locale());
    }

    /**
     * <p>Creates a new {@link Money} instance from a String representation that
     * follows the same syntax described in {@link Money#toString()} documentation.
     * If the parameter String doesn't follow that syntax, an {@link IllegalArgumentException}
     * will be thrown.</p>
     * 
     * <p>Two {@link Money} objects returned by this method that used Strings that
     * are equals by {@link String#equalsIgnoreCase(String)} are considered equals
     * by {@link Money#equals(Object)} method.</p>
     * 
     * @param money the String representation of a Money object
     * @return the respective Money object
     * @throws IllegalArgumentException if the parameter String doesn't follow
     * the syntax described in {@link Money#toString()} documentation.
     */
    public static Money valueOf(String money) {
        if (!PATTERN.matcher(money).matches()) {
            throw new IllegalArgumentException("Must match regex: " + REGEX);
        }
        int symbolIndex = money.length() - 1;
        char symbol = money.charAt(symbolIndex);
        Currency currency = Currency.fromDefaultSymbol(symbol);
        String valueString = money.substring(0, symbolIndex);
        BigInteger value = new BigInteger(valueString);
        return new Money(currency, value);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (o == this)
            return true;
        if (o.getClass() == Money.class) {
            Money money = (Money) o;
            return currency.equals(money.currency) && value.equals(money.value);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(currency, value);
    }

    @Override
    public Currency getCurrency() {
        return currency;
    }
    
    @Override
    public BigInteger getValue() {
        return value;
    }

}
