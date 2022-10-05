/**
 * Copyright (C) 2017 Good Sign
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

package org.bleachhack.util.doom.v.scale;

class VideoScaleInfo implements VideoScale {
    
   protected float scale;
   protected int width;
   protected int height;
   protected int bestScaleX;
   protected int bestScaleY;
   protected int bestSafeScale;
   
   /** Scale is intended as a multiple of the base resolution, 320 x 200.
    *  If changing the ratio is also desired, then keep in mind that
    *  the base width is always considered fixed, while the base height
    *  is not. 
    * 
    * @param scale
    */
   
    public VideoScaleInfo(float scale) {
        this.scale = scale;
        width = (int) (BASE_WIDTH * scale);
        height = (int) (scale * BASE_WIDTH * INV_ASPECT_RATIO);
        bestScaleX = (int) Math.floor((float) width / (float) BASE_WIDTH);
        bestScaleY = (int) Math.floor((float) height / (float) BASE_HEIGHT);
        bestSafeScale = Math.min(bestScaleX, bestScaleY);
    }
   
   /** It's possible to specify other aspect ratios, too, keeping in mind
    *  that there are maximum width and height limits to take into account,
    *  and that scaling of graphics etc. will be rather problematic. Default
    *  ratio is 0.625, 0.75 will give a nice 4:3 ratio.
    *  
    *  TODO: pretty lame...
    *  
    * @param scale
    * @param ratio
    */
   
   public VideoScaleInfo(float scale, float ratio){
       this.scale=scale;
       width=(int) (BASE_WIDTH*scale);
       height=(int) (scale*BASE_WIDTH*ratio);
       bestScaleX= (int) Math.floor((float)width/(float)BASE_WIDTH);
       bestScaleY= (int) Math.floor((float)height/(float)BASE_HEIGHT);
       bestSafeScale= Math.min(bestScaleX, bestScaleY);
       
   }
      
    @Override
    public int getScreenWidth() {
        return width;
    }

    @Override
    public int getScreenHeight() {      
        return height;
    }

    @Override
    public int getScalingX() {
        return bestScaleX;
    }

    @Override
    public int getScalingY() {
        return bestScaleY;
    }

    @Override
    public int getSafeScaling() {
        return bestSafeScale;
    }

    @Override
    public boolean changed() {
        return false;
    }

    @Override
    public float getScreenMul() {        
        return scale;
    }

}
