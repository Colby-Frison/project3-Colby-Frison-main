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

    private static int stops;

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

    public static void readFile(String filename) throws FileNotFoundException, IOException {

		// construct a file object for the file with the given name.
		File file = new File(filename);

		// construct a scanner to read the file.
		Scanner fileScanner = new Scanner(file);
		
		// initiliaze trip
		trip = new ArrayList<TripPoint>();

		// create the Array that will store each lines data so we can grab the time, lat, and lon
		String[] fileData = null;

		// grab the next line
		while (fileScanner.hasNextLine()) {
			String line = fileScanner.nextLine();

			// split each line along the commas
			fileData = line.split(",");

			// only write relevant lines
			if (!line.contains("Time")) {
				// fileData[0] corresponds to time, fileData[1] to lat, fileData[2] to lon
				trip.add(new TripPoint(Integer.parseInt(fileData[0]), Double.parseDouble(fileData[1]), Double.parseDouble(fileData[2])));
			}
		}

		// close scanner
		fileScanner.close();
	}

    public static double DegreesToRadians(double degrees) // convert from degree to radian for haversine formula
    {
        return degrees * Math.PI / 180.0;
    }

    public static double haversineDistance(TripPoint first, TripPoint second)
    {
        double lat1 = first.getLat();
		double lat2 = second.getLat();
		double lon1 = first.getLon();
		double lon2 = second.getLon();
		
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
 
        // convert to radians
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
 
        // apply formulae
        double a = Math.pow(Math.sin(dLat / 2), 2) +
                   Math.pow(Math.sin(dLon / 2), 2) *
                   Math.cos(lat1) *
                   Math.cos(lat2);
        double rad = 6371;
        double c = 2 * Math.asin(Math.sqrt(a));
        return rad * c;
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
		double displacementThreshold = 0.6; // Threshold in kilometers
        movingTrip = new ArrayList<>(trip); // add all points from trip to movingTrip

        stops = 0; // reset stops
        for (int i = 1; i < trip.size(); i++) { // loop
            double distance = haversineDistance(trip.get(i), trip.get(i - 1)); // get distance of point 'i' and point 'i - 1'
            if (distance <= displacementThreshold) { // compare previous distance to the threshold
                movingTrip.remove(trip.get(i)); // remove stopped point from moving trip
                stops++; // add to stops
            }
        }
        return stops;
	}

	public static int h2StopDetection(){
    /* 
    I just realized that the tests say there are more stops even 
    when the threshold is smaller, so I'm  not sure how that works

    the code I have here is way different from the first one I'll 
    try to put notations, but in case its indecernable.

    This one uses a nested loop to loop through every point past 
    current point, this makes it so if any of the points in the entire 
    list are within a .5km threshold of the current point it is counted 
    as a stop then removed from the list. In a real world application 
    this would defiently not work, but I was trying whatever I could

    you should be able to look at previous commits on the github to see 
    what I was doing before.
    */ 

		double threshold = 0.5; // Threshold in kilometers
        movingTrip = new ArrayList<>(trip);

        stops = 0;

        double distance = 0.0;
        for (int i = 0; i < movingTrip.size() - 1; i++) {
            TripPoint currentPoint = movingTrip.get(i);

            int j = 1;
            distance = haversineDistance(currentPoint, movingTrip.get(i + j));
            while(j < movingTrip.size() - i - 1 && distance <= threshold){
                if (distance <= threshold) {
                    movingTrip.remove(movingTrip.get(i + j));
                    stops++;
                }
                j++;
                distance = haversineDistance(currentPoint, movingTrip.get(i + j));
            }
        }
        return stops;
	}

    public static double getTimeDifference(TripPoint point1, TripPoint point2) {
        // Assuming TripPoint has a field representing time in hours
        double timeDifference = Math.abs(point1.time - point2.time);
        return timeDifference;
    }
	
	public static double movingTime(){
        return (totalTime() - ((stops * 5) / 60.0));
	}

	public static double stoppedTime(){
        return (totalTime() - movingTime());
	}

	public static double avgMovingSpeed(){
        return totalDistance() / movingTime(); // test on h1 says it should be 103.7 but I'm getting 103.8 so...
	}

	public static ArrayList<TripPoint> getMovingTrip(){
		return new ArrayList<>(movingTrip);
	}

}
