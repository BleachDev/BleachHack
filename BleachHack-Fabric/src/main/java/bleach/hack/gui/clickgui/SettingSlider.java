package bleach.hack.gui.clickgui;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class SettingSlider extends SettingBase {

	public double min;
	public double max;
	private double value;
	public int round;
	public String text;
	
	public SettingSlider(String text, double min, double max, double value, int round) {
		this.min = min;
		this.max = max;
		this.value = value;
		this.round = round;
		this.text = text;
	}
	
	public double getValue() {
		return round(value, round);
	}
	
	public void setValue(double value) {
		this.value = value;
	}
	
	public double round(double value, int places) {
		BigDecimal bd = new BigDecimal(value);
	    bd = bd.setScale(places, RoundingMode.HALF_UP);
	    return bd.doubleValue();
	}
}
