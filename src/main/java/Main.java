import com.google.gson.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Main
{
    private static String url = "https://ru.wikipedia.org/wiki/%D0%A1%D0%BF%D0%B8%D1%81%D0%BE%D0%BA_%D1%81%D1%82%D0%B0%D0%BD%D1%86%D0%B8%D0%B9_%D0%9C%D0%BE%D1%81%D0%BA%D0%BE%D0%B2%D1%81%D0%BA%D0%BE%D0%B3%D0%BE_%D0%BC%D0%B5%D1%82%D1%80%D0%BE%D0%BF%D0%BE%D0%BB%D0%B8%D1%82%D0%B5%D0%BD%D0%B0";
    private static StationIndex stationIndex;
    private static String dataFile = "data/metro.json";

    public static void main(String[] args) throws FileNotFoundException {
        getStationIndex(url);

        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(StationIndex.class, new StationIndexSerializer())
                .create();
        String json = gson.toJson(stationIndex);
        PrintWriter writer = new PrintWriter(dataFile);
        writer.write(json);
        writer.flush();
        writer.close();

        getStationCount();
    }
    private static void getStationIndex (String url)
    {
        stationIndex = new StationIndex();
        try
        {
            Document doc = Jsoup.connect(url).maxBodySize(0).get();
            addLines(doc);
            addStations(doc);
            addConnections(doc);
        }
        catch (Exception ex){
            ex.printStackTrace();
        }

    }

    private static void addLines(Document doc)
    {
        for (Element row : doc.select("table.standard.sortable tr")) //add Lines
        {

            if (row.select("td:nth-of-type(1)").text().equals("")){
                continue;
            }
            List<String> nameLine = row.select("td:nth-of-type(1)").select("span").eachAttr("title");
            List<String> numberLine = row.select("td:nth-of-type(1)").select("span").eachText();

            if (nameLine.get(0) == null || numberLine.get(0) == null)
            {
                continue;
            }
            Line line = new Line(numberLine.get(0), nameLine.get(0));
            stationIndex.addLine(line);


            if (nameLine.size() > 1){
                Line line2 = new Line(numberLine.get(1), nameLine.get(1));
                stationIndex.addLine(line2);
            }
        }
    }

    private static void addStations(Document doc)
    {
        for (Element row : doc.select("table.standard.sortable tr"))  //add Station
        {

            if (row.select("td:nth-of-type(1)").text().equals("")){
                continue;
            }
            List<String> nameLine = row.select("td:nth-of-type(1)").select("span").eachAttr("title");
            List<String> numberLine = row.select("td:nth-of-type(1)").select("span").eachText();
            String nameStation = row.select("td:nth-of-type(2)").select("a").first().text();

            Line line = new Line(numberLine.get(0), nameLine.get(0));
            Station station = new Station(nameStation, line);
            stationIndex.addStation(station);

            if (nameLine.size() > 1){
                Line line2 = new Line(numberLine.get(1), nameLine.get(1));
                Station station2 = new Station(nameStation, line2);
                stationIndex.addStation(station2);
            }
        }
    }

    private static void addConnections(Document doc)
    {
        for (Element row : doc.select("table.standard.sortable tr"))  //add Connection
        {

            if (row.select("td:nth-of-type(1)").text().equals("")) {
                continue;
            }
            List<String> nameLine = row.select("td:nth-of-type(1)").select("span").eachAttr("title");
            List<String> numberLine = row.select("td:nth-of-type(1)").select("span").eachText();
            String nameStation = row.select("td:nth-of-type(2)").select("a").first().text();
            List<String> connect = row.select("td:nth-of-type(4)").select("a").eachAttr("title");
            List<String> numberLineConnect = row.select("td:nth-of-type(4)").select("span").eachText();

            if (!connect.isEmpty())
            {
                for (int i = 0; i < connect.size(); i++)
                {

                    String c = connect.get(i).trim();
                    String n = numberLineConnect.get(i).trim().charAt(0) == '0'
                            ? numberLineConnect.get(i).substring(1).trim() : numberLineConnect.get(i).trim();

                    c = c.substring(c.indexOf("станцию") + 7).trim();
                    String[] con = c.split(" ");
                    String conStation = con[0].trim();
                    for (int j = 0; j < con.length; j++)
                    {
                            if (stationIndex.getStation(conStation) == null) {
                                conStation = conStation + " " + con[(1 + j)];
                                continue;
                            }
                            Station connectedStation = new Station(conStation, stationIndex.getLine(n));
                            Line line = new Line(numberLine.get(0).trim(), nameLine.get(0).trim());
                            Station station = new Station(nameStation, line);
                            List<Station> connections = new ArrayList<>();
                            connections.add(connectedStation);
                            connections.add(station);
                        try {
                            stationIndex.addConnection(connections);
                        }
                        catch (Exception ex){
                            System.out.println(connections);
                            System.out.println(n);
                        }



                            if (nameLine.size() > 1) {
                                Line line2 = new Line(numberLine.get(1), nameLine.get(1));
                                Station station2 = new Station(nameStation, line2);
                                List<Station> connections2 = new ArrayList<>();
                                connections2.add(connectedStation);
                                connections2.add(station2);
                                stationIndex.addConnection(connections2);
                            }

                        break;
                    }
                }
            }
        }
    }

    private static void getStationCount()
    {
        try
        {
            JSONParser parser = new JSONParser();
            JSONObject jsonData = (JSONObject) parser.parse(getJsonFile());

            JSONObject stationsObject = (JSONObject) jsonData.get("stations");
            parseStations(stationsObject);
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void parseStations(JSONObject stationsObject)
    {

        stationsObject.keySet().forEach(lineNumberObject ->
        {
            String lineNumber = (String) lineNumberObject;
            JSONArray stationsArray = (JSONArray) stationsObject.get(lineNumberObject);

            System.out.println("Номер линии - " + lineNumber + ", всего станций - " + stationsArray.size());
        });
    }

    private static String getJsonFile()
    {
        StringBuilder builder = new StringBuilder();
        try {
            List<String> lines = Files.readAllLines(Paths.get(dataFile));
            lines.forEach(builder::append);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return builder.toString();
    }
}

