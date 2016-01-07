import processing.core.*;

public class XiaoHaPApplet1 extends PApplet {
	
	// Background Image Object Reference
	private PImage webImg;
	private int count = 0;
	private int[] rgb = new int[]{255, 255, 0};
	
	

	
	public void setup(){
		size(500, 500);
		// Load image from a web server into memory
		String url = "https://www.apdgroup.com/media/1294/ds20-thumb.jpg";
		webImg = loadImage(url, "jpg");
	}
	
	
	
	public void draw(){
		// set height fixed and automatically calculated width, then dynamically change when resize the canvas window
		webImg.resize(0, height);		
		//the last two parameters are coordinates of top left corner of image.
		image(webImg, 0, 0);
		
		if( count == 25 ){
			count = 0;
			updateColor(rgb);
		}else{
			count++;
		}
		
		fill(rgb[0],rgb[1],rgb[2]);
		ellipse(width/4, height/5, width/5, height/5);
		
	}
	
	
	private void updateColor(int[] color){
		if( color[0] == 0 && color[1] == 0 ){
			color[0] = 255;
			color[1] = 255;
		}else{
			color[0] -= 5;
			color[1] -= 5;
		}
	}
	
	// won't execute
	public static void main(String[] args) {
		System.out.println("test");
	}
	
}


