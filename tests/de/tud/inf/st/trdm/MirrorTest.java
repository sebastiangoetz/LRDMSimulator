package de.tud.inf.st.trdm;

import org.junit.jupiter.api.Test;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class MirrorTest {
    Properties props;
    public void loadProperties()  throws IOException {
        props = new Properties();
        props.load(new FileReader("resources/sim-test-1.conf"));
    }
    @Test
    void testMirror() throws IOException {
        loadProperties();
        Mirror m = new Mirror(1, 0, props);
        assertEquals(1, m.getID());
        assertEquals(0, m.getLinks().size());
        assertEquals(0, m.getNumNonClosedLinks());
        assertEquals(Mirror.State.DOWN, m.getState());
        assertNotEquals(0, m.toString().length());
        m.shutdown(0);
        assertEquals(Mirror.State.STOPPING, m.getState());
    }

    @Test
    void testTimes() throws IOException {
        loadProperties();
        int startup_time_min = Integer.parseInt(props.get("startup_time_min").toString());
        int startup_time_max = Integer.parseInt(props.get("startup_time_max").toString());
        int ready_time_min = Integer.parseInt(props.get("ready_time_min").toString());
        int ready_time_max = Integer.parseInt(props.get("ready_time_max").toString());
        for(int i = 0; i < 100; i++) {
            Mirror m = new Mirror(i, 0, props);
            assertTrue(m.getReadyTime() >= ready_time_min && m.getReadyTime() < ready_time_max);
            assertTrue(m.getStartupTime() >= startup_time_min && m.getStartupTime() < startup_time_max);
        }
    }
}
