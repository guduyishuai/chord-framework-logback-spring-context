package com.chord.framework.logback;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.jul.LevelChangePropagator;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.status.Status;
import org.slf4j.ILoggerFactory;
import org.slf4j.impl.StaticLoggerBinder;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ResourceUtils;

import java.io.FileNotFoundException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.List;

/**
 *
 * 针对于非spring-boot项目，对logback进行自定义
 * @see SpringJoranConfigurator
 *
 * Created on 2020/11/12
 *
 * @author: wulinfeng
 */
public class LogbackInitialization implements BeanFactoryPostProcessor, EnvironmentAware {

    private static final String LOGBACK_XML_NAME = "logback-spring.xml";

    private static final String BRIDGE_HANDLER = "org.slf4j.bridge.SLF4JBridgeHandler";

    private Environment environment;

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

        String location = getLogbackPackagedConfigFile();
        LoggerContext loggerContext;
        try {
            loggerContext = getLoggerContext();
            stopAndReset(loggerContext);
            configureByResourceUrl(environment, loggerContext,
                    ResourceUtils.getURL(location));
        } catch (FileNotFoundException | JoranException e) {
            throw new IllegalStateException(
                    "Could not initialize logback logging from " + location, e);
        }

        List<Status> statuses = loggerContext.getStatusManager().getCopyOfStatusList();
        StringBuilder errors = new StringBuilder();
        for (Status status : statuses) {
            if (status.getLevel() == Status.ERROR) {
                errors.append(errors.length() > 0 ? String.format("%n") : "");
                errors.append(status.toString());
            }
        }
        if (errors.length() > 0) {
            throw new IllegalStateException(
                    String.format("Logback configuration error detected: %n%s", errors));
        }

    }

    protected final String getLogbackPackagedConfigFile() {
        return "classpath:" + LOGBACK_XML_NAME;
    }

    private LoggerContext getLoggerContext() {
        ILoggerFactory factory = StaticLoggerBinder.getSingleton().getLoggerFactory();
        Assert.isInstanceOf(LoggerContext.class, factory,
                String.format(
                        "LoggerFactory is not a Logback LoggerContext but Logback is on "
                                + "the classpath. Either remove Logback or the competing "
                                + "implementation (%s loaded from %s). If you are using "
                                + "WebLogic you will need to add 'org.slf4j' to "
                                + "prefer-application-packages in WEB-INF/weblogic.xml",
                        factory.getClass(), getLocation(factory)));
        return (LoggerContext) factory;
    }

    private Object getLocation(ILoggerFactory factory) {
        try {
            ProtectionDomain protectionDomain = factory.getClass().getProtectionDomain();
            CodeSource codeSource = protectionDomain.getCodeSource();
            if (codeSource != null) {
                return codeSource.getLocation();
            }
        }
        catch (SecurityException ex) {
            // Unable to determine location
        }
        return "unknown location";
    }

    private void stopAndReset(LoggerContext loggerContext) {
        loggerContext.stop();
        loggerContext.reset();
        if (isBridgeHandlerAvailable()) {
            addLevelChangePropagator(loggerContext);
        }
    }

    protected final boolean isBridgeHandlerAvailable() {
        return ClassUtils.isPresent(BRIDGE_HANDLER, this.getClass().getClassLoader());
    }

    private void addLevelChangePropagator(LoggerContext loggerContext) {
        LevelChangePropagator levelChangePropagator = new LevelChangePropagator();
        levelChangePropagator.setResetJUL(true);
        levelChangePropagator.setContext(loggerContext);
        loggerContext.addListener(levelChangePropagator);
    }

    private void configureByResourceUrl(
            Environment environment,
            LoggerContext loggerContext, URL url) throws JoranException {
        if (url.toString().endsWith("xml")) {
            JoranConfigurator configurator = new SpringJoranConfigurator(
                    environment);
            configurator.setContext(loggerContext);
            configurator.doConfigure(url);
        }
        else {
            new ContextInitializer(loggerContext).configureByResource(url);
        }
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
