import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.TreeSet;

public class StationIndexSerializer implements JsonSerializer<StationIndex>
{
    @Override
    public JsonElement serialize(StationIndex src, Type typeOfSrc, JsonSerializationContext context)
    {
        JsonObject result = new JsonObject();
        JsonArray lines = new JsonArray();
        JsonObject stations = new JsonObject();
        JsonArray connections = new JsonArray();
        result.add("lines", lines);
        result.add("stations", stations);
        result.add("connections", connections);

        for (Map.Entry<String, Line> entry : src.number2line.entrySet()) {
            JsonObject line = new JsonObject();
            line.add("number", new JsonPrimitive(entry.getValue().getNumber()));
            line.add("name", new JsonPrimitive(entry.getValue().getName()));
            lines.add(line);

            JsonArray stationsOnLine = new JsonArray();
            for (Station station : src.stations)
            {
                if (station.getLine().equals(entry.getValue())){
                    stationsOnLine.add(new JsonPrimitive(station.getName()));
                }
            }
            stations.add(entry.getValue().getNumber(), stationsOnLine);
        }

        for (Map.Entry<Station, TreeSet<Station>> entry : src.connections.entrySet())
        {
            JsonArray stationsConnected = new JsonArray();
            JsonObject stationCon1 = new JsonObject();
            stationCon1.add("line", new JsonPrimitive(entry.getKey().getLine().getNumber()));
            stationCon1.add("station", new JsonPrimitive(entry.getKey().getName()));
            for (Station station : entry.getValue()){
                JsonObject stationCon = new JsonObject();
                stationCon.add("line", new JsonPrimitive(station.getLine().getNumber()));
                stationCon.add("station", new JsonPrimitive(station.getName()));
                stationsConnected.add(stationCon);
            }
            stationsConnected.add(stationCon1);

            connections.add(stationsConnected);
        }

        return result;
    }
}
