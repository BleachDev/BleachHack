package org.bleachhack.util.doom.s;

public class DSP {

    /**
     * QDSS Windowed Sinc ReSampling subroutine in Basic
     * 
     * @param x
     *        new sample point location (relative to old indexes) (e.g. every
     *        other integer for 0.5x decimation)
     * @param indat
     *        = original data array
     * @param alim
     *        = size of data array
     * @param fmax
     *        = low pass filter cutoff frequency
     * @param fsr
     *        = sample rate
     * @param wnwdth
     *        = width of windowed Sinc used as the low pass filter rem resamp()
     *        returns a filtered new sample point
     */

    public float resamp(float x, float[] indat, int alim, float fmax,
            float fsr, int wnwdth) {
        int i, j;
        float r_w, r_g, r_a;
        int r_snc, r_y; // some local variables
        r_g = 2 * fmax / fsr; // Calc gain correction factor
        r_y = 0;
        for (i = -wnwdth / 2; i < wnwdth / 2; i++) { // For 1 window width
            j = (int) (x + i); // Calc input sample index
            // calculate von Hann Window. Scale and calculate Sinc
            r_w =
                (float) (0.5 - 0.5 * Math.cos(2 * Math.PI
                        * (0.5 + (j - x) / wnwdth)));
            r_a = (float) (2 * Math.PI * (j - x) * fmax / fsr);
            r_snc = 1;
            if (Math.abs(r_a) > 0)
                r_snc = (int) (Math.sin(r_a) / r_a);
            if ((j >= 0) && (j < alim)) {
                r_y = (int) (r_y + r_g * r_w * r_snc * indat[j]);
            }
        }
        return r_y; // return new filtered sample
    }

    /*
     * Ron Nicholson's QDSS ReSampler cookbook recipe QDSS = Quick, Dirty,
     * Simple and Short Version 0.1b - 2007-Aug-01 Copyright 2007 Ronald H.
     * Nicholson Jr. No warranties implied. Error checking, optimization, and
     * quality assessment of the "results" is left as an exercise for the
     * student. (consider this code Open Source under a BSD style license) IMHO.
     * YMMV. http://www.nicholson.com/rhn/dsp.html
     */

    /**
     * R. Nicholson's QDDS FIR filter generator cookbook recipe QDDS = Quick,
     * Dirty, Dumb and Short version 0.6b - 2006-Dec-14, 2007-Sep-30 No
     * warranties implied. Error checking, optimization, and quality assessment
     * of the "results" is left as an exercise for the student. (consider this
     * code Open Source under a BSD style license) Some example filter
     * parameters:
     * 
     * @param fsr
     *        = 44100 : rem set fsr = sample rate
     * @param fc
     *        = 0 : rem set fc = 0 for lowpass fc = center frequency for
     *        bandpass filter fc = fsr/2 for a highpass
     * @param bw
     *        = 3000 : rem bw = bandwidth, range 0 .. fsr/2 and bw >= fsr/n bw =
     *        3 db corner frequency for a lowpass bw = half the 3 db passband
     *        for a bandpass filter
     * @param nt
     *        = 128 : rem nt = number of taps + 1 (nt must be even) nt should be
     *        > fsr / bw transition band will be around 3.5 * fsr / nt depending
     *        on the window applied and ripple spec.
     * @param g
     *        = 1 : rem g = filter gain for bandpass g = 0.5 , half the gain for
     *        a lowpass filter
     * @return array of FIR taps
     */

