package de.tud.inf.st.trdm;

import org.junit.jupiter.api.Test;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class MirrorTest {
    Properties props;
    public void loadProperties() {
        props = new Properties();
        try {
            props.load(new FileReader("resources/sim-test-1.conf"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    public void testMirror() {
        loadProperties();
        Mirror m = new Mirror(1, 0, props);
        assertEquals(1, m.getID());
        assertEquals(0, m.getLinks().size());
        assertEquals(0, m.getNumNonClosedLinks());
        assertEquals(Mirror.State.down, m.getState());
        assertNotEquals(0, m.toString().length());
        m.shutdown(0);
        assertEquals(Mirror.State.stopping, m.getState());
    }
}
