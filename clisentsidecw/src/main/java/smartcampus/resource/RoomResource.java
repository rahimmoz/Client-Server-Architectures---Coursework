package smartcampus.resource;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import smartcampus.exception.RoomNotEmptyException;
import smartcampus.model.Room;
import smartcampus.store.DataStore;

import java.util.ArrayList;
import java.util.List;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    // GET /api/v1/rooms — return all rooms
    @GET
    public Response getAllRooms() {
        List<Room> allRooms = new ArrayList<>(DataStore.rooms.values());
        return Response.ok(allRooms).build();
    }

    // POST /api/v1/rooms — create a new room
    @POST
    public Response createRoom(Room room) {
        if (room.getId() == null || room.getId().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Room ID is required\"}")
                    .build();
        }
        if (DataStore.rooms.containsKey(room.getId())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"error\":\"Room with this ID already exists\"}")
                    .build();
        }
        DataStore.rooms.put(room.getId(), room);
        return Response.status(Response.Status.CREATED).entity(room).build();
    }

    // GET /api/v1/rooms/{roomId} — get one room by ID
    @GET
    @Path("/{roomId}")
    public Response getRoom(@PathParam("roomId") String roomId) {
        Room room = DataStore.rooms.get(roomId);
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Room not found: " + roomId + "\"}")
                    .build();
        }
        return Response.ok(room).build();
    }

    // DELETE /api/v1/rooms/{roomId} — delete a room (only if it has no sensors)
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = DataStore.rooms.get(roomId);
        if (room == null) {
            // Idempotent: already gone, return 204 No Content
            return Response.noContent().build();
        }
        if (!room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(roomId);
        }
        DataStore.rooms.remove(roomId);
        return Response.noContent().build();
    }
}