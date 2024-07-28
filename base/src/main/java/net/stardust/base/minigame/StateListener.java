package net.stardust.base.minigame;

public abstract class StateListener {

    public void onPreMatch(Minigame minigame) {}
    public void onLatePreMatch(Minigame minigame) {}
    public void onMatch(Minigame minigame) {}
    public void onEndMatch(Minigame minigame) {}
    public void onLateEndMatch(Minigame minigame) {}

}
