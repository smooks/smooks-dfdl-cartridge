package org.milyn.dfdl;

import org.junit.jupiter.api.Test;
import org.milyn.Smooks;
import org.milyn.SmooksUtil;
import org.milyn.container.ExecutionContext;
import org.milyn.io.StreamUtils;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DFDLReaderTestCase {

    @Test
    public void testSmooksConfig() throws Exception {
        Smooks smooks = new Smooks("/smooks-config.xml");
        ExecutionContext executionContext = smooks.createExecutionContext();

        String result = SmooksUtil.filterAndSerialize(executionContext, getClass().getResourceAsStream("/simpleCSV.csv"), smooks);
        assertTrue(StreamUtils.compareCharStreams(new ByteArrayInputStream(StreamUtils.readStream(getClass().getResourceAsStream("/simpleCSV.xml"))), new ByteArrayInputStream(result.getBytes())));
    }
}
