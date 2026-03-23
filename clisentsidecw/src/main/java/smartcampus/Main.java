package smartcampus;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import java.net.URI;

public class Main {
    public static final String BASE_URI = "http://localhost:8080/";

    public static void main(String[] args) {
        // Look for resources in the 'resource' package
        final ResourceConfig rc = new ResourceConfig().packages("smartcampus.resource");
        
        // Start server and prepend /api/v1 to everything [cite: 104]
        final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
        
        System.out.println("Smart Campus API started at " + BASE_URI + "api/v1");
        System.out.println("Press Enter to stop...");
        try {
            System.in.read();
            server.shutdownNow();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}