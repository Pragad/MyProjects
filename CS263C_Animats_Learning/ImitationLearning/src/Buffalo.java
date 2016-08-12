import java.util.ArrayList;
import java.util.Iterator;

import uchicago.src.sim.util.RepastException;

public class Buffalo extends Animat  {
	private ArrayList<Animat> alRange = new ArrayList<Animat>();
	protected Board board;
	public Buffalo(double x, double y,Board board, String image, int size) throws RepastException {
		super(Constant.BUFFALO, x, y, image, Constant.largeSize);
		this.board = board;
	}

	public boolean killBuffalo()
	{
		if(!(board.buffalos.isEmpty()))
		{
			board.buffalos.remove(this);
			NeuralModel.model.removeObject(this);	
			return true;
		}
		return false;
	}

	//1.Scan for animals near Buffalo
	public ArrayList<Animat> scanArea(int RANGE)
	{
		if(!alRange.isEmpty())
			alRange.clear();
		//Removed for now as Buffalos would be interested in Lions
		/*for (Iterator iter = getGoats().iterator(); iter.hasNext();) 
			{
				Goat goat= (Goat) iter.next();
				if(goat.getX()>=this.getX()-RANGE && goat.getX()<=this.getX()+ RANGE)
				{
					if(goat.getY()>=this.getY()-RANGE && goat.getY()<=this.getY()+ RANGE)
						alRange.add(goat);
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