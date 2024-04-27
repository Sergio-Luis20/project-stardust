package net.stardust.base.model.economy.item;

import java.math.BigInteger;
import java.util.Objects;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.stardust.base.model.economy.MonetaryEntity;
import net.stardust.base.model.economy.wallet.Currency;
import net.stardust.base.utils.persistence.DataCaster;
import net.stardust.base.utils.persistence.DataManager;
import net.stardust.base.utils.persistence.NonRepresentativeDataException;

@Getter
@EqualsAndHashCode
public class Check implements MonetaryEntity {

    private static DataCaster<Check, ItemMeta> dataCaster = new CheckDataCaster();

    private UUID id;
    private MonetaryEntity money;
    private ItemStack check;

    public Check(UUID id, MonetaryEntity money) {
        this.id = Objects.requireNonNull(id, "id");
        this.money = Objects.requireNonNull(money, "money");
        check = new ItemStack(Material.PAPER);
        ItemMeta meta = check.getItemMeta();
        dataCaster.record(this, new DataManager<>(meta));
        check.setItemMeta(meta);
    }

    public Check(ItemStack check) throws NonRepresentativeDataException {
        Check obj = dataCaster.cast(new DataManager<>(check.getItemMeta()));
        this.money = obj.getMoney();
        this.id = obj.getId();
        this.check = obj.getCheck();
    }

    @Override
    public Currency getCurrency() {
        return money.getCurrency();
    }

    @Override
    public BigInteger getValue() {
        return money.getValue();
    }

    public ItemStack getItemCopy() {
        return check.clone();
    }
    
}
