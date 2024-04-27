package net.stardust.base.model.economy;

import java.math.BigInteger;

import net.stardust.base.model.economy.wallet.Currency;

public interface MonetaryEntity {
    
    Currency getCurrency();
    BigInteger getValue();

}
