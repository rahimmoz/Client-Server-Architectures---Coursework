package smartcampus.resource;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import smartcampus.exception.LinkedResourceNotFoundException;
import smartcampus.model.Sensor;
import smartcampus.store.DataStore;

import java.util.ArrayList;
import java.util.List;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    // GET /api/v1/sensors or /api/v1/sensors?type=CO2
    @GET
    public Response getSensors(@QueryParam("type") String type) {
        List<Sensor> result = new ArrayList<>(DataStore.sensors.values());

        if (type != null && !type.isBlank()) {
            result.removeIf(s -> !s.getType().equalsIgnoreCase(type));
        }

        return Response.ok(result).build();
    }

    // POST /api/v1/sensors — register a new sensor
    @POST
    public Response createSensor(Sensor sensor) {
        if (sensor.getId() == null || sensor.getId().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Sensor ID is required\"}")
                    .build();
        }
        // Validate that the roomId actually exists
        if (sensor.getRoomId() == null || !DataStore.rooms.containsKey(sensor.getRoomId())) {
            throw new LinkedResourceNotFoundException(sensor.getRoomId());
        }

        DataStore.sensors.put(sensor.getId(), sensor);

        // Add this sensor's ID to its room's sensorIds list
        DataStore.rooms.get(sensor.getRoomId()).getSensorIds().add(sensor.getId());

        // Initialise an empty readings list for this sensor
        DataStore.readings.put(sensor.getId(), new ArrayList<>());

        return Response.status(Response.Status.CREATED).entity(sensor).build();
    }

    // Sub-resource locator — hands off to SensorReadingResource
    // for any path like /sensors/{sensorId}/readings
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }
}