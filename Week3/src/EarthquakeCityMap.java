package module4;

import java.util.ArrayList;
import java.util.List;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.GeoJSONReader;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.*;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.MultiMarker;
import de.fhpotsdam.unfolding.providers.Google;
import de.fhpotsdam.unfolding.providers.MBTilesMapProvider;
import de.fhpotsdam.unfolding.utils.MapUtils;
import parsing.ParseFeed;
import processing.core.PApplet;

/** EarthquakeCityMap
 * An application with an interactive map displaying earthquake data.
 * Author: UC San Diego Intermediate Software Development MOOC team
 * @author Xin Zhang
 * Date: 01/14/2016
 * */
public class EarthquakeCityMap extends PApplet {
	
	// We will use member variables, instead of local variables, to store the data
	// that the setUp and draw methods will need to access (as well as other methods)
	// You will use many of these variables, but the only one you should need to add
	// code to modify is countryQuakes, where you will store the number of earthquakes
	// per country.
	
	// You can ignore this.  It's to get rid of eclipse warnings
	private static final long serialVersionUID = 1L;

	// IF YOU ARE WORKING OFFILINE, change the value of this variable to true
	private static final boolean offline = false;
	
	/** This is where to find the local tiles, for working without an Internet connection */
	public static String mbTilesString = "blankLight-1-3.mbtiles";
	
	
	//feed with magnitude 2.5+ Earthquakes
	private String earthquakesURL = "http://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_week.atom";
	
	// The files containing city names and info and country names and info
	private String cityFile = "city-data.json";
	private String countryFile = "countries.geo.json";
	
	// The map
	private UnfoldingMap map;
	
	// Markers for each city
	private List<Marker> cityMarkers;
	
	// Markers for each earthquake
	private List<Marker> quakeMarkers;
	
	// A List of country markers
	private List<Marker> countryMarkers;
	
