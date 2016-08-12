package com.example.android.Accelerometer;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import com.example.android.Accelerometer.*;
import android.widget.Toast;
import java.util.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.ArrayList;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.bluetooth.*;
import android.app.ListActivity;

class Val
{
	double x;
	double y;
	
	Val(double X, double Y)
	{
		x = X;
		y = Y;
	}
	
	
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


public class Accelerometer extends Activity implements AccelerometerListener,SensorEventListener,OnClickListener 
{
    private static Context CONTEXT;
    public static int count=0;
    public static double prevdist =0;
    public static String plot2M ="N";
    public float magnitude;
    public static int StepCount, TotalStepCount ;
    public static double distance,totaldistance;
    String flg= "down";
    private SensorManager mSensorManager;
    Sensor accelerometer;
    Sensor magnetometer;
    float azimut;
    float[] mGravity;
    float[] mGeomagnetic;
    public static String direction;
    public static String prevdirection = null;
    public static ArrayList<Integer> stepcounts = new ArrayList<Integer>(); 
    public static Hashtable<Integer, String> trace = new Hashtable<Integer, String>();
    public static HashMap<Integer,Double> stridemap = new HashMap<Integer,Double>();
    
    final ArrayList<Integer> todoItems = new ArrayList<Integer>();
    
    public ArrayAdapter<Integer> aa;  // Create the array adapter to bind the array to the listview
 		
    //removed REQUEST_ENABLE_BT
	
	public static ArrayList<Integer> bluetoothi = new ArrayList<Integer>();
	public static ArrayList<Integer> bluetoothj = new ArrayList<Integer>();
    boolean flag1 = false;
    
    Button btnStart, btnStop;                                 //2/9
    boolean startbuttonstate = true, stopbuttonstate = false; 
    
    //wifi declarations
    WifiManager mainWifi;
	WifiReceiver receiverWifi;
	List<ScanResult> wifiList;
	Button bt_wifi;
	StringBuilder sb = new StringBuilder();
	ArrayList<Integer> alRssi = new ArrayList<Integer>();
    
    private OnClickListener mStartListener = new OnClickListener() {     //2/9
  		public void onClick(View v) {
  			Log.v("Start", "Starting");
  		//	timeStampBegin = now("HHmmss");
  			startbuttonstate = false;
  			//stopbuttonstate = true;
  			try {
  				
  				IntentFilter filter = new IntentFilter(
  						"com.example.android.Accelerometer.Accelerometer");
  				registerReceiver(receiver, filter);
  				startService(new Intent(Accelerometer.this,
  						StepCountingService.class));
  				//btnStop.setEnabled(stopbuttonstate);
  				btnStart.setEnabled(startbuttonstate);
  			} catch (Exception e) {
  				e.printStackTrace();
  			
  			}
  		}

  	};
      
  	//removed reset
      
      private  BroadcastReceiver receiver = new BroadcastReceiver() {
  		@Override
  		public void onReceive(Context context, Intent intent) {
  			
  			
  			Log.i("REceived intent", "I Received intent");
  			Log.i("REceived intent", intent.getAction());
  			String temp = intent.getAction();
  			//totalStepCount = 20;
  			if (temp.compareTo("com.example.android.Accelerometer.Accelerometer") == 0) {
  				StepCount = intent.getIntExtra("stepCount", -1);
  				TotalStepCount = intent.getIntExtra("totalStepCount", -1);
  				Log.v("steps", String.valueOf(StepCount));
  				
  				Double stridelength;
  		    	
  		    	if (!stridemap.containsKey(StepCount*10))
  		         	stridelength = 0.65;
  		         	else
  		         		stridelength =	stridemap.get(StepCount*10);
  		         	
  		         		
  		    	distance = StepCount * stridelength;
  		    	totaldistance += distance;
  		    	
  		        if (totaldistance >= 2)
  		        {
  		        	totaldistance = 0;
  		        	plot2M = "Y";
  		        }
  				
  		        
  			} else {
  				Log.i("In Else ", "Inside Else" + temp);
  			}
  		}
  	};    
 
    @Override
    public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        CONTEXT = this;
        
        mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        
        Button button_start = (Button) findViewById(R.id.start);
        button_start.setOnClickListener(mStartListener);
        
        Button bt_wifi = (Button) findViewById(R.id.bt_wifi);
        bt_wifi.setOnClickListener(this);
        
