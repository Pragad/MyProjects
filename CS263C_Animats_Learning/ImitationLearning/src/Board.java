
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
//import java.util.Random;

import uchicago.src.sim.engine.ModelManipulator;
import uchicago.src.sim.engine.SimModel;
import uchicago.src.sim.util.Random;
import uchicago.src.sim.util.RepastException;

/**
 * The space that the employees work in.  This is NOT directly related to the
 * displays, it is a container for the agents.
 * 
 * @author Jerry Vos
 * @version $Revision: 1.1 $ $Date: 2005/08/12 20:04:54 $
 */

//PRAG
class AnimatRange
{
	int Animat;
	double animatX;
	double animatY;
	int width;
	int height;
}

public class Board {
	private Lion cub;

	private Lion mother;

	private int width;

	private int height;

	//PRAG
	//Changed visibility to access in Goats class 03 11 2012
	static ArrayList<Animat> goats = new ArrayList();
	static ArrayList<Animat> buffalos = new ArrayList();
	static ArrayList<Animat> lions = new ArrayList();

	private String[] goatImage = {"goat1.gif","goat2.gif","goat3.gif"};

	public Board() {
		super();
	}

	public Board(int width, int height) {
		super();

		this.width = width;
		this.height = height;
	}

	public void createCub() throws RepastException {
		//	System.out.println("Buffalo -Width: "+lion.getWidth() +"; Height: "+lion.getHeight());
		//System.out.println("Cub createion");
		double x = Random.uniform.nextDoubleFromTo(0, width-45);
		double y = Random.uniform.nextDoubleFromTo(0, height-45);
		//03_11_2012
		cub = new Cub(x, y, this);
		lions.add(cub);
		//final Cub cub1 = new Cub(227, 358, this);
		//		moveFarther(cub1);
		//		System.out.println("Cub moved");
	}

	public void createMother() throws RepastException {
		//03_11_2012
		mother = new Mother((double) width / 2, (double) height / 2	+ height / 20, this);
		lions.add(mother);
		//mother = new Mother(200, 425, this);
		((Mother) mother).setMotherId(Cub.Lioness);

		//PRAG COMMENTED BELOW CODE ON 03_17_2012 
		//	moveFarther(mother);
	}

	/**
	 * @return Returns the height of the office.
	 */
	public int getHeight() {
		return height;
	}
	//PRAG

	public void addGoats(int numGoats) throws RepastException {

		//03_11_2012
		for (int i = 0; i < numGoats; i++) 
		{
			//double x = Random.uniform.nextDoubleFromTo(10, width-45);
			//double y = Random.uniform.nextDoubleFromTo(10, height-45);
			//int rand = Random.uniform.nextIntFromTo(0,3);
			double x = 10 + (Math.random() * ((width - 45 - 10) + 10));
			double y = 10 + (Math.random() * ((height - 45 - 10) + 10));
			java.util.Random randomGenerator = new java.util.Random();
			int rand = randomGenerator.nextInt(3);
			//String s3 ="goat1.gif";
			String s3 =goatImage[rand];
			int size = Constant.smallSize;
			// Differnt image have different sizes
			if(rand==0)
				size=Constant.mediumSize;
			else if(rand==1)
				size = Constant.largeSize;

			final Goat goat1 = new Goat(x,y, this, s3, size);
			goats.add(goat1);
		}

	}

	public void addBuffalo(int numBuffalo) throws RepastException
	{
		//	Min + (int)(Math.random() * ((Max - Min) + 1))
		for (int i = 0; i < numBuffalo; i++) 
		{
			//int num =  10 + (int)(Math.random() * ((width - 45 - 10) + 10));
			//double x = Random.uniform.nextDoubleFromTo(10, width-45);
			//double y = Random.uniform.nextDoubleFromTo(10, height-45);
			double x = 10 + (Math.random() * ((width - 45 - 10) + 10));
			double y = 10 + (Math.random() * ((height - 45 - 10) + 10));
			String s3 ="buff.jpg";
			final Buffalo buffalo1 = new Buffalo(x, y,this, s3, Constant.largeSize);
			//final Buffalo buffalo2 = new Buffalo(400, 400, "goat1.gif", Constant.largeSize);
			buffalos.add(buffalo1);
		}

		//moveAnimat(bf1, 250, Constant.NORTHWEST);

	}
	public void addGoats(Goat goat)
	{
		goats.add(goat);
	}

