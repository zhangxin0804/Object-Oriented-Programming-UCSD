package module4;

import de.fhpotsdam.unfolding.data.PointFeature;
import processing.core.PGraphics;

/** Implements a visual marker for land earthquakes on an earthquake map
 * 
 * @author UC San Diego Intermediate Software Development MOOC team
 * @author Your name here
 *
 */
public class LandQuakeMarker extends EarthquakeMarker {
	
	
	public LandQuakeMarker(PointFeature quake) {
		
		// calling EarthquakeMarker constructor
		super(quake);
		
		// setting field in earthquake marker
		isOnLand = true;
	}

	// the size of the marker varies by its magnitude (larger magnitude=larger marker). 
	// 注意land marker我们用circle表示，但是我们希望circle的size能够跟地震的magnitude有关。
	// 在EarthquakeMarker.java中，不同的magnitude的地震对应的radius也不一样，我们可以利用这个radius来draw circle.
	
	@Override
	public void drawEarthquake(PGraphics pg, float x, float y) {
		// Draw a centered circle for land quakes
		// DO NOT set the fill color here.  That will be set in the EarthquakeMarker
		// class to indicate the depth of the earthquake.
		// Simply draw a centered circle.
		
		// HINT: Notice the radius variable in the EarthquakeMarker class
		// and how it is set in the EarthquakeMarker constructor
		
		// TODO: Implement this method
		pg.ellipse(x, y, (float)(radius*1.2), (float)(radius*1.2));
	}
	

	// Get the country the earthquake is in
	public String getCountry() {
		return (String) getProperty("country");
	}



		
}