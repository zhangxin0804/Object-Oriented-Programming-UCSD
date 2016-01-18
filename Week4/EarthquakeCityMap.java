package module5;

import java.util.ArrayList;
import java.util.List;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.GeoJSONReader;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.AbstractShapeMarker;
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
 * Date: 01/20/2016
 * */
public class EarthquakeCityMap extends PApplet {
	
	// We will use member variables, instead of local variables, to store the data
	// that the setup and draw methods will need to access (as well as other methods)
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
	
	// NEW IN MODULE 5
	private CommonMarker lastSelected;
	private CommonMarker lastClicked;
	
	private int bgColorRed;
	private int bgColorGreen;
	private int bgColorBlue;
	
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
		
		
		// (2) Reading in earthquake data and geometric properties
	    //     STEP 1: load country features and markers
		List<Feature> countries = GeoJSONReader.loadData(this, countryFile);
		countryMarkers = MapUtils.createSimpleMarkers(countries);
		
		//     STEP 2: read in city data
		List<Feature> cities = GeoJSONReader.loadData(this, cityFile);
		cityMarkers = new ArrayList<Marker>();
		for(Feature city : cities) {
		  cityMarkers.add(new CityMarker(city));
		}
	    
		//     STEP 3: read in earthquake RSS feed
	    List<PointFeature> earthquakes = ParseFeed.parseEarthquake(this, earthquakesURL);
	    quakeMarkers = new ArrayList<Marker>();
	    
	    for(PointFeature feature : earthquakes) {
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
	    printQuakes();
	 		
	    // (3) Add markers to map
	    //     NOTE: Country markers are not added to the map.  They are used
	    //           for their geometric properties
	    map.addMarkers(quakeMarkers);
	    map.addMarkers(cityMarkers);
	    
	    
	    bgColorRed = 0;
	    bgColorGreen = 0;
	    bgColorBlue = 0;
	}  // End setup
	
	
	public void draw() {
		
		// 注意这里一定要加上一个背景颜色的绘制，否则drag地图时，会造成颜色拖尾的现象。
		background(bgColorRed, bgColorGreen, bgColorBlue); 
		
		map.draw();
		addKey();
		// 绘制两个button, 用于改变背景颜色，测试event handler
		// drawButton();
		
	}
	
	//-------------- Just Test Customized Event Handler ------------------//	
	private void drawButton(){
		fill(255, 255, 0);
		rect(100, 500, 25, 25);
		fill(100, 100, 100);
		rect(100, 550, 25, 25);		
	}
		
	public void mouseReleased(){
		if( mouseX > 100 && mouseX < 125 && mouseY > 500 && mouseY < 525 ){
			bgColorRed = 255;
			bgColorGreen = 255;
			bgColorBlue = 255;
		}else if( mouseX > 100 && mouseX < 125 && mouseY > 550 && mouseY < 575 ){
			bgColorRed = 100;
			bgColorGreen = 100;
			bgColorBlue = 100;
		}
	}
	//-------------- Just Test Customized Event Handler ------------------//
	
	/** Event handler that gets called automatically when the 
	 * mouse moves.
	 */
	@Override
	public void mouseMoved()
	{
		// clear the last selection
		// 因为假如我们从marker上hover离开后，要把之前的select status要复原成false, 这样就不会再去drawTitle了。
		if (lastSelected != null) {
			lastSelected.setSelected(false);
			lastSelected = null;
		}
		selectMarkerIfHover(quakeMarkers);
		selectMarkerIfHover(cityMarkers);
	}
	
	// If there is a marker under the cursor, and lastSelected is null 
	// set the lastSelected to be the first marker found under the cursor
	// Make sure you do not select two markers.
	// 
	private void selectMarkerIfHover(List<Marker> markers)
	{
		// TODO: Implement this method
		if( lastSelected != null ){
			return;
		}
		for(int i = 0; i < markers.size(); i++){
			Marker mk = markers.get(i);
			if( mk.isInside(map, mouseX, mouseY) ){
				lastSelected = (CommonMarker)mk;
				lastSelected.setSelected(true);
				break;
			}
		}
		
	}
	
