package net.stardust.base.model.economy.transaction.operation;

import net.stardust.base.model.economy.transaction.Transaction;

public interface Operation {

    void execute(Transaction transaction) throws OperationFailedException;

}