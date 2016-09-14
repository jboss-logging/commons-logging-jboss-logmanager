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

import java.util.Hashtable;

/**
 * <strong>Note this implementation only works with JBoss Log Manager. No configuration can be done via this
 * API.</strong>
 * <p>
 * Factory for creating {@link Log} instances, with discovery and
 * configuration features similar to that employed by standard Java APIs
 * such as JAXP.
 * </p>
 * <p>
 * <strong>IMPLEMENTATION NOTE</strong> - This implementation is heavily
 * based on the SAXParserFactory and DocumentBuilderFactory implementations
 * (corresponding to the JAXP pluggability APIs) found in Apache Xerces.
 *
 * @version $Id: LogFactory.java 1606041 2014-06-27 11:56:59Z tn $
 */
public abstract class LogFactory {

    private static class Holder {
        static final LogFactory LOG_FACTORY = new JBossLogFactory();
    }

    // Implementation note re AccessController usage
    //
    // It is important to keep code invoked via an AccessController to small
    // auditable blocks. Such code must carefully evaluate all user input
    // (parameters, system properties, config file contents, etc). As an
    // example, a Log implementation should not write to its logfile
    // with an AccessController anywhere in the call stack, otherwise an
    // insecure application could configure the log implementation to write
    // to a protected file using the privileges granted to JCL rather than
    // to the calling application.
    //
    // Under no circumstance should a non-private method return data that is
    // retrieved via an AccessController. That would allow an insecure app
    // to invoke that method and obtain data that it is not permitted to have.
    //
    // Invoking user-supplied code with an AccessController set is not a major
    // issue (eg invoking the constructor of the class specified by
    // HASHTABLE_IMPLEMENTATION_PROPERTY). That class will be in a different
    // trust domain, and therefore must have permissions to do whatever it
    // is trying to do regardless of the permissions granted to JCL. There is
    // a slight issue in that untrusted code may point that environment var
    // to another trusted library, in which case the code runs if both that
    // lib and JCL have the necessary permissions even when the untrusted
    // caller does not. That's a pretty hard route to exploit though.

    // ----------------------------------------------------- Manifest Constants

    /**
     * The name (<code>priority</code>) of the key in the config file used to
     * specify the priority of that particular config file. The associated value
     * is a floating-point number; higher values take priority over lower values.
     */
    public static final String PRIORITY_KEY = "priority";

    /**
     * The name (<code>use_tccl</code>) of the key in the config file used
     * to specify whether org.apache.commons.logging classes should be loaded via the thread
     * context class loader (TCCL), or not. By default, the TCCL is used.
     */
    public static final String TCCL_KEY = "use_tccl";

    /**
     * The name (<code>org.apache.commons.org.apache.commons.logging.LogFactory</code>) of the property
     * used to identify the LogFactory implementation
     * class name. This can be used as a system property, or as an entry in a
     * configuration properties file.
     */
    public static final String FACTORY_PROPERTY = "org.apache.commons.org.apache.commons.logging.LogFactory";

    /**
     * The fully qualified class name of the fallback <code>LogFactory</code>
     * implementation class to use, if no other can be found.
     */
    public static final String FACTORY_DEFAULT = "org.apache.commons.org.apache.commons.logging.impl.LogFactoryImpl";

    /**
     * The name (<code>commons-org.apache.commons.logging.properties</code>) of the properties file to search for.
     */
    public static final String FACTORY_PROPERTIES = "commons-org.apache.commons.logging.properties";

    /**
     * JDK1.3+ <a href="http://java.sun.com/j2se/1.3/docs/guide/jar/jar.html#Service%20Provider">
     * 'Service Provider' specification</a>.
     */
    protected static final String SERVICE_ID =
            "META-INF/services/org.apache.commons.logging.LogFactory";

