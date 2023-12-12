package org.lrdm;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class DataPackageTest {
    @Test
    void testInitialization() {
        DataPackage dp = new DataPackage(100);
        assertEquals(100, dp.getFileSize());
        assertEquals(0,dp.getReceived());
        assertFalse(dp.isLoaded());
    }

    @Test
    void testReceival() {
        DataPackage dp = new DataPackage(50);
        dp.increaseReceived(50);
        assertEquals(50,dp.getFileSize());
        assertEquals(50,dp.getReceived());
        assertTrue(dp.isLoaded());
    }
}
