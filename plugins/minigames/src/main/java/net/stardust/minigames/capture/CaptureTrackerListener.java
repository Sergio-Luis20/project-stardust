package net.stardust.minigames.capture;

import net.stardust.base.events.TrackerListener;
import net.stardust.base.minigame.Minigame.MinigameState;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;

public class CaptureTrackerListener extends TrackerListener {

    private Capture capture;

    public CaptureTrackerListener(Capture capture) {
        super(capture::getWorld);
        this.capture = Objects.requireNonNull(capture, "capture");
    }

    @Override
    protected List<Player> getPlayersToTrack(Player user, World world) {
        if(capture.getState() == MinigameState.PRE_MATCH) {
            return super.getPlayersToTrack(user, world);
        } else {
            return CaptureTeam.getTeam(capture, user).other().getFreePlayers(capture);
        }
    }

}
