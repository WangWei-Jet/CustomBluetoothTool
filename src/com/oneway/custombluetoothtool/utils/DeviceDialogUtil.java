package com.oneway.custombluetoothtool.utils;

import java.lang.reflect.Field;

import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.os.Handler;
import android.view.KeyEvent;
import android.widget.ArrayAdapter;

public class DeviceDialogUtil {

	private Handler handler;
	private Dialog mDialog;
	private ArrayAdapter<BluetoothInfo> adapter = null;
	private BluetoothDevice device;
	private BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

	public DeviceDialogUtil(Handler handler) {
		super();
		this.handler = handler;
		device = null;
	}

	private void createDialog(final Context context) {
		device = null;
		adapter = new ArrayAdapter<BluetoothInfo>(context,
				android.R.layout.select_dialog_singlechoice,
				BlueToothDeviceReceiver.items);
		mDialog = new AlertDialog.Builder(context)
				.setTitle("Device Found")
				.setSingleChoiceItems(adapter, -1,
						new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialog,
									final int whichButton) {
								BluetoothAdapter.getDefaultAdapter()
										.cancelDiscovery();
								System.out
										.println("whichButton:" + whichButton);
								System.out.println("keys[whichButton]:"
										+ BlueToothDeviceReceiver.items.get(
												whichButton).getName());
								device = BlueToothDeviceReceiver.items.get(
										whichButton).getDevice();
								System.out.println("device:" + device);
								System.out.println("deviceType:"
										+ device.getType());

							}
						})
				.setPositiveButton("scan",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								btAdapter.cancelDiscovery();
								// BlueToothUtil.items.clear();
								BlueToothDeviceReceiver.items.clear();
								adapter.notifyDataSetChanged();
								btAdapter.startDiscovery();
								dismissDialog(mDialog, false);
							}
						})
				.setNegativeButton("sure",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								if (device != null) {
									handler.obtainMessage(
											HandlerMSG.DEVICE_ENSURED, device)
											.sendToTarget();
									dismissDialog(mDialog, true);
									BlueToothDeviceReceiver.items.clear();
									mDialog = null;
									btAdapter.cancelDiscovery();

								} else {
									dismissDialog(mDialog, false);
									mDialog = null;
									handler.obtainMessage(
											HandlerMSG.NO_DEVICE_SELECTED)
											.sendToTarget();
								}
							}
						}).create();
		mDialog.setCancelable(false);
		mDialog.show();

		/**
		 * 当dialog在页面上的时候，监听此时的返回键
		 */
		mDialog.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent event) {
				// 按下返回键的时候关闭dialog
				if (keyCode == KeyEvent.KEYCODE_BACK) {
					dismissDialog(mDialog, true);
					btAdapter.cancelDiscovery();
					BlueToothDeviceReceiver.items.clear();
					mDialog = null;
					return true;
				}
				return false;
			}
		});
	}

	/**
	 * 决定是否关闭dialog
	 * 
	 * @param dialog
	 * @param flag
	 */
	private void dismissDialog(Dialog dialog, boolean flag) {
		try {
			Field field = dialog.getClass().getSuperclass()
					.getDeclaredField("mShowing");
			field.setAccessible(true);
			field.set(dialog, flag);
			dialog.dismiss();
		} catch (Exception e) {

		}
	}

	public void listDevice(final Context context) {
		if (mDialog == null) {
			adapter = null;
			createDialog(context);
		}
		adapter.notifyDataSetChanged();
	}
}
