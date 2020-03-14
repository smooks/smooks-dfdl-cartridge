package org.smooks.cartridges.dfdl.delivery;

import org.apache.daffodil.japi.DataProcessor;
import org.smooks.cartridges.dfdl.DfdlSchema;
import org.smooks.cdr.SmooksConfigurationException;
import org.smooks.cdr.SmooksResourceConfiguration;
import org.smooks.delivery.ContentHandler;
import org.smooks.delivery.annotation.Resource;

@Resource(type = "dfdl-parser")
public class DfdlParserContentHandlerFactory extends AbstractDfdlContentHandlerFactory {
    @Override
    public ContentHandler doCreate(final SmooksResourceConfiguration smooksResourceConfiguration, final DfdlSchema dfdlSchema, final DataProcessor dataProcessor) throws SmooksConfigurationException {
        smooksResourceConfiguration.setParameter("dataProcessorName", dfdlSchema.getName());
        smooksResourceConfiguration.setResource("org.smooks.cartridges.dfdl.DfdlParser");
        return new ContentHandler() {
        };
    }
}
