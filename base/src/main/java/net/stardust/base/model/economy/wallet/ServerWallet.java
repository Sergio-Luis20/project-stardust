package net.stardust.base.model.economy.wallet;

import java.io.ObjectStreamException;

public class ServerWallet extends Wallet {

    public static final ServerWallet INSTANCE = new ServerWallet();
    
    private ServerWallet() {
        bronze = ServerMoney.BRONZE;
        silver = ServerMoney.SILVER;
        gold = ServerMoney.GOLD;
    }

    private Object readResolve() throws ObjectStreamException {
        return INSTANCE;
    }

}
