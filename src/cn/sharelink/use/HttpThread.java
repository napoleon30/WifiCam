package cn.sharelink.use;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.AES.AESCrypto;

/**
 * @author ChenJun 处理HTTP通信的线程类
 * 
 */
public class HttpThread extends Thread {

	private static final String TAG = "HTTP_Thread";

	public static final int HTTP_START = 0;
	public static final int HTTP_SET_TIME = 1;
	public static final int HTTP_CHECK_STROAGE = 2;
	public static final int HTTP_BRIDGE = 3;
	public static final int HTTP_TAKEPHOTO = 4;
	public static final int HTTP_START_RECORD = 5;
	public static final int HTTP_STOP_RECORD = 6;
	public static final int HTTP_GET_PRIVILEGE = 7;
	public static final int HTTP_RELEASE_PRIVILEGE = 8;
	public static final int HTTP_FLIP_MIRROR_IMAGE = 9;

	private static final String http_url = "http://192.168.100.1:80";
	private static final String authcode_str = "&authcode=";
	private static final String getPrivilege_cmd = "/server.command?command=get_privilege";
	private static final String releasePrivilege_cmd = "/server.command?command=release_privilege"
			+ authcode_str;
	private static final String setDate_cmd = "/server.command?command=set_date&tz=GMT-8:00&date=";
	private static final String checkStroage_cmd = "/server.command?command=check_storage";
	private static final String snapshot_cmd = "/server.command?command=snapshot&pipe=0"
			+ authcode_str;
	private static final String isRecord_cmd = "/server.command?command=is_pipe_record";
	private static final String startRecord_cmd = "/server.command?command=start_record_pipe&type=h264&pipe=0"
			+ authcode_str;
	private static final String stopRecord_cmd = "/server.command?command=stop_record&type=h264&pipe=0"
			+ authcode_str;
	private static final String flipOrMirror_cmd = "/server.command?command=set_flip_mirror&value=";
	private static final String bridge1_cmd = "/server.command?command=bridge&type=0&value=";
	private static final String bridge2_cmd = authcode_str;
	public boolean isRun;
	public int cmd_index;
	private int value;
	private int mode;

	private Handler handler;

	public HttpThread(int cmd_index, Handler handler) {
		// TODO Auto-generated constructor stub
		this.cmd_index = cmd_index;
		this.handler = handler;
	}

	public HttpThread(int cmd_index, int value, Handler handler) {
		// TODO Auto-generated constructor stub
		this.cmd_index = cmd_index;
		this.handler = handler;
		this.value = value;
	}

	public HttpThread(int cmd_index, int value, int mode, Handler handler) {
		// TODO Auto-generated constructor stub
		this.cmd_index = cmd_index;
		this.handler = handler;
		this.value = value;
		this.mode = mode;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			switch (cmd_index) {
			case HTTP_START:
				do_setDate(); // 设置时间
				do_getPrivilege(); // 获取权限
				do_flipOrMirror(value);
				do_checkStorage(); // 检测SD卡是否存在
				do_bridge(mode); // 不断的发送控制数据
				break;

			case HTTP_TAKEPHOTO:
				do_takePhoto(); // 发送拍照
				break;

			case HTTP_START_RECORD:
				do_startRecord(); // 发送开始录像
				break;

			case HTTP_STOP_RECORD: // 停止录像
				do_stopRecord();
				break;

			case HTTP_RELEASE_PRIVILEGE: // 释放控制权限
				do_releasePrivilege();
				break;
			case HTTP_FLIP_MIRROR_IMAGE:
				do_flipOrMirror(value);
				break;
			default:
				break;
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	static String sAuthcode = null;

	/**
	 * 获取控制权限
	 */
	void do_getPrivilege() throws Exception {
		String strResult;
		int value = -1;
		HttpGet getMethod = new HttpGet(http_url + getPrivilege_cmd);
		HttpClient httpClient = new DefaultHttpClient();
		HttpResponse httpResponse = httpClient.execute(getMethod);
		/* 若状态码为200 ok */
		if (httpResponse.getStatusLine().getStatusCode() == 200) {
			/* 读返回数据 */
			HttpEntity httpEntity = httpResponse.getEntity();
			InputStream inputStream = httpEntity.getContent();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					inputStream));
			String line = reader.readLine();
			JSONObject jsonObj = new JSONObject(line);
			value = jsonObj.getInt("value");
			if (value == 0) {
				line = reader.readLine();
				jsonObj = new JSONObject(line);
				String nonce = jsonObj.getString("nonce");
				sAuthcode = AESCrypto.getAuthcode(nonce);
				Log.i("do_getPrivilege", "nonce:" + nonce);
				// Log.i("do_getPrivilege", "encrypt:" +
				// AESCrypto.encrypt(nonce));
				Log.i("do_getPrivilege", "Authcode:" + sAuthcode);
			}
		} else {
			strResult = httpResponse.getStatusLine().toString();
		}

		Message msg = handler.obtainMessage(HTTP_GET_PRIVILEGE, value);
		Bundle bundle = new Bundle();
		bundle.putString("authcode", sAuthcode);
		msg.setData(bundle);
		handler.sendMessage(msg);
	}

