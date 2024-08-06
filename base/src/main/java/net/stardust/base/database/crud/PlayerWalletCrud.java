package net.stardust.base.database.crud;

import java.math.BigInteger;
import java.util.UUID;

import net.stardust.base.model.economy.wallet.Currency;
import net.stardust.base.model.economy.wallet.Money;
import net.stardust.base.model.economy.wallet.PlayerWallet;

public final class PlayerWalletCrud extends Crud<UUID, PlayerWallet> {

    public PlayerWalletCrud() {
        super(PlayerWallet.class);
    }

    public BigInteger getBronze(UUID id) {
        return getOrThrow(id).getBronze().getValue();
    }

    public boolean addBronze(UUID id, BigInteger bronze) {
        return updateBronze(id, getBronze(id).add(bronze));
    }

    public boolean subtractBronze(UUID id, BigInteger bronze) {
        return updateBronze(id, getBronze(id).subtract(bronze));
    }

    public boolean updateBronze(UUID id, BigInteger bronze) {
        return updateMoney(id, new Money(Currency.BRONZE, bronze));
    }

    public BigInteger getSilver(UUID id) {
        return getOrThrow(id).getSilver().getValue();
    }
    
    public boolean addSilver(UUID id, BigInteger silver) {
        return updateSilver(id, getSilver(id).add(silver));
    }
    
    public boolean subtractSilver(UUID id, BigInteger silver) {
        return updateSilver(id, getSilver(id).subtract(silver));
    }
    
    public boolean updateSilver(UUID id, BigInteger silver) {
        return updateMoney(id, new Money(Currency.SILVER, silver));
    }

    public BigInteger getGold(UUID id) {
        return getOrThrow(id).getGold().getValue();
    }
    
    public boolean addGold(UUID id, BigInteger gold) {
        return updateGold(id, getGold(id).add(gold));
    }
    
    public boolean subtractGold(UUID id, BigInteger gold) {
        return updateGold(id, getGold(id).subtract(gold));
    }
    
    public boolean updateGold(UUID id, BigInteger gold) {
        return updateMoney(id, new Money(Currency.GOLD, gold));
    }

    public boolean updateMoney(UUID id, Money money) {
        PlayerWallet wallet = getOrThrow(id);
        wallet.setMoney(money);
        return update(wallet);
    }

}
