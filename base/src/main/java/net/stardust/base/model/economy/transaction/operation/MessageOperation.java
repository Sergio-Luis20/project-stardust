package net.stardust.base.model.economy.transaction.operation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.stardust.base.model.economy.transaction.Transaction;
import net.stardust.base.utils.message.Messageable;
import net.stardust.base.utils.message.Messager;
import net.stardust.base.utils.plugin.PluginConfig;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MessageOperation implements Operation {

    private static final Messager messager = PluginConfig.get().getPlugin().getMessager();

    protected Component buyerMessage, sellerMessage;

    @Override
    public void execute(Transaction transaction) throws OperationFailedException {
        var pair = transaction.getNegotiators();
        if (pair.getBuyer() instanceof Messageable msg && buyerMessage != null) {
            messager.message(msg, buyerMessage);
        }
        if (pair.getSeller() instanceof Messageable msg && sellerMessage != null) {
            messager.message(msg, sellerMessage);
        }
    }
    
}
