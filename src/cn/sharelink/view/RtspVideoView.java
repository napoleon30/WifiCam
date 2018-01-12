package cn.sharelink.view;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import cn.sharelink.MyApplication;
import cn.sharelink.use.AppUtil;
import cn.sharelink.use.PlayVoice;
import cn.sharelink.wificam.R;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class RtspVideoView extends SurfaceView implements
		SurfaceHolder.Callback {

	private static final String TAG = "RtspVideoView";

	public static final int START_RTSP = 1;
	public static final int STOP_RTSP = 2;
	public static final int SNAPSHOT = 3;
	public static final int RECORD = 4;

	private int mWidth, mHeight;
	private SurfaceHolder mHolder;
	private DrawThread mDrawThread;
	private DecoderThread mDecoderThread;
	private DecoderThread sDecoderThread;
	private int bmp_Width = 640;
	private int bmp_height = 368;
	private byte[] yuv_pixel;
	private Bitmap mBitmap;
	private boolean isGetBitmap = false;
	private Rect displayRect = new Rect();
	private String mRtspUrl = null;
	private Handler mHandler = null;
	public static Bitmap startBitmap;
	private boolean isGetSurface;
	private ByteBuffer buffer;
	private Canvas canvas;
	private Context context = MyApplication.getContext();

	public RtspVideoView(Context context) {
		super(context);
	}

	public RtspVideoView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		mHolder = this.getHolder();
		mHolder.addCallback(this);
		mHolder.setFormat(PixelFormat.RGB_565);
		mDecoderThread = new DecoderThread();
		mDecoderThread.setPriority(Thread.MAX_PRIORITY);
		mDrawThread = new DrawThread();
		mDrawThread.setPriority(Thread.MAX_PRIORITY);
		startBitmap = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.second_pic);
		isGetSurface = false;
	}

	public RtspVideoView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	class DecoderThread extends Thread {
		int time_out = 1;
		boolean isRun;
		boolean isPause;

		public void doPause() {
			isPause = true;
		}

		public synchronized void doResume() {
			isPause = false;
			notify();
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			isRun = true;
			isPause = false;
			if (!StartRtsp(mRtspUrl)) {
				sendHandler(START_RTSP, false, null);
				Log.e("RVV", "THIS UNISIS DECODER");
			} else {
				int w = GetWidth();
				int h = GetHeight();
				getBitmap(w, h);
				Log.e("RVV", "THIS ISIS DECODER");
				while (isRun) {
					if (isPause) {
						try {
							synchronized (this) {
								wait();
							}
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else {
						// Log.e("JNI", "THIS IS THREAD OF DECODER");
						Decoder();

					}
				}
				sendHandler(STOP_RTSP, true, null);
			}
			Release();
		}
	}

	class DrawThread extends Thread {
		boolean isRun = false;
		boolean isPause;
		String imageName;
		boolean isSnapshot;

		public void snapshot(String imageName) {
			this.imageName = imageName;
			isSnapshot = true;
		}

		private void doSnapshot(Bitmap bitmap) {
			try {
				boolean isSuccess;
				File file = new File(imageName);
				if (file.exists()) {
					file.delete();
				}
				FileOutputStream fos = new FileOutputStream(file);
				isSuccess = bitmap.compress(Bitmap.CompressFormat.JPEG, 100,
						fos);
				fos.close();
				Log.i("TAG", "Save as" + imageName);
				sendHandler(SNAPSHOT, isSuccess, imageName);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public void doPause() {
			isPause = true;
		}

		public synchronized void doResume() {
			isPause = false;
			notify();
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			isRun = true;
			isSnapshot = false;
			// isPause = false;
			while (isRun && !isInterrupted()) {
				if (isPause) {
					try {
						synchronized (this) {
							wait();
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else if (isGetSurface) {
					if (GetYUV(yuv_pixel)) {
						buffer = ByteBuffer.wrap(yuv_pixel);

						if (buffer != null) {
							if (mBitmap != null) {
								mBitmap.copyPixelsFromBuffer(buffer);
								buffer = null;
								System.gc();
								if (isSnapshot) {
									doSnapshot(mBitmap);
									isSnapshot = false;
								}
								canvas = mHolder.lockCanvas(null);
								synchronized (displayRect) {

									if (isRotate) {
										canvas.rotate(180.0f);
										canvas.translate(-displayRect.width(),
												-displayRect.height());
									}

									if (canvas != null) {
										canvas.drawBitmap(mBitmap, null,
												displayRect, null);
										mHolder.unlockCanvasAndPost(canvas);
									} else {
										doPause();
										doResume();
									}
								}

							}
						}
					} else {
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
			Release();
		}
	}

	public void setVideo(String url, Handler handler) {
		mRtspUrl = url;
		mHandler = handler;

		if (mDecoderThread != null && !mDecoderThread.isAlive()) {
			mDecoderThread.start();
		}

		if (mDrawThread != null && !mDrawThread.isAlive()) {
			mDrawThread.start();
		}

	}

	public void setHandler(Handler handler) {
		mHandler = handler;
	}

	private void sendHandler(int what, boolean isSuccess, String msg) {
		if (mHandler != null) {
			mHandler.obtainMessage(what, new HandlerMsg(isSuccess, msg))
					.sendToTarget();
		}
	}

	public class HandlerMsg {
		public boolean isSuccess;
		public String msg;

		public HandlerMsg(boolean isSuccess, String msg) {
			// TODO Auto-generated constructor stub
			this.isSuccess = isSuccess;
			this.msg = msg;

		}
	}

	public void takeSnapShot(String imageName) {
		mDrawThread.snapshot(imageName);
	}

	public void videoRecordStart(final String videoName) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (!StartRecord(videoName)) {
					sendHandler(RECORD, false, videoName);
				} else {
					sendHandler(RECORD, true, videoName);
				}
			}
		}).start();
	}

	public boolean isReady() {
		return IsConnected();
	}

	public boolean videoIsRecording() {
		return IsRecording();
	}

	public void videoRecordStop() {
		StopRecord();
	}

	public void videoPause() {

		mDrawThread.doPause();
		Log.i("AAAAA", "mDrawThread.doPause");
		// mDecoderThread.doPause();

	}

	public void videoResume() {
		mDrawThread.doResume();
		mDecoderThread.doResume();
		Log.i("AAAAA", "mDrawThread.doResume");

	}

	public void destory() {
		mDrawThread.isRun = false;
		mDecoderThread.isRun = false;
		
		if(mBitmap!=null){
			mBitmap.recycle();
			mBitmap = null;
		}	
		if (startBitmap!=null) {
			startBitmap.recycle();
			startBitmap = null;
		}
		canvas = null;
		
		yuv_pixel=null;
		
		Log.i("AAAAA", "mDrawThread.destroy");

		// mDrawThread=null;
		// mDecoderThread=null;

	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
		mWidth = width;
		mHeight = height;
		displayRect.set(0, 0, mWidth, mHeight);

		if (!isGetSurface) {
			canvas = mHolder.lockCanvas(null);
			synchronized (displayRect) {
				if(canvas!=null){
				canvas.drawBitmap(startBitmap, null, displayRect, null);}
			}
			mHolder.unlockCanvasAndPost(canvas);
		}
		isGetSurface = true;
		Log.i(TAG, "surface:" + width + "/" + height);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		isGetSurface = false;
		Log.e(TAG, "videoview destroy");
	}

	boolean isRotate = false;

	public void rotate() {
		isRotate = !isRotate;
	}

	public void rotate(boolean isRotate) {
		this.isRotate = isRotate;
	}

	public void getBitmap(int w, int h) {
		bmp_Width = w;
		bmp_height = h;

		yuv_pixel = new byte[w * h * 4];
		if (mBitmap != null && !mBitmap.isRecycled()) {
			mBitmap.recycle();
			mBitmap = null;
			System.gc();
		}
		mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
		isGetBitmap = true;
		Log.i(TAG, "bmp:" + bmp_Width + "/" + bmp_height);
	}
	
	static
	{
		System.loadLibrary("avutil-52");
		System.loadLibrary("avcodec-55");
		System.loadLibrary("avformat-55");
		System.loadLibrary("swscale-2");
		
		System.loadLibrary("client1");
	}

	

	public native boolean StartRtsp(String rtspUrl);

	private native boolean Decoder();

	private native int GetWidth();

	private native int GetHeight();

	private native boolean IsConnected();

	private native boolean GetYUV(byte[] yuv);

	private native void Release();

	private native boolean StartRecord(String path);

	private native boolean IsRecording();

	private native void StopRecord();
}
