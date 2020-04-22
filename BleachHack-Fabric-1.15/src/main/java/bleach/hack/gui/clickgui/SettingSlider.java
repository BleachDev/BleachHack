/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/bleachhack-1.14/).
 * Copyright (c) 2019 Bleach.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
