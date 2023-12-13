package de.tud.inf.st.trdm;

import de.tud.inf.st.trdm.DataUpdateStrategy.DeltaDataUpdateStrategy;
import de.tud.inf.st.trdm.DirtyFlagUpdateStrategy.HighestFlagPerTimestep;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DeltaUpdateStrategyTest {

    @Test
    void updateMapTest() throws IOException {
        TestUtils.loadProperties("resources/sim-test-1.conf");

        Mirror m1 = new Mirror(1,0,TestUtils.props,new DeltaDataUpdateStrategy());
        Mirror m2 = new Mirror(2,0, TestUtils.props, new DeltaDataUpdateStrategy());

        Map<Integer, DirtyFlag> testMap= new HashMap<>();

        testMap = new HighestFlagPerTimestep().updateMap(m1, testMap, m2.getLookingFlag());

        assertEquals(testMap.get(0), m2.getLookingFlag());
    }

    @Test
    void updateDirtyFlagTest() throws IOException {
        TestUtils.loadProperties("resources/sim-test-1.conf");

        Mirror m1 = new Mirror(1,0,TestUtils.props,new DeltaDataUpdateStrategy());
        Mirror m2 = new Mirror(2,0, TestUtils.props, new DeltaDataUpdateStrategy());
        Mirror m3 = new Mirror(3,0,TestUtils.props, new DeltaDataUpdateStrategy());

        List<Mirror> mirrors = new ArrayList<>();
        mirrors.add(m1);
        mirrors.add(m2);
        mirrors.add(m3);

        //new HighestFlagPerTimestep().updateDirtyFlag(mirrors, new Network());
        new HighestFlagPerTimestep().updateMap(m1, new HashMap<>(), m2.getLookingFlag());

        assertEquals(mirrors.get(0).getLookingFlag(), m2.getLookingFlag());
        assertEquals(mirrors.get(2).getLookingFlag(), m3.getLookingFlag());
    }
}
