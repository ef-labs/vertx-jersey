package com.englishtown.vertx.jersey;

import com.englishtown.vertx.jersey.security.DefaultSecurityContextProvider;
import com.englishtown.vertx.jersey.security.SecurityContextProvider;
import org.glassfish.jersey.server.ApplicationHandler;
import org.junit.Test;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpServerResponse;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Container;

import javax.ws.rs.HttpMethod;
import java.net.URI;

import static org.mockito.Mockito.*;

/**
 * Created with IntelliJ IDEA.
 * User: adriangonzalez
 * Date: 4/18/13
 * Time: 5:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class JerseyHandlerTest {
    @Test
    public void testHandle() throws Exception {

        JerseyHandler handler = createInstance();
        HttpServerRequest request = mock(HttpServerRequest.class);
        HttpServerResponse response = mock(HttpServerResponse.class);
        when(request.absoluteURI()).thenReturn(URI.create("http://test.englishtown.com/test"));
        when(request.method()).thenReturn(HttpMethod.GET);
        when(request.response()).thenReturn(response);

        handler.handle(request);

    }

    private JerseyHandler createInstance() {

        Vertx vertx = mock(Vertx.class);
        Container container = mock(Container.class);
        URI baseUri = URI.create("/");
        ApplicationHandler application = new ApplicationHandler();
        SecurityContextProvider securityContextProvider = new DefaultSecurityContextProvider();

        JsonObject config = new JsonObject();
        Logger logger = mock(Logger.class);
        when(container.config()).thenReturn(config);
        when(container.logger()).thenReturn(logger);

        return new JerseyHandler(vertx, container, baseUri, application, securityContextProvider, null);
    }

//    @Test
//    public void testHandle() throws Exception {
//
//    }
//
//    @Test
//    public void testHandle() throws Exception {
//
//    }
//
//    @Test
//    public void testCallVertxHandler() throws Exception {
//
//    }
//
//    @Test
//    public void testHandle() throws Exception {
//
//    }

    @Test
    public void testShouldReadData() throws Exception {

    }
}
