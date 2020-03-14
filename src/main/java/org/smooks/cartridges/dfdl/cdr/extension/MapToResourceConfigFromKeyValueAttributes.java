/*
	Milyn - Copyright (C) 2006 - 2010

	This library is free software; you can redistribute it and/or
	modify it under the terms of the GNU Lesser General Public
	License (version 2.1) as published by the Free Software
	Foundation.

	This library is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

	See the GNU Lesser General Public License for more details:
	http://www.gnu.org/licenses/lgpl.txt
*/
package org.smooks.cartridges.dfdl.cdr.extension;

import org.smooks.SmooksException;
import org.smooks.cdr.Parameter;
import org.smooks.cdr.SmooksResourceConfiguration;
import org.smooks.cdr.annotation.ConfigParam;
import org.smooks.cdr.extension.ExtensionContext;
import org.smooks.container.ExecutionContext;
import org.smooks.delivery.dom.DOMVisitBefore;
import org.smooks.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.AbstractMap;

public class MapToResourceConfigFromKeyValueAttributes implements DOMVisitBefore {

    @ConfigParam
    private String mapTo;

    @ConfigParam
    private String keyAttribute;

    @ConfigParam
    private String valueAttribute;

    public void visitBefore(Element element, ExecutionContext executionContext) throws SmooksException {
        final SmooksResourceConfiguration config = ExtensionContext.getExtensionContext(executionContext).getResourceStack().peek();
        final String key = DomUtils.getAttributeValue(element, keyAttribute);
        final String value = DomUtils.getAttributeValue(element, valueAttribute);

        config.setParameter(new Parameter(mapTo, new AbstractMap.SimpleEntry(key, value)));
    }
}