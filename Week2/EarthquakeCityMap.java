package module3;

//Java utilities libraries
import java.util.ArrayList;
import java.util.HashMap;
//import java.util.Collections;
//import java.util.Comparator;
import java.util.List;


//Processing library
import processing.core.PApplet;

//Unfolding libraries
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import de.fhpotsdam.unfolding.providers.Google;
import de.fhpotsdam.unfolding.providers.MBTilesMapProvider;
import de.fhpotsdam.unfolding.utils.MapUtils;

//Parsing library
import parsing.ParseFeed;

/** EarthquakeCityMap
 * An application with an interactive map displaying earthquake data.
 * Author: UC San Diego Intermediate Software Development MOOC team
 * @author Xin Zhang - University of Southern California
 * Date: Jan 7, 2016
 * */
public class EarthquakeCityMap extends PApplet {

	// You can ignore this.  It's to keep eclipse from generating a warning.
	private static final long serialVersionUID = 1L;

	// IF YOU ARE WORKING OFFLINE, change the value of this variable to true
	private static final boolean offline = false;
	
	// Less than this threshold is a light earthquake
	public static final float THRESHOLD_MODERATE = 5;
	// Less than this threshold is a minor earthquake
	public static final float THRESHOLD_LIGHT = 4;

	/** This is where to find the local tiles, for working without an Internet connection */
	public static String mbTilesString = "blankLight-1-3.mbtiles";
	
	// The map
	private UnfoldingMap map;
	
	//feed with magnitude 2.5+ Earthquakes
	private String earthquakesURL = "http://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_week.atom";

	
	public void setup() {
		// 第三个参数是renderer model
		size(950, 600, OPENGL);

		if (offline) {
		    map = new UnfoldingMap(this, 200, 50, 700, 500, new MBTilesMapProvider(mbTilesString));
		    earthquakesURL = "2.5_week.atom"; 	// Same feed, saved Aug 7, 2015, for working offline
		}
		else {
			map = new UnfoldingMap(this, 200, 50, 700, 500, new Google.GoogleMapProvider());
			// IF YOU WANT TO TEST WITH A LOCAL FILE, uncomment the next line
			//earthquakesURL = "2.5_week.atom";
		}
		// Zooms to the given level. Map tiles will be non-scaled.
		// 相当于初始化的一个zoom level, 比如下面参数为0，意味着初始化的map就没有zoom in过，为最最初的level.
	    map.zoomToLevel(2);
	    // make map in default interactive way.
	    MapUtils.createDefaultEventDispatcher(this, map);	
			
	    // The List you will populate with new SimplePointMarkers
	    List<Marker> markers = new ArrayList<Marker>();

	    //Use provided parser to collect properties for each earthquake
	    //PointFeatures have a getLocation method
	    List<PointFeature> earthquakes = ParseFeed.parseEarthquake(this, earthquakesURL);
	    
	    // In particular, notice that the SimplePointMarker constructor takes a Location object, and the 
	    // PointFeature has a way of getting its location.
	    
	    // Do not call the draw method on the markers. That method will be called automatically for you when 
	    // you add the markers to the map.
	    
	    // SimplePointMarker representing a single location. Use directly to display as simple circle, or extend 
	    // it for custom styles.
	    for(int i = 0; i < earthquakes.size(); i++){
	    	SimplePointMarker sPointMarker = createMarker(earthquakes.get(i), earthquakes.get(i).getProperties());
	    	markers.add(sPointMarker);
	    	// System.out.println(sPointMarker.getProperties());
	    }
	    map.addMarkers(markers);
	    shadeMarkers(markers);
	}
	
	private void shadeMarkers(List<Marker> markers){
		// 注意：因为上面我们创建SimplePointerMarker的时候，不光传递了SimplePointFeature的location信息也传递了它的properties信息
		// 因此，SimplePointerMarker中也有这些properties信息，根据这些properties信息，我们来对其进行一些visual层面的处理。但是需要注意
		// 我们用的还是接口类型的list存储的这些SimplePointerMarker对象，使用的时候还需要向下cast.
		
	    // Here is an example of how to use Processing's color method to generate 
	    // an int that represents the color yellow.  
	    //int yellow = color(255, 255, 0);
		// 1. 不同震级magnitude用不同颜色的marker表示。
		// 2. 不同震级magnitude的marker的大小也不同。
		for(int i = 0; i < markers.size(); i++){
			SimplePointMarker sPointMarker = (SimplePointMarker)markers.get(i);
			float magnitude = (float)sPointMarker.getProperty("magnitude");
			if( magnitude >= THRESHOLD_MODERATE ){
				int red = color(255, 0, 0);
				sPointMarker.setColor(red);
				sPointMarker.setRadius((float) 15.0);
			}else if( magnitude < THRESHOLD_MODERATE && magnitude >= THRESHOLD_LIGHT ){
				int yellow = color(255, 255, 0);
				sPointMarker.setColor(yellow);
				sPointMarker.setRadius((float) 10.0);
			}else{
				int gray = color(100,100,100);
				sPointMarker.setColor(gray);
				sPointMarker.setRadius((float) 5.0);
			}
			
		}
	}
	
	// A suggested helper method that takes in an earthquake feature and 
	// returns a SimplePointMarker for that earthquake
	// TODO: Implement this method and call it from setUp, if it helps
	private SimplePointMarker createMarker(PointFeature feature, HashMap<String,java.lang.Object> properties)
	{
		// finish implementing and use this method, if it helps.
		return new SimplePointMarker(feature.getLocation(), properties);
	}
	
	public void draw() {
	    background(10);
	    map.draw();
	    addKey();
	}


	// helper method to draw key in GUI
	// TODO: Implement this method to draw the key
	private void addKey() 
	{	
		// Remember you can use Processing's graphics methods here
		fill(255, 255, 255);
		rect(10, 50, 150, 200, 7);
		//-----------------------//
		fill(255, 0, 0);
		ellipse(40, 120, 15, 15);
		textSize(10);
		fill(0, 0, 0);
		text("Mag >= 5.0", 65, 125);
		//-----------------------//
		fill(255, 255, 0);
		ellipse(40, 150, 10, 10);
		textSize(10);
		fill(0, 0, 0);
		text("Mag >= 4.0", 65, 155);
		//-----------------------//
		fill(100, 100, 0);
		ellipse(40, 180, 5, 5);
		textSize(10);
		fill(0, 0, 0);
		text("Mag < 4.0", 65, 185);
	}
}
