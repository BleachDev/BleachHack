package org.bleachhack.util.doom.wad;

public class JadDecompress {

    public final static int WINDOW_SIZE = 4096;

    public final static int LOOKAHEAD_SIZE = 16;

    public final static int LENSHIFT = 4; /* this must be log2(LOOKAHEAD_SIZE) */

    public static void decode(byte[] input, byte[] output) {
        /*
         * #ifdef JAGUAR decomp_input = input; decomp_output = output;
         * gpufinished = zero; gpucodestart = (int)&decomp_start; while
         * (!I_RefreshCompleted () ) ; #else
         */
        int getidbyte = 0;
        int len;
        int pos;
        int i;
        int source_ptr, input_ptr = 0, output_ptr = 0;
        int idbyte = 0;

        while (true) {
            /* get a new idbyte if necessary */
            if (getidbyte == 0) idbyte = 0xFF & input[input_ptr++];
            getidbyte = (getidbyte + 1) & 7;

            if ((idbyte & 1) != 0) {
                /* decompress */
                pos = (0xFF & input[input_ptr++]) << LENSHIFT;
                pos = pos | ((0xFF & input[input_ptr]) >> LENSHIFT);
                source_ptr = output_ptr - pos - 1;

                len = ((0xFF & input[input_ptr++]) & 0xf) + 1;

                if (len == 1)
                    break;
                for (i = 0; i < len; i++)
                    output[output_ptr++] = output[source_ptr++];
            } else {
                output[output_ptr++] = input[input_ptr++];
            }

            idbyte = idbyte >> 1;

        }

        System.out.printf("Expanded %d to %d\n", input_ptr, output_ptr);
    }

}
