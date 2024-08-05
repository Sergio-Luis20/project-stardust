package net.stardust.base.model.economy.transaction.operation;

import net.kyori.adventure.text.Component;
import net.stardust.base.BasePlugin;
import net.stardust.base.model.economy.transaction.Negotiators;
import net.stardust.base.model.economy.transaction.Transaction;
import net.stardust.base.utils.message.Messageable;
import net.stardust.base.utils.message.Messager;
import net.stardust.base.utils.plugin.PluginConfig;

/**
 * This operation sends messages to a buyer and a seller
 * of a {@link Transaction}. The messages (which are {@link Component}s)
 * can be passed via constructor and via setters and they can be null.
 * If a negotiator is not an implementation of {@link Messageable} or
 * its respective message it null, then it will be ignored. The
 * message sending is done by a {@link Messager} object, which is
 * the same returned by {@link BasePlugin#getMessager()}, using the
 * {@link BasePlugin} instance returned by {@link PluginConfig#getPlugin()}.
 * This is made that way to ensure that the messaging task is done by the
 * Bukkit Main thread, so it is thread-safe in that context. Because of that,
 * if for some reason this class initializes before {@link BasePlugin#onLoad()},
 * the messager used might be null, and that is a bug!
 * 
 * @see Transaction
 * @see Negotiators
 * @see Component
 * @see BasePlugin
 * @see PluginConfig
 * @see Messageable
 * @see Messager
 * @see Operation
 * 
 * @author Sergio Luis
 */
public class MessageOperation implements Operation {

    private static final Messager MESSAGER = PluginConfig.get().getPlugin().getMessager();

    private Component buyerMessage, sellerMessage;

    /**
     * Constructs an empty {@link MessageOperation} with null
     * values to buyer and seller messages.
     * 
     * @see MessageOperation
     */
    public MessageOperation() {
    }

    /**
     * Constructs a {@link MessageOperation} with the messages
     * passed as parameters, which can be null.
     * 
     * @see MessageOperation
     * @param buyerMessage the buyer message
     * @param sellerMessage the seller message
     */
    public MessageOperation(Component buyerMessage, Component sellerMessage) {
        this.buyerMessage = buyerMessage;
        this.sellerMessage = sellerMessage;
    }

    /**
     * Sends messages to a buyer and a seller of a {@link Transaction}. If
     * a negotiator is not instance of {@link Messageable} or its respective
     * message is null, it will be ignored. This method uses a {@link Messager}
     * to ensure that messages will be sent in Bukkit Main thread, so it is
     * thread-safe in that context.
     * 
     * @see Transaction
     * @see Negotiators
     * @see Component
     * @see Messageable
     * @see Messager
     * @param transaction the transaction with the negotiators to
     * send messages
     */
    @Override
    public void execute(Transaction transaction) {
        var pair = transaction.getNegotiators();
        if (pair.getBuyer() instanceof Messageable msg && buyerMessage != null) {
            MESSAGER.message(msg, buyerMessage);
        }
        if (pair.getSeller() instanceof Messageable msg && sellerMessage != null) {
            MESSAGER.message(msg, sellerMessage);
        }
    }

    /**
     * Returns the message that is supposed to be sent
     * to the {@code buyer} of the {@link Transaction}.
     * Can be null.
     * 
     * @return the {@code buyer} message
     */
    public Component getBuyerMessage() {
        return buyerMessage;
    }

    /**
     * Sets the message that is supposed to be sent
     * to the {@code buyer} of the {@link Transaction}.
     * Can be null.
     * 
     * @param buyerMessage the {@code buyer} message
     */
    public void setBuyerMessage(Component buyerMessage) {
        this.buyerMessage = buyerMessage;
    }

    /**
     * Returns the message that is supposed to be sent
     * to the {@code seller} of the {@link Transaction}.
     * Can be null.
     * 
     * @return the {@code seller} message
     */
    public Component getSellerMessage() {
        return sellerMessage;
    }

    /**
     * Sets the message that is supposed to be sent
     * to the {@code seller} of the {@link Transaction}.
     * Can be null.
     * 
     * @param sellerMessage the {@code seller} message
     */
    public void setSellerMessage(Component sellerMessage) {
        this.sellerMessage = sellerMessage;
    }
    
}