	public void setup() {		
		// (1) Initializing canvas and map tiles
		size(900, 700, OPENGL);
		if (offline) {
		    map = new UnfoldingMap(this, 200, 50, 650, 600, new MBTilesMapProvider(mbTilesString));
		    earthquakesURL = "2.5_week.atom";  // The same feed, but saved August 7, 2015
		}
		else {
			map = new UnfoldingMap(this, 200, 50, 650, 600, new Google.GoogleMapProvider());
			// IF YOU WANT TO TEST WITH A LOCAL FILE, uncomment the next line
		    //earthquakesURL = "2.5_week.atom";
		}
		MapUtils.createDefaultEventDispatcher(this, map);
		
		// FOR TESTING: Set earthquakesURL to be one of the testing files by uncommenting
		// one of the lines below.  This will work whether you are online or offline
		//earthquakesURL = "test1.atom";
		//earthquakesURL = "test2.atom";
		
		// WHEN TAKING THIS QUIZ: Uncomment the next line
		earthquakesURL = "quiz1.atom";
		
		
		// (2) Reading in earthquake data and geometric properties
	    //     STEP 1: load country features and markers
		// 	   type: Polygon
		List<Feature> countries = GeoJSONReader.loadData(this, countryFile);
		countryMarkers = MapUtils.createSimpleMarkers(countries);
		
		//     STEP 2: read in city data
		//     type: Point
		List<Feature> cities = GeoJSONReader.loadData(this, cityFile);
		
		cityMarkers = new ArrayList<Marker>(); // private List<Marker> cityMarkers;
		for(Feature city : cities) {
		  cityMarkers.add(new CityMarker(city));
		 // System.out.println(city.getClass()), 输出的都是PointFeature
		}
	    
		//     STEP 3: read in earthquake RSS feed
		// 先通过自定义实现的解析函数，将所有earthquakes数据，解析出来存成 list PointFeature
	    List<PointFeature> earthquakes = ParseFeed.parseEarthquake(this, earthquakesURL);
	    quakeMarkers = new ArrayList<Marker>(); // private List<Marker> quakeMarkers;
	    
	    // 既然是list of PointFeature, 那么自然就要用SimplePointMarker来实现visual representation
	    // 但是在我们这个编程作业中，为了更好的配合 继承inheritance和多态polymorphism的概念，因此有更细致的实现方式。
	    /*
		我们来设计class hierarchy,首先肯定得有LandQuakeMarker和OceanQuakeMarker, 因为我们要在不同的地方有不同的展示。这两个肯定是实际的
		class, 但是我们也能够很明显的直到，这两个类中，他们是有很多common behavior的，比如都有地震的magnitude信息，depth信息等等，但是
		又希望Split different behavior into separate classes比如发生在不同的地方的marker，我希望visual的表示也不同，因此一些绘制visual
		representation的函数，需要声明成abstract即抽象方法，从而我们可以很明显的直到，需要一个抽象类也即EarthquakeMarker Class, 同时
		这个抽象类要继承自SimplePointMarker类。
	     */
	    
	    for(PointFeature feature : earthquakes) {
	    	
	      // ParseFeed解析出来的RSS feed地震信息中包含的properties如下
	      // {title=M 3.0 - 66km ESE of Lakeview, Oregon, age=Past Hour, magnitude=3.0, depth=9.7}
	    
		  //check if LandQuake
		  if(isLand(feature)) {
		    quakeMarkers.add(new LandQuakeMarker(feature));
		  }
		  // OceanQuakes
		  else {
		    quakeMarkers.add(new OceanQuakeMarker(feature));
		  }
	    }

	    // could be used for debugging
	    /*
 		This method should use System.out.println() to list each country for which there was 1 or more earthquakes and
 		the number of earthquakes detected in that country. Then it should print out the number of quakes that were 
 		detected in the ocean. Note that this method is not trivial. You will have to calculate the number of 
 		earthquakes per country from the information you have available. As an aside: If you are running the applet 
 		with a large earthquake file/feed (e.g. 1.0+ Past week or 30 days), you might find that printQuakes takes a 
 		long time to run. Feel free to comment out the call to printQuakes() in setup once you get it working if you 
 		find this is the case.
	    */
	    printQuakes( earthquakes );
	    	 		
	    // (3) Add markers to map
	    //     NOTE: Country markers are not added to the map.  They are used
	    //           for their geometric properties
	    
	    // map.addMarkers(countryMarkers);
	    map.addMarkers(quakeMarkers);
	    map.addMarkers(cityMarkers);
	    
	}  // End setup
	
	
	public void draw() {
		background(0);
		map.draw();
		addKey();
		
	}
	
	// helper method to draw key in GUI
	// TODO: Update this method as appropriate
	private void addKey() {	
		// Remember you can use Processing's graphics methods here
		fill(255, 250, 240);
		rect(25, 50, 150, 300);
		
		fill(0);
		textAlign(LEFT, CENTER);
		textSize(12);
		text("Earthquake Key", 50, 75);
		
		// icon
		fill(color(150, 30, 30));
		triangle(40, 90, 35, 100, 45, 100);
		fill(color(255, 255, 255));
		ellipse(40, 120, 10, 10);
		rect(35, 140, 10, 10);
		
		fill(color(255, 255, 0));
		ellipse(40, 200, 15, 15);
		fill(color(0, 0, 255));
		ellipse(40, 240, 15, 15);
		fill(color(255, 0, 0));
		ellipse(40, 280, 15, 15);
		
		// text
		fill(0, 0, 0);
		text("City Marker", 60, 95);
		text("Land Quake", 60, 120);
		text("Ocean Quake", 60, 145);
		textAlign(LEFT, CENTER);
		textSize(12);
		text("Size ~ Magnitude", 50, 175);
		fill(0, 0, 0);
		text("Shallow", 60, 200);
		text("Intermediate", 60, 240);
		text("Deep", 60, 280);
		
		
		/*
		fill(color(255, 0, 0));
		ellipse(50, 125, 15, 15);
		fill(color(255, 255, 0));
		ellipse(50, 175, 10, 10);
		fill(color(0, 0, 255));
		ellipse(50, 225, 5, 5);
		
		fill(0, 0, 0);
		text("5.0+ Magnitude", 75, 125);
		text("4.0+ Magnitude", 75, 175);
		text("Below 4.0", 75, 225);
		*/
	}

	
	