	//prar
	public void addLions(Lion lion)
	{
		lions.add(lion);
	}

	public void addBuffalo(Buffalo buffalo)
	{
		buffalos.add(buffalo);
	}

	public void moveAnimat(Animat objAnimat, int direction, int speed)
	{
		
		objAnimat.energy -= 10;
		double nX = 0, nY=0;
		//	System.out.println("MoveAnimat: 1");
		if(objAnimat.animatType==1 || objAnimat.animatType==2)
		{
			//		System.out.println("MoveAnimat: 1a");
			switch(direction)
			{
			case Constant.NORTH:
				if((objAnimat.getY() -Constant.STEP)>0)
				{
					nY = objAnimat.getY()-Constant.STEP -speed; 
					nX = objAnimat.getX();
				}
				break;
			case Constant.SOUTH:
				if(objAnimat.getY() <(this.height-Constant.STEP))
				{
					nY = objAnimat.getY()+Constant.STEP + speed; 
					nX = objAnimat.getX();
				}
				break;
			case Constant.EAST:
				if(objAnimat.getY() < (this.width-Constant.STEP))
				{
					nY = objAnimat.getY(); 
					nX = objAnimat.getX() + Constant.STEP + speed;
				}
				break;
			case Constant.WEST:
				if((objAnimat.getX() -Constant.STEP)>0)
				{
					nY = objAnimat.getY(); 
					nX = objAnimat.getX()-Constant.STEP - speed;
				}
				break;
			case Constant.NORTHEAST:
				if(((objAnimat.getY() -Constant.STEP)>0) && objAnimat.getY() < (this.width-Constant.STEP))
				{
					nX = objAnimat.getX() + Constant.STEP + speed;
					nY = objAnimat.getY()-Constant.STEP - speed; 
				}
				break;
			case Constant.NORTHWEST:
				if(((objAnimat.getY() -Constant.STEP)>0) && ((objAnimat.getX() -Constant.STEP)>0))
				{
					nX = objAnimat.getX()-Constant.STEP - speed;
					nY = objAnimat.getY()-Constant.STEP - speed; 
				}
				break;
			case Constant.SOUTHEAST:
				if(objAnimat.getY() <(this.height-Constant.STEP) && objAnimat.getY() < (this.width-Constant.STEP))
				{
					nX = objAnimat.getX() + Constant.STEP + speed;
					nY = objAnimat.getY()+Constant.STEP + speed; 
				}
				break;
			case Constant.SOUTHWEST:
				if(objAnimat.getY() <(this.height-Constant.STEP) && ((objAnimat.getX() -Constant.STEP)>0))
				{
					nX = objAnimat.getX()-Constant.STEP - speed;
					nY = objAnimat.getY()+Constant.STEP + speed; 
				}
				break;
			}
			objAnimat.animatX = nX;
			objAnimat.animatY = nY;
			objAnimat.setX(nX);
			objAnimat.setY(nY);
		}
		else if(objAnimat.animatType==3 || objAnimat.animatType==4 || objAnimat.animatType==5)
		{
			//			System.out.println("MoveAnimat: 1b");
					
			int LIONSTEP = 3;
			switch(direction)
			{
			case Constant.NORTH:
				if((objAnimat.getY() -Constant.STEP)>0)
				{
					nY = objAnimat.getY()-Constant.STEP-speed; 
					nX = objAnimat.getX();
				}
				break;
			case Constant.SOUTH:
				if(objAnimat.getY() <(this.height-Constant.STEP))
				{
					nY = objAnimat.getY()+Constant.STEP+speed; 
					nX = objAnimat.getX();
				}
				break;
			case Constant.EAST:
				if(objAnimat.getY() < (this.width-Constant.STEP))
				{
					nY = objAnimat.getY(); 
					nX = objAnimat.getX() + Constant.STEP+ speed;
				}
				break;
			case Constant.WEST:
				if((objAnimat.getX() -Constant.STEP)>0)
				{
					nY = objAnimat.getY(); 
					nX = objAnimat.getX()-Constant.STEP - speed;
				}
				break;
			case Constant.NORTHEAST:
				if(((objAnimat.getY() -Constant.STEP)>0) && objAnimat.getY() < (this.width-Constant.STEP))
				{
					nX = objAnimat.getX() + Constant.STEP + speed;
					nY = objAnimat.getY()-Constant.STEP - speed; 
				}
				break;
			case Constant.NORTHWEST:
				if(((objAnimat.getY() -Constant.STEP)>0) && ((objAnimat.getX() -Constant.STEP)>0))
				{
					nX = objAnimat.getX()-Constant.STEP - speed;
					nY = objAnimat.getY()-Constant.STEP - speed; 
				}
				break;
			case Constant.SOUTHEAST:
				if(objAnimat.getY() <(this.height-Constant.STEP) && objAnimat.getY() < (this.width-Constant.STEP))
				{
					nX = objAnimat.getX() + Constant.STEP + speed;
					nY = objAnimat.getY()+Constant.STEP + speed; 
				}
				break;
			case Constant.SOUTHWEST:
				if(objAnimat.getY() <(this.height-Constant.STEP) && ((objAnimat.getX() -Constant.STEP)>0))
				{
					nX = objAnimat.getX()-Constant.STEP - speed;
					nY = objAnimat.getY()+Constant.STEP + speed; 
				}
				break;
			}
			objAnimat.animatX = nX;
			objAnimat.animatY = nY;
			objAnimat.setX(nX);
			objAnimat.setY(nY);
			//To kill an animat if it lies close enough
			//03_11_2012
			ArrayList<Animat> alHunt = ((Lion)(objAnimat)).scanArea(Constant.IMAGERANGE);
			if(!(alHunt.isEmpty()))
			{
				for(Animat objAni: alHunt)
				{
					if(objAni.animatType == 1)
					{
						((Buffalo)objAni).killBuffalo();
						objAnimat.energy += Constant.BUFFALO_ENERGY;
					}
					else if(objAni.animatType == 2)
					{
						((Goat)objAni).killGoats();
						objAnimat.energy += Constant.GOAT_ENERGY;
					}
					/*else if (objAni.animatType == 3 || objAni.animatType == 4 || objAni.animatType == 5)
						((Lion)objAni).killLion();*/

				}
			}
			//Kill Lion if its energy < 20
			System.out.println("Lion Energy: "+objAnimat.energy);
			if(objAnimat.energy < 20)
				((Lion)objAnimat).killLion();	
		}
	}


