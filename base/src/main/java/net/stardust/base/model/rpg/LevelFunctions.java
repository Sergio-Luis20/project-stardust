package net.stardust.base.model.rpg;

import br.sergio.utils.math.MathUtils;
import br.sergio.utils.math.Point;

public final class LevelFunctions {
    
    private LevelFunctions() {}

    public static LevelFunction linear(long zero, Point point) {
        if(checks(zero, point)) {
            return currentLevel -> zero;
        }
        double constant = (point.getY() - zero) / (point.getX() - 1);
        return currentLevel -> Math.round((constant * currentLevel + zero));
    }

    public static LevelFunction exponential(long zero, Point point) {
        if(checks(zero, point)) {
            return currentLevel -> zero;
        }
        double constant = MathUtils.root(point.getY() / zero, point.getX() - 1);
        return currentLevel -> Math.round((zero * Math.pow(constant, currentLevel)));
    }

    public static LevelFunction logarithmic(long zero, Point point) {
        if(checks(zero, point)) {
            return currentLevel -> zero;
        }
        double constant = (point.getY() - zero) / MathUtils.ln(point.getX());
        return currentLevel -> Math.round((constant * MathUtils.ln(currentLevel + 1) + zero));
    }

    private static boolean checks(long zero, Point point) {
        if(zero <= 0) {
            throw new IllegalArgumentException("f(0) must be greater than 0");
        }
        double x = point.getX();
        if(x <= 0) {
            throw new IllegalArgumentException("x must be greater than 0");
        }
        if(point.getY() < zero) {
            throw new IllegalArgumentException("y must be greater or equals to f(0)");
        }
        return x == 1;
    }

}
