package org.milyn.dfdl;

import org.apache.daffodil.infoset.DIArray;
import org.apache.daffodil.infoset.DIComplex;
import org.apache.daffodil.infoset.DIElement;
import org.apache.daffodil.infoset.DISimple;
import org.apache.daffodil.japi.*;
import org.apache.daffodil.japi.infoset.InfosetOutputter;
import org.apache.daffodil.japi.io.InputSourceDataInputStream;
import org.milyn.SmooksException;
import org.milyn.cdr.annotation.AppContext;
import org.milyn.cdr.annotation.ConfigParam;
import org.milyn.container.ApplicationContext;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.annotation.Initialize;
import org.milyn.resource.URIResourceLocator;
import org.milyn.util.ClassUtil;
import org.milyn.xml.SmooksXMLReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.*;
import org.xml.sax.helpers.AttributesImpl;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static javax.xml.XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI;

public class DFDLReader implements SmooksXMLReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(DFDLReader.class);
    private static final char[] INDENT = "\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t".toCharArray();

    @AppContext
    private ApplicationContext applicationContext;

    @ConfigParam(use = ConfigParam.Use.REQUIRED)
    private String schemaUri;

    @ConfigParam(defaultVal = "false")
    private Boolean validateDFDLSchemas;

    @ConfigParam(defaultVal = "false")
    private Boolean indent;

    private ContentHandler contentHandler;
    private ErrorHandler errorHandler;
    private DTDHandler dtdHandler;

    @Initialize
    public void initialize() {
        if (applicationContext.getAttribute(DFDLReader.class) == null) {
            synchronized (DFDLReader.class) {
                if (applicationContext.getAttribute(DFDLReader.class) == null) {
                    applicationContext.setAttribute(DFDLReader.class, new ConcurrentHashMap<>());
                }
            }
        }
    }

    private DataProcessor compileSchema(final String uriAsString, final boolean validateDFDLSchemas) {
        try {
            final URI uri = URI.create(uriAsString);
            final URI newUri;
            if (uri.getScheme().equals(URIResourceLocator.SCHEME_CLASSPATH)) {
                final List<URL> resources = ClassUtil.getResources(uri.getRawSchemeSpecificPart(), DFDLReader.class);
                if (resources.isEmpty()) {
                    final IOException ioException = new IOException("Failed to read DFDL schema: " + uri);
                    throw new SmooksException(ioException.getMessage(), ioException);
                } else {
                    newUri = resources.get(0).toURI();
                }
            } else {
                newUri = uri;
            }

            LOGGER.info("Compiling and caching DFDL schema...");
            final org.apache.daffodil.japi.Compiler compiler = Daffodil.compiler();
            compiler.setValidateDFDLSchemas(validateDFDLSchemas);

            final ProcessorFactory processorFactory = compiler.compileSource(newUri);
            if (processorFactory.isError()) {
                final List<Diagnostic> diagnostics = processorFactory.getDiagnostics();
                throw diagnostics.get(0).getSomeCause();
            }
            final DataProcessor dataProcessor = processorFactory.onPath("/");
            if (dataProcessor.isError()) {
                final List<Diagnostic> diagnostics = dataProcessor.getDiagnostics();
                for (Diagnostic diagnostic : diagnostics) {
                    throw diagnostic.getSomeCause();
                }
            }

            return dataProcessor;
        } catch (Throwable t) {
            throw new SmooksException(t.getMessage(), t);
        }
    }

    @Override
    public void setExecutionContext(final ExecutionContext executionContext) {

    }

    @Override
    public boolean getFeature(final String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        return false;
    }

    @Override
    public void setFeature(final String name, final boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {

    }

    @Override
    public Object getProperty(final String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        return null;
    }

    @Override
    public void setProperty(final String name, final Object value) throws SAXNotRecognizedException, SAXNotSupportedException {

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

    @Override
    public void parse(final InputSource input) {
        final Map<String, DataProcessor> schemas = (Map<String, DataProcessor>) applicationContext.getAttribute(DFDLReader.class);
        final DataProcessor dataProcessor = schemas.computeIfAbsent(schemaUri, k -> compileSchema(schemaUri, validateDFDLSchemas));
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
                    final AttributesImpl attributes = new AttributesImpl();
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
                    contentHandler.startElement(diComplex.erd().targetNamespace().uri().toString(), diComplex.erd().name(), getQName(diComplex), new AttributesImpl());
                    elementLevel++;
                    if (diComplex.isEmpty()) {
                        elementLevel--;
                        contentHandler.endElement(diComplex.erd().targetNamespace().uri().toString(), diComplex.erd().name(), getQName(diComplex));
                    }
                } catch (SAXException e) {
                    throw new SmooksException(e.getMessage(), e);
                }
                return true;
            }

            @Override
            public boolean endComplex(final DIComplex diComplex) {
                try {
                    elementLevel--;
                    indent(elementLevel);
                    contentHandler.endElement(diComplex.erd().targetNamespace().uri().toString(), diComplex.erd().name(), getQName(diComplex));
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
}
