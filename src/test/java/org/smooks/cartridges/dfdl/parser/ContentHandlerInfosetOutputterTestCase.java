/*-
 * ========================LICENSE_START=================================
 * Smooks DFDL Cartridge
 * %%
 * Copyright (C) 2020 - 2021 Smooks
 * %%
 * Licensed under the terms of the Apache License Version 2.0, or
 * the GNU Lesser General Public License version 3.0 or later.
 *
 * SPDX-License-Identifier: Apache-2.0 OR LGPL-3.0-or-later
 *
 * ======================================================================
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ======================================================================
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * =========================LICENSE_END==================================
 */
package org.smooks.cartridges.dfdl.parser;

import org.apache.daffodil.infoset.ContentLengthState;
import org.apache.daffodil.infoset.DIElement;
import org.apache.daffodil.infoset.DINode;
import org.apache.daffodil.infoset.InfosetComplexElement;
import org.apache.daffodil.infoset.ValueLengthState;
import org.apache.daffodil.processors.ElementRuntimeData;
import org.apache.daffodil.xml.GlobalQName;
import org.apache.daffodil.xml.NS;
import org.junit.jupiter.api.Test;
import org.xml.sax.helpers.AttributesImpl;
import scala.collection.IndexedSeq;
import scala.collection.immutable.Stream;
import scala.xml.NamespaceBinding;
import scala.xml.TopScope$;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ContentHandlerInfosetOutputterTestCase {

    private static class StubDIElement implements DIElement {

        private final ElementRuntimeData erd;

        public StubDIElement(ElementRuntimeData erd) {
            this.erd = erd;
        }

        @Override
        public boolean isRoot() {
            return true;
        }

        @Override
        public ContentLengthState _contentLength() {
            return null;
        }

        @Override
        public void _contentLength_$eq(ContentLengthState x$1) {

        }

        @Override
        public ValueLengthState _valueLength() {
            return null;
        }

        @Override
        public void _valueLength_$eq(ValueLengthState x$1) {

        }

        @Override
        public boolean _isNilled() {
            return false;
        }

        @Override
        public void _isNilled_$eq(boolean x$1) {

        }

        @Override
        public int _validity() {
            return 0;
        }

        @Override
        public void _validity_$eq(int x$1) {

        }

        @Override
        public ContentLengthState contentLength() {
            return null;
        }

        @Override
        public ValueLengthState valueLength() {
            return null;
        }

        @Override
        public boolean isSimple() {
            return false;
        }

        @Override
        public boolean isComplex() {
            return false;
        }

        @Override
        public boolean isArray() {
            return false;
        }

        @Override
        public int infosetWalkerBlockCount() {
            return 0;
        }

        @Override
        public IndexedSeq<DINode> contents() {
            return null;
        }

        @Override
        public Object maybeLastChild() {
            return null;
        }

        @Override
        public void freeChildIfNoLongerNeeded(int index, boolean doFree) {

        }

        @Override
        public boolean wouldHaveBeenFreed() {
            return false;
        }

        @Override
        public void wouldHaveBeenFreed_$eq(boolean x$1) {

        }

        @Override
        public boolean isFinal() {
            return false;
        }

        @Override
        public void isFinal_$eq(boolean x$1) {

        }

        @Override
        public void requireFinal() {

        }

        @Override
        public void infosetWalkerBlockCount_$eq(int x$1) {

        }

        @Override
        public boolean isDefaulted() {
            return false;
        }

        @Override
        public Stream<DINode> children() {
            return null;
        }

        @Override
        public long totalElementCount() {
            return 0;
        }

        @Override
        public ElementRuntimeData erd() {
            return erd;
        }

        @Override
        public String valueStringForDebug() {
            return null;
        }

        @Override
        public boolean _isHidden() {
            return false;
        }

        @Override
        public void _isHidden_$eq(boolean x$1) {

        }

        @Override
        public InfosetComplexElement _parent() {
            return null;
        }

        @Override
        public void _parent_$eq(InfosetComplexElement x$1) {

        }

        @Override
        public boolean _isNilledSet() {
            return false;
        }

        @Override
        public void _isNilledSet_$eq(boolean x$1) {

        }

        @Override
        public Object org$apache$daffodil$infoset$DIElement$$_array() {
            return null;
        }

        @Override
        public void org$apache$daffodil$infoset$DIElement$$_array_$eq(Object x$1) {

        }

        @Override
        public boolean isNilled() {
            return false;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }

    @Test
    public void testCreateAttributesGivenUndefinedLocalName() throws URISyntaxException {
        ContentHandlerInfosetOutputter contentHandlerInfosetOutputter = new ContentHandlerInfosetOutputter(null, ThreadLocalRandom.current().nextBoolean());
        ElementRuntimeData erd = new ElementRuntimeData(0, null, null, null, null,
                null, null, null, null, new NamespaceBinding(null, null, TopScope$.MODULE$), null,
                null, null, null, null,
                0, 0, null, null, null,
                ThreadLocalRandom.current().nextBoolean(), ThreadLocalRandom.current().nextBoolean(), ThreadLocalRandom.current().nextBoolean(),
                ThreadLocalRandom.current().nextBoolean(), new GlobalQName(scala.Option.apply(null), null, new NS(new URI(""))), ThreadLocalRandom.current().nextBoolean(),
                ThreadLocalRandom.current().nextBoolean(), 0, false, null,
                null, null, null, null, null,
                null, null, null, ThreadLocalRandom.current().nextBoolean(),
                null);

        AttributesImpl attributes = contentHandlerInfosetOutputter.createAttributes(new StubDIElement(erd));
        assertEquals(0, attributes.getLength());
    }

    @Test
    public void testCreateAttributesGivenLocalName() throws URISyntaxException {
        ContentHandlerInfosetOutputter contentHandlerInfosetOutputter = new ContentHandlerInfosetOutputter(null, ThreadLocalRandom.current().nextBoolean());
        ElementRuntimeData erd = new ElementRuntimeData(0, null, null, null, null,
                null, null, null, null, new NamespaceBinding(null, null, TopScope$.MODULE$), null,
                null, null, null, null,
                0, 0, null, null, null,
                ThreadLocalRandom.current().nextBoolean(), ThreadLocalRandom.current().nextBoolean(), ThreadLocalRandom.current().nextBoolean(),
                ThreadLocalRandom.current().nextBoolean(), new GlobalQName(scala.Option.apply("foo"), null, new NS(new URI(""))), ThreadLocalRandom.current().nextBoolean(),
                ThreadLocalRandom.current().nextBoolean(), 0, false, null,
                null, null, null, null, null,
                null, null, null, ThreadLocalRandom.current().nextBoolean(),
                null);

        AttributesImpl attributes = contentHandlerInfosetOutputter.createAttributes(new StubDIElement(erd));
        assertEquals(1, attributes.getLength());
    }
}
