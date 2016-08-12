import java.io.*;
import java.util.*;


//This class is added to do Matching where unique items need to be found for every Tx Ty values omitting Rx and Ry
class Val
{
	double x;
	double y;
	
	Val(double X, double Y)
	{
		x = X;
		y = Y;
	}
	
	//Automatically added using eclipse IDE for comparing Class Objecs in Hash Code (hash.containsKey)
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(x);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(y);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	//Automatically added using eclipse IDE for comparing Class Objecs in Hash Code (hash.containsKey)
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override	
	public boolean equals(Object obj) 
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Val other = (Val) obj;
		if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
			return false;
		if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
			return false;
		return true;
	}
}

//DataStructure for holding the Values of Pathloss file
class PathLossValues
{
	public double Tx;
	public double Ty;
	public double Rx;
	public double Ry;
	public double dbVal;	
}

class BT_FileOp
{
	static String [][] Matrix =  {
        {"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w"},
        {"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"0"},
        {"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"1",	"1",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"1",	"1",	"1",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w"},
        {"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"w",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"w",	"w",	"w"},
        {"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"w",	"w",	"w",	"w",	"1",	"w",	"1",	"w",	"w",	"1",	"w",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"w",	"w",	"w",	"w",	"w",	"1",	"1",	"1",	"w",	"w",	"w"},
        {"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"w",	"w",	"w",	"1",	"w",	"w",	"w",	"w",	"w",	"w",	"1",	"w",	"1",	"w",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"w",	"w",	"w",	"w",	"w",	"1",	"1",	"1",	"w",	"w",	"w"},
        {"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"w",	"1",	"w",	"1",	"1",	"1",	"w",	"1",	"1",	"w",	"1",	"1",	"w",	"w",	"1",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"1",	"1",	"1",	"w",	"w",	"w",	"w",	"w",	"1",	"1",	"1",	"w",	"w",	"w"},
        {"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"1",	"w",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"w",	"1",	"w",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"w",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1"},
        {"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"1",	"1",	"1",	"1",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"w",	"w",	"w",	"w",	"w"},
        {"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w"},
        {"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"1",	"1",	"1",	"1",	"w",	"1",	"w",	"w",	"w",	"w",	"w",	"1",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"w",	"w",	"w",	"w",	"1",	"w",	"1",	"w",	"w",	"w",	"1",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"1",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"1",	"1",	"w",	"1",	"1",	"1",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w"},
        {"0",	"0",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1"},
        {"0",	"0",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"1",	"1",	"1",	"1",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"1",	"1",	"1",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"0",	"0"},
        {"0",	"0",	"w",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"w",	"1",	"1",	"1",	"1",	"w",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"w",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"0",	"0"},
        {"0",	"0",	"w",	"w",	"w",	"w",	"w",	"w",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"1",	"1",	"1",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"0",	"0"},
        {"0",	"0",	"w",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"w",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"w",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"w",	"1",	"1",	"1",	"1",	"w",	"w",	"1",	"w",	"1",	"1",	"1",	"w",	"1",	"w",	"1",	"w",	"1",	"1",	"1",	"w",	"0",	"0"},
        {"0",	"0",	"w",	"w",	"w",	"w",	"w",	"w",	"1",	"w",	"w",	"1",	"1",	"1",	"1",	"w",	"1",	"w",	"1",	"w",	"w",	"w",	"w",	"w",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"w",	"w",	"w",	"w",	"w",	"w",	"1",	"1",	"w",	"1",	"1",	"1",	"w",	"1",	"w",	"1",	"w",	"w",	"w",	"w",	"w",	"0",	"0"},
        {"0",	"0",	"w",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"w",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"w",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"0",	"0"},
        {"0",	"0",	"w",	"w",	"w",	"w",	"w",	"w",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"w",	"w",	"w",	"w",	"w",	"w",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"w",	"w",	"w",	"w",	"w",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"w",	"w",	"w",	"w",	"0",	"0"},
        {"0",	"0",	"w",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"w",	"0",	"0"},
        {"0",	"0",	"w",	"w",	"w",	"w",	"w",	"w",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"w",	"w",	"w",	"w",	"w",	"w",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"w",	"0",	"0"},
        {"0",	"0",	"w",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"w",	"0",	"0"},
        {"0",	"0",	"w",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"w",	"0",	"0"},
        {"0",	"0",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"1",	"1",	"1",	"1",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"1",	"1",	"1",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"0",	"0"},
        {"0",	"0",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"0",	"0",	"0",	"0",	"0",	"0",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"0",	"0"},
        {"0",	"0",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"1",	"1",	"1",	"1",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"1",	"1",	"1",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"0",	"0"},
        {"0",	"0",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"0",	"0"},
        {"0",	"0",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"1",	"1",	"1",	"1",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"1",	"1",	"1",	"w",	"w",	"w",	"w",	"w",	"0",	"0",	"0",	"0",	"0",	"0"},
        {"0",	"0",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"w",	"0",	"0",	"0",	"0",	"0",	"0"},
        {"0",	"0",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"w",	"w",	"w",	"w",	"w",	"w",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"w",	"0",	"0",	"0",	"0",	"0",	"0"},
        {"0",	"0",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"w",	"w",	"w",	"w",	"w",	"0",	"0",	"0",	"0",	"0",	"0"},
        {"0",	"0",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"1",	"1",	"1",	"1",	"w",	"w",	"w",	"w",	"1",	"w",	"w",	"1",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"w",	"w",	"w",	"1",	"w",	"w",	"w",	"w",	"w",	"1",	"w",	"1",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"0",	"0",	"0",	"0",	"0",	"0"},
        {"0",	"0",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"w",	"0",	"0",	"0",	"0",	"0",	"0"},
        {"0",	"0",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"w",	"1",	"w",	"w",	"w",	"w",	"w",	"w",	"1",	"w",	"w",	"w",	"w",	"w",	"1",	"w",	"w",	"w",	"w",	"w",	"w",	"1",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"1",	"w",	"w",	"w",	"w",	"w",	"w",	"1",	"w",	"w",	"w",	"w",	"w",	"1",	"w",	"w",	"w",	"w",	"1",	"1",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"1",	"w",	"w",	"1",	"w",	"w",	"w",	"w",	"w",	"w",	"0",	"0",	"0",	"0",	"0",	"0"},
        {"0",	"0",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"0",	"0",	"0",	"0",	"0",	"0"},
        {"0",	"0",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"0",	"0",	"0",	"0",	"0",	"0"},
        {"0",	"0",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"1",	"1",	"1",	"1",	"1",	"1",	"1",	"w",	"0",	"0",	"0",	"0",	"0",	"0"},
        {"0",	"0",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"0",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"w",	"0",	"0",	"0",	"0",	"0",	"0"}
       };
	
	@SuppressWarnings("rawtypes")
	public static void main(String[] args)
	{
		long startTime = System.nanoTime();

	  System.out.println("File Operations");
	  String line, line2;
	  double aDouble,bDouble,cDouble,dDouble,eDouble,dMyDbValue, t5, tempDbVal = 0.0;
	  PathLossValues objPLtemp ;
	  int iSizeOfDbVal, con = 0 , count = 0;
	  ArrayList<PathLossValues> alValues = new ArrayList<PathLossValues>();
	  ArrayList<Double> alMyValues = new ArrayList<Double>();
	  ArrayList<Double> list2 = new ArrayList<Double>(); // to find duplicate elements in the DbValues array list
	  HashSet hs = new HashSet();				// to find duplicate elements in the DbValues array list
	  ArrayList<Double> list;
	  Hashtable<Val,ArrayList<Double>> numbers = new Hashtable<Val,ArrayList<Double>>();
	  ArrayList<Val> alValAcc = new ArrayList<Val>();
	  ArrayList<Val> alValRange = new ArrayList<Val>();
	  try{
		  FileReader frPLFile = new FileReader("F:\\Prag\\CS_UCLA\\CS218_ACN\\Project\\MATCHING\\Pathloss_Sample.txt");
		  FileReader frMyVal = new FileReader("F:\\Prag\\CS_UCLA\\CS218_ACN\\Project\\MATCHING\\DB_Values_Sample.txt");
//		  FileWriter fwOutFile = new FileWriter("F:\\Prag\\CS_UCLA\\CS218_ACN\\Project\\MATCHING\\P_B_Sample.txt");
	//	  FileReader frMyHashVal = new FileReader("F:\\Prag\\CS_UCLA\\CS218_ACN\\Project\\MATCHING\\P_B_Sample.txt");
//		  FileWriter fwOutFileRange = new FileWriter("F:\\Prag\\CS_UCLA\\CS218_ACN\\Project\\MATCHING\\Range_Sample.txt");
		  
		  BufferedReader brPLFile = new BufferedReader(frPLFile);
		  BufferedReader brMyVal = new BufferedReader(frMyVal);
//		  BufferedReader brMyHashval = new BufferedReader(frMyHashVal);
//		  BufferedWriter bwOutFile = new BufferedWriter(fwOutFile);
//		  BufferedWriter bwOutFilerange = new BufferedWriter(fwOutFileRange);
		  
//Take values that fall with the range(<85 and >45) into a separate List and add all columns into a list
		  while((line = brPLFile.readLine()) != null)
		  {
			  if(!(line.isEmpty()))
			  {
				  count++;
				  String[] temp;
				  temp = line.split(" ");		 
				  eDouble = Double.parseDouble(temp[4].toString());
				  if(eDouble < 95.0 && eDouble > 45.0)
				  {
					  PathLossValues objAllValues = new PathLossValues();
					  objAllValues.Tx 		= Double.parseDouble(temp[0].toString());
					  objAllValues.Ty  		= Double.parseDouble(temp[1].toString());
					  objAllValues.Rx 		= Double.parseDouble(temp[2].toString());
					  objAllValues.Ry 		= Double.parseDouble(temp[3].toString());
					  objAllValues.dbVal 	= eDouble;
//					  bwOutFilerange.write(objAllValues.Rx + " " + objAllValues.Ry + " " + objAllValues.Tx + " " + objAllValues.Tx+ " " + objAllValues.dbVal + "\n");
					  alValues.add(objAllValues);
				  }
			  }
		  }
//Read My recorded Signal Strength values from another file and put in a List, so that both values can be compared 
		  while((line2 = brMyVal.readLine())!=null)
		  {
			  if(!(line2.isEmpty()))
			  {
				  dMyDbValue = Double.parseDouble(line2.toString());
				  alMyValues.add(dMyDbValue);
				  //System.out.println("Added:"+dMyDbValue);
			  }
		  }
//Write entries only those whose dbValues fall withing the range to a new file of the form Tx Ty Rx Rx GivenDb MyDb 	
		  
		  Map<Integer, Set<PathLossValues>> mapBySortVal = new LinkedHashMap<>();

		  for(PathLossValues x: alValues)
		  {
			  Set<PathLossValues> set = mapBySortVal.get(x.dbVal);
			   if (set == null)
			      mapBySortVal.put(x.sortVal, set = new LinkedHashSet<>());
			   set.add(x);

		  }
		  
		  for(int i =0;i<alMyValues.size();i++)
		  {
			  t5 = (Math.abs(alMyValues.get(i)));
			  for(int j=0;j<alValues.size();j++)
			  {
				  objPLtemp = alValues.get(j);
				  tempDbVal = objPLtemp.dbVal;
				  if((t5 > tempDbVal-1) && (t5 < tempDbVal+1))
				  {
					  Val key = new Val(objPLtemp.Tx,objPLtemp.Ty);
					  alValRange.add(key);
					  con++;
					  list2.add(t5); //a temporary list to find the number of unique values of MyDbVal (i.e. no. of friends at diff positions  
						//Val key = new Val(Double.parseDouble(temp[0].toString()) ,Double.parseDouble(temp[1].toString()) );
											
						if(!(numbers.containsKey(key)))
						{
							list = new ArrayList<Double>();
						    numbers.put(key, list);
						}
						else
						{
							list = numbers.get(key);
						}
						list.add(t5); 
	//					break;
				  }				  
			  }
		  }		  
		  brMyVal.close();
		  brPLFile.close();
//		  bwOutFile.close();
//		  bwOutFilerange.close();
		  frMyVal.close();
		  frPLFile.close();
//		  fwOutFile.close();
//		  fwOutFileRange.close();
		  
//Finding duplicate entires in the MyDbVal		  
		  hs.addAll(list2);
		  list2.clear();
		  list2.addAll(hs);
		  iSizeOfDbVal = list2.size();
		  
		  
//		  while((line = brMyHashval.readLine()) != null)
//			{
//				if(!(line.isEmpty()))
//				{				
//					String[] temp;
//					temp = line.split(" ");		 
//					eDouble = Double.parseDouble(temp[5].toString());
//
//					Val key = new Val(Double.parseDouble(temp[0].toString()) ,Double.parseDouble(temp[1].toString()) );
//										
//					if(!(numbers.containsKey(key)))
//					{
//						list = new ArrayList<Double>();
//					    numbers.put(key, list);
//					}
//					else
//					{
//						list = numbers.get(key);
//					}
//					list.add(eDouble); 
//				 }
//			}
						
			java.util.Enumeration keys = numbers.keys();
			
			while( keys.hasMoreElements() ) 
		    {
				Object aKey = keys.nextElement();
				Val ob = (Val) aKey;
		        ArrayList aValue = numbers.get(aKey);
		       
//		        Remove duplicate entries from the RecordedDbValues. ASSUMING all devices have different db values
		        HashSet hs2 = new HashSet();
				hs2.addAll(aValue);
				aValue.clear();
				aValue.addAll(hs2);

		        if (iSizeOfDbVal <= aValue.size())
		        {
		        	System.out.println("Key: \""+Math.round(ob.x*0.5286)+":"+ob.y+"\" has value of: \"" +aValue.toString()+"\"");
		        }
		    }
			
//			ArrayList<>
//			for(int k =0; k<alValues.size();k++)
//			{
//				bwOutFileTemp.write(alValues.get(k));
//			}
			String str = alValues.toString();
			//while(alValues.)
			
//			brMyHashval.close();
//			frMyHashVal.close();
			
		  long totalTime = System.nanoTime()-startTime;
		  System.out.println("Done File Operations: Con: "+con +"Time: "+totalTime);
	  		}catch (Exception e){//Catch exception if any
		  System.out.println("Error: "+e.getMessage() );
		  
		}
	}
}