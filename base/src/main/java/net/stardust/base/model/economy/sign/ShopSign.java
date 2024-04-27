package net.stardust.base.model.economy.sign;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.stardust.base.model.Identifier;
import net.stardust.base.model.economy.wallet.Money;
import net.stardust.base.utils.persistence.DataManager;

public final class ShopSign {
    
    private ShopSign() {}

    public static String toItemString(ItemStack stack) {
        return itemNameString(stack) + "x" + stack.getAmount();
    }

    public static String itemNameString(ItemStack stack) {
        Component displayName = stack.getItemMeta().displayName();
        String name;
        if(displayName != null && displayName instanceof TextComponent text) {
            name = text.content();
        } else {
            name = stack.getType().toString();
        }
        return name;
    }

    public static Component toItemComponent(ItemStack stack) {
        return itemNameComponent(stack).append(Component.text("x", NamedTextColor.LIGHT_PURPLE))
            .append(Component.text(stack.getAmount(), NamedTextColor.DARK_GREEN));
    }

    public static Component itemNameComponent(ItemStack stack) {
        return Component.text(itemNameString(stack), NamedTextColor.GREEN);
    }

    public static SignUnitData readSign(Sign sign) {
        try {
            DataManager<Sign> dataManager = new DataManager<>(sign);
            SignCaster caster = new SignCaster();
            return caster.cast(dataManager);
        } catch(Exception e) {
            return null;
        }
    }

    public static List<Component> readSignToMessages(Sign sign) {
        SignUnitData unitData = readSign(sign);
        if(unitData != null) {
            SignShopData shopData = unitData.shopData();
            List<Component> messages = new ArrayList<>(4);
            messages.add(Component.translatable("shop.sign.presentation.header", NamedTextColor.GREEN));
            Identifier<?> identifier = shopData.getIdentifier();
            Component name = identifier.getComponentName().color(NamedTextColor.BLUE);
            messages.add(Component.translatable("shop.sign.presentation.owner", NamedTextColor.GREEN, name));
            messages.add(Component.translatable("shop.sign.presentation.item", NamedTextColor.GREEN, toItemComponent(shopData.getItem())));
            Money buy = shopData.getBuy(), sell = shopData.getSell();
            if(buy != null) {
                messages.add(Component.translatable("shop.sign.presentation.buy-price", NamedTextColor.GREEN, buy.toComponent()));
            }
            if(sell != null) {
                messages.add(Component.translatable("shop.sign.presentation.sell-price", NamedTextColor.GREEN, sell.toComponent()));
            }
            return messages;
        }
        return null;
    }

    public static void writeSignText(String ownerName, Sign sign, SignShopData data) {
        SignSide side = sign.getSide(Side.FRONT);
        TextColor operationColor = NamedTextColor.AQUA;

        side.line(0, Component.text(ownerName, NamedTextColor.BLUE));
        side.line(1, toItemComponent(data.getItem()));
        
        Money buy = data.getBuy();
        int line = 2;
        if(buy != null) {
            side.line(line, Component.text("B ", operationColor).append(buy.toComponent()));
            line++;
        }
        Money sell = data.getSell();
        if(sell != null) {
            side.line(line, Component.text("S ", operationColor).append(sell.toComponent()));
        }
        sign.update();
    }

}
