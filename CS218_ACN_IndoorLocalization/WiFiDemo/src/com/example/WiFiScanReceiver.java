package com.example;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.text.format.DateFormat;
import android.util.Log;

public class WiFiScanReceiver extends BroadcastReceiver {
  private static final String TAG = "WiFiScanReceiver";
  WiFiDemo wifiDemo;

  
  public WiFiScanReceiver(WiFiDemo wifiDemo) {
    super();
    this.wifiDemo = wifiDemo;
  }

  @Override
  public void onReceive(Context c, Intent intent) {
	  
    List<ScanResult> results = wifiDemo.wifi.getScanResults();
    ScanResult bestSignal = null;
    
    //to print out current TimeStamp
    String currentTime = (String) DateFormat.format("yyyy-MM-dd hh:mm:ss", new java.util.Date());
	wifiDemo.textStatus.append("\n Available APs on " + currentTime + " are below: \n");
    
    for (ScanResult result : results) {
    	wifiDemo.textStatus.append(result.SSID + ": "+ result.level + "dB" + 
    			"   (" + result.frequency +"Hz) \n");
    	if (bestSignal == null
    			|| WifiManager.compareSignalLevel(bestSignal.level, result.level) < 0)
        bestSignal = result;
    }  
    String message = String.format("\n\n To sum up, \n Among the %s APs. %s (%s dB) is the strongest AP.\n\n",
        results.size(), bestSignal.SSID, bestSignal.level);
    
    wifiDemo.textStatus.append(message);
    //Toast.makeText(wifiDemo, message, Toast.LENGTH_LONG).show();
    Log.d(TAG, "onReceive() message: " + message);
  }

}
