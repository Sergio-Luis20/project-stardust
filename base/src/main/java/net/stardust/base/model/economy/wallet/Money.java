package net.stardust.base.model.economy.wallet;

import java.beans.ConstructorProperties;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Locale;
import java.util.Objects;

import org.bukkit.entity.Player;

import com.fasterxml.jackson.annotation.JsonCreator;

import lombok.EqualsAndHashCode;
import net.kyori.adventure.text.Component;
import net.stardust.base.model.economy.MonetaryEntity;

@EqualsAndHashCode
public sealed class Money implements Serializable, Cloneable, MonetaryEntity, Comparable<Money> permits ServerMoney {
    
    public static final String REGEX = "\\A\\d+[BSGbsg]\\z";

    protected Currency currency;
    private BigInteger value;

    public Money(Currency currency) {
        this(currency, 0);
    }

    public Money(Currency currency, long value) {
        this(currency, BigInteger.valueOf(value));
    }

    @JsonCreator
    @ConstructorProperties({"currency", "value"})
    public Money(Currency currency, BigInteger value) {
        this.currency = Objects.requireNonNull(currency);
        setValue(value == null ? BigInteger.ZERO : value);
    }

    public void add(BigInteger value) {
        setValue(this.value.add(value));
    }

    public void subtract(BigInteger value) {
        setValue(this.value.subtract(value));
    }

    public boolean isSubtractionPossible(BigInteger value) {
        return this.value.subtract(value).signum() >= 0;
    }

    public void setValue(BigInteger value) {
        this.value = value.signum() >= 0 ? value : BigInteger.ZERO;
    }

    @Override
    public Money clone() {
        return new Money(currency, value);
    }

    @Override
    public int compareTo(Money money) {
        int compareOrder = currency.getOrder() - money.currency.getOrder();
        return compareOrder == 0 ? value.compareTo(money.value) : compareOrder;
    }

    @Override
    public String toString() {
        return value.toString() + currency.getDefaultSymbol();
    }

    public String toString(Locale locale) {
        return value.toString() + currency.getSymbol(locale);
    }

    public String toString(Player player) {
        return toString(player.locale());
    }

    public Component toComponent() {
        return Component.text(value.toString(), currency.getTextColor()).append(currency.getSymbolAsComponent());
    }

    public static Money valueOf(String money) {
        if(!money.matches(REGEX)) {
            throw new IllegalArgumentException("must match regex: " + REGEX);
        }
        int symbolIndex = money.length() - 1;
        char symbol = money.charAt(symbolIndex);
        Currency currency = Currency.fromDefaultSymbol(symbol);
        String valueString = money.substring(0, symbolIndex);
        BigInteger value = new BigInteger(valueString);
        return new Money(currency, value);
    }

    public BigInteger getValue() {
        return value;
    }

    public Currency getCurrency() {
        return currency;
    }

}
