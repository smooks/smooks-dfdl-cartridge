package org.milyn.cartridges.dfdl;

import org.apache.daffodil.japi.DataProcessor;
import org.apache.daffodil.japi.Diagnostic;
import org.apache.daffodil.japi.UnparseResult;
import org.apache.daffodil.japi.infoset.W3CDOMInfosetInputter;
import org.milyn.SmooksException;
import org.milyn.cdr.SmooksConfigurationException;
import org.milyn.cdr.annotation.ConfigParam;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.ContentDeliveryConfigBuilderLifecycleEvent;
import org.milyn.delivery.ContentDeliveryConfigBuilderLifecycleListener;
import org.milyn.delivery.Fragment;
import org.milyn.delivery.VisitLifecycleCleanable;
import org.milyn.delivery.dom.DOMVisitAfter;
import org.milyn.delivery.dom.serialize.TextSerializationUnit;
import org.milyn.delivery.ordering.Producer;
import org.milyn.xml.DomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.ByteArrayOutputStream;
import java.nio.channels.Channels;
import java.util.Set;

public class DFDLUnparser implements DOMVisitAfter, ContentDeliveryConfigBuilderLifecycleListener, Producer, VisitLifecycleCleanable {

    private static final Logger LOGGER = LoggerFactory.getLogger(DFDLUnparser.class);
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

    public DFDLUnparser(final DataProcessor dataProcessor) {
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