    /**
     * The name (<code>org.apache.commons.org.apache.commons.logging.diagnostics.dest</code>)
     * of the property used to enable internal commons-org.apache.commons.logging
     * diagnostic output, in order to get information on what org.apache.commons.logging
     * implementations are being discovered, what classloaders they
     * are loaded through, etc.
     * <p>
     * If a system property of this name is set then the value is
     * assumed to be the name of a file. The special strings
     * STDOUT or STDERR (case-sensitive) indicate output to
     * System.out and System.err respectively.
     * <p>
     * Diagnostic org.apache.commons.logging should be used only to debug problematic
     * configurations and should not be set in normal production use.
     */
    public static final String DIAGNOSTICS_DEST_PROPERTY =
            "org.apache.commons.org.apache.commons.logging.diagnostics.dest";

    /**
     * Setting this system property
     * (<code>org.apache.commons.org.apache.commons.logging.LogFactory.HashtableImpl</code>)
     * value allows the <code>Hashtable</code> used to store
     * classloaders to be substituted by an alternative implementation.
     * <p>
     * <strong>Note:</strong> <code>LogFactory</code> will print:
     * <pre>
     * [ERROR] LogFactory: Load of custom hashtable failed
     * </pre>
     * to system error and then continue using a standard Hashtable.
     * <p>
     * <strong>Usage:</strong> Set this property when Java is invoked
     * and <code>LogFactory</code> will attempt to load a new instance
     * of the given implementation class.
     * For example, running the following ant scriplet:
     * <pre>
     *  &lt;java classname="${test.runner}" fork="yes" failonerror="${test.failonerror}"&gt;
     *     ...
     *     &lt;sysproperty
     *        key="org.apache.commons.org.apache.commons.logging.LogFactory.HashtableImpl"
     *        value="org.apache.commons.org.apache.commons.logging.AltHashtable"/&gt;
     *  &lt;/java&gt;
     * </pre>
     * will mean that <code>LogFactory</code> will load an instance of
     * <code>org.apache.commons.org.apache.commons.logging.AltHashtable</code>.
     * <p>
     * A typical use case is to allow a custom
     * Hashtable implementation using weak references to be substituted.
     * This will allow classloaders to be garbage collected without
     * the need to release them (on 1.3+ JVMs only, of course ;).
     * <p>
     * This property is not used and only here to provide compatibility.
     * </p>
     */
    public static final String HASHTABLE_IMPLEMENTATION_PROPERTY =
            "org.apache.commons.org.apache.commons.logging.LogFactory.HashtableImpl";

    // ----------------------------------------------------------- Constructors

    /**
     * Protected constructor that is not available for public use.
     */
    protected LogFactory() {
    }

    // --------------------------------------------------------- Public Methods

    /**
     * Return the configuration attribute with the specified name (if any),
     * or <code>null</code> if there is no such attribute.
     *
     * @param name Name of the attribute to return
     */
    public abstract Object getAttribute(String name);

    /**
     * Return an array containing the names of all currently defined
     * configuration attributes.  If there are no such attributes, a zero
     * length array is returned.
     */
    public abstract String[] getAttributeNames();

    /**
     * Convenience method to derive a name from the specified class and
     * call <code>getInstance(String)</code> with it.
     *
     * @param clazz Class for which a suitable Log name will be derived
     *
     * @throws LogConfigurationException if a suitable <code>Log</code>
     *                                   instance cannot be returned
     */
    public abstract Log getInstance(Class clazz)
            throws LogConfigurationException;

    /**
     * Construct (if necessary) and return a <code>Log</code> instance,
     * using the factory's current set of configuration attributes.
     * <p>
     * <strong>NOTE</strong> - Depending upon the implementation of
     * the <code>LogFactory</code> you are using, the <code>Log</code>
     * instance you are returned may or may not be local to the current
     * application, and may or may not be returned again on a subsequent
     * call with the same name argument.
     *
     * @param name Logical name of the <code>Log</code> instance to be
     *             returned (the meaning of this name is only known to the underlying
     *             org.apache.commons.logging implementation that is being wrapped)
     *
     * @throws LogConfigurationException if a suitable <code>Log</code>
     *                                   instance cannot be returned
     */
    public abstract Log getInstance(String name)
            throws LogConfigurationException;

