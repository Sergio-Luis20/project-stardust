package net.stardust.base.utils.gameplay;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import br.sergio.utils.math.Point;
import br.sergio.utils.math.Vector;

public final class BukkitMath {

    public static final double DEFAULT_ANGLE_VARIATION = Math.PI / 50;
    
    private BukkitMath() {}

    public static Vector getEyePosition(Player player) {
        return toVector(player.getEyeLocation());
    }

    public static Vector getPosition(Player player) {
        return toVector(player.getLocation());
    }

    public static Vector toVector(Location loc) {
        return new Vector(loc.getX(), loc.getY(), loc.getZ());
    }

    public static Location toLocation(Vector v) {
        return toLocation(null, v);
    }

    public static Location toLocation(World world, Vector v) {
        return toLocation(world, v, 0, 0);
    }

    public static Location toLocation(World world, Vector v, float yaw, float pitch) {
        return new Location(world, v.getX(), v.getY(), v.getZ(), yaw, pitch);
    }

    public static Vector getPointingEyeVector(Player player) {
        return getDirectionalVector(player.getEyeLocation());
    }

    public static Vector getPointingVector(Player player) {
        return getDirectionalVector(player.getLocation());
    }

    public static Vector getDirectionalVector(Location loc) {
        return getDirectionalVector(loc.getYaw(), loc.getPitch());
    }

    public static Vector getDirectionalVector(double yaw, double pitch) {
        double radYaw = Math.toRadians(yaw);
        double radPitch = Math.toRadians(pitch);

        double pitchCos = Math.cos(radPitch);

        double x = -Math.sin(radYaw) * pitchCos;
        double y = -Math.sin(radPitch);
        double z = Math.cos(radYaw) * pitchCos;

        return new Vector(x, y, z);
    }

    public static double getYaw(Vector v) {
        return -Math.toDegrees(Math.atan2(v.getX(), v.getZ()));
    }

    public static double getPitch(Vector v) {
        return -Math.toDegrees(Math.atan2(v.getY(), Math.hypot(v.getX(), v.getZ())));
    }

    public static Vector fromBukkitVector(org.bukkit.util.Vector bukkitVector) {
        return new Vector(bukkitVector.getX(), bukkitVector.getY(), bukkitVector.getZ());
    }

    public static org.bukkit.util.Vector toBukkitVector(Vector vector) {
        return new org.bukkit.util.Vector(vector.getX(), vector.getY(), vector.getZ());
    }

    public static List<Vector> sphere(Point center, double radius) {
        return sphere(center, radius, DEFAULT_ANGLE_VARIATION);
    }

    public static List<Vector> sphere(Point center, double radius, double angleVariation) {
        List<Vector> sphere = new ArrayList<>();
        double semiperiod = Math.PI;
        double period = 2 * semiperiod;
        for(double vAngle = 0; vAngle <= semiperiod; vAngle += angleVariation) {
            double y = center.getY() - radius * Math.cos(vAngle);
            for(double hAngle = 0; hAngle <= period; hAngle += angleVariation) {
                double x = center.getX() + radius * Math.cos(hAngle);
                double z = center.getZ() + radius * Math.sin(hAngle);
                sphere.add(new Vector(x, y, z));
            }
        }
        return sphere;
    }

}
