package net.stardust.base.model.terrain;

import lombok.Getter;
import net.stardust.base.model.economy.wallet.Currency;
import net.stardust.base.model.economy.wallet.Money;

@Getter
public enum TerrainSize {

    SMALL(20, new Money(Currency.SILVER, 1000)),
    MEDIUM(100, new Money(Currency.SILVER, 15000)),
    LARGE(500, new Money(Currency.SILVER, 50000)),
    VERY_LARGE(2000, new Money(Currency.SILVER, 120000)),
    ASTRONOMICALLY_LARGE(25000, new Money(Currency.GOLD, 1000));

    private final int radius;
    private final Money price;

    TerrainSize(int radius, Money price) {
        this.radius = radius;
        this.price = price;
    }

}
