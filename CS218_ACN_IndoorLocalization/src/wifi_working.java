package prag.cs219.wifinet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import prag.cs219.wifinet.R;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
	public int hashCode() 
	{
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


public class WifinetActivity extends Activity implements OnClickListener 
{
	public void MatchingDbValues()
	{
		//Copied from FileOpAndroidActivity.java
    	//Declarations
    	File fPLFile = new File("/sdcard/Sample.txt");

		ArrayList<PathLossValues> alValues = new ArrayList<PathLossValues>();
		ArrayList<Double> alMyValues = new ArrayList<Double>();
		String line2, line;
		int iSizeOfDbVal,count = 0;
    	double dMyDbValue,eDouble;
    	
    	ArrayList<Double> list2 = new ArrayList<Double>(); // to find duplicate elements in the DbValues array list
  	    HashSet hs2 = new HashSet();				// to find duplicate elements in the DbValues array list
  	    ArrayList<Double> list;
  	    Hashtable<Val,ArrayList<Double>> numbers = new Hashtable<Val,ArrayList<Double>>();
    	
  	    for(int c = 0; c < todoItems.size();c++)
	  	{
	  		alMyValues.add((double)todoItems.get(c));
	  		
	  	}
  	  for(int i=0;i<alMyValues.size();i++)
      {
      	Log.v("prag","I:"+alMyValues.get(i).toString());
      	
      }
        try 
        {
        	FileReader frPLFile = new FileReader(fPLFile);
            BufferedReader brPLFile = new BufferedReader(frPLFile);
    		
    		while((line = brPLFile.readLine()) != null)
  		    {
  			  if(!(line.isEmpty()))
  			  {
  				  count++;
  				  String[] temp;
  				  temp = line.split(" ");		 
  				  eDouble = Double.parseDouble(temp[4].toString());
  				  if(eDouble < 85.0 && eDouble > 45.0)
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
    		
    	  Log.v("prag","Part1b");
        	
//Read My recorded Signal Strength values from another file and put in a List, so that both values can be compared 
//  		  while((line2 = brMyVal.readLine())!=null)
//  		  {
//  			  if(!(line2.isEmpty()))
//  			  {
//  				  dMyDbValue = Double.parseDouble(line2.toString());
//  				  alMyValues.add(dMyDbValue);
//  				  //System.out.println("Added:"+dMyDbValue);
//  			  }
//  		  }
//  		  Log.v("prag","Part2");

//Compare My Recorder Values with the Database values and get co-ordinates if falls with the range (-.5 to +.5) and write in a new file
  		  PathLossValues objPLtemp ;
		  double tempDbVal = 0.0;
		  double t5;
		  int con = 0;
		  for(int i =0;i<alMyValues.size();i++)
		  {
			  t5 = (Math.abs(alMyValues.get(i)));
			  for(int j=0;j<alValues.size();j++)
			  {
				  objPLtemp = alValues.get(j);
				  tempDbVal = objPLtemp.dbVal;
				  
				  
				  if((t5 > tempDbVal-1) && (t5 < tempDbVal+1))
				  {
					  con++;
					  list2.add(t5); //a temporary list to find the number of unique values of MyDbVal (i.e. no. of friends at diff positions
					  System.out.println("Found - MyVal: "+t5 + "  Val: "+tempDbVal);
//	    					  bwOutFile.write(objPLtemp.Rx + " " + objPLtemp.Ry + " " + objPLtemp.Tx + " " + objPLtemp.Ty + " " + objPLtemp.dbVal + " " + t5 + "\n");
				  }			  
			  }
		  }	
		  
		  Log.v("prag","Part3b");
		  
		//Finding duplicate entires in the MyDbVal		  
		  hs2.addAll(list2);
		  list2.clear();
		  list2.addAll(hs2);
		  iSizeOfDbVal = list2.size();
		  
		  
		//  while((line = brHashFile.readLine()) != null)
		  while((line = brPLFile.readLine()) != null)
			{
				if(!(line.isEmpty()))
				{				
					String[] temp;
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
		  	Log.v("prag","Part4b");
		  	Log.v("prag","iSizeOfDbVal: "+iSizeOfDbVal);
			java.util.Enumeration keys = numbers.keys();
			
			while( keys.hasMoreElements() ) 
		    {
				Object aKey = keys.nextElement();
				Val ob = (Val) aKey;
		        ArrayList aValue = numbers.get(aKey);
//		        Remove duplicate entries from the RecordedDbValues. ASSUMING all devices have different db values
		        HashSet hs3 = new HashSet();
				hs2.addAll(aValue);
				aValue.clear();
				aValue.addAll(hs3);
		        if (iSizeOfDbVal <= aValue.size())
		        {
		        	Log.v("prag","Key: \""+ob.x+":"+ob.y+"\" has value of: \"" +aValue.toString()+"\"");
		        }
		    }
		    brPLFile.close();
			Log.v("prag","Part5b");
          } catch (Exception e) 
              {
            	Log.v("prag","catch:B: "+e);
              }
	}
	
	
	TextView mainText;
	WifiManager mainWifi;
	WifiReceiver receiverWifi;
	List<ScanResult> wifiList;
	Button bt_wifi;
	StringBuilder sb = new StringBuilder();
	ArrayList<Integer> alRssi = new ArrayList<Integer>();
	final ArrayList<Integer> todoItems = new ArrayList<Integer>();
	ArrayAdapter<Integer> aa;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        
        
        aa = new ArrayAdapter<Integer>(this,
        android.R.layout.simple_list_item_1,
        todoItems);
        
        mainText = (TextView) findViewById(R.id.Tv1);
        mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        bt_wifi = (Button) findViewById(R.id.bt_wifi);
        bt_wifi.setOnClickListener(this);
    }
    
    public void onClick(View v) 
	{
    	if(!todoItems.isEmpty())
    		todoItems.clear();
		if (v.getId() == R.id.bt_wifi) 
		{
			Log.d("CS219Wifi", "onClick() wifi.startScan()");
			if(!(mainWifi.isWifiEnabled()))
			{	
				Toast.makeText(this, "Turning on Wi-Fi", Toast.LENGTH_LONG).show();
				mainWifi.setWifiEnabled(true);
			}
		}
		receiverWifi = new WifiReceiver();
        registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        mainWifi.startScan();
	}
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		menu.add(0, 0, 0, "Refresh");
		return super.onCreateOptionsMenu(menu);
	}

	public boolean onMenuItemSelected(int featureId, MenuItem item) 
	{
		mainWifi.startScan();
		mainText.setText("Starting Scan");
		return super.onMenuItemSelected(featureId, item);
	}

	protected void onPause() {
		unregisterReceiver(receiverWifi);
		super.onPause();
	}

	protected void onResume() {
		registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		super.onResume();
	}
	
	class WifiReceiver extends BroadcastReceiver 
	{
		public void onReceive(Context c, Intent intent) 
		{
			sb = new StringBuilder();
			wifiList = mainWifi.getScanResults();
	        for(int i = 0; i < wifiList.size(); i++)
	        {
	        	sb.append(new Integer(i+1).toString() + ".");
	        	sb.append("SSID: "+(wifiList.get(i)).SSID.toString());
	        	sb.append(";\tRSSI: "+(wifiList.get(i)).level);
//	        	alRssi.add((wifiList.get(i)).level);
	        	Log.v("prag","Level:"+(wifiList.get(i)).level);
	        	todoItems.add((wifiList.get(i)).level);
	        	
//		        Remove duplicate entries from the RecordedDbValues. ASSUMING all devices have different db values
		        /*HashSet hs3 = new HashSet();
				hs2.addAll(aValue);
				aValue.clear();
				aValue.addAll(hs3);*/
	        	
	        	sb.append("\n");
	        }
	        mainText.setText(sb);
	        /*for(int i=0;i<alRssi.size();i++)
	        {
	        	Log.v("prag","I:"+todoItems.get(i).toString());
	        	
	        }*/
	        aa.notifyDataSetChanged();
		}
	}
}



