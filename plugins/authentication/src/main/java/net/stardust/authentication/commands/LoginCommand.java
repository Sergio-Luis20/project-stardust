package net.stardust.authentication.commands;

import java.util.Arrays;
import java.util.UUID;
import java.util.logging.Level;

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
import net.stardust.base.model.channel.ChannelStatus;
import net.stardust.base.model.user.User;
import net.stardust.base.utils.Throwables;
import net.stardust.base.utils.database.crud.ChannelStatusCrud;
import net.stardust.base.utils.database.crud.UserCrud;

@BaseCommand(value = "login", types = SenderType.PLAYER)
public class LoginCommand extends AsyncCommand<StardustAuthentication> {

    /*
     * Fazer verificação de dois fatores caso
     * o jogador tenha configurado para receber
     * código por email ou cadastrado algum número
     * de telefone.
     */

    private UserCrud userCrud;
    private ChannelStatusCrud channelCrud;

    public LoginCommand(StardustAuthentication plugin) {
        super(plugin);
        userCrud = new UserCrud();
        channelCrud = new ChannelStatusCrud();
    }
    
    @CommandEntry(types = SenderType.PLAYER, oneWordFinalString = true)
    public void login(String password) {
        Player player = sender();
    	UUID uid = uniqueId(player);
        if(!plugin.isWaiting(uid)) {
            messager.message(player, Component.translatable("authorized", NamedTextColor.RED));
            return;
        }
        User user = userCrud.getOrNull(uid);
        if(user == null) {
            messager.message(player, Component.translatable("login.not-registered", NamedTextColor.RED));
            return;
        }
        login(player, uid, user, password);
    }

    private void login(Player player, UUID uid, User user, String password) {
        byte[] salt = user.getSalt();
        byte[] hash = user.getPassword();
        try {
            if(!Arrays.equals(hash, PasswordEncryption.generateHash(password, salt))) {
                messager.message(player, Component.translatable("login.wrong-pw", NamedTextColor.RED));
                return;
            }
            String id = plugin.getId();
            ChannelStatus channel = channelCrud.getOrThrow(uid);
            Request<ChannelStatus> channelRequest = Request.newRequest(id, "channels", RequestMethod.POST, channel);
            if(Communication.send(channelRequest).getStatus() != ResponseStatus.OK) {
                plugin.unauthorize(player, uid, "error.login");
                return;
            }
            messager.message(player, Component.translatable("login.welcome-back", NamedTextColor.GREEN, Component.text(player.getName(), NamedTextColor.GREEN)));
            plugin.authorize(uid);
        } catch(Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Exceção ao verificar senhas", e);
            plugin.unauthorize(player, uid, "error.internal");
            Throwables.send(plugin.getId(), e);
        }
    }
    
}
