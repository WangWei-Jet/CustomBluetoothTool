package com.oneway.custombluetoothtool.utils;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

import com.oneway.custombluetoothtool.R;

public class LoadingDialog extends Dialog {

	private TextView tv;

	public LoadingDialog(Context context) {
		super(context, R.style.loadingdialog);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// dialog show��ʱ�򱻵���
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog_loading);
		tv = (TextView) this.findViewById(R.id.tv);
		// tv.setText("���Ժ�...");
	}

	public void setContent(String showMSG) {
		tv.setText(showMSG);
	}
}
