package org.bleachhack.util.doom.rr;

public class base_ratio_t {

  public base_ratio_t(int base_width, int base_height, int psprite_offset,
            int multiplier, float gl_ratio) {
        this.base_width = base_width;
        this.base_height = base_height;
        this.psprite_offset = psprite_offset;
        this.multiplier = multiplier;
        this.gl_ratio = (float) (RMUL*gl_ratio);
    }

public int base_width;      // Base width (unused)
  public int base_height;     // Base height (used for wall visibility multiplier)
  public  int psprite_offset;  // Psprite offset (needed for "tallscreen" modes)
  public  int multiplier;      // Width or height multiplier
  public  float gl_ratio;
  
  public static final double RMUL =1.6d/1.333333d;
  
}