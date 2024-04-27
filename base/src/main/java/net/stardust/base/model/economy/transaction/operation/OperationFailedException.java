package net.stardust.base.model.economy.transaction.operation;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.StandardException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@Getter
@Setter
@StandardException
public class OperationFailedException extends Exception {
    
    private String failKey;
    private Component[] args;

    public static OperationFailedException fromKey(String failKey) {
        var exception = new OperationFailedException();
        exception.setFailKey(failKey);
        return exception;
    }

    public static OperationFailedException withArgs(String failKey, Component... args) {
        var exception = fromKey(failKey);
        exception.setArgs(args);
        return exception;
    }

    public Component getDefaultFailMessage(boolean buy) {
        Component[] args = getArgs();
        String key = "transaction." + (buy ? "buy" : "sell") + ".fail-key." + getFailKey();
        Component failMessage;
        if(args != null) {
            failMessage = Component.translatable(key, NamedTextColor.RED, args);
        } else {
            failMessage = Component.translatable(key, NamedTextColor.RED);
        }
        return failMessage;
    }

}
