package net.stardust.base.model.rpg;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class NumberComposition implements Serializable {

    public Multiplier[][] layers;

    public float function(float input) {
        float result = input;
        for (int i = 0; i < layers.length; i++) {
            result *= 1 + getLayerSum(i);
        }
        return result;
    }

    private float getLayerSum(int index) {
        float sum = 0;
        for (Multiplier multiplier : layers[index]) {
            sum += multiplier == null ? 0 : multiplier.getValue();
        }
        return sum;
    }
    
}
