package org.lrdm;

import static de.tud.inf.st.trdm.TestUtils.loadProperties;
import static de.tud.inf.st.trdm.TestUtils.props;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;

class DataPackageTest {
    @Test
    void testInitialization() {
        int fileSize = 30;
        DirtyFlag dirtyFlag = new DirtyFlag(Arrays.asList(0,0,1));
        Data data1 = new Data(fileSize, 34);
        Data data2 = new Data(fileSize, 100);
        Data data3 = new Data(fileSize, 45);

        DataPackage dataPackage = new DataPackage(Arrays.asList(data1,data2,data3), dirtyFlag);
        for(Data data: dataPackage.getData()) {
            assertEquals(fileSize, data.getFileSize());
            assertEquals(0, data.getReceived());
        }
        assertFalse(dataPackage.isLoaded());
    }

    @Test
    void testReceival() {
        int fileSize = 30;
        DirtyFlag dirtyFlag = new DirtyFlag(Arrays.asList(0,0,1));
        Data data1 = new Data(fileSize, 34);
        Data data2 = new Data(fileSize, 100);
        Data data3 = new Data(fileSize, 45);

        DataPackage dataPackage = new DataPackage(Arrays.asList(data1,data2,data3), dirtyFlag);

        for(Data data: dataPackage.getData()){
            data.increaseReceived(fileSize+1);
        }
        assertTrue(dataPackage.isLoaded());
    }
}
