// exception/SensorUnavailableException.java
package smartcampus.exception;

public class SensorUnavailableException extends RuntimeException {
    public SensorUnavailableException(String sensorId) {
        super("Sensor " + sensorId + " is under MAINTENANCE and cannot accept readings.");
    }
}