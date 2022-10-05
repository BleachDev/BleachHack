package org.bleachhack.util.doom.rr;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utilities to synthesize patch_t format images from multiple patches
 * (with transparency).
 * 
 * 
 * @author velktron
 *
 */

public class MultiPatchSynthesizer {

    
    static class PixelRange {
        public PixelRange(int start, int end) {
            this.start=start;
            this.end=end;
        }
        
        public int getLength(){
            return (end-start+1);
        }
        
        public int start;
        public int end;
    }
    
    public static patch_t synthesizePatchFromFlat(String name,byte[] flat, int width, int height){
        
        byte[] expected=new byte[width *height];        
        byte[][] pixels=new byte[width][height];
        boolean[][] solid=new boolean[width][height];
        
        // Copy as much data as possible.
        System.arraycopy(flat,0,expected,0,Math.min(flat.length,expected.length));

        for (int i=0;i<width;i++){           
            Arrays.fill(solid[i],true);
            for (int j=0;j<height;j++){
                pixels[i][j]=expected[i+j*width];
            }
        }
        
        patch_t result=new patch_t(name,width,height,0,0);
        column_t[] columns=new column_t[width];
        
        for (int x=0;x<width;x++)
            columns[x]=getColumnStream(pixels[x],solid[x],height);
        
       result.columns=columns;
        return result;
    }
    
    public static patch_t synthesize(String name,byte[][] pixels, boolean[][] solid, int width, int height){
        
        patch_t result=new patch_t(name,width,height,0,0);
        column_t[] columns=new column_t[width];
        
        for (int x=0;x<width;x++)
            columns[x]=getColumnStream(pixels[x],solid[x],height);
        
       result.columns=columns;
        return result;
    }

    public static column_t getColumnStream(byte[] pixels, boolean[] solid, int height){
    
        column_t result=new column_t();
        int start=-1;
        int end=-1;
        
        List<PixelRange> ranges=new ArrayList<PixelRange>();
        
        // Scan column for continuous pixel ranges                
        for (int i=0;i<height;i++){
            
            // Encountered solid start.
            if (solid[i] && start==-1){
                start=i; // mark start
            }
                
            // Last solid pixel
            if (solid[i] && i==height-1 && start!=-1 ){
                end=i;
                ranges.add(new PixelRange(start,end));
                start=end=-1; // reset start/end
            }
               
            // Start defined and ending not yet detected
            if (!solid[i] && start!=-1 && end ==-1){
                end=i-1; // Single-pixel runs would be e.g. 1-2 -> 1-1
            }            

            if (start!=-1 && end!=-1){
                // Range complete.
                ranges.add(new PixelRange(start,end));
                start=end=-1; // reset start/end
            }
        }
        
        // There should be at least an empty post
        if (ranges.size()==0){
            ranges.add(new PixelRange(0,-1));
        }
        
        // Ideal for this use, since we don't know how big the patch is going to be a-priori
        ByteArrayOutputStream file=new ByteArrayOutputStream();
        int topdelta=0;
        int n=ranges.size();
        short topdeltas[]=new short[n];
        int postofs[]=new int[n];
        short postlens[]=new short[n];
        

        
        for (int i=0;i<n;i++){
            PixelRange pr=ranges.get(i);
            topdelta=pr.start; // cumulative top delta  
            
            // Precomputed column data
            postofs[i]=(short) file.size()+3; // Last written post +3, pointing at first pixel of data.
            topdeltas[i]=(short) topdelta;
            postlens[i]=(short) (pr.getLength()); // Post lengths are at net of padding  
                      
            file.write(topdeltas[i]);
            file.write(postlens[i]);
            file.write(0); // padding
            file.write(pixels,pr.start,pr.getLength()); // data
            file.write(0); // padding
            }
    
        file.write(0xFF); // Terminator
        
        result.data=file.toByteArray();
        result.postdeltas=topdeltas;
        result.postlen=postlens;
        result.postofs=postofs;
        result.posts=ranges.size();
        
        // The ranges tell us where continuous posts
        
        return result;
    }
    
    /*
    public static patch_t synthesize(byte[][] pixels, boolean[][] solid, int width, int height, int picture_top, int picture_left){
        // Ideal for this use, since we don't know how big the patch is going to be a-priori
        ByteArrayOutputStream file=new ByteArrayOutputStream();
        
        int offset;
        
        int[] columnofs=new int[width];
        
        // Patch header
        file.write(width);
        file.write(height);
        file.write(picture_top);
        file.write(picture_left);

        int column_array=0;
        int x=0,y;
        byte dummy_value;
        while (x<width){

            //write memory buffer position to end of column_array
            columnofs[column_array]=file.size();
            column_array++;

            y = 0;

            boolean operator = true;
            int pixel_count = 0;
            
            while (y < height){
                byte val=pixels[x][y];
                boolean transparent=!solid[x][y];
                
                
                // Pixel is transparent
                if (transparent && !operator ) {
                    dummy_value = 0;                    
                    file.write(dummy_value);
                    operator = true;
                    }
                else //Pixel not transparent, and operator condition set.
                    if (!transparent && operator){
                    int row_start = y;                    
                    pixel_count = 0;
                    dummy_value = 0;
                    // write above post data to memory buffer

                    offset = file.size(); //current post position in memory buffer

                    operator = false;
                    } else 
                   if (!transparent && !operator){
                       pixel_count++; // increment current post pixel_count
                   }

                    if (offset > 0 && pixel_count > 0){
                        previous_offset = current post position

                        seek back in memory buffer by offset - 2

                        write pixel_count to memory buffer

                        seek back to previous_offset
                    end block
                    
                    write pixel to memory buffer
                end block

                increment y by 1

            end block

            if operator = true or y = height then
                Pixel = 0

                write Pixel to memory buffer

                rowstart = 255
                
                write rowstart to memory buffer
            end block

            increment x by 1

        end block

        seek memory buffer position to 0

        block_size = picture_width * size of dword

        allocate block_memory, filled with 0's, with block_size

        write block_memory to file, using block_size as size

        offset = current file_position

        free block_memory

        seek to position 8 in file from start

        for loop, count = 0, break on count = number of elements in column_array
            column_offset = column_array[count] + offset

            write column_offset to file
        end block

        write memory buffer to file
    }
    } */
    
}
