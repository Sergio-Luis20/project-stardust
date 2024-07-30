package net.stardust.base.model.channel;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.StandardException;

@Getter
@Setter
@StandardException
public class ChannelNotFoundException extends ChannelException {
    
    private String channelClassName;

}