        Button map_button = (Button) findViewById(R.id.mapbutton);
        map_button.setOnClickListener(this);
        
        stridemap.put(17,0.72);
        stridemap.put(18,0.72);
        stridemap.put(19,0.72);
        stridemap.put(20,0.72);
        stridemap.put(21,0.72);
        stridemap.put(22,1.23);
        stridemap.put(23,1.23);
        stridemap.put(24,1.23);
        stridemap.put(25,1.23);
        stridemap.put(26,1.23);
        stridemap.put(27,1.67);
        stridemap.put(28,1.67);
        stridemap.put(29,1.67);
        stridemap.put(30,1.67);

        //removed part
        
        aa = new ArrayAdapter<Integer>(this,android.R.layout.simple_list_item_1, todoItems);
        
        
        //removed final intent i
        
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
    }
    
    public void Matching(ArrayList<Integer> todoItems)
	{
		File fPLFile = new File("/sdcard/Sample.txt");
		File fOutFile = new File("/sdcard/Prag.txt");
    	File fOutFile2 = new File("/sdcard/Prag2.txt");
    	File fHashFile = new File("/sdcard/Prag.txt");
    	
		ArrayList<PathLossValues> alValues = new ArrayList<PathLossValues>();
		ArrayList<Double> alMyValues = new ArrayList<Double>();
		String line2, line;
		int iSizeOfDbVal,countt = 0;
    	double dMyDbValue,eDouble;
    	
    	ArrayList<Double> list2 = new ArrayList<Double>(); // to find duplicate elements in the DbValues array list
  	    HashSet hs = new HashSet();				// to find duplicate elements in the DbValues array list
  	    ArrayList<Double> list;
  	    Hashtable<Val,ArrayList<Double>> numbers = new Hashtable<Val,ArrayList<Double>>();
  	    for(int c = 0; c < todoItems.size();c++)
	  	{
	  		alMyValues.add((double)todoItems.get(c));
	  	}
  	    
        try 
        {
        	FileReader frPLFile = new FileReader(fPLFile);
        	FileWriter fwOutFile = new FileWriter(fOutFile);
        	FileWriter fwOutFile2 = new FileWriter(fOutFile2);
        	FileReader  frHashVal = new FileReader(fHashFile);
  		  	
            BufferedReader brPLFile = new BufferedReader(frPLFile);
    		BufferedWriter bwOutFile = new BufferedWriter(fwOutFile);
    		BufferedWriter bwOutFile2 = new BufferedWriter(fwOutFile2);
    		BufferedReader brHashFile = new BufferedReader(frHashVal);
    		
    		while((line = brPLFile.readLine()) != null)
  		    {
  			  if(line.length()!=0)
  			  {
  				  countt++;
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
					  bwOutFile.write(objPLtemp.Rx + " " + objPLtemp.Ry + " " + objPLtemp.Tx + " " + objPLtemp.Ty + " " + objPLtemp.dbVal + " " + t5 + "\n");
				  }			  
			  }
		  }	
		  
		 
		  bwOutFile.close();
		  fwOutFile.close();
		  
		  //Finding duplicate entires in the MyDbVal		  
		  hs.addAll(list2);
		  list2.clear();
		  list2.addAll(hs);
		  iSizeOfDbVal = list2.size();
		  
		  while((line = brHashFile.readLine()) != null)
			{
				if(line.length()!=0)
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
		  	
			java.util.Enumeration keys = numbers.keys();
			
			while( keys.hasMoreElements() ) 
		    {
				Object aKey = keys.nextElement();
				Val ob = (Val) aKey;
		        ArrayList aValue = numbers.get(aKey);
		       
		        HashSet hs2 = new HashSet();   //Remove duplicate entries from the RecordedDbValues. ASSUMING all devices have different db values
				hs2.addAll(aValue);
				aValue.clear();
				aValue.addAll(hs2);
		        
				if (iSizeOfDbVal <= aValue.size())
		        {
		        	bluetoothi.add((int)Math.floor(ob.x*0.53623));
		            bluetoothj.add((int)Math.floor(ob.y*0.988505));
		        	bwOutFile2.write("Key: \""+Math.floor(ob.x*(0.53623))+":"+Math.floor(ob.y*(0.988505))+"\" has value of: \"" +aValue.toString()+"\""+"\n");
		        	Log.v("prag","Key: \""+ob.x+"FloorX:" +Math.floor(ob.x*0.53623) + ":"+ob.y+"FloorY:" +Math.floor(ob.y*0.988505) +"\"");
		        }
		    }
			
			
			count = 0;                  //initialize count to zero once we re search bluetooth devices
            prevdirection = null;
			stepcounts.clear();
			trace.clear();
			Matrixmap.flag = false;
				
			brHashFile.close();
			frHashVal.close();
			bwOutFile2.close();
        } catch (Exception e) 
		{
			Log.v("prag","catch"+e);
	    }
	}
 
