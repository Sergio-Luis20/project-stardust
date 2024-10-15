package net.stardust.base.utils.world;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.StandardException;
import org.bukkit.event.Listener;

@Getter
@Setter
@AllArgsConstructor
@StandardException
public class NotWorldListenerException extends RuntimeException {

    private Listener listener;

}
