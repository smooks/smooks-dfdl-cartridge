package org.smooks.cartridges.dfdl;

import org.apache.daffodil.infoset.DIArray;
import org.apache.daffodil.infoset.DIComplex;
import org.apache.daffodil.infoset.DIElement;
import org.apache.daffodil.infoset.DISimple;
import org.apache.daffodil.japi.DataProcessor;
import org.apache.daffodil.japi.Diagnostic;
import org.apache.daffodil.japi.ParseResult;
import org.apache.daffodil.japi.infoset.InfosetOutputter;
import org.apache.daffodil.japi.io.InputSourceDataInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smooks.SmooksException;
import org.smooks.cartridges.dfdl.delivery.AbstractDfdlContentHandlerFactory;
import org.smooks.cdr.annotation.AppContext;
import org.smooks.cdr.annotation.ConfigParam;
import org.smooks.container.ApplicationContext;
import org.smooks.container.ExecutionContext;
import org.smooks.delivery.annotation.Initialize;
import org.smooks.xml.SmooksXMLReader;
import org.xml.sax.*;
import org.xml.sax.helpers.AttributesImpl;
import scala.xml.NamespaceBinding;
import scala.xml.TopScope$;

import javax.xml.XMLConstants;
import java.io.IOException;
import java.util.Map;

import static javax.xml.XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI;

