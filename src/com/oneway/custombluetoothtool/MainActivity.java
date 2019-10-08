package com.oneway.custombluetoothtool;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.oneway.bt.BluetoothDriver;
import com.oneway.bt.controller.LogInfoController;
import com.oneway.custombluetoothtool.utils.BlueToothDeviceReceiver;
import com.oneway.custombluetoothtool.utils.BluetoothInfo;
import com.oneway.custombluetoothtool.utils.DeviceDialogUtil;
import com.oneway.custombluetoothtool.utils.HandlerMSG;
import com.oneway.custombluetoothtool.utils.Utils;

public class MainActivity extends Activity {

	private Button button11, button12, button21, button31, button41, button42;
	private TextView textResult;
	private ScrollView resultScrollView;

	private BluetoothDriver btDriver = new BluetoothDriver();
	private DialogHandler dialogHandler;
	private DeviceDialogUtil devicedialog;
	private BlueToothDeviceReceiver receiver;
	private BluetoothDevice currentDevice;
	private BluetoothDevice disconnDevice = null;
	// private BluetoothDevice targetCommuDevice = null;
	private AsyncTask<Object, Integer, Boolean> connectAsync = null;
	private AsyncTask<Object, Integer, Boolean> disconnectAsync = null;
	private AsyncTask<Object, Integer, Boolean> commuAsync = null;
	private AsyncTask<Object, Integer, Boolean> swipeCardAsync = null;

	private StringBuffer resultHolder = new StringBuffer();
	private String tag = MainActivity.class.getSimpleName();

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		initUI();
		btDriver.init(MainActivity.this);

		LogInfoController.setAllowNormalLogPrint(true);
		LogInfoController.setAllowCommunicationLogPrint(true);

		// handler用于跟UI的交互
		dialogHandler = new DialogHandler();
		devicedialog = new DeviceDialogUtil(dialogHandler);

