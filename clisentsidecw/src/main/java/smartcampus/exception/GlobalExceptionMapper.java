// exception/GlobalExceptionMapper.java
package smartcampus.exception;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.Map;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {
    @Override
    public Response toResponse(Throwable e) {
        // Log it server-side but never expose the stack trace to the client
        System.err.println("Unexpected error: " + e.getMessage());
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(Map.of("error", "500 Internal Server Error",
                               "message", "An unexpected error occurred."))
                .build();
    }
}