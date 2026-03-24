# Client-Server-Architectures---Coursework
REST API design, development and implementation.

# Smart Campus Sensor & Room Management API
A RESTful API built with JAX-RS (Jersey) and Java for managing university campus rooms and IoT sensors. Built as part of the 5COSC022W Client-Server Architectures module at the University of Westminster.

## API Overview
This API provides a comprehensive interface for campus facilities managers and automated building systems to interact with campus data. It manages three core resources:
- **Rooms** — physical spaces on campus with capacity and sensor assignments
- **Sensors** — IoT devices deployed in rooms (temperature, CO2, occupancy, etc.)
- **Sensor Readings** — historical measurement logs recorded by each sensor
All data is stored in-memory using ConcurrentHashMap and ArrayList data structures. No database is used.

## Technology Stack
- Java 17
- JAX-RS via Jersey 3.1.3
- Grizzly HTTP Server (embedded)
- Jackson (JSON serialisation)
- Maven (build tool)

## Project Structure

src/main/java/smartcampus/
├── Main.java                          — starts the Grizzly server
├── SmartCampusApp.java                — JAX-RS application configuration
├── model/
│   ├── Room.java
│   ├── Sensor.java
│   └── SensorReading.java
├── store/
│   └── DataStore.java                 — in-memory ConcurrentHashMap store
├── resource/
│   ├── DiscoveryResource.java         — GET /api/v1
│   ├── RoomResource.java              — /api/v1/rooms
│   ├── SensorResource.java            — /api/v1/sensors
│   └── SensorReadingResource.java     — /api/v1/sensors/{id}/readings
└── exception/
    ├── RoomNotEmptyException.java
    ├── LinkedResourceNotFoundException.java
    ├── SensorUnavailableException.java
    ├── ExceptionMappers.java          — all four exception mappers
    └── LoggingFilter.java

## How to Build and Run

### Prerequisites
- Java 17 or higher
- Maven 3.8 or higher
- NetBeans IDE (recommended) or any Java IDE

### Steps
1. Clone the repository:
```bash
git clone https://github.com/rahimmoz/Client-Server-Architectures---Coursework.git
cd smart-campus-api
```

2. Build the project:
```bash
mvn clean compile
```

3. Run the server:
```bash
mvn exec:java
```

4. The API will be available at:
```
http://localhost:8080/api/v1
```
To stop the server, press **ENTER** in the terminal.

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/v1 | Discovery — API metadata and resource links |
| GET | /api/v1/rooms | List all rooms |
| POST | /api/v1/rooms | Create a new room |
| GET | /api/v1/rooms/{roomId} | Get a specific room |
| DELETE | /api/v1/rooms/{roomId} | Delete a room (blocked if sensors exist) |
| GET | /api/v1/sensors | List all sensors (optional ?type= filter) |
| POST | /api/v1/sensors | Register a new sensor |
| GET | /api/v1/sensors/{sensorId}/readings | Get all readings for a sensor |
| POST | /api/v1/sensors/{sensorId}/readings | Add a new reading |

## Sample curl Commands

### 1. Get API discovery info
```bash
curl -X GET http://localhost:8080/api/v1
```

### 2. Create a room
```bash
curl -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id":"LIB-301","name":"Library Quiet Study","capacity":50}'
```

### 3. Create a sensor (links to room LIB-301)
```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"TEMP-001","type":"Temperature","status":"ACTIVE","currentValue":0.0,"roomId":"LIB-301"}'
```

### 4. Add a reading to a sensor
```bash
curl -X POST http://localhost:8080/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value":22.5}'
```

### 5. Filter sensors by type
```bash
curl -X GET "http://localhost:8080/api/v1/sensors?type=Temperature"
```

### 6. Get all readings for a sensor
```bash
curl -X GET http://localhost:8080/api/v1/sensors/TEMP-001/readings
```

### 7. Attempt to delete a room with sensors (triggers 409 Conflict)
```bash
curl -X DELETE http://localhost:8080/api/v1/rooms/LIB-301
```

### 8. Attempt to create a sensor with a non-existent room (triggers 422)
```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"CO2-999","type":"CO2","status":"ACTIVE","currentValue":0.0,"roomId":"FAKE-ROOM"}'
```

## Report — Question Answers

### Part 1.1 — JAX-RS Resource Lifecycle

