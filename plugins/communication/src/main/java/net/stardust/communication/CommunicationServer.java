package net.stardust.communication;

import br.sergio.comlib.Communication;
import br.sergio.comlib.ServerHandler;
import net.stardust.base.BasePlugin;

public class CommunicationServer extends BasePlugin {

    private ServerHandler server;

    @Override
    public void onLoad() {
        super.onLoad();
        server = Communication.getServer(getLogger());
    }

    @Override
    public void onEnable() {
        super.onEnable();
        server.start();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        server.stop();
    }

}
