package prag.com.FileOpAndroid;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

//DataStructure for holding the Values of Pathloss file
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


class PathLossValues
{
	public double Tx;
	public double Ty;
	public double Rx;
	public double Ry;
	public double dbVal;	
}

public class FileOpAndroidActivity extends Activity {
    /** Called when the activity is first created. */
    @SuppressWarnings("rawtypes")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        long startTime = System.nanoTime();
//Declarations
    	File fPLFile = new File("/sdcard/Sample.txt");
    	File fMyVal = new File("/sdcard/myRecorded_DB_Values.txt");
 //   	File fOutFile = new File("/sdcard/Prag.txt");
 //   	File fOutFile2 = new File("/sdcard/Prag2.txt");
 //   	File fHashFile = new File("/sdcard/Prag.txt");
    	//File fin=new File(Environment.getExternalStorageDirectory() +"Sample.txt");
    	
		ArrayList<PathLossValues> alValues = new ArrayList<PathLossValues>();
		ArrayList<Double> alMyValues = new ArrayList<Double>();
		String line2, line;
		int iSizeOfDbVal;
    	double dMyDbValue,eDouble;
    	
    	
    	ArrayList<Double> list2 = new ArrayList<Double>(); // to find duplicate elements in the DbValues array list
  	    HashSet hs = new HashSet();				// to find duplicate elements in the DbValues array list
  	    ArrayList<Double> list;
  	    Hashtable<Val,ArrayList<Double>> numbers = new Hashtable<Val,ArrayList<Double>>();
    	
  	    
        try 
        {
        	FileReader frPLFile = new FileReader(fPLFile);
        	FileReader frMyVal = new FileReader(fMyVal);
  //      	FileWriter fwOutFile = new FileWriter(fOutFile);
  //      	FileWriter fwOutFile2 = new FileWriter(fOutFile2);
 //       	FileReader  frHashVal = new FileReader(fHashFile);
  		  	
            BufferedReader brPLFile = new BufferedReader(frPLFile);
    		BufferedReader brMyVal = new BufferedReader(frMyVal);
//    		BufferedWriter bwOutFile = new BufferedWriter(fwOutFile);
//    		BufferedWriter bwOutFile2 = new BufferedWriter(fwOutFile2);
//    		BufferedReader brHashFile = new BufferedReader(frHashVal);
    		
    		while((line = brPLFile.readLine()) != null)
  		    {
  			  if(!(line.isEmpty()))
  			  {
  				  String[] temp;
  				  temp = line.split(" ");		 
  				  eDouble = Double.parseDouble(temp[4].toString());
  				  if(eDouble < 95.0 && eDouble > 45.0)
  				  {
  					  PathLossValues objAllValues = new PathLossValues();
  					  objAllValues.Rx 		= Double.parseDouble(temp[0].toString());
  					  objAllValues.Ry  		= Double.parseDouble(temp[1].toString());
  					  objAllValues.Tx 		= Double.parseDouble(temp[2].toString());
  					  objAllValues.Ty 		= Double.parseDouble(temp[3].toString());
  					  objAllValues.dbVal 	= eDouble;
  					  
  					  alValues.add(objAllValues);
  				  }
  			  }
  		  }
    		
 //   	  Log.v("prag","Part1");
        	
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
 // 		  Log.v("prag","Part2");

//Compare My Recorder Values with the Database values and get co-ordinates if falls with the range (-.5 to +.5) and write in a new file
  		  PathLossValues objPLtemp ;
		  double tempDbVal = 0.0;
		  double t5;
		  for(int i =0;i<alMyValues.size();i++)
		  {
	//		  t5 = (Math.abs(alMyValues.get(i)));
			  for(int j=0;j<alValues.size();j++)
			  {
				  objPLtemp = alValues.get(j);
	//			  tempDbVal = objPLtemp.dbVal;
				  
				  
				  if((Math.abs(alMyValues.get(i)) > objPLtemp.dbVal -1) && (Math.abs(alMyValues.get(i)) < objPLtemp.dbVal +1))
				  {
					  list2.add(Math.abs(alMyValues.get(i))); //a temporary list to find the number of unique values of MyDbVal (i.e. no. of friends at diff positions
	//				  System.out.println("Found - MyVal: "+t5 + "  Val: "+tempDbVal);
	//				  bwOutFile.write(objPLtemp.Rx + " " + objPLtemp.Ry + " " + objPLtemp.Tx + " " + objPLtemp.Ty + " " + objPLtemp.dbVal + " " + t5 + "\n");
				  }			  
			  }
		  }	
		  
		  Log.v("prag","Part3");
		  brMyVal.close();
		  //brPLFile.close();
//		  bwOutFile.close();
		  frMyVal.close();
		  //frPLFile.close();
//		  fwOutFile.close();
		  
		  
		 
		  
		//Finding duplicate entires in the MyDbVal		  
		  hs.addAll(list2);
		  list2.clear();
		  list2.addAll(hs);
		  iSizeOfDbVal = list2.size();
		  String[] temp;
		  //Log.v("prgas","Part3: brPLFile:" + brPLFile.readLine());
		  while((line = brHashFile.readLine()) != null)
			{
				if(!(line.isEmpty()))
				{								
					temp = line.split(" ");		 
					eDouble = Double.parseDouble(temp[5].toString());
					Val key = new Val(Double.parseDouble(temp[0].toString()) ,Double.parseDouble(temp[1].toString()) );
										
					if(!(numbers.containsKey(key)))
					{
						list = new ArrayList<Double>();
					    numbers.put(key, list);
					}
					else
					{
						list = numbers.get(key);
					}
					list.add(eDouble); 
				 }
			}
		  	Log.v("prag","Part4");
		  	Log.v("prag","iSizeOfDbVal: "+iSizeOfDbVal);
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
		        	bwOutFile2.write("Key: \""+ob.x+":"+ob.y+"\" has value of: \"" +aValue.toString()+"\""+"\n");
		        	Log.v("prag","Key: \""+ob.x+":"+ob.y+"\" has value of: \"" +aValue.toString()+"\"");
		        }
		    }
			brHashFile.close();
			frHashVal.close();
			bwOutFile2.close();
			Log.v("prag","Part5");
			long totalTime = System.nanoTime()-startTime;
			  Log.v("prag","Done File Operations - Time: "+totalTime);
        	//String line2 = count;
 /*       	
        //Write to a text file
            
        	//BufferedWriter out = new BufferedWriter(new FileWriter(f1,true));
            out.write("Pragadheesh1" + line+ "\n");
            Log.v("prag","3");
            
            out.close();
 */           } catch (Exception e) 
              {
            	Log.v("prag","catch"+e);
            	//e.printStackTrace();}
              }
    }
}