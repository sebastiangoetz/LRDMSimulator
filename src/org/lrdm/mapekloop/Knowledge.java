package org.lrdm.mapekloop;

import java.util.Map;

/**
 * Just the scope of all variables to monitor.
 * They will be saved as an object
 */
public class Knowledge {
    private Integer latency;
    private Integer bandwidth;
    private Integer timeToWrite;
    private Integer activeLinks;
    //smth else?

    public  Knowledge(){
    }

    public Knowledge(Integer latency, Integer bandwidth, Integer timeToWrite, Integer activeLinks) {
        this.latency = latency;
        this.bandwidth = bandwidth;
        this.timeToWrite = timeToWrite;
        this.activeLinks = activeLinks;
    }

    public Knowledge(Integer bandwidth, Integer timeToWrite, Integer activeLinks) {
        this.bandwidth = bandwidth;
        this.timeToWrite = timeToWrite;
        this.activeLinks = activeLinks;
    }

    public Integer getLatency() {
        return latency;
    }

    public void setLatency(Integer latency) {
        this.latency = latency;
    }

    public Integer getBandwidth() {
        return bandwidth;
    }

    public void setBandwidth(Integer bandwidth) {
        this.bandwidth = bandwidth;
    }

    public Integer getTimeToWrite() {
        return timeToWrite;
    }

    public void setTimeToWrite(Integer timeToWrite) {
        this.timeToWrite = timeToWrite;
    }

    public Integer getActiveLinks() {
        return activeLinks;
    }

    public void setActiveLinks(Integer activeLinks) {
        this.activeLinks = activeLinks;
    }
}
