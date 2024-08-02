package net.stardust.base.model.economy.transaction.operation;

import java.util.Objects;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.stardust.base.model.economy.transaction.Transaction;
import net.stardust.base.utils.Messageable;
import net.stardust.base.utils.Messager;
import net.stardust.base.utils.plugin.PluginConfig;

@Getter
@Setter
public class MessageOperation implements Operation {

    private static final Messager messager = PluginConfig.get().getPlugin().getMessager();

    protected Component buyerMessage, sellerMessage;

    public MessageOperation(Component buyerMessage, Component sellerMessage) {
        this.buyerMessage = Objects.requireNonNull(buyerMessage, "buyerMessage");
        this.sellerMessage = Objects.requireNonNull(sellerMessage, "sellerMessage");
    }

    @Override
    public void execute(Transaction transaction) throws OperationFailedException {
        var pair = transaction.getNegotiators();
        if (pair.getBuyer() instanceof Messageable msg) {
            messager.message(msg, buyerMessage);
        }
        if (pair.getSeller() instanceof Messageable msg) {
            messager.message(msg, sellerMessage);
        }
    }
    
}
