package org.bleachhack.util.doom.rr;

public interface IDetailAware {

    public static int HIGH_DETAIL=0;
    public static int LOW_DETAIL=1;
    void setDetail(int detailshift);
}
