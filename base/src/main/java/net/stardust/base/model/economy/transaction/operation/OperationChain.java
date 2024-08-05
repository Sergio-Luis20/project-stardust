package net.stardust.base.model.economy.transaction.operation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.stardust.base.model.economy.transaction.Transaction;

/**
 * This class is a chain of responsability. It maintains internally
 * a list of other operation implementations (nodes) to construct
 * an entirely complex operation. The nodes will be executed in the
 * same order as they are inserted in the internal list, either by
 * constructor ({@link OperationChain#OperationChain(Operation...)}) or
 * via add method ({@link OperationChain#addAll(Operation...)}). If
 * an inserted node is null, it will be ignored during execution.
 * 
 * @see Operation
 * 
 * @author Sergio Luis
 */
public class OperationChain implements Operation {

    private List<Operation> nodes;

    /**
     * Creates a chain with an empty node internal list.
     * 
     * @see #OperationChain(Operation...)
     */
    public OperationChain() {
        nodes = new ArrayList<>();
    }

    /**
     * Creates a chain with the nodes passed as parameters.
     * 
     * @see #OperationChain()
     * @param operations the nodes to be executed
     */
    public OperationChain(Operation... operations) {
        this();
        addAll(operations);
    }

    /**
     * Executes all nodes inside the internal list. They are executed
     * in the same order of insertion. If a node is null, it will be ignored.
     */
    @Override
    public void execute(Transaction transaction) throws OperationFailedException {
        for (Operation node : nodes) {
            if (node != null) {
                node.execute(transaction);
            }
        }
    }

    /**
     * Returns the list of internal nodes. Note: this is not a copy list,
     * so be careful on how you manage it.
     * 
     * @return the nodes list
     */
    public List<Operation> getNodes() {
        return nodes;
    }

    /**
     * Adds all nodes inside the parameter array into the internal
     * node list.
     * 
     * @param operations the nodes to be added
     */
    public void addAll(Operation... operations) {
        nodes.addAll(Arrays.asList(operations));
    }

}