    /**
     * Release any internal references to previously created {@link Log}
     * instances returned by this factory.  This is useful in environments
     * like servlet containers, which implement application reloading by
     * throwing away a ClassLoader.  Dangling references to objects in that
     * class loader would prevent garbage collection.
     */
    public abstract void release();

    /**
     * Remove any configuration attribute associated with the specified name.
     * If there is no such attribute, no action is taken.
     *
     * @param name Name of the attribute to remove
     */
    public abstract void removeAttribute(String name);

    /**
     * Set the configuration attribute with the specified name.  Calling
     * this with a <code>null</code> value is equivalent to calling
     * <code>removeAttribute(name)</code>.
     *
     * @param name  Name of the attribute to set
     * @param value Value of the attribute to set, or <code>null</code>
     *              to remove any setting for this attribute
     */
    public abstract void setAttribute(String name, Object value);

    // ------------------------------------------------------- Static Variables

    /**
     * The previously constructed <code>LogFactory</code> instances, keyed by
     * the <code>ClassLoader</code> with which it was created.
     *
     * @deprecated this is not used as other factory types are not supported
     */
    @Deprecated
    protected static Hashtable factories = null;

    /**
     * Previously constructed <code>LogFactory</code> instance as in the
     * <code>factories</code> map, but for the case where
     * <code>getClassLoader</code> returns <code>null</code>.
     * This can happen when:
     * <ul>
     * <li>using JDK1.1 and the calling code is loaded via the system
     * classloader (very common)</li>
     * <li>using JDK1.2+ and the calling code is loaded via the boot
     * classloader (only likely for embedded systems work).</li>
     * </ul>
     * Note that <code>factories</code> is a <i>Hashtable</i> (not a HashMap),
     * and hashtables don't allow null as a key.
     *
     * @deprecated since 1.1.2 - this is not used as other factory types are not supported
     */
    @Deprecated
    protected static volatile LogFactory nullClassLoaderFactory = null;

    // --------------------------------------------------------- Static Methods

    /**
     * Construct (if necessary) and return a <code>LogFactory</code>
     * instance, using the following ordered lookup procedure to determine
     * the name of the implementation class to be loaded.
     * <p>
     * <ul>
     * <li>The <code>org.apache.commons.org.apache.commons.logging.LogFactory</code> system
     * property.</li>
     * <li>The JDK 1.3 Service Discovery mechanism</li>
     * <li>Use the properties file <code>commons-org.apache.commons.logging.properties</code>
     * file, if found in the class path of this class.  The configuration
     * file is in standard <code>java.util.Properties</code> format and
     * contains the fully qualified name of the implementation class
     * with the key being the system property defined above.</li>
     * <li>Fall back to a default implementation class
     * (<code>org.apache.commons.org.apache.commons.logging.impl.LogFactoryImpl</code>).</li>
     * </ul>
     * <p>
     * <em>NOTE</em> - If the properties file method of identifying the
     * <code>LogFactory</code> implementation class is utilized, all of the
     * properties defined in this file will be set as configuration attributes
     * on the corresponding <code>LogFactory</code> instance.
     * <p>
     * <em>NOTE</em> - In a multi-threaded environment it is possible
     * that two different instances will be returned for the same
     * classloader environment.
     *
     * @throws LogConfigurationException if the implementation class is not
     *                                   available or cannot be instantiated.
     */
    public static LogFactory getFactory() throws LogConfigurationException {
        return Holder.LOG_FACTORY;
    }

    /**
     * Convenience method to return a named logger, without the application
     * having to care about factories.
     *
     * @param clazz Class from which a log name will be derived
     *
     * @throws LogConfigurationException if a suitable <code>Log</code>
     *                                   instance cannot be returned
     */
    public static Log getLog(Class clazz) throws LogConfigurationException {
        return getFactory().getInstance(clazz);
    }

