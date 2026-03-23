package smartcampus.model;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import java.net.URI;
import smartcampus.SmartCampusApp;

public class Room {

    public static final String BASE_URI = "http://localhost:8080/";

    public static void main(String[] args) throws Exception {
        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(
            URI.create(BASE_URI),
            new SmartCampusApp()
        );

        System.out.println("Smart Campus API running at " + BASE_URI + "api/v1");
        System.out.println("Press ENTER to stop the server...");
        System.in.read();
        server.stop();
    }
}