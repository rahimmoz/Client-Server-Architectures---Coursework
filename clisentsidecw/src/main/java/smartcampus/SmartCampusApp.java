package smartcampus;

import org.glassfish.jersey.server.ResourceConfig;
import jakarta.ws.rs.ApplicationPath;

@ApplicationPath("/api/v1")
public class SmartCampusApp extends ResourceConfig {

    public SmartCampusApp() {
        packages("smartcampus");
    }
}