	//Move an animat to a specific target position
	/*public void moveAnimat(Animat objAnimat, int x, int y)
	{
		objAnimat.energy -=10;
		if(objAnimat.getX() > x)
		{
			objAnimat.setX(objAnimat.getX()-Constant.STEP);
		}
		else if (objAnimat.getX()  < x)
		{
			objAnimat.setX(objAnimat.getX()+Constant.STEP);
		}

		if(objAnimat.getY() > y)
		{
			objAnimat.setY(objAnimat.getY()-Constant.STEP);
		}
		else if (objAnimat.getY()  < y)
		{
			objAnimat.setY(objAnimat.getY()+Constant.STEP);
		}
	}*/

	//Get Euclidien distance between two objects
	public double getDistance(Animat source, Animat dest)
	{
		double x1 = source.getPosX();
		double y1 = source.getPosY();
		double x2 = dest.getPosX();
		double y2 = dest.getPosY();
		double distance = Math.sqrt(((x2-x1)*(x2-x1)) + ((y2-y1)*(y2-y1)));
		return distance;
	}

	public ArrayList<Animat> isEnemyPresent(Animat objAnimat)
	{
		ArrayList<Animat> alEnemy = null;
		if(objAnimat.animatType == 1 )
			alEnemy = ((Buffalo)objAnimat).scanArea(Constant.HUNTRANGE);
		else if(objAnimat.animatType == 2 )
			alEnemy = ((Goat)objAnimat).scanArea(Constant.HUNTRANGE);
		else if(objAnimat.animatType == 3 || objAnimat.animatType==4 || objAnimat.animatType==5)
			alEnemy = ((Lion)objAnimat).scanArea(Constant.HUNTRANGE);
		return alEnemy;
	}

