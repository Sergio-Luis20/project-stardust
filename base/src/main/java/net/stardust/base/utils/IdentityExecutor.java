package net.stardust.base.utils;

import java.util.concurrent.Executor;

public class IdentityExecutor implements Executor {

    @Override
    public void execute(Runnable command) {
        command.run();
    }

}
