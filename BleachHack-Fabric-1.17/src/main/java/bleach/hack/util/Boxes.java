package bleach.hack.util;

import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;

public class Boxes {

	/** Returns the vector of the min pos of this box. **/
	public static Vec3d getMinVec(Box box) {
		return new Vec3d(box.minX, box.minY, box.minZ);
	}

	/** Returns the vector of the max pos of this box. **/
	public static Vec3d getMaxVec(Box box) {
		return new Vec3d(box.maxX, box.maxY, box.maxZ);
	}

	/** Offsets this box so that minX, minY and minZ are all zero. **/
	public static Box moveToZero(Box box) {
		return box.offset(getMinVec(box).negate());
	}

	/** Returns the distance between to oppisite corners of the box. **/
	public static double getCornerLength(Box box) {
		return getMinVec(box).distanceTo(getMaxVec(box));
	}

	/** Returns the length of an axis in the box. **/
	public static double getAxisLength(Box box, Axis axis) {
		return box.getMax(axis) - box.getMin(axis);
	}

}
