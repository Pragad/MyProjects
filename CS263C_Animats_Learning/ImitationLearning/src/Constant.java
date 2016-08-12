import java.util.HashMap;
import java.util.Map;


public class Constant {
	
	public static final int NORTH = 8;
	public static final int EAST = 6;
	public static final int WEST = 4;
	public static final int SOUTH = 2;
	public static final int NORTHEAST = 9;
	public static final int NORTHWEST = 7;
	public static final int SOUTHWEST = 1;
	public static final int SOUTHEAST = 3;
	public static final int HUNTRANGE = 150;
	public static final int IMAGERANGE = 10; 
	public static final int BUFFALO = 1;
	public static final int GOAT = 2;
	public static final int CUB = 3;
	public static final int LION = 4;
	public static final int MOTHER = 5;
	public static final int SPEED = 0;
	public static final int smallSize = 5;
	public static final int mediumSize = 10;
	public static final int largeSize = 15;
	public static final int BUFFALO_ENERGY = 300;
	public static final int GOAT_ENERGY = 150;
	public static final int STEP = 5;
	
	public static final HashMap<Integer, String> NAMES_AND_TYPES = 
	new HashMap<Integer, String>(){
		{
		put(1, "Buffalo");
		put(2, "Goat");
		put(3, "Cub");
		put(4, "Lion");
		put(5, "Mother");
		}
	};
	
	public String getAnimatTypeString(int type){
		if(type == 2) return "Goat";
		else if(type == 1) return "Buffalo";
		else if(type == 3) return "Cub";
		else if(type == 4) return "Lion";
		return null;
	}

}
