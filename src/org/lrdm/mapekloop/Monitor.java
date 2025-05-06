package org.lrdm.mapekloop;

public class Monitor {

    public Monitor() {
    }


    public static boolean monitorLinkRatio(double linkRatio, double goalLinkRatio) {
        return linkRatio > goalLinkRatio;
    }
}
