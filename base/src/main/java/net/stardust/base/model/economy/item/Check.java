package net.stardust.base.model.economy.item;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;

import net.stardust.base.model.economy.MonetaryEntity;
import net.stardust.base.model.economy.wallet.Currency;
import net.stardust.base.model.economy.wallet.Money;
import net.stardust.base.model.user.PlayerIdentifier;
import net.stardust.base.model.user.PlayerIdentifierProvider;
import net.stardust.base.utils.persistence.DataCaster;
import net.stardust.base.utils.persistence.DataManager;
import net.stardust.base.utils.persistence.KeyMapper;
import net.stardust.base.utils.persistence.NonRepresentativeDataException;

public record Check(UUID id, MonetaryEntity money) implements MonetaryEntity, PlayerIdentifierProvider, Serializable {
    
    /**
     * The default {@link KeyMapper} for checks
     */
    public static final KeyMapper KEY_MAPPER = () -> {
        Map<NamespacedKey, Class<?>> map = new HashMap<>();
        map.put(new NamespacedKey("stardust", "check"), Check.class);
        return map;
    };

    /**
     * The default {@link DataCaster} for checks.
     */
    public static final DataCaster<Check, ItemMeta> DATA_CASTER = new DataCaster<>() {
        
        @Override
        public Check cast(DataManager<ItemMeta> dataManager) throws NonRepresentativeDataException {
            if (!KeyMapper.check(KEY_MAPPER, dataManager))
                throw new NonRepresentativeDataException("object does not represent a check");
            return dataManager.readObject("stardust:check", Check.class);
        }

        @Override
        public void record(Check obj, DataManager<ItemMeta> dataManager) {
            dataManager.writeObject("stardust:check", obj);
        }

    };
    
    /**
     * Creates a new {@link Check} instance with the id of the player who
     * generated this check and the monetary quantity that this object will
     * carry. This constructor creates internally a new instance of a
     * {@link Money}, so it is independent of the one passed as parameter.
     * 
     * @param id the id of the player that generated this check
     * @param money the monetary quantity that this check will carry
     * @throws NullPointerException if id or money is null
     */
    public Check(UUID id, MonetaryEntity money) {
        this.id = Objects.requireNonNull(id, "id");
        this.money = new Money(money.getCurrency(), money.getValue());
    }

    /**
     * Returns a new {@link PlayerIdentifier} of the player
     * who created this check.
     * 
     * @see PlayerIdentifier
     * @see PlayerIdentifierProvider
     * @return the identifier of this player who created this check
     */
    @Override
    public PlayerIdentifier getIdentifier() {
        return new PlayerIdentifier(id);
    }

    @Override
    public Currency getCurrency() {
        return money.getCurrency();
    }

    @Override
    public BigInteger getValue() {
        return money.getValue();
    }

    /**
     * Returns the id of this player who created this check.
     * 
     * @return the id of the player who created this check
     */
    @Override
    public UUID id() {
        return id;
    }

    /**
     * Returns the money that this check carries. This creates a
     * new copy of the internal money, so any changes on the returned
     * object will not affect the internal value of this check.
     * 
     * @return
     */
    @Override
    public MonetaryEntity money() {
        try {
            return ((Money) money).clone();
        } catch (CloneNotSupportedException e) {
            throw new Error("could not clone check money", e);
        }
    }

    /**
     * Creates a new {@link ItemStack} representing this check in a material
     * form. This by default uses {@link Material#PAPER} as the material of the
     * item stack.
     * 
     * @see ItemStack
     * @see ItemMeta
     * @see DataManager
     * @see Material#PAPER
     * @see #DATA_CASTER
     * @return the item representation of this check
     */
    public ItemStack getItem() {
        ItemStack check = new ItemStack(Material.PAPER);
        ItemMeta meta = check.getItemMeta();
        DATA_CASTER.record(this, new DataManager<>(meta));
        check.setItemMeta(meta);
        return check;
    }

    /**
     * Creates a new {@link Check} instance from an {@link ItemStack} object
     * that represents a check in its item form following the contract of
     * {@link KeyMapper}. If the passed parameter doesn't represent a check
     * in its item form, a {@link NonRepresentativeDataException} is thrown.
     * 
     * @see ItemStack
     * @see PersistentDataHolder
     * @see PersistentDataContainer
     * @see DataManager
     * @see DataCaster
     * @see KeyMapper
     * @see NonRepresentativeDataException
     * @see Check
     * @param check the check in item form
     * @return a new {@link Check} instance from the item
     * @throws NonRepresentativeDataException if the passed item parameter doesn't
     * represent a check in its item form
     * @throws NullPointerException if check is null
     */
    public static Check of(ItemStack check) throws NonRepresentativeDataException {
        return DATA_CASTER.cast(new DataManager<>(check.getItemMeta()));
    }
    
}
