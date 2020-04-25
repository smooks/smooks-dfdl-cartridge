package org.smooks.cartridges.dfdl.unparser;

import org.apache.daffodil.japi.DataProcessor;
import org.apache.daffodil.japi.Diagnostic;
import org.apache.daffodil.japi.UnparseResult;
import org.apache.daffodil.japi.infoset.W3CDOMInfosetInputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smooks.SmooksException;
import org.smooks.cdr.SmooksConfigurationException;
import org.smooks.cdr.annotation.ConfigParam;
import org.smooks.container.ExecutionContext;
import org.smooks.delivery.ContentDeliveryConfigBuilderLifecycleEvent;
import org.smooks.delivery.ContentDeliveryConfigBuilderLifecycleListener;
import org.smooks.delivery.Fragment;
import org.smooks.delivery.VisitLifecycleCleanable;
import org.smooks.delivery.dom.DOMVisitAfter;
import org.smooks.delivery.dom.serialize.TextSerializationUnit;
import org.smooks.delivery.ordering.Producer;
import org.smooks.xml.DomUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.ByteArrayOutputStream;
import java.nio.channels.Channels;
import java.util.Set;

public class DfdlUnparser implements DOMVisitAfter, ContentDeliveryConfigBuilderLifecycleListener, Producer, VisitLifecycleCleanable {

    private static final Logger LOGGER = LoggerFactory.getLogger(DfdlUnparser.class);
    private final DataProcessor dataProcessor;

    @ConfigParam(use = ConfigParam.Use.REQUIRED)
    private String schemaURI;

    @ConfigParam(use = ConfigParam.Use.OPTIONAL)
    private String outputStreamResource;

    @Override
    public void handle(ContentDeliveryConfigBuilderLifecycleEvent event) throws SmooksConfigurationException {

    }

    @Override
    public void executeVisitLifecycleCleanup(Fragment fragment, ExecutionContext executionContext) {

    }

    public DfdlUnparser(final DataProcessor dataProcessor) {
        this.dataProcessor = dataProcessor;
    }

    @Override
    public void visitAfter(final Element element, final ExecutionContext executionContext) throws SmooksException {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final UnparseResult unparseResult = dataProcessor.unparse(new W3CDOMInfosetInputter(element.getOwnerDocument()), Channels.newChannel(byteArrayOutputStream));
        for (Diagnostic diagnostic : unparseResult.getDiagnostics()) {
            if (diagnostic.isError()) {
                throw new SmooksException(diagnostic.getSomeMessage(), diagnostic.getSomeCause());
            } else {
                LOGGER.warn(diagnostic.getMessage());
            }
        }
        final Node resultNode = TextSerializationUnit.createTextElement(element, byteArrayOutputStream.toString());
        DomUtils.replaceNode(resultNode, element);
    }

    @Override
    public Set<? extends Object> getProducts() {
        return null;
    }
}
