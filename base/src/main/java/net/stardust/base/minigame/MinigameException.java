package net.stardust.base.minigame;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.StandardException;

@Getter
@StandardException
@AllArgsConstructor
public class MinigameException extends Exception {

    private Minigame match;

}
