package module4;

import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import processing.core.PGraphics;

/** Implements a visual marker for cities on an earthquake map
 * 
 * @author UC San Diego Intermediate Software Development MOOC team
 * @author Your name here
 *
 */
public class CityMarker extends SimplePointMarker {
	
	// The size of the triangle marker
	// It's a good idea to use this variable in your draw method
	public static final int TRI_SIZE = 5;  
	
	public CityMarker(Location location) {
		super(location);
	}
	
	// System.out.println(city.getClass()), 输出的都是PointFeature
	public CityMarker(Feature city) {
		super(((PointFeature)city).getLocation(), city.getProperties());
	}
	
	
	// HINT: pg is the graphics object on which you call the graphics
	// methods.  e.g. pg.fill(255, 0, 0) will set the color to red
	// x and y are the center of the object to draw. 
	// They will be used to calculate the coordinates to pass
	// into any shape drawing methods.  
	// e.g. pg.rect(x, y, 10, 10) will draw a 10x10 square
	// whose upper left corner is at position x, y
	/**
	 * Implementation of method to draw marker on the map.
	 */
	
	/*
	draw

	public abstract void draw(processing.core.PGraphics pg,
                          	float x,
                          	float y)
	Draws a visual representation of this marker. The given x,y coordinates are already converted into the local 
	coordinate system, so no need for further conversion. That is, a position of (0, 0) is the origin of this marker. 
	Subclasses must override this method to draw a marker.
	
	Parameters:
	pg - The PGraphics to draw on
	x - The x position in outer object coordinates.
	y - The y position in outer object coordinates.
	 */
	// 这里draw方法实际上是去overriding父类的该方法，因此这里实际上是一种多态polymorphism
	public void draw(PGraphics pg, float x, float y) {
		
		// Save previous drawing style
		pg.pushStyle();
		
		// TODO: Add code to draw a triangle to represent the CityMarker
		
		// https://processing.org/reference/PGraphics.html     PGraphics继承自PImage类
		// x,y coordinates are already converted into the local coordinate system, 所以我们可以直接在local坐标系下计算坐标。
		//pg.triangle(x1, y1, x2, y2, x3, y3);
		pg.fill(150, 30, 30);
		pg.triangle(x, y-TRI_SIZE, x-TRI_SIZE, y+TRI_SIZE, x+TRI_SIZE, y+TRI_SIZE);
		// Restore previous drawing style
		pg.popStyle();
	}
	
	/* Local getters for some city properties.  You might not need these 
	 * in module 4. 	 */
	public String getCity()
	{
		return getStringProperty("name");
	}
	
	public String getCountry()
	{
		return getStringProperty("country");
	}
	
	public float getPopulation()
	{
		return Float.parseFloat(getStringProperty("population"));
	}
	
}
