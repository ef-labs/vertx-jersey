package com.englishtown.vertx.jersey.integration;

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.PlatformLocator;
import org.vertx.java.platform.PlatformManager;
import org.vertx.testtools.JavaClassRunner;
import org.vertx.testtools.TestVerticleInfo;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created with IntelliJ IDEA.
 * User: adriangonzalez
 * Date: 7/18/13
 * Time: 5:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class CPJavaClassRunner extends JavaClassRunner {

    private final PlatformManager mgr;

    public CPJavaClassRunner(Class<?> klass) throws InitializationError {
        super(klass);
        mgr = PlatformLocator.factory.createPlatformManager();
    }

    @Override
    protected void runChild(FrameworkMethod method, RunNotifier notifier) {
        Class<?> testClass = getTestClass().getJavaClass();
        String methodName = method.getName();
        String testDesc = method.getName();
        Description desc = Description.createTestDescription(testClass, testDesc);
        notifier.fireTestStarted(desc);
        final AtomicReference<Throwable> failure = new AtomicReference<>();
        try {
            JsonObject conf = new JsonObject().putString("methodName", getActualMethodName(methodName));
            final CountDownLatch testLatch = new CountDownLatch(1);
            Handler<Message<JsonObject>> handler = new Handler<Message<JsonObject>>() {
                @Override
                public void handle(Message<JsonObject> msg) {
                    JsonObject jmsg = msg.body();
                    String type = jmsg.getString("type");
                    try {
                        switch (type) {
                            case "done":
                                break;
                            case "failure":
                                byte[] bytes = jmsg.getBinary("failure");
                                // Deserialize
                                ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
                                Throwable t = (Throwable) ois.readObject();
                                // We display this since otherwise Gradle doesn't display it to stdout/stderr
                                t.printStackTrace();
                                failure.set(t);
                                break;
                        }
                    } catch (ClassNotFoundException | IOException e) {
                        e.printStackTrace();
                        failure.set(e);
                    } finally {
                        testLatch.countDown();
                    }
                }
            };

            EventBus eb = mgr.vertx().eventBus();
            eb.registerHandler(TESTRUNNER_HANDLER_ADDRESS, handler);
            final CountDownLatch deployLatch = new CountDownLatch(1);
            final AtomicReference<String> deploymentIDRef = new AtomicReference<>();
            String includes;
            TestVerticleInfo annotation = getAnnotation();
            if (annotation != null) {
                includes = getAnnotation().includes().trim();
                if (includes.isEmpty()) {
                    includes = null;
                }
            } else {
                includes = null;
            }
            System.out.println("Starting test: " + testDesc);
            String main = getMain(methodName);
            URL[] urls = getClassPaths(methodName);
            final AtomicReference<Throwable> deployThrowable = new AtomicReference<>();
            mgr.deployVerticle(main, conf, urls, 1, includes, new AsyncResultHandler<String>() {
                public void handle(AsyncResult<String> ar) {
                    if (ar.succeeded()) {
                        deploymentIDRef.set(ar.result());
                    } else {
                        deployThrowable.set(ar.cause());
                    }
                    deployLatch.countDown();
                }
            });
            waitForLatch(deployLatch);
            if (deployThrowable.get() != null) {
                notifier.fireTestFailure(new Failure(desc, deployThrowable.get()));
                notifier.fireTestFinished(desc);
                return;
            }
            waitForLatch(testLatch);
            eb.unregisterHandler(TESTRUNNER_HANDLER_ADDRESS, handler);
            final CountDownLatch undeployLatch = new CountDownLatch(1);
            final AtomicReference<Throwable> undeployThrowable = new AtomicReference<>();
            mgr.undeploy(deploymentIDRef.get(), new AsyncResultHandler<Void>() {
                public void handle(AsyncResult<Void> ar) {
                    if (ar.failed()) {
                        undeployThrowable.set(ar.cause());
                    }
                    undeployLatch.countDown();
                }
            });
            waitForLatch(undeployLatch);
            if (undeployThrowable.get() != null) {
                notifier.fireTestFailure(new Failure(desc, undeployThrowable.get()));
                notifier.fireTestFinished(desc);
                return;
            }
            if (failure.get() != null) {
                notifier.fireTestFailure(new Failure(desc, failure.get()));
            }
            notifier.fireTestFinished(desc);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected URL[] getClassPaths(String methodName) {
        List<URL> urls = new ArrayList<>();

        URL cp = getClassPath(methodName);
        if (cp != null) {
            urls.add(cp);
        }

        String classPaths = System.getProperty("java.class.path");
        String pathSeparator = System.getProperty("path.separator");
        String fileSeparator = System.getProperty("file.separator");

        // Include everything on the classpath except for java jars and vertx-core/vertx-platform
        String[] cps = classPaths.split(pathSeparator);
        String javaHome = System.getProperty("java.home");
        String vertxCore = fileSeparator + "vertx-core" + fileSeparator;
        String vertxPlatform = fileSeparator + "vertx-platform" + fileSeparator;

        for (String s : cps) {
            if (!s.startsWith(javaHome) && !s.contains(vertxCore) && !s.contains(vertxPlatform)) {
                File f = new File(s);
                if (f.exists()) {
                    try {
                        urls.add(f.toURI().toURL());
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return urls.toArray(new URL[urls.size()]);
    }

    private void waitForLatch(CountDownLatch latch) {
        while (true) {
            try {
                if (!latch.await(TIMEOUT, TimeUnit.SECONDS)) {
                    throw new AssertionError("Timed out waiting for test to complete");
                }
                break;
            } catch (InterruptedException e) {
                // Ignore
            }
        }
    }

}
