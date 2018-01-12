package cn.sharelink.use;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import cn.sharelink.MyApplication;

import android.R;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

/**
 * APP工具类
 */
public class AppUtil {

	public static final String RTSP_URL_nHD = "rtsp://192.168.100.1/cam1/h264-1";
	// public static final String RTSP_URL_720P =
	// "rtsp://192.168.100.1/cam1/h264";

	public static final String APP_PATH = getSDPath()
			+ "/Android/data/com.example.gigacraft";

	public static final String IMG_TYPE = ".jpg";
	public static final String VID_TYPE = ".mp4";

	public static String getCurrentTime() {
		// return new SimpleDateFormat("yyyyMMdd_hhmmss").format(new
		// Date(System.currentTimeMillis())); // 12小时制
		return new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date(System
				.currentTimeMillis())); // 24小时制
	}

	public static String getFilePath() {
		return getSDPath() + "/FlyAPP";
	}

	public static String getImagePath() {
		return getFilePath() + "/snapshot";
	}

	public static String getVideoPath() {
		return getFilePath() + "/video";
	}

	public static String getImageName() {
		return AppUtil.getImagePath() + "/IMG_" + AppUtil.getCurrentTime()
				+ ".jpg";
	}

	public static String getVideoName() {
		return AppUtil.getVideoPath() + "/VID_" + AppUtil.getCurrentTime()
				+ ".mp4";
	}


	public static final String[][] text_dialog_setting_content0 = {
	{ "网络设置", "Configure network", "網絡設置" }, };
	public static final String[][][] text_dialog_setting_content = {
			{ { "影像翻转", "Image to flip", "影像翻轉" }, { "关闭", "Close", "關閉" },{ "开启", "Open", "開啟" } },
			{ { "存储位置", "Stroage Location", "存儲位置" },{ "SD卡", "SD Card", "SD卡" }, { "手机", "Phone", "手機" } }, 
	};
	public static final String[][][] text_dialog_setting_content1 = {
		{ { "影像翻转", "Image to flip", "影像翻轉" }, { "关闭", "Close", "關閉" },{ "开启", "Open", "開啟" } },
		{ { "存储位置", "Stroage Location", "存儲位置" },{ "SD卡", "SD Card", "SD卡" }, { "手机", "Phone", "手機" } }, 
		{ {"操作模式", "Control mode","操作模式"}, {"左手", "Left","左手"}, {"右手", "Right","右手"}}
	};

	public static String getSDPath() {
		boolean hasSDCard = Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
		if (hasSDCard) {
			return Environment.getExternalStorageDirectory().toString();
		} else
			return Environment.getDownloadCacheDirectory().toString();
	}

	public static int s_SpeedChoose;
	public static int s_NoHead;
	public static int s_Language;
	public static int s_ControlMode;
	public static int s_StroageLocation;
	public static int s_FlipImage;

	public static int trim1;
	public static int trim2;
	public static int trim3;

	public static void readDataFile() throws NumberFormatException, IOException {

		File appDir = new File(APP_PATH);
		if (!appDir.exists()) {
			appDir.mkdirs();
		}

		File dataFile = new File(AppUtil.APP_PATH + "/data");

		if (dataFile.exists()) {
			FileInputStream fis = new FileInputStream(dataFile);
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					fis));
			String line;

			while ((line = reader.readLine()) != null) {
				String dat = line.substring(line.indexOf(":") + 1);
				if (line.startsWith("speed")) {

					if (dat.equals("30%")) {
						s_SpeedChoose = 0;
					} else if (dat.equals("60%")) {
						s_SpeedChoose = 1;
					} else if (dat.equals("100%")) {
						s_SpeedChoose = 2;
					} else {
						s_SpeedChoose = 0;
					}
				}

				if (line.startsWith("nohead")) {

					if (dat.equals("CLOSE")) {
						s_NoHead = 0;
					} else if (dat.equals("OPEN")) {
						s_NoHead = 1;
					} else {
						s_NoHead = 0;
					}
				}

				Locale locale = MyApplication.getContext().getResources()
						.getConfiguration().locale;
				String language = locale.getLanguage();
				String country = locale.getCountry();
//				Log.i("AAAAAA", "语言是" + language + country);

				if (language.endsWith("zh")) {
					if (country.endsWith("CH")) {
						s_Language = 0;
					}
					if (country.endsWith("TW")) {
						s_Language = 2;
					}
				} else if (language.endsWith("en")) {
					s_Language = 1;
				}

				// if(line.startsWith("language")) {
				// if(dat.equals("CH")) {
				// s_Language = 0;
				// } else if(dat.equals("EN")) {
				// s_Language = 1;
				// } else {
				// s_Language = 0;
				// }
				// }

				if (line.startsWith("hand")) {
					if (dat.equals("LEFT")) {
						s_ControlMode = 0;
					} else if (dat.equals("RIGHT")) {
						s_ControlMode = 1;
					} else {
						s_ControlMode = 0;
					}
				}

				if (line.startsWith("stroage")) {
					if (dat.equals("SDcard")) {
						s_StroageLocation = 0;
					} else if (dat.equals("Phone")) {
						s_StroageLocation = 1;
					} else {
						s_StroageLocation = 0;
					}
				}

				if (line.startsWith("rotate")) {
					if (dat.equals("0")) {
						s_FlipImage = 1;
					} else if (dat.equals("180")) {
						s_FlipImage = 0;
					} else {
						s_FlipImage = 1;
					}
				}

				if (line.startsWith("trim1")) {
					trim1 = Integer.parseInt(dat);
				}
				if (line.startsWith("trim2")) {
					trim2 = Integer.parseInt(dat);
				}
				if (line.startsWith("trim3")) {
					trim3 = Integer.parseInt(dat);
				}
			}
			reader.close();
			fis.close();
		} else {
			s_SpeedChoose = 0;
			s_Language = 0;
			s_ControlMode = 0;
			s_NoHead = 0;
			s_StroageLocation = 0;
			s_FlipImage = 0;
			trim1 = 32;
			trim2 = 32;
			trim3 = 32;

			writeSetupParameterToFile();
		}
	}

	public static void writeSetupParameterToFile() {

		try {
			File dataFile = new File(AppUtil.APP_PATH + "/data");
			FileOutputStream fos = new FileOutputStream(dataFile, false); // 如果采用追加方式用true
			StringBuffer sb = new StringBuffer();

			if (s_SpeedChoose == 0) {
				sb.append("speed:30%\n");
			} else if (s_SpeedChoose == 1) {
				sb.append("speed:60%\n");
			} else if (s_SpeedChoose == 2) {
				sb.append("speed:100%\n");
			}

			if (s_NoHead == 0) {
				sb.append("nohead:CLOSE\n");
			} else {
				sb.append("nohead:OPEN\n");
			}

			// if(s_Language == 0) {
			// sb.append("language:CH\n");
			// } else {
			// sb.append("language:EN\n");
			// }

			if (s_ControlMode == 0) {
				sb.append("hand:LEFT\n");
			} else {
				sb.append("hand:RIGHT\n");
			}

			if (s_StroageLocation == 0) {
				sb.append("stroage:SDcard\n");
			} else {
				sb.append("stroage:Phone\n");
			}

			if (s_FlipImage == 1) {
				sb.append("rotate:0\n");
			} else {
				sb.append("rotate:180\n");
			}

			sb.append("trim1:" + trim1 + "\n").append("trim2:" + trim2 + "\n")
					.append("trim3:" + trim3 + "\n");

			fos.write(sb.toString().getBytes("UTF8"));
			fos.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void setSetupParameter(int speedChoose, 
			int language, int controlMode,int stroageLocation, int flipImage,
			int trim1, int trim2, int trim3) {

		AppUtil.s_SpeedChoose = speedChoose;
//		AppUtil.s_NoHead = headDirection;
		AppUtil.s_Language = language;
		AppUtil.s_ControlMode = controlMode;
		AppUtil.s_StroageLocation = stroageLocation;
		AppUtil.s_FlipImage = flipImage;
		AppUtil.trim1 = trim1;
		AppUtil.trim2 = trim2;
		AppUtil.trim3 = trim3;

		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				writeSetupParameterToFile();
			}
		}).start();

	}
	public static void setSetupParameter(int speedChoose, 
			int language, int stroageLocation, int flipImage,
			int trim1, int trim2, int trim3) {
		
		AppUtil.s_SpeedChoose = speedChoose;
//		AppUtil.s_NoHead = headDirection;
		AppUtil.s_Language = language;
//		AppUtil.s_ControlMode = controlMode;
		AppUtil.s_StroageLocation = stroageLocation;
		AppUtil.s_FlipImage = flipImage;
		AppUtil.trim1 = trim1;
		AppUtil.trim2 = trim2;
		AppUtil.trim3 = trim3;
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				writeSetupParameterToFile();
			}
		}).start();
		
	}
}
