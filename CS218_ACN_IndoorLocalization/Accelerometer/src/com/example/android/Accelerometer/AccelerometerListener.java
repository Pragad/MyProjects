package com.example.android.Accelerometer;

import android.hardware.SensorEvent;

public interface AccelerometerListener {

	public void onAccelerationChanged(float x, float y, float z, SensorEvent event);
	
	// public void onSensorChangedd(SensorEvent event);
	 
	public void onShake(float force);
 
}
