package cn.sharelink.activity;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import cn.sharelink.use.AppUtil;
import cn.sharelink.use.SetupListViewAdapter;
import cn.sharelink.wificam.R;


public class HomeActivity extends Activity implements OnClickListener {
	
	protected static final String TAG = "HomeActivity";
	public static final int REQUSET = 1;
	private ImageView ivMode1;
	private ImageView ivMode2;
	private ImageView ivMode3;
	private ImageView ivMode4;
	private ImageView ivMode5;
	private ImageView ivMode6;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_home);
		
		
		initeView();
		setLinstener();
		Log.e(TAG, "onCreate");
		
		//帮助按钮，用于打开帮助
		findViewById(R.id.btn_help).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(HomeActivity.this, HelpAcitivity.class);
				startActivity(intent);
			}
		});

		// WiFi按钮设置监听，用于设置WiFi
		findViewById(R.id.btn_setting0).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				View layout_setup = LayoutInflater.from(HomeActivity.this)
						.inflate(R.layout.dialog_setup, null);
				ListView listView_setup = (ListView) layout_setup
						.findViewById(R.id.listview_setup);
				final SetupListViewAdapter setupListViewAdapter = new SetupListViewAdapter(
						HomeActivity.this, AppUtil.s_Language, new int[] { 
								AppUtil.s_FlipImage, 
								AppUtil.s_StroageLocation });

				listView_setup.setAdapter(setupListViewAdapter);

				final AlertDialog setupDialog = new AlertDialog.Builder(HomeActivity.this,AlertDialog.THEME_DEVICE_DEFAULT_DARK)
				.setTitle(getResources().getString(R.string.setting))				
				.setView(layout_setup)
				.setPositiveButton(getResources().getString(R.string.ok),
						new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog,
							int which) {
						int len = setupListViewAdapter.arrayList.size();

						int[] nums = new int[len];
						for(int i = 1; i < len; i++) {
							SetupListViewAdapter.SetupItem setupItem = setupListViewAdapter.arrayList.get(i);
							nums[i] = setupItem.getSetupNum();
							Log.i(TAG, setupItem.toString());
						}

						AppUtil.s_StroageLocation = nums[len - 1];
						AppUtil.s_FlipImage = nums[len - 2];
						AppUtil.writeSetupParameterToFile();
					}
				})
				.setNegativeButton(getResources().getString(R.string.cancel), null)
				.show();

				listView_setup.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						// TODO Auto-generated method stub
						if(position == 0) {
							setupDialog.dismiss();
							setNetwork();
						}
					}

				});
			}
		});

		// file按钮设置监听，用于查看截图和录像的文件
		findViewById(R.id.btn_file).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(HomeActivity.this, FileActivity.class);
				//				Intent intent = new Intent(HomeActivity.this, SetupActivity.class);
				startActivity(intent);
			}
		});

		pathIsExist();

		try {
			AppUtil.readDataFile();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	 

	private void initeView() {
		ivMode1= (ImageView) findViewById(R.id.iv_mode1);
		ivMode2= (ImageView) findViewById(R.id.iv_mode2);
		ivMode3= (ImageView) findViewById(R.id.iv_mode3);
		ivMode4= (ImageView) findViewById(R.id.iv_mode4);
		ivMode5= (ImageView) findViewById(R.id.iv_mode5);
		ivMode6= (ImageView) findViewById(R.id.iv_mode6);

	}


	private void setLinstener() {
		ivMode1.setOnClickListener(this);
		ivMode2.setOnClickListener(this);
		ivMode3.setOnClickListener(this);
		ivMode4.setOnClickListener(this);
		ivMode5.setOnClickListener(this);
		ivMode6.setOnClickListener(this);

	}


	/**
	 * VideoPlayerActivity中无法连接到摄像头时，调用该函数
	 * 弹出是否需要设置WiFi的对话框
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUSET && resultCode == -1) {  
			dialog_setNetwork();
		}
	}


	/**
	 * 检测网络状态
	 * @return true   连接的是WiFi
	 *         false  连接的是移动数据或者没有连接上网络
	 */
	private boolean isWifiConnected() {

		boolean flag = false;
		WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE); 
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		String ssid = wifiInfo.getSSID().toLowerCase();
		Log.d(TAG, ssid);
		if(ssid.startsWith("skycam", 1) || ssid.startsWith("Innocam", 1)) {
			flag = true;
		} else {
			flag = false;
		}
		//		} 

		return flag;
	}

	/**
	 * 连接网络失败弹出的dialog
	 */
	private void dialog_setNetwork() {
		int i = AppUtil.s_Language;

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
//		builder.setTitle(AppUtil.text_dialog1_tittle[i]);
		builder.setTitle(getResources().getString(R.string.wiFi_is_disable));
//		builder.setMessage(AppUtil.text_dialog1_info[i]);
		builder.setMessage(getResources().getString(R.string.please_enable_wiFi_and_connect_to_specified_SSID));
//		builder.setPositiveButton(AppUtil.text_dialog1_confirm[i],
		builder.setPositiveButton(getResources().getString(R.string.ok),
				new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				setNetwork();
			}
		});
//		builder.setNegativeButton(AppUtil.text_dialog1_cancel[i], null);
		builder.setNegativeButton(getResources().getString(R.string.cancel), null);
		builder.create();
		builder.show();
	}

	/**
	 * 网络未连接时，调用设置方法
	 */
	private void setNetwork() {

		Intent intent = null;
		/**
		 * 判断手机系统的版本！如果API大于10 就是3.0+ 因为3.0以上的版本的设置和3.0以下的设置不一样，调用的方法不同
		 */
		if (android.os.Build.VERSION.SDK_INT > 10) {
			intent = new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
		} else {
			intent = new Intent();
			ComponentName component = new ComponentName("com.android.settings",
					"com.android.settings.WirelessSettings");
			intent.setComponent(component);
			intent.setAction("android.intent.action.VIEW");
		}
		startActivity(intent);
	}

	@Override
	protected void onDestroy() {
		System.exit(0);
		super.onDestroy();
	}
	/**
	 * 路径是否存在 不存在则创建
	 */
	private void pathIsExist() {
		File file = new File(AppUtil.getImagePath());
		if (!file.exists()) {
			file.mkdirs();
		}

		File file1 = new File(AppUtil.getVideoPath());
		if (!file1.exists()) {
			file1.mkdirs();
		}
	}


	@Override
	public void onClick(View v) {
        Intent intent;
        intent =new Intent(HomeActivity.this,PlayActivity.class);
        switch (v.getId())
        {
            case R.id.iv_mode1:
                intent.putExtra("Mode", "model1");
                startActivity(intent);
                break;
            case R.id.iv_mode2:
                intent.putExtra("Mode", "model2");
                startActivity(intent);
                break;
            case R.id.iv_mode3:
                intent.putExtra("Mode", "model3");
                startActivity(intent);
                break;
            case R.id.iv_mode4:
                intent.putExtra("Mode", "model4");
                startActivity(intent);
                break;
            case R.id.iv_mode5:
                intent.putExtra("Mode", "model5");
                startActivity(intent);
                break;
            case R.id.iv_mode6:
                intent.putExtra("Mode", "model6");
                startActivity(intent);
                break;

        }		
	}
}
