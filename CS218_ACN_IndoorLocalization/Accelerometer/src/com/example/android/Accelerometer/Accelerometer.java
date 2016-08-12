package com.example.android.Accelerometer;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

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
import android.view.View;
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

 
/**
 * Android accelerometer sensor tutorial
 * @author antoine vianey
 * under GPL v3 : http://www.gnu.org/licenses/gpl-3.0.html
 */
public class Accelerometer extends Activity implements AccelerometerListener,SensorEventListener 
{
    private static Context CONTEXT;
    public static int count=0;
    public static int prevcount =0;
    public static String plot2M ="N";
    public float magnitude;
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
 //   public static BT_FileDroidActivity bt = new BT_FileDroidActivity();
    
    
    protected static final int REQUEST_ENABLE_BT = 0;
	protected static final int DISCOVERY_REQUEST = 0;
	
	public static ArrayList<Integer> bluetoothi = new ArrayList<Integer>();
	public static ArrayList<Integer> bluetoothj = new ArrayList<Integer>();
   
 
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        CONTEXT = this;
     // bt.onCreate(savedInstanceState);
        
        
        
        
        Button button = (Button) findViewById(R.id.myButton1);

	//	ListView myListView = (ListView) findViewById(R.id.mylistView1);
// Create the array list of to do items
		final ArrayList<Short> todoItems = new ArrayList<Short>();
// Create the array adapter to bind the array to the listview
		final ArrayAdapter<Short> aa;
		aa = new ArrayAdapter<Short>(this,
				android.R.layout.simple_list_item_1, todoItems);
// Bind the array adapter to the listview.
	//	myListView.setAdapter(aa);

		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				try {
					if (!todoItems.isEmpty())
						todoItems.clear();
					Log.v("prag", "1a");
			//		final TextView tv3 = (TextView) findViewById(R.id.textView3);
			//		final ListView lv1 = (ListView) findViewById(R.id.mylistView1);
					BluetoothAdapter mBluetoothAdapter = BluetoothAdapter
							.getDefaultAdapter();
					Log.v("prag", "1b");
					if (mBluetoothAdapter == null) {
			//			tv3.setText("Device does not support Bluetooth");
					} else {
						String toastText;
				//		tv3.setText("Device supports Bluetooth");
						Intent enableBtIntent = new Intent(
								BluetoothAdapter.ACTION_REQUEST_ENABLE);
						startActivityForResult(enableBtIntent,
								REQUEST_ENABLE_BT);
						String address = mBluetoothAdapter.getAddress();
						String name = mBluetoothAdapter.getName();
						toastText = name + " : " + address;
						Toast.makeText(Accelerometer.this, toastText,
								Toast.LENGTH_LONG).show();
				//		tv3.setText(toastText);
					}
					Log.v("prag", "1c");
					boolean value = mBluetoothAdapter.startDiscovery();
					final String dStarted = BluetoothAdapter.ACTION_DISCOVERY_STARTED;
					final String dFinished = BluetoothAdapter.ACTION_DISCOVERY_FINISHED;
					BroadcastReceiver discoveryMonitor = new BroadcastReceiver() {
						@Override
						public void onReceive(Context context, Intent intent) {
							if (dStarted.equals(intent.getAction())) {
// Discovery has started.
								Toast.makeText(getApplicationContext(),
										"Discovery Started ... ",
										Toast.LENGTH_SHORT).show();
							} else if (dFinished.equals(intent.getAction())) {
// Discovery has completed.
								Toast.makeText(getApplicationContext(),
										"Discovery Completed ... ",
										Toast.LENGTH_SHORT).show();
							}
							Log.v("prag", "1d");
							String remoteDeviceName = intent
									.getStringExtra(BluetoothDevice.EXTRA_NAME);
							BluetoothDevice remoteDevice;
							remoteDevice = intent
									.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
							short rssi = intent.getShortExtra(
									BluetoothDevice.EXTRA_RSSI,
									Short.MIN_VALUE);
							Toast.makeText(
									getApplicationContext(),
									"Dev: " + remoteDeviceName + "; RSSI:"
											+ rssi, Toast.LENGTH_SHORT).show();
							todoItems.add(0, rssi);
							HashSet hs = new HashSet();
							hs.addAll(todoItems);
							todoItems.clear();
							todoItems.addAll(hs);
							aa.notifyDataSetChanged();
//							for(int i =0; i<todoItems.size();i++)
//								Log.i("prgas", "Loop: " + i + "Size: " + todoItems.size() + "ToDo: "+ todoItems.get(i));
							Log.v("prag", "Call Matching: "+todoItems.size());
							Matching(todoItems);
							Log.v("prag", "Called Matching");
							
						}
					};
					Log.v("prag", "1e");
					registerReceiver(discoveryMonitor, new IntentFilter(
							dStarted));
					Log.v("prag", "1f");
					registerReceiver(discoveryMonitor, new IntentFilter(
							dFinished));
					Log.v("prag", "1g");
					registerReceiver(discoveryMonitor, new IntentFilter(
							BluetoothDevice.ACTION_FOUND));
					Log.v("prag", "1h");
					if (!mBluetoothAdapter.isDiscovering()) {
						mBluetoothAdapter.startDiscovery();
					}	
					
//					Log.v("prag", "Call Matching: "+todoItems.size());
//					Matching(todoItems);
//					Log.v("prag", "Called Matching");
					//flag = true;
				} catch (Exception e) {
					Log.v("prag", "catch" + e);
				}
			}
			
		});
      
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
    }
    
    public void Matching(ArrayList<Short> todoItems)
	{
    	//Copied from FileOpAndroidActivity.java 
    	//Declarations
    			File fPLFile = new File("/sdcard/Sample.txt");
    	    	File fMyVal = new File("/sdcard/myRecorded_DB_Values.txt");
    	    	File fOutFile = new File("/sdcard/Prag.txt");
    	    	File fOutFile2 = new File("/sdcard/Prag2.txt");
    	    	File fHashFile = new File("/sdcard/Prag.txt");
    	    	//File fin=new File(Environment.getExternalStorageDirectory() +"Sample.txt");
    	    	
    			ArrayList<PathLossValues> alValues = new ArrayList<PathLossValues>();
    			ArrayList<Double> alMyValues = new ArrayList<Double>();
    			String line2, line;
    			int iSizeOfDbVal,count = 0;
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
    	        	FileReader frMyVal = new FileReader(fMyVal);
    	        	FileWriter fwOutFile = new FileWriter(fOutFile);
    	        	FileWriter fwOutFile2 = new FileWriter(fOutFile2);
    	        	FileReader  frHashVal = new FileReader(fHashFile);
    	  		  	
    	            BufferedReader brPLFile = new BufferedReader(frPLFile);
    	    		BufferedReader brMyVal = new BufferedReader(frMyVal);
    	    		BufferedWriter bwOutFile = new BufferedWriter(fwOutFile);
    	    		BufferedWriter bwOutFile2 = new BufferedWriter(fwOutFile2);
    	    		BufferedReader brHashFile = new BufferedReader(frHashVal);
    	    		
    	    		while((line = brPLFile.readLine()) != null)
    	  		    {
    	  			  if(line.length()!=0)
    	  			  {
    	  				  count++;
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
    	    		
    	    	  Log.v("prag","Part1");
    	        	
    	//Read My recorded Signal Strength values from another file and put in a List, so that both values can be compared 
    	  		  while((line2 = brMyVal.readLine())!=null)
    	  		  {
    	  			  if(line2.length()!=0)
    	  			  {
    	  				  dMyDbValue = Double.parseDouble(line2.toString());
    	  				  alMyValues.add(dMyDbValue);
    	  				  //System.out.println("Added:"+dMyDbValue);
    	  			  }
    	  		  }
    	  		  Log.v("prag","Part2");

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
    						  bwOutFile.write(objPLtemp.Rx + " " + objPLtemp.Ry + " " + objPLtemp.Tx + " " + objPLtemp.Ty + " " + objPLtemp.dbVal + " " + t5 + "\n");
    					  }			  
    				  }
    			  }	
    			  
    			  Log.v("prag","Part3");
    			  brMyVal.close();
    			  //brPLFile.close();
    			  bwOutFile.close();
    			  frMyVal.close();
    			  //frPLFile.close();
    			  fwOutFile.close();
    			  
    			  
    			 
    			  
    			//Finding duplicate entires in the MyDbVal		  
    			  hs.addAll(list2);
    			  list2.clear();
    			  list2.addAll(hs);
    			  iSizeOfDbVal = list2.size();
    			  
    			  //Log.v("prgas","Part3: brPLFile:" + brPLFile.readLine());
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
    			  	Log.v("prag","Part4");
    			  	Log.v("prag","iSizeOfDbVal: "+iSizeOfDbVal);
    				java.util.Enumeration keys = numbers.keys();
    				
    				while( keys.hasMoreElements() ) 
    			    {
    					Object aKey = keys.nextElement();
    					Val ob = (Val) aKey;
    			        ArrayList aValue = numbers.get(aKey);
//    			        Remove duplicate entries from the RecordedDbValues. ASSUMING all devices have different db values
    			        HashSet hs2 = new HashSet();
    					hs2.addAll(aValue);
    					aValue.clear();
    					aValue.addAll(hs2);
    					if (iSizeOfDbVal <= aValue.size())
    			        {   bluetoothi.add((int)Math.round(ob.x*0.5286));
    			        bluetoothj.add((int)ob.y);
    			        	//bwOutFile2.write("Key: \""+ob.x+":"+ob.y+"\" has value of: \"" +aValue.toString()+"\""+"\n");
    			        	//Log.v("prgas","Key: \""+ob.x+":"+ob.y+"\" has value of: \"" +aValue.toString()+"\"");
    			        }
    			    }
    				brHashFile.close();
    				frHashVal.close();
    				bwOutFile2.close();
    				Log.v("prag","Part5");
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
 
    protected void onResume() 
	{
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
    	//TextView l1 = new TextView(this);
    	//TextView l2 = new TextView(this);
    	//TextView l3 = new TextView(this);
    	
    	
   // 	String res ="NULL";
    	    
    	magnitude = (float)(y*y + z*z);
    	    
      	    
/* Calculate the Step count */
            
    	if ((magnitude> 160) && (flg =="down") )
 	        {
			 flg ="up";
			}
        if ((flg =="up") && (magnitude < 60))
            {
			 flg ="down";
             count++;
             if (count -prevcount == 2)
                { 
            	  prevcount =count;
                  plot2M ="Y";
                }
            }

        
      /*  l1.setText("Count:  "+  count+ "\r\n");
       l2.setText("X :" +String.valueOf(x)+ "   Y :"+String.valueOf(y)+"   Z :"+String.valueOf(z) + "\r\n");
       setContentView(l1);
      setContentView(l2); */
        
        
        ((TextView) findViewById(R.id.y)).setText( "Count:"+  count);
       ((TextView) findViewById(R.id.x)).setText("X :" +String.valueOf(x)+ "   Y :"+String.valueOf(y)+"   Z :"+String.valueOf(z));
                 
            
           /* Calculate Orientation */
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
            
            
            
          /* if((azimut > -0.19) && (azimut < 0.6))
           {
            direction = "N";	 
            }
           else if ((azimut > 0.61) && (azimut < 1.4))
           {
        	   direction ="NE";
           }
           else if ((azimut > 1.41) && (azimut < 2.2))
           {
        	   direction ="E";
           }
           else if ((azimut > 2.21) && (azimut < 2.8))
           {
        	   direction ="SE";
           }
           else if (((azimut > 2.81) && (azimut < 3.3))||((azimut < -2.81) && (azimut > -3.3)) )
           {
        	   direction ="S";
           }
           else if ((azimut > -2.7) && (azimut < -1.8))
           {
        	   direction ="SW";
           }
           else if ((azimut > -1.79) && (azimut < -1.1))
           {
        	   direction ="W";
           }
           else if ((azimut > -1.09) && (azimut < -0.2))
           {
        	   direction ="NW";
           }*/
           
           
          //if ((direction != prevdirection)||(plot2M == "Y"))
          if (plot2M == "Y")
           {
        	   stepcounts.add(count);   //here stepcount is actually co-ordinate count of 1M and count gives actual foot steps
        	   trace.put(count, direction);
        	   prevdirection = direction;
        	   if (plot2M == "Y")
        	   {plot2M ="N";}
        	   
        	//   Matrixmap.Matmap();
        	
        	//   l3.setText( count + "  "+ direction +"      "+ Matrixmap.Matmap()  );
        //	   setContentView(l3);
          ((TextView) findViewById(R.id.z)).setText( count + "  "+ direction +"      "+ Matrixmap.Matmap()); 
        	    
        	 
            }
	
        
        //  ((TextView) findViewById(R.id.z)).setText(count + "  "+ direction +"      "+ Matrixmap.Matmap()  ); 
         
        }
    }
 public void onAccuracyChanged(Sensor sensor, int accuracy) { }
 public void onSensorChanged(SensorEvent event) {}   
          
	   
	   
	  
	    
	  
	   

    
   
 
    
    	    
    	   
    	   
}





























