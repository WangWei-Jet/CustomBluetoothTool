package com.oneway.custombluetoothtool.utils;

import android.bluetooth.BluetoothDevice;

public class BluetoothInfo {
	private String name;
	private String mac;
	private BluetoothDevice device;

	public BluetoothInfo(String name, String mac, BluetoothDevice device) {
		super();
		this.name = name;
		this.mac = mac;
		this.device = device;
	}

	public String toString() {
		return name;
	}

	public BluetoothDevice getDevice() {
		return device;
	}

	public void setDevice(BluetoothDevice device) {
		this.device = device;
	}

	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