	public boolean isPreyKilled(Animat predator, Animat prey)
	{
		if((prey.animatX > predator.animatX  && prey.animatX < (predator.animatX+Constant.STEP+predator.animatImage.getWidth(null))) && (prey.animatY > (predator.animatY-predator.animatImage.getHeight(null))  && prey.animatY < (predator.animatY-predator.animatImage.getHeight(null)) ))
		{
			return true;
		}
		return false;
	}

	//Function to move in a random direction when no animals are nearby
	//Also if wall is present the animat should move in proper direction
	public int findRandomDirection(Animat objAnimat)
	{
		int[] move = {1,2,3,4,0,6,7,8,9};

		int randDir=0;
		//Generate a random direction to move
		//java.util.Random randomGenerator = new java.util.Random();
		//Check if move[random] == 0; If so call the function again
		java.util.Random randomGenerator = new java.util.Random();
		do
		{
			// Min + (int)(Math.random() * ((Max - Min) + 1))
			randDir = randomGenerator.nextInt(9);
			//randDir =  (int)(Math.random() * 8);

			//Code to move away from wall
			//(objAnimat.getX() > 10 && objAnimat.getX() < 480) && (objAnimat.getY() > 10 && objAnimat.getY() < 480)
			if(objAnimat.getX() < 25)
			{
				move[0] = 0;
				move[3] = 0;
				move[6] = 0;
			}

			if(objAnimat.getX() > 465)
			{
				move[2] = 0;
				move[5] = 0;
				move[8] = 0;				
			}
			if(objAnimat.getY() < 25)
			{
				move[6] = 0;
				move[7] = 0;
				move[8] = 0;
			}

			if(objAnimat.getY() > 465)
			{
				move[0] = 0;
				move[1] = 0;
				move[2] = 0;				
			}
			
			//If the animat cannot move in any direction i.e. move[0-9]=0, return a random direction
			/*int val=0;
			for(val =0;val<9;val++)
			{
				if(move[val]!=0)
					break;
			}*/
			
			/*if(val==9)
				return move[randDir];*/
		} while(move[randDir]==0);
		return move[randDir];		
	}

