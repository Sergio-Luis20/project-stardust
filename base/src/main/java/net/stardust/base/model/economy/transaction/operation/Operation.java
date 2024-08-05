package net.stardust.base.model.economy.transaction.operation;

import net.stardust.base.model.economy.transaction.Transaction;

/**
 * This interface defines a task that processes a Transaction.
 * It can process the entire Transaction or process part of it.
 * For more flexibility on how different transactions are processed,
 * it is recommended use {@link OperationChain}, which is a chain
 * of responsability where you can set what operations will be
 * executed to perform the whole process.
 * 
 * @see OperationChain
 * 
 * @author Sergio Luis
 */
public interface Operation {

    /**
     * Performs a Transaction process. If the transaction fails by
     * business rules, it will thrown an {@link OperationFailedException}.
     * 
     * @see OperationFailedException
     * @param transaction the transaction object to be processed
     * @throws OperationFailedException if the operation fails due to business rules
     */
    void execute(Transaction transaction) throws OperationFailedException;

}