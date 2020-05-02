package org.smooks.cartridges.dfdl.parser;

import org.apache.daffodil.japi.ValidationMode;
import org.junit.jupiter.api.Test;
import org.smooks.Smooks;
import org.smooks.cartridges.dfdl.AbstractTestCase;
import org.smooks.cartridges.dfdl.TestKit;
import org.smooks.io.StreamUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.smooks.SmooksUtil.filterAndSerialize;

public class DfdlReaderConfiguratorTestCase extends AbstractTestCase {
    @Test
    public void testToConfig() throws IOException {
        DfdlReaderConfigurator dfdlReaderConfigurator = new DfdlReaderConfigurator("/csv.dfdl.xsd").setDebugging(ThreadLocalRandom.current().nextBoolean()).setCacheOnDisk(ThreadLocalRandom.current().nextBoolean()).setIndent(ThreadLocalRandom.current().nextBoolean());
        if (dfdlReaderConfigurator.getCacheOnDisk()) {
            dfdlReaderConfigurator.setValidationMode(TestKit.getRandomItem(TestKit.getCacheOnDiskSupportedValidationModes()));
        } else {
            dfdlReaderConfigurator.setValidationMode(TestKit.getRandomItem(Arrays.asList(ValidationMode.values())));
        }

        Smooks smooks = new Smooks();
        smooks.setReaderConfig(dfdlReaderConfigurator);

        String result = filterAndSerialize(smooks.createExecutionContext(), getClass().getResourceAsStream("/data/simpleCSV.comma.csv"), smooks);
        assertTrue(StreamUtils.compareCharStreams(StreamUtils.readStreamAsString(getClass().getResourceAsStream("/data/simpleCSV.xml")), result));
    }
}
