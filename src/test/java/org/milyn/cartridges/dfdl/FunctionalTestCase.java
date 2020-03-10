package org.milyn.cartridges.dfdl;

import org.junit.jupiter.api.Test;
import org.milyn.Smooks;
import org.milyn.io.StreamUtils;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.milyn.SmooksUtil.filterAndSerialize;

public class FunctionalTestCase {

    @Test
    public void testSmooksConfig() throws Exception {
        Smooks smooks = new Smooks("/smooks-config.xml");
        String result = filterAndSerialize(smooks.createExecutionContext(), getClass().getResourceAsStream("/data/simpleCSV.comma.csv"), smooks);

        assertTrue(StreamUtils.compareCharStreams(StreamUtils.readStreamAsString(getClass().getResourceAsStream("/data/simpleCSV.comma.csv")), result));
    }

    @Test
    public void testSmooksConfigGivenVariables() throws Exception {
        Smooks smooks = new Smooks("/smooks-variables-config.xml");
        String result = filterAndSerialize(smooks.createExecutionContext(), getClass().getResourceAsStream("/data/simpleCSV.tilde.csv"), smooks);

        assertTrue(StreamUtils.compareCharStreams(StreamUtils.readStreamAsString(getClass().getResourceAsStream("/data/simpleCSV.pipe.csv")), result));
    }
}