	/** The event handler for mouse clicks
	 * It will display an earthquake and its threat circle of cities
	 * Or if a city is clicked, it will display all the earthquakes 
	 * where the city is in the threat circle
	 */
	// 这个event handler是当鼠标按下再松手后，才会响应事件。
	@Override
	public void mouseClicked()
	{
		// Hint: You probably want a helper method or two to keep this code
		// from getting too long/disorganized

		// if lastClick is not null, this click “de-selects” whichever marker was clicked on last and 
		// so all markers should be displayed. 
		
		if( lastClicked != null ){
			lastClicked.setClicked(false);
			lastClicked = null;
			unhideMarkers();
			return;
		}
		// hideMarkers();
		selectMarkerIfClicked(cityMarkers);
		selectMarkerIfClicked(quakeMarkers);
		// 鼠标click了一个CityMarker或者EarthQuakeMarker
		
		if( lastClicked != null ){
			/*
				When an earthquake’s marker is selected, all cities within the threat circle of this earthquake are 
				displayed on the map and all other cities and earthquakes are hidden. You are given an implementation 
				of the threatCircle() method in the EarthquakeMarker class.
			 */
			if( lastClicked instanceof OceanQuakeMarker || lastClicked instanceof LandQuakeMarker ){
				// System.out.println("earthquaker marker ");
				showThreatendCities(lastClicked, cityMarkers);
			}
			/*
			When a city’s marker is selected, all earthquakes which contain that city in their threat circle are 
			displayed on the map and all other cities and earthquakes are hidden. You are given an implementation 
			of the threatCircle() method in the EarthquakeMarker class.
			 */
			if( lastClicked instanceof CityMarker ){
				//System.out.println("city marker ");
				showThreatEarthquakes(lastClicked, quakeMarkers);
			}	
		}
		
	}
	
	private void showThreatEarthquakes(CommonMarker city, List<Marker> quakeMarkers){
		
		for(int i = 0; i < quakeMarkers.size(); i++){
			Marker mk = quakeMarkers.get(i);
			double threatCircle = ((EarthquakeMarker) mk).threatCircle();
			if(mk.getDistanceTo(city.getLocation()) <= threatCircle){
				mk.setHidden(false); // 单独set unhidden
			}
		}
	}
	
	private void showThreatendCities(CommonMarker earthquake, List<Marker> cityMarkers){
		double threatCircle = ((EarthquakeMarker) earthquake).threatCircle();
		for(int i = 0; i < cityMarkers.size(); i++){
			Marker mk = cityMarkers.get(i);
			if(mk.getDistanceTo(earthquake.getLocation()) <= threatCircle){
				mk.setHidden(false); // 单独set unhidden
			}
		}	
	}
	
	private void selectMarkerIfClicked(List<Marker> markers){
		if( lastClicked != null ){
			return;
		}
		for(int i = 0; i < markers.size(); i++){
			Marker mk = markers.get(i);
			if( mk.isInside(map, mouseX, mouseY) ){
				lastClicked = (CommonMarker)mk;
				lastClicked.setClicked(true);
				// 先hidden all marker, 然后再把需要unhidden的单独set
				hideMarkers();
				lastClicked.setHidden(false); // 单独set unhidden
				break;
			}
		}
	}
	
	// loop over and hide all markers
	private void hideMarkers(){
		for(Marker marker : quakeMarkers) {
			marker.setHidden(true);
		}
			
		for(Marker marker : cityMarkers) {
			marker.setHidden(true);
		}		
	}
	
	// loop over and unhide all markers
	private void unhideMarkers() {
		for(Marker marker : quakeMarkers) {
			marker.setHidden(false);
		}
			
		for(Marker marker : cityMarkers) {
			marker.setHidden(false);
		}
	}
	
