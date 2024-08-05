package net.stardust.base.model.economy.wallet;

import java.io.ObjectStreamException;

/**
 * Represents the wallet of the server, which has infinity money.
 * 
 * @see ServerMoney
 * 
 * @author Sergio Luis
 */
public class ServerWallet extends Wallet {

    /**
     * The {@link ServerWallet} singleton instance.
     */
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
