package net.stardust.base.utils.property;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Exclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
@AllArgsConstructor
@NoArgsConstructor
public class Property implements Serializable, Cloneable {
    
    @NonNull
    private String name;

    @Setter
    @Exclude private boolean activated;

    @Override
    public Property clone() {
        return new Property(name, activated);
    }

}
