package net.stardust.base.database.crud;

import java.util.UUID;

import net.stardust.base.model.channel.ChannelStatus;

public class ChannelStatusCrud extends Crud<UUID, ChannelStatus> {
    
    public ChannelStatusCrud() {
        super(ChannelStatus.class);
    }

}
