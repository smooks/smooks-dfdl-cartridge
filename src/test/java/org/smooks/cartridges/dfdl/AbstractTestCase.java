package org.smooks.cartridges.dfdl;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;

public abstract class AbstractTestCase {

    @BeforeEach
    public void beforeEach() {
        reset();
        doBeforeEach();
    }

    protected void doBeforeEach() {

    }

    public void reset() {
        FileUtils.deleteQuietly(new File(DfdlSchema.WORKING_DIRECTORY));
    }
}
