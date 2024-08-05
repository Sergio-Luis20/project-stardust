package net.stardust.base.model.economy.transaction.operation;

import java.util.Objects;

import lombok.experimental.StandardException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * This checked exception is thrown if an {@link Operation}
 * fails. It has some information about the fail translation
 * message key to be used in a {@link TranslatableComponent}
 * and the {@link Operation} object (normally the one that
 * thrown this exception). It is always recommendable passing
 * the failed operation, though it's not mandatory.
 * 
 * @see Operation
 * @see TranslatableComponent
 * 
 * @author Sergio Luis
 */

@StandardException
public class OperationFailedException extends Exception {
    
    private String failKey;
    private Component[] args;
    private Operation failedOperation;

    /**
     * Static factory that constructs an {@link OperationFailedException}
     * with the failed {@link Operation} as parameter. The failed operation
     * can be null, but as said in documentation of {@link OperationFailedException},
     * it is not recommended.
     * 
     * @see OperationFailedException
     * @see Operation
     * @param failedOperation the failed operation
     * @return the {@link OperationFailedException} instance with the failed
     * operation object passed as parameter
     */
    public static OperationFailedException fromFailedOperation(Operation failedOperation) {
        var exception = new OperationFailedException();
        exception.setFailedOperation(failedOperation);
        return exception;
    }

    /**
     * Static factory that constructs an {@link OperationFailedException}
     * with the passed fail translation message key as parameter. The
     * fail key can be null.
     * 
     * @see OperationFailedException
     * @see TranslatableComponent
     * @param failKey the fail translation message key
     * @return an {@link OperationFailedException} instance with the fail key
     * passed as parameter
     */
    public static OperationFailedException fromKey(String failKey) {
        var exception = new OperationFailedException();
        exception.setFailKey(failKey);
        return exception;
    }

    /**
     * Static factory that constructs an {@link OperationFailedException}
     * with the passed fail translation message key and the {@link Operation}
     * that failed. Both can be null.
     * 
     * @see OperationFailedException
     * @see Operation
     * @see TranslatableComponent
     * @param failKey the fail translation message key
     * @param failedOperation the failed operation
     * @return an {@link OperationFailedException} instance with the fail key
     * and the failed operation passed as parameters
     */
    public static OperationFailedException fromKey(String failKey, Operation failedOperation) {
        var exception = fromKey(failKey);
        exception.setFailedOperation(failedOperation);
        return exception;
    }

    /**
     * Static factory that constructs an {@link OperationFailedException} with
     * the passed fail translation message key and the {@link Component} arguments
     * for using with any overload of {@link Component#translatable()} that supports
     * arguments. Both fail key and arguments can be null.
     * 
     * @see OperationFailedException
     * @see Component
     * @see TranslatableComponent
     * @param failKey the fail translation message key
     * @param args the arguments of the translated message
     * @return an {@link OperationFailedException} instance with the fail key
     * and the arguments passed as parameters
     */
    public static OperationFailedException withArgs(String failKey, Component... args) {
        var exception = fromKey(failKey);
        exception.setArgs(args);
        return exception;
    }

    /**
     * Static factory that constructs an {@link OperationFailedException} with
     * the passed fail translation message key, the failed operation and the
     * {@link Component} arguments for using with any overload of {@link Component#translatable()}
     * that suppots arguments. Every parameter can be null.
     * 
     * @see OperationFailedException
     * @see Component
     * @see TranslatableComponent
     * @param failKey the fail translation message key
     * @param failedOperation the failed operation
     * @param args the arguments of the translated message
     * @return an {@link OperationFailedException} with the fail key,
     * the failed operation and the arguments passed as parameters
     */
    public static OperationFailedException withArgs(String failKey, Operation failedOperation,
            Component... args) {
        var exception = withArgs(failKey, args);
        exception.setFailedOperation(failedOperation);
        return exception;
    }

    /**
     * Creates a {@link TranslatableComponent} using the fail key
     * string passed during the construction of this exception or
     * via setter. If the arguments array is not null, it will
     * be used.
     * 
     * @see TranslatableComponent
     * @return a {@link TranslatableComponent} using the fail key
     * string referenced internally
     * @throws NullPointerException if the fail key is null
     */
    public TranslatableComponent getDirectFailMessage() {
        return getFailMessage(Objects.requireNonNull(getFailKey(), "failKey"));
    }

    /**
     * Creates a {@link TranslatableComponent} processing the fail key
     * string passed during construction of this exception or via setter
     * to create a new fail key based on if the fail message will be sent
     * to the buyer or the seller of the transaction processed by the failed
     * operation. Note: all fail keys made this way must be at
     * {@code transaction.(buy|sell).fail-key.<fail key>}. This method
     * is called "default" because it defines a default path to fail keys
     * of buyers and sellers.
     * 
     * @see TranslatableComponent
     * @param buy true for making a message for buyer, false for seller
     * @return the directed fail message
     * @throws NullPointerException if the fail key is null
     */
    public TranslatableComponent getDefaultFailMessage(boolean buy) {
        String key = Objects.requireNonNull(getFailKey(), "failKey");
        return getFailMessage("transaction." + (buy ? "buy" : "sell") + ".fail-key." + key);
    }

    /**
     * Returns the fail translation message key of this exception.
     * Can be null.
     * 
     * @see TranslatableComponent
     * @return the fail key
     */
    public String getFailKey() {
        return failKey;
    }

    /**
     * Sets the fail translation message key of this exception.
     * Can be null.
     * 
     * @see TranslatableComponent
     * @param failKey the fail key
     */
    public void setFailKey(String failKey) {
        this.failKey = failKey;
    }

    /**
     * Returns the failed operation (normally the one that thrown this exception).
     * Can be null.
     * 
     * @see Operation
     * @return the failed operation
     */
    public Operation getFailedOperation() {
        return failedOperation;
    }

    /**
     * Sets the failed operation (normally the one that thrown this exception).
     * Can be null.
     * 
     * @see Operation
     * @param failedOperation the failed operation
     */
    public void setFailedOperation(Operation failedOperation) {
        this.failedOperation = failedOperation;
    }

    /**
     * Return the arguments to be used in some overload of {@link Component#translatable()}
     * that supports arguments. Can be null.
     * 
     * @see TranslatableComponent
     * @return the args array
     */
    public Component[] getArgs() {
        return args;
    }

    /**
     * Sets the arguments to be used in some overload if {@link Component#translatable()}
     * that suppots arguments. Can be null.
     * 
     * @see TranslatableComponent
     * @param args the args array
     */
    public void setArgs(Component... args) {
        this.args = args;
    }
    
    private TranslatableComponent getFailMessage(String key) {
        Component[] args = getArgs();
        TranslatableComponent failMessage;
        if (args != null) {
            failMessage = Component.translatable(key, NamedTextColor.RED, args);
        } else {
            failMessage = Component.translatable(key, NamedTextColor.RED);
        }
        return failMessage;
    }

}
