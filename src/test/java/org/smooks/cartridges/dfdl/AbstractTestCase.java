package org.smooks.cartridges.dfdl;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;

public abstract class AbstractTestCase {

    @BeforeEach
    public void beforeEach() {
        reset();
        doBefore();
    }

    protected void doBefore() {

    }

    public void reset() {
        FileUtils.deleteQuietly(new File(DfdlSchema.WORKING_DIRECTORY));
    }
}
