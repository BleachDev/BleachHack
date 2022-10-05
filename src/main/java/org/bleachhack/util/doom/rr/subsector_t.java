package org.bleachhack.util.doom.rr;

import org.bleachhack.util.doom.p.Resettable;

/**
 * 
 * A SubSector. References a Sector. Basically, this is a list of LineSegs,
 * indicating the visible walls that define (all or some) sides of a convex BSP
 * leaf.
 * 
 * @author admin
 */
public class subsector_t implements Resettable{

	public subsector_t() {
		this(null,  0,  0);
	} 

	public subsector_t(sector_t sector, int numlines, int firstline) {
		this.sector = sector;
		this.numlines = numlines;
		this.firstline = firstline;
	}

	// Maes: single pointer
	public sector_t sector;
	// e6y: support for extended nodes
	// 'int' instead of 'short'
	public int numlines;
	public int firstline;
	
	public String toString(){
		sb.setLength(0);
		sb.append("Subsector");
		sb.append('\t');
		sb.append("Sector: ");
		sb.append(sector);
		sb.append('\t');
		sb.append("numlines ");
		sb.append(numlines);
		sb.append('\t');
		sb.append("firstline ");
		sb.append(firstline);
		return sb.toString();
		
		
	}
	
	private static StringBuilder sb=new StringBuilder();

    @Override
    public void reset() {
        sector=null;
        firstline=numlines=0;        
    }
	

}
