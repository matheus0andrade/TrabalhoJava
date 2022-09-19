import java.util.ArrayList;

public class Airport {
    String name, shortName, cidade, estado;
    double latitude, longitude;
    Integer index;
    public static class Connections {
        double weight;
        Airport to;
        public Connections(double d, Airport a) {
            this.weight = d;
            this.to = a;
        }
        public Airport getAirport() { return to;}
        public double getDistance() { return weight;}
    }
    ArrayList<Connections> edges = new ArrayList<>();
    // Construtor padrão
    public Airport(String name, String shortName, String cidade, String estado, double latitude, double longitude, Integer index) {
        this.index = index;
        this.name = name;
        this.shortName = shortName;
        this.cidade = cidade;
        this.estado = estado;
        this.latitude = latitude * Math.PI / 180;
        this.longitude = longitude * Math.PI / 180;
    }

    public double getLongitude() { return longitude;}
    public double getLatitude() { return latitude;}
    public String getEstado() { return estado;}
    public String getCidade() { return cidade;}
    public String getShortName() { return shortName;}
    public Integer getIndex() { return index;}
    public ArrayList<Connections> getConnections() { return edges;}

    public Double distanceTo(Airport airport) {
        Double x1 = Math.cos(latitude) * Math.sin(longitude), x2 = Math.cos(airport.latitude) * Math.sin(airport.longitude);
        Double y1 = Math.cos(latitude) * Math.cos(longitude), y2 = Math.cos(airport.latitude) * Math.cos(airport.longitude);
        Double z1 = Math.sin(latitude), z2 = Math.sin(airport.latitude);
        return 2 * Math.asin(Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2) + (z1 - z2) * (z1 - z2)) / 2);
    }
    public boolean addConnection(double w, Airport t) {
        if(w < 1.5) {
            edges.add(new Connections(w, t));
            return true;
        }
        return false;
    }
    public void removeConnection(Airport t) {
        for(int i = 0; i < edges.size(); i++) {
            if(edges.get(i).getAirport() == t) {
                edges.remove(i);
                break;
            }
        }
    }
    public void print() {
        System.out.println(name + "(" + shortName + "): " + latitude + " " + longitude);
    }
    public void printConnections() {
        for (Connections e : edges) {
            System.out.println(shortName + " to " + e.getAirport().getShortName() + " with distance " + e.getDistance());
        }
    }
}