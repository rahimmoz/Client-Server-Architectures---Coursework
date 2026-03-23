package smartcampus.resource;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import smartcampus.exception.SensorUnavailableException;
import smartcampus.model.Sensor;
import smartcampus.model.SensorReading;
import smartcampus.store.DataStore;

import java.util.List;
import java.util.UUID;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    // GET /api/v1/sensors/{sensorId}/readings
    @GET
    public Response getReadings() {
        if (!DataStore.sensors.containsKey(sensorId)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Sensor not found: " + sensorId + "\"}")
                    .build();
        }
        List<SensorReading> sensorReadings = DataStore.readings.get(sensorId);
        return Response.ok(sensorReadings).build();
    }

    // POST /api/v1/sensors/{sensorId}/readings
    @POST
    public Response addReading(SensorReading reading) {
        Sensor sensor = DataStore.sensors.get(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Sensor not found: " + sensorId + "\"}")
                    .build();
        }
        // Block readings for sensors in MAINTENANCE status
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(sensorId);
        }

        // Auto-generate ID and timestamp if not provided
        if (reading.getId() == null) {
            reading.setId(UUID.randomUUID().toString());
        }
        if (reading.getTimestamp() == 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        // Save the reading
        DataStore.readings.get(sensorId).add(reading);

        // Update the sensor's currentValue to match this latest reading
        sensor.setCurrentValue(reading.getValue());

        return Response.status(Response.Status.CREATED).entity(reading).build();
    }
}