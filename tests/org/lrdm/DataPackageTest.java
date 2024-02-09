package org.lrdm;


import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;

class DataPackageTest {
    @Test
    void testGetAndSet() {
        int fileSize = 30;
        DirtyFlag dirtyFlag = new DirtyFlag(Arrays.asList(0,0,1));
        DirtyFlag dirtyFlag2 = new DirtyFlag(Arrays.asList(4,5,6));
        Data data1 = new Data(fileSize, 34);
        Data data2 = new Data(fileSize, 100);
        Data data3 = new Data(fileSize, 45);

        DataPackage dataPackage = new DataPackage(Arrays.asList(data1,data2,data3), dirtyFlag);
        assertTrue(dataPackage.getDirtyFlag().equalDirtyFlag(dirtyFlag.getFlag()));
        assertFalse(dataPackage.getInvalid());
        for(Data data: dataPackage.getData()) {
            assertEquals(fileSize, data.getFileSize());
            assertEquals(0, data.getReceived());
        }
        assertEquals(90, dataPackage.getFileSize());
        assertEquals(0, dataPackage.getReceived());

        dataPackage.setDirtyFlag(dirtyFlag2);
        dataPackage.setInvalid(true);

        assertTrue(dataPackage.getDirtyFlag().equalDirtyFlag(dirtyFlag2.getFlag()));
        assertTrue(dataPackage.getInvalid());
    }

    @Test
    void testReceived() {
        int fileSize = 30;
        DirtyFlag dirtyFlag = new DirtyFlag(Arrays.asList(0,0,1));
        Data data1 = new Data(fileSize, 34);
        Data data2 = new Data(fileSize, 100);
        Data data3 = new Data(fileSize, 45);

        DataPackage dataPackage = new DataPackage(Arrays.asList(data1,data2,data3), dirtyFlag);

        for(Data data: dataPackage.getData()){
            data.increaseReceived(fileSize);
        }
        assertEquals(90, dataPackage.getFileSize());
        assertEquals(90, dataPackage.getReceived());
        assertTrue(dataPackage.isLoaded());
    }
}
