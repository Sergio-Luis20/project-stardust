package net.stardust.base.model.economy.wallet;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Objects;

public class Wallet implements Serializable, Cloneable {
    
    protected Money bronze, silver, gold;

    public Wallet() {
        this(0, 0, 0);
    }

    public Wallet(long bronze, long silver, long gold) {
        this(BigInteger.valueOf(bronze), BigInteger.valueOf(silver), BigInteger.valueOf(gold));
    }

    public Wallet(BigInteger bronze, BigInteger silver, BigInteger gold) {
        this.bronze = new Money(Currency.BRONZE, bronze);
        this.silver = new Money(Currency.SILVER, silver);
        this.gold = new Money(Currency.GOLD, gold);
    }

    public Money getBronze() {
        return bronze;
    }

    public Money getSilver() {
        return silver;
    }

    public Money getGold() {
        return gold;
    }

    public Money getMoney(Currency currency) {
        return switch(currency) {
            case BRONZE -> bronze;
            case SILVER -> silver;
            case GOLD -> gold;
            default -> null;
        };
    }

    public void setMoney(Money money) {
        switch(money.getCurrency()) {
            case BRONZE -> this.bronze = money;
            case SILVER -> this.silver = money;
            case GOLD -> this.gold = money;
        }
    }

    @Override
    public Wallet clone() {
        return new Wallet(bronze.getValue(), silver.getValue(), gold.getValue());
    }

    @Override
    public boolean equals(Object o) {
        if(o == null) {
            return false;
        }
        if(o == this) {
            return true;
        }
        if(o instanceof Wallet wallet) {
            return bronze.equals(wallet.bronze) && silver.equals(wallet.silver) && gold.equals(wallet.gold);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(bronze, silver, gold);
    }

}