		// 广播接收者接收监听蓝牙状态，然后将需要的信息由Hanlder放到队列以便更新UI使用
		receiver = new BlueToothDeviceReceiver(dialogHandler);
		IntentFilter intent = new IntentFilter();
		intent.addAction(BluetoothDevice.ACTION_FOUND);
		intent.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
		intent.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
		intent.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		intent.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
		intent.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
		intent.addAction(BluetoothDevice.ACTION_NAME_CHANGED);
		intent.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
		intent.addAction(HandlerMSG.SHOW_TRACE_INFO);
		intent.setPriority(-1000);
		this.registerReceiver(receiver, intent);

	}

	@Override
	protected void onResume() {
		super.onResume();
		if (dialogHandler != null) {
			// 不能直接调用fullscroll，有可能达不到效果，放在队列中执行
			dialogHandler.post(new Runnable() {
				@Override
				public void run() {
					resultScrollView.fullScroll(ScrollView.FOCUS_DOWN);
				}
			});
		}
	}

	// handler获取队列中的信息,更新UI
	@SuppressLint("HandlerLeak")
	class DialogHandler extends Handler {

		@Override
		public void dispatchMessage(Message msg) {

			super.dispatchMessage(msg);

			switch (msg.what) {

			case HandlerMSG.NO_DEVICE_SELECTED:
				Toast.makeText(MainActivity.this, "未选择设备", Toast.LENGTH_SHORT)
						.show();
				break;

			// 搜索到蓝牙设备
			case HandlerMSG.DEVICE_FOUND:
				// if (!btDriver.isDeviceConnected()) {
				BluetoothDevice bluetoothDeviceFound = (BluetoothDevice) msg.obj;
				int index = findBluetoothDevice(
						bluetoothDeviceFound.getAddress(),
						BlueToothDeviceReceiver.items);
				if (index < 0 && bluetoothDeviceFound.getName() != null) {
					BlueToothDeviceReceiver.items.add(new BluetoothInfo(
							bluetoothDeviceFound.getName(),
							bluetoothDeviceFound.getAddress(),
							bluetoothDeviceFound));
				}
				devicedialog.listDevice(MainActivity.this);
				// }
				break;

			// 选中蓝牙设备
			case HandlerMSG.DEVICE_ENSURED:
				currentDevice = (BluetoothDevice) msg.obj;
				showResult(textResult, resultHolder,
						"已选择设备:" + currentDevice.getName());
				// textResult.setText("已选择设备:" + currentDevice.getName());
				break;

			case HandlerMSG.SHOW_COMMON_MSG:
				showResult(textResult, resultHolder, (String) msg.obj);
				// textResult.setText((String) msg.obj);
				break;

			// 蓝牙断开连接
			case HandlerMSG.DEVICE_DISCONNECTED:
				showResult(textResult, resultHolder, "设备断开连接");
				// showResult.setText("设备断开连接");
				break;
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(tag, "MainActivity destroied.");
		if (btDriver.isDeviceConnected()) {
			new Thread(new Runnable() {

				@Override
				public void run() {

					btDriver.disconnect();

				}
			}).start();
		}
		if (receiver != null) {
			this.unregisterReceiver(receiver);
		}
	}

	private int findBluetoothDevice(String mac,
			ArrayList<BluetoothInfo> deviceList) {
		for (int i = 0; i < deviceList.size(); i++) {
			if (((BluetoothInfo) deviceList.get(i)).getMac().equals(mac))
				return i;
		}
		return -1;
	}

	private void showResult(TextView targetView, StringBuffer bufferHolder,
			String msg) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
				Locale.getDefault());
		bufferHolder.append(format.format(new java.util.Date()) + "\n" + msg
				+ "\n");
		targetView.setText(bufferHolder.toString());
		dialogHandler.post(new Runnable() {
			@Override
			public void run() {
				resultScrollView.fullScroll(ScrollView.FOCUS_DOWN);
			}
		});
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private void initUI() {
		button11 = (Button) findViewById(R.id.mainLinear11);
		button11.setText("scan device");
		button11.setVisibility(View.VISIBLE);
		button11.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// if (btDriver.isDeviceConnected()) {
				// Toast.makeText(MainActivity.this, "当前有设备连接",
				// Toast.LENGTH_SHORT).show();
				// return;
				// }
				BlueToothDeviceReceiver.items.clear();
				BluetoothAdapter.getDefaultAdapter().startDiscovery();
			}
		});
		button12 = (Button) findViewById(R.id.mainLinear12);
		button12.setText("connect device");
		button12.setVisibility(View.VISIBLE);
		button12.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// if (currentDevice == null) {
				// Toast.makeText(MainActivity.this,
				// "pls choose device first", Toast.LENGTH_SHORT)
				// .show();
				// return;
				// }
				// if (btDriver.isDeviceConnected()) {
				// Toast.makeText(MainActivity.this, "当前有设备连接",
				// Toast.LENGTH_SHORT).show();
				// return;
				// }
				Log.d(tag, "开启连接设备线程");
				connectAsync = new AsyncTask<Object, Integer, Boolean>() {
					BluetoothDevice targetDevice = null;
					AlertDialog showDialog;

					@Override
					protected void onPreExecute() {
						super.onPreExecute();
						textResult.setText("");
						if (showDialog == null) {
							showDialog = new AlertDialog.Builder(
									MainActivity.this).setTitle("提示")
									.setMessage("正在连接设备，请稍后！").create();
							showDialog.setCancelable(false);
						}
						showDialog.show();
					}

					@Override
					protected Boolean doInBackground(Object... params) {
						targetDevice = (BluetoothDevice) params[0];
						boolean conFlag = btDriver.connectDevice(targetDevice);
						if (conFlag) {
							Log.d(tag, "设备连接成功");
						} else {
							Log.d(tag, "设备连接失败");
						}
						return conFlag;
					}

					@Override
					protected void onPostExecute(Boolean result) {
						super.onPostExecute(result);
						if (showDialog != null) {
							showDialog.dismiss();
						}
						if (result) {
							dialogHandler.obtainMessage(
									HandlerMSG.SHOW_COMMON_MSG,
									"蓝牙连接成功\n已连接设备:" + targetDevice.getName())
									.sendToTarget();
						} else {
							dialogHandler.obtainMessage(
									HandlerMSG.SHOW_COMMON_MSG, "连接设备失败")
									.sendToTarget();
						}
					}

				};
				connectAsync.execute(currentDevice);
			}
		});

		button21 = (Button) findViewById(R.id.mainLinear21);
		button21.setText("show Connected Devices");
		button21.setVisibility(View.VISIBLE);
		button21.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				List<BluetoothSocket> sockets = btDriver.getSocketList();
				if (sockets == null || sockets.size() == 0) {
					Toast.makeText(getApplicationContext(), "当前无设备连接",
							Toast.LENGTH_SHORT).show();
					return;
				}
				for (BluetoothSocket socket : sockets) {
					BluetoothDevice device = socket.getRemoteDevice();
					showResult(
							textResult,
							resultHolder,
							"已连接设备:" + device.getName() + "\nmac:"
									+ device.getAddress() + "\nconn:"
									+ socket.isConnected());
				}

			}
		});

		button41 = (Button) findViewById(R.id.mainLinear41);
		button41.setText("transcommand");
		button41.setVisibility(View.VISIBLE);
		button41.setOnClickListener(new OnClickListener() {

			BluetoothDevice tarDevice = null;

			@Override
			public void onClick(View v) {
				if (!btDriver.isDeviceConnected()) {
					Toast.makeText(MainActivity.this, "当前无设备连接",
							Toast.LENGTH_SHORT).show();
					return;
				}
				Map<BluetoothDevice, BluetoothSocket> allDevices = btDriver
						.getConnectedInfo();
				if (allDevices == null || allDevices.size() == 0) {
					Toast.makeText(getApplicationContext(), "当前无设备连接",
							Toast.LENGTH_SHORT).show();
					return;
				}
				Log.d(tag, "开启通讯线程");
				commuAsync = new AsyncTask<Object, Integer, Boolean>() {
					BluetoothDevice targetDevice = null;

					@Override
					protected void onPreExecute() {
						super.onPreExecute();
						textResult.setText("");
					}

					@Override
					protected Boolean doInBackground(Object... params) {
						if (params != null && params.length > 0
								&& params[0] instanceof BluetoothDevice) {
							targetDevice = (BluetoothDevice) params[0];
						}
						byte[] cmd = Utils.buildErrorRateTestCommand(240);
						byte[] response = new byte[300];
						int resLen = btDriver.transmit(cmd, cmd.length,
								response, 3000l, targetDevice);
						System.out.println("resLen:" + resLen);
						if (resLen > 0) {
							Log.d(tag,
									"收到数据:"
											+ Utils.bytesToHexString(response,
													resLen));
							Log.d(tag, "通讯成功");
							dialogHandler.obtainMessage(
									HandlerMSG.SHOW_COMMON_MSG,
									"通讯成功"
											+ "\nSend:"
											+ Utils.bytesToHexString(cmd)
											+ "\nReceive:"
											+ Utils.bytesToHexString(response,
													resLen)).sendToTarget();
						} else {
							Log.d(tag, "通讯失败");
							dialogHandler.obtainMessage(
									HandlerMSG.SHOW_COMMON_MSG, "通讯失败")
									.sendToTarget();
						}
						return resLen > 0 ? true : false;
					}

					@Override
					protected void onPostExecute(Boolean result) {
						super.onPostExecute(result);
					}

				};
				if (allDevices.size() > 1) {
					List<BluetoothInfo> all = new ArrayList<BluetoothInfo>();
					Set<BluetoothDevice> devices = allDevices.keySet();
					for (BluetoothDevice temp : devices) {
						all.add(new BluetoothInfo(temp.getName(), temp
								.getAddress(), temp));
					}
					final List<BluetoothInfo> showAll = all;
					ArrayAdapter<BluetoothInfo> listAdapter = new ArrayAdapter<BluetoothInfo>(
							MainActivity.this,
							android.R.layout.select_dialog_singlechoice, all);
					AlertDialog mDialog = new AlertDialog.Builder(
							MainActivity.this)
							.setTitle("请选择要通讯的设备")
							.setSingleChoiceItems(listAdapter, -1,
									new DialogInterface.OnClickListener() {
										public void onClick(
												final DialogInterface dialog,
												final int whichButton) {
											System.out.println("whichButton:"
													+ whichButton);
											System.out
													.println("keys[whichButton]:"
															+ showAll
																	.get(whichButton)
																	.getName());
											tarDevice = showAll
													.get(whichButton)
													.getDevice();
											System.out.println("device:"
													+ tarDevice);

										}
									})
							.setPositiveButton("sure",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int whichButton) {
											dialog.dismiss();
											commuAsync.execute(tarDevice);
										}
									})
							.setNegativeButton("cancel",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int whichButton) {
											dialog.dismiss();
										}
									}).create();
					mDialog.show();
				} else {
					tarDevice = btDriver.getSocketList().get(0)
							.getRemoteDevice();
					commuAsync.execute(tarDevice);
				}
			}
		});

		button42 = (Button) findViewById(R.id.mainLinear42);
		button42.setText("swipe card");
		button42.setVisibility(View.VISIBLE);
		button42.setOnClickListener(new OnClickListener() {

			BluetoothDevice tarDevice = null;

			@Override
			public void onClick(View v) {
				if (!btDriver.isDeviceConnected()) {
					Toast.makeText(MainActivity.this, "当前无设备连接",
							Toast.LENGTH_SHORT).show();
					return;
				}
				Map<BluetoothDevice, BluetoothSocket> allDevices = btDriver
						.getConnectedInfo();
				if (allDevices == null || allDevices.size() == 0) {
					Toast.makeText(getApplicationContext(), "当前无设备连接",
							Toast.LENGTH_SHORT).show();
					return;
				}
				Log.d(tag, "开启刷卡线程");
				swipeCardAsync = new AsyncTask<Object, Integer, Boolean>() {
					BluetoothDevice targetDevice = null;

					@Override
					protected void onPreExecute() {
						super.onPreExecute();
						textResult.setText("");
					}

					@Override
					protected Boolean doInBackground(Object... params) {
						if (params != null && params.length > 0
								&& params[0] instanceof BluetoothDevice) {
							targetDevice = (BluetoothDevice) params[0];
						}
						byte[] cmd = Utils
								.str2bytes("F0F900000E0000000001001511230918200010");
						byte[] response = new byte[300];
						int resLen = btDriver.transmit(cmd, cmd.length,
								response, 3000l, targetDevice);
						System.out.println("resLen:" + resLen);
						if (resLen > 0) {
							Log.d(tag,
									"收到数据:"
											+ Utils.bytesToHexString(response,
													resLen));
							Log.d(tag, "通讯成功");
							dialogHandler.obtainMessage(
									HandlerMSG.SHOW_COMMON_MSG,
									"通讯成功"
											+ "\nSend:"
											+ Utils.bytesToHexString(cmd)
											+ "\nReceive:"
											+ Utils.bytesToHexString(response,
													resLen)).sendToTarget();
						} else {
							Log.d(tag, "通讯失败");
							dialogHandler.obtainMessage(
									HandlerMSG.SHOW_COMMON_MSG, "通讯失败")
									.sendToTarget();
						}
						return resLen > 0 ? true : false;
					}

					@Override
					protected void onPostExecute(Boolean result) {
						super.onPostExecute(result);
					}

				};
				if (allDevices.size() > 1) {
					List<BluetoothInfo> all = new ArrayList<BluetoothInfo>();
					Set<BluetoothDevice> devices = allDevices.keySet();
					for (BluetoothDevice temp : devices) {
						all.add(new BluetoothInfo(temp.getName(), temp
								.getAddress(), temp));
					}
					final List<BluetoothInfo> showAll = all;
					ArrayAdapter<BluetoothInfo> listAdapter = new ArrayAdapter<BluetoothInfo>(
							MainActivity.this,
							android.R.layout.select_dialog_singlechoice, all);
					AlertDialog mDialog = new AlertDialog.Builder(
							MainActivity.this)
							.setTitle("请选择要通讯的设备")
							.setSingleChoiceItems(listAdapter, -1,
									new DialogInterface.OnClickListener() {
										public void onClick(
												final DialogInterface dialog,
												final int whichButton) {
											System.out.println("whichButton:"
													+ whichButton);
											System.out
													.println("keys[whichButton]:"
															+ showAll
																	.get(whichButton)
																	.getName());
											tarDevice = showAll
													.get(whichButton)
													.getDevice();
											System.out.println("device:"
													+ tarDevice);

										}
									})
							.setPositiveButton("sure",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int whichButton) {
											dialog.dismiss();
											swipeCardAsync.execute(tarDevice);
										}
									})
							.setNegativeButton("cancel",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int whichButton) {
											dialog.dismiss();
										}
									}).create();
					mDialog.show();
				} else {
					tarDevice = btDriver.getSocketList().get(0)
							.getRemoteDevice();
					swipeCardAsync.execute(tarDevice);
				}
			}
		});

		button31 = (Button) findViewById(R.id.mainLinear31);
		button31.setText("disconnect device");
		button31.setVisibility(View.VISIBLE);
		button31.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!btDriver.isDeviceConnected()) {
					Toast.makeText(MainActivity.this, "当前无设备连接",
							Toast.LENGTH_SHORT).show();
					return;
				}
				Map<BluetoothDevice, BluetoothSocket> allDevices = btDriver
						.getConnectedInfo();
				if (allDevices == null || allDevices.size() == 0) {
					Toast.makeText(getApplicationContext(), "当前无设备连接",
							Toast.LENGTH_SHORT).show();
					return;
				}
				Log.d(tag, "开启断开连接线程");
				if (disconnectAsync != null) {
					disconnectAsync.cancel(true);
				}
				disconnectAsync = new AsyncTask<Object, Integer, Boolean>() {
					BluetoothDevice targetDevice = null;
					AlertDialog showDialog;

					@Override
					protected void onPreExecute() {
						super.onPreExecute();
						textResult.setText("");
						if (showDialog == null) {
							showDialog = new AlertDialog.Builder(
									MainActivity.this).setTitle("提示")
									.setMessage("正在断开连接，请稍后！").create();
							showDialog.setCancelable(false);
						}
						showDialog.show();
					}

					@Override
					protected Boolean doInBackground(Object... params) {
						if (params != null && params.length > 0
								&& params[0] instanceof BluetoothDevice) {
							targetDevice = (BluetoothDevice) params[0];
						}
						boolean disconFlag = false;
						if (targetDevice == null) {
							disconFlag = btDriver.disconnect();
						} else {
							disconFlag = btDriver.disconnect(targetDevice);
						}
						if (disconFlag) {
							Log.d(tag, "设备断开成功");
						} else {
							Log.d(tag, "设备断开失败");
						}
						return disconFlag;
					}

					@Override
					protected void onPostExecute(Boolean result) {
						super.onPostExecute(result);
						if (showDialog != null) {
							showDialog.dismiss();
						}
						if (result) {
							dialogHandler.obtainMessage(
									HandlerMSG.SHOW_COMMON_MSG, "设备断开连接")
									.sendToTarget();
						} else {
							dialogHandler.obtainMessage(
									HandlerMSG.SHOW_COMMON_MSG, "连接断开失败")
									.sendToTarget();
						}
					}

				};
				// disconnectAsync.execute();
				disconnDevice = null;
				if (allDevices.size() > 1) {
					List<BluetoothInfo> all = new ArrayList<BluetoothInfo>();
					Set<BluetoothDevice> devices = allDevices.keySet();
					for (BluetoothDevice temp : devices) {
						all.add(new BluetoothInfo(temp.getName(), temp
								.getAddress(), temp));
					}
					final List<BluetoothInfo> showAll = all;
					ArrayAdapter<BluetoothInfo> listAdapter = new ArrayAdapter<BluetoothInfo>(
							MainActivity.this,
							android.R.layout.select_dialog_singlechoice, all);
					AlertDialog mDialog = new AlertDialog.Builder(
							MainActivity.this)
							.setTitle("请选择要断开的设备")
							.setSingleChoiceItems(listAdapter, -1,
									new DialogInterface.OnClickListener() {
										public void onClick(
												final DialogInterface dialog,
												final int whichButton) {
											System.out.println("whichButton:"
													+ whichButton);
											System.out
													.println("keys[whichButton]:"
															+ showAll
																	.get(whichButton)
																	.getName());
											disconnDevice = showAll.get(
													whichButton).getDevice();
											System.out.println("device:"
													+ disconnDevice);

										}
									})
							.setPositiveButton("all",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int whichButton) {
											dialog.dismiss();
											disconnectAsync.execute();
										}
									})
							.setNegativeButton("sure",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int whichButton) {
											System.out.println("device:"
													+ disconnDevice.getName());
											dialog.dismiss();
											disconnectAsync
													.execute(disconnDevice);
										}
									}).create();
					mDialog.show();
				} else {
					disconnectAsync.execute();
				}

			}
		});
		// scrollview
		resultScrollView = (ScrollView) findViewById(R.id.mainSrcollView);
		// 结果显示
		textResult = (TextView) findViewById(R.id.mainTextview1);
		// textResult.setGravity(Gravity.CENTER_VERTICAL);
		// textResult.setGravity(Gravity.CENTER_HORIZONTAL);
		textResult.setText("result display area");
	}
}
