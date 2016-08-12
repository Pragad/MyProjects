import java.awt.Color;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;

import uchicago.src.sim.adaptation.neural.NeuralException;
import uchicago.src.sim.gui.OvalNetworkItem;
import uchicago.src.sim.gui.SimGraphics;
import uchicago.src.sim.network.DefaultDrawableNode;
import uchicago.src.sim.space.Object2DGrid;
import uchicago.src.sim.util.RepastException;


public class Animat extends DefaultDrawableNode{
	
	int animatType;
	double animatX;
	double animatY;
	int width;
	int height;
	Image animatImage;
	static int baseIdNumber =0;
	int size;
	int energy;
	
	//MOVE
	private int vX;
	private int vY;
	private static final Color[] colors = new Color[] {
		Color.PINK,
		Color.BLUE,
		Color.GRAY,
		Color.GREEN,
		Color.MAGENTA,
		Color.YELLOW,
		Color.WHITE,
		Color.CYAN
	};
	private static int colorIndex = 0;
	
	Animat(int animat, double animatX, double animatY, String image, int size){
		super(new OvalNetworkItem(animatX, animatY));
		this.animatType = animat;
		this.animatX = animatX;
		this.animatY = animatY;
		Image animatImage = loadAnimatPicture(image);
		this.animatImage = animatImage;
		this.width = animatImage.getWidth(null);
		this.height = animatImage.getHeight(null);
		this.size = size;
		this.energy = 1000;
		this.setColor(getNextColor());
		this.setNodeLabel(Constant.NAMES_AND_TYPES.get(animat) + ++baseIdNumber);
	}
	
	public Animat() throws RepastException {
		this(0, 0, 0, null, 0);
	}
	
	public static void resetIndices() {
		baseIdNumber = 0;
	}
	
	public int getSize(){
		return this.size;
	}
	public int getWidth(){
		return this.width;
	}
	
	public int getHeight(){
		return this.height;
	}
	public double getPosX(){
		return this.animatX;
	}
	public double getPosY(){
		return this.animatY;
	}

	public int getAnimatType(){
		return this.animatType;
	}
	
	private Image loadAnimatPicture(String image) {
		//System.out.println(image);
		java.net.URL animatPicURL = Animat.class.getResource(image);
		Image animatPicture = new ImageIcon(animatPicURL).getImage();
		return animatPicture;
	}
	
	public void draw(SimGraphics g) {
		// draw the employee's picture
		int width= this.width;
		
		g.drawImageScaled(this.animatImage);
		
	
		g.setFont(super.getFont());
		
		// get the size of the node's text
		Rectangle2D bounds = g.getStringBounds(this.getNodeLabel());
		
		// set the graphics to draw the text above the label
		// the x coordinate is relative to the upper left corner of the image
		// so the coordinates are shifted to account for that
		g.setDrawingCoordinates((float) (this.getX() + width / 2.0 - bounds.getWidth() / 2.0),
								(float) (this.getY() - bounds.getHeight() - 2),
								0f);
		
		// draw the label
		g.drawString(getNodeLabel(), Color.BLACK);
		/*Board objBoard = new Board(500,500);
		ArrayList<Animat> alGoat =  objBoard.getGoats();
		for(Animat goat:alGoat)
		{
			objBoard.moveFarther(goat);
		}
		
		ArrayList<Animat> alBuffalo =  objBoard.getBuffalos();
		for(Animat buffalo:alBuffalo)
		{
			objBoard.moveFarther(buffalo);
		}*/
		
		/*TimerTask tt2 = new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				
			}
		};
		Timer t = new Timer();
		t.scheduleAtFixedRate(tt2, 0, 60*1000);*/
		
	}
	
	private static Color getNextColor() {
		if (colorIndex == colors.length)
			colorIndex = 0;
		
		return colors[colorIndex++];
	}
 }
