package prag.com.BT_FileDroid;

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
import prag.com.BT_FileDroid.R;

//import prag.com.bluetooth1.Activity;
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


public class BT_FileDroidActivity extends Activity {
	protected static final int REQUEST_ENABLE_BT = 0;
	protected static final int DISCOVERY_REQUEST = 0;
	
	
	
	public void MatchingDbValues()
	{
//Copied from FileOpAndroidActivity.java
    	//Declarations
    	File fPLFile = new File("/sdcard/Sample.txt");
//    	File fMyVal = new File("/sdcard/myRecorded_DB_Values.txt");
//    	File fOutFile = new File("/sdcard/Prag.txt");
//    	File fOutFile2 = new File("/sdcard/Prag2.txt");
//    	File fHashFile = new File("/sdcard/Prag.txt");
    	//File fin=new File(Environment.getExternalStorageDirectory() +"Sample.txt");
    	
		ArrayList<PathLossValues> alValues = new ArrayList<PathLossValues>();
		ArrayList<Double> alMyValues = new ArrayList<Double>();
		String line2, line;
		int iSizeOfDbVal,count = 0;
    	double dMyDbValue,eDouble;
    	
    	//alMyValues = todoItems;
    	ArrayList<Double> list2 = new ArrayList<Double>(); // to find duplicate elements in the DbValues array list
  	    HashSet hs2 = new HashSet();				// to find duplicate elements in the DbValues array list
  	    ArrayList<Double> list;
  	    Hashtable<Val,ArrayList<Double>> numbers = new Hashtable<Val,ArrayList<Double>>();
    	
  	    
        try 
        {
        	FileReader frPLFile = new FileReader(fPLFile);
//        	FileReader frMyVal = new FileReader(fMyVal);
//        	FileWriter fwOutFile = new FileWriter(fOutFile);
//        	FileWriter fwOutFile2 = new FileWriter(fOutFile2);
//        	FileReader  frHashVal = new FileReader(fHashFile);
  		  	
            BufferedReader brPLFile = new BufferedReader(frPLFile);
//    		BufferedReader brMyVal = new BufferedReader(frMyVal);
//    		BufferedWriter bwOutFile = new BufferedWriter(fwOutFile);
//    		BufferedWriter bwOutFile2 = new BufferedWriter(fwOutFile2);
//    		BufferedReader brHashFile = new BufferedReader(frHashVal);
    		
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
		//        	bwOutFile2.write("Key: \""+ob.x+":"+ob.y+"\" has value of: \"" +aValue.toString()+"\""+"\n");
		        	Log.v("prag","Key: \""+ob.x+":"+ob.y+"\" has value of: \"" +aValue.toString()+"\"");
		        }
		    }
		    brPLFile.close();
//			brHashFile.close();
//			frHashVal.close();
//			bwOutFile2.close();
			Log.v("prag","Part5b");
          } catch (Exception e) 
              {
            	Log.v("prag","catch:B: "+e);
            	//e.printStackTrace();}
              }
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
Button button = (Button) findViewById(R.id.myButton1);
        
//        ListView myListView = (ListView)findViewById(R.id.mylistView1);
        // Create the array list of to do items
        final ArrayList<Double> todoItems = new ArrayList<Double>();
        // Create the array adapter to bind the array to the listview
        final ArrayAdapter<Double> aa;
        aa = new ArrayAdapter<Double>(this,
        android.R.layout.simple_list_item_1,
        todoItems);
        // Bind the array adapter to the listview.
 //       myListView.setAdapter(aa);
        
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	if(!todoItems.isEmpty())
            		todoItems.clear();
  //          	final TextView tv3 = (TextView) findViewById(R.id.textView3);
  //          	final ListView lv1 = (ListView) findViewById(R.id.mylistView1);
            	BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//                if (mBluetoothAdapter == null) 
//                {
//                	tv3.setText("Device does not support Bluetooth");
//                }
//                else
//                {
                	String toastText;
//                	tv3.setText("Device supports Bluetooth");
            	    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            	    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            	    String address = mBluetoothAdapter.getAddress();
            	    String name = mBluetoothAdapter.getName();
            	    toastText = name + " : " + address; 
            	    Toast.makeText(BT_FileDroidActivity.this, toastText, Toast.LENGTH_LONG).show();
//            	    tv3.setText(toastText);
 //               }
                boolean value = mBluetoothAdapter.startDiscovery();
                Log.i("prgas"," in on recv resi;1");
                final String dStarted = BluetoothAdapter.ACTION_DISCOVERY_STARTED;
            	final String dFinished = BluetoothAdapter.ACTION_DISCOVERY_FINISHED;
                BroadcastReceiver discoveryMonitor = new BroadcastReceiver() 
                {
                	@Override
                	public void onReceive(Context context, Intent intent) 
                	{
                		Log.i("prgas"," in on recv resi;1B");
	                	if (dStarted.equals(intent.getAction())) 
	                	{
	                		//Log.i("prgas"," in on recv resi;2");
		                	// Discovery has started.
		                	Toast.makeText(getApplicationContext(),
		                	"Discovery Started ... ", Toast.LENGTH_SHORT).show();
	                	}
	                	else if (dFinished.equals(intent.getAction())) 
	                	{
	                		//Log.i("prgas"," in on recv resi;3");
		                	// Discovery has completed.
		                	Toast.makeText(getApplicationContext(),
		                	"Discovery Completed ... ", Toast.LENGTH_SHORT).show();
	                	}
	                	String remoteDeviceName = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
	            		BluetoothDevice remoteDevice;
	            		remoteDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
	            		double rssi = intent.getDoubleExtra(BluetoothDevice.EXTRA_RSSI, Double.MIN_VALUE);
	            		
	            		Toast.makeText(getApplicationContext(), "Dev: " + remoteDeviceName + "; RSSI:" + rssi, Toast.LENGTH_SHORT).show();           		 
	            		//Log.i("prgas"," in on recv resi;DUP10");
	            	//	todoItems.add(0, remoteDeviceName.toString()+": RSSI "+rssi);
	            		todoItems.add(0, rssi);
			        	
			        	HashSet hs = new HashSet();
			        	hs.addAll(todoItems);
			        	todoItems.clear();
			        	todoItems.addAll(hs);
			        	aa.notifyDataSetChanged();
	                	//Log.i("prgas"," in on recv resi;3B");
                	}
                };
                //Log.i("prgas"," in on recv resi;4");
            	registerReceiver(discoveryMonitor, 	new IntentFilter(dStarted));
            	//Log.i("prgas"," in on recv resi;5");
            	registerReceiver(discoveryMonitor, 	new IntentFilter(dFinished));
            	registerReceiver(discoveryMonitor, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            	if (!mBluetoothAdapter.isDiscovering())
            	{
            		mBluetoothAdapter.startDiscovery();
            	}
            	//Log.i("prgas"," in on recv resi;6");
//            	BroadcastReceiver discoveryResult = new BroadcastReceiver() 
//            	{
//            		@Override
//            		public void onReceive(Context context, Intent intent) 
//            		{
//            			//Log.i("prgas"," in on recv resi;10");
//	            		
//            		}
//            	};
            	registerReceiver(discoveryMonitor, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            	if (!mBluetoothAdapter.isDiscovering())
            	{
            		mBluetoothAdapter.startDiscovery();
            	}
            }   
        });
    }
}