	// Checks whether this quake occurred on land.  If it did, it sets the 
	// "country" property of its PointFeature to the country where it occurred
	// and returns true.  Notice that the helper method isInCountry will
	// set this "country" property already.  Otherwise it returns false.
	private boolean isLand(PointFeature earthquake) {
		// a list of country markers
		// private List<Marker> countryMarkers;
		
		// IMPLEMENT THIS: loop over all countries to check if location is in any of them		
		for(int i = 0; i < countryMarkers.size(); i++){
			if( isInCountry(earthquake, countryMarkers.get(i)) == true ){
				return true;
			}
		}
		// not inside any country
		return false;
	}
		
	// helper method to test whether a given earthquake is in a given country
	// This will also add the country property to the properties of the earthquake 
	// feature if it's in one of the countries.
	// You should not have to modify this code
	private boolean isInCountry(PointFeature earthquake, Marker country) {
		
		// getting location of feature
		// Location信息是在ParseFeed解析处理时，创建SimplePointFeatrue时extract出来并且传入的。
		
		Location checkLoc = earthquake.getLocation(); // de.fhpotsdam.unfolding.geo.Location
		// some countries represented it as MultiMarker
		// looping over SimplePolygonMarkers which make them up to use isInsideByLoc
		/*
		注意关于countries的marker中，根据输入文件可以知道有些国家的Feature type是MultiPolygon, 比如美国United States,它其实是由多个
		polygon area组成的！！需要注意！！因此，在我们用API创建SimpleMarker时，API内部应该是会根据不同的Feature Type创建不同的Marker比如
		对于MultiPolygon，是创建 MultiMarker. 对于一般的Polygon, 是创建SimplePolygonMarker
		*/
		// System.out.println( country.getClass() );
		/*
		boolean	isInsideByLocation(float latitude, float longitude) 
			--Checks whether given position is inside this marker, according to the shape defined by the marker's 
			  locations.

		boolean	isInsideByLocation(Location location)  
		 */
		
		if(country.getClass() == MultiMarker.class) {
			// looping over markers making up MultiMarker
			for(Marker marker : ((MultiMarker)country).getMarkers()) {
				// checking if inside
				if(((SimplePolygonMarker)marker).isInsideByLocation(checkLoc)) {
					earthquake.addProperty("country", country.getProperty("name"));	
					// return if is inside one
					return true;
				}
			}
		}
		// check if inside country represented by SimplePolygonMarker
		else if(((SimplePolygonMarker)country).isInsideByLocation(checkLoc)) {
			earthquake.addProperty("country", country.getProperty("name"));
			return true;
		}
		return false;
	}
	
	// prints countries with number of earthquakes
	// You will want to loop through the country markers or country features
	// (either will work) and then for each country, loop through
	// the quakes to count how many occurred in that country.
	// Recall that the country markers have a "name" property, 
	// And LandQuakeMarkers have a "country" property set.
	private void printQuakes(List<PointFeature> earthquakes) {
		int size = countryMarkers.size();
		int numQuakeInLand = 0;
		int numQuakeInOcean = 0;
		for(int i = 0; i < size; i++){
			String countryName = (String)countryMarkers.get(i).getProperty("name");
			int count = 0;
			for(int j = 0; j < earthquakes.size(); j++){
				if( earthquakes.get(j).properties.containsKey("country") ){
					
					String inWhichCountry = (String)earthquakes.get(j).properties.get("country");
					if( inWhichCountry.equals(countryName) ){
						count++;
					}
				}
			}
			if( count > 0 ){
				System.out.println("Country: " + countryName + "	" + count + " quakes. ");
			}
		}
		// count how many earthquakes occur in land
		for(int j = 0; j < earthquakes.size(); j++){
			if( earthquakes.get(j).properties.containsKey("country") ){
				numQuakeInLand++;
			}
		}
		// count how many earthquakes occur in ocean
		numQuakeInOcean = earthquakes.size() - numQuakeInLand;
		System.out.println("" + numQuakeInOcean + " quakes in Ocean. " );
	}

}
