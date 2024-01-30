package org.lrdm;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class TestUtils {
    static final Properties props = new Properties();
    public static void loadProperties(String config)  throws IOException {
        props.load(new FileReader(config));
    }
}