By default, JAX-RS creates a new instance of every resource class for each incoming HTTP request. This is known as the per-request lifecycle. The runtime instantiates the resource class, processes the request, and then discards the instance — meaning no state is preserved between requests inside the resource object itself.
This architectural decision has a direct and critical impact on how in-memory data must be managed. If data were stored as instance fields inside a resource class, it would be wiped after every single request, making persistence impossible. To work around this, all data in this application is stored in static ConcurrentHashMap fields inside a dedicated DataStore class. Because static fields belong to the class itself rather than any instance, they persist for the entire lifetime of the JVM regardless of how many resource instances are created and destroyed.
Thread safety is also a concern. Since multiple requests can arrive simultaneously and each spawns its own thread, a regular HashMap would be vulnerable to race conditions — two threads writing at the same moment could corrupt the data structure. ConcurrentHashMap prevents this by using internal locking mechanisms, ensuring that concurrent reads and writes are handled safely without data loss or corruption.

### Part 1.2 — HATEOAS and Hypermedia

HATEOAS (Hypermedia as the Engine of Application State) is considered a hallmark of advanced RESTful design because it makes an API self-describing and self-navigable. Rather than requiring a client to already know all available URLs from external documentation, a HATEOAS-compliant API embeds navigation links directly inside its responses. For example, the discovery endpoint at GET /api/v1 returns links to /api/v1/rooms and /api/v1/sensors, allowing a client to discover and navigate the API dynamically.
This approach significantly benefits client developers in several ways. First, it reduces coupling — clients do not need to hardcode URL structures, so if the server changes an endpoint path, clients that follow links rather than hardcoded URLs continue to work without modification. Second, it reduces the need to read extensive external documentation, as the API itself communicates what actions are available and where. Third, it makes the API more intuitive to explore, particularly during development and testing, as each response guides the developer to the next logical step.

### Part 2.1 — Returning IDs vs Full Objects in Lists

When a GET request returns a list of rooms, there is a trade-off between returning only IDs versus returning full room objects.
Returning only IDs is very lightweight on network bandwidth — the response payload is minimal regardless of how many rooms exist. However, this forces the client to make a separate HTTP request for each ID to retrieve the actual room data, resulting in N+1 requests. For a client displaying a list of 100 rooms, this means 101 HTTP round trips, which is inefficient and slow.
Returning full room objects in a single response is heavier in terms of payload size, but it allows the client to render a complete list in a single request with no further API calls needed. This is far more efficient for typical use cases such as displaying a dashboard or table of all rooms. The trade-off only becomes significant at very large scale, where pagination should be introduced instead. For this application, returning full objects is the correct and practical choice.

### Part 2.2 — Idempotency of DELETE

Yes, the DELETE operation is idempotent in this implementation. Idempotency means that making the same request multiple times produces the same result as making it once.
In this API, if a client sends DELETE /api/v1/rooms/LIB-301 and the room exists and has no sensors, it is deleted and a 204 No Content response is returned. If the client then mistakenly sends the exact same DELETE request again, the room no longer exists in the data store. Rather than returning a 404 Not Found error, the implementation returns 204 No Content again — the same response as the first successful deletion. This is the correct idempotent behaviour because the end state is identical both times: the room does not exist. Clients can safely retry DELETE requests without worrying about receiving unexpected errors on the second attempt.

### Part 3.1 — @Consumes and Content-Type Mismatch

The @Consumes(MediaType.APPLICATION_JSON) annotation tells JAX-RS that the POST endpoint only accepts requests where the Content-Type header is application/json. If a client sends data with a different content type, such as text/plain or application/xml, JAX-RS will automatically reject the request before it even reaches the resource method.
Specifically, JAX-RS returns a 415 Unsupported Media Type HTTP response. The framework inspects the incoming Content-Type header, compares it against the declared @Consumes types, and if no match is found, it short-circuits the request processing entirely. This means the resource method is never invoked and no partial processing occurs. This behaviour protects the API from receiving malformed or unexpected data formats and ensures that Jackson, the JSON deserialiser, only receives data it can actually parse. Without this annotation, a client sending XML or plain text would cause a deserialisation exception deep inside the processing chain.

### Part 3.2 — @QueryParam vs Path Parameter for Filtering

Using @QueryParam for filtering (e.g. GET /api/v1/sensors?type=CO2) is considered superior to embedding the filter in the URL path (e.g. /api/v1/sensors/type/CO2) for several important reasons.
First, query parameters are semantically correct for filtering and searching. A path parameter identifies a specific resource — `/sensors/TEMP-001` identifies one unique sensor. A filter is not identifying a resource; it is narrowing down a collection. Using the path for filtering blurs this distinction and makes the API less intuitive.
Second, query parameters are optional by nature. If no ?type= is provided, the endpoint simply returns all sensors. With a path-based approach, you would need a separate route to handle the unfiltered case, leading to route duplication.
Third, query parameters compose well. Multiple filters can be added easily, such as ?type=CO2&status=ACTIVE, without any changes to the URL structure. Adding a second path-based filter would require restructuring the entire URL pattern.
Finally, query parameters are the established REST convention for filtering, sorting, and searching collections, and are widely understood by API consumers and tools such as Postman and Swagger.

