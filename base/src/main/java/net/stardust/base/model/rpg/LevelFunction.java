package net.stardust.base.model.rpg;

import java.io.Serializable;
import java.util.function.Function;

@FunctionalInterface
public interface LevelFunction extends Serializable, Function<Integer, Long> {

}