    protected void onResume() 
	{
    	registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        super.onResume();
        if (AccelerometerManager.isSupported()) 
		{
            AccelerometerManager.startListening(this);
        }
    }
 
    protected void onDestroy() 
	{
        super.onDestroy();
        if (AccelerometerManager.isListening()) 
		{
            AccelerometerManager.stopListening();
        }
        stopService(new Intent(Accelerometer.this,
				StepCountingService.class));
		unregisterReceiver(receiver);
    }
 
    public static Context getContext() 
	{
        return CONTEXT;
    }
 
    public void onShake(float force) 
	{
        Toast.makeText(this, "Phone shaked : " + force, 1000).show();
    }
 
    public void onAccelerationChanged(float x, float y, float z, SensorEvent event) 
	{   
    	float[] inR = new float[9];
    	float[] outR= new float[9];
    	float[] I = new float[9];
    	float[] gravity = {x, y, z};
    	float[] geomag = new float[3];
    	float[] orientVals = new float[3];
    		
		((TextView) findViewById(R.id.y)).setText( "Count:"+  TotalStepCount +"   "+ totaldistance +"   "+plot2M + "    "); 
                 
            
        // Calculate Orientation
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = event.values;
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = event.values;
        if (mGravity != null && mGeomagnetic != null) 
		{
            boolean success = SensorManager.getRotationMatrix(inR, I, mGravity, mGeomagnetic);
            if (success) 
			{
                float orientation[] = new float[3];
                SensorManager.getOrientation(inR, orientation);
                azimut = orientation[0]; 
            }
        
            
            if((azimut > -0.5) && (azimut < 1.1))
            {
             direction = "N";	 
             }
            else if ((azimut > 1.11) && (azimut < 2.5))
            {
         	   direction ="E";
            }
            
            else if (((azimut > 2.51) && (azimut < 3.3))||((azimut < -2.21) && (azimut > -3.3)) )
            {
         	   direction ="S";
            }
            else if ((azimut > -2.2) && (azimut < -0.5))
            {
         	   direction ="W";
            }
           
          if ((plot2M == "Y"))
           {
        	   stepcounts.add(StepCount);   //here stepcount is actually co-ordinate count of 1M and count gives actual foot steps
        	   trace.put(StepCount, direction);
        	   prevdirection = direction;
        	   if (plot2M == "Y")
        	   {
        		   plot2M ="N";
        	   }
              ((TextView) findViewById(R.id.z)).setText( count + "  "+ direction +"      "+ Matrixmap.Matmap()); 
        	
            }
        }
    }
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }
    public void onSensorChanged(SensorEvent event) {}
    Intent i;

    public boolean onCreateOptionsMenu(Menu menu) 
	{
		menu.add(0, 0, 0, "Refresh");
		return super.onCreateOptionsMenu(menu);
	}

	public boolean onMenuItemSelected(int featureId, MenuItem item) 
	{
		mainWifi.startScan();
		return super.onMenuItemSelected(featureId, item);
	}
	
	protected void onPause() 
	{
		unregisterReceiver(receiverWifi);
		super.onPause();
	}

    public void onClick(View v) 
    {
    	if(v==findViewById(R.id.mapbutton))
		{
    		i = new Intent(this,com.example.android.Accelerometer.MatrixGrid2_2DroidActivity.class);
    		startActivity(i);
    		Log.v("priya","afteractivity");
		}
	if(v==findViewById(R.id.bt_wifi))
	{
		try 
		{
			if (!todoItems.isEmpty())
				todoItems.clear();
					//BT removed
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
	        
				} catch (Exception e) {
					Log.v("prag", "catch" + e);
				}
		}
			
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
	        	Log.v("prag","Level:"+(wifiList.get(i)).level);
				todoItems.add((wifiList.get(i)).level);
	        	sb.append("\n");
	        }
	        aa.notifyDataSetChanged();
	        Matching(todoItems);
		}
	}
}