    public static double[] wsfiltgen(int nt, double fc, double fsr, double bw, double g) {
        double[] fir = new double[nt];//
        // fir(0) = 0
        // fir(1) is the first tap
        // fir(nt/2) is the middle tap
        // fir(nt-1) is the last tap

        double a, ys, yg, yf, yw;
        for (int i = 1; i < nt; i++) {
            a = (i - nt / 2) * 2.0 * Math.PI * bw / fsr; // scale Sinc width
            ys = 1;
            if (Math.abs(a) > 0)
                ys = Math.sin(a) / a; // calculate Sinc function
            yg = g * (4.0 * bw / fsr); // correct window gain
            yw = 0.54 - 0.46 * Math.cos(i * 2.0 * Math.PI / nt); // Hamming
                                                                 // window
            yf = Math.cos((i - nt / 2) * 2.0 * Math.PI * fc / fsr); // spectral
                                                                    // shift to
                                                                    // fc
            fir[i] = yf * yw * yg * ys; // rem assign fir coeff.
        }
        return fir;
    }
    
    public static void main(String[] argv){
        double[] fir=wsfiltgen(128,11025/2.0,22050,22050*3.0/4,0.5);
        System.out.println(fir);
        
    }
    
    public static byte[] crudeResample(byte[] input,int factor){        
        
        if (input==null || input.length<1) return null;
        
        final int LEN=input.length;
        
        byte[] res=new byte[LEN*factor];
        int k=0;        
        float start,end;
        
        res[0]=input[0];
        
        for (int i=0;i<LEN;i++){
            
            if (i==0) 
                start=127;
            else
                start=0xFF&input[i];
            
            if (i<LEN-1)
                end=0xFF&input[i+1];
            else
                end=127;
            
            double slope=(end-start)/factor;
            
            res[k]=input[i];
            //res[k+factor]=input[i+1];
            
            for (int j=1;j<factor;j++){
                double ratio=j/(double)factor;
                double value=start+slope*ratio;
                byte bval=(byte)Math.round(value);
                res[k+j]=bval;
                }
            k+=factor;
        }
        
        return res;
        
    }
    
    public static void filter(byte[] input,int samplerate, int cutoff){        
       
        double[] tmp=new double[input.length];
        
        // Normalize
        for (int i=0;i<input.length;i++){
            tmp[i]=(0xFF&input[i])/255.0;
        }
        
        filter(tmp,samplerate,cutoff,tmp.length);
        
        // De-normalize
        for (int i=0;i<input.length;i++){
            input[i]=(byte) (0xFF&(int)(tmp[i]*255.0));
        }
        
    }
    

    /** Taken from here
     * http://baumdevblog.blogspot.gr/2010/11/butterworth-lowpass-filter-coefficients.html
     */
    
    private static void getLPCoefficientsButterworth2Pole(final int samplerate, final double cutoff, final double[] ax, final double[] by)
    {
        double PI      = 3.1415926535897932385;
        double sqrt2 = 1.4142135623730950488;

        double QcRaw  = (2 * PI * cutoff) / samplerate; // Find cutoff frequency in [0..PI]
        double QcWarp = Math.tan(QcRaw); // Warp cutoff frequency

        double gain = 1 / (1+sqrt2/QcWarp + 2/(QcWarp*QcWarp));
        by[2] = (1 - sqrt2/QcWarp + 2/(QcWarp*QcWarp)) * gain;
        by[1] = (2 - 2 * 2/(QcWarp*QcWarp)) * gain;
        by[0] = 1;
        ax[0] = 1 * gain;
        ax[1] = 2 * gain;
        ax[2] = 1 * gain;
    }



    public static void filter(double[] samples, int smp, double cutoff,int count)
    {
      // Essentially a 3-tap filter?
       double[] ax=new double[3];
       double[] by=new double[3];
       double[] xv=new double[3];
       double[] yv=new double[3];

       getLPCoefficientsButterworth2Pole(smp, cutoff, ax, by);

       for (int i=0;i<count;i++)
       {
           xv[2] = xv[1];
           xv[1] = xv[0];
           xv[0] = samples[i];
           
           yv[2] = yv[1]; 
           yv[1] = yv[0];
           yv[0] =   (ax[0] * xv[0] + ax[1] * xv[1] + ax[2] * xv[2]
                        - by[1] * yv[0]
                        - by[2] * yv[1]);

           samples[i] = yv[0];
       }
    }
    
}
