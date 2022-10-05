package org.bleachhack.util.doom.p;

public interface ISightChecker {

    public void setZStartTopBOttom(int zstart, int top, int bottom);
    public void setSTrace(mobj_t t1, mobj_t t2);
    public boolean CrossBSPNode(int bspnum);
    
}