    /**
     * Convenience method to return a named logger, without the application
     * having to care about factories.
     *
     * @param name Logical name of the <code>Log</code> instance to be
     *             returned (the meaning of this name is only known to the underlying
     *             org.apache.commons.logging implementation that is being wrapped)
     *
     * @throws LogConfigurationException if a suitable <code>Log</code>
     *                                   instance cannot be returned
     */
    public static Log getLog(String name) throws LogConfigurationException {
        return getFactory().getInstance(name);
    }

    /**
     * Release any internal references to previously created {@link LogFactory}
     * instances that have been associated with the specified class loader
     * (if any), after calling the instance method <code>release()</code> on
     * each of them.
     *
     * @param classLoader ClassLoader for which to release the LogFactory
     */
    public static void release(ClassLoader classLoader) {
        getFactory().release();
    }

    /**
     * Release any internal references to previously created {@link LogFactory}
     * instances, after calling the instance method <code>release()</code> on
     * each of them.  This is useful in environments like servlet containers,
     * which implement application reloading by throwing away a ClassLoader.
     * Dangling references to objects in that class loader would prevent
     * garbage collection.
     */
    public static void releaseAll() {
        getFactory().release();
    }

    // ------------------------------------------------------ Protected Methods

    /**
     * Safely get access to the classloader for the specified class.
     * <p>
     * Theoretically, calling getClassLoader can throw a security exception,
     * and so should be done under an AccessController in order to provide
     * maximum flexibility. However in practice people don't appear to use
     * security policies that forbid getClassLoader calls. So for the moment
     * all code is written to call this method rather than Class.getClassLoader,
     * so that we could put AccessController stuff in this method without any
     * disruption later if we need to.
     * <p>
     * Even when using an AccessController, however, this method can still
     * throw SecurityException. Commons-org.apache.commons.logging basically relies on the
     * ability to access classloaders, ie a policy that forbids all
     * classloader access will also prevent commons-org.apache.commons.logging from working:
     * currently this method will throw an exception preventing the entire app
     * from starting up. Maybe it would be good to detect this situation and
     * just disable all commons-org.apache.commons.logging? Not high priority though - as stated
     * above, security policies that prevent classloader access aren't common.
     * <p>
     * Note that returning an object fetched via an AccessController would
     * technically be a security flaw anyway; untrusted code that has access
     * to a trusted JCL library could use it to fetch the classloader for
     * a class even when forbidden to do so directly.
     *
     * @since 1.1
     */
    protected static ClassLoader getClassLoader(Class clazz) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the current context classloader.
     * <p>
     * In versions prior to 1.1, this method did not use an AccessController.
     * In version 1.1, an AccessController wrapper was incorrectly added to
     * this method, causing a minor security flaw.
     * <p>
     * In version 1.1.1 this change was reverted; this method no longer uses
     * an AccessController. User code wishing to obtain the context classloader
     * must invoke this method via AccessController.doPrivileged if it needs
     * support for that.
     *
     * @return the context classloader associated with the current thread,
     * or null if security doesn't allow it.
     *
     * @throws LogConfigurationException if there was some weird error while
     *                                   attempting to get the context classloader.
     */
    protected static ClassLoader getContextClassLoader() throws LogConfigurationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Return the thread context class loader if available; otherwise return null.
     * <p>
     * Most/all code should call getContextClassLoaderInternal rather than
     * calling this method directly.
     * <p>
     * The thread context class loader is available for JDK 1.2
     * or later, if certain security conditions are met.
     * <p>
     * Note that no internal org.apache.commons.logging is done within this method because
     * this method is called every time LogFactory.getLogger() is called,
     * and we don't want too much output generated here.
     *
     * @return the thread's context classloader or {@code null} if the java security
     * policy forbids access to the context classloader from one of the classes
     * in the current call stack.
     *
     * @throws LogConfigurationException if a suitable class loader
     *                                   cannot be identified.
     * @since 1.1
     */
    protected static ClassLoader directGetContextClassLoader() throws LogConfigurationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Return a new instance of the specified <code>LogFactory</code>
     * implementation class, loaded by the specified class loader.
     * If that fails, try the class loader used to load this
     * (abstract) LogFactory.
     * <h2>ClassLoader conflicts</h2>
     * <p>
     * Note that there can be problems if the specified ClassLoader is not the
     * same as the classloader that loaded this class, ie when loading a
     * concrete LogFactory subclass via a context classloader.
     * <p>
     * The problem is the same one that can occur when loading a concrete Log
     * subclass via a context classloader.
     * <p>
     * The problem occurs when code running in the context classloader calls
     * class X which was loaded via a parent classloader, and class X then calls
     * LogFactory.getFactory (either directly or via LogFactory.getLog). Because
     * class X was loaded via the parent, it binds to LogFactory loaded via
     * the parent. When the code in this method finds some LogFactoryYYYY
     * class in the child (context) classloader, and there also happens to be a
     * LogFactory class defined in the child classloader, then LogFactoryYYYY
     * will be bound to LogFactory@childloader. It cannot be cast to
     * LogFactory@parentloader, ie this method cannot return the object as
     * the desired type. Note that it doesn't matter if the LogFactory class
     * in the child classloader is identical to the LogFactory class in the
     * parent classloader, they are not compatible.
     * <p>
     * The solution taken here is to simply print out an error message when
     * this occurs then throw an exception. The deployer of the application
     * must ensure they remove all occurrences of the LogFactory class from
     * the child classloader in order to resolve the issue. Note that they
     * do not have to move the custom LogFactory subclass; that is ok as
     * long as the only LogFactory class it can find to bind to is in the
     * parent classloader.
     *
     * @param factoryClass       Fully qualified name of the <code>LogFactory</code>
     *                           implementation class
     * @param classLoader        ClassLoader from which to load this class
     * @param contextClassLoader is the context that this new factory will
     *                           manage org.apache.commons.logging for.
     *
     * @throws LogConfigurationException if a suitable instance
     *                                   cannot be created
     * @since 1.1
     */
    protected static LogFactory newFactory(final String factoryClass,
                                           final ClassLoader classLoader,
                                           final ClassLoader contextClassLoader)
            throws LogConfigurationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Method provided for backwards compatibility; see newFactory version that
     * takes 3 parameters.
     * <p>
     * This method would only ever be called in some rather odd situation.
     * Note that this method is static, so overriding in a subclass doesn't
     * have any effect unless this method is called from a method in that
     * subclass. However this method only makes sense to use from the
     * getFactory method, and as that is almost always invoked via
     * LogFactory.getFactory, any custom definition in a subclass would be
     * pointless. Only a class with a custom getFactory method, then invoked
     * directly via CustomFactoryImpl.getFactory or similar would ever call
     * this. Anyway, it's here just in case, though the "managed class loader"
     * value output to the diagnostics will not report the correct value.
     */
    protected static LogFactory newFactory(final String factoryClass,
                                           final ClassLoader classLoader) {
        throw new UnsupportedOperationException();
    }

    /**
     * Indicates true if the user has enabled internal org.apache.commons.logging.
     * <p>
     * By the way, sorry for the incorrect grammar, but calling this method
     * areDiagnosticsEnabled just isn't java beans style.
     *
     * @return true if calls to logDiagnostic will have any effect.
     *
     * @since 1.1
     */
    protected static boolean isDiagnosticsEnabled() {
        throw new UnsupportedOperationException();
    }

    /**
     * Write the specified message to the internal org.apache.commons.logging destination.
     *
     * @param msg is the diagnostic message to be output.
     *
     * @since 1.1
     */
    protected static final void logRawDiagnostic(String msg) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a string that uniquely identifies the specified object, including
     * its class.
     * <p>
     * The returned string is of form "classname@hashcode", ie is the same as
     * the return value of the Object.toString() method, but works even when
     * the specified object's class has overidden the toString method.
     *
     * @param o may be null.
     *
     * @return a string of form classname@hashcode, or "null" if param o is null.
     *
     * @since 1.1
     */
    public static String objectId(Object o) {
        if (o == null) {
            return "null";
        } else {
            return o.getClass().getName() + "@" + System.identityHashCode(o);
        }
    }
}
