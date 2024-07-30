package net.stardust.channels;

import java.io.IOException;
import java.io.Serializable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import br.sergio.comlib.Communication;
import br.sergio.comlib.ConnectionException;
import br.sergio.comlib.Request;
import br.sergio.comlib.RequestListener;
import br.sergio.comlib.RequestMapper;
import br.sergio.comlib.Response;
import br.sergio.comlib.ResponseStatus;
import br.sergio.utils.Pair;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.stardust.base.BasePlugin;
import net.stardust.base.model.channel.Ad;
import net.stardust.base.model.channel.Channel;
import net.stardust.base.model.channel.ChannelStatus;
import net.stardust.base.model.channel.Global;
import net.stardust.base.utils.StardustThreads;
import net.stardust.base.utils.Throwables;
import net.stardust.base.utils.database.crud.ChannelStatusCrud;

public class ChannelsPlugin extends BasePlugin implements Listener, RequestMapper {

    private Set<UUID> loggedPlayers;
    private RequestListener listener;
    private String doctorToken;
    @Getter
    private long discordChannelId;
    private JDA jda;
    private DiscordBridge bridge;
    private Pair<Guild, TextChannel> jdaInfo;

    @Getter
    private Ad ad;

    @Getter
    private Global global;

    @Getter
    private Set<Channel> channels;
    @Getter
    private Set<UUID> discordParticipants;

    @Override
    public void onLoad() {
    	super.onLoad();
    }
    
    @Override
    public void onEnable() {
        Logger log = getLogger();
        loggedPlayers = Collections.synchronizedSet(new HashSet<>());
        channels = Collections.synchronizedSet(new HashSet<>());
        channels.add(ad = new Ad(this));
        channels.add(global = new Global(this));
        log.info("Chamando super.onEnable()");
        super.onEnable();
        ad.load();
        global.load();
        try {
            listener = Communication.newRequestListener(getId(), this);
            listener.start();
        } catch (ConnectionException e) {
            Throwables.sendAndThrow(e);
        }
        log.info("Carregando dados da config");
        FileConfiguration config = getConfig();
        discordParticipants = Collections.synchronizedSet(new HashSet<>(config
            .getStringList("discord-participants").stream().map(UUID::fromString).toList()));
        doctorToken = config.getString("discord-bot-token");
        discordChannelId = config.getLong("discord-channel-id");
        if(doctorToken == null) {
            log.severe("""
                O token do Doutor na config não foi especificado. \
                Criando plugin sem conexão com o discord
                   \s""");
        } else {
            bridge = new DiscordBridge(this);
            Thread jdaStarter = Thread.ofPlatform().daemon().name("JDADaemonThreadStarter").start(() -> {
                log.info("Construindo JDA");
                jda = JDABuilder.createDefault(doctorToken).setEnabledIntents(Arrays
                    .asList(GatewayIntent.values()))
                    .addEventListeners(bridge)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .enableCache(CacheFlag.EMOJI)
                    .enableCache(CacheFlag.ONLINE_STATUS)
                    .build();
                try {
                    log.info("Aguardando ready");
                    jda.awaitReady();
                    log.info("JDA construído");
                } catch (InterruptedException e) {
                    Throwables.sendAndThrow(e);
                }
            });
            try {
                jdaStarter.join();
            } catch(InterruptedException e) {
                Throwables.sendAndThrow(e);
            }
            jdaInfo = bridge.setJDA(jda);
            getServer().getPluginManager().registerEvents(bridge, this);
        }
        loadOnlinePlayers();
    }

    @Override
    public void onDisable() {
        Logger log = getLogger();
        log.info("Salvando usuários do discord na config");
        getConfig().set("discord-participants", discordParticipants.stream()
            .map(UUID::toString).toList());
        saveConfig();
        log.info("Desregistrando discord bridge");
        HandlerList.unregisterAll(bridge);
        log.info("Desligando JDA");
        try {
            if(jda != null) {
                jda.shutdown();
                if(!jda.awaitShutdown(Duration.ofSeconds(5))) {
                    jda.shutdownNow();
                    jda.awaitShutdown();
                }
            }
            log.info("JDA desligado");
        } catch(InterruptedException e) {
            log.warning("Não foi possível fechar o JDA propriamente");
            Throwables.send(e).printStackTrace();
        }
        log.info("Chamando super.onDisable()");
        super.onDisable();
        log.info("Cancelando bukkit tasks do plugin");
        Bukkit.getScheduler().cancelTasks(this);
        log.info("Fechando request listener");
        try {
            listener.close();
            log.info("Request listener fechado");
        } catch(IOException e) {
            log.warning("Não foi possível fechar o request listener propriamente");
            Throwables.send(e).printStackTrace();
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        loggedPlayers.remove(event.getPlayer().getUniqueId());
    }

    @Override
    public Response<? extends Serializable> handle(Request<? extends Serializable> request) {
        ChannelStatus status = (ChannelStatus) request.getContent().get();
        channels.forEach(channel -> {
            if(status.isChannelActivated(channel.getClass().getName(), "status")) {
                channel.addParticipant(StardustThreads.call(this, () -> Bukkit.getPlayer(status.getId())));
            }
        });
        return Response.emptyResponse(ResponseStatus.OK);
    }

    public boolean isPropertyActivated(UUID playerId, String channelClassName, String propertyName) {
        ChannelStatusCrud crud = new ChannelStatusCrud();
        ChannelStatus status = crud.getOrThrow(playerId);
        return status.isChannelActivated(channelClassName, propertyName);
    }

    public Channel getChannel(String name) {
        synchronized(channels) {
            for(Channel channel : channels) {
                if(channel.getName().equals(name)) {
                    return channel;
                }
            }
        }
        return null;
    }

    private void loadOnlinePlayers() {
        List<UUID> keys = Bukkit.getOnlinePlayers().stream().map(Player::getUniqueId).collect(Collectors.toList());
        ChannelStatusCrud crud = new ChannelStatusCrud();
        List<ChannelStatus> statusList = crud.getAll((ArrayList<UUID>) keys);
        synchronized(channels) {
            for(ChannelStatus status : statusList) {
                Player player = Bukkit.getPlayer(status.getId());
                for(Channel channel : channels) {
                    if(status.isChannelActivated(channel.getClass().getName(), "status")) {
                        channel.addParticipant(player);
                    }
                }
            }
        }
    }

    public JDA getJDA() {
        return jda;
    }

    public Pair<Guild, TextChannel> getJDAInfo() {
        return jdaInfo;
    }

}
