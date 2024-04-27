package net.stardust.base.model;

import java.io.Serializable;

public interface StardustEntity<T> extends Serializable {
    
    T getEntityId();

}
