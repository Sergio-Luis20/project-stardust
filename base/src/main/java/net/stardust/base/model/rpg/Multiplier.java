package net.stardust.base.model.rpg;

import java.io.Serializable;


public class Multiplier implements Cloneable, Serializable, Comparable<Multiplier> {
    
    protected float value;

    public Multiplier() {
        this(1);
    }

    public Multiplier(float value) {
        this.value = value;
    }

    public static Multiplier percentage(float value) {
        return new Multiplier(value / 100);
    }

    public float apply(float x) {
        return x * value;
    }

    public void add(float x) {
        value += x;
    }

    public void subtract(float x) {
        value -= x;
    }

    public void addPercentage(float x) {
        value += x / 100;
    }

    public void subtractPercentage(float x) {
        value -= x / 100;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public float getPercentageValue() {
        return value * 100;
    }

    public void setPercentageValue(float value) {
        this.value = value / 100;
    }

    @Override
    public Multiplier clone() {
        return new Multiplier(value);
    }

    @Override
    public int compareTo(Multiplier multiplier) {
        return Float.compare(value, multiplier.value);
    }

    @Override
    public boolean equals(Object o) {
        if(o == null) {
            return false;
        }
        if(o == this) {
            return true;
        }
        if(o instanceof Multiplier multiplier) {
            return value == multiplier.value;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Float.valueOf(value).hashCode();
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

}
