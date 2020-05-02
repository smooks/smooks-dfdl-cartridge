package org.smooks.cartridges.dfdl;

import org.apache.commons.io.FileUtils;
import org.apache.daffodil.japi.DataProcessor;
import org.apache.daffodil.japi.ValidationMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;

public class DfdlSchemaTestCase {

    @BeforeEach
    public void beforeEach() {
        FileUtils.deleteQuietly(new File(DfdlSchema.WORKING_DIRECTORY));
    }

    @Test
    public void testCompileHitsCacheGivenCacheOnDiskIsSetToTrue() throws Throwable {
        CountDownLatch countDownLatch = new CountDownLatch(2);
        DfdlSchema dfdlSchema = new DfdlSchema(new URI("/csv.dfdl.xsd"), new HashMap<>(), ValidationMode.values()[ThreadLocalRandom.current().nextInt(ValidationMode.values().length)], true, ThreadLocalRandom.current().nextBoolean()) {
            @Override
            protected DataProcessor compileSource() throws Throwable {
                countDownLatch.countDown();
                return super.compileSource();
            }
        };
        dfdlSchema.compile();
        assertEquals(1, countDownLatch.getCount());
        dfdlSchema.compile();
        assertEquals(1, countDownLatch.getCount());
    }

    @Test
    public void testCompileGivenCacheOnDiskIsSetToTrue() throws Throwable {
        DfdlSchema dfdlSchema = new DfdlSchema(new URI("/csv.dfdl.xsd"), new HashMap<>(), ValidationMode.values()[ThreadLocalRandom.current().nextInt(ValidationMode.values().length)], true, ThreadLocalRandom.current().nextBoolean());
        dfdlSchema.compile();
        assertTrue(new File(DfdlSchema.WORKING_DIRECTORY + "/csv.dfdl.xsd.dat").exists());
    }

    @Test
    public void testCompileGivenCacheOnDiskIsSetToFalse() throws Throwable {
        DfdlSchema dfdlSchema = new DfdlSchema(new URI("/csv.dfdl.xsd"), new HashMap<>(), ValidationMode.values()[ThreadLocalRandom.current().nextInt(ValidationMode.values().length)], false, ThreadLocalRandom.current().nextBoolean());
        dfdlSchema.compile();
        assertFalse(new File(DfdlSchema.WORKING_DIRECTORY + "/csv.dfdl.xsd.dat").exists());
    }
}
