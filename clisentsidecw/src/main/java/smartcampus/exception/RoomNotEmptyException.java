// exception/RoomNotEmptyException.java
package smartcampus.exception;

public class RoomNotEmptyException extends RuntimeException {
    public RoomNotEmptyException(String roomId) {
        super("Room " + roomId + " still has sensors assigned to it.");
    }
}