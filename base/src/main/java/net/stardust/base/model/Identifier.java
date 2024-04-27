package net.stardust.base.model;

import java.io.Serializable;

import net.kyori.adventure.text.Component;

public interface Identifier<T extends Serializable> extends Serializable {
    
    T getId();

    String getStringName();

    Component getComponentName();

}
