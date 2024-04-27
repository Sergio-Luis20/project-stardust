package net.stardust.authentication.commands;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;

import br.sergio.comlib.Communication;
import br.sergio.comlib.Request;
import br.sergio.comlib.RequestMethod;
import br.sergio.comlib.ResponseStatus;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.stardust.authentication.PasswordEncryption;
import net.stardust.authentication.StardustAuthentication;
import net.stardust.base.command.AsyncCommand;
import net.stardust.base.command.BaseCommand;
import net.stardust.base.command.CommandEntry;
import net.stardust.base.command.SenderType;
import net.stardust.base.model.channel.ChannelPropertiesProvider;
import net.stardust.base.model.channel.ChannelStatus;
import net.stardust.base.model.economy.wallet.PlayerWallet;
import net.stardust.base.model.rpg.RPGPlayer;
import net.stardust.base.model.user.User;
import net.stardust.base.utils.Throwables;
import net.stardust.base.utils.database.crud.ChannelStatusCrud;
import net.stardust.base.utils.database.crud.PlayerWalletCrud;
import net.stardust.base.utils.database.crud.RPGPlayerCrud;
import net.stardust.base.utils.database.crud.UserCrud;
import net.stardust.base.utils.property.Property;

@BaseCommand(value = "register", types = SenderType.PLAYER)
public class RegisterCommand extends AsyncCommand<StardustAuthentication> {

    // Lembrar de enviar um email de confirmação para o jogador.

    private UserCrud userCrud;
    private ChannelStatusCrud channelCrud;
    private RPGPlayerCrud rpgCrud;
    private PlayerWalletCrud walletCrud;

    private String id;

    public RegisterCommand(StardustAuthentication plugin) {
    	super(plugin);
        userCrud = new UserCrud();
        channelCrud = new ChannelStatusCrud();
        rpgCrud = new RPGPlayerCrud();
        walletCrud = new PlayerWalletCrud();
    }
    
    @CommandEntry(types = SenderType.PLAYER, oneWordFinalString = true)
    public void register(String email, String password, String confirmPassword) {
        Player player = sender();
    	UUID playerId = uniqueId(player);
        
        if(!plugin.isWaiting(playerId)) {
            messager.message(player, Component.translatable("authorized", NamedTextColor.RED));
            return;
        }
        messager.message(player, Component.translatable("register.verifying", NamedTextColor.YELLOW));
        if(userCrud.getOrNull(playerId) != null) {
            messager.message(player, Component.translatable("register.already-registered", NamedTextColor.RED));
            return;
        }
        
        int min = plugin.getMinPwLength();
        if(password.length() < min) {
            messager.message(player, Component.translatable("register.min-pw-len", 
                NamedTextColor.RED, Component.text(min, NamedTextColor.RED)));
            return;
        }
        if(!password.equals(confirmPassword)) {
            messager.message(player, Component.translatable("register.pw-dont-match", NamedTextColor.RED));
            return;
        }
        register(player, playerId, email, password);
    }

    private void register(Player player, UUID uid, String email, String password) {
        Logger logger = plugin.getLogger();

        byte[] salt = PasswordEncryption.generateSalt();
        byte[] hash = null;

        try {
            hash = PasswordEncryption.generateHash(password, salt);
        } catch(NoSuchAlgorithmException | InvalidKeySpecException e) {
            logger.log(Level.SEVERE, "Erro ao criptografar uma senha", e);
            Throwables.send(id, e);
        }
        if(hash == null) {
            plugin.unauthorize(player, uid, "error.internal");
            return;
        }
        
        User user = User.builder()
                .id(uid)
                .name(player.getName())
                .email(email)
                .salt(salt)
                .password(hash)
                .registered(System.currentTimeMillis())
                .build();
        ChannelStatus channel = buildChannelStatus(uid);
        RPGPlayer rpgPlayer = new RPGPlayer(uid);
        PlayerWallet wallet = new PlayerWallet(uid);
        Request<ChannelStatus> toCh = Request.newRequest(plugin.getId(), "channels", RequestMethod.POST, channel);
        try {
            if(!userCrud.create(user) || !channelCrud.create(channel) || !rpgCrud.create(rpgPlayer)
                || !walletCrud.create(wallet) || Communication.send(toCh).getStatus() != ResponseStatus.OK) {
                    plugin.unauthorize(player, uid, "register.could-not-register");
                return;
            }
            messager.message(player, Component.translatable("register.success", NamedTextColor.GREEN));
            plugin.authorize(uid);
        } catch(IOException | NoSuchElementException e) {
            plugin.unauthorize(player, uid, "register.could-not-register");
            logger.log(Level.SEVERE, "Exceção ao registrar jogador", e);
        }
    }

    private ChannelStatus buildChannelStatus(UUID uid) {
        ChannelStatus channel = new ChannelStatus(uid);
        ChannelPropertiesProvider.getProperties().forEach((channelName, props) -> channel.getProperties()
                .put(channelName, props.stream().map(propName -> new Property(propName, true)).collect(Collectors.toSet())));
        return channel;
    }

}
