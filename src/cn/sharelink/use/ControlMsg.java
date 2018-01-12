package cn.sharelink.use;

import android.util.Log;

/**
 * 
 * 发送间隔50MS，一次发送6个BYTE，
 * 
 * BYTE[0]:数据头，固定为0x66 BYTE[1]:高四位是炮台左右；左或者右的标识在BYTE3.1，左右各15档；
 * 低四位是炮台上下及打炮，标识在BYTE3.0，各15档； BYTE[2]:高四位是左右，标识BYTE3.6; 低四位是前后；标识BYTE3.5;
 * BYTE[3]:BIT0：炮台左右标识，左是1；右是0； BIT1：炮台上下标识，左是1，右是0； BIT2：打炮按键，有按时为1；没按时为0；
 * BIT3: 机关枪按键，有按时为1；没按时为0； BIT4：开关键按键，有按时为1；没按时为0； BIT5：左右标识，左是1；右是0；
 * BIT6：前后标识，前是1；后是0；
 * BYTE[4]:校验字节=字节1+字节2+字节3+0x0B;如果数据中断：前三个字节发送0x0，第四个字节发送0x0C;
 * BYTE[5]：数据尾，固定为0x99；
 * 
 * 
 */
public class ControlMsg {

	private static final int YAW_MAX = 255;
	private static final int YAW_MIN = 128;

	private static final int CONNON_FOUR_HIGH_MIN = 15;
	private static final int FOUR_MIN = 15;

	private static ControlMsg sControlMsg;
	private byte[] data = new byte[8];

	// 0
	private static final byte head = (byte) 0x66;// MSG 头

	private byte b1;
	private byte b2;
	private byte b3;
	private byte b4;
	private byte b5;
	private byte b6;

	private int speed_limit;

	// 5
	private static final byte tail = (byte) 0x99;// MSG 尾

	/**
	 * 获取ControlMsg的一个对象
	 * 
	 * @return
	 */


	static int a;

	public static ControlMsg getInstance() {
		synchronized (ControlMsg.class) {
			if (sControlMsg == null) {
				sControlMsg = new ControlMsg();
			}
			return sControlMsg;
		}
	}
	
	

	public ControlMsg() {
//		this.a = a;
//		//if (a == 6) {
//
//
//			data[0] = head;
//			b1 = a==6?(byte) 128:(byte)0;
//			b2 = (byte) 128;
//
//			b3 = a==6?(byte) 0:(byte)128;
//			b4 = (byte) 128;
//			b5 = a==6?(byte) 0:(byte)128;
//
//			b6 = a==6?(byte) 128:(byte)0;
//			
//		/*} else {
			
			data[0] = head;
			b1 = (byte) 0;
			b2 = (byte) 128;

			b3 = (byte) 128;
			b4 = (byte) 128;
			b5 = (byte) 128;

			b6 = (byte) 0;
//		}*/

	}

	public void setData() {
		data[0] = head;
		data[1] = b1;
		data[2] = b2;
		/*
		 * data[3] = (byte) (connon_left_right | connon_up_down | connon_btn |
		 * gun_btn | power_btn | left_right | front_back);
		 * 
		 * data[3] = (byte) (connon_up_down * 1 + connon_left_right * 2 +
		 * connon_btn * 4 + gun_btn * 8 + power_btn * 16 + front_back 32 +
		 * left_right * 64);
		 */
		data[3] = b3;
		data[4] = b4;
		data[5] = b5;
		data[6] = b6;
		data[7] = tail;
	}

	/**
	 * @return data数组
	 */
	public byte[] getData() {
		setData();
		return data;
	}

	/**
	 * @param index
	 *            第index个元素
	 * @return data[index]的HEX字符串
	 */
	public String getHexData(int index) {
		return String.format("%02x", data[index]);
	}

	/**
	 * 将data数组转换成HEX字符串
	 * 
	 * @param data间分割符
	 * @return
	 */
	public String getDataHexString(String separator) {
		setData();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < data.length; i++) {
			sb.append(getHexData(i));
			if (i < data.length - 1) {
				sb.append(separator);
			}
		}
		return sb.toString();
	}

	public void setSpeedLimit(int speed_limit) {
		this.speed_limit = speed_limit;
		if (speed_limit == 0) {
			this.b6 = (byte) 3;
		} else if (speed_limit == 1) {
			this.b6 = (byte) 2;
		} else if (speed_limit == 2) {
			this.b6 = (byte) 1;
		}
		// throttle = (byte) ((int)(throttle0 * throttle_limit) & 0xff);
		// Log.i("aa", String.format("%.2f", throttle_limit));
	}

	public int getSpeedLimit() {
		return speed_limit;
	}

	public void setB1(int connon) {
		this.b1 = (byte) (connon);
	}

	public int getB1() {
		return (b1 & 0xff);
	}

	public void setX(float x) {
		this.b2 = (byte) ((byte) (YAW_MIN * x + YAW_MIN) & 0xff);
		if (x < -1.0f) {
			this.b2 = (byte) 0;
		} else if (x > 1.0f) {
			this.b2 = (byte) YAW_MAX;
		}
	}

	public void setY(float y) {
		this.b3 = (byte) ((byte) (YAW_MIN * y + YAW_MIN) & 0xff);
		Log.i("test", "" + YAW_MIN * y + YAW_MIN);
		if (y < -1.0f) {
			this.b3 = (byte) 0;
		} else if (y > 1.0f) {
			this.b3 = (byte) YAW_MAX;
		}
	}
	
	//mode6 设置right 上下
	public void setRY(float y) {
		if (y>0.9f) {
			this.b2=(byte) 168;
			this.b6=(byte) 169;
		}else if(y<-0.9f) {
			this.b2=(byte) 88;
			this.b6=(byte) 88;
		}else{
			this.b2=(byte) 128;
			this.b6=(byte) 128;
		}
	}
	public void setThrottle(float y) {
		if (y>0.9f) {
			this.b3=(byte) 255;
			this.b6=(byte) 127;
		}else if(y<-0.9f) {
			this.b3=(byte) 129;
			this.b6=(byte) 1;
		}else{
			this.b3=(byte) 128;
			this.b6=(byte) 1;
		}
	}
	
	
	//mode6 设置right 左右
		public void setRX(float x) {
			if (x>0.9f) {
				this.b1=(byte) 88;
				this.b2=(byte) 127;
				this.b6=(byte) 167;
			}else if(x<-0.9f) {
				this.b1=(byte) 128;
				this.b2=(byte) 128;
				this.b6=(byte) 128;
			}
		}

	public void setB2(int connon) {
		this.b2 = (byte) (connon);
	}

	public int getB2() {
		return (b2 & 0xff);
	}

	public void setB3(int connon) {
		this.b3 = (byte) (connon);
	}

	public int getB3() {
		return (b3 & 0xff);
	}

	public void setB4(int connon) {
		this.b4 = (byte) (connon);
	}

	public int getB4() {
		return (b4 & 0xff);
	}

	public void setB5(int connon) {
		this.b5 = (byte) (connon);
	}

	public int getB5() {
		return (b5 & 0xff);
	}

	public void setB6(int connon) {
		this.b6 = (byte) (connon);
	}

	public int getB6() {
		return (b6 & 0xff);
	}

}
