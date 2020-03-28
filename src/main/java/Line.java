import java.util.ArrayList;
import java.util.List;

public class Line implements Comparable<Line> {
    private String number;
    private String name;
    private List<Station> stations;

    public Line(String number, String name) {
        this.number = number.trim().charAt(0) == '0' ? number.substring(1).trim() : number.trim();
        this.name = name;
        stations = new ArrayList<>();
    }

    public String getNumber() {
        return number;
    }

    public String getName() {
        return name;
    }

    public void addStation(Station station) {
        stations.add(station);
    }

    public List<Station> getStations() {
        return stations;
    }

    @Override
    public int compareTo(Line line) {

            int number1 = Integer.parseInt(number.replaceAll("[^0-9]", ""));
            int number2 = Integer.parseInt(line.getNumber().replaceAll("[^0-9]", ""));
            if (number.replaceAll("[^0-9]", "").length()
                    < number.length() && number1 == number2) {

                return 1;
            }
            if (line.getNumber().replaceAll("[^0-9]", "").length()
                    < line.getNumber().length() && number1 == number2) {

                return -1;
            }
            if (number.equals(line.getNumber())) {

                return 0;

            } else {
                return Integer.compare(number1, number2);
            }

    }

    @Override
    public boolean equals(Object obj) {
        return compareTo((Line) obj) == 0;
    }

    @Override
    public String toString() {
        return "Line{" +
                "number='" + number + '\'' +
                ", name='" + name + '\'' +
                //", stations=" + stations +
                '}';
    }
}
