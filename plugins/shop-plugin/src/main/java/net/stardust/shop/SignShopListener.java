package net.stardust.shop;

import static net.kyori.adventure.text.Component.translatable;

import java.util.function.BiConsumer;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import io.papermc.paper.event.player.PlayerOpenSignEvent;
import io.papermc.paper.event.player.PlayerOpenSignEvent.Cause;
import net.kyori.adventure.text.format.NamedTextColor;
import net.stardust.base.ServerIdentifier;
import net.stardust.base.events.BaseListener;
import net.stardust.base.model.Identifier;
import net.stardust.base.model.IllegalIdentifierException;
import net.stardust.base.model.economy.sign.ShopSign;
import net.stardust.base.model.economy.sign.SignUnitData;
import net.stardust.base.model.economy.transaction.operation.OperationFailedException;
import net.stardust.base.model.user.PlayerIdentifier;
import net.stardust.base.utils.Throwables;
import net.stardust.base.utils.persistence.DataManager;
import net.stardust.base.utils.persistence.NonRepresentativeDataException;

@BaseListener
public class SignShopListener implements Listener {

    private SignShopService service = SignShopService.INSTANCE;
    
    @EventHandler
    public void onCreateShopSign(PlayerInteractEvent event) {
        Block clickedBlock = event.getClickedBlock();
        if(event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() != EquipmentSlot.HAND 
            || !(clickedBlock.getState() instanceof Sign sign) || service.isShopSign(sign)) {
            return;
        }
        Player player = event.getPlayer();
        DataManager<Sign> signManager = new DataManager<>(sign);
        Identifier<?> identifier = signManager.readObject("stardust:block_owner", Identifier.class);
        if(identifier == null) {
            player.sendMessage(translatable("block.option-disabled-for-public-blocks", NamedTextColor.RED));
            return;
        }
        if(identifier instanceof ServerIdentifier) {
            if(!player.isOp()) {
                player.sendMessage(translatable("block.owner.not-owner", NamedTextColor.RED));
                return;
            }
        } else if(identifier instanceof PlayerIdentifier playerIdentifier) {
            if(!player.getUniqueId().equals(playerIdentifier.getId())) {
                player.sendMessage(translatable("block.owner.not-owner", NamedTextColor.RED));
                return;
            }
        } else {
            player.sendMessage(translatable("block.could-not-identify", NamedTextColor.RED));
            return;
        }
        if(!service.isInConfigState(player)) {
            return;
        }
        try {
            service.createShopSign(player, sign);
        } catch(NotInConfigStateException | IllegalIdentifierException e) {
            Throwables.send(e).printStackTrace();
            return;
        } catch(NotAChestException e) {
            player.sendMessage(translatable("shop.sign.not-a-chest", NamedTextColor.RED));
            return;
        }
        player.sendMessage(translatable("shop.sign.configured", NamedTextColor.GREEN));
    }

    @EventHandler
    public void onEditSign(PlayerOpenSignEvent event) {
        if(event.getCause() == Cause.INTERACT) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBuy(PlayerInteractEvent event) {
        doOperation(event, Action.RIGHT_CLICK_BLOCK, (player, sign) -> behavior(service::buy, player, sign, true), "buy");
    }

    @EventHandler
    public void onSell(PlayerInteractEvent event) {
        doOperation(event, Action.LEFT_CLICK_BLOCK, (player, sign) -> behavior(service::sell, player, sign, false), "sell");
    }

    private void doOperation(PlayerInteractEvent event, Action action, BiConsumer<Player, Sign> serviceConsumer, String op) {
        Player player = event.getPlayer();
        if(!player.isSneaking() || event.getAction() != action || event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        Block clickedBlock = event.getClickedBlock();
        if(!(clickedBlock.getState() instanceof Sign sign) || service.isInConfigState(player)) {
            return;
        }
        SignUnitData unitData = ShopSign.readSign(sign);
        if(unitData == null) {
            return;
        }
        if(player.getUniqueId().equals(unitData.shopData().getIdentifier().getId())) {
            player.sendMessage(translatable("transaction." + op + ".not-yourself", NamedTextColor.RED));
            return;
        }
        serviceConsumer.accept(player, sign);
    }

    private void behavior(ServiceOperator operator, Player player, Sign sign, boolean isBuyer) {
        try {
            operator.operate(player, sign);
        } catch(NonRepresentativeDataException e) {
            Throwables.sendAndThrow(e);
        } catch(OperationFailedException e) {
            player.sendMessage(e.getDefaultFailMessage(isBuyer));
        }
    }

    @FunctionalInterface
    private interface ServiceOperator {
        void operate(Player player, Sign sign) throws NonRepresentativeDataException, OperationFailedException;
    }

}