public class DfdlParser implements SmooksXMLReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(DfdlParser.class);
    private static final char[] INDENT = "\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t".toCharArray();

    protected DataProcessor dataProcessor;

    @AppContext
    protected ApplicationContext applicationContext;

    @ConfigParam(use = ConfigParam.Use.REQUIRED)
    private String dataProcessorName;

    @ConfigParam(defaultVal = "false")
    private Boolean indent;

    private ContentHandler contentHandler;
    private ErrorHandler errorHandler;
    private DTDHandler dtdHandler;

    @Override
    public void setExecutionContext(final ExecutionContext executionContext) {

    }

    @Override
    public boolean getFeature(final String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        return false;
    }

    @Override
    public void setFeature(String name, final boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {

    }

    @Override
    public Object getProperty(final String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        return null;
    }

    @Override
    public void setProperty(String name, final Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
    }

    @Override
    public void setEntityResolver(final EntityResolver resolver) {

    }

    @Override
    public EntityResolver getEntityResolver() {
        return null;
    }

    @Override
    public void setDTDHandler(final DTDHandler dtdHandler) {
        this.dtdHandler = dtdHandler;
    }

    @Override
    public DTDHandler getDTDHandler() {
        return dtdHandler;
    }

    @Override
    public void setContentHandler(final ContentHandler contentHandler) {
        this.contentHandler = contentHandler;
    }

    @Override
    public ContentHandler getContentHandler() {
        return contentHandler;
    }

    @Override
    public void setErrorHandler(final ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    @Override
    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    @Initialize
    public void initialize() {
        final Map<String, DataProcessor> dataProcessors = (Map<String, DataProcessor>) applicationContext.getAttribute(AbstractDfdlContentHandlerFactory.class);
        dataProcessor = dataProcessors.get(dataProcessorName);
    }

    @Override
    public void parse(final InputSource input) {
        final ParseResult parseResult = dataProcessor.parse(new InputSourceDataInputStream(input.getByteStream()), new InfosetOutputter() {
            private int elementLevel = 0;

            @Override
            public void reset() {

            }

            @Override
            public boolean startDocument() {
                try {
                    contentHandler.startDocument();
                } catch (SAXException e) {
                    throw new SmooksException(e.getMessage(), e);
                }
                return true;
            }

            @Override
            public boolean endDocument() {
                try {
                    contentHandler.endDocument();
                } catch (SAXException e) {
                    throw new SmooksException(e.getMessage(), e);
                }
                return true;
            }

            @Override
            public boolean startSimple(DISimple diSimple) {
                try {
                    final AttributesImpl attributes = createAttributes(diSimple);
                    if (isNilled(diSimple)) {
                        attributes.addAttribute(W3C_XML_SCHEMA_INSTANCE_NS_URI, "nil", "xsi:nil", "NMTOKEN", "true");
                    }
                    indent(elementLevel);
                    contentHandler.startElement(diSimple.erd().targetNamespace().toString(), diSimple.erd().name(), getQName(diSimple), attributes);
                    if (!isNilled(diSimple) && diSimple.hasValue()) {
                        contentHandler.characters(diSimple.dataValueAsString().toCharArray(), 0, diSimple.dataValueAsString().length());
                    }
                } catch (Exception e) {
                    throw new SmooksException(e.getMessage(), e);
                }
                return true;
            }

            @Override
            public boolean endSimple(final DISimple diSimple) {
                try {
                    contentHandler.endElement(diSimple.erd().targetNamespace().toString(), diSimple.erd().name(), getQName(diSimple));
                } catch (Exception e) {
                    throw new SmooksException(e.getMessage(), e);
                }
                return true;
            }

            @Override
            public boolean startComplex(final DIComplex diComplex) {
                try {
                    indent(elementLevel);
                    contentHandler.startElement(diComplex.erd().targetNamespace().toString(), diComplex.erd().name(), getQName(diComplex), createAttributes(diComplex));
                    elementLevel++;
                    if (diComplex.isEmpty()) {
                        elementLevel--;
                        contentHandler.endElement(diComplex.erd().targetNamespace().toString(), diComplex.erd().name(), getQName(diComplex));
                    }
                } catch (SAXException e) {
                    throw new SmooksException(e.getMessage(), e);
                }
                return true;
            }

            private AttributesImpl createAttributes(final DIElement diElement) {
                final NamespaceBinding nsbStart = diElement.erd().minimizedScope();
                final NamespaceBinding nsbEnd = diElement.isRoot() ? TopScope$.MODULE$ : diElement.diParent().erd().minimizedScope();
                final AttributesImpl attributes = new AttributesImpl();
                if (nsbStart != nsbEnd) {
                    NamespaceBinding namespaceBinding = nsbStart;
                    while (namespaceBinding != TopScope$.MODULE$) {
                        attributes.addAttribute(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, namespaceBinding.prefix(), XMLConstants.XMLNS_ATTRIBUTE + ":" + namespaceBinding.prefix(), "CDATA", namespaceBinding.uri());
                        namespaceBinding = namespaceBinding.copy$default$3();
                    }
                }

                return attributes;
            }

            @Override
            public boolean endComplex(final DIComplex diComplex) {
                try {
                    elementLevel--;
                    indent(elementLevel);
                    contentHandler.endElement(diComplex.erd().targetNamespace().toString(), diComplex.erd().name(), getQName(diComplex));
                } catch (SAXException e) {
                    throw new SmooksException(e.getMessage(), e);
                }
                return true;
            }

            @Override
            public boolean startArray(DIArray diArray) {
                return true;
            }

            @Override
            public boolean endArray(DIArray diArray) {
                return true;
            }

            private String getQName(DIElement diElement) {
                final String prefix = diElement.erd().thisElementsNamespacePrefix();
                return (prefix == null || prefix == "") ? "" : prefix + ":" + diElement.erd().name();
            }
        });

        for (Diagnostic diagnostic : parseResult.getDiagnostics()) {
            if (diagnostic.isError()) {
                throw new SmooksException(diagnostic.getSomeMessage(), diagnostic.getSomeCause());
            } else {
                LOGGER.warn(diagnostic.getMessage());
            }
        }
    }

    @Override
    public void parse(String systemId) throws IOException, SAXException {

    }

    private void indent(final int elementLevel) throws SAXException {
        if (indent) {
            contentHandler.characters(INDENT, 0, elementLevel + 1);
        }
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public String getDataProcessorName() {
        return dataProcessorName;
    }

    public void setDataProcessorName(String dataProcessorName) {
        this.dataProcessorName = dataProcessorName;
    }

    public void setIndent(Boolean indent) {
        this.indent = indent;
    }
}
