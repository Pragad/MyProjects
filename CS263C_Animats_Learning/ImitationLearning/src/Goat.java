import java.util.ArrayList;
import java.util.Iterator;

import uchicago.src.sim.util.RepastException;

public class Goat extends Animat  {
	private ArrayList<Animat> alRange = new ArrayList<Animat>();
	protected Board board;
	public Goat(double x, double y,Board board, String image, int size) throws RepastException {

		super(Constant.GOAT, x, y, image, size);
		this.board = board;

	}

	public boolean killGoats()
	{
		if(!(board.goats.isEmpty()))
		{
		board.goats.remove(this);
		NeuralModel.model.removeObject(this);	
		return true;
		}
		return false;
	}
	
	//2.Scan for animals near Goat
	public ArrayList<Animat> scanArea(int RANGE)
	{
		if(!alRange.isEmpty())
			alRange.clear();
		//Removed for now as Goats would be interested in Lions
		/*for (Iterator iter = getBuffalos().iterator(); iter.hasNext();) 
			{
				Buffalo buffalo= (Buffalo) iter.next();
				if(buffalo.getX()>=this.getX()-RANGE && buffalo.getX()<=this.getX()+ RANGE)
				{
					if(buffalo.getY()>=this.getY()-RANGE && buffalo.getY()<=this.getY()+ RANGE)
						alRange.add(buffalo);
				}
			}*/

		for (Iterator iter = board.getLions().iterator(); iter.hasNext();) 
		{
			Lion lion= (Lion) iter.next();
			if(lion.getX()>=this.getX()-RANGE && lion.getX()<=this.getX()+ RANGE)
			{
				if(lion.getY()>=this.getY()-RANGE && lion.getY()<=this.getY()+ RANGE)
					alRange.add(lion);
			}
		}
		return alRange; 
	}
}