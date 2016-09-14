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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jboss.logmanager.Level;
import org.jboss.logmanager.LogContext;
import org.jboss.logmanager.Logger;
import org.jboss.logmanager.Logger.AttachmentKey;

/**
 * An implementation of Apache Commons Logging {@code LogFactory} for JBoss Log Manager.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
class JBossLogFactory extends LogFactory {
    private static final Collection<String> UNSUPPORTED_PROPERTIES = Arrays.asList(
            LogFactory.FACTORY_PROPERTY,
            "org.apache.commons.org.apache.commons.logging.Log",
            "org.apache.commons.org.apache.commons.logging.log"
    );
    private static final AttachmentKey<Log> LOG_KEY = new AttachmentKey<Log>();
    private static final AttachmentKey<Map<String, Object>> ATTRIBUTE_KEY = new AttachmentKey<Map<String, Object>>();

    private final Logger logger = Logger.getLogger(JBossLogFactory.class.getPackage().getName());

    @Override
    public Object getAttribute(final String name) {
        return getAttributeMap().get(name);
    }


    @Override
    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public String[] getAttributeNames() {
        final Map<String, Object> attributes = getAttributeMap();
        final String[] names;
        synchronized (attributes) {
            final Set<String> s = attributes.keySet();
            names = s.toArray(new String[s.size()]);
        }
        return names;
    }

    @Override
    public Log getInstance(final Class clazz) throws LogConfigurationException {
        return getInstance(clazz.getName());
    }

    @Override
    public Log getInstance(final String name) throws LogConfigurationException {
        Log log = LogContext.getLogContext().getAttachment(name, LOG_KEY);
        if (log != null) {
            return log;
        }
        final Logger logger = Logger.getLogger(name);
        log = new JBossLog(logger);
        final Log appearing = logger.attachIfAbsent(LOG_KEY, log);
        if (appearing != null) {
            log = appearing;
        }
        return log;
    }

    @Override
    public void release() {
        // Clear the attributes
        getAttributeMap().clear();
    }

    @Override
    public void removeAttribute(final String name) {
        getAttributeMap().remove(name);
    }

    @Override
    public void setAttribute(final String name, final Object value) {
        final Map<String, Object> attributes = getAttributeMap();
        if (value == null) {
            attributes.remove(name);
        } else {
            if (!(value instanceof String)) {
                logger.log(Level.WARN, String.format("Attribute values must be of type java.lang.String. Attribute %s with value %s will be ignored.", name, value));
            } else if (UNSUPPORTED_PROPERTIES.contains(name)) {
                logger.log(Level.WARN, String.format("Attribute %s is not supported. Value %s will be ignored.", name, value));
            } else {
                attributes.put(name, value);
            }
        }
    }

    private Map<String, Object> getAttributeMap() {
        final Logger rootLogger = Logger.getLogger("");
        Map<String, Object> map = rootLogger.getAttachment(ATTRIBUTE_KEY);
        if (map == null) {
            map = Collections.synchronizedMap(new HashMap<String, Object>());
            final Map<String, Object> appearing = rootLogger.attachIfAbsent(ATTRIBUTE_KEY, map);
            if (appearing != null) {
                map = appearing;
            }
        }
        return map;
    }
}
