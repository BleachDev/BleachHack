package bleach.hack.util.render;

import bleach.hack.mixin.AccessorFrustum;
import bleach.hack.mixin.AccessorWorldRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Frustum;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vector4f;

public class FrustumUtils {

	public static Frustum getFrustum() {
		return ((AccessorWorldRenderer) MinecraftClient.getInstance().worldRenderer).getFrustum();
	}

	public static boolean isBoxVisible(Box box) {
		return getFrustum().isVisible(box);
	}

	public static boolean isPointVisible(Vec3d vec) {
		return isPointVisible(vec.x, vec.y, vec.z);
	}

	public static boolean isPointVisible(double x, double y, double z) {
		AccessorFrustum frustum = (AccessorFrustum) getFrustum();
		Vector4f[] frustumCoords = frustum.getHomogeneousCoordinates();
		Vector4f pos = new Vector4f((float) (x - frustum.getX()), (float) (y - frustum.getY()), (float) (z - frustum.getZ()), 1f);

		for (int i = 0; i < 6; ++i) {
			if (frustumCoords[i].dotProduct(pos) <= 0f) {
				return false;
			}
		}

		return true;
	}
}
