package net.stardust.base.minigame;

@FunctionalInterface
public interface TickListener {

    /**
     * This method is called every x time units since the object
     * is created and only stops when the plugin is disabled.
     * Though the class and the method are named "tick", the rate
     * they're called by the scheduler is per second (20 ticks).
     * @param minigame a minigame to what this listener was added
     */
    void tick(Minigame minigame);

}