	//This is called when there is a Predator near by
	public ArrayList<Integer> findMoveDirection(Animat objAnimat, ArrayList<Animat> al)
	{
		//Move Farther from Predator
		ArrayList<Integer> direction = new ArrayList<Integer>();
		ArrayList<Integer> go = new ArrayList<Integer>();
		int[] move = {1,2,3,4,0,6,7,8,9};

		if(!direction.isEmpty())
			direction.clear();
		if(objAnimat.animatType == 1 || objAnimat.animatType ==2)
		{

			//Add the direction where the PREDATOR is present
			for(Animat animal : al)
			{
				if(objAnimat.getX() > animal.getX() && objAnimat.getY() > animal.getY())
					direction.add(Constant.NORTHWEST);
				if(objAnimat.getX() > animal.getX() && objAnimat.getY() < animal.getY())
					direction.add(Constant.SOUTHWEST);
				if(objAnimat.getX() < animal.getX() && objAnimat.getY() > animal.getY())
					direction.add(Constant.NORTHEAST);
				if(objAnimat.getX() < animal.getX() && objAnimat.getY() < animal.getY())
					direction.add(Constant.SOUTHEAST);
				if(objAnimat.getX() > animal.getX() && objAnimat.getY() == animal.getY())
					direction.add(Constant.SOUTH);
				if(objAnimat.getX() < animal.getX() && objAnimat.getY() == animal.getY())
					direction.add(Constant.NORTH);
				if(objAnimat.getX() == animal.getX() && objAnimat.getY() > animal.getY())
					direction.add(Constant.WEST);
				if(objAnimat.getX() == animal.getX() && objAnimat.getY() < animal.getY())
					direction.add(Constant.EAST);
			}
			//Remove duplicate directions of where the Prey/Predator might be located
			HashSet hs = new HashSet();
			hs.addAll(direction);
			direction.clear();
			direction.addAll(hs);
			for(Integer i: direction)
			{
				if(i == Constant.NORTH)
				{
					move[6] = 0;
					move[7] = 0;
					move[8] = 0;
				}
				if(i == Constant.SOUTH)
				{
					move[0] = 0;
					move[1] = 0;
					move[2] = 0;
				}
				if(i == Constant.EAST)
				{
					move[2] = 0;
					move[5] = 0;
					move[8] = 0;
				}
				if(i == Constant.WEST)
				{
					move[0] = 0;
					move[4] = 0;
					move[7] = 0;
				}
				if(i == Constant.NORTHEAST)
				{
					move[5] = 0;
					move[7] = 0;
					move[8] = 0;
				}
				if(i == Constant.NORTHWEST)
				{
					move[3] = 0;
					move[6] = 0;
					move[7] = 0;
				}
				if(i == Constant.SOUTHEAST)
				{
					move[1] = 0;
					move[2] = 0;
					move[5] = 0;
				}
				if(i == Constant.SOUTHWEST)
				{
					move[0] = 0;
					move[1] = 0;
					move[3] = 0;
				}
			}

			//Code to move away from wall
			//(objAnimat.getX() > 10 && objAnimat.getX() < 480) && (objAnimat.getY() > 10 && objAnimat.getY() < 480)
			if(objAnimat.getX() < 10)
			{
				move[0] = 0;
				move[3] = 0;
				move[6] = 0;
			}

			if(objAnimat.getX() > 475)
			{
				move[2] = 0;
				move[5] = 0;
				move[8] = 0;				
			}
			if(objAnimat.getY() < 10)
			{
				move[6] = 0;
				move[7] = 0;
				move[8] = 0;
			}

			if(objAnimat.getY() > 475)
			{
				move[0] = 0;
				move[1] = 0;
				move[2] = 0;				
			}
			int count = 0;
			for(int i=0;i<9;i++)
			{
				if(move[i]!=0)
				{
					count++;
					go.add(move[i]);
				}
			}
			int rand=0;
			if(count==0)
			{
				java.util.Random randomGenerator = new java.util.Random();
				rand = randomGenerator.nextInt(9);
			}
			if(go.isEmpty())
				go.add(rand);

		}
		//Move Closer to Prey
		//Choose one prey based on distance and go towards that prey
		else if(objAnimat.animatType == 3 || objAnimat.animatType ==4 || objAnimat.animatType==5)
		{
			ArrayList<Double> alDist = new ArrayList<Double>();
			//Add the direction where the PREDATOR can move
			for(Animat animal : al)
			{
				alDist.add(getDistance(objAnimat, animal));
			}
			Collections.sort(alDist);
			for(Animat animal : al)
			{	
				if(isPreyKilled(objAnimat,animal))
				{
					System.out.println("Prey Killed");

				}
				else
				{
					if(alDist.get(0).equals(getDistance(objAnimat, animal)))
					{
						if(objAnimat.getX() > animal.getX() && objAnimat.getY() > animal.getY())
							direction.add(Constant.NORTHWEST);
						if(objAnimat.getX() > animal.getX() && objAnimat.getY() < animal.getY())
							direction.add(Constant.SOUTHWEST);
						if(objAnimat.getX() < animal.getX() && objAnimat.getY() > animal.getY())
							direction.add(Constant.NORTHEAST);
						if(objAnimat.getX() < animal.getX() && objAnimat.getY() < animal.getY())
							direction.add(Constant.SOUTHEAST);
						if(objAnimat.getX() > animal.getX() && objAnimat.getY() == animal.getY())
							direction.add(Constant.WEST);
						if(objAnimat.getX() < animal.getX() && objAnimat.getY() == animal.getY())
							direction.add(Constant.EAST);
						if(objAnimat.getX() == animal.getX() && objAnimat.getY() > animal.getY())
							direction.add(Constant.NORTH);
						if(objAnimat.getX() == animal.getX() && objAnimat.getY() < animal.getY())
							direction.add(Constant.SOUTH);
					}
				}
			}
			HashSet hs = new HashSet();
			hs.addAll(direction);
			direction.clear();
			direction.addAll(hs);
			return direction;
		}
		//Remove duplicate directions of where the Prey/Predator might be located

		return go;
	}