	// helper method to draw key in GUI
	private void addKey() {	
		// Remember you can use Processing's graphics methods here
		fill(255, 250, 240);
		
		int xbase = 25;
		int ybase = 50;
		
		rect(xbase, ybase, 150, 250);
		
		fill(0);
		textAlign(LEFT, CENTER);
		textSize(12);
		text("Earthquake Key", xbase+25, ybase+25);
		
		fill(150, 30, 30);
		int tri_xbase = xbase + 35;
		int tri_ybase = ybase + 50;
		triangle(tri_xbase, tri_ybase-CityMarker.TRI_SIZE, tri_xbase-CityMarker.TRI_SIZE, 
				tri_ybase+CityMarker.TRI_SIZE, tri_xbase+CityMarker.TRI_SIZE, 
				tri_ybase+CityMarker.TRI_SIZE);

		fill(0, 0, 0);
		textAlign(LEFT, CENTER);
		text("City Marker", tri_xbase + 15, tri_ybase);
		
		text("Land Quake", xbase+50, ybase+70);
		text("Ocean Quake", xbase+50, ybase+90);
		text("Size ~ Magnitude", xbase+25, ybase+110);
		
		fill(255, 255, 255);
		ellipse(xbase+35, 
				ybase+70, 
				10, 
				10);
		rect(xbase+35-5, ybase+90-5, 10, 10);
		
		fill(color(255, 255, 0));
		ellipse(xbase+35, ybase+140, 12, 12);
		fill(color(0, 0, 255));
		ellipse(xbase+35, ybase+160, 12, 12);
		fill(color(255, 0, 0));
		ellipse(xbase+35, ybase+180, 12, 12);
		
		textAlign(LEFT, CENTER);
		fill(0, 0, 0);
		text("Shallow", xbase+50, ybase+140);
		text("Intermediate", xbase+50, ybase+160);
		text("Deep", xbase+50, ybase+180);

		text("Past hour", xbase+50, ybase+200);
		
		fill(255, 255, 255);
		int centerx = xbase+35;
		int centery = ybase+200;
		ellipse(centerx, centery, 12, 12);

		strokeWeight(2);
		line(centerx-8, centery-8, centerx+8, centery+8);
		line(centerx-8, centery+8, centerx+8, centery-8);
			
	}

	// Checks whether this quake occurred on land.  If it did, it sets the 
	// "country" property of its PointFeature to the country where it occurred
	// and returns true.  Notice that the helper method isInCountry will
	// set this "country" property already.  Otherwise it returns false.	
	private boolean isLand(PointFeature earthquake) {
		// IMPLEMENT THIS: loop over all countries to check if location is in any of them
		// If it is, add 1 to the entry in countryQuakes corresponding to this country.
		for (Marker country : countryMarkers) {
			if (isInCountry(earthquake, country)) {
				return true;
			}
		}
		// not inside any country
		return false;
	}
	
	// prints countries with number of earthquakes
	private void printQuakes() {
		int totalWaterQuakes = quakeMarkers.size();
		for (Marker country : countryMarkers) {
			String countryName = country.getStringProperty("name");
			int numQuakes = 0;
			for (Marker marker : quakeMarkers)
			{
				EarthquakeMarker eqMarker = (EarthquakeMarker)marker;
				if (eqMarker.isOnLand()) {
					if (countryName.equals(eqMarker.getStringProperty("country"))) {
						numQuakes++;
					}
				}
			}
			if (numQuakes > 0) {
				totalWaterQuakes -= numQuakes;
				System.out.println(countryName + ": " + numQuakes);
			}
		}
		System.out.println("OCEAN QUAKES: " + totalWaterQuakes);
	}
	
	// helper method to test whether a given earthquake is in a given country
	// This will also add the country property to the properties of the earthquake feature if 
	// it's in one of the countries.
	// You should not have to modify this code
	private boolean isInCountry(PointFeature earthquake, Marker country) {
		// getting location of feature
		Location checkLoc = earthquake.getLocation();

		// some countries represented it as MultiMarker
		// looping over SimplePolygonMarkers which make them up to use isInsideByLoc
		if(country.getClass() == MultiMarker.class) {
			// looping over markers making up MultiMarker
			for(Marker marker : ((MultiMarker)country).getMarkers()) {
				// checking if inside
				if(((AbstractShapeMarker)marker).isInsideByLocation(checkLoc)) {
					earthquake.addProperty("country", country.getProperty("name"));				
					// return if is inside one
					return true;
				}
			}
		}
		// check if inside country represented by SimplePolygonMarker
		else if(((AbstractShapeMarker)country).isInsideByLocation(checkLoc)) {
			earthquake.addProperty("country", country.getProperty("name"));
			return true;
		}
		return false;
	}

}
