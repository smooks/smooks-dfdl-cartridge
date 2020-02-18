package org.milyn.dfdl;

import org.junit.jupiter.api.Test;
import org.milyn.Smooks;
import org.milyn.io.StreamUtils;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.milyn.SmooksUtil.filterAndSerialize;

public class FunctionalTestCase {

    @Test
    public void testSmooksConfig() throws Exception {
        Smooks smooks = new Smooks("/smooks-config.xml");
        String result = filterAndSerialize(smooks.createExecutionContext(), getClass().getResourceAsStream("/simpleCSV.csv"), smooks);

        assertTrue(StreamUtils.compareCharStreams(StreamUtils.readStreamAsString(getClass().getResourceAsStream("/simpleCSV.csv")), result));
    }
}
