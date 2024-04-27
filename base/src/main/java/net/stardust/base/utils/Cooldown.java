package net.stardust.base.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import net.stardust.base.Communicable;
import net.stardust.base.utils.plugin.PluginConfig;

public class Cooldown implements Communicable {
    
    private static ExecutorService service = Executors.newVirtualThreadPerTaskExecutor();

    private boolean sync;
    private CooldownTask task;
    private AtomicBoolean running, canceled;

    public Cooldown(boolean sync) {
        this(sync, null);
    }

    public Cooldown(boolean sync, CooldownTask task) {
        this.task = task;
        this.sync = sync;
        running = new AtomicBoolean();
        canceled = new AtomicBoolean();
    }

    public void start(int time, TimeUnit unit) {
        if(tryStart(time, unit)) {
            service.submit(() -> {
                try {
                    Thread.sleep(unit.toMillis(time));
                } catch(InterruptedException e) {
                    Throwables.send(getId(), e);
                    complete();
                    return;
                }
                if(!canceled.get() && task != null) {
                    if(sync) {
                        StardustThreads.run(PluginConfig.get().getPlugin(), () -> task.execute(0, unit));
                    } else {
                        task.execute(0, unit);
                    }
                }
                complete();
            });
        }
    }

    public void startRepeat(int time, TimeUnit unit) {
        if(tryStart(time, unit)) {
            service.submit(() -> {
                AtomicInteger remainingTime = new AtomicInteger(time);
                TimeUnit usingUnit = unit;
                long sleepTime = usingUnit.toMillis(1);
                while(remainingTime.get() >= 0) {
                    final int t = remainingTime.get();
                    if(canceled.get()) break;
                    if(task != null) {
                        if(sync && StardustThreads.call(PluginConfig.get()
                                .getPlugin(), () -> task.execute(t, unit))) break;
                        if(task.execute(t, unit)) break;
                    }
                    if(t == 0) break;
                    remainingTime.decrementAndGet();
                    try {
                        Thread.sleep(sleepTime);
                    } catch(InterruptedException e) {
                        Throwables.send(getId(), e);
                        complete();
                        return;
                    }
                }
                complete();
            });
        }
    }

    private boolean tryStart(int time, TimeUnit unit) {
        if(running.get()) return false;
        if(time <= 0) throw new IllegalArgumentException("time must be greater than zero");
        if(unit == null) throw new NullPointerException("unit");
        running.set(true);
        return true;
    }

    public void cancel() {
        if(!running.get()) return;
        canceled.set(true);
    }

    private void complete() {
        running.set(false);
    }

    public void reset() {
        if(running.get()) throw new IllegalStateException("running");
        running.set(false);
        canceled.set(false);
    }

    public boolean isRunning() {
        return running.get();
    }

    public boolean isCanceled() {
        return canceled.get();
    }

    public boolean isSync() {
        return sync;
    }

    public void setSync(boolean sync) {
        if(running.get()) throw new IllegalStateException("running");
        this.sync = sync;
    }

    public CooldownTask getTask() {
        return task;
    }

    public void setTask(CooldownTask task) {
        if(running.get()) throw new IllegalStateException("running");
        this.task = task;
    }

    @FunctionalInterface
    public static interface CooldownTask {

        /**
         * Must return if the cooldown should cancel or not.
         * This boolean value is only used if this task is used
         * in {@link Cooldown#startRepeat(int, TimeUnit)} method.
         * The cooldown will stop anyway when the call with
         * <code>remainingTime</code> being 0 is done.
         * @param remainingTime the remaining time to end the cooldown.
         * It belongs to the interval [0, initialTime].
         * @param unit the time unit used in {@link Thread#sleep(long)}
         * @return <code>true</code> if the cooldown should cancel,
         * <code>false</code> otherwise.
         */
        boolean execute(int remainingTime, TimeUnit unit);

    }

}
