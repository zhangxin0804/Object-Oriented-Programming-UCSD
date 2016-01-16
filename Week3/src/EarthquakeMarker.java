package module4;

import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import processing.core.PGraphics;

/** Implements a visual marker for earthquakes on an earthquake map
 * 
 * @author UC San Diego Intermediate Software Development MOOC team
 * @author Xin Zhang
 *
 */
public abstract class EarthquakeMarker extends SimplePointMarker
{
	
	// Did the earthquake occur on land?  This will be set by the subclasses.
	protected boolean isOnLand;

	// The radius of the Earthquake marker
	// You will want to set this in the constructor, either
	// using the thresholds below, or a continuous function
	// based on magnitude. 
	protected float radius;
	
	
	/** Greater than or equal to this threshold is a moderate earthquake */
	public static final float THRESHOLD_MODERATE = 5;
	/** Greater than or equal to this threshold is a light earthquake */
	public static final float THRESHOLD_LIGHT = 4;

	/** Greater than or equal to this threshold is an intermediate depth */
	public static final float THRESHOLD_INTERMEDIATE = 70;
	/** Greater than or equal to this threshold is a deep depth */
	public static final float THRESHOLD_DEEP = 300;

	// ADD constants for colors

	
	// abstract method implemented in derived classes
	public abstract void drawEarthquake(PGraphics pg, float x, float y);
		
	
	// constructor
	/*
	注意，比如SimplePointMarker这些对象中也有Location信息和Properties信息，可以通过方法得到。但是这些信息不是新copy的信息。根据
	ParseFeed.java文件可知，Location对象信息和Properties对象信息，最初的创建都是随着PointFeature创建的，因此在针对PointFeature创建
	SimplePointMarker时，传递给SimplePointMarker的信息也是同一份信息而不是新copy的信息！
	*/
	public EarthquakeMarker (PointFeature feature) 
	{
		super(feature.getLocation());
		// Add a radius property and then set the properties
		java.util.HashMap<String, Object> properties = feature.getProperties();
		float magnitude = Float.parseFloat(properties.get("magnitude").toString());
		properties.put("radius", 2*magnitude );
		setProperties(properties);
		
		// 见上面成员变量 protected float radius;
		this.radius = 1.75f*getMagnitude();
	}
	

	// calls abstract method drawEarthquake and then checks age and draws X if needed
	public void draw(PGraphics pg, float x, float y) {
		// save previous styling
		pg.pushStyle();
			
		// determine color of marker from depth
		colorDetermine(pg);
		
		// call abstract method implemented in child class to draw marker shape
		drawEarthquake(pg, x, y);
		
		// OPTIONAL TODO: draw X over marker if within past day		
		
		// reset to previous styling
		pg.popStyle();
		
	}
	
	// determine color of marker from depth
	// We suggest: Deep = red, intermediate = blue, shallow = yellow
	// But this is up to you, of course.
	// You might find the getters below helpful.
	/*
	“Shallow earthquakes are between 0 and 70 km deep; intermediate earthquakes, 70 - 300 km deep; 
	and deep earthquakes, 300 - 700 km deep. In general, the term "deep-focus earthquakes" is applied to earthquakes 
	deeper than 70 km. All earthquakes deeper than 70 km are localized within great slabs of shallow lithosphere that 
	are sinking into the Earth's mantle.”
	 */
	private void colorDetermine(PGraphics pg) {
		//TODO: Implement this method
		
		float depth = getDepth();
		
		if( depth >= 0.0 && depth < 70.0 ){
			// shallow
			int yellow = pg.color(255, 255, 0);
			pg.fill(yellow);
		}else if( depth >= 70.0 && depth < 300.0 ){
			// intermediate
			int blue = pg.color(0, 0, 255);
			pg.fill(blue);
		}else if( depth >= 300.0 && depth < 700.0 ){
			// deep
			int red = pg.color(255, 0, 0);
			pg.fill(red);
		}
		
	}
	
	
	/*
	 * getters for earthquake properties
	 */
	public float getMagnitude() {
		return Float.parseFloat(getProperty("magnitude").toString());
	}
	public float getDepth() {
		return Float.parseFloat(getProperty("depth").toString());	
	}
	public String getTitle() {
		return (String) getProperty("title");		
	}
	public float getRadius() {
		return Float.parseFloat(getProperty("radius").toString());
	}
	
	public boolean isOnLand()
	{
		return isOnLand;
	}
	
}
