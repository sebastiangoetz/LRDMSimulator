package de.tud.inf.st.trdm.effectors;

public class Effect {
    private Action action;

    public Effect(Action action) {
        this.action = action;
    }
    public int getDeltaActiveLinks() {
        int m1 = action.getNetwork().getNumMirrors();
        int lpm = action.getNetwork().getNumTargetLinksPerMirror();
        //TODO check for topology
        if(action instanceof ChangeMirrorAction cma) {
            int m2 = cma.getNewMirrors();
            return (2 * lpm * (m2 - m1))/((m1-1)*(m2-1));
        }
        return 0;
    }

    public int getDeltaBandwidth() {
        return 0;
    }

    public int getDeltaTimeToWrite() {
        return 0;
    }

    public int getLatency() {
        int minStartup = Integer.parseInt(action.getNetwork().getProps().getProperty("startup_time_min"));
        int maxStartup = Integer.parseInt(action.getNetwork().getProps().getProperty("startup_time_max"));
        int minReady = Integer.parseInt(action.getNetwork().getProps().getProperty("ready_time_min"));
        int maxReady = Integer.parseInt(action.getNetwork().getProps().getProperty("ready_time_min"));
        int minActive = Integer.parseInt(action.getNetwork().getProps().getProperty("link_activation_time_min"));
        int maxActive = Integer.parseInt(action.getNetwork().getProps().getProperty("link_activation_time_max"));
        int time = 0;
        if(action instanceof ChangeMirrorAction) {
            time = (maxStartup - minStartup) / 2 + (maxReady - minReady) / 2 + (maxActive - minActive) / 2;
        } else if(action instanceof TargetLinkChange) {
            time = 0;
        }
        return time;
    }
}
