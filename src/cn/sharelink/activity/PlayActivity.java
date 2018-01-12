package cn.sharelink.activity;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import cn.sharelink.MyApplication;
import cn.sharelink.use.AppUtil;
import cn.sharelink.use.ControlMsg;
import cn.sharelink.use.ControlMsg6;
import cn.sharelink.use.HttpThread;
import cn.sharelink.use.PlayVoice;
import cn.sharelink.use.RoatAnimUtil;
import cn.sharelink.use.SetupListViewAdapter;
import cn.sharelink.view.MenuButton;
import cn.sharelink.view.MyToast;
import cn.sharelink.view.RockerView;
import cn.sharelink.view.RtspVideoView;
import cn.sharelink.view.TrimView;
import cn.sharelink.view.MenuButton.MenuButtonOnClickListener;
import cn.sharelink.wificam.R;

/**
 * @author ChenJun video显示、飞行控制的Activity
 * 
 */
public class PlayActivity extends Activity implements OnTouchListener,
		OnClickListener {

	private final static String TAG = "DEBUG/VideoPlayerActivity";
	int mode = 0;
	// HTTP通信的控制参数
	private static final int HTTP_START = HttpThread.HTTP_START;
	private static final int HTTP_SET_TIME = HttpThread.HTTP_SET_TIME;
	private static final int HTTP_CHECK_STROAGE = HttpThread.HTTP_CHECK_STROAGE;
	private static final int HTTP_BRIDGE = HttpThread.HTTP_BRIDGE;
	private static final int HTTP_TAKEPHOTO = HttpThread.HTTP_TAKEPHOTO;
	private static final int HTTP_START_RECORD = HttpThread.HTTP_START_RECORD;
	private static final int HTTP_STOP_RECORD = HttpThread.HTTP_STOP_RECORD;
	private static final int HTTP_GET_PRIVILEGE = HttpThread.HTTP_GET_PRIVILEGE;
	private static final int HTTP_RELEASE_PRIVILEGE = HttpThread.HTTP_RELEASE_PRIVILEGE;
	// 各个窗口的layout定义
	private RelativeLayout mLayoutView_menu; // 菜单栏的layout
	private RelativeLayout mLayoutView_screen; // 屏幕, 用于监听触摸屏幕

	private FrameLayout playView;

	// mLayoutView_videoView中的控件
	private RtspVideoView mVideoView;

	// mLayoutView_menu中的控件
	private MenuButton mBtn_exit; // 退出按钮
	private MenuButton mBtn_snapShot; // 截图按钮
	private MenuButton mBtn_record; // 录像按钮
	private MenuButton mBtn_playback; // 查看本地文件按钮
	private MenuButton mBtn_SDRecord; // 远程SD卡录像按钮
	private MenuButton mBtn_speedChoose; // 速度选择
	private MenuButton mBtn_lock; // 锁定按钮
	private MenuButton mBtn_gravity; // 重力感应控制按钮
	private MenuButton mBtn_offControl; // 关闭控制界面

	// private MenuButton mBtn_landing; // 一键翻滚按钮
	private MenuButton mBtn_setting; // 设置按钮

	private Button total;
	private Button one_start; // 一键起飞按钮
	private Button one_land; // 一键着陆按钮
	private Button one_stop; // 紧急按钮
	private MenuButton one_highlimit; // 定高按钮
	private MenuButton one_nohead; // 一键着陆按钮
	private Button one_roll; // 一键翻转按钮
	private Button one_balance; // 一键平衡按钮

	private boolean isSDRecording = false; // 正在录像
	private boolean isRecording = false; // 正在本地录像
	private boolean isStartRecord = false; // 已启动本地录像
	private long mSDRecord_startTime = 0; // 录像开始时间
	private long mRecord_startTime = 0; // 本地录像开始时间

	private RockerView mode_rock;
	private RockerView model6_rocker_left;
	private RockerView model6_rocker_right;
	
	private LinearLayout model6_all; // 模式六的按钮
	private RadioGroup radioGroup_grade;
	private RadioButton grade_1;
	private RadioButton grade_2;
	private RadioButton grade_3;
	private RadioButton grade_4;
	public static int radio_checked = 1;

	private View playModelView;

	private MyToast mToast; // 定义Toast

	public ControlMsg mCtlMsg = null; // 飞控控制数据
	public ControlMsg6 mCtlMsg6 = null; // 飞控控制数据

	private boolean haveSDcard = false; // 摄像头模块有无SDcard

	private ListenRecordThread listenRecordThread = new ListenRecordThread(); // 监听录像线程
	private HttpThread bridgeThread = null; // HTTP桥接线程

	private String mAuthcode; // 控制权限的数据

	private boolean isHideAllView = false;
	private boolean isCountDown_HideAllView = true; // 可以倒计时
	private static final int s_TotalTime_HideAllView = 60;
	private int mTime_HideAllView = s_TotalTime_HideAllView;
	private int mLanguage;
	private boolean isOpenControl = true;
	private int mControlMode;
	private int mFlipImage;
	private int mStroageLocaltion;

	private boolean Flag = false;
	private boolean Flag2 = false;

	private PlayVoice mPlayVoice;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
		// | View.SYSTEM_UI_FLAG_IMMERSIVE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// 设置全屏 , 屏幕长亮

		setContentView(R.layout.activity_play);
		Log.i("AAAAA", "This is create");
		initView();
		// Log.e(TAG, "onCreate");
		mToast = new MyToast(this);
		mPlayVoice = new PlayVoice(this);

		/********* mLayoutView_videoView 中的控件 ***********/
		mVideoView = (RtspVideoView) findViewById(R.id.rtsp_videoView);

		/********* layout_menu 中的控件 ***********/
		mLayoutView_menu = (RelativeLayout) findViewById(R.id.layoutView_menu);
		mBtn_exit = (MenuButton) findViewById(R.id.btn_exit);
		mBtn_snapShot = (MenuButton) findViewById(R.id.btn_snapshot);
		mBtn_record = (MenuButton) findViewById(R.id.btn_record);
		mBtn_playback = (MenuButton) findViewById(R.id.btn_playback);
		mBtn_SDRecord = (MenuButton) findViewById(R.id.btn_sdRecord);
		mBtn_speedChoose = (MenuButton) findViewById(R.id.btn_speedChoose);
		// mBtn_lock = (MenuButton) findViewById(R.id.btn_lock);
		mBtn_gravity = (MenuButton) findViewById(R.id.btn_gravity);
		mBtn_offControl = (MenuButton) findViewById(R.id.btn_offControl);
		mBtn_setting = (MenuButton) findViewById(R.id.btn_setting);

		playModelView = findViewById(R.id.layoutView_screen);

		model6_all = (LinearLayout) findViewById(R.id.ll_control);// 模式六的按钮
		radioGroup_grade = (RadioGroup) findViewById(R.id.radioGroup_grade);
		grade_1 = (RadioButton) findViewById(R.id.radio0);
		grade_2 = (RadioButton) findViewById(R.id.radio1);
		grade_3 = (RadioButton) findViewById(R.id.radio2);
		grade_4 = (RadioButton) findViewById(R.id.radio3);
		radioGroup_grade.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if(checkedId == grade_1.getId()){
					radio_checked = 1;
				}else if(checkedId == grade_2.getId()){
					radio_checked = 2;
				}else if(checkedId == grade_3.getId()){
					radio_checked =3;
				}else if(checkedId == grade_4.getId()){
					radio_checked = 4;
					
				}
				Log.e("radio_checked", radio_checked+"");
				
			}
		});

		playView = (FrameLayout) findViewById(R.id.play_activity);
		// 设置监听
		mBtn_exit.setMenuOnClickListener(new MyMenuOnClickListener());
		mBtn_snapShot.setMenuOnClickListener(new MyMenuOnClickListener());
		mBtn_record.setMenuOnClickListener(new MyMenuOnClickListener());
		mBtn_playback.setMenuOnClickListener(new MyMenuOnClickListener());
		mBtn_SDRecord.setMenuOnClickListener(new MyMenuOnClickListener());
		mBtn_speedChoose.setMenuOnClickListener(new MyMenuOnClickListener());
		mBtn_gravity.setMenuOnClickListener(new MyMenuOnClickListener());
		mBtn_offControl.setMenuOnClickListener(new MyMenuOnClickListener());
		// mBtn_landing.setMenuOnClickListener(new MyMenuOnClickListener());
		// mBtn_rolling.setMenuOnClickListener(new MyMenuOnClickListener());
		mBtn_setting.setMenuOnClickListener(new MyMenuOnClickListener());

		if (mode == 6) {
			mCtlMsg6 = ControlMsg6.getInstance();
		} else {
			mCtlMsg = ControlMsg.getInstance();
		}

		mLanguage = AppUtil.s_Language;
		mControlMode = AppUtil.s_ControlMode;
		mFlipImage = AppUtil.s_FlipImage;

		mStroageLocaltion = AppUtil.s_StroageLocation;

		mVideoView.rotate(mFlipImage == 3);
		mBtn_speedChoose.setSpeed(AppUtil.s_SpeedChoose);

		bridgeThread = new HttpThread(HTTP_START, mFlipImage, mode,
				HTTP_handler);

		if (isWifiConnected()) {
			mVideoView.setVideo(AppUtil.RTSP_URL_nHD, videoEventHandler);

			// 创建并开启bridgeThread线程
			// bridgeThread = new HttpThread(HTTP_START, HTTP_handler);
			bridgeThread.start(); // 获取控制权限-->设置时间-->检测SD卡-->发送控制数据

			listenRecordThread.start(); // 开启录像监听线程

		}

		changeRecordLocaltion();
		playView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				mTime_HideAllView = s_TotalTime_HideAllView;

				if (isHideAllView) {
					isHideAllView = false;

					setViewVisibility(mLayoutView_menu, View.VISIBLE);
					setViewVisibility(playModelView, View.VISIBLE);
					if (mode == 6) {
						setViewVisibility(model6_all, View.VISIBLE);
						
					}
					if (!mBtn_offControl.isChecked()) {
					}
					return true; // 避免显现其他View时的触摸操作
				} else {
					return false;
				}
			}
		});
	}

	private boolean isWifiConnected() {

		boolean flag = false;
		WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		String ssid = wifiInfo.getSSID();
		Log.d("JNI", "this is wificheck" + ssid);
		if (ssid.startsWith("Skycam", 1) || ssid.startsWith("Skycam")
				|| ssid.startsWith("TANK") || ssid.startsWith("TANK", 1)) {
			flag = true;
		} else {
			flag = false;
		}

		return flag;
	}

	private void initView() {
		Intent intent = getIntent();
		String flag = intent.getStringExtra("Mode");
		Log.d(TAG, "Flag : " + flag);
		switch (flag) {
		case "model1":
			mode = 1;
			Log.d(TAG, "我选的是模式1--------------------");
			break;
		case "model2":
			mode = 2;
			Log.d(TAG, "我选的是模式2--------------------");
			break;
		case "model3":
			mode = 3;
			Log.d(TAG, "我选的是模式3--------------------");
			break;
		case "model4":
			mode = 4;
			Log.d(TAG, "我选的是模式4--------------------");
			break;
		case "model5":
			mode = 5;
			Log.d(TAG, "我选的是模式5--------------------");
			break;
		case "model6":
			mode = 6;
			Log.d(TAG, "我选的是模式6--------------------");
			break;
		}
		switchMode(mode);
	}

	private void switchMode(int mode) {
		setModle(mode);
		switch (mode) {
		case 1:
			View model1View = getControlViewByMode();
			mode_rock = (RockerView) model1View
					.findViewById(R.id.rockerview_model1);
			mode_rock.setRockerChangeListener(new MyRockerChangeListener());

			model1View.findViewById(R.id.btn_mode1_lup)
					.setOnTouchListener(this);
			model1View.findViewById(R.id.btn_mode1_ldown).setOnTouchListener(
					this);
			model1View.findViewById(R.id.btn_mode1_rup)
					.setOnTouchListener(this);
			model1View.findViewById(R.id.btn_mode1_rdown).setOnTouchListener(
					this);
			break;
		case 2:
			View model2View = getControlViewByMode();
			mode_rock = (RockerView) model2View
					.findViewById(R.id.rockerview_model2);
			mode_rock.setRockerChangeListener(new MyRockerChangeListener());

			model2View.findViewById(R.id.ibtn_mode2_right).setOnTouchListener(
					this);
			model2View.findViewById(R.id.ibtn_mode2_up)
					.setOnTouchListener(this);
			model2View.findViewById(R.id.ibtn_mode2_down).setOnTouchListener(
					this);
			model2View.findViewById(R.id.ibtn_mode2_left).setOnTouchListener(
					this);

			model2View.findViewById(R.id.iv_mode2_cannon).setOnTouchListener(
					this);
			model2View.findViewById(R.id.iv_mode2_guns)
					.setOnTouchListener(this);
			model2View.findViewById(R.id.iv_mode2_btn3)
					.setOnTouchListener(this);
			model2View.findViewById(R.id.iv_mode2_batteryLeft)
					.setOnTouchListener(this);
			model2View.findViewById(R.id.iv_mode2_batteryRight)
					.setOnTouchListener(this);
			model2View.findViewById(R.id.iv_mode2_btn6)
					.setOnTouchListener(this);

			break;
		case 3:
			View model3View = getControlViewByMode();
			mode_rock = (RockerView) model3View
					.findViewById(R.id.rockerview_model3);
			mode_rock.setRockerChangeListener(new MyRockerChangeListener());

			model3View.findViewById(R.id.ibtn_mode3_left_button1)
					.setOnTouchListener(this);
			model3View.findViewById(R.id.ibtn_mode3_left_button2)
					.setOnTouchListener(this);
			model3View.findViewById(R.id.ibtn_mode3_left_button3)
					.setOnTouchListener(this);
			model3View.findViewById(R.id.ibtn_mode3_left_button4)
					.setOnTouchListener(this);
			model3View.findViewById(R.id.ibtn_mode3_right_button1)
					.setOnTouchListener(this);
			model3View.findViewById(R.id.ibtn_mode3_right_button2)
					.setOnTouchListener(this);
			model3View.findViewById(R.id.ibtn_mode3_right_button3)
					.setOnTouchListener(this);
			model3View.findViewById(R.id.ibtn_mode3_right_button4)
					.setOnTouchListener(this);
			break;
		case 4:
			View model4View = getControlViewByMode();
			mode_rock = (RockerView) model4View
					.findViewById(R.id.rockerview_model4);
			mode_rock.setRockerChangeListener(new MyRockerChangeListener());

			model4View.findViewById(R.id.ibtn_mode4_up)
					.setOnTouchListener(this);
			model4View.findViewById(R.id.ibtn_mode4_down).setOnTouchListener(
					this);
			model4View.findViewById(R.id.ibtn_mode4_left).setOnTouchListener(
					this);
			model4View.findViewById(R.id.ibtn_mode4_right).setOnTouchListener(
					this);
			break;

		case 5:
			View model5View = getControlViewByMode();
			mode_rock = (RockerView) model5View.findViewById(R.id.mode_rock);
			mode_rock.setRockerChangeListener(new MyRockerChangeListener());

			model5View.findViewById(R.id.ibtn_mode5_cannon).setOnTouchListener(
					this);
			model5View.findViewById(R.id.ibtn_mode5_guns).setOnTouchListener(
					this);
			model5View.findViewById(R.id.ibtn_model5_button3)
					.setOnTouchListener(this);
			model5View.findViewById(R.id.ibtn_mode5_batteryLeft)
					.setOnTouchListener(this);

			model5View.findViewById(R.id.ibtn_mode5_batteryRight)
					.setOnTouchListener(this);
			model5View.findViewById(R.id.ibtn_mode5_button6)
					.setOnTouchListener(this);

			break;

		case 6:
			View model6View = getControlViewByMode();
			// one_start = (MenuButton) model6View
			// .findViewById(R.id.btn_one_start);
			// one_start.setMenuOnClickListener(new MyMenuOnClickListener());
			total = (Button) findViewById(R.id.btn_total);
			total.setOnClickListener(this);

			one_start = (Button) findViewById(R.id.btn_one_start);
			one_start.setVisibility(View.INVISIBLE);
			one_start.setOnClickListener(this);

			one_land = (Button) findViewById(R.id.btn_one_land);
			one_land.setVisibility(View.INVISIBLE);
			one_land.setOnClickListener(this);

			one_stop = (Button) findViewById(R.id.btn_one_stop);
			one_stop.setVisibility(View.INVISIBLE);
			one_stop.setOnClickListener(this);

			one_highlimit = (MenuButton) model6View
					.findViewById(R.id.btn_one_highlimit);
			one_highlimit.setVisibility(View.INVISIBLE);
			one_highlimit.setMenuOnClickListener(new MyMenuOnClickListener());

			one_nohead = (MenuButton) model6View
					.findViewById(R.id.btn_one_nohead);
			one_nohead.setVisibility(View.INVISIBLE);
			one_nohead.setMenuOnClickListener(new MyMenuOnClickListener());

			one_roll = (Button) findViewById(R.id.btn_one_doroll);
			one_roll.setVisibility(View.INVISIBLE);
			one_roll.setOnClickListener(this);

			one_balance = (Button) findViewById(R.id.btn_one_balance);
			one_balance.setVisibility(View.INVISIBLE);
			one_balance.setOnClickListener(this);

			model6_rocker_left = (RockerView) model6View
					.findViewById(R.id.model6_rocker_left);
			model6_rocker_right = (RockerView) model6View
					.findViewById(R.id.model6_rocker_right);
			model6_rocker_right
					.setRockerChangeListener(new MyRockerChangeListener());
			model6_rocker_left
					.setRockerChangeListener(new MyRockerChangeListener());

			break;

		}
	}

	int x = 0;

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_total:
			if (x == 0) {
				RoatAnimUtil.startAnimationIn(total);

				one_start.startAnimation(SCALEbIGaNIM(500));
				one_land.startAnimation(SCALEbIGaNIM(500));
				one_stop.startAnimation(SCALEbIGaNIM(500));
				one_highlimit.startAnimation(SCALEbIGaNIM(500));
				one_nohead.startAnimation(SCALEbIGaNIM(500));
				one_roll.startAnimation(SCALEbIGaNIM(500));
				one_balance.startAnimation(SCALEbIGaNIM(500));

				one_start.setVisibility(View.VISIBLE);
				one_land.setVisibility(View.VISIBLE);
				one_stop.setVisibility(View.VISIBLE);
				one_highlimit.setVisibility(View.VISIBLE);
				one_nohead.setVisibility(View.VISIBLE);
				one_roll.setVisibility(View.VISIBLE);
				one_balance.setVisibility(View.VISIBLE);
				x = 1;
			} else if (x == 1) {
				RoatAnimUtil.startAnimationOut(total);

				one_start.startAnimation(scaleSmallAnim(500));
				one_land.startAnimation(scaleSmallAnim(500));
				one_stop.startAnimation(scaleSmallAnim(500));
				one_highlimit.startAnimation(scaleSmallAnim(500));
				one_nohead.startAnimation(scaleSmallAnim(500));
				one_roll.startAnimation(scaleSmallAnim(500));
				one_balance.startAnimation(scaleSmallAnim(500));

				one_start.setVisibility(View.INVISIBLE);
				one_land.setVisibility(View.INVISIBLE);
				one_stop.setVisibility(View.INVISIBLE);
				one_highlimit.setVisibility(View.INVISIBLE);
				one_nohead.setVisibility(View.INVISIBLE);
				one_roll.setVisibility(View.INVISIBLE);
				one_balance.setVisibility(View.INVISIBLE);
				x = 0;
			}

			break;
		case R.id.btn_one_start:
			mCtlMsg6.setStartFly(1);
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					mCtlMsg6.setStartFly(0);
					// mCtlMsg6.setThrottle(-1);
				}

			}, 1000);
			break;

		case R.id.btn_one_land:
			mCtlMsg6.setLand(1);
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					mCtlMsg6.setLand(0);
				}
			}, 1000);
			break;
		case R.id.btn_one_stop:
			mCtlMsg6.setStop(1);
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					mCtlMsg6.setStop(0);
				}
			}, 1000);
			break;
		case R.id.btn_one_doroll:
			mCtlMsg6.setDoRoll(1);
			Flag = true;
			// new Handler().postDelayed(new Runnable() {
			// @Override
			// public void run() {
			// mCtlMsg6.setDoRoll(0);
			// }
			// }, 1000);

			break;

		case R.id.btn_one_balance:
			Flag2 = true;
			/*
			 * mCtlMsg6.setBalance(1); new Handler().postDelayed(new Runnable()
			 * {
			 * 
			 * @Override public void run() { mCtlMsg6.setBalance(0); } }, 1000);
			 */

			break;
		}

	}

	// 缩小动画
	private Animation scaleSmallAnim(int duration) {
		AnimationSet animationSet = new AnimationSet(true);
		ScaleAnimation scaleAnim = new ScaleAnimation(1.0f, 0.0f, 1.0f, 0.0f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		AlphaAnimation alphaAnim = new AlphaAnimation(1.0f, 0.0f);
		animationSet.addAnimation(scaleAnim);
		animationSet.addAnimation(alphaAnim);
		animationSet.setDuration(duration);
		animationSet.setFillAfter(true);
		return animationSet;
	}

	// 放大动画
	private Animation SCALEbIGaNIM(int duration) {
		AnimationSet animationSet = new AnimationSet(true);
		ScaleAnimation scaleAnim = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		AlphaAnimation alphaAnim = new AlphaAnimation(0.0f, 1.0f);
		animationSet.addAnimation(scaleAnim);
		animationSet.addAnimation(alphaAnim);
		animationSet.setDuration(duration);
		animationSet.setFillAfter(false);
		return animationSet;
	}

	private void setModle(int mode) {
		int[] ids = new int[] { R.id.ll_model1, R.id.ll_model2, R.id.ll_model3,
				R.id.ll_model4, R.id.ll_model5, R.id.ll_model6 };
		for (int id : ids) {
			findViewById(id).setVisibility(View.INVISIBLE);
		}
		findViewById(ids[mode - 1]).setVisibility(View.VISIBLE);
	}

	private View getControlViewByMode() {
		int[] ids = new int[] { R.id.ll_model1, R.id.ll_model2, R.id.ll_model3,
				R.id.ll_model4, R.id.ll_model5, R.id.ll_model6 };
		return findViewById(ids[mode - 1]);
	}

	class MyRockerChangeListener implements RockerView.RockerChangeListener {
		
		@Override
		public void report(View v, float x, float y) {
			// TODO Auto-generated method stub
			if (v.getId() == R.id.mode_rock) {
				// if (mode == 6) {
				// mCtlMsg6.setB1(1);
				// mCtlMsg6.setY(y);
				// mCtlMsg6.setX(x);
				// } else {
				mCtlMsg.setB1(1);
				mCtlMsg.setY(y);
				mCtlMsg.setX(x);
				
				isCountDown_HideAllView = true; // 重新启动隐藏所用View倒计时
				mTime_HideAllView = s_TotalTime_HideAllView;
				// }
			}
			if (v.getId() == R.id.model6_rocker_left) {
				mCtlMsg6.setYaw(x); // 设置方向舵
				mCtlMsg6.setThrottle(y); // 设置油门
				isCountDown_HideAllView = true; // 重新启动隐藏所用View倒计时
				mTime_HideAllView = s_TotalTime_HideAllView;
				if (y == -1.0f) {
					mCtlMsg6.setBalance(1);
					new Handler().postDelayed(new Runnable() {

						@Override
						public void run() {
							mCtlMsg6.setBalance(0);
						}
					}, 1000);
				} else {
					Flag2 = false;
				}

			} else if (v.getId() == R.id.model6_rocker_right) {
				mCtlMsg6.setRoll(x); // 设置副翼
				mCtlMsg6.setPitch(y); // 设置升降舵
				isCountDown_HideAllView = true; // 重新启动隐藏所用View倒计时
				mTime_HideAllView = s_TotalTime_HideAllView;
				if (Flag) {
					if ((-0.5f < x && x < 0.5f) || (-0.5f < y && y < 0.5f)) {
						mCtlMsg6.setDoRoll(0);
						Log.i("aa", "====");
					} else {
						Flag = false;
					}
				}

			}
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		ControlMsg controlMsg = ControlMsg.getInstance();
		byte[] bytes = new byte[8];
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			switch (v.getId()) {
			case R.id.btn_mode1_lup:
				mCtlMsg.setB1(2);
				mCtlMsg.setB2(255);
				break;
			case R.id.btn_mode1_ldown:
				mCtlMsg.setB1(2);
				mCtlMsg.setB2(0);
				break;
			case R.id.btn_mode1_rup:
				mCtlMsg.setB1(2);
				mCtlMsg.setB3(255);
				break;
			case R.id.btn_mode1_rdown:
				mCtlMsg.setB1(2);
				mCtlMsg.setB3(0);
				break;
			// mode2
			case R.id.ibtn_mode2_up:
				mCtlMsg.setB1(1);
				mCtlMsg.setB2(128);
				mCtlMsg.setB3(255);
				break;
			case R.id.ibtn_mode2_down:
				mCtlMsg.setB1(1);
				mCtlMsg.setB2(128);
				mCtlMsg.setB3(0);
				break;
			case R.id.ibtn_mode2_left:
				mCtlMsg.setB1(1);
				mCtlMsg.setB2(0);
				mCtlMsg.setB3(128);
				break;
			case R.id.ibtn_mode2_right:
				mCtlMsg.setB1(1);
				mCtlMsg.setB2(255);
				mCtlMsg.setB3(128);
				break;
			case R.id.iv_mode2_cannon:
				mCtlMsg.setB1(1);
				mCtlMsg.setB4(255);
				break;
			case R.id.iv_mode2_guns:
				mCtlMsg.setB1(1);
				mCtlMsg.setB5(255);
				break;
			case R.id.iv_mode2_btn3:
				mCtlMsg.setB1(1);
				mCtlMsg.setB6(4);
				break;
			case R.id.iv_mode2_batteryLeft:
				mCtlMsg.setB1(1);
				mCtlMsg.setB4(0);
				break;
			case R.id.iv_mode2_batteryRight:
				mCtlMsg.setB1(1);
				mCtlMsg.setB5(0);
				break;
			case R.id.iv_mode2_btn6:
				mCtlMsg.setB1(1);
				mCtlMsg.setB6(5);
				break;

			// mode3

			case R.id.ibtn_mode3_left_button1:
				mCtlMsg.setB1(2);
				mCtlMsg.setB2(255);
				break;
			case R.id.ibtn_mode3_left_button2:
				mCtlMsg.setB1(2);
				mCtlMsg.setB3(255);
				break;
			case R.id.ibtn_mode3_left_button3:
				mCtlMsg.setB1(2);
				mCtlMsg.setB4(255);
				break;
			case R.id.ibtn_mode3_left_button4:
				mCtlMsg.setB1(2);
				mCtlMsg.setB5(255);
				break;
			case R.id.ibtn_mode3_right_button1:
				mCtlMsg.setB1(2);
				mCtlMsg.setB2(0);
				break;
			case R.id.ibtn_mode3_right_button2:
				mCtlMsg.setB1(2);
				mCtlMsg.setB3(0);
				break;
			case R.id.ibtn_mode3_right_button3:
				mCtlMsg.setB1(2);
				mCtlMsg.setB4(0);
				break;
			case R.id.ibtn_mode3_right_button4:
				mCtlMsg.setB1(2);
				mCtlMsg.setB5(0);
				break;

			// mode4
			case R.id.ibtn_mode4_up:
				mCtlMsg.setB1(2);
				mCtlMsg.setB2(255);
				break;
			case R.id.ibtn_mode4_down:
				mCtlMsg.setB1(2);
				mCtlMsg.setB2(0);
				break;
			case R.id.ibtn_mode4_left:
				mCtlMsg.setB1(2);
				mCtlMsg.setB3(255);
				break;
			case R.id.ibtn_mode4_right:
				mCtlMsg.setB1(2);
				mCtlMsg.setB3(0);
				break;

			// mode5
			case R.id.ibtn_mode5_cannon:
				mCtlMsg.setB1(1);
				mCtlMsg.setB4(255);
				break;
			case R.id.ibtn_mode5_guns:
				mCtlMsg.setB1(1);
				mCtlMsg.setB5(255);
				break;
			case R.id.ibtn_model5_button3:
				mCtlMsg.setB1(1);
				mCtlMsg.setB6(4);
				break;
			case R.id.ibtn_mode5_batteryLeft:
				mCtlMsg.setB1(1);
				mCtlMsg.setB4(0);
				break;
			case R.id.ibtn_mode5_batteryRight:
				mCtlMsg.setB1(1);
				mCtlMsg.setB5(0);
				break;
			case R.id.ibtn_mode5_button6:
				mCtlMsg.setB1(1);
				mCtlMsg.setB6(5);
				break;

			}
		}

		if (event.getAction() == MotionEvent.ACTION_UP) {
			switch (v.getId()) {
			case R.id.btn_mode1_lup:
				mHandler.sendEmptyMessageDelayed(2, 50);
				break;
			case R.id.btn_mode1_ldown:
				mHandler.sendEmptyMessageDelayed(3, 50);
				break;
			case R.id.btn_mode1_rup:
				mHandler.sendEmptyMessageDelayed(4, 50);
				break;
			case R.id.btn_mode1_rdown:
				mHandler.sendEmptyMessageDelayed(5, 50);

				break;

			case R.id.ibtn_mode2_up:
				mHandler.sendEmptyMessageDelayed(6, 50);
				break;
			case R.id.ibtn_mode2_down:
				mHandler.sendEmptyMessageDelayed(7, 50);
				break;
			case R.id.ibtn_mode2_left:
				mHandler.sendEmptyMessageDelayed(8, 50);
				break;
			case R.id.ibtn_mode2_right:
				mHandler.sendEmptyMessageDelayed(9, 50);
				break;
			case R.id.iv_mode2_cannon:
				mHandler.sendEmptyMessageDelayed(10, 50);
				break;
			case R.id.iv_mode2_guns:
				mHandler.sendEmptyMessageDelayed(11, 50);
				break;
			case R.id.iv_mode2_btn3:
				mHandler.sendEmptyMessageDelayed(12, 50);
				break;
			case R.id.iv_mode2_batteryLeft:
				mHandler.sendEmptyMessageDelayed(13, 50);
				break;
			case R.id.iv_mode2_batteryRight:
				mHandler.sendEmptyMessageDelayed(14, 50);
				break;
			case R.id.iv_mode2_btn6:
				mHandler.sendEmptyMessageDelayed(15, 50);
				break;

			// mode3
			case R.id.ibtn_mode3_left_button1:
				mHandler.sendEmptyMessageDelayed(21, 50);
				break;
			case R.id.ibtn_mode3_left_button2:
				mHandler.sendEmptyMessageDelayed(22, 50);
				break;
			case R.id.ibtn_mode3_left_button3:
				mHandler.sendEmptyMessageDelayed(23, 50);
				break;
			case R.id.ibtn_mode3_left_button4:
				mHandler.sendEmptyMessageDelayed(24, 50);
				break;
			case R.id.ibtn_mode3_right_button1:
				mHandler.sendEmptyMessageDelayed(25, 50);
				break;
			case R.id.ibtn_mode3_right_button2:
				mHandler.sendEmptyMessageDelayed(26, 50);
				break;
			case R.id.ibtn_mode3_right_button3:
				mHandler.sendEmptyMessageDelayed(27, 50);
				break;
			case R.id.ibtn_mode3_right_button4:
				mHandler.sendEmptyMessageDelayed(28, 50);
				break;
			// mode4

			case R.id.ibtn_mode4_up:
				mHandler.sendEmptyMessageDelayed(41, 50);
				break;
			case R.id.ibtn_mode4_down:
				mHandler.sendEmptyMessageDelayed(42, 50);
				break;
			case R.id.ibtn_mode4_left:
				mHandler.sendEmptyMessageDelayed(43, 50);
				break;
			case R.id.ibtn_mode4_right:
				mHandler.sendEmptyMessageDelayed(44, 50);
				break;
			// mode5

			case R.id.ibtn_mode5_cannon:
				mHandler.sendEmptyMessageDelayed(51, 50);
				break;
			case R.id.ibtn_mode5_guns:
				mHandler.sendEmptyMessageDelayed(52, 50);
				break;
			case R.id.ibtn_model5_button3:
				mHandler.sendEmptyMessageDelayed(53, 50);
				break;
			case R.id.ibtn_mode5_batteryLeft:
				mHandler.sendEmptyMessageDelayed(54, 50);
				break;
			case R.id.ibtn_mode5_batteryRight:
				mHandler.sendEmptyMessageDelayed(55, 50);
				break;
			case R.id.ibtn_mode5_button6:
				mHandler.sendEmptyMessageDelayed(56, 50);
				break;

			}
		}
		return false;
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		changeControlMode();
	}

	/**
	 * 　录像监听线程
	 */
	class ListenRecordThread extends Thread {

		public boolean isRun;
		int sleepTime = 1000;

		@Override
		public void run() {
			isRun = true;
			while (isRun) {

				if (mVideoView.videoIsRecording()) {
					mHandler.sendEmptyMessage(0);
					sleepTime = 1000;
				} else {
					mHandler.sendEmptyMessage(1);
					mRecord_startTime = System.currentTimeMillis(); // 记录本地录像的启动时间
					sleepTime = 1000;
				}
				try {
					Thread.sleep(sleepTime);
					if (isCountDown_HideAllView) {
						mTime_HideAllView--;
					}
					// Log.i(TAG, "Time:" + mTime_HideAllView);
					if (mTime_HideAllView <= 0) {
						mTime_HideAllView = 0;
						isHideAllView = true;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 　用于处理录像信息的Handler 和 清除mCtlMsg byte[5]
	 */
	Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0: // 正在本地录像
				isRecording = true;
				setRecordTime(mBtn_record, mRecord_startTime);
				break;

			case 1: // 停止本地录像
				isRecording = false;
				if (mBtn_record != null) {
					mBtn_record.setText(getResources().getString(
							R.string.record));
				}
				break;
			case 2:
				mCtlMsg.setB1(2);
				mCtlMsg.setB2(128);
				Log.i("mHandler", "clear Balance");
				break;
			case 3:
				mCtlMsg.setB1(2);
				mCtlMsg.setB2(128);
				Log.i("mHandler", "clear stop");
				break;
			case 4:
				mCtlMsg.setB1(2);
				mCtlMsg.setB3(128);
				// mBtn_landing.setChecked(false);
				Log.i("mHandler", "clear Landing");
				break;
			case 5:
				mCtlMsg.setB1(2);
				mCtlMsg.setB3(128);
				Log.i("mHandler", "clear StartFly");
				break;

			// mode2
			case 6:
				mCtlMsg.setB1(1);
				mCtlMsg.setB2(128);
				mCtlMsg.setB3(128);
				break;
			case 7:
				mCtlMsg.setB1(1);
				mCtlMsg.setB2(128);
				mCtlMsg.setB3(128);
				break;
			case 8:
				mCtlMsg.setB1(1);
				mCtlMsg.setB2(128);
				mCtlMsg.setB3(128);
				break;

			case 9:
				mCtlMsg.setB1(1);
				mCtlMsg.setB2(128);
				mCtlMsg.setB3(128);
				break;
			case 10:
				mCtlMsg.setB4(128);
				break;
			case 11:
				mCtlMsg.setB5(128);
				break;
			case 12:
				mCtlMsg.setB6(0);
				break;
			case 13:
				mCtlMsg.setB4(128);
				break;
			case 14:
				mCtlMsg.setB5(128);
				break;
			case 15:
				mCtlMsg.setB6(0);
				break;

			// mode3
			case 21:
				mCtlMsg.setB1(2);
				mCtlMsg.setB2(128);
				break;
			case 22:
				mCtlMsg.setB1(2);
				mCtlMsg.setB3(128);
				break;
			case 23:
				mCtlMsg.setB1(2);
				mCtlMsg.setB4(128);
				break;
			case 24:
				mCtlMsg.setB1(2);
				mCtlMsg.setB5(128);
				break;
			case 25:
				mCtlMsg.setB1(2);
				mCtlMsg.setB2(128);
				break;
			case 26:
				mCtlMsg.setB1(2);
				mCtlMsg.setB3(128);
				break;
			case 27:
				mCtlMsg.setB1(2);
				mCtlMsg.setB4(128);
				break;
			case 28:
				mCtlMsg.setB1(2);
				mCtlMsg.setB5(128);
				break;

			// mode4
			case 41:
				mCtlMsg.setB1(2);
				mCtlMsg.setB2(128);
				break;
			case 42:
				mCtlMsg.setB1(2);
				mCtlMsg.setB2(128);
				break;
			case 43:
				mCtlMsg.setB1(2);
				mCtlMsg.setB3(128);
				break;
			case 44:
				mCtlMsg.setB1(2);
				mCtlMsg.setB3(128);
				break;
			// mode5

			case 51:
				mCtlMsg.setB4(128);
				break;
			case 52:
				mCtlMsg.setB5(128);
				break;
			case 53:
				mCtlMsg.setB6(0);
				break;
			case 54:
				mCtlMsg.setB4(128);
				break;
			case 55:
				mCtlMsg.setB5(128);
				break;
			case 56:
				mCtlMsg.setB6(0);
				break;

			default:
				break;
			}

			if (isSDRecording) { // 正在录像
				setRecordTime(mBtn_SDRecord, mSDRecord_startTime); // 修改mTv_record
			} else {
				mSDRecord_startTime = 0;
			}

			if (isHideAllView) {
				setViewVisibility(mLayoutView_menu, View.INVISIBLE);
				setViewVisibility(playModelView, View.INVISIBLE);
				if (mode == 6) {
					setViewVisibility(model6_all, View.INVISIBLE);
				}
			}
		}

		/**
		 * 设置录像时间TextView
		 * 
		 * @param tv
		 *            需要设置的TextView
		 * @param startTime
		 *            录制开始时间
		 */
		private void setRecordTime(MenuButton btn, long startTime) {
			int time = (int) ((System.currentTimeMillis() - startTime) / 1000); // 总时间，单位s
			String sTime = String.format("%02d", time / 60) + ":"
					+ String.format("%02d", time % 60);
			btn.setText(1, sTime);
		}
	};

	Handler videoEventHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			RtspVideoView.HandlerMsg handlerMsg = (RtspVideoView.HandlerMsg) msg.obj;
			boolean isSuccess = handlerMsg.isSuccess;
			switch (msg.what) {
			case RtspVideoView.START_RTSP:
				if (!isSuccess) {
					mToast.showToast("START_RTSP failed");
				}
				break;
			case RtspVideoView.SNAPSHOT:
				if (!isSuccess) {
					mToast.showToast(getResources().getString(
							R.string.failed_to_snapshot));
				} else {
					mPlayVoice.play(PlayVoice.VOICE_SNAPSHOT);
					mToast.showToast(getResources().getString(R.string.save_as)
							+ "\"" + handlerMsg.msg + "\"");
				}
				break;

			case RtspVideoView.RECORD:
				if (!isSuccess) {
					mToast.showToast(getResources().getString(
							R.string.failed_to_record));
				} else {
					mPlayVoice.play(PlayVoice.VOICE_RECORD1);
					mToast.showToast(getResources().getString(R.string.save_as)
							+ "\"" + handlerMsg.msg + "\"");
				}
				break;
			case RtspVideoView.STOP_RTSP:
				if (!isSuccess) {
					Log.d(TAG, "stop_stsp success");
				}
			default:
				break;
			}
		}
	};

	/**
	 * 监听摇杆动作的类
	 */

	class MyTrimChangeListener implements TrimView.TrimChangeListener {

		@Override
		public void report(View v, int progress) {
			switch (v.getId()) {
			case R.id.trimview1:
				Log.i(TAG, "trimview1: " + progress);
				// mCtlMsg.setYaw_trim(progress * 2);
				break;

			case R.id.trimview2:
				Log.i(TAG, "trimview2: " + progress);
				// mCtlMsg.setRoll_trim(progress * 2);
				break;

			case R.id.trimview3:
				Log.i(TAG, "trimview3: " + progress);
				// mCtlMsg.setPitch_trim(progress * 2);
				break;

			default:
				break;
			}
		}
	}

	class MyMenuOnClickListener implements MenuButtonOnClickListener {

		@Override
		public void onClick(View v) {

			switch (v.getId()) {
			// 退出
			case R.id.btn_exit:
				finish();
				break;
			// 截图
			case R.id.btn_snapshot:
				mBtn_snapShot.setEnabled(false);
				mVideoView.takeSnapShot(AppUtil.getImageName());
				// if (mVideoView.takeSnapShot(name)) {
				// mToast.showToast(AppUtil.text_toast1[mLanguage] + name);
				// } else {
				// mToast.showToast(AppUtil.text_toast2[mLanguage]);
				// }
				mBtn_snapShot.setEnabled(true);
				break;
			// 录像
			case R.id.btn_record:
				try {
					if (mVideoView.isReady()) {
						if (mVideoView.videoIsRecording()) {
							isStartRecord = false;
							mVideoView.videoRecordStop(); // 停止录像
						} else {
							mVideoView.videoRecordStart(AppUtil.getVideoName()); // 启动录像
							mPlayVoice.play(PlayVoice.VOICE_RECORD0);
						}
					} else {
						if (mBtn_record.isChecked()) {
							mBtn_record.setChecked(false);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			// SD录像
			case R.id.btn_sdRecord:
				if (haveSDcard) {
					mBtn_SDRecord.setEnabled(false);

					if (mBtn_SDRecord.isChecked()) {
						new HttpThread(HTTP_START_RECORD, HTTP_handler).start();
					} else {
						new HttpThread(HTTP_STOP_RECORD, HTTP_handler).start();
					}
				} else {
					mToast.showToast(getResources().getString(
							R.string.please_check_the_SDcard));
					if (mBtn_SDRecord.isChecked()) {
						mBtn_SDRecord.setChecked(false);
					}
				}
				break;
			// 查看截图录像文件
			case R.id.btn_playback:
				if (!isSDRecording && !isRecording) {
					Intent intent = new Intent(PlayActivity.this,
							FileActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
				} else {
					mToast.showToast(getResources().getString(
							R.string.recording_please_wait));
				}
				break;
			// 速度选择
			case R.id.btn_speedChoose:
				int index = ((MenuButton) v).getSpeed();
				if (mode == 6) {
					mCtlMsg6.setSpeedLimit(index);
				} else {
					mCtlMsg.setSpeedLimit(index);
				}

				break;
			// 重力控制
			case R.id.btn_gravity:
				// //////////////////////////////////
				if (mode == 6) {
					if (mBtn_gravity.isChecked()) {
						model6_rocker_right.setUseSensor(true);
					} else {
						model6_rocker_right.setUseSensor(false);
					}
				} else {
					if (mBtn_gravity.isChecked()) {
						mode_rock.setUseSensor(true);
					} else {
						mode_rock.setUseSensor(false);
					}
				}
				break;
			// 开启/关闭控制界面
			case R.id.btn_offControl:
				if (mBtn_offControl.isChecked()) {
					setViewVisibility(playModelView, View.INVISIBLE);
					// setViewVisibility(mLayoutView_rocker, View.INVISIBLE);
				} else {
					setViewVisibility(playModelView, View.VISIBLE);
					// setViewVisibility(mLayoutView_rocker, View.VISIBLE);
				}
				// 一键翻滚

				break;

			case R.id.btn_one_nohead:
				if (one_nohead.isChecked()) {
					mCtlMsg6.setNoHead(1);
				} else {
					mCtlMsg6.setNoHead(0);
				}
				break;

			case R.id.btn_one_highlimit:
				if (one_highlimit.isChecked()) {
					mCtlMsg6.setHighLimit(1);
				} else {
					mCtlMsg6.setHighLimit(0);
				}
				break;

			// 设置按键
			case R.id.btn_setting:
				isCountDown_HideAllView = false; // 关闭隐藏所有控制View倒计时

				View layout_setup = LayoutInflater.from(PlayActivity.this)
						.inflate(R.layout.dialog_setup, null);
				ListView listView_setup = (ListView) layout_setup
						.findViewById(R.id.listview_setup);
				// final boolean isAddBalance = mCtlMsg.getThrottle() == 0;

				if (mode == 6) {// 模式六的设置下有操控模式的选项
					final SetupListViewAdapter setupListViewAdapter = new SetupListViewAdapter(
							PlayActivity.this, mLanguage, true,
							new int[] { mFlipImage, mStroageLocaltion,
									mControlMode });

					listView_setup.setAdapter(setupListViewAdapter);

					final AlertDialog setupDialog = new AlertDialog.Builder(
							PlayActivity.this,
							AlertDialog.THEME_DEVICE_DEFAULT_DARK)
							.setTitle(
									getResources().getString(R.string.setting))
							.setView(layout_setup)
							.setPositiveButton(
									getResources().getString(R.string.ok),
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											int len = setupListViewAdapter.arrayList
													.size();
											int[] nums = new int[len];
											boolean[] isChangeds = new boolean[len];
											for (int i = 0; i < len; i++) {
												SetupListViewAdapter.SetupItem setupItem = setupListViewAdapter.arrayList
														.get(i);
												Log.i(TAG, setupItem.toString());
												nums[i] = setupItem
														.getSetupNum();
												isChangeds[i] = setupItem
														.isChanged();
											}

											if (isChangeds[len - 2]) {
												mStroageLocaltion = nums[len - 2];
												changeRecordLocaltion();
											}

											if (isChangeds[len - 3]) {
												mFlipImage = nums[len - 3];

												if (mFlipImage == 1) {
													mFlipImage = 3;
												}
												new HttpThread(
														HttpThread.HTTP_FLIP_MIRROR_IMAGE,
														mFlipImage,
														HTTP_handler).start();

												if (mFlipImage == 3) {
													mFlipImage = 1;
												}

											}

											if (isChangeds[len - 1]) {
												mControlMode = nums[len - 1];
												changeControlMode();
											}
											writeSetupParameter();
										}
									})
							.setNegativeButton(
									getResources().getString(R.string.cancel),
									null).show();
					// 对setupDialog设置dismiss监听，当setupDialog消失时进入
					setupDialog.setOnDismissListener(new OnDismissListener() {
						@Override
						public void onDismiss(DialogInterface dialog) {
							isCountDown_HideAllView = true; // 重新启动隐藏所用View倒计时
							mTime_HideAllView = s_TotalTime_HideAllView;
						}
					});
					listView_setup
							.setOnItemClickListener(new OnItemClickListener() {

								@Override
								public void onItemClick(AdapterView<?> parent,
										View view, int position, long id) {

									if (position == 0) { // 网络设置
										setupDialog.dismiss();
										setNetwork();
									}
								}

							});

				} else {
					final SetupListViewAdapter setupListViewAdapter = new SetupListViewAdapter(
							PlayActivity.this, mLanguage, new int[] {
									mFlipImage, mStroageLocaltion });

					listView_setup.setAdapter(setupListViewAdapter);

					final AlertDialog setupDialog = new AlertDialog.Builder(
							PlayActivity.this,
							AlertDialog.THEME_DEVICE_DEFAULT_DARK)
							.setTitle(
									getResources().getString(R.string.setting))
							.setView(layout_setup)
							.setPositiveButton(
									getResources().getString(R.string.ok),
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											int len = setupListViewAdapter.arrayList
													.size();
											int[] nums = new int[len];
											boolean[] isChangeds = new boolean[len];
											for (int i = 0; i < len; i++) {
												SetupListViewAdapter.SetupItem setupItem = setupListViewAdapter.arrayList
														.get(i);
												Log.i(TAG, setupItem.toString());
												nums[i] = setupItem
														.getSetupNum();
												isChangeds[i] = setupItem
														.isChanged();
											}

											if (isChangeds[len - 1]) {
												mStroageLocaltion = nums[len - 1];
												changeRecordLocaltion();
											}

											if (isChangeds[len - 2]) {
												mFlipImage = nums[len - 2];

												if (mFlipImage == 1) {
													mFlipImage = 3;
												}
												new HttpThread(
														HttpThread.HTTP_FLIP_MIRROR_IMAGE,
														mFlipImage,
														HTTP_handler).start();

												if (mFlipImage == 3) {
													mFlipImage = 1;
												}

											}

											writeSetupParameter();
										}
									})
							.setNegativeButton(
									getResources().getString(R.string.cancel),
									null).show();
					// 对setupDialog设置dismiss监听，当setupDialog消失时进入
					setupDialog.setOnDismissListener(new OnDismissListener() {
						@Override
						public void onDismiss(DialogInterface dialog) {
							isCountDown_HideAllView = true; // 重新启动隐藏所用View倒计时
							mTime_HideAllView = s_TotalTime_HideAllView;
						}
					});

					listView_setup
							.setOnItemClickListener(new OnItemClickListener() {

								@Override
								public void onItemClick(AdapterView<?> parent,
										View view, int position, long id) {
									// if (isAddBalance && position == 0) {
									// mCtlMsg.setBalance(1);
									// mHandler.sendEmptyMessageDelayed(2,
									// 1000);
									// setupDialog.dismiss();
									// }
									// if (isAddBalance && position == 1
									// || !isAddBalance && position == 0) {
									// mCtlMsg.setStop(1);
									// mHandler.sendEmptyMessageDelayed(3,
									// 1000);
									// setupDialog.dismiss();
									// }

									if (position == 0) { // 网络设置
										setupDialog.dismiss();
										setNetwork();
									}
								}

							});
				}

				break;
			// 定高
			// case R.id.btn_lock:
			// control_lockAndHighLimit(2);
			// break;
			default:
				break;
			}
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
				intent = new Intent(
						android.provider.Settings.ACTION_WIFI_SETTINGS);
			} else {
				intent = new Intent();
				ComponentName component = new ComponentName(
						"com.android.settings",
						"com.android.settings.WirelessSettings");
				intent.setComponent(component);
				intent.setAction("android.intent.action.VIEW");
			}
			startActivity(intent);

		}
	}

	private void changeRecordLocaltion() {
		if (mStroageLocaltion == 0) {
			setViewVisibility(mBtn_SDRecord, View.VISIBLE);
			setViewVisibility(mBtn_record, View.GONE);
		} else {
			setViewVisibility(mBtn_SDRecord, View.GONE);
			setViewVisibility(mBtn_record, View.VISIBLE);
		}
	}

	/**
	 * 左右手切换及关闭控制
	 */
	private void changeControlMode() {
		Log.e(TAG, "changeControlMode");

		if (mBtn_offControl.isChecked()) { // 表示已经关闭控制
			// setViewVisibility(mLayoutView_trim, View.INVISIBLE);
			// setViewVisibility(model6_all, View.INVISIBLE);
		} else {
			// setViewVisibility(mLayoutView_trim, View.VISIBLE);
			// setViewVisibility(model6_all, View.VISIBLE);
		}
		if (mode == 6) {
			int ll = model6_rocker_left.getLeft();
			int lt = model6_rocker_left.getTop();
			int lr = model6_rocker_left.getRight();
			int lb = model6_rocker_left.getBottom();

			int rl = model6_rocker_right.getLeft();
			int rt = model6_rocker_right.getTop();
			int rr = model6_rocker_right.getRight();
			int rb = model6_rocker_right.getBottom();

			boolean isLeftHand = mControlMode == 1;
			if ((isLeftHand && ll > rl) || (!isLeftHand && ll < rl)) {
				// 左手模式且左摇杆起始坐标比右边大
				// 右手模式且左摇杆起始坐标比右边小
				model6_rocker_left.layout(rl, rt, rr, rb);
				model6_rocker_right.layout(ll, lt, lr, lb);
			}

		}
	}

	private void setViewVisibility(View view, int visibility) {
		if (view.getVisibility() != visibility) {
			view.setVisibility(visibility);
		}
	}

	/**
	 * @author Administrator 处理HTTP反馈信息Handler
	 */
	public Handler HTTP_handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			int value = (Integer) msg.obj;

			if (msg.what < 0) {
				return;
			}

			switch (msg.what) {

			case HTTP_GET_PRIVILEGE: // 获取权限返回的消息
				if (value == 0) {
					Bundle bundle = msg.getData();
					mAuthcode = bundle.getString("authcode");
					Log.w(TAG, "mAuthcode" + mAuthcode);
				} else {
					mToast.showToast(getResources().getString(
							R.string.failed_to_get_privilege));
				}
				break;
			case HTTP_RELEASE_PRIVILEGE:// 释放权限返回的消息
				if (value != 0) {
					mToast.showToast(getResources().getString(
							R.string.failed_to_release_privilege));
				}
				break;

			case HTTP_SET_TIME: // 设置时间返回的消息
				if (value != 0) {
					mToast.showToast(getResources().getString(
							R.string.failed_to_set_time));
				}
				break;

			case HTTP_CHECK_STROAGE: // 检测SDcard返回的消息
				if (value == 0) {
					haveSDcard = false;
				} else if (value == 1) {
					haveSDcard = true;
				}
				break;
			case HTTP_BRIDGE: // 发送控制数据返回的消息
				Bundle bundle = msg.getData();
				String send = bundle.getString("send");
				break;
			// case HTTP_TAKEPHOTO: // 截图返回的消息
			// if (value == 0) {
			// mToast.showToast("拍照成功");
			// } else {
			// mToast.showToast("拍照失败");
			// }
			// mBtn_takePhoto.setEnabled(true);
			// break;

			case HTTP_START_RECORD: // 启动录像返回的消息
				if (value == 0) {
					mPlayVoice.play(PlayVoice.VOICE_RECORD0);
					mSDRecord_startTime = System.currentTimeMillis();
					mToast.showToast(getResources().getString(
							R.string.recording));
					isSDRecording = true;
				} else {
					mToast.showToast(getResources().getString(
							R.string.failed_to_record));
					mBtn_SDRecord.setChecked(false);
					mBtn_SDRecord.setText(getResources().getString(
							R.string.sd_record));
					isSDRecording = false;
				}
				mBtn_SDRecord.setEnabled(true);
				break;

			case HTTP_STOP_RECORD: // 停止录像返回的消息
				if (value == 0) {
					mPlayVoice.play(PlayVoice.VOICE_RECORD1);
					mToast.showToast(getResources().getString(
							R.string.recording_success));
				} else {
					mToast.showToast(getResources().getString(
							R.string.failed_to_record));
				}
				isSDRecording = false;
				mBtn_SDRecord.setText(getResources().getString(
						R.string.sd_record));
				mBtn_SDRecord.setEnabled(true);
				break;
			}

		}
	};

	private void writeSetupParameter() {
		if (mode == 6) {
			AppUtil.setSetupParameter(mCtlMsg6.getSpeedLimit(),
					// mCtlMsg.getNoHead(),
					mLanguage, mControlMode, mStroageLocaltion, mFlipImage, 0,
					0, 0);
		} else {
			AppUtil.setSetupParameter(mCtlMsg.getSpeedLimit(),
			// mCtlMsg.getNoHead(),
					mLanguage, mStroageLocaltion, mFlipImage, 0, 0, 0);
		}

	}

	@Override
	protected void onResume() {
		mVideoView.videoResume();
		isCountDown_HideAllView = true; // 重新启动隐藏所用View倒计时
		mTime_HideAllView = s_TotalTime_HideAllView;
		super.onResume();
	}

	@Override
	protected void onPause() {
		mVideoView.videoPause();
		isCountDown_HideAllView = false;
		super.onPause();

	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		mCtlMsg = null;
		mCtlMsg6 = null;
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		// writeSetupParameter();
		if (mode_rock != null) {
			mode_rock.destory();
		}
		mVideoView.destory();

		mBtn_exit = null;
		mBtn_snapShot = null;
		mBtn_record = null;
		mBtn_playback = null;
		mBtn_SDRecord = null;
		mBtn_speedChoose = null;
		mBtn_gravity = null;
		mBtn_offControl = null;
		mBtn_setting = null;
		mCtlMsg = null;
		mCtlMsg6 = null;

		mLayoutView_menu = null;
		// playView=null;
		playModelView = null;

		listenRecordThread.isRun = false; // 关闭录像监听线程
		listenRecordThread.interrupt();
		listenRecordThread = null;

		// if(bridgeThread==null){
		// finish();
		// }else{
		bridgeThread.isRun = false; // 关闭桥接控制线程
		bridgeThread.interrupt();
		bridgeThread = null;
		// }

		new HttpThread(HTTP_RELEASE_PRIVILEGE, HTTP_handler).start(); // 释放控制权限
		playView.removeAllViews();
		super.onDestroy();
	}

}
