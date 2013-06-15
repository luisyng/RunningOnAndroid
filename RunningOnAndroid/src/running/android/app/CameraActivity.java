package running.android.app;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import running.domain.Competition;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.SurfaceHolder.Callback;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class CameraActivity extends Activity implements SurfaceHolder.Callback {

	Camera mCamera;
	SurfaceView mSurfaceView;
	SurfaceHolder mSurfaceHolder;
	boolean mPreviewRunning = false;
	Button cameraClick;
	Bitmap imagen;
	ImageView vista;

	boolean mExternalStorageAvailable = false;
	boolean mExternalStorageWriteable = false;
	String state = Environment.getExternalStorageState();
	File path;
	File file;
	// Nombre de la foto a guardar
	String photoName = "DemoPicture";

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Obtiene el nombre del intent
		photoName = getIntent().getStringExtra("nombre");
		Log.i("CAM", "Nombre: " + photoName);

		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.camera);
		mSurfaceView = (SurfaceView) findViewById(R.id.surface_camera);
		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.addCallback((Callback) this);
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		mPreviewRunning = false;

		cameraClick = (Button) findViewById(R.id.cameraClick);
		cameraClick.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				cameraClick.setEnabled(true);
				mCamera.takePicture(shutter, raw, jpeg);
			}
		});

		// Obtencion del entorno del dispositivo de almacenamiento
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// We can read and write the media
			mExternalStorageAvailable = mExternalStorageWriteable = true;
			path = Environment.getExternalStorageDirectory();
			Log.i("CAM", "escr path: " + path.getAbsolutePath());
			file = new File(path, photoName);
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// We can only read the media
			mExternalStorageAvailable = true;
			mExternalStorageWriteable = false;
		} else {
			// Something else is wrong. It may be one of many other states, but
			// all we need
			// to know is we can neither read nor write
			mExternalStorageAvailable = mExternalStorageWriteable = false;
		}

	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		if (mPreviewRunning) {
			mCamera.stopPreview();
		}

		Camera.Parameters p = mCamera.getParameters();
		p.setPreviewSize(240, 160);
		mCamera.setParameters(p);
		try {
			mCamera.setPreviewDisplay(holder);
		} catch (IOException e) {
			e.printStackTrace();
		}
		mCamera.startPreview();
		mPreviewRunning = true;

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		mCamera = Camera.open();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		mCamera.stopPreview();
		mPreviewRunning = false;
		mCamera.release();
	}

	ShutterCallback shutter = new ShutterCallback() {
		public void onShutter() {
		}
	};

	PictureCallback raw = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
		}
	};

	PictureCallback jpeg = new PictureCallback() {
		public void onPictureTaken(byte[] imageData, Camera c) {
			imagen = BitmapFactory.decodeByteArray(imageData, 0,
					imageData.length);
			try {
				OutputStream os = new FileOutputStream(file);
				os.write(imageData);
				os.close();
			} catch (IOException e) {
			}
			finish();
		}
	};

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
}
