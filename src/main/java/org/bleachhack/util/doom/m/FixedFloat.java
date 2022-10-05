package org.bleachhack.util.doom.m;

/** Some utilities for switching between floating and signed 16.16 fixed-point at will.
 *  They use direct bit manipulation with little -if any- looping.
 *  
 *  The methods can probably be generalized but not a priority for now.
 *  They do not handle Infinities, NaNs and unnormalized numbers.
 *  
 * @author Maes
 *
 */

public class FixedFloat {

    // Various bit masks for IEEE-754 floating point 
    public static final int MANTISSA_32=0x007FFFFF;    
    public static final int EXP_32=0x7F800000;
    public static final int IMPLICIT_32=0x00800000;
    public static final int SIGN_32=0x80000000;
    public static final int NONSIGN_32=0x7FFFFFFF;
    public static final long SIGN_64=0x8000000000000000L;
    public static final long EXP_64=0x7FF0000000000000L;
    public static final long IMPLICIT_64=0x0010000000000000L;
    public static final long MANTISSA_64=0x000fffffffffffffL;
    
    public static float toFloat(int fixed){
        if (fixed==0) return (float)(0.0);
        // Remember sign.
        int sign=fixed&SIGN_32;
        if (fixed<0) fixed=-fixed;
        int exp=findShift(fixed);
        // First shift to left to "cancel" bits "above" the first.
        int mantissa=(fixed<<(exp+2))>>>9;
        int result=sign|(((14-exp)+127)<<23)|mantissa;
        /*if (fixed<0) System.out.println(Integer.toBinaryString(fixed) +"\n"+
                                        Integer.toBinaryString(-fixed) +"\n"+
                                        Integer.toBinaryString(result));*/
        return Float.intBitsToFloat(result);
    }
 
    private static int findShift(int fixed){
        // only non-sign bits.
        fixed&=NONSIGN_32;
        // We assume that the MSb after the sign is set.
        int shift=30;
        while((shift>=0)&&(fixed>>>shift)==0)
            // It's not, apparently
            shift--;

        // Positions 0-15 are fractional, anything above 15 is integer.
        // Return two's complement shift.
        return (30-shift);
        
    }
    
    public static double toDouble(int fixed){
        
        
        // Remember sign.
        
        long fx=fixed;
        fx<<=32;
        long sign=(long)fx&SIGN_64;
        
         if (fixed<0) {
             fixed=-fixed;
             fx=-fx;
         }
        long exp=findShift(fixed);
        // First shift to left to "swallow" sign and implicit 1.
        long bits=(fx<<(exp+2))>>>12;
        long result=sign|(((14-exp)+1023)<<52)|bits;
        return Double.longBitsToDouble(result);
    }
    
    public static int toFixed(float fl){
        // Get the raw bits.
        int flbits=Float.floatToRawIntBits(fl);
        // Remember sign.
        int sign=flbits&SIGN_32;
        // Join together: the implcit 1 and the mantissa bits.
        // We now have the "denormalized" value. 
        int denorm=IMPLICIT_32|(flbits&MANTISSA_32);
        // Get exponent...acceptable values are (-15 ~ 15), else wrap around (use only sign and lowest 4 bits).
        int exp=(((flbits&EXP_32)>>23)-127)&0x8000000F;
        /* Remember, leftmost "1" will be at position 23.
         * So for an exponent of 0, we must shift to position 16.
         * For positive exponents in general, we must shift -7 + exp.
         * and for one of 15, to position 30, plus the sign.
         * While there is space for all bits, we can't keep them all, 
         * as some (well, many)numbers can't be represented in fixed point.
         * 
         */
        int result;
        if ((exp-7)>=0)
            result=sign|(denorm<<(exp-7));
        else
            result=sign|(denorm>>>(7-exp));
        return result;
        }
    
    public static int toFixed(double fl){
        
        // Get the raw bits.
        long flbits=Double.doubleToRawLongBits(fl);
        // Remember sign.
        int sign=(int)((flbits&SIGN_64)>>32);
        // Join together: the implcit 1 and the mantissa bits.
        // We now have the "denormalized" value. 
        long denorm=IMPLICIT_64|(flbits&MANTISSA_64);
        //System.out.println("Denorm"+Integer.toBinaryString(denorm));
        // Get exponent...acceptable values are (-15 ~ 15), else wrap around (use only sign and lowest 4 bits).
        int exp=(int)(((flbits&EXP_64)>>52)-1023)&0x8000000F;
        /* Remember, leftmost "1" will be at position 53.
         * So for an exponent of 0, we must shift to position 16.
         * For positive exponents in general, we must shift -37 + exp.
         * and for one of 15, to position 30, plus the sign.
         * While there is space for all bits, we can't keep them all, 
         * as some (well, many)numbers can't be represented in fixed point.
         * 
         */
        int result;
        if ((exp-36)>=0)
            result=(int) (sign|(denorm<<(exp-36)));
        else
            result=(int) (sign|(denorm>>>(36-exp)));
        //int result=sign|(IMPLICIT_32|(mantissa<<(exp-127)))<<8;
        return result;
        }
    
}
