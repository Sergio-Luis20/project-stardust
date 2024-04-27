package net.stardust.base.model.economy.sign;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.io.Serializable;

import org.bukkit.inventory.ItemStack;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.stardust.base.model.Identifier;
import net.stardust.base.model.economy.wallet.Money;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class SignShopData implements Serializable {
    
    private final Identifier<?> identifier;
    private transient ItemStack item;
    private Money buy, sell;

    @Serial
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeObject(item.serializeAsBytes());
    }

    @Serial
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        item = ItemStack.deserializeBytes((byte[]) in.readObject());
    }

}
