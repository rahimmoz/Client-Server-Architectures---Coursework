// exception/LinkedResourceNotFoundException.java
package smartcampus.exception;

public class LinkedResourceNotFoundException extends RuntimeException {
    public LinkedResourceNotFoundException(String roomId) {
        super("Referenced room does not exist: " + roomId);
    }
}