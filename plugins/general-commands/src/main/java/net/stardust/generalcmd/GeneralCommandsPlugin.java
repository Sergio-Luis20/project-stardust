package net.stardust.generalcmd;

import java.util.Collections;
import java.util.List;

import lombok.Getter;
import net.stardust.base.BasePlugin;

@Getter
public class GeneralCommandsPlugin extends BasePlugin {

    private List<String> notAllowedNicks;

    @Override
    public void onEnable() {
        super.onEnable();
        saveDefaultConfig();
        notAllowedNicks = Collections.unmodifiableList(getConfig()
            .getStringList("not-allowed-nicks"));
    }

}
