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
    private Class<? extends Operation> failedOperation;

    public static OperationFailedException fromKey(String failKey) {
        var exception = new OperationFailedException();
        exception.setFailKey(failKey);
        return exception;
    }

    public static OperationFailedException fromKey(String failKey, Class<? extends Operation> failedOperation) {
        var exception = fromKey(failKey);
        exception.setFailedOperation(failedOperation);
        return exception;
    }

    public static OperationFailedException withArgs(String failKey, Component... args) {
        var exception = fromKey(failKey);
        exception.setArgs(args);
        return exception;
    }

    public static OperationFailedException withArgs(String failKey, Class<? extends Operation> failedOperation,
            Component... args) {
        var exception = withArgs(failKey, args);
        exception.setFailedOperation(failedOperation);
        return exception;
    }

    public Component getDirectFailMessage() {
        return getFailMessage(getFailKey());
    }

    public Component getDefaultFailMessage(boolean buy) {
        return getFailMessage("transaction." + (buy ? "buy" : "sell") + ".fail-key." + getFailKey());
    }
    
    private Component getFailMessage(String key) {
        Component[] args = getArgs();
        Component failMessage;
        if (args != null) {
            failMessage = Component.translatable(key, NamedTextColor.RED, args);
        } else {
            failMessage = Component.translatable(key, NamedTextColor.RED);
        }
        return failMessage;
    }

}
