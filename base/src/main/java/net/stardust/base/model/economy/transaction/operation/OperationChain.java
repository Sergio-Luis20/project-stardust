package net.stardust.base.model.economy.transaction.operation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.stardust.base.model.economy.transaction.Transaction;

// Chain of responsability
public class OperationChain implements Operation {

    private List<Operation> nodes;

    public OperationChain() {
        nodes = new ArrayList<>();
    }

    public OperationChain(Operation... operations) {
        this();
        addAll(operations);
    }

    @Override
    public void execute(Transaction transaction) throws OperationFailedException {
        for(Operation node : nodes) {
            if(node != null) {
                node.execute(transaction);
            }
        }
    }

    public List<Operation> getNodes() {
        return nodes;
    }

    public void addAll(Operation... operations) {
        nodes.addAll(Arrays.asList(operations));
    }

}
