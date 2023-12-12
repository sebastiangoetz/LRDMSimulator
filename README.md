# Latency-aware RDMSim
A Latency-aware Version of a Remote Data Mirroring Simulator

This simulator is inspired by the [RDM simulator](https://www.hpi.uni-potsdam.de/giese/public/selfadapt/exemplars/rdmsim/) developed by Huma Samin et al. 

The framework simulates a network of mirrors and links between them, which aim to distribute the same data among all mirrors.
It allows to observe the current overall bandwidth used. The main difference to the original simulator is that in this simulator the network is represented by actual objects.
This allows to investigate the timing behavior of the network.

## Installation / Setup

The simulator requires a Java Runtime Environment 17+ ([get it here](https://jdk.java.net/java-se-ri/17)).

To run the example simulation from the paper, download the release (jar) and execute it:

``java -jar lrdm.jar``

To write your own optimizer setup a new Java project in your favorite IDE and add the release jar to your classpath.

A simple example looks as follows:

```java
import org.lrdm.TimedRDMSim;

class Example {
    public static void main(String args[]) {
        TimedRDMSim sim = new TimedRDMSim("sim.conf");
        sim.initialize(new BalancedTreeTopologyStrategy());
        Effector effector = sim.getEffector();
        effector.setMirrors(20,10); //change number of mirrors to 20 at timestep 10
        
        for(int t = 0; t < sim.getSimTime(); t++) {
            sim.runStep(t);
            //let the probes print 
            for(Probe p : sim.getProbes()) {
                p.print(t);
            }
        }
    }
}
```

In addition, you need to provide a configuration file (sim.conf) with the following parameters:

```properties
debug=true
sim_time=500
num_mirrors=50
num_links_per_mirror=2
startup_time_min=5
startup_time_max=10
ready_time_min=2
ready_time_max=20
stop_time_min=2
stop_time_max=5
link_activation_time_min=5
link_activation_time_max=10
fileSize=80
min_bandwidth=2
max_bandwidth=8
fault_probability=0.005
```

Exemplary configuration files can be found within the release in the ``resources`` folder.

TODO Javadoc