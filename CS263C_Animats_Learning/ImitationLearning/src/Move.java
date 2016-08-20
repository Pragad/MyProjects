/*import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import uchicago.src.sim.util.Random;


public class Move {

	
	public void moveFarther(Animat objAnimat)
	{
		Board objBoard = new Board();
		if(objAnimat.animatType ==1 || objAnimat.animatType==2)
		{
		for(int i =0;i<20;i++)
		{


			ArrayList<Animat> al = new ArrayList(objBoard.isEnemyPresent(objAnimat));
			
			if(al.isEmpty())
			{
				int dir=0;
				do
				{
					dir = Random.uniform.nextIntFromTo(1,9);
				}while(dir==5);
				//		dir=7;
				if((objAnimat.getX() > 10 && objAnimat.getX() < 480) && (objAnimat.getY() > 10 && objAnimat.getY() < 480) )
				{
					objBoard.moveAnimat(objAnimat, dir, Constant.SPEED);
					try 
					{
						Thread.sleep(500);
					} catch (InterruptedException ex) {
						System.out.println("Thread Timer Exception");
					}
				}

			}

			else
			{
				ArrayList<Integer> direction = objBoard.findMoveDirection(objAnimat,al);
				int dir=0;
				do
				{
					dir = direction.get(Random.uniform.nextIntFromTo(1,direction.size())-1);
				}while(dir==5);
				//		dir=1;
				if((objAnimat.getX() > 10 && objAnimat.getX() < 480) && (objAnimat.getY() > 10 && objAnimat.getY() < 480) )
				{
					objBoard.moveAnimat(objAnimat, dir,Constant.SPEED+1);
					try {
						Thread.sleep(500);
					} catch (InterruptedException ex) {
						System.out.println("Thread Timer Exception");
					}
				}


			}
			al.clear();
		}
		}//Move closer for lions
		else if(objAnimat.animatType==3 || objAnimat.animatType==4)
		{
			for(int i =0;i<20;i++)
			{


				ArrayList<Animat> al = objBoard.isEnemyPresent(objAnimat);
				if(al.isEmpty())
				{
					int dir=0;
					do
					{
						dir = Random.uniform.nextIntFromTo(1,9);
					}while(dir==5);
					//		dir=7;
					if((objAnimat.getX() > 10 && objAnimat.getX() < 480) && (objAnimat.getY() > 10 && objAnimat.getY() < 480) )
					{
						objBoard.moveAnimat(objAnimat, dir,Constant.SPEED);
						try 
						{
							Thread.sleep(500);
						} catch (InterruptedException ex) {
							System.out.println("Thread Timer Exception");
						}
					}

				}

				else
				{
					ArrayList<Integer> direction = objBoard.findMoveDirection(objAnimat,al);
					int dir=0;
					do
					{
						dir = Random.uniform.nextIntFromTo(1,direction.size());
					}while(dir==5);
					//		dir=1;
					if((objAnimat.getX() > 10 && objAnimat.getX() < 480) && (objAnimat.getY() > 10 && objAnimat.getY() < 480) )
					{
						objBoard.moveAnimat(objAnimat, dir,Constant.SPEED+3);
						try {
							Thread.sleep(500);
						} catch (InterruptedException ex) {
							System.out.println("Thread Timer Exception");
						}
					}


				}
				al.clear();
			}
		}
	}
}
*/