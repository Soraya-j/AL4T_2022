package trafficsim;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TrafficLight implements ActionListener{
	
	private Image layoutImg;	//the layout image of the traffic light which would have transparent hole for light
	final AffineTransform trans;
	final ImageObserver imObs;
	final int id;
	private int timer = 0;
	final Color[] currentLightColor;
	//Individual light positions
	private Vector2 left_light_pos;
	private Vector2 right_light_pos;
	private Vector2 forward_pos;
	final int trafficTime = 5;
	final int orientation; // 0 -Horizontal, 1-Vertical
	public Vector2 getLeft_light_pos() {
		return left_light_pos;
	}


	public void setLeft_light_pos(Vector2 left_light_pos) {
		this.left_light_pos = left_light_pos;
	}


	public Vector2 getRight_light_pos() {
		return right_light_pos;
	}


	public void setRight_light_pos(Vector2 right_light_pos) {
		this.right_light_pos = right_light_pos;
	}


	public Vector2 getForward_pos() {
		return forward_pos;
	}


	public void setForward_pos(Vector2 forward_pos) {
		this.forward_pos = forward_pos;
	}


	public boolean leftGo, forwardGo, rightGo;
	/*Constant state
	 * Red = 0
	 * Yellow = 1
	 * Green = 2
	 * */

	private static final Logger logger = Logger.getLogger(TrafficLight.class.getName());
	public TrafficLight(InputStream imgSrc, int x, int y, int angle, int orient, int id, ImageObserver iObs){
		Timer tm;
		Vector2 position;
		imObs = iObs;
		trans = new AffineTransform();
		orientation = orient;
		leftGo = false;
		forwardGo = false;
		rightGo = false;
		tm = new Timer(1, this);
		this.id = id;
		currentLightColor = new Color[3]; //index 0-Left, 1-forward, 2-right
		currentLightColor[0] = Color.red; currentLightColor[1] = Color.red; currentLightColor[2] = Color.red;
		
		try {
			layoutImg = ImageIO.read(imgSrc);
			tm = new Timer(1, this);
			position = new Vector2(x,y);
			trans.setToTranslation(position.x, position.y);
			trans.rotate(Math.toRadians(angle), (double) layoutImg.getWidth(imObs) /2, (double) layoutImg.getHeight(imObs) /2);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.log(Level.SEVERE, "Failed to load images", e);
		}
		
		tm.start();
	}

	
	public int getOrientation() {
		return orientation;
	}


	public AffineTransform getTrans() {
		return trans;
	}

	public Image getLayoutImg() {
		return layoutImg;
	}

	public Color[] getCurrentLightColor() {
		return currentLightColor;
	}


	
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		timer++;
		if(id == 1){
			if(timer >550 && timer < (trafficTime*1000)-700){
				leftGo = true;
				forwardGo = true;
				rightGo=false;
			}
			/* if(timer > 1000 && timer < ){
				
			}*/
			if(timer < trafficTime*1000 && timer >(trafficTime*1000)-499){
				leftGo = false;
				forwardGo = false;
				currentLightColor[0] = Color.yellow;
				currentLightColor[1] = Color.yellow;
			}
			if(timer > trafficTime*1000){
				currentLightColor[0] = Color.red;
				currentLightColor[1] = Color.red;
			}
			if(timer > (trafficTime+4)*1000){
				timer = 0;
			}
		}
		
		if(id == 2){
			if(timer > trafficTime*1000 && timer < ((trafficTime+4)*1000)-500){
				forwardGo = true;
				rightGo = true;
				leftGo = false;
			}
			
			if(timer < (trafficTime+4)*1000 && timer > ((trafficTime+4)*1000)-499){
				forwardGo = false;
				rightGo = false;
				
				currentLightColor[2] = Color.yellow;
				currentLightColor[1] = Color.yellow;
			}
			
			if(timer > (trafficTime+4)*1000){
				currentLightColor[2] = Color.red;
				currentLightColor[1] = Color.red;
			}
			if(timer > (trafficTime+4)*1000){
				timer = 0;
			}
		}
		
		if(id == 3){
			if(timer > 550 && timer < (trafficTime*1000)-700){
				leftGo = false;
				forwardGo = true;
				rightGo=true;
			}
			if(timer < trafficTime*1000 && timer >(trafficTime*1000)-499){
				rightGo = false;
				forwardGo = false;
				currentLightColor[2] = Color.yellow;
				currentLightColor[1] = Color.yellow;
			}
			if(timer > trafficTime*1000){
				rightGo = false;
				forwardGo = false;
				currentLightColor[2] = Color.red;
				currentLightColor[1] = Color.red;
			}
			if(timer > (trafficTime+4)*1000){
				timer = 0;
			}
		}
		
		if(id == 4){
						
			if(timer > trafficTime*1000 && timer < ((trafficTime+4)*1000)-500){
				forwardGo = true;
				rightGo = false;
				leftGo = true;
			}
			
			if(timer < (trafficTime+4)*1000 && timer > ((trafficTime+4)*1000)-499){
				forwardGo = false;
				leftGo = false;
				
				currentLightColor[0] = Color.yellow;
				currentLightColor[1] = Color.yellow;
			}
			
			if(timer > (trafficTime+4)*1000){
				currentLightColor[0] = Color.red;
				currentLightColor[1] = Color.red;
				timer =0;
			}
		}
		//color index, 0-left, 1-forward, 2-right
		if(leftGo){
			currentLightColor[0] = Color.green;
		}
		
		if(forwardGo){
			currentLightColor[1] = Color.green;
		}
		
		if(rightGo){
			currentLightColor[2] = Color.green;
		}
	}
}
