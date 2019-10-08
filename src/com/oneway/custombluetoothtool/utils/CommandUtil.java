package com.oneway.custombluetoothtool.utils;

import java.util.Random;

public class CommandUtil {

	/**
	 * �������ָ�����ȵ�ָ��
	 * 
	 * @param length
	 *            ������ָ���
	 * @return
	 */
	public static byte[] getByteCmd(int length) {
		Random random = new Random();
		int dataLen = length;
		byte[] data = new byte[dataLen];
		for (int j = 0; j < dataLen; j++) {
			data[j] = (byte) random.nextInt(255);
		}
		return data;
	}
	/**
	 * ��byte����ת����16������ɵ��ַ��� ���� һ��byte���� b[0]=0x07;b[1]=0x10;...b[5]=0xFB;
	 * byte2hex(b); ������һ���ַ���"0710BE8716FB"
	 * 
	 * @param bytes
	 *            ��ת����byte����
	 * @return
	 */
	public static String bytesToHexString(byte[] bytes) {
		if (bytes == null) {
			return "";
		}
		StringBuffer buff = new StringBuffer();
		int len = bytes.length;
		for (int j = 0; j < len; j++) {
			if ((bytes[j] & 0xff) < 16) {
				buff.append('0');
			}
			buff.append(Integer.toHexString(bytes[j] & 0xff));
		}
		return buff.toString();
	}

	/**
	 * ָ��ǰ���f0c300xx��ָ��ͷ��xx����ָ��ĳ���
	 * 
	 * @param data
	 *            ָ���
	 * @return
	 */
	public static byte[] buildErrorRateTestCommand(byte[] data) {
		byte[] cmd = new byte[data.length + 5];

		cmd[0] = (byte) 0xf0;
		cmd[1] = (byte) 0xc3;
		cmd[2] = 0x00;
		cmd[3] = 0x00;
		cmd[4] = (byte) data.length;

		for (int i = 0; i < data.length; i++) {
			cmd[5 + i] = data[i];
		}

		return cmd;
	}

	/**
	 * ����������ȵ�������ָ��
	 * 
	 * @param len
	 *            ָ�� ����
	 * @return
	 */
	public static byte[] buildErrorRateTestCommand(int len) {
		byte[] data = getByteCmd(len);
		return buildErrorRateTestCommand(data);
	}
}
