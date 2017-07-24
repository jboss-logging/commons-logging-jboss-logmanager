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

import java.io.ObjectStreamException;
import java.io.Serializable;

import org.jboss.logmanager.ExtLogRecord;
import org.jboss.logmanager.Level;
import org.jboss.logmanager.Logger;

/**
 * Log implementation that logs to JBoss Log Manager.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
class JBossLog implements Log, Serializable {
    private static final long serialVersionUID = 7757100415375072992L;
    private static final String LOGGER_CLASS_NAME = JBossLog.class.getName();
    private final String name;
    private final transient Logger delegate;

    JBossLog(final Logger delegate) {
        this.delegate = delegate;
        this.name = delegate.getName();
    }

    @Override
    public void debug(final Object message) {
        log(Level.DEBUG, message);
    }

    @Override
    public void debug(final Object message, final Throwable t) {
        log(Level.DEBUG, message, t);
    }

    @Override
    public void error(final Object message) {
        log(Level.ERROR, message);
    }

    @Override
    public void error(final Object message, final Throwable t) {
        log(Level.ERROR, message, t);
    }

    @Override
    public void fatal(final Object message) {
        log(Level.FATAL, message);
    }

    @Override
    public void fatal(final Object message, final Throwable t) {
        log(Level.FATAL, message, t);
    }

    @Override
    public void info(final Object message) {
        log(Level.INFO, message);
    }

    @Override
    public void info(final Object message, final Throwable t) {
        log(Level.INFO, message, t);
    }

    @Override
    public boolean isDebugEnabled() {
        return delegate.isLoggable(Level.DEBUG);
    }

    @Override
    public boolean isErrorEnabled() {
        return delegate.isLoggable(Level.ERROR);
    }

    @Override
    public boolean isFatalEnabled() {
        return delegate.isLoggable(Level.FATAL);
    }

    @Override
    public boolean isInfoEnabled() {
        return delegate.isLoggable(Level.INFO);
    }

    @Override
    public boolean isTraceEnabled() {
        return delegate.isLoggable(Level.TRACE);
    }

    @Override
    public boolean isWarnEnabled() {
        return delegate.isLoggable(Level.WARN);
    }

    @Override
    public void trace(final Object message) {
        log(Level.TRACE, message);
    }

    @Override
    public void trace(final Object message, final Throwable t) {
        log(Level.TRACE, message, t);
    }

    @Override
    public void warn(final Object message) {
        log(Level.WARN, message);
    }

    @Override
    public void warn(final Object message, final Throwable t) {
        log(Level.WARN, message, t);
    }

    private void log(final Level level, final Object message) {
        log(level, message, null);
    }

    private void log(final Level level, final Object message, final Throwable t) {
        final String msg = String.valueOf(message);
        final ExtLogRecord record = new ExtLogRecord(level, msg, LOGGER_CLASS_NAME);
        record.setThrown(t);
        delegate.log(record);
    }

    private Object readResolve() throws ObjectStreamException {
        return LogFactory.getLog(name);
    }
}
