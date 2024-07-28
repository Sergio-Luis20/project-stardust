package net.stardust.base.model.rpg;

import br.sergio.utils.math.Point;
import lombok.Getter;

import java.io.Serializable;
import java.util.Objects;

public class Level implements Serializable, Comparable<Level> {

    public static final LevelFunction DEFAULT_FUNCTION = LevelFunctions.linear(100, new Point(1000, 100000));
    private static final double PROPORTION = (double) Integer.MAX_VALUE / Long.MAX_VALUE;
    
    @Getter
    private int value;
    private long xp;

    @Getter
    private LevelFunction function;

    public Level() {
        function = DEFAULT_FUNCTION;
    }

    public Level(LevelFunction function) {
        setFunction(function);
    }

    public Level(int value) {
        this();
        setValue(value);
    }

    public Level(LevelFunction function, int value) {
        setFunction(function);
        setValue(value);
    }

    public void reset() {
        setValue(0);
        setXP(0);
    }

    public long getXPToNextLevel() {
        return function.apply(value);
    }

    public void addXP(long xp) {
        sumXP(Math.abs(xp), Math.signum(xp));
    }

    public void subtractXP(long xp) {
        sumXP(Math.abs(xp), -Math.signum(xp));
    }

    private void sumXP(long xp, double signum) {
        long levelXP = this.xp;
        int level = value;
        if(signum < 0) {
            while(level > 0) {
                long partialXP = levelXP - xp;
                if(partialXP < 0) {
                    xp -= levelXP;
                    level--;
                    levelXP = function.apply(level);
                    if(level == 0) {
                        partialXP = levelXP - xp;
                        levelXP = partialXP < 0 ? 0 : partialXP;
                    }
                } else {
                    levelXP = partialXP;
                    break;
                }
            }
        } else {
            while(xp > 0) {
                long xpToNextLevel = function.apply(level);
                long fill = xpToNextLevel - levelXP;
                xp -= fill;
                if(xp > 0) {
                    level++;
                    levelXP = 0;
                } else {
                    levelXP = xp + fill;
                    break;
                }
            }
        }
        value = level;
        this.xp = levelXP;
    }

    public void setFunction(LevelFunction function) {
        this.function = Objects.requireNonNull(function, "null function");
        long xp = this.xp;
        this.xp = 0;
        setXP(xp);
    }

    public void setValue(int value) {
        this.value = Math.max(value, 0);
        this.xp = 0;
    }

    public long getXP() {
        return xp;
    }

    public void setXP(long xp) {
        if(xp > this.xp) {
            addXP(xp - this.xp);
        } else {
            this.xp = xp < 0 ? 0 : xp;
        }
    }

    @Override
    public int compareTo(Level level) {
        int valueComparing = value - level.value;
        return valueComparing == 0 ? (int) ((xp - level.xp) * PROPORTION) : valueComparing;
    }
    
    @Override
    public boolean equals(Object o) {
        if(o == null) {
            return false;
        }
        if(o == this) {
            return true;
        }
        if(o instanceof Level level) {
            return value == level.value && xp == level.xp && function.equals(level.function);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, xp, function);
    }

    @Override
    public String toString() {
        return value + " (" + xp + "/" + getXPToNextLevel() + ")";
    }

}
