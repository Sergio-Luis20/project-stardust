package net.stardust.shop;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.stardust.base.ServerIdentifier;
import net.stardust.base.command.BaseCommand;
import net.stardust.base.command.CommandEntry;
import net.stardust.base.command.DirectCommand;
import net.stardust.base.model.Identifier;
import net.stardust.base.model.economy.sign.SignShopData;
import net.stardust.base.model.economy.wallet.Money;
import net.stardust.base.model.user.PlayerIdentifier;

@BaseCommand(value = "configsign", types = Player.class)
public class ConfigSignCommand extends DirectCommand<ShopPlugin> {

    public ConfigSignCommand(ShopPlugin plugin) {
        super(plugin);
    }

    @CommandEntry
    public void enterPlayerState(String arg1, String arg2) {
        enterState(new PlayerIdentifier((Player) sender()), arg1, arg2);
    }
    
    @CommandEntry(value = "server", opOnly = true, oneWordFinalString = true)
    public void enterServerState(String arg1, String arg2) {
        enterState(ServerIdentifier.INSTANCE, arg1, arg2);
    }
    
    @CommandEntry("cancel")
    public void cancelState() {
        var service = SignShopService.INSTANCE;
        Player player = sender();
        if(!service.isInConfigState(player)) {
            player.sendMessage(Component.translatable("shop.sign.not-in-state", NamedTextColor.RED));
            return;
        }
        service.removeState(player);
        player.sendMessage(Component.translatable("shop.sign.state-cancel", NamedTextColor.YELLOW));
    }

    private void enterState(Identifier<?> identifier, String arg1, String arg2) {
        /*
         * Primeiro caso: arg1 é buy ou sell, enquanto arg2 é o preço
         * Segundo caso: arg1 é o preço de compra, arg2 é o preço de venda
         */
        var verification = verify();
        if(verification == null) return;
        var player = verification.player();
        Money arg2Money = null;
        try {
            arg2Money = Money.valueOf(arg2);
        } catch(IllegalArgumentException e) {
            player.sendMessage(Component.translatable("money.format", NamedTextColor.RED));
            return;
        }
        var signData = new SignShopData(identifier);
        signData.setItem(verification.item());
        if(arg1.equalsIgnoreCase("buy")) {
            signData.setBuy(arg2Money);
        } else if(arg1.equalsIgnoreCase("sell")) {
            signData.setSell(arg2Money);
        } else {
            try {
                Money arg1Money = Money.valueOf(arg1);
                signData.setBuy(arg1Money);
                signData.setSell(arg2Money);
            } catch(IllegalArgumentException e) {
                player.sendMessage(Component.translatable("money.format", NamedTextColor.RED));
                return;
            }
        }
        verification.service().putState(player, signData);
        player.sendMessage(Component.translatable("shop.sign.in-state", NamedTextColor.YELLOW, 
            Component.text(SignShopService.CONFIG_TIME, NamedTextColor.AQUA)));
    }

    private ConfigVerification verify() {
        var service = SignShopService.INSTANCE;
        Player player = sender();
        if(service.isInConfigState(player)) {
            player.sendMessage(Component.translatable("shop.sign.already-in-state", NamedTextColor.YELLOW));
            return null;
        }
        var item = player.getInventory().getItemInMainHand();
        if(item == null || item.getType() == Material.AIR) {
            player.sendMessage(Component.translatable("must-hold-item", NamedTextColor.RED));
            return null;
        }
        return new ConfigVerification(service, player, item);
    }

    private static record ConfigVerification(SignShopService service, Player player, ItemStack item) {

    }
    
}