	void do_releasePrivilege() throws ClientProtocolException, IOException,
			JSONException {
		String strResult;
		if (sAuthcode == null) {
			return;
		}
		int value = -1;
		String url = http_url + releasePrivilege_cmd + sAuthcode;
		Log.i("do_releasePrivilege", url);
		HttpGet getMethod = new HttpGet(url);
		HttpClient httpClient = new DefaultHttpClient();

		/* 发送请求并等待响应 */
		HttpResponse httpResponse = httpClient.execute(getMethod);
		/* 若状态码为200 ok */
		if (httpResponse.getStatusLine().getStatusCode() == 200) {
			/* 读返回数据 */
			strResult = EntityUtils.toString(httpResponse.getEntity());
			value = JSON_getValue(strResult);
		} else {
			strResult = httpResponse.getStatusLine().toString();
		}

		Log.i("do_releasePrivilege", strResult);
		handler.obtainMessage(HTTP_RELEASE_PRIVILEGE, value).sendToTarget();
	}

	void do_setDate() throws ClientProtocolException, IOException,
			JSONException {
		String strResult;
		int ret = -1;
		SimpleDateFormat df = new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss");
		String time = df.format(new Date());
		HttpGet getMethod = new HttpGet(http_url + setDate_cmd + time);
		HttpClient httpClient = new DefaultHttpClient();
		Log.e("test", "do_setDate()");

		/* 发送请求并等待响应 */
		HttpResponse httpResponse = httpClient.execute(getMethod);
		/* 若状态码为200 ok */
		if (httpResponse.getStatusLine().getStatusCode() == 200) {
			/* 读返回数据 */
			strResult = EntityUtils.toString(httpResponse.getEntity());
			ret = JSON_getValue(strResult);
		} else {
			strResult = httpResponse.getStatusLine().toString();
		}

		Log.i("do_setDate", strResult);
		handler.obtainMessage(HTTP_SET_TIME, ret).sendToTarget();
	}

	void do_checkStorage() throws ClientProtocolException, IOException,
			JSONException {
		String strResult;
		int value = -1;
		HttpGet getMethod = new HttpGet(http_url + checkStroage_cmd);
		HttpClient httpClient = new DefaultHttpClient();

		Log.e("test", "do_checkStorage()");
		/* 发送请求并等待响应 */
		HttpResponse httpResponse = httpClient.execute(getMethod);
		/* 若状态码为200 ok */
		if (httpResponse.getStatusLine().getStatusCode() == 200) {
			/* 读返回数据 */
			strResult = EntityUtils.toString(httpResponse.getEntity());
			value = JSON_getValue(strResult);
		} else {
			strResult = httpResponse.getStatusLine().toString();
		}

		Log.i("do_checkStorage", strResult);
		handler.obtainMessage(HTTP_CHECK_STROAGE, value).sendToTarget();

	}

