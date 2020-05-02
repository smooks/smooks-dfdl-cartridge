package org.smooks.cartridges.dfdl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.smooks.Smooks;
import org.smooks.io.StreamUtils;
import org.xml.sax.SAXException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.smooks.SmooksUtil.filterAndSerialize;

public class FunctionalTestCase {

    private Smooks smooks;

    @BeforeEach
    public void beforeEach() throws IOException, SAXException {
        smooks = new Smooks();
    }

    @AfterEach
    public void afterEach() throws IOException, SAXException {
        smooks.close();
    }

    @Test
    public void testSmooksConfig() throws Exception {
        smooks.addConfigurations("/smooks-config.xml");
        String result = filterAndSerialize(smooks.createExecutionContext(), getClass().getResourceAsStream("/data/simpleCSV.comma.csv"), smooks);

        assertTrue(StreamUtils.compareCharStreams(StreamUtils.readStreamAsString(getClass().getResourceAsStream("/data/simpleCSV.comma.csv")), result));
    }

    @Test
    public void testSmooksConfigGivenVariables() throws Exception {
        smooks.addConfigurations("/smooks-variables-config.xml");
        String result = filterAndSerialize(smooks.createExecutionContext(), getClass().getResourceAsStream("/data/simpleCSV.tilde.csv"), smooks);

        assertTrue(StreamUtils.compareCharStreams(StreamUtils.readStreamAsString(getClass().getResourceAsStream("/data/simpleCSV.pipe.csv")), result));
    }

    @Test
    public void testSmooksConfigGivenCacheOnDiskAttributeIsSetToTrue() throws Exception {
        smooks.addConfigurations("/smooks-cacheOnDisk-config.xml");
        String result = filterAndSerialize(smooks.createExecutionContext(), getClass().getResourceAsStream("/data/simpleCSV.comma.csv"), smooks);

        assertTrue(StreamUtils.compareCharStreams(StreamUtils.readStreamAsString(getClass().getResourceAsStream("/data/simpleCSV.comma.csv")), result));
    }

    @Test
    public void testSmooksConfigGivenDebuggingAttributeIsSetToTrue() throws Exception {
        smooks.addConfigurations("/smooks-debugging-config.xml");
        String result = filterAndSerialize(smooks.createExecutionContext(), getClass().getResourceAsStream("/data/simpleCSV.comma.csv"), smooks);

        assertTrue(StreamUtils.compareCharStreams(StreamUtils.readStreamAsString(getClass().getResourceAsStream("/data/simpleCSV.comma.csv")), result));
    }
}
