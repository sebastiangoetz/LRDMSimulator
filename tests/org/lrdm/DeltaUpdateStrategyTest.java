package org.lrdm;


import org.junit.jupiter.api.Test;
import org.lrdm.data_update_strategy.DeltaDataUpdateStrategy;
import org.lrdm.dirty_flag_update_strategy.HighestFlagPerTimestep;
import org.lrdm.topologies.BalancedTreeTopologyStrategy;
import org.lrdm.topologies.TopologyStrategy;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class DeltaUpdateStrategyTest {


    @Test
    void updateRequiredTest() throws IOException{
        TestUtils.loadProperties("resources/sim-test-1.conf");

        int numMirrors = Integer.parseInt(TestUtils.props.getProperty("num_mirrors"));
        int numLinksPerMirror = Integer.parseInt(TestUtils.props.getProperty("num_links_per_mirror"));
        int fileSize = Integer.parseInt(TestUtils.props.getProperty("fileSize"));

        TopologyStrategy strategy = new BalancedTreeTopologyStrategy();

        DirtyFlag dirtyFlag = new DirtyFlag(Arrays.asList(0,0,1));
        Data data1 = new Data(fileSize, 34);
        Data data2 = new Data(fileSize, 100);
        Data data3 = new Data(fileSize, 45);
        DataPackage dataPackage = new DataPackage(Arrays.asList(data1,data2,data3), dirtyFlag);

        // create network of mirrors
        Network network = new Network(strategy, numMirrors, numLinksPerMirror, dataPackage, TestUtils.props, new HighestFlagPerTimestep(), new DeltaDataUpdateStrategy());

        Mirror m1 = new Mirror(1,0,TestUtils.props,new DeltaDataUpdateStrategy());
        Mirror m2 = new Mirror(2,0, TestUtils.props, new DeltaDataUpdateStrategy());
        Mirror m3 = new Mirror(3,0,TestUtils.props, new DeltaDataUpdateStrategy());
        Mirror m4 = new Mirror(7,0,TestUtils.props,new DeltaDataUpdateStrategy());

        Link l1 = new Link(4,m1,m2,0,TestUtils.props);
        Link l2 = new Link(5,m1,m3,0,TestUtils.props);
        Link l3 = new Link(6,m1,m4,0,TestUtils.props);
        //Link l4 = new Link(8,m3,m4,0,TestUtils.props);

        //new List<Integer> flag1 = new ArrayList<Integer>(Arrays.asList(1,2,4));
        DirtyFlag dirty1 = new DirtyFlag(new ArrayList<Integer>(Arrays.asList(1,2,4)));
        DirtyFlag dirty2 = new DirtyFlag(new ArrayList<Integer>(Arrays.asList(2,5,4)));
        DirtyFlag dirty3 = new DirtyFlag(new ArrayList<Integer>(Arrays.asList(1,8,4)));
        Data d = new Data(10, 3);

        DataPackage package1 = new DataPackage(new ArrayList<>(List.of(d)), dirty1);
        DataPackage package4 = new DataPackage(new ArrayList<>(List.of(d)), dirty1);
        DataPackage package2 = new DataPackage(new ArrayList<>(List.of(d)), dirty2);
        DataPackage package3 = new DataPackage(new ArrayList<>(List.of(d)), dirty3);

        m1.setDataPackage(package1);
        m2.setDataPackage(package4);
        m3.setDataPackage(package2);
        m4.setDataPackage(package2);
        m1.setInvalidFlagState();


        boolean answer = new DeltaDataUpdateStrategy().updateRequired(m1,network);

        assertFalse(answer);

    }
}
