package net.stardust.base.utils.world;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.StandardException;
import org.bukkit.World;

@Getter
@AllArgsConstructor
@StandardException
public class WorldInUseException extends RuntimeException {

    private World world;

}
