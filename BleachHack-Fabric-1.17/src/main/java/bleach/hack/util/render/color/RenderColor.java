package bleach.hack.util.render.color;

public abstract class RenderColor implements Cloneable {

	protected Integer[] overwriteColor = new Integer[4];

	public void overwriteRed(Integer red) {
		overwriteColor[0] = red;
	}

	public void overwriteGreen(Integer green) {
		overwriteColor[1] = green;
	}

	public void overwriteBlue(Integer blue) {
		overwriteColor[2] = blue;
	}

	public void overwriteAlpha(Integer alpha) {
		overwriteColor[3] = alpha;
	}

	protected void cloneOverwriteTo(RenderColor otherColor) {
		otherColor.overwriteColor = overwriteColor.clone();
	}

}
