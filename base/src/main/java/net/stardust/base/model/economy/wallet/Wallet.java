package net.stardust.base.model.economy.wallet;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Objects;

import net.stardust.base.model.economy.Cash;

/**
 * Represents a wallet in Stardust server. A wallet stores 3 different
 * values, one for every instance of {@link Currency}.
 * 
 * @author Sergio Luis
 */
public class Wallet implements Serializable, Cloneable, Cash {
    
    protected Money bronze, silver, gold;

    /**
     * Constructs a new empty {@link Wallet} (values bronze, silver and gold all
     * initialized as 0).
     */
    public Wallet() {
        this(0, 0, 0);
    }

    /**
     * Constructs a new {@link Wallet} with the long values passed as parameters.
     * If a value is negative, it will be replaced by {@code 0}.
     * 
     * @param bronze the bronze value
     * @param silver the silver value
     * @param gold the gold value
     */
    public Wallet(long bronze, long silver, long gold) {
        this(BigInteger.valueOf(bronze), BigInteger.valueOf(silver), BigInteger.valueOf(gold));
    }

    /**
     * Constructs a new {@link Wallet} with the {@link BigInteger} values
     * passed as parameters. If a value is null or negative it will be
     * replaced by {@code 0}.
     * 
     * @see Money#Money(Currency, BigInteger)
     * @param bronze the bronze value
     * @param silver the silver value
     * @param gold the gold value
     */
    public Wallet(BigInteger bronze, BigInteger silver, BigInteger gold) {
        this.bronze = new Money(Currency.BRONZE, bronze);
        this.silver = new Money(Currency.SILVER, silver);
        this.gold = new Money(Currency.GOLD, gold);
    }

    /**
     * Returns the bronze {@link Money} instance.
     * 
     * @return the bronze money instance
     */
    public Money getBronze() {
        return bronze;
    }

    /**
     * Sets the bronze {@link Money} value. Check {@link Money#setValue(BigInteger)}
     * documentation for implementation details.
     * 
     * @see Money#setValue(BigInteger)
     * @param bronze the new bronze value
     */
    public void setBronze(BigInteger bronze) {
        this.bronze.setValue(bronze);
    }

    /**
     * Returns the silver {@link Money} instance.
     * 
     * @return the silver money instance
     */
    public Money getSilver() {
        return silver;
    }

    /**
     * Sets the silver {@link Money} value. Check {@link Money#setValue(BigInteger)}
     * documentation for implementation details.
     * 
     * @see Money#setValue(BigInteger)
     * @param silver the new silver value
     */
    public void setSilver(BigInteger silver) {
        this.silver.setValue(silver);
    }

    /**
     * Returns the gold {@link Money} instance.
     * 
     * @return the gold money instance
     */
    public Money getGold() {
        return gold;
    }

    /**
     * Sets the gold {@link Money} value. Check {@link Money#setValue(BigInteger)}
     * documentation for implementation details.
     * 
     * @see Money#setValue(BigInteger)
     * @param gold the new gold value
     */
    public void setGold(BigInteger gold) {
        this.gold.setValue(gold);
    }

    /**
     * Returns the {@link Money} instance of the currency parameter.
     * If currency is null, this method returns null.
     * 
     * @param currency the currency of the requested {@link Money} instance
     * @return the requested money instance
     */
    public Money getMoney(Currency currency) {
        if (currency == null) {
            return null;
        }
        return switch (currency) {
            case BRONZE -> bronze;
            case SILVER -> silver;
            case GOLD -> gold;
        };
    }

    /**
     * Sets the {@link Money} instance based on the currency of the
     * money parameter itself.
     * 
     * @param money the new money instance
     * @throws NullPointerException if money is null
     */
    public void setMoney(Money money) {
        switch (money.getCurrency()) {
            case BRONZE -> this.bronze = money;
            case SILVER -> this.silver = money;
            case GOLD -> this.gold = money;
        }
    }

    @Override
    public Wallet clone() throws CloneNotSupportedException {
        return new Wallet(bronze.getValue(), silver.getValue(), gold.getValue());
    }

    /**
     * Method implementation for {@link Cash}. This allows the wallet itself
     * to be passed as a negotiator of a {@link Transaction}.
     * 
     * @return this object
     */
    @Override
    public Wallet getWallet() {
        return this;
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