### Part 4.1 — Sub-Resource Locator Pattern

The Sub-Resource Locator pattern provides significant architectural benefits in large APIs by promoting separation of concerns and keeping individual classes focused and manageable.
Without this pattern, every nested endpoint would need to be defined in a single resource class. As the API grows, a class like SensorResource would accumulate methods for managing sensors, their readings, their alerts, their calibration history, and any other nested resource — becoming a large and difficult-to-maintain controller class with hundreds of lines and unclear responsibilities.
By using the Sub-Resource Locator pattern, SensorResource simply delegates to SensorReadingResource by returning an instance of it when the /readings path is matched. All logic related to readings is encapsulated entirely within SensorReadingResource, which has a single, clear responsibility. This makes the code easier to read, test, and extend. Adding new functionality to readings only requires changes to SensorReadingResource, with no risk of accidentally breaking sensor logic. It also mirrors good object-oriented design principles — each class does one thing well.

### Part 5.2 — HTTP 422 vs 404 for Missing References

HTTP 422 Unprocessable Entity is more semantically accurate than 404 Not Found when a sensor references a non-existent room, because the two status codes describe fundamentally different problems.
A 404 Not Found means the requested resource URL does not exist — the thing the client is trying to reach cannot be found on the server. In this scenario, the client is successfully reaching POST /api/v1/sensors — that endpoint exists and is responding correctly. The problem is not that the URL is wrong.
The actual issue is that the request body is syntactically valid JSON but semantically invalid — it contains a roomId that refers to a resource that does not exist. The request cannot be processed not because of a missing URL but because of a broken reference inside the payload. HTTP 422 was designed precisely for this situation: the server understands the request, the JSON is well-formed, but the instructions cannot be carried out because the data fails a business rule or referential integrity check. Using 422 gives the client a much clearer signal about what went wrong and where to look to fix it.

### Part 5.4 — Security Risks of Exposing Stack Traces

Exposing internal Java stack traces to external API consumers presents serious cybersecurity risks.
First, stack traces reveal the internal structure of the application — package names, class names, method names, and line numbers. An attacker can use this information to map out the codebase, identify which frameworks and libraries are in use, and look up known vulnerabilities for those specific versions.
Second, stack traces can expose the technology stack in detail. For example, seeing org.glassfish.jersey or com.fasterxml.jackson in a trace immediately tells an attacker exactly which versions of which libraries are running, making it trivial to search for unpatched CVEs (Common Vulnerabilities and Exposures).
Third, exception messages embedded in stack traces sometimes contain sensitive data such as SQL queries, file paths, configuration values, or internal server hostnames — information that should never leave the server boundary.
Fourth, the line numbers and method signatures in a trace can help an attacker craft targeted exploits, such as injection attacks or logic bypasses, by revealing exactly where and how input is processed.
The Global Exception Mapper in this API addresses this by catching all unhandled exceptions and returning only a generic 500 Internal Server Error message to the client, while logging the full details server-side where only authorised developers can see them.

### Part 5.5 — JAX-RS Filters vs Manual Logging

Using JAX-RS filters for cross-cutting concerns like logging is far superior to manually inserting Logger.info() statements inside every resource method for several reasons.
First, filters implement the DRY (Don't Repeat Yourself) principle. With a filter, logging is defined once and applies automatically to every single endpoint. Manual logging requires adding the same boilerplate code to every method — and if a new endpoint is added and the developer forgets to add logging, that endpoint is silently unobserved.
Second, filters enforce consistency. Every request and response is logged in exactly the same format, making logs easier to read, search, and analyse. Manual logging is prone to inconsistency — different developers write log messages differently, some include more detail than others, and the format varies across methods.
Third, filters keep resource methods clean and focused on business logic. A room resource method should only contain code about managing rooms — not logging infrastructure. Mixing concerns makes code harder to read and maintain.
Fourth, filters can be added, removed, or modified without touching any resource class. If the logging format needs to change, only the filter class needs updating. With manual logging, every method across every resource class would need to be edited.
This separation is a core principle of Aspect-Oriented Programming (AOP) and is one of the key advantages of the JAX-RS filter architecture.
