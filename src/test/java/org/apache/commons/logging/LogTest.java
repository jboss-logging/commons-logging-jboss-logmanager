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

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import org.jboss.logmanager.ExtHandler;
import org.jboss.logmanager.ExtLogRecord;
import org.jboss.logmanager.Level;
import org.jboss.logmanager.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class LogTest {

    private final Logger rootLogger = Logger.getLogger("");

    private QueuedHandler handler;

    @Before
    public void setup() throws Exception {
        handler = new QueuedHandler();
        rootLogger.addHandler(handler);
        rootLogger.setLevel(Level.ALL);
    }

    @After
    public void tearDown() throws Exception {
        rootLogger.removeHandler(handler);
        handler.close();
    }

    @Test
    public void testLogger() throws Exception {
        final Log log = LogFactory.getLog(LogTest.class);
        Assert.assertTrue(log instanceof JBossLog);
        log.info("Test message");
        final ExtLogRecord record = handler.queue.poll();
        Assert.assertNotNull(record);
        Assert.assertEquals("Test message", record.getMessage());
    }

    @Test
    public void testLogLevels() throws Exception {
        final Log log = LogFactory.getLog(LogTest.class);
        final String msg = "Test log level";

        // Test fatal
        logAndValidate(log, msg, Level.FATAL);

        // Test error
        logAndValidate(log, msg, Level.ERROR, Level.FATAL);

        // Test warning
        logAndValidate(log, msg, Level.WARN, Level.FATAL, Level.ERROR);

        // Test info
        logAndValidate(log, msg, Level.INFO, Level.FATAL, Level.ERROR, Level.WARN);

        // Test debug
        logAndValidate(log, msg, Level.DEBUG, Level.FATAL, Level.ERROR, Level.WARN, Level.INFO);

        // Test trace
        logAndValidate(log, msg, Level.TRACE, Level.FATAL, Level.ERROR, Level.WARN, Level.INFO, Level.DEBUG);
    }

    private void logAndValidate(final Log log, final String msg, final Level level, final Level... expectedOtherLogLevels) {
        rootLogger.setLevel(level);
        logAllLevels(log, msg);
        final int expectedEntries = expectedOtherLogLevels.length + 1;
        Assert.assertEquals(String.format("Expected %d log entries but found %d.", expectedEntries, handler.queue.size()),
                expectedEntries, handler.queue.size());
        ExtLogRecord record = handler.queue.pollLast();
        Assert.assertNotNull("Found a null record", record);
        Assert.assertEquals(msg + " " + level.getName(), record.getMessage());
        for (Level l : expectedOtherLogLevels) {
            record = handler.queue.poll();
            Assert.assertNotNull("Found a null record", record);
            Assert.assertEquals(msg + " " + l.getName(), record.getMessage());
        }
    }

    private void logAllLevels(final Log log, final String msg) {
        log.fatal(msg + " " + Level.FATAL.getName());
        log.error(msg + " " + Level.ERROR.getName());
        log.warn(msg + " " + Level.WARN.getName());
        log.info(msg + " " + Level.INFO.getName());
        log.debug(msg + " " + Level.DEBUG.getName());
        log.trace(msg + " " + Level.TRACE.getName());
    }

    private static class QueuedHandler extends ExtHandler {

        final BlockingDeque<ExtLogRecord> queue;

        private QueuedHandler() {
            queue = new LinkedBlockingDeque<ExtLogRecord>();
        }

        @Override
        protected void doPublish(final ExtLogRecord record) {
            queue.addLast(record);
        }

        @Override
        public void close() {
            try {
                queue.clear();
            } finally {
                super.close();
            }
        }
    }
}
