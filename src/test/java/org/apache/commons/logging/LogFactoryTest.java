/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2016 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
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
 */

package org.apache.commons.logging;

import java.util.Arrays;
import java.util.ServiceLoader;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class LogFactoryTest {

    @Test
    public void testLogFactory() {
        Assertions.assertTrue(LogFactory.getFactory() instanceof JBossLogFactory);
    }

    @Test
    public void testAttributes() {
        final LogFactory logFactory = LogFactory.getFactory();
        logFactory.setAttribute("test1", "value1");
        Assertions.assertArrayEquals(new String[] { "test1" }, logFactory.getAttributeNames());
        Assertions.assertEquals("value1", logFactory.getAttribute("test1"));
        logFactory.removeAttribute("test1");
        Assertions.assertEquals(0, logFactory.getAttributeNames().length);
        Assertions.assertNull(logFactory.getAttribute("test1"));

        logFactory.setAttribute("test1", "value1");
        logFactory.setAttribute("test2", "value2");
        Assertions.assertTrue(Arrays.asList(logFactory.getAttributeNames()).containsAll(Arrays.asList("test1", "test2")));
        Assertions.assertEquals("value1", logFactory.getAttribute("test1"));
        Assertions.assertEquals("value2", logFactory.getAttribute("test2"));

        logFactory.setAttribute("test3", "value3");
        Assertions.assertTrue(
                Arrays.asList(logFactory.getAttributeNames()).containsAll(Arrays.asList("test1", "test2", "test3")));
        Assertions.assertEquals("value3", logFactory.getAttribute("test3"));
    }

    @Test
    public void testServiceProvider() {
        final ServiceLoader<LogFactory> service = ServiceLoader.load(LogFactory.class);
        Assertions.assertNotNull(service);
        Assertions.assertTrue(service.iterator().hasNext());
        Assertions.assertTrue(service.iterator().next() instanceof JBossLogFactory);
    }
}
