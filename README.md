# TimedRDMSimulator
A Time-aware Version of a Remote Data Mirroring Simulator

This simulator is inspired by the [RDM simulator](https://www.hpi.uni-potsdam.de/giese/public/selfadapt/exemplars/rdmsim/) developed by Huma Samin et al. 

![Class diagram](https://github.com/sebastiangoetz/TimedRDMSimulator/blob/main/doc/images/classes.png)

Find the JavaDoc here soon.

The framework simulates a network of mirrors and links between them, which aim to distribute the same data among all mirrors.
It allows to observe the current overall bandwidth used. The main difference to the original simulator is that in this simulator the network is represented by actual objects.
This allows to investigate the timing behavior of the network.

![Dashboard](https://github.com/sebastiangoetz/TimedRDMSimulator/blob/main/doc/images/dashboard.png)
