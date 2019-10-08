package com.oneway.custombluetoothtool.utils;

import java.util.ArrayList;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

public class BlueToothDeviceReceiver extends BroadcastReceiver {
	public static ArrayList<BluetoothInfo> items;
	private Handler handler;
	private String tag = BlueToothDeviceReceiver.class.getSimpleName();

	public BlueToothDeviceReceiver(Handler mHandler) {
		this.handler = mHandler;
		items = new ArrayList<BluetoothInfo>();
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void onReceive(Context context, Intent intent) {

		String action = intent.getAction();
		Bundle b = intent.getExtras();
		Object[] lstName = b.keySet().toArray();

		// ��ʾ�����յ�����Ϣ����ϸ��
		for (int i = 0; i < lstName.length; i++) {
			String keyName = lstName[i].toString();
			Log.d(keyName, String.valueOf(b.get(keyName)));
		}

		if (BluetoothDevice.ACTION_FOUND.equals(action)) {

			BluetoothDevice bluetoothDevice = intent
					.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			handler.obtainMessage(HandlerMSG.DEVICE_FOUND, bluetoothDevice)
					.sendToTarget();
		}

		if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
			Log.e(tag, "�յ����������Ĺ㲥XXXXX");
		}

		if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
			Log.e(tag, "�յ��������ӶϿ��Ĺ㲥XXXXX");
			handler.obtainMessage(HandlerMSG.DEVICE_DISCONNECTED)
					.sendToTarget();
		} else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
			int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
			Log.e(tag, "����������״̬�ı�XXXXX,state:" + state);
			if (state == BluetoothAdapter.STATE_OFF) {
				Log.e(tag, "�����������ر�");
			} else if (state == BluetoothAdapter.STATE_ON) {
				Log.e(tag, "��������������");
			}
		} else if (BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED
				.equals(action)) {
			int state = intent.getIntExtra(
					BluetoothAdapter.EXTRA_CONNECTION_STATE, -1);
			Log.e(tag, "��������������״̬�ı�YYYYYYYYY,state:" + state);
			BluetoothAdapter.getDefaultAdapter().enable();
		} else if (HandlerMSG.SHOW_TRACE_INFO.equals(action)) {
			String traceInfo = intent.getStringExtra("traceInfo");
			handler.obtainMessage(HandlerMSG.SHOW_COMMON_MSG, traceInfo)
					.sendToTarget();
		}
	}

	// private int findBluetoothDevice(String mac,
	// ArrayList<BluetoothStruct> deviceList) {
	// for (int i = 0; i < deviceList.size(); i++) {
	// if (((BluetoothStruct) deviceList.get(i)).getMac().equals(mac))
	// return i;
	// }
	// return -1;
	// }

}
