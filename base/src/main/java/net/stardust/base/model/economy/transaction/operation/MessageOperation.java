package net.stardust.base.model.economy.transaction.operation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.stardust.base.model.economy.transaction.Transaction;
import net.stardust.base.utils.Messageable;

@Getter
@Setter
@AllArgsConstructor
public class MessageOperation implements Operation {

    @NonNull
    protected Component buyerMessage, sellerMessage;

    @Override
    public void execute(Transaction transaction) throws OperationFailedException {
        var pair = transaction.getNegotiators();
        if(pair.getBuyer() instanceof Messageable msg) {
            msg.sendMessage(buyerMessage);
        }
        if(pair.getSeller() instanceof Messageable msg) {
            msg.sendMessage(sellerMessage);
        }
    }
    
}