	public void movePreys(){
		
		ArrayList<Animat> animats = new ArrayList<Animat>();
		animats.addAll(this.getGoats());
		animats.addAll(this.getBuffalos());
		//03_11_2012
		for(Animat animat:animats)
		{
			moveFarther(animat);
		}

	}

	public void moveFarther(Animat objAnimat)
	{
		if(objAnimat.animatType ==1 || objAnimat.animatType==2)
		{
			//for(int i =0;i<20;i++)
			//{


				ArrayList<Animat> al = isEnemyPresent(objAnimat);
				if(al.isEmpty())
				{
					int dir=findRandomDirection(objAnimat);
					moveAnimat(objAnimat, dir, Constant.SPEED);
				}

				else
				{
					ArrayList<Integer> direction = findMoveDirection(objAnimat,al);
					int x=0,dir=0;
					do
					{
						x = Random.uniform.nextIntFromTo(0,direction.size()-1);
						dir = direction.get(x);
					}while(dir==5);
					//		dir=1;
					if((objAnimat.getX() > 10 && objAnimat.getX() < 480) && (objAnimat.getY() > 10 && objAnimat.getY() < 480) )
					{
						moveAnimat(objAnimat, dir, Constant.SPEED+1);
					}
				}
				al.clear();
				//}
		//	}
		}
		//Move closer for lions
		else if(objAnimat.animatType==3 || objAnimat.animatType==4 || objAnimat.animatType == 5)
		{			
			ArrayList<Animat> al = isEnemyPresent(objAnimat);
			if(al.isEmpty())
			{
				int dir=findRandomDirection(objAnimat);
			//	System.out.println("Direction: "+dir);
				moveAnimat(objAnimat, dir, Constant.SPEED);
			}

			else
			{
				ArrayList<Integer> direction = findMoveDirection(objAnimat,al);
				int dir=0,x=0;
				do
				{
					x = Random.uniform.nextIntFromTo(0,direction.size()-1);
					dir = direction.get(x);
				}while(dir==5);
				moveAnimat(objAnimat, dir, Constant.SPEED+3);

			}
			al.clear();
		}
	}

	public ArrayList getGoats()
	{
		return goats;
	}

	public ArrayList getBuffalos()
	{
		return buffalos;
	}
	/**
	 * @param height The height of the office.
	 */
	public void setHeight(int height) {
		this.height = height;
	}

	/**
	 * @return Returns the width of the office.
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @param width The width of the office.
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * @return Returns the employees.
	 */
	public Lion getCub() {
		return cub;
	}

	//	/**
	//	 * @param employees Sets the list of employees.
	//	 */
	//	public void setEmployees(ArrayList employees) {
	//		this.employees = employees;
	//	}
	/**
	 * @return Returns the bosses.
	 */
	public Lion getMother() {
		return mother;
	}

	public ArrayList getLions(){
		ArrayList mergedLions = new ArrayList();
		mergedLions.add(mother);
		mergedLions.add(cub);
		return mergedLions;

	}
}
