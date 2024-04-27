package net.stardust.base.model.economy.wallet;

import java.io.ObjectStreamException;
import java.math.BigInteger;

public final class ServerMoney extends Money {

    static final ServerMoney BRONZE = new ServerMoney(Currency.BRONZE);
    static final ServerMoney SILVER = new ServerMoney(Currency.SILVER);
    static final ServerMoney GOLD = new ServerMoney(Currency.GOLD);
    
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
    public int compareTo(Money money) {
        return 1;
    }

    @Override
    public Money clone() {
        return new ServerMoney(currency);
    }

    private Object readResolve() throws ObjectStreamException {
        if(equals(BRONZE)) return BRONZE;
        if(equals(SILVER)) return SILVER;
        if(equals(GOLD)) return GOLD;
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
        return currency.hashCode();
    }

}
