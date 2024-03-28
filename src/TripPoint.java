import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Scanner;

public class TripPoint {
    
    //variables
    private double lat;
    private double lon;
    private int time;
    private static ArrayList<TripPoint> trip = new ArrayList<TripPoint>();
	private static ArrayList<TripPoint> movingTrip = new ArrayList<TripPoint>();

    //Constructor
    public TripPoint(int time, double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
        this.time = time;

    }

	public TripPoint() {
		time = 0;
		lat = 0.0;
		lon = 0.0;
	}

    //Getters
    public double getLat() {
        return this.lat;
    }
    public double getLon() {
        return this.lon;
    }
    public int getTime() {
        return this.time;
    }
    public static ArrayList<TripPoint> getTrip() {
        ArrayList<TripPoint> copiedList = new ArrayList<>();

        // Using a loop to copy each element from original to copied list
        for (TripPoint point : trip) {
            // Assuming TripPoint has a copy constructor or is immutable
            // If not, you may need to create new TripPoint objects
            TripPoint copiedPoint = new TripPoint(point.getTime(), point.getLat(), point.getLon());
            copiedList.add(copiedPoint);
        }

        return copiedList;
    }

    public static void readFile(String fileName) throws FileNotFoundException{

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            // Skip the header row
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(","); // split based on comma
                if (parts.length >= 3) { // ensure line is formatted properly
                    int time = Integer.parseInt(parts[0]); // assign values while parsing to correct values
                    double latitude = Double.parseDouble(parts[1]);
                    double longitude = Double.parseDouble(parts[2]);
                    trip.add(new TripPoint(time, latitude, longitude)); // add to list
                } else {
                    System.err.println("Incorrect file formating at line: " + line); //error message if file formatted incorectly
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading the file: " + e.getMessage()); // filename is wrong
        } catch (NumberFormatException e) {
            System.err.println("Error converting latitude/longitude to double: " + e.getMessage()); // lat or long is formatted inccorectly
        }
    }

    private static double EarthRadiusKm = 6371.0; // constant for radius of the Earth in kilometers

    public static double DegreesToRadians(double degrees) // convert from degree to radian for haversine formula
    {
        return degrees * Math.PI / 180.0;
    }

    public static double haversineDistance(TripPoint a, TripPoint b)
    {
        double lat1 = DegreesToRadians(a.getLat()); // assign lats and lons to variables 
        double lon1 = DegreesToRadians(a.getLon());
        double lat2 = DegreesToRadians(b.getLat());
        double lon2 = DegreesToRadians(b.getLon());

        double deltaLat = lat2 - lat1; // change in lat
        double deltaLon = lon2 - lon1; // change in lon

        double c = Math.pow(Math.sin(deltaLat / 2), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(deltaLon / 2), 2); // formula p1

        double d = 2 * Math.atan2(Math.sqrt(c), Math.sqrt(1 - c)); // formula p2

        double distance = EarthRadiusKm * d;

        return distance;
    }

    public static double totalDistance(){
        double totalDist = 0.0; // create variable

        TripPoint point1; //create empty trippoints
        TripPoint point2;

        for(int i = 1; i < trip.size(); i++){ // loop through trip arraylist and get distance between then add to totalDist
            point1 = trip.get(i - 1);
            point2 = trip.get(i);

            totalDist += haversineDistance(point1, point2);
        }

        return totalDist; //return
    }

	public static double totalMovingDistance(){
        double totalDist = 0.0; // create variable

        TripPoint point1; //create empty trippoints
        TripPoint point2;

        for(int i = 1; i < movingTrip.size(); i++){ // loop through trip arraylist and get distance between then add to totalDist
            point1 = movingTrip.get(i - 1);
            point2 = movingTrip.get(i);

            totalDist += haversineDistance(point1, point2);
        }

        return totalDist; //return
    }

    public static double totalTime(){
        return (trip.get(trip.size() - 1).getTime() / 60.0); // get time of the final tripPoint then convert to hours
    }

    public static double avgSpeed(TripPoint point1, TripPoint point2) {
        double distance = haversineDistance(point1, point2); // Calculate distance between the points
        double timeDifference = Math.abs(point2.getTime() - point1.getTime()); // Calculate time difference between the points in hours
    
        return distance / (timeDifference / 60); // Calculate and return average speed
    }


	public static int h1StopDetection(){
		double buffer = 0.6; // threshold radius to determine moving or not

		int stops = 0;

		int tempIndex = -1;
		boolean stopped = false;

		movingTrip = new ArrayList<>(trip);

		for(int i = 1; i < movingTrip.size(); i++){
			if(!stopped){
				if(haversineDistance(movingTrip.get(i - 1), movingTrip.get(i)) <= buffer){
					stopped = true;
					tempIndex = i;
				}
			}
			if(stopped){
				if(haversineDistance(movingTrip.get(tempIndex - 1), movingTrip.get(i)) <= buffer){
					stops++;
					movingTrip.remove(i);
					i--;
				}
				else{
					stopped = false;
				}
			}
		}

		return stops;
	}

	public static int h2StopDetection(){
		double buffer = 1.72; // threshold radius to determine moving or not

		int stops = 0;

		int tempIndex = -1;
		boolean stopped = false;

		movingTrip = new ArrayList<>(trip);

		for(int i = 1; i < movingTrip.size(); i++){
			if(!stopped){
				if(haversineDistance(movingTrip.get(i - 1), movingTrip.get(i)) <= buffer){
					stopped = true;
					tempIndex = i;
				}
			}
			if(stopped){
				if(haversineDistance(movingTrip.get(tempIndex - 1), movingTrip.get(i)) <= buffer){
					stops++;
					movingTrip.remove(i);
					i--;
				}
				else{
					stopped = false;
				}
			}
		}

		return stops;
	}
	
	public static double movingTime(){
		return ((movingTrip.size() - 1) * 5 - 10) / 60.000;
	}

	public static double stoppedTime(){
		ArrayList<TripPoint> temp1 = getMovingTrip();
		ArrayList<TripPoint> temp2 = getTrip();
		return ((temp2.size() * 5 - 10) - ((temp1.size() - 1 * 5 - 10))) / 60;
	}

	public static double avgMovingSpeed(){
		double dist = totalMovingDistance();
		double time = movingTime();

		return dist/time;
	}

	public static ArrayList<TripPoint> getMovingTrip(){
		return new ArrayList<>(movingTrip);
	}

}
