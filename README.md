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
    public static void main(String[] args) {
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
sim_time=500                  //how many timesteps shall be simulated
num_mirrors=50                //number of mirrors
num_links_per_mirror=2        //number of links per mirror
startup_time_min=5            //minimum startup time of mirrors
startup_time_max=10           //maximum startup time of mirrors
ready_time_min=2              //minimum ready time of mirrors
ready_time_max=20             //maximum ready time of mirrors
stop_time_min=2               //minimum stop time of mirrors
stop_time_max=5               //maximum stop time of mirrors
link_activation_time_min=5    //minimum activation time of links
link_activation_time_max=10   //maximum activation time of links 
fileSize=80                   //size of the data package
min_bandwidth=2               //minimum bandwidth per link 
max_bandwidth=8               //maximum bandwidth per link
fault_probability=0.005       //probability of mirror crashes at each timestep in percent
```

If you do not provide this file, predefined values will be used and the file will be created for you. Once created, this file will be used instead of the predefined version. Thus, you can start the example from the paper on most systems by simply executing the jar. 

Further exemplary configuration files used, e.g., for testing, can be found within the release in the ``resources`` folder.

To get an overview of the framework, have a look at the Javadoc (to be found in folder doc/javadoc).

## Developer Setup

To extend the simulator framework, just clone the repository and import it in your IDE as a Maven project.

You can build the framework using ``mvn package``, which will run all tests, too. This will take quite a while. If you do this on a machine without screen, you need to skip the tests: ``mvn package -DskipTests=true``. This is, because the visualization code is tested, too. 

This project is preconfigured to work with [SonarQube](https://www.sonarsource.com/products/sonarqube/) and JaCoCo. If you want to get an overview with a local SonarQube use:
``mvn clean verify jacoco:report sonar:sonar -Dsonar.projectKey=<<YourName>> -Dsonar.host.url=http://localhost:9000 -Dsonar.token=<<YourToken>> -f pom.xml``

the real deal:
mvn clean org.jacoco:jacoco-maven-plugin:prepare-agent package  org.jacoco:jacoco-maven-plugin:report sonar:sonar -Dsonar.host.url=https://ipra38.inf.tu-dresden.de/sonar -Dsonar.projectKey=TimedRDMSimulator -Dsonar.login=rschmidt -Dsonar.password=Sonysony37?