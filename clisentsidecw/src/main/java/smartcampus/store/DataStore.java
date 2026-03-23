package smartcampus.store;

import smartcampus.model.Room;
import smartcampus.model.Sensor;
import smartcampus.model.SensorReading;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DataStore {

    // ConcurrentHashMap is thread-safe — prevents data corruption if two
    // requests arrive at the exact same moment
    public static final Map<String, Room> rooms = new ConcurrentHashMap<>();
    public static final Map<String, Sensor> sensors = new ConcurrentHashMap<>();
    public static final Map<String, List<SensorReading>> readings = new ConcurrentHashMap<>();
}