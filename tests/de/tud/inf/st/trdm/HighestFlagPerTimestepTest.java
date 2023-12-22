package de.tud.inf.st.trdm;

import de.tud.inf.st.trdm.DataUpdateStrategy.DeltaDataUpdateStrategy;
import de.tud.inf.st.trdm.DirtyFlagUpdateStrategy.HighestFlagPerTimestep;
import de.tud.inf.st.trdm.topologies.BalancedTreeTopologyStrategy;
import de.tud.inf.st.trdm.topologies.TopologyStrategy;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.*;

import static de.tud.inf.st.trdm.TestUtils.props;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class HighestFlagPerTimestepTest {

    @Test
    void updateMapTest() throws IOException {
        TestUtils.loadProperties("resources/sim-test-1.conf");

        Mirror m1 = new Mirror(1,0,TestUtils.props,new DeltaDataUpdateStrategy());
        Mirror m2 = new Mirror(2,0, TestUtils.props, new DeltaDataUpdateStrategy());

        Map<Integer, DirtyFlag> testMap= new HashMap<>();

        testMap = new HighestFlagPerTimestep().updateMap(m1, testMap, m2.getData().getDirtyFlag());

        assertEquals(testMap.get(0), m2.getData().getDirtyFlag());
    }

    @Test
    void updateDirtyFlagTest() throws IOException {
        TestUtils.loadProperties("resources/sim-test-1.conf");

        int numMirrors = Integer.parseInt(props.getProperty("num_mirrors"));
        int numLinksPerMirror = Integer.parseInt(props.getProperty("num_links_per_mirror"));
        int fileSize = Integer.parseInt(props.getProperty("fileSize"));

        TopologyStrategy strategy = new BalancedTreeTopologyStrategy();

        DirtyFlag dirtyFlag = new DirtyFlag(Arrays.asList(0,0,1));
        Data data1 = new Data(fileSize, 34);
        Data data2 = new Data(fileSize, 100);
        Data data3 = new Data(fileSize, 45);

        DataPackage dataPackage = new DataPackage(Arrays.asList(data1,data2,data3), dirtyFlag);

        // create network of mirrors
        Network network = new Network(strategy, numMirrors, numLinksPerMirror, dataPackage, props, new HighestFlagPerTimestep(), new DeltaDataUpdateStrategy());

        Mirror m1 = new Mirror(1,0,TestUtils.props,new DeltaDataUpdateStrategy());
        Mirror m2 = new Mirror(2,0, TestUtils.props, new DeltaDataUpdateStrategy());
        Mirror m3 = new Mirror(3,0,TestUtils.props, new DeltaDataUpdateStrategy());

        List<Mirror> mirrors = new ArrayList<>();
        mirrors.add(m1);
        mirrors.add(m2);
        mirrors.add(m3);

        new HighestFlagPerTimestep().updateDirtyFlag(mirrors, network);

        assertEquals(mirrors.get(0).getData().getDirtyFlag(), m2.getData().getDirtyFlag());
        assertEquals(mirrors.get(2).getData().getDirtyFlag(), m3.getData().getDirtyFlag());
    }
}
