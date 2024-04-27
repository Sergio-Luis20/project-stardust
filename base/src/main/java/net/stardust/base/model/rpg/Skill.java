package net.stardust.base.model.rpg;

import java.io.Serializable;
import java.util.ResourceBundle;

public interface Skill extends Serializable {

    String getGeneralName();
    String getName(ResourceBundle bundle);
    Level getLevel();

}
