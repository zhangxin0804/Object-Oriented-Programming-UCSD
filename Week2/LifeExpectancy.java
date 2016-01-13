package demos;

import processing.core.PApplet;
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.utils.MapUtils;
import de.fhpotsdam.unfolding.providers.*;


import java.util.List;
import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.GeoJSONReader;
import java.util.*;
import de.fhpotsdam.unfolding.marker.Marker;

/**
 * Visualizes life expectancy in different countries. 
 * 
 * It loads the country shapes from a GeoJSON file via a data reader, and loads the population density values from
 * another CSV file (provided by the World Bank). The data value is encoded to transparency via a simplistic linear
 * mapping.
 */

/*
 * 1. Set up Map
 * 2. Read data from each country
 * 3. Display data for each country
 */
public class LifeExpectancy extends PApplet {

	private UnfoldingMap map;
	private HashMap<String, Float> lifeExpMap;
	private List<Feature> countries;
	private List<Marker> countryMarkers;

	public void setup() {
		
		size(800, 600, OPENGL);
		map = new UnfoldingMap(this, 50, 50, 700, 500, new Google.GoogleMapProvider());
		// Initializes default events, i.e. all given maps handle mouse and keyboard interactions. 
		MapUtils.createDefaultEventDispatcher(this, map);

		// Load lifeExpectancy data, key->country, value->life expectancy value
		lifeExpMap = loadLifeExpectancyFromCSV("LifeExpectancyWorldBankModule3.csv");
				
		// Load country polygons and adds them as markers
		
		countries = GeoJSONReader.loadData(this, "test.json");
		// countries = GeoJSONReader.loadData(this, "countries.geo.json");
		countryMarkers = MapUtils.createSimpleMarkers(countries);
		
		for(int i = 0; i < countries.size(); i++){
			Feature f = countries.get(i);
			System.out.println(f.getProperties());
		}
		
		map.addMarkers(countryMarkers);
		
		// Country markers are shaded according to life expectancy (only once)
		shadeCountries();
	}

	public void draw() {
		// Draw map tiles and country markers
		map.draw();
	}
	
	//Helper method to color each country based on life expectancy
	//Red-orange indicates low (near 40)
	//Blue indicates high (near 100)
	private void shadeCountries() {
		for (Marker marker : countryMarkers) {
			// Find data for country of the current marker
			String countryId = marker.getId();
			
			if (lifeExpMap.containsKey(countryId)) {
				float lifeExp = lifeExpMap.get(countryId);
				// Encode value as brightness (values range: 40-90)
				// 注意下面这个map函数，是Processing Library中的一个函数，不是Unfolding Map中，具体可以看PApplet的doc.
				int colorLevel = (int) map(lifeExp, 40, 90, 10, 255);
				marker.setColor(color(255-colorLevel, 100, colorLevel));
			}
			else {
				marker.setColor(color(150,150,150));
			}
		}
	}

	//Helper method to load life expectancy data from file
	private HashMap<String, Float> loadLifeExpectancyFromCSV(String fileName) {

		HashMap<String, Float> lifeExpMap = new HashMap<String, Float>();
		// 查看API, http://processing.github.io/processing-javadocs/core/
		String[] rows = loadStrings(fileName);
		
		for (String row : rows) {
			// Reads country name and population density value from CSV row
			// NOTE: Splitting on just a comma is not a great idea here, because
			// the csv file might have commas in their entries, as this one does.  
			// We do a smarter thing in ParseFeed, but for simplicity, 
			// we just use a comma here, and ignore the fact that the first field is split.
			
			String[] columns = row.split(",");	
			// 注意查看输入文件数据格式 LifeExpectancyWorldBankModule3.csv
			if (columns.length == 6 && !columns[5].equals("..")) {
				// key->country code, value->life density value
				lifeExpMap.put(columns[4], Float.parseFloat(columns[5]));
			}
		}

		return lifeExpMap;
	}

}
