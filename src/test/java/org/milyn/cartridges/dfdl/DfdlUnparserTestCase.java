package org.milyn.cartridges.dfdl;

import org.apache.daffodil.japi.Daffodil;
import org.apache.daffodil.japi.DataProcessor;
import org.apache.daffodil.japi.ProcessorFactory;
import org.junit.jupiter.api.Test;
import org.milyn.container.MockExecutionContext;
import org.milyn.io.StreamUtils;
import org.milyn.xml.XmlUtil;
import org.w3c.dom.Document;
import scala.Predef;
import scala.collection.JavaConverters;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DfdlUnparserTestCase {

    @Test
    public void testVisitAfter() throws Exception {
        org.apache.daffodil.japi.Compiler compiler = Daffodil.compiler();
        ProcessorFactory processorFactory = compiler.compileSource(getClass().getResource("/csv.dfdl.xsd").toURI());
        DataProcessor dataProcessor = processorFactory.onPath("/");
        dataProcessor.setExternalVariables(JavaConverters.mapAsScalaMapConverter(new HashMap<String, String>(){{
            this.put("{http://example.com}Delimiter", ",");
        }}).asScala().toMap(Predef.$conforms()));

        Document document = XmlUtil.parseStream(getClass().getResourceAsStream("/data/simpleCSV.xml"));

        DfdlUnparser dfdlUnparser = new DfdlUnparser(dataProcessor);
        dfdlUnparser.visitAfter(document.getDocumentElement(), new MockExecutionContext());

        assertTrue(StreamUtils.compareCharStreams(StreamUtils.readStreamAsString(getClass().getResourceAsStream("/data/simpleCSV.comma.csv")), document.getDocumentElement().getTextContent()));
    }

}
