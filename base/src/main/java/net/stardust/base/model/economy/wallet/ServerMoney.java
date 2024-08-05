package net.stardust.base.model.economy.wallet;

import java.io.ObjectStreamException;
import java.math.BigInteger;

import net.stardust.base.model.economy.MonetaryEntity;

/**
 * Represents the server money, which is infinity for every currency.
 * Also, the server money class is immutable and only accessed via singletons,
 * though it is serializable. The numeric value of this class should not be
 * used.
 * 
 * @see Money
 * 
 * @author Sergio Luis
 */
public final class ServerMoney extends Money {

    /**
     * The bronze currency server money.
     */
    public static final ServerMoney BRONZE = new ServerMoney(Currency.BRONZE);

    /**
     * The silver currency server money.
     */
    public static final ServerMoney SILVER = new ServerMoney(Currency.SILVER);

    /**
     * The gold currency server money.
     */
    public static final ServerMoney GOLD = new ServerMoney(Currency.GOLD);
    
    private ServerMoney(Currency currency) {
        super(currency);
    }

    @Override
    public void add(BigInteger value) {}

    @Override
    public void subtract(BigInteger value) {}

    @Override
    public boolean isSubtractionPossible(BigInteger value) {
        return true;
    }

    @Override
    public void setValue(BigInteger value) {}
    
    @Override
    public int compareTo(MonetaryEntity monetaryEntity) {
        return 1;
    }

    /**
     * Throws a {@link CloneNotSupportedException}. {@link ServerMoney} can't
     * be cloned.
     */
    @Override
    public Money clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("ServerMoney cannot be clonned");
    }

    private Object readResolve() throws ObjectStreamException {
        if (equals(BRONZE))
            return BRONZE;
        if (equals(SILVER))
            return SILVER;
        if (equals(GOLD))
            return GOLD;
        throw new Error("Deserialized SeverMoney object is not one of the singletons");
    }

    @Override
    public boolean equals(Object o) {
        if(o == null) return false;
        if(o == this) return true;
        if(o instanceof ServerMoney money) {
            return currency.equals(money.currency);
        }
        return false;
    }

    @Override
    public int hashCode() {
        // As this class has only singleton instances, keep hash code computation simple.
        return currency.getOrder() + 1;
    }

}