	void do_bridge(int a) throws ClientProtocolException, IOException,
			JSONException, InterruptedException {
		if (sAuthcode == null) {
			return;
		}
		// Log.e("test", "do_bridge()");

		String strResult;
		HttpClient httpClient = new DefaultHttpClient();
		HttpGet getMethod;
		HttpResponse httpResponse;
		// RequestConfig requestConfig =
		// RequestConfig.custom().setSocketTimeout(200).setConnectTimeout(200).build();//设置请求和传输超时时间
		isRun = true;
		ControlMsg6	mCtlMsg6 = null;
		ControlMsg mCtlMsg = null;
		
		if (mode == 6) {
			mCtlMsg6 = ControlMsg6.getInstance();
		}else{
			mCtlMsg= ControlMsg.getInstance();
		}
		String sendData = null;
		
		while (isRun) {
			int value = -1;
			long time0 = System.currentTimeMillis();
			if(mode == 6){
				sendData = mCtlMsg6.getDataHexString("_");
			}else{
				
				sendData = mCtlMsg.getDataHexString("_");
			}
			
			Log.e("test", "do_bridge():  " +a+"  :" +sendData);

			getMethod = new HttpGet(http_url + bridge1_cmd + sendData
					+ bridge2_cmd + sAuthcode);
			long time1 = System.currentTimeMillis();
			/* 发送请求并等待响应 */
			httpResponse = httpClient.execute(getMethod);
			long time2 = System.currentTimeMillis();
			// Log.i("time", String.valueOf(time2-time1));
			/* 若状态码为200 ok */
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				/* 读返回数据 */
				strResult = EntityUtils.toString(httpResponse.getEntity());
				value = JSON_getValue(strResult);
			} else {
				strResult = httpResponse.getStatusLine().toString();
			}
			long curtime = System.currentTimeMillis();
			// Log.i("time", (curtime - time0) + "/" + (curtime - time1) + "/" +
			// (curtime - time2));

			Message msg = handler.obtainMessage(HTTP_BRIDGE, value);
			Bundle bundle = new Bundle();
			bundle.putString("send", sendData.replace('_', ' '));
			msg.setData(bundle);
			handler.sendMessage(msg);
			// handler.obtainMessage(HTTP_BRIDGE, value).sendToTarget();
			// Thread.sleep(10);
		}
	}

	void do_takePhoto() throws ClientProtocolException, IOException,
			JSONException {
		if (sAuthcode == null) {
			return;
		}
		Log.e("test", "do_takePhoto()");

		String strResult;
		int value = -1;
		String url = http_url + snapshot_cmd + sAuthcode;
		Log.i("do_takePhoto", url);
		HttpGet getMethod = new HttpGet(url);
		HttpClient httpClient = new DefaultHttpClient();

		/* 发送请求并等待响应 */
		HttpResponse httpResponse = httpClient.execute(getMethod);
		/* 若状态码为200 ok */
		if (httpResponse.getStatusLine().getStatusCode() == 200) {
			/* 读返回数据 */
			strResult = EntityUtils.toString(httpResponse.getEntity());
			value = JSON_getValue(strResult);
		} else {
			strResult = httpResponse.getStatusLine().toString();
		}

		Log.i("do_takePhoto", strResult);
		handler.obtainMessage(HTTP_TAKEPHOTO, value).sendToTarget();
	}

	void do_startRecord() throws ClientProtocolException, IOException,
			JSONException {
		if (sAuthcode == null) {
			return;
		}
		Log.e("test", "do_startRecord()");
		String strResult;
		int value = -1;

		HttpClient httpClient = new DefaultHttpClient();
		HttpGet getMethod = new HttpGet(http_url + isRecord_cmd);
		/* 发送请求并等待响应 */
		HttpResponse httpResponse = httpClient.execute(getMethod);
		/* 若状态码为200 ok */
		if (httpResponse.getStatusLine().getStatusCode() == 200) {
			/* 读返回数据 */
			strResult = EntityUtils.toString(httpResponse.getEntity());
			value = JSON_getValue(strResult);
		} else {
			strResult = httpResponse.getStatusLine().toString();
		}
		Log.i("do_startRecord1", strResult);

		if (value == 1) { // 表示正在录制
			handler.obtainMessage(HTTP_START_RECORD, value).sendToTarget();
			return;
		}

		getMethod = new HttpGet(http_url + startRecord_cmd + sAuthcode);
		httpResponse = httpClient.execute(getMethod);
		if (httpResponse.getStatusLine().getStatusCode() == 200) {
			/* 读返回数据 */
			strResult = EntityUtils.toString(httpResponse.getEntity());
			value = JSON_getValue(strResult);
		} else {
			strResult = httpResponse.getStatusLine().toString();
		}

		handler.obtainMessage(HTTP_START_RECORD, value).sendToTarget();

		Log.i("do_startRecord2", strResult);
	}

	void do_stopRecord() throws ClientProtocolException, IOException,
			JSONException {
		if (sAuthcode == null) {
			return;
		}
		Log.e("test", "do_stopRecord()");
		String strResult;
		int value = -1;

		HttpClient httpClient = new DefaultHttpClient();
		HttpGet getMethod = new HttpGet(http_url + isRecord_cmd);
		/* 发送请求并等待响应 */
		HttpResponse httpResponse = httpClient.execute(getMethod);
		/* 若状态码为200 ok */
		if (httpResponse.getStatusLine().getStatusCode() == 200) {
			/* 读返回数据 */
			strResult = EntityUtils.toString(httpResponse.getEntity());
			value = JSON_getValue(strResult);
		} else {
			strResult = httpResponse.getStatusLine().toString();
		}
		Log.i("do_stopRecord1", strResult);

		if (value == 0) { // 表示没有录制
			handler.obtainMessage(HTTP_STOP_RECORD, value).sendToTarget();
			return;
		}

		getMethod = new HttpGet(http_url + stopRecord_cmd + sAuthcode);
		httpResponse = httpClient.execute(getMethod);
		if (httpResponse.getStatusLine().getStatusCode() == 200) {
			/* 读返回数据 */
			strResult = EntityUtils.toString(httpResponse.getEntity());
			value = JSON_getValue(strResult);
		} else {
			strResult = httpResponse.getStatusLine().toString();
		}

		handler.obtainMessage(HTTP_STOP_RECORD, value).sendToTarget();

		Log.i("do_stopRecord2", strResult);

	}

	void do_flipOrMirror(int value) throws ClientProtocolException,
			IOException, JSONException {
		if (sAuthcode == null) {
			return;
		}

		Log.e("test", "do_flipOrMirror()");
		String strResult;
		int ret = -1;

		StringBuffer sb = new StringBuffer();
		sb.append(http_url).append(flipOrMirror_cmd).append(value)
				.append(authcode_str).append(sAuthcode);
		Log.e("====================", sb + "");

		HttpGet getMethod = new HttpGet(sb.toString());
		HttpClient httpClient = new DefaultHttpClient();

		/* 发送请求并等待响应 */
		HttpResponse httpResponse = httpClient.execute(getMethod);
		/* 若状态码为200 ok */
		if (httpResponse.getStatusLine().getStatusCode() == 200) {
			/* 读返回数据 */
			strResult = EntityUtils.toString(httpResponse.getEntity());
			ret = JSON_getValue(strResult);
		} else {
			strResult = httpResponse.getStatusLine().toString();
		}

		handler.obtainMessage(HTTP_FLIP_MIRROR_IMAGE, ret).sendToTarget();

		Log.i("do_flipOrMirror", strResult);

	}

	int JSON_getValue(String strResult) throws JSONException {
		JSONObject jsonObj = new JSONObject(strResult);
		int value = jsonObj.getInt("value");
		return value;
	}
}