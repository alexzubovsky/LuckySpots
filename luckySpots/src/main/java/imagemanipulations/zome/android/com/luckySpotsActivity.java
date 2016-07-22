package imagemanipulations.zome.android.com;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaActionSound;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/*
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
*/


public class luckySpotsActivity extends Activity implements CameraBridgeViewBase.OnLongClickListener, CameraBridgeViewBase.OnTouchListener, CvCameraViewListener2 {
	private static final String TAG = "LuckySpots::Activity";

	public static final int VIEW_MODE_RGBA = 0;

	public static final int VIEW_MODE_HIST = 1;
	public static final int VIEW_MODE_CANNY = 2;
	public static final int VIEW_MODE_SEPIA = 3;
	public static final int VIEW_MODE_SOBEL = 4;
	public static final int VIEW_MODE_ZOOM = 5;
	public static final int VIEW_MODE_PIXELIZE = 6;
	public static final int VIEW_MODE_POSTERIZE = 7;
	public static final int VIEW_MODE_THRESHOLD_ZERO = 8;
	public static final int VIEW_MODE_THRESHOLD_SPOT = 9;
	//public static final int VIEW_MODE_THRESHOLD_SPOT_AUX = 10;
	//saving values
	public static final String THRESHOLD_DETECTION_PERCENTAGE	= "THRESHOLD_DETECTION_PERCENTAGE";
	public static final String THRESHOLD_WINDOW_RED	= "THRESHOLD_WINDOW_RED";
	public static final String THRESHOLD_WINDOW_GREEN	= "THRESHOLD_WINDOW_GREEN";
	public static final String THRESHOLD_WINDOW_BLUE	= "THRESHOLD_WINDOW_BLUE";
	public static final String THRESHOLD_WINDOW_RED_CHECK	= "THRESHOLD_WINDOW_RED_CHECK";
	public static final String THRESHOLD_WINDOW_GREEN_CHECK	= "THRESHOLD_WINDOW_GREEN_CHECK";
	public static final String THRESHOLD_WINDOW_BLUE_CHECK	= "THRESHOLD_WINDOW_BLUE_CHECK";
	//public static final String THRESHOLD_SPOT_VIEW_MODE_ABSOLUTE	= "THRESHOLD_SPOT_VIEW_MODE_ABSOLUTE";
	public static final String THRESHOLD_SPOT_VIEW_MODE_BY_AREA	= "THRESHOLD_SPOT_VIEW_MODE_BY_AREA";
	public static final String IN_FRAME_ZOOM_VALUE	= "IN_FRAME_ZOOM_VALUE";
	public static final String IN_FRAME_RESOLUTION	= "IN_FRAME_RESOLUTION";

	private String VIEW_MODE_VIEW_MODE;
	private String THRESHOLD_SAVING_PATH_PICTURE;
	private String THRESHOLD_SAVING_PATH_VIDEO;
	private String CHECK_BOX_HISTOGRAM;
	private String MASK_DETECTION_CHANNEL;
	private String CURRENT_RESOLUTION;
	private String THRESHOLD_SPOT_ACCUMULATION_WEIGHT;
	private String THRESHOLD_WINDOW_INTEGRATION_DEEP;
	private String THRESHOLD_SHOWING_PERCENTAGE;
	private String MASK_BACKGROUND_VALUE;
	private ArrayList<String> savablePreferences;
	private static final Point THRESHOLD_DETECTION_PERCENTAGE_BOUNDARIES = new Point(10, 90);
	private static final Point THRESHOLD_SPOT_WEIGHT_BOUNDARIES = new Point(0.01, 99.99);
	private static final Point THRESHOLD_INTEGRATION_BOUNDARIES = new Point(1, 16);
	private static final int THRESHOLD_STRUCT_INDEX = 0;
	private static final int THRESHOLD_STRUCT_VALUE = 1;
	private static final int THRESHOLD_STRUCT_ID = 2;
	private static final int THRESHOLD_STRUCT_RED = 3;
	private static final int THRESHOLD_STRUCT_GREEN = 4;
	private static final int THRESHOLD_STRUCT_BLUE = 5;
	private static final int THRESHOLD_STRUCT_INDICATOR_ID = 6;
	private static final int MY_PERMISSIONS_REQUEST_CAMERA = 1;
	private static final int MY_PERMISSIONS_REQUEST_WRITE = 2;
	private static final int PERMISSION_REQUEST_REJECTED = -1;
	private static final int PERMISSION_REQUEST_WAITING = 0;
	private static final int PERMISSION_REQUEST_GRUNTED = 1;
	private MenuItem mItemPreviewRGBA;
	private MenuItem mItemPreviewThresholdZero;
	private MenuItem mItemPreviewThresholdSpot;
	//private MenuItem mItemPreviewThresholdSpotAux;
	private MenuItem mItemPreviewHist;
	private MenuItem mItemPreviewCanny;
	private MenuItem mItemPreviewSepia;
	private MenuItem mItemPreviewSobel;
	private MenuItem mItemPreviewZoom;
	private MenuItem mItemPreviewPixelize;
	private MenuItem mItemPreviewPosterize;
	//private CameraBridgeViewBase mOpenCvCameraView;
	private OpenCVJavaCameraViewExtension mOpenCvCameraView;

	private final Point THRESHOLD_SHOWING_PERCENTAGE_BOUNDARIES = new Point(10, 100);
	private Size mSize0;

	private Mat mIntermediateMat;
	private Mat mRgbaF;
	private Mat mRgbaT;

	private Mat mMat0;
	private MatOfInt mChannels[];
	private MatOfInt mHistSize;
	private int mHistSizeNum = 25;
	private MatOfFloat mRanges;
	private Scalar mColorsRGB[];
	private Scalar mColorsHue[];
	private Scalar mWhilte;
	private Point mP1;
	private Point mP2;
	private float mBuff[];
	private Mat mSepiaKernel;

	private static int viewMode = VIEW_MODE_THRESHOLD_SPOT;//VIEW_MODE_THRESHOLD_ZERO;//VIEW_MODE_RGBA;

	private boolean mOnCreateFinished = false;
	private boolean mOnResumeCalledBeforeOnCreateFinished = false;

	private final BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
				case LoaderCallbackInterface.SUCCESS: {
					CondLog('i', "OpenCV loaded successfully");
					mOpenCvCameraView.enableView();
				}
				break;
				default: {
					super.onManagerConnected(status);
				}
				break;
			}
		}
	};
	//Threshold fields
	private int mThresholdDetectionPercentageSize = 30;
	private int mThresholdShowingPercentageSize = mThresholdDetectionPercentageSize + 10;
	private boolean mResizingThresholdWindowInProgress = false;
	private boolean mResizingShowingWindowInProgress = false;
	private int mThresholdIntegrationDeep = 3;
	private double mThresholdSpotAccumulationWeight = 10.00;
	private ArrayList<Mat> mThresholdFramesIntegrator = new ArrayList<>(mThresholdIntegrationDeep);
	private Mat mLastThresholdWindowContent = null;
	private final boolean mLogIsEnabled = false;
	private boolean mVideoRecordingOngoing = false;
	private boolean mVideoRecordingPaused = false;
	private boolean mStopVideoRecording = false;
	private String mDirectoryPathPicture;
	private String mDirectoryPathVideo;
	//private boolean mThresholdViewModeAbsolute = false;
	//private boolean mThresholdViewModeByArea = false;
	private boolean mMakeSnapshot = false;
	private int mInFrameZoomValue = 100;
	private String mInFrameResolution = "";
	private boolean mCheckBoxHistogram;
	private boolean mCheckBoxMaskDetectionChannel;

	private List<android.util.Size> mResolutionList;
	private MenuItem[] mEffectMenuItems;
	private SubMenu mColorEffectsMenu;
	private MenuItem[] mResolutionMenuItems;
	private SubMenu mResolutionMenu;
	private String mCurrentResolution;


	//////////////////

	public luckySpotsActivity(MenuItem mItemPreviewCanny) {
		this.mItemPreviewCanny = mItemPreviewCanny;
		CondLog('i', "Instantiated new " + this.getClass());

	}
	public luckySpotsActivity() {
		CondLog('i', "Instantiated new " + this.getClass());
	}


	private int getPermission(String permission, int myPermissionRequestId) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (getApplicationContext().checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
				// Should we show an explanation?
				if (shouldShowRequestPermissionRationale(permission)) {
					return PERMISSION_REQUEST_REJECTED;
					// Show an expanation to the user *asynchronously* -- don't block
					// this thread waiting for the user's response! After the user
					// sees the explanation, try again to request the permission.
				} else {
					// No explanation needed, we can request the permission. myPermissionRequestId is an
					requestPermissions(new String[]{permission}, myPermissionRequestId);// app-defined int constant. The callback method gets the result of the request.
					return PERMISSION_REQUEST_WAITING;
				}
			}
		}
		return PERMISSION_REQUEST_GRUNTED;
	}

	private boolean mWriteAccessGrunted = true;

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
		switch (requestCode) {
			case MY_PERMISSIONS_REQUEST_CAMERA: {

				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {// If request is cancelled, the result arrays are empty.
					continueOnCreate();// permission was granted, Do the contacts-related task you need to do.
				} else {// permission denied! Disable the functionality that depends on this permission.
					setContentView(R.layout.no_permission_message);// this thread waiting for the user's response! After the user
				}
				break;
			}
			case MY_PERMISSIONS_REQUEST_WRITE: {

				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {// If request is cancelled, the result arrays are empty.
					;// permission was granted, Do the contacts-related task you need to do.
				} else {
					mWriteAccessGrunted = false;// permission denied! Disable the functionality that depends on this permission.
				}
				break;
			}
			// other 'case' lines to check for other
			// permissions this app might request
		}
	}

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceStateBundle) {
		CondLog('i', "called onCreate");
		super.onCreate(savedInstanceState = savedInstanceStateBundle);
		switch (getPermission(Manifest.permission.CAMERA, MY_PERMISSIONS_REQUEST_CAMERA)) {
			case PERMISSION_REQUEST_REJECTED:// Show an expanation to the user *asynchronously* -- don't block
				setContentView(R.layout.no_permission_message);// this thread waiting for the user's response! After the user
				return;// sees the explanation, try again to request the permission.
			case PERMISSION_REQUEST_WAITING:
				return;
			case PERMISSION_REQUEST_GRUNTED:
				continueOnCreate();
		}
		switch (getPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, MY_PERMISSIONS_REQUEST_WRITE)) {
			case PERMISSION_REQUEST_REJECTED:// Show an expanation to the user *asynchronously* -- don't block
				mWriteAccessGrunted = false;// this thread waiting for the user's response! After the user
			case PERMISSION_REQUEST_WAITING:
			case PERMISSION_REQUEST_GRUNTED:
				break;// sees the explanation, try again to request the permission.
		}
	}
	private void initSavablePreferences(){
		THRESHOLD_SAVING_PATH_PICTURE	= getString(R.string.THRESHOLD_SAVING_PATH_PICTURE);
		THRESHOLD_SAVING_PATH_VIDEO	= getString(R.string.THRESHOLD_SAVING_PATH_VIDEO);
		CHECK_BOX_HISTOGRAM	= getString(R.string.CHECK_BOX_HISTOGRAM);
		MASK_DETECTION_CHANNEL	= getString( R.string.MASK_DETECTION_CHANNEL);
		CURRENT_RESOLUTION = getString(R.string.CURRENT_RESOLUTION);
		VIEW_MODE_VIEW_MODE = getString( R.string.VIEW_MODE_AS_STRING/*VIEW_MODE_VIEW_MODE*/);
		THRESHOLD_WINDOW_INTEGRATION_DEEP	= getString(R.string.THRESHOLD_WINDOW_INTEGRATION_DEEP);
		THRESHOLD_SPOT_ACCUMULATION_WEIGHT	= getString(R.string.THRESHOLD_SPOT_ACCUMULATION_WEIGHT);
		THRESHOLD_SHOWING_PERCENTAGE = getString(R.string.THRESHOLD_SHOWING_PERCENTAGE);
		MASK_BACKGROUND_VALUE = getString(R.string.MASK_BACKGROUND_VALUE);
		savablePreferences = new ArrayList<>(20);
		savablePreferences.add(THRESHOLD_DETECTION_PERCENTAGE);
		savablePreferences.add(THRESHOLD_SHOWING_PERCENTAGE);
		savablePreferences.add(MASK_BACKGROUND_VALUE);
		savablePreferences.add(THRESHOLD_WINDOW_INTEGRATION_DEEP);
		savablePreferences.add(THRESHOLD_WINDOW_RED);
		savablePreferences.add(THRESHOLD_WINDOW_GREEN);
		savablePreferences.add(THRESHOLD_WINDOW_BLUE);
		savablePreferences.add(THRESHOLD_WINDOW_RED_CHECK);
		savablePreferences.add(THRESHOLD_WINDOW_GREEN_CHECK);
		savablePreferences.add(THRESHOLD_WINDOW_BLUE_CHECK);
		savablePreferences.add(THRESHOLD_SPOT_ACCUMULATION_WEIGHT);
		savablePreferences.add(THRESHOLD_SAVING_PATH_PICTURE);
		savablePreferences.add(THRESHOLD_SAVING_PATH_VIDEO);
		//savablePreferences.add(THRESHOLD_SPOT_VIEW_MODE_ABSOLUTE);
		//savablePreferences.add(THRESHOLD_SPOT_VIEW_MODE_BY_AREA);
		savablePreferences.add(VIEW_MODE_VIEW_MODE);
		savablePreferences.add(IN_FRAME_ZOOM_VALUE);
		savablePreferences.add(IN_FRAME_RESOLUTION);
		savablePreferences.add(CHECK_BOX_HISTOGRAM);
		savablePreferences.add(MASK_DETECTION_CHANNEL);
		savablePreferences.add(CURRENT_RESOLUTION);

	}
	public void closeApplication() {
		this.finish();
	}

	private Bundle savedInstanceState;

	private void continueOnCreate() {
		mOnCreateFinished = true;
		initSavablePreferences();
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.image_surface_view);

		mOpenCvCameraView = /*(CameraBridgeViewBase)*/ (OpenCVJavaCameraViewExtension) findViewById(R.id.image_surface_view);
		mOpenCvCameraView.setVisibility(CameraBridgeViewBase.VISIBLE);
		mOpenCvCameraView.setCvCameraViewListener(this);
		mOpenCvCameraView.setOnTouchListener(this);
		mOpenCvCameraView.setOnLongClickListener(this);
		if (mOnResumeCalledBeforeOnCreateFinished)
			this.onResume();
	}

	private boolean handleSavedPreferences(boolean readMode, Object preferencesHolder) {
		Bundle instState = preferencesHolder instanceof Bundle? (Bundle)preferencesHolder : null;
		SharedPreferences pref = preferencesHolder instanceof SharedPreferences? (SharedPreferences)preferencesHolder : null;
		SharedPreferences.Editor prefEditor = !readMode && pref != null ? pref.edit() : null;
		boolean stringChangeDetected=false;
		boolean intChangeDetected=false;
		boolean booleanChangeDetected=false;
		String tempString;
		int tempInt;
		boolean tempBoolean;
		double tempDouble;
		if( pref== null && instState == null && prefEditor == null)
			throw new RuntimeException((preferencesHolder == null ? "No": preferencesHolder.getClass() + " Wrong")+ " object type for handleSavedPreferences");
		for(int i=0;savablePreferences.size() > i; i++){
			String preferenceName = savablePreferences.get(i);
			switch(preferenceName) {
				case THRESHOLD_DETECTION_PERCENTAGE:
					if(readMode) {
						mThresholdDetectionPercentageSize = setBoundaries(savedPreferencesGetInt(pref, instState, preferenceName, 10), THRESHOLD_DETECTION_PERCENTAGE_BOUNDARIES);
						THRESHOLD_SHOWING_PERCENTAGE_BOUNDARIES.x = mThresholdDetectionPercentageSize + 10;
					}else if (pref == null)
						instState.putInt(preferenceName, mThresholdDetectionPercentageSize);
					else
						prefEditor.putInt(preferenceName, mThresholdDetectionPercentageSize);
					break;
				case THRESHOLD_WINDOW_RED:
					if(readMode)
						mThresholdRed = setBoundaries(0, savedPreferencesGetInt(pref, instState, preferenceName, 0), 255);
					else if (pref == null)
						instState.putInt(preferenceName, mThresholdRed);
					else
						prefEditor.putInt(preferenceName,mThresholdRed );
					break;
				case THRESHOLD_WINDOW_GREEN:
					if(readMode)
						mThresholdGreen = setBoundaries(0, savedPreferencesGetInt(pref, instState, preferenceName, 0), 255);
					else if (pref == null)
						instState.putInt(preferenceName, mThresholdGreen);
					else
						prefEditor.putInt(preferenceName, mThresholdGreen);
					break;
				case THRESHOLD_WINDOW_BLUE:
					if(readMode)
						mThresholdBlue = setBoundaries(0, savedPreferencesGetInt(pref, instState, preferenceName, 0), 255);
					else if (pref == null)
						instState.putInt(preferenceName, mThresholdBlue);
					else
						prefEditor.putInt(preferenceName, mThresholdBlue);
					break;
				case THRESHOLD_WINDOW_RED_CHECK:
					if(readMode)
						mThresholdRedCheck = setCheckBox(R.id.checkBoxRed, savedPreferencesGetBoolean(pref, instState, preferenceName, false));
					else if (pref == null)
						instState.putBoolean(preferenceName, mThresholdRedCheck);
					else
						prefEditor.putBoolean(preferenceName, mThresholdRedCheck);
					break;
				case THRESHOLD_WINDOW_GREEN_CHECK:
					if(readMode)
						mThresholdGreenCheck = setCheckBox(R.id.checkBoxGreen, savedPreferencesGetBoolean(pref, instState, preferenceName, false));
					else if (pref == null)
						instState.putBoolean(preferenceName, mThresholdGreenCheck);
					else
						prefEditor.putBoolean(preferenceName, mThresholdGreenCheck);
					break;
				case THRESHOLD_WINDOW_BLUE_CHECK:
					if(readMode)
						mThresholdBlueCheck = setCheckBox(R.id.checkBoxBlue, savedPreferencesGetBoolean(pref, instState, preferenceName, false));
					else if (pref == null)
						instState.putBoolean(preferenceName, mThresholdBlueCheck);
					else
						prefEditor.putBoolean(preferenceName, mThresholdBlueCheck);
					break;
				/*case THRESHOLD_SPOT_VIEW_MODE_ABSOLUTE:
					if(readMode)
						mThresholdViewModeAbsolute = savedPreferencesGetBoolean(pref, instState, preferenceName, false);
					else if (pref == null)
						instState.putBoolean(preferenceName, mThresholdViewModeAbsolute);
					else
						prefEditor.putBoolean(preferenceName, mThresholdViewModeAbsolute);
					break;*/
				/*case THRESHOLD_SPOT_VIEW_MODE_BY_AREA:
					if(readMode)
						mThresholdViewModeByArea = savedPreferencesGetBoolean(pref, instState, preferenceName, false);
					else if (pref == null)
						instState.putBoolean(preferenceName, mThresholdViewModeByArea);
					else
						prefEditor.putBoolean(preferenceName,mThresholdViewModeByArea );
					break;*/
				case IN_FRAME_ZOOM_VALUE:
					if(readMode)
						mInFrameZoomValue = setBoundaries(10, savedPreferencesGetInt(pref, instState, preferenceName, 100), 100);
					else if (pref == null)
						instState.putInt(preferenceName,mInFrameZoomValue );
					else
						prefEditor.putInt(preferenceName, mInFrameZoomValue);
					break;
				case IN_FRAME_RESOLUTION:
					if(readMode)
						mInFrameResolution = savedPreferencesGetString(pref, instState, preferenceName, "");
					else if (pref == null)
						instState.putString(preferenceName, mInFrameResolution);
					else
						prefEditor.putString(preferenceName, mInFrameResolution);
					break;
				default:
					if(readMode){
						if (preferenceName.equals(THRESHOLD_SAVING_PATH_PICTURE)) {
							tempString = savedPreferencesGetString(pref, instState, preferenceName, "luckyspots/pictures");
							if(!tempString.equals(mDirectoryPathPicture)){
								stringChangeDetected = true;
								mDirectoryPathPicture = tempString;
							}
						}else if(preferenceName.equals( THRESHOLD_SAVING_PATH_VIDEO)) {
							tempString = savedPreferencesGetString(pref, instState, preferenceName, "luckyspots/video");
							if(!tempString.equals(mDirectoryPathVideo)) {
								stringChangeDetected = true;
								mDirectoryPathVideo = tempString;
							}
						}else if(preferenceName.equals(CHECK_BOX_HISTOGRAM)) {
							tempBoolean = savedPreferencesGetBoolean(pref, instState, preferenceName, false);
							if(tempBoolean != mCheckBoxHistogram){
								booleanChangeDetected = true;
								mCheckBoxHistogram = tempBoolean;
							}
						}else if(preferenceName.equals( MASK_DETECTION_CHANNEL)) {
							tempBoolean = savedPreferencesGetBoolean(pref, instState, preferenceName, true);
							if(tempBoolean != mCheckBoxMaskDetectionChannel){
								booleanChangeDetected = true;
								mCheckBoxMaskDetectionChannel= tempBoolean;
							}
						}else if(preferenceName.equals(CURRENT_RESOLUTION)) {
							tempString = savedPreferencesGetString(pref, instState, preferenceName, "");
							if(!tempString.equals(mCurrentResolution)){
								stringChangeDetected = true;
								mCurrentResolution = tempString;
							}
						}else if(preferenceName == VIEW_MODE_VIEW_MODE) {
							tempInt = Integer.parseInt(savedPreferencesGetString(pref, instState, preferenceName, String.valueOf(VIEW_MODE_THRESHOLD_SPOT)));
							if(tempInt != viewMode){
								intChangeDetected = true;
								viewMode= tempInt;
							}
						}else if(preferenceName == THRESHOLD_SPOT_ACCUMULATION_WEIGHT){
							tempDouble = setBoundaries(Double.parseDouble(savedPreferencesGetString(pref, instState, preferenceName, "10.00")), THRESHOLD_SPOT_WEIGHT_BOUNDARIES);
							if(tempDouble != mThresholdSpotAccumulationWeight) {
								intChangeDetected = true;
								mThresholdSpotAccumulationWeight = tempDouble;
							}
						}else if(preferenceName == THRESHOLD_WINDOW_INTEGRATION_DEEP){
							tempInt = setBoundaries(Integer.parseInt(savedPreferencesGetString(pref, instState, preferenceName, "3")), THRESHOLD_INTEGRATION_BOUNDARIES);
							if(tempInt != mThresholdIntegrationDeep) {
								intChangeDetected = true;
								mThresholdIntegrationDeep = tempInt;
							}
						}else if(preferenceName == THRESHOLD_SHOWING_PERCENTAGE){
							tempInt = setBoundaries(Integer.parseInt(savedPreferencesGetString(pref, instState, preferenceName, "0")), THRESHOLD_SHOWING_PERCENTAGE_BOUNDARIES);
							if(tempInt != mThresholdShowingPercentageSize) {
								intChangeDetected = true;
								mThresholdShowingPercentageSize = tempInt;
							}
						}else if(preferenceName == MASK_BACKGROUND_VALUE){
							tempInt = setBoundaries(0, Integer.parseInt(savedPreferencesGetString(pref, instState, preferenceName, "0")), 100);
							if(tempInt != mMaskBackgroundValue) {
								intChangeDetected = true;
								mMaskBackgroundValue = tempInt;
							}
						}else
							throw new RuntimeException(noSupportForPreferenceName(preferenceName));
					}else{
						String defaultString = null;
						Boolean defaultBoolean = null;
						Integer defaultInteger = null;
						if (preferenceName == THRESHOLD_SAVING_PATH_PICTURE)
							defaultString = mDirectoryPathPicture;
						else if(preferenceName == THRESHOLD_SAVING_PATH_VIDEO)
							defaultString  = mDirectoryPathVideo;
						else if(preferenceName == CHECK_BOX_HISTOGRAM)
							defaultBoolean = mCheckBoxHistogram;
						else if(preferenceName == MASK_DETECTION_CHANNEL)
							defaultBoolean = mCheckBoxMaskDetectionChannel;
						else if(preferenceName == CURRENT_RESOLUTION)
							defaultString = mCurrentResolution;
						else if(preferenceName == VIEW_MODE_VIEW_MODE)
							defaultString = String.valueOf(viewMode);
						else if(preferenceName == THRESHOLD_SPOT_ACCUMULATION_WEIGHT)
							defaultString = String.valueOf(mThresholdSpotAccumulationWeight);
						else if(preferenceName == THRESHOLD_WINDOW_INTEGRATION_DEEP)
							defaultString = String.valueOf(mThresholdIntegrationDeep);
						else if(preferenceName == THRESHOLD_SHOWING_PERCENTAGE)
							defaultString = String.valueOf(mThresholdShowingPercentageSize);
						else if(preferenceName == MASK_BACKGROUND_VALUE)
							defaultString = String.valueOf(mMaskBackgroundValue);
						if(defaultString == null && defaultBoolean==null && defaultInteger==null)
							throw new RuntimeException(noSupportForPreferenceName(preferenceName));
						else if (pref == null) {
							if (defaultBoolean != null)
								instState.putBoolean(preferenceName, defaultBoolean);
							else if(defaultString != null)
								instState.putString(preferenceName, defaultString);
							else if(defaultInteger != null)
								instState.putInt(preferenceName, defaultInteger);
						}else{
							if (defaultBoolean != null)
								prefEditor.putBoolean(preferenceName, defaultBoolean);
							else if(defaultString != null)
								prefEditor.putString(preferenceName, defaultString);
							else if(defaultInteger != null)
								prefEditor.putInt(preferenceName, defaultInteger);
						}
					}
			}
		}
		if(prefEditor != null)
			prefEditor.apply();
		return stringChangeDetected || intChangeDetected || booleanChangeDetected;
	}
	private String noSupportForPreferenceName(String preferenceName){
		return "No support in handleSavedPreferences for setting name '"+preferenceName+"'";
	}
	private int savedPreferencesGetInt(SharedPreferences pref, Bundle instState, String name, int defaultValue){
		int tempInt;
		try{
			tempInt = pref==null ? instState.getInt(name, defaultValue) : pref.getInt(name, defaultValue);
		}catch(RuntimeException e) {
			String defaultString = String.valueOf(defaultValue);
			tempInt = new Integer(pref==null ? instState.getString(name, defaultString) : pref.getString(name, defaultString));
		}
		return tempInt;
	}
	private String savedPreferencesGetString(SharedPreferences pref, Bundle instState, String name, String defaultValue){
		return (pref==null ? instState.getString(name, defaultValue) : pref.getString(name, defaultValue));
	}
	private Boolean savedPreferencesGetBoolean(SharedPreferences pref, Bundle instState, String name, boolean defaultValue){
		return (pref==null ? instState.getBoolean(name, defaultValue) : pref.getBoolean(name, defaultValue));
	}
	private int setBoundaries(int min, int value, int max) {
		return Math.min(max, Math.max(min, value));
	}

	private int setBoundaries(int value, Point boundaries) {
		return (int) Math.min(boundaries.y, Math.max(boundaries.x, value));
	}
	private double setBoundaries(double value, Point boundaries) {
		return Math.min(boundaries.y, Math.max(boundaries.x, value));
	}
	private void initCheckBoxes() {
		final int[] boxIds = {R.id.checkBoxRed, R.id.checkBoxBlue, R.id.checkBoxGreen};
		int numberOfVisible = 0;
		Boolean checked;
		for (int boxId : boxIds) {
			CheckBox checkbox = (CheckBox) findViewById(boxId);
			if (checkbox != null) {
				if (numberOfVisible == 0) {
					checked = getThresholdCheckField(boxId);
					if (checked)
						numberOfVisible++;
				} else
					setThresholdCheckField(boxId, checked = false);
				checkbox.setChecked(checked);
				hideCheckBoxAndSeekBar(boxId, !checked);
				checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						int thisBoxId = buttonView.getId();
						setThresholdCheckField(thisBoxId, isChecked);
						for (int boxId : boxIds) {
							hideCheckBoxAndSeekBar(boxId, thisBoxId != boxId && isChecked);
						}
					}
				});
			}
		}
		if (numberOfVisible == 0) {
			CheckBox checkbox = hideCheckBoxAndSeekBar(boxIds[0], false);
			if(checkbox != null)
				checkbox.setChecked(true);
		}
	}

	private CheckBox hideCheckBoxAndSeekBar(int id, Boolean hide) {
		CheckBox checkBox = (CheckBox) findViewById(id);
		if(checkBox != null) {
			LinearLayout parent = (LinearLayout) checkBox.getParent();
			parent.setVisibility(hide ? View.GONE : View.VISIBLE);
		}
		return checkBox;
	}

	private void setThresholdCheckField(int id, Boolean checked) {
		switch (id) {
			case R.id.checkBoxRed:
				mThresholdRedCheck = checked;
				break;
			case R.id.checkBoxGreen:
				mThresholdGreenCheck = checked;
				break;
			case R.id.checkBoxBlue:
				mThresholdBlueCheck = checked;
				break;
		}
	}

	private Boolean getThresholdCheckField(int id) {
		switch (id) {
			case R.id.checkBoxRed:
				return mThresholdRedCheck;
			case R.id.checkBoxGreen:
				return mThresholdGreenCheck;
			case R.id.checkBoxBlue:
				return mThresholdBlueCheck;
		}
		return false;
	}

	private int[] getThresholdCheckStructure() {
		int[] struct = {0, mThresholdRed, R.id.checkBoxRed, 255, 0, 0, R.id.seekBarRedValue};
		if (!mThresholdRedCheck) {
			if (mThresholdGreenCheck) {
				struct[THRESHOLD_STRUCT_INDEX] = 1;
				struct[THRESHOLD_STRUCT_VALUE] = mThresholdGreen;
				struct[THRESHOLD_STRUCT_ID] = R.id.checkBoxGreen;
				struct[THRESHOLD_STRUCT_RED] = 0;
				struct[THRESHOLD_STRUCT_GREEN] = 255;
				struct[THRESHOLD_STRUCT_INDICATOR_ID] = R.id.seekBarGreenValue;
			} else if (mThresholdBlueCheck) {
				struct[THRESHOLD_STRUCT_INDEX] = 2;
				struct[THRESHOLD_STRUCT_VALUE] = mThresholdBlue;
				struct[THRESHOLD_STRUCT_ID] = R.id.checkBoxBlue;
				struct[THRESHOLD_STRUCT_RED] = 0;
				struct[THRESHOLD_STRUCT_BLUE] = 255;
				struct[THRESHOLD_STRUCT_INDICATOR_ID] = R.id.seekBarBlueValue;
			}
		}
		return struct;
	}

	private Boolean setCheckBox(int id, Boolean checked) {
		CheckBox checkbox = (CheckBox) findViewById(id);
		if (checkbox != null)
			checkbox.setChecked(checked);
		return checked;
	}

	private Boolean getCheckBoxChecked(int id) {
		CheckBox checkbox = (CheckBox) findViewById(id);
		return (checkbox != null && checkbox.isChecked());
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		handleSavedPreferences(false, outState);
		savedInstanceState = null;
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
		savedInstanceState = null;
		handleSavedPreferences(false, PreferenceManager.getDefaultSharedPreferences(getApplicationContext())/*this.getPreferences(Context.MODE_PRIVATE)*/);
		PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!mOnCreateFinished) {
			mOnResumeCalledBeforeOnCreateFinished = true;
		} else {
			mOnResumeCalledBeforeOnCreateFinished = false;
			if (!OpenCVLoader.initDebug()) {
				CondLog('d', "Internal OpenCV library not found. Using OpenCV Manager for initialization");
				OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
			} else {
				CondLog('d', "OpenCV library found inside package. Using it!");
				mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
			}
			if(handleSavedPreferences(true,savedInstanceState != null ? savedInstanceState : PreferenceManager.getDefaultSharedPreferences(getApplicationContext())/*this.getPreferences(Context.MODE_PRIVATE)*/)) {
				resetLastThresholdWindowContent();
				thresholdContainersVisibility();
				thresholdIntegrationDeepChanged(null);//Show proper value
			}
		}
	}

	public void onDestroy() {
		super.onDestroy();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	@Override
	protected void onStop() {
		super.onStop();
//		System.exit(0);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Intent intent = new Intent();
		intent.setClassName(this, "imagemanipulations.zome.android.com.ThisSettingsActivity");
		startActivity(intent);
		//new MainMenuIntent().execute(this);

		/*MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);*/
		/*
		CondLog('i', "called onCreateOptionsMenu");
		mItemPreviewRGBA = menu.add("Preview RGBA");
		//mItemPreviewThresholdZero = menu.add("Threshold");
		mItemPreviewThresholdSpot = menu.add("Mask Spots");
		//mItemPreviewThresholdSpotAux = menu.add("Mask Spots Mod");
		mItemPreviewHist = menu.add("Histograms");
		//mItemPreviewCanny = menu.add("Canny");
		//mItemPreviewSepia = menu.add("Sepia");
		//mItemPreviewSobel = menu.add("Sobel");
		//mItemPreviewZoom = menu.add("Zoom");
		//mItemPreviewPixelize = menu.add("Pixelize");
		//mItemPreviewPosterize = menu.add("Posterize");
		*/
		return false;
	}
	private void performOpenOptionsMenu(){
		this.openOptionsMenu();
	}
	/*
	private class MainMenuIntent extends AsyncTask<Context, Integer, Long>{
		@Override
		protected Long doInBackground(Context... contexts) {
			Intent intent = new Intent();
			intent.setClassName(contexts[0], "imagemanipulations.zome.android.com.ThisSettingsActivity");
			startActivity(intent);
			return null;
		}
	}*/
	private int mThresholdRed = -1;
	private int mThresholdGreen = 0;
	private int mThresholdBlue = 0;
	private Boolean mThresholdRedCheck = true;
	private Boolean mThresholdGreenCheck = false;
	private Boolean mThresholdBlueCheck = false;
	private int mMaskBackgroundValue = 0;

	private SeekBar initSeekBar(View view, int initValue, int indicatorId){
		updateIndicatorValue(indicatorId,initValue);
		return initSeekBar(view, initValue, false);
	}

	private void updateIndicatorValue(int indicatorId, int value) {
		TextView indicator = (TextView) findViewById(indicatorId);
		if(indicator != null)
			indicator.setText(String.valueOf(value));
	}

	private SeekBar initSeekBar(View view, int initValue, boolean noColor) {
		SeekBar seekBarView	= null;
		if (view != null) {
			initCheckBoxes();
			seekBarView = (SeekBar) view;//mThresholdFragment.findViewById(R.id.seekBarBlue);
			seekBarView.setProgress(initValue);
			if(!noColor)
				seekBarView.setProgressDrawable(new ColorDrawable(getSeekBarColor(seekBarView.getId())));
			seekBarView.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

				/**
				 * Notification that the progress level has changed. Clients can use the fromUser parameter
				 * to distinguish user-initiated changes from those that occurred programmatically.
				 *
				 * @param seekBar  The SeekBar whose progress has changed
				 * @param progress The current progress level. This will be in the range 0..max where max
				 *                 was set by {@@link ProgressBar#setMax(int)}. (The default value for max is 100.)
				 * @param fromUser True if the progress change was initiated by the user.
				 */
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					if (fromUser && progress >= 0 && progress < 256) {
						switch (seekBar.getId()) {
							case R.id.seekBarRed:
								updateIndicatorValue(R.id.seekBarRedValue, mThresholdRed = progress);
								break;
							case R.id.seekBarBlue:
								updateIndicatorValue(R.id.seekBarBlueValue, mThresholdBlue = progress);
								break;
							case R.id.seekBarGreen:
								updateIndicatorValue(R.id.seekBarGreenValue, mThresholdGreen = progress);
								break;
							case R.id.seekBarMaskBackground:
								updateIndicatorValue(R.id.seekBarMaskBackgroundValue, mMaskBackgroundValue = progress);
								break;
							case R.id.seekBar_zoom:
								updateIndicatorValue(R.id.text_zoom_value,(mInFrameZoomValue = setBoundaries(10,100 - progress,100)));
								engageOnScreenControls();
								break;
						}
					}
					CondLog('i', "onProgressChanged " + getSeekBarId(seekBar));
				}

				/**
				 * Notification that the user has started a touch gesture. Clients may want to use this
				 * to disable advancing the seekbar.
				 *
				 * @param seekBar The SeekBar in which the touch gesture began
				 */
				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
					CondLog('i', "StartTrackingTouch " + getSeekBarId(seekBar));
				}

				/**
				 * Notification that the user has finished a touch gesture. Clients may want to use this
				 * to re-enable advancing the seekbar.
				 *
				 * @param seekBar The SeekBar in which the touch gesture began
				 */
				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
					CondLog('i', "StopTrackingTouch " + getSeekBarId(seekBar));
				}
			});
		}
		return seekBarView;
	}
	private String resolutionToString(Camera.Size size){
		return (Integer.toString(size.width)) + "x"+(Integer.toString(size.height));
	}
	private String getSeekBarId(SeekBar seekBar) {
		String id;
		switch (seekBar.getId()) {
			case R.id.seekBarRed:
				id = "red";
				break;
			case R.id.seekBarBlue:
				id = "Blue";
				break;
			case R.id.seekBarGreen:
				id = "Green";
				break;
			default:
				id = "Unknown";
		}
		return id;
	}

	private int getSeekBarColor(int id) {
		int color;
		switch (id) {
			case R.id.seekBarRed:
				color = Color.argb(40, 255, 0, 0);
				break;
			case R.id.seekBarGreen:
				color = Color.argb(40, 0, 255, 0);
				break;
			case R.id.seekBarBlue:
				color = Color.argb(40, 0, 0, 255);
				break;
			default:
				color = Color.argb(40, 100, 100, 100);
		}
		return color;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId())
		{
			case R.id.preferences:
				Intent intent = new Intent();
				intent.setClassName(this, "imagemanipulations.zome.android.com.ThisSettingsActivity");
				startActivity(intent);
				return true;
			default:
				CondLog('i', "called onOptionsItemSelected; selected item: " + item);

				if (item == mItemPreviewRGBA)
					viewMode = VIEW_MODE_RGBA;
				if (item == mItemPreviewHist)
					viewMode = VIEW_MODE_HIST;
				else if (item == mItemPreviewCanny)
					viewMode = VIEW_MODE_CANNY;
				else if (item == mItemPreviewSepia)
					viewMode = VIEW_MODE_SEPIA;
				else if (item == mItemPreviewSobel)
					viewMode = VIEW_MODE_SOBEL;
				else if (item == mItemPreviewZoom)
					viewMode = VIEW_MODE_ZOOM;
				else if (item == mItemPreviewPixelize)
					viewMode = VIEW_MODE_PIXELIZE;
				else if (item == mItemPreviewPosterize)
					viewMode = VIEW_MODE_POSTERIZE;
				else if (item == mItemPreviewThresholdSpot ||item == mItemPreviewThresholdZero) {
					viewMode = VIEW_MODE_THRESHOLD_SPOT;
					//mThresholdViewModeAbsolute = false;
					//mThresholdViewModeByArea = false;
					resetLastThresholdWindowContent();
					thresholdIntegrationDeepChanged(null);//Show proper value
				}
				else
					return super.onOptionsItemSelected(item);
				resetLastThresholdWindowContent();
				thresholdContainersVisibility();
				thresholdIntegrationDeepChanged(null);//Show proper value
				return true;
		}
	}

	private void CondLog(Character type, String mess) {
		if (mLogIsEnabled || type == 'e')
			switch (type) {
				case 'i':
					Log.i(TAG, mess);
					break;
				case 'd':
					Log.d(TAG, mess);
					break;
				case 'e':
					Log.e(TAG, mess);
					break;
				case 'w':
					Log.w(TAG, mess);
					break;
			}
	}

	private void thresholdContainersVisibility() {
		boolean visible = isModeThresholdRelated();
		setConditionalControlVisibility(thresholdActivityColorIds, visible, seekBarsInit);
		//setConditionalControlVisibility(thersholdActivityWindowSizeIds, visible, thresholdWindowSizeControlInit);
	}

	private int mThresholdIntegrationDeepTemp = -1;
	/*
		private Handler.Callback thresholdWindowSizeControlInit = new android.os.Handler.Callback() {

			@Override
			public boolean handleMessage(Message msg) {
				Object obj = msg.obj;
				if (obj != null) {
					View thresholdWindowSizeContainer = (View) obj;
					initSeekBarThresholdWindowSize(thresholdWindowSizeContainer.findViewById(R.id.seekBarSize), mThresholdDetectionPercentageSize);
				}
				return false;
			}
		};
	*/
	private final Handler.Callback seekBarsInit = new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			Object obj = msg.obj;
			if (obj != null) {
				View seekbarsContainer = (View) obj;
				initSeekBar(seekbarsContainer.findViewById(R.id.seekBarBlue), mThresholdBlue, R.id.seekBarBlueValue);
				initSeekBar(seekbarsContainer.findViewById(R.id.seekBarGreen), mThresholdGreen, R.id.seekBarGreenValue);
				initSeekBar(seekbarsContainer.findViewById(R.id.seekBarRed), mThresholdRed, R.id.seekBarRedValue);
				initSeekBar(seekbarsContainer.findViewById(R.id.seekBarMaskBackground), mMaskBackgroundValue, R.id.seekBarMaskBackgroundValue);
				initTextControl(R.id.threshold_deep_control_value, String.valueOf(viewMode==VIEW_MODE_THRESHOLD_ZERO ? mThresholdIntegrationDeep : mThresholdSpotAccumulationWeight));
				initTextControl(R.id.threshold_deep_control_less, null);
				initTextControl(R.id.threshold_deep_control_more, null);
			}
			return false;
		}
	};

	private void thresholdIntegrationDeepChanged(View lessMoreButton) {
		TextView text = (TextView) findViewById(R.id.threshold_deep_control_value);
		if (text != null) {
			double delta = lessMoreButton == null ? 0 : Double.parseDouble((String) lessMoreButton.getTag());
			if(viewMode== VIEW_MODE_THRESHOLD_SPOT && lessMoreButton!= null && delta == 0){
				showIntegrationNumberPickerDialog().show();
				//LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				//View container = inflater.inflate(R.layout.integration_number_picker, (ViewGroup) findViewById(R.id.main_layout));
			}
			else if (viewMode==VIEW_MODE_THRESHOLD_SPOT)
				updateThresholdSpotAccumulationWeight(mThresholdSpotAccumulationWeight + delta, text);
			else
				updateThresholdSpotAccumulationWeight(mThresholdIntegrationDeep + delta, text);


		}
	}
	private void updateThresholdSpotAccumulationWeight(double value, TextView text){
		mThresholdSpotAccumulationWeight = setBoundaries(value, THRESHOLD_SPOT_WEIGHT_BOUNDARIES);
		if(text == null)text = (TextView) findViewById(R.id.threshold_deep_control_value);
		StringBuffer buffer = new StringBuffer(String.valueOf(0.01 * Math.round(10000.0 + mThresholdSpotAccumulationWeight*100)));//to show leading and trailing zeros
		buffer.deleteCharAt(0);
		int length = buffer.length();
		if(length != 5) {
			while (length > 5)
				buffer.deleteCharAt(--length);
			while (length < 5) {
				buffer.append('0');
				length++;
			}
			mThresholdSpotAccumulationWeight = Double.parseDouble(String.valueOf(buffer));
		}
		text.setText(buffer);
	}
	private void updateThresholdSpotAccumulationWeight(int value, TextView text){
		mThresholdIntegrationDeep = setBoundaries(value, THRESHOLD_INTEGRATION_BOUNDARIES);
		text.setText(String.valueOf(mThresholdIntegrationDeep));
	}
	private int[] numberPickerIds = {
			R.id.integration_number_picker_value_0,
			R.id.integration_number_picker_value_1,
			R.id.integration_number_picker_value_2,
			R.id.integration_number_picker_value_3};
	private AlertDialog showIntegrationNumberPickerDialog(){
		LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View npView = inflater.inflate(R.layout.integration_number_picker, null);
		NumberPicker numberPicker;
		int position;
		CharSequence chars = String.valueOf(mThresholdSpotAccumulationWeight +100.001);//to have leading and trailing zeros
		for(int id :numberPickerIds){
			numberPicker = (NumberPicker)npView.findViewById(id);
			if(numberPicker != null){
				//setNumberPicker(numberPicker,chars.charAt(1+Integer.parseInt((String) numberPicker.getTag())));
				numberPicker.setMinValue(0);
				numberPicker.setMaxValue(9);
				numberPicker.setWrapSelectorWheel(true);
				position = Integer.parseInt((String) numberPicker.getTag());
				numberPicker.setValue(Integer.parseInt(String.valueOf(chars.charAt(1+position) )));
			}
		}
		return new AlertDialog.Builder(this)
				.setTitle(R.string.AccumulationWeightDialog)
				.setView(npView)
				.setPositiveButton(R.string.integration_number_picker_done,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								NumberPicker numberPicker;
								StringBuffer chars = new StringBuffer("00.00");
								int position;
								for(int id :numberPickerIds){
									numberPicker = (NumberPicker)((AlertDialog) dialog).findViewById(id);
									if(numberPicker != null) {
										position = Integer.parseInt((String) numberPicker.getTag());
										chars.setCharAt(position, Character.forDigit(numberPicker.getValue(), 10));//charAt(Integer.parseInt((String) numberPicker.getTag())));
										if(chars.length()> 5)
											chars.delete(5,chars.length());
									}
								}
								updateThresholdSpotAccumulationWeight(Double.parseDouble(String.valueOf(chars)), null);
							}
						})
				.setNegativeButton(R.string.integration_number_picker_cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
							}
						})
				.create();
	}
	/*
	private void setNumberPicker(Spinner digit, int value) {
		//Spinner digit = (Spinner) findViewById(id);
		if (digit != null) {
			if (digit.getAdapter() == null) {
				int selected = 0;
				ArrayList<String> itemNames = new ArrayList<String>();
				for (int i = 0; i < 10; i++) {
					itemNames.add(String.valueOf(i));
				}
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, itemNames);
				digit.setAdapter(adapter);
			}
			digit.setSelection(value);
		}
	}
	*/
	private void setConditionalControlVisibility(int[] id, boolean visible, Handler.Callback initMethod) {
		View container = findViewById(id[0]);
		if (container == null && visible) {
			LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			container = inflater.inflate(id[1], (ViewGroup) findViewById(R.id.main_layout));
			if (initMethod != null) {
				Message initMessage = new Message();
				initMessage.obj = container;
				initMethod.handleMessage(initMessage);
			}
		}
		if (container != null)
			container.setVisibility(visible ? View.VISIBLE : View.GONE);
	}

	public void onCameraViewStarted(int width, int height) {
		if(!mCurrentResolution.isEmpty() && !resolutionToString(mOpenCvCameraView.getResolution()).equals(mCurrentResolution)) {
			mOpenCvCameraView.setResolution(mCurrentResolution);
			Camera.Size newSize = mOpenCvCameraView.getResolution();
			width = newSize.width;
			height = newSize.height;
		}
		mIntermediateMat = new Mat();
		mRgbaF = new Mat(height, width, CvType.CV_8UC4);
		mRgbaT = new Mat(width, width, CvType.CV_8UC4);

		mSize0 = new Size();
		mChannels = new MatOfInt[]{new MatOfInt(0), new MatOfInt(1), new MatOfInt(2)};
		mBuff = new float[mHistSizeNum];
		mHistSize = new MatOfInt(mHistSizeNum);
		mRanges = new MatOfFloat(0f, 256f);
		mMat0 = new Mat();
		mColorsRGB = new Scalar[]{new Scalar(200, 0, 0, 255), new Scalar(0, 200, 0, 255), new Scalar(0, 0, 200, 255)};
		mColorsHue = new Scalar[]{
				new Scalar(255, 0, 0, 255), new Scalar(255, 60, 0, 255), new Scalar(255, 120, 0, 255), new Scalar(255, 180, 0, 255), new Scalar(255, 240, 0, 255),
				new Scalar(215, 213, 0, 255), new Scalar(150, 255, 0, 255), new Scalar(85, 255, 0, 255), new Scalar(20, 255, 0, 255), new Scalar(0, 255, 30, 255),
				new Scalar(0, 255, 85, 255), new Scalar(0, 255, 150, 255), new Scalar(0, 255, 215, 255), new Scalar(0, 234, 255, 255), new Scalar(0, 170, 255, 255),
				new Scalar(0, 120, 255, 255), new Scalar(0, 60, 255, 255), new Scalar(0, 0, 255, 255), new Scalar(64, 0, 255, 255), new Scalar(120, 0, 255, 255),
				new Scalar(180, 0, 255, 255), new Scalar(255, 0, 255, 255), new Scalar(255, 0, 215, 255), new Scalar(255, 0, 85, 255), new Scalar(255, 0, 0, 255)
		};
		mWhilte = Scalar.all(255);
		mP1 = new Point();
		mP2 = new Point();

		// Fill sepia kernel
		mSepiaKernel = new Mat(4, 4, CvType.CV_32F);
		mSepiaKernel.put(0, 0, /* R */0.189f, 0.769f, 0.393f, 0f);
		mSepiaKernel.put(1, 0, /* G */0.168f, 0.686f, 0.349f, 0f);
		mSepiaKernel.put(2, 0, /* B */0.131f, 0.534f, 0.272f, 0f);
		mSepiaKernel.put(3, 0, /* A */0.000f, 0.000f, 0.000f, 1f);
			/*android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
			android.hardware.Camera.getCameraInfo(0/*cameraId* /, info);
			int rotation = getWindowManager().getDefaultDisplay().getRotation();
			int degrees = 0;
			switch (rotation) {
				case Surface.ROTATION_0: degrees = 0; break;
				case Surface.ROTATION_90: degrees = 90; break;
				case Surface.ROTATION_180: degrees = 180; break;
				case Surface.ROTATION_270: degrees = 270; break;
			}
			CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
			manager.getCameraCharacteristics(manager.getCameraIdList()[0]);
			int result;
			if (info.facing == android.hardware.camera2.Camera.CameraInfo.CAMERA_FACING_FRONT) {
				result = (info.orientation + degrees) % 360;
				result = (360 - result) % 360;  // compensate the mirror
			} else {  // back-facing
				result = (info.orientation - degrees + 360) % 360;
			}
			camera.setDisplayOrientation(result);*/
	}

	public void onCameraViewStopped() {
		// Explicitly deallocate Mats
		mIntermediateMat = releaseMat(mIntermediateMat);
		mRgbaF = releaseMat(mRgbaF);
		mRgbaT= releaseMat(mRgbaT);
		mLastThresholdWindowContent= releaseMat(mLastThresholdWindowContent);
		resetLastThresholdWindowContent();
		emptyThresholdFramesIntegrator();
	}
	private Mat releaseMat(Mat mat){
		if (mat != null) {
			mat.release();
		}
		return null;
	}
	private void resetLastThresholdWindowContent() {
		synchronized (luckySpotsActivity.class) {
			mLastThresholdWindowContent = releaseMat(mLastThresholdWindowContent);
		}
	}

	private void resetThresholdWindowMonitors() {
		mThresholdFramesIntegrator = new ArrayList<>(mThresholdIntegrationDeep);
		resetLastThresholdWindowContent();
		//mThresholdIntegrationCount = mThresholdIntegrationMax;
	}
	private void emptyThresholdFramesIntegrator() {
		if (mThresholdFramesIntegrator != null) {
			for (int i = mThresholdFramesIntegrator.size() - 1; i >= 0; i--) {
				mThresholdFramesIntegrator.get(i).release();
				mThresholdFramesIntegrator.remove(i);
			}
		}
	}

	private int lastRGBAWindowHeight = -1;
	private int lastRGBAWindowWidth = -1;

	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		Mat rgba = inputFrame.rgba();
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			// Rotate mRgba 90 degrees
			Core.transpose(rgba, mRgbaT);
			Size origSize = mRgbaT.size();
			Imgproc.resize(mRgbaT, mRgbaF, mRgbaF.size(), 0, 0, 0/*Imgproc.INTER_LINEAR*/);
			Core.flip(mRgbaF, rgba, 1);
		}
		Size sizeRgba = rgba.size();
		lastRGBAWindowHeight = (int) sizeRgba.height;
		lastRGBAWindowWidth = (int) sizeRgba.width;
		Mat rgbaInnerWindow;
		if(mInFrameZoomValue < 100){
			int zoomWidth = (int) (lastRGBAWindowWidth * (mInFrameZoomValue /100.0));
			int zoomHeight= (int) (lastRGBAWindowHeight * (mInFrameZoomValue /100.0));
			int zoomTop = (lastRGBAWindowHeight - zoomHeight)/2;
			int zoomLeft = (lastRGBAWindowWidth - zoomWidth)/2;
			rgbaInnerWindow = new Mat(sizeRgba, rgba.type());
			rgba.submat(zoomTop, zoomTop+zoomHeight, zoomLeft, zoomLeft+zoomWidth).copyTo(rgbaInnerWindow);
			Imgproc.resize(rgbaInnerWindow, rgba, sizeRgba);
			rgbaInnerWindow.release();
		}

		int left = lastRGBAWindowWidth / 8;
		int top = lastRGBAWindowHeight / 8;

		int width = lastRGBAWindowWidth * 3 / 4;
		int height = lastRGBAWindowHeight * 3 / 4;
		switch (luckySpotsActivity.viewMode) {
		/*case luckySpotsActivity.VIEW_MODE_RGBA:
            break;*/

			case luckySpotsActivity.VIEW_MODE_HIST:
				drawHistogram(rgba);
				break;

			case luckySpotsActivity.VIEW_MODE_CANNY:
				rgbaInnerWindow = rgba.submat(top, top + height, left, left + width);
				Imgproc.Canny(rgbaInnerWindow, mIntermediateMat, 80, 90);
				Imgproc.cvtColor(mIntermediateMat, rgbaInnerWindow, Imgproc.COLOR_GRAY2BGRA, 4);
				rgbaInnerWindow.release();
				break;

			case luckySpotsActivity.VIEW_MODE_SOBEL:
				Mat gray = inputFrame.gray();
				Mat grayInnerWindow = gray.submat(top, top + height, left, left + width);
				rgbaInnerWindow = rgba.submat(top, top + height, left, left + width);
				Imgproc.Sobel(grayInnerWindow, mIntermediateMat, CvType.CV_8U, 1, 1);
				Core.convertScaleAbs(mIntermediateMat, mIntermediateMat, 10, 0);
				Imgproc.cvtColor(mIntermediateMat, rgbaInnerWindow, Imgproc.COLOR_GRAY2BGRA, 4);
				grayInnerWindow.release();
				rgbaInnerWindow.release();
				break;

			case luckySpotsActivity.VIEW_MODE_SEPIA:
				rgbaInnerWindow = rgba.submat(top, top + height, left, left + width);
				Core.transform(rgbaInnerWindow, rgbaInnerWindow, mSepiaKernel);
				rgbaInnerWindow.release();
				break;

			case luckySpotsActivity.VIEW_MODE_ZOOM:
				Mat zoomCorner = rgba.submat(0, lastRGBAWindowHeight / 2 - lastRGBAWindowHeight / 10, 0, lastRGBAWindowWidth / 2 - lastRGBAWindowWidth / 10);
				Mat mZoomWindow = rgba.submat(lastRGBAWindowHeight / 2 - 9 * lastRGBAWindowHeight / 100, lastRGBAWindowHeight / 2 + 9 * lastRGBAWindowHeight / 100, lastRGBAWindowWidth / 2 - 9 * lastRGBAWindowWidth / 100, lastRGBAWindowWidth / 2 + 9 * lastRGBAWindowWidth / 100);
				Imgproc.resize(mZoomWindow, zoomCorner, zoomCorner.size());
				Size wsize = mZoomWindow.size();
				Imgproc.rectangle(mZoomWindow, new Point(1, 1), new Point(wsize.width - 2, wsize.height - 2), new Scalar(255, 0, 0, 255), 2);
				zoomCorner.release();
				mZoomWindow.release();
				break;

			case luckySpotsActivity.VIEW_MODE_PIXELIZE:
				rgbaInnerWindow = rgba.submat(top, top + height, left, left + width);
				Imgproc.resize(rgbaInnerWindow, mIntermediateMat, mSize0, 0.1, 0.1, Imgproc.INTER_NEAREST);
				Imgproc.resize(mIntermediateMat, rgbaInnerWindow, rgbaInnerWindow.size(), 0., 0., Imgproc.INTER_NEAREST);
				rgbaInnerWindow.release();
				break;

			case luckySpotsActivity.VIEW_MODE_POSTERIZE:
            /*
            Imgproc.cvtColor(rgbaInnerWindow, mIntermediateMat, Imgproc.COLOR_RGBA2RGB);
            Imgproc.pyrMeanShiftFiltering(mIntermediateMat, mIntermediateMat, 5, 50);
            Imgproc.cvtColor(mIntermediateMat, rgbaInnerWindow, Imgproc.COLOR_RGB2RGBA);
            */
				rgbaInnerWindow = rgba.submat(top, top + height, left, left + width);
				Imgproc.Canny(rgbaInnerWindow, mIntermediateMat, 80, 90);
				rgbaInnerWindow.setTo(new Scalar(0, 0, 0, 255), mIntermediateMat);
				Core.convertScaleAbs(rgbaInnerWindow, mIntermediateMat, 1. / 16, 0);
				Core.convertScaleAbs(mIntermediateMat, rgbaInnerWindow, 16, 0);
				rgbaInnerWindow.release();
				break;
			case luckySpotsActivity.VIEW_MODE_THRESHOLD_ZERO:
				int[] thresholdChannelStruct = getThresholdCheckStructure();
				Rect rectShowing = getThresholdWindowRectangle(false/*showing*/);
				Rect rectDetect = getThresholdWindowRectangle(true/*detection*/);
				rgbaInnerWindow = rgba.submat(rectShowing.y/*top*/, rectShowing.y + rectShowing.height/*bottom*/, rectShowing.x/*left*/, rectShowing.x + rectShowing.width/*right*/);
				Mat detectionWindow = rgba.submat(rectDetect.y/*top*/, rectDetect.y + rectDetect.height/*bottom*/, rectDetect.x/*left*/, rectDetect.x + rectDetect.width/*right*/);
				if (!mResizingThresholdWindowInProgress && !mResizingShowingWindowInProgress) {
					ArrayList<Mat> singleChannels = new ArrayList(rgbaInnerWindow.channels());
					Core.split(detectionWindow, singleChannels);
					Core.MinMaxLocResult result = Core.minMaxLoc(singleChannels.get(thresholdChannelStruct[THRESHOLD_STRUCT_INDEX]));
					if (thresholdChannelStruct[THRESHOLD_STRUCT_VALUE] == -1) {
						handleSavedPreferences(true, PreferenceManager.getDefaultSharedPreferences(getApplicationContext())/*this.getPreferences(Context.MODE_PRIVATE)*/);
						initSeekBar(findViewById(thresholdChannelStruct[THRESHOLD_STRUCT_ID]), thresholdChannelStruct[THRESHOLD_STRUCT_VALUE], thresholdChannelStruct[THRESHOLD_STRUCT_INDICATOR_ID]);
					}
					if (result.maxVal < thresholdChannelStruct[THRESHOLD_STRUCT_VALUE]) {
						if (mLastThresholdWindowContent == null)
							mLastThresholdWindowContent = rgbaInnerWindow.clone();
						else
							mLastThresholdWindowContent.copyTo(rgbaInnerWindow);
					} else {
						if (mThresholdIntegrationDeepTemp != -1) {
							if (mThresholdIntegrationDeepTemp != mThresholdIntegrationDeep) {
								emptyThresholdFramesIntegrator();
								mThresholdFramesIntegrator = new ArrayList<>(mThresholdIntegrationDeep = mThresholdIntegrationDeepTemp);
							}
							mThresholdIntegrationDeepTemp = -1;
						}
						if (mThresholdFramesIntegrator.size() >= mThresholdIntegrationDeep)
							mThresholdFramesIntegrator.remove(0).release();
						mThresholdFramesIntegrator.add(rgbaInnerWindow.clone());
						Iterator<Mat> iterator = mThresholdFramesIntegrator.iterator();
						if (iterator.hasNext()) {
							mLastThresholdWindowContent = rgbaInnerWindow.clone();
							try {
								int nominator = mThresholdFramesIntegrator.size() + 1;
								int count = 0;
								double weight;
								Mat frame;
								while (iterator.hasNext()) {
									frame = iterator.next();
									weight = Math.log10(10.0 * (1.0 - ((float) ++count) / (float) (nominator)));
									if (weight <= 0.0)
										weight = 0.01;
									else if (weight >= 1.0)
										weight = 0.99;
									Core.addWeighted(frame, 1 - weight, mLastThresholdWindowContent, weight, 0, mLastThresholdWindowContent);
									//Imgproc.accumulateWeighted(frame, mLastThresholdWindowContent, weight);
								}
								Core.convertScaleAbs(mLastThresholdWindowContent, rgbaInnerWindow);
							} catch (Exception e) {
								resetThresholdWindowMonitors();
							}
						}
					}
					for (int i = 0; singleChannels.size() > i; i++) {
						singleChannels.get(i).release();
						//maskedChannels.get(i).release();
					}
				}
				drowRectangle(rgbaInnerWindow, new Scalar(255, 216, 0, 255));
				drowRectangle(detectionWindow, new Scalar(
						thresholdChannelStruct[THRESHOLD_STRUCT_RED],
						thresholdChannelStruct[THRESHOLD_STRUCT_GREEN],
						thresholdChannelStruct[THRESHOLD_STRUCT_BLUE], 255));
				//release
				detectionWindow.release();
				rgbaInnerWindow.release();
				//mask.release();
				break;
			case luckySpotsActivity.VIEW_MODE_THRESHOLD_SPOT:
				int[] channelStruct = getThresholdCheckStructure();
				Rect rect = getThresholdWindowRectangle(false/*showing*/);
				rgbaInnerWindow = rgba.submat(rect.y/*top*/, rect.y + rect.height/*bottom*/, rect.x/*left*/, rect.x + rect.width/*right*/);
				if (!mResizingThresholdWindowInProgress && !mResizingShowingWindowInProgress) {
					ArrayList<Mat> singleChannels = new ArrayList(rgbaInnerWindow.channels());
					Core.split(rgbaInnerWindow, singleChannels);
					//Core.MinMaxLocResult result=Core.minMaxLoc(singleChannels.get(channelStruct[THRESHOLD_STRUCT_INDEX]));
					if (channelStruct[THRESHOLD_STRUCT_VALUE] == -1) {
						handleSavedPreferences(true, PreferenceManager.getDefaultSharedPreferences(getApplicationContext())/*this.getPreferences(Context.MODE_PRIVATE)*/);
						initSeekBar(findViewById(channelStruct[THRESHOLD_STRUCT_ID]), channelStruct[THRESHOLD_STRUCT_VALUE], channelStruct[THRESHOLD_STRUCT_INDICATOR_ID]);

					}
					Mat mask = new Mat(rect.height, rect.width, CvType.CV_8U);
					int thresholdChannelIndex = channelStruct[THRESHOLD_STRUCT_INDEX];
					Imgproc.threshold(singleChannels.get(thresholdChannelIndex), mask, channelStruct[THRESHOLD_STRUCT_VALUE], 255, Imgproc.THRESH_TOZERO);
					Scalar mean;//to use when mMaskBackgroundValue > 0
					double[] meanValue;
					Mat dst = new Mat(mask.rows(),mask.cols(),mask.type());
					ArrayList<Mat> maskedChannels = new ArrayList<>(rgbaInnerWindow.channels());
					for (int i = 0; singleChannels.size() > i; i++) {
						if (mCheckBoxMaskDetectionChannel && i == thresholdChannelIndex) {
							maskedChannels.add(i, new Mat(rect.height, rect.width, CvType.CV_8U, new Scalar(0, 0, 0, 0)));
						} else {
							maskedChannels.add(i, new Mat(rect.height, rect.width, CvType.CV_8U));
							if(mMaskBackgroundValue > 0) {//mask for the channels made, now check is background average level shall be subtracted
								meanValue = (mean = Core.mean(singleChannels.get(i))).val;
								meanValue[0] *= (mMaskBackgroundValue / 100.0);
								mean.set(meanValue);
								Core.subtract(singleChannels.get(i), mean, dst);
							}
							else
								dst = singleChannels.get(i);
							dst.copyTo(maskedChannels.get(i), mask);
						}
					}
					Core.merge(maskedChannels, rgbaInnerWindow);
					//Imgproc.accumulateWeighted(rgbaInnerWindow, mLastThresholdWindowContent, weight);
					synchronized (luckySpotsActivity.class) {
						if (mLastThresholdWindowContent != null) {
							double weightMain =  mThresholdSpotAccumulationWeight / 100.0;
							double weightInt = 1.0 - weightMain;
							/*if (mThresholdViewModeAbsolute) {
								weightInt = weightMain;
								weightMain = 1.0;
							}*/
							Core.addWeighted(rgbaInnerWindow, weightMain, mLastThresholdWindowContent, weightInt, 0, mLastThresholdWindowContent);
							Core.convertScaleAbs(mLastThresholdWindowContent, rgbaInnerWindow);
						}
						if(mLastThresholdWindowContent != null)
							mLastThresholdWindowContent.release();
						mLastThresholdWindowContent = rgbaInnerWindow.clone();
					}
					//release
					mask.release();
					for (int i = 0; singleChannels.size() > i; i++) {
						singleChannels.get(i).release();
						maskedChannels.get(i).release();
					}
				}
				drowRectangle(rgbaInnerWindow, new Scalar(
						channelStruct[THRESHOLD_STRUCT_RED],
						channelStruct[THRESHOLD_STRUCT_GREEN],
						channelStruct[THRESHOLD_STRUCT_BLUE], 255));
				//release
				rgbaInnerWindow.release();
			break;
		}
		if(mCheckBoxHistogram)
			drawHistogram(rgba);
		if (mVideoRecordingOngoing) {
			if (mStopVideoRecording){
				mStopVideoRecording = false;
				stopVideoRecording();
			}else if(!mVideoRecordingPaused)
				recordVideoFrame(rgba);
		}
		if(mMakeSnapshot){
			mMakeSnapshot = false;
			//Mat tmp = rgba.clone();
			Bitmap bmp = convertMatToBitmap(rgba);
			if (bmp != null){
				String modifier = null;
				if( viewMode == VIEW_MODE_THRESHOLD_SPOT )
					modifier = "spot"/*+(mThresholdViewModeAbsolute ? "abs" :"norm")*/;
				else if(viewMode==VIEW_MODE_THRESHOLD_ZERO)
					modifier = "area";
				if(saveBitmapAsPicture(bmp, modifier))
					playSound(MediaActionSound.SHUTTER_CLICK);
				bmp.recycle();
			}
			//tmp.release();
		}
		return rgba;
	}
	private void drawHistogram(Mat rgba){
		Size sizeRgba = rgba.size();
		Mat hist = new Mat();
		int thikness = (int) (sizeRgba.width / (mHistSizeNum + 10) / 5);
		if (thikness > 5) thikness = 5;
		int offset = (int) ((sizeRgba.width - (5 * mHistSizeNum + 4 * 10) * thikness) / 2);
		// RGB
		for (int c = 0; c < 3; c++) {
			Imgproc.calcHist(Arrays.asList(rgba), mChannels[c], mMat0, hist, mHistSize, mRanges);
			Core.normalize(hist, hist, sizeRgba.height / 2, 0, Core.NORM_INF);
			hist.get(0, 0, mBuff);
			for (int h = 0; h < mHistSizeNum; h++) {
				mP1.x = mP2.x = offset + (c * (mHistSizeNum + 10) + h) * thikness;
				mP1.y = sizeRgba.height - 1;
				mP2.y = mP1.y - 2 - (int) mBuff[h];
				Imgproc.line(rgba, mP1, mP2, mColorsRGB[c], thikness);
			}
		}
		// Value and Hue
		Imgproc.cvtColor(rgba, mIntermediateMat, Imgproc.COLOR_RGB2HSV_FULL);
		// Value
		Imgproc.calcHist(Arrays.asList(mIntermediateMat), mChannels[2], mMat0, hist, mHistSize, mRanges);
		Core.normalize(hist, hist, sizeRgba.height / 2, 0, Core.NORM_INF);
		hist.get(0, 0, mBuff);
		for (int h = 0; h < mHistSizeNum; h++) {
			mP1.x = mP2.x = offset + (3 * (mHistSizeNum + 10) + h) * thikness;
			mP1.y = sizeRgba.height - 1;
			mP2.y = mP1.y - 2 - (int) mBuff[h];
			Imgproc.line(rgba, mP1, mP2, mWhilte, thikness);
		}
		// Hue
		Imgproc.calcHist(Arrays.asList(mIntermediateMat), mChannels[0], mMat0, hist, mHistSize, mRanges);
		Core.normalize(hist, hist, sizeRgba.height / 2, 0, Core.NORM_INF);
		hist.get(0, 0, mBuff);
		for (int h = 0; h < mHistSizeNum; h++) {
			mP1.x = mP2.x = offset + (4 * (mHistSizeNum + 10) + h) * thikness;
			mP1.y = sizeRgba.height - 1;
			mP2.y = mP1.y - 2 - (int) mBuff[h];
			Imgproc.line(rgba, mP1, mP2, mColorsHue[h], thikness);
		}
	}

	private void drowRectangle(Mat mat, Scalar color) {
		Size wSize = mat.size();
		Imgproc.rectangle(mat, new Point(1, 1), new Point(wSize.width - 2, wSize.height - 2), color, 2);
	}

	private Rect getThresholdWindowRectangle(Boolean detection) {
		return getThresholdWindowRectangle(detection, false);
	}

	private Rect getThresholdWindowRectangle(Boolean detection, boolean absolutePositioning) {
		int percentage = detection ? mThresholdDetectionPercentageSize : mThresholdShowingPercentageSize;
		int width = absolutePositioning ? mOpenCvCameraView.getWidth() : lastRGBAWindowWidth;
		int height = absolutePositioning ? mOpenCvCameraView.getHeight() : lastRGBAWindowHeight;
		int thresholdWindowHeight = (int) ((double) percentage / 100.0 * height); //max is less then half of lastRGBAWindowHeight/lastRGBAWindowWidth
		int thresholdWindowWidth = (int) ((double) percentage / 100.0 * width);
		return new Rect((width - thresholdWindowWidth) / 2, (height - thresholdWindowHeight) / 2, thresholdWindowWidth, thresholdWindowHeight);
	}
	private boolean isModeThresholdRelated(){
		switch(viewMode){
			case VIEW_MODE_THRESHOLD_SPOT:
			case VIEW_MODE_THRESHOLD_ZERO:
				return true;
		}
		return false;
	}

	/**
	 * Called when a touch event is dispatched to a view. This allows listeners to
	 * get a chance to respond before the target view.
	 *
	 * @param v     The view the touch event has been dispatched to.
	 * @param event The MotionEvent object containing full information about
	 *              the event.
	 * @return True if the listener has consumed the event, false otherwise.
	 */
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (isModeThresholdRelated()) {
			float x = event.getX();
			float y = event.getY();
			int action = event.getAction();
			switch (action) {
				case MotionEvent.ACTION_DOWN:
					if (!mResizingThresholdWindowInProgress && !mResizingShowingWindowInProgress) {
						if (testThresholdWindowRectangles(x, y))
							return true;
						else {
							int cameraViewHeight = mOpenCvCameraView.getHeight();
							int seekBarsHeight = findViewById(R.id.seekbars_frame).getHeight();
							if(cameraViewHeight - seekBarsHeight > y)
								engageOnScreenControls();
						}
					}
					break;
				case MotionEvent.ACTION_MOVE:
					if (!mResizingThresholdWindowInProgress && !mResizingShowingWindowInProgress)
						testThresholdWindowRectangles(x, y);
				case MotionEvent.ACTION_UP:
					if (mResizingThresholdWindowInProgress || mResizingShowingWindowInProgress) {
						int oldPercentage = mResizingThresholdWindowInProgress ? mThresholdDetectionPercentageSize : mThresholdShowingPercentageSize;
						Point boundaries = mResizingThresholdWindowInProgress ? THRESHOLD_DETECTION_PERCENTAGE_BOUNDARIES : THRESHOLD_SHOWING_PERCENTAGE_BOUNDARIES;
						int cameraViewWidth = mOpenCvCameraView.getWidth();
						int newPercentage = setBoundaries(100 - Math.round(((cameraViewWidth - x) * 2 / cameraViewWidth) * 100), boundaries);
						if (newPercentage != oldPercentage) {
							if (mResizingThresholdWindowInProgress)
								mThresholdDetectionPercentageSize = newPercentage;
							else
								mThresholdShowingPercentageSize = newPercentage;
							resetThresholdWindowMonitors();

						}
						if (action == MotionEvent.ACTION_UP)
							mResizingThresholdWindowInProgress = mResizingShowingWindowInProgress = false;
						return true;
					}
					break;
				case MotionEvent.ACTION_OUTSIDE:
				case MotionEvent.ACTION_CANCEL:
					mResizingThresholdWindowInProgress = mResizingShowingWindowInProgress = false;
					return true;
			}
		}
		else if(event.getAction() == MotionEvent.ACTION_DOWN)
			engageOnScreenControls();
		return false;
	}

	//Auto-hiding on touch menu
	private final Handler onScreenActivityControlsRunnableHandler = new Handler();// Hide after some seconds
	private final Runnable onScreenActivityControlsRunnable = new Runnable() {
		@Override
		public void run() {
			hideOnScreenControls();
		}
	};

	private void hideOnScreenControls() {
		onScreenActivityControlsRunnableHandler.removeCallbacks(onScreenActivityControlsRunnable);
		setConditionalControlVisibility(onScreenActivityControls, false, null);
	}

	private void engageOnScreenControls() {
		setConditionalControlVisibility(onScreenActivityControls, true, onScreenMenuInit);
		keepOnScreenActivityControls();
	}
	private void keepOnScreenActivityControls(){
		onScreenActivityControlsRunnableHandler.removeCallbacks(onScreenActivityControlsRunnable);
		onScreenActivityControlsRunnableHandler.postDelayed(onScreenActivityControlsRunnable, 4000);
	}
	private final Handler onScreenActivityFramesCountRunnableHandler = new Handler();// Hide after some seconds
	private final Runnable onScreenActivityFramesCountRunnable = new Runnable() {
		@Override
		public void run() {
			TextView framesCounter = (TextView) findViewById(R.id.on_screen_controls_video_frames);
			if(framesCounter != null) {
				framesCounter.setText(String.valueOf(mRecordedVideoFramesCounter));
				updateFramesCount(true);
			}
		}
	};
	private void updateFramesCount(boolean keepUpdating){
		if(keepUpdating)
			onScreenActivityFramesCountRunnableHandler.postDelayed(onScreenActivityFramesCountRunnable, 500);
		else
			onScreenActivityFramesCountRunnableHandler.removeCallbacks(onScreenActivityFramesCountRunnable);
	}

	private Boolean testThresholdWindowRectangles(float x, float y) {
		Boolean ret = false;
		if(isModeThresholdRelated()) {
			Rect rect;
			if (viewMode==VIEW_MODE_THRESHOLD_ZERO) {
				rect = getThresholdWindowRectangle(true/*detection*/, true/*absolute*/);
				if (Math.abs(rect.x + rect.width - x) <= 50) {
					ret = mResizingThresholdWindowInProgress = true;
					mResizingShowingWindowInProgress = false;
				}
			}
			if (!ret) {
				rect = getThresholdWindowRectangle(false/*showing*/, true/*absolute*/);
				if (Math.abs(rect.x + rect.width - x) < 10) {
					ret = mResizingShowingWindowInProgress = true;
					mResizingThresholdWindowInProgress = false;
				}
			}
		}
		return ret;
	}

	private final Handler.Callback onScreenMenuInit = new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			Object obj = msg.obj;
			if (obj != null) {
				initOnScreenMenuControl(R.id.on_screen_controls_menu_button);
				initOnScreenMenuControl(R.id.on_screen_controls_photo_button);
				initOnScreenMenuControl(R.id.on_screen_controls_video_record);
				initOnScreenMenuControl(R.id.on_screen_controls_video_pause);
				initOnScreenMenuControl(R.id.on_screen_controls_video_record_after_pause);
				initOnScreenMenuControl(R.id.on_screen_controls_video_stop);
				initOnScreenMenuControl(R.id.on_screen_controls_image_view);
				initOnScreenMenuRadioControl(R.id.threshold_mode_radio_group);
				SeekBar zoomBar = initSeekBar(findViewById(R.id.seekBar_zoom), 100 - mInFrameZoomValue, true/*No Color change*/);
				RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) zoomBar.getLayoutParams();
				params.width = (int)(mOpenCvCameraView.getHeight() *0.75);
				zoomBar.setLayoutParams(params);
				initGeneralCheckBox(R.id.checkBoxHistogram, mCheckBoxHistogram, generalOnCheckedChangeListener);
				initGeneralCheckBox(R.id.checkBox_mask_selected_color, mCheckBoxMaskDetectionChannel, generalOnCheckedChangeListener);
				updateIndicatorValue(R.id.text_zoom_value, mInFrameZoomValue);
				setTextValueForCurrentResolution();
			}
			return false;
		}
	};
	private void initGeneralCheckBox(int id, boolean initValue, CheckBox.OnCheckedChangeListener listener) {
		CheckBox checkBox = (CheckBox) findViewById(id);
		if (checkBox != null) {
			checkBox.setChecked(initValue);
			checkBox.setOnCheckedChangeListener(listener);
		}
	}
	private final CheckBox.OnCheckedChangeListener generalOnCheckedChangeListener = new CheckBox.OnCheckedChangeListener() {
		/** Called when the checked state of a compound button has changed.
		 * @param buttonView The compound button view whose state has changed.
		 * @param isChecked  The new checked state of buttonView.
		 */
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			onScreenMenuControlClick(buttonView, isChecked);
		}
	};
	private final View.OnClickListener onScreenButtonOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			onScreenMenuControlClick(v, null);
		}
	};
	private View initOnScreenMenuControl(int id) {
		View onScreenMenuButton = findViewById(id);
		if (onScreenMenuButton != null) {
			onScreenMenuButton.setOnClickListener(onScreenButtonOnClickListener);
		}
		return onScreenMenuButton;
	}
	private void setRadioButtonChecked(int id, Boolean checked ){
		setRadioButtonChecked(id, checked, null);
	}
	private void setRadioButtonChecked(int id, Boolean checked, View.OnClickListener onClickListener ){
		RadioButton radio = (RadioButton) findViewById(id);
		if(radio != null) {
			radio.setChecked(checked);
			if (onClickListener != null)
				radio.setOnClickListener(onClickListener);
		}
	}
	private void initOnScreenMenuRadioControl(int id) {
		RadioGroup radioGroup = (RadioGroup)findViewById(id);
		setRadioButtonChecked(R.id.threshold_mode_area, viewMode==VIEW_MODE_THRESHOLD_ZERO, onScreenButtonOnClickListener);
		setRadioButtonChecked(R.id.threshold_mode_normal, viewMode==VIEW_MODE_THRESHOLD_SPOT /*&& !mThresholdViewModeAbsolute*/, onScreenButtonOnClickListener);
		//setRadioButtonChecked(R.id.threshold_mode_absolute, !viewMode==VIEW_MODE_THRESHOLD_ZERO && mThresholdViewModeAbsolute, onScreenButtonOnClickListener);
		setRadioButtonChecked(R.id.view_mode_preview, viewMode == VIEW_MODE_RGBA, onScreenButtonOnClickListener);
		if (radioGroup != null) {
				radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
				/**
				 * <p>Called when the checked radio button has changed. When the
				 * selection is cleared, checkedId is -1.</p>
				 *
				 * @param group     the group in which the checked radio button has changed
				 * @param checkedId the unique identifier of the newly checked radio button
				 */
				@Override
				public void onCheckedChanged(RadioGroup group, int checkedId) {
					onCheckedChangedOrClicked(checkedId);
				}
			});
		}

	}
	private void onCheckedChangedOrClicked(int checkedId){
		switch(checkedId){
			case R.id.threshold_mode_area:
				viewMode = VIEW_MODE_THRESHOLD_ZERO;
				break;
			case R.id.threshold_mode_normal:
			//case R.id.threshold_mode_absolute:
				viewMode = VIEW_MODE_THRESHOLD_SPOT;
				//mThresholdViewModeAbsolute = checkedId == R.id.threshold_mode_absolute;
				break;
			case R.id.view_mode_preview:
				viewMode = VIEW_MODE_RGBA;
				break;
		}
		resetLastThresholdWindowContent();
		thresholdContainersVisibility();
		thresholdIntegrationDeepChanged(null);//Show proper value
	}

	private void initTextControl(int id, String text) {
		TextView control = (TextView) findViewById(id);
		if (control != null) {
			if (id == R.id.threshold_deep_control_value && viewMode == VIEW_MODE_THRESHOLD_SPOT)
				updateThresholdSpotAccumulationWeight(mThresholdSpotAccumulationWeight, control);
			else if (text != null)
				control.setText(text);
			control.setOnClickListener(new TextView.OnClickListener() {
				@Override
				public void onClick(View v) {
					thresholdIntegrationDeepChanged(v);
				}
			});
		}
	}
	private final int[] thresholdActivityColorIds = {R.id.seekbars_frame, R.layout.threshold_window_colors};
	private final int[] onScreenActivityControls = {R.id.on_screen_controls_frame, R.layout.on_screen_activity_controls};
	//private int[] thersholdActivityWindowSizeIds = {R.id.threshold_window_size_frame, R.layout.threshold_window_size};
	private void onScreenMenuControlClick(View control, Boolean isChecked) {//isChecked for the case of checkBox only
		View other;
		switch (control.getId()) {
			case R.id.on_screen_controls_menu_button:
				hideOnScreenControls();
				performOpenOptionsMenu();
				break;
			case R.id.on_screen_controls_photo_button:
				mMakeSnapshot = true;
				playSound(MediaActionSound.FOCUS_COMPLETE);
				hideOnScreenControls();
				break;
			case R.id.on_screen_controls_video_record:
				playSound(MediaActionSound.START_VIDEO_RECORDING);
				startVideoRecorder();
				control.setVisibility(View.GONE);
				other = findViewById(R.id.on_screen_controls_video_stop_group);
				other.setVisibility(View.VISIBLE);
				other = findViewById(R.id.on_screen_controls_video_pause);
				other.setVisibility(View.VISIBLE);
				onScreenActivityControlsRunnableHandler.removeCallbacks(onScreenActivityControlsRunnable);
				updateFramesCount(true);
				break;
			case R.id.on_screen_controls_video_stop:
				playSound(MediaActionSound.STOP_VIDEO_RECORDING);
				control.setVisibility(View.VISIBLE);
				other = findViewById(R.id.on_screen_controls_video_pause);
				other.setVisibility(View.VISIBLE);
				other = findViewById(R.id.on_screen_controls_video_record_after_pause);
				other.setVisibility(View.GONE);
				other = findViewById(R.id.on_screen_controls_video_stop_group);
				other.setVisibility(View.GONE);
				other = findViewById(R.id.on_screen_controls_video_record);
				other.setVisibility(View.VISIBLE);
				hideOnScreenControls();
				updateFramesCount(false);
				mStopVideoRecording = true;
				//stopVideoRecording();
				break;
			case R.id.on_screen_controls_video_pause:
				playSound(MediaActionSound.STOP_VIDEO_RECORDING);
				control.setVisibility(View.GONE);
				other = findViewById(R.id.on_screen_controls_video_record_after_pause);
				other.setVisibility(View.VISIBLE);
				mVideoRecordingPaused = true;
				updateFramesCount(false);
				break;
			case R.id.on_screen_controls_video_record_after_pause:
				playSound(MediaActionSound.START_VIDEO_RECORDING);
				control.setVisibility(View.GONE);
				other = findViewById(R.id.on_screen_controls_video_pause);
				other.setVisibility(View.VISIBLE);
				mVideoRecordingPaused = false;
				updateFramesCount(true);
				break;
			case R.id.on_screen_controls_image_view:
				Intent intentBrowseFiles = new Intent(Intent.ACTION_VIEW);
				intentBrowseFiles.setType("image/* video/*");
				intentBrowseFiles.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
				try{
					startActivity(intentBrowseFiles);
				}catch(RuntimeException e){
					try{
						intentBrowseFiles.setType("image/*");
						startActivity(intentBrowseFiles);
					}catch(RuntimeException e1){
						CondLog('e', e1.getMessage());
					}
					CondLog('e', e.getMessage());
				}
				break;
			//case R.id.threshold_mode_absolute:
			case R.id.threshold_mode_area:
			case R.id.threshold_mode_normal:
				onCheckedChangedOrClicked(control.getId());
				break;
			case R.id.checkBoxHistogram:
				mCheckBoxHistogram = isChecked == null ? !mCheckBoxHistogram : isChecked;
				break;
			case R.id.checkBox_mask_selected_color:
				mCheckBoxMaskDetectionChannel = isChecked == null ? !mCheckBoxMaskDetectionChannel : isChecked;
				break;
		}
	}
	private Bitmap convertMatToBitmap(Mat srcMat) {
		Bitmap bmp; // bmp is your Bitmap instance
		try {
			bmp = Bitmap.createBitmap(srcMat.cols(), srcMat.rows(), Bitmap.Config.ARGB_8888);
			Utils.matToBitmap(srcMat, bmp);
		} catch (CvException e) {
			CondLog('d',  e.getMessage());
			bmp = null;
		}
		//srcMat.release();
		return bmp;
	}
	private final MediaScannerConnection.OnScanCompletedListener scanCompleteListener= new MediaScannerConnection.OnScanCompletedListener() {
		@Override
		public void onScanCompleted(String path, Uri uri) {

		}
	};
	private File getAvailableForStoreDirectory(){
		return mWriteAccessGrunted ? Environment.getExternalStorageDirectory() : Environment.getDataDirectory();
	}

	private boolean saveBitmapAsPicture(Bitmap bmp, String modifier) {
		boolean result = false;
		File dest = prepareNewFile(mDirectoryPathPicture, "img_", modifier, "png");//new File(sd, fileName);
		if(dest != null) {
			FileOutputStream out = null;
			try {
				out = new FileOutputStream(dest);
				bmp.compress(Bitmap.CompressFormat.PNG, 100, out);// PNG is a lossless format, the compression factor (100) is ignored
				MediaScannerConnection.scanFile(this, new String[]{dest.getAbsolutePath()}, null, scanCompleteListener);
				//mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + getAvailableForStoreDirectory())));
				result = true;
			} catch (Exception e) {
				e.printStackTrace();
				CondLog('d', e.getMessage());
			} finally {
				try {
					if (out != null) {
						result = true;
						out.close();
						CondLog('v', "OK!!");
					}
				} catch (IOException e) {
					CondLog('e', e.getMessage() + "Error");
					e.printStackTrace();
				}
			}
		}
		return result;
	}
	private File prepareNewFile(String path, String prefix, String modifier, String ext) {
		File dest = null;
		FileOutputStream out = null;
		File sd = new File( getAvailableForStoreDirectory()+ "/"+path);
		boolean success = true;
		if (!sd.exists())
			success = sd.mkdirs();
		if (success) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
			String currentDateandTime = sdf.format(new Date());
			String fileName = prefix +(modifier!=null ? modifier+"_" : "") +currentDateandTime + "."+ ext;
			dest = new File(sd, fileName);
		}
		return dest;
	}
	private void playSound(int soundId) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			MediaActionSound sound = new MediaActionSound();
			sound.play(soundId);
		}
		/*//alternative
		SoundPool soundPool = new SoundPool(1, AudioManager.STREAM_NOTIFICATION, 0);
		int shutterSound = soundPool.load(this, R.raw.camera_click, 0);
		///...and then to play the sound
		soundPool.play(shutterSound, 1f, 1f, 0, 0, 1);//http://developer.android.com/reference/android/media/SoundPool.html for parameters.
		*/
	}
	//DEFINING A STRING ADAPTER WHICH WILL HANDLE THE DATA OF THE LISTVIEW
	private ArrayAdapter<String> listFieldadApter;
	private void setTextValueForCurrentResolution(){
		Spinner listField = (Spinner) findViewById(R.id.current_resolution);
		if(listField != null) {
			if(mCurrentResolution.isEmpty()){
				mCurrentResolution = resolutionToString(mOpenCvCameraView.getResolution());
			}
			List<Camera.Size> resolutions = mOpenCvCameraView.getResolutionList();
			ArrayList<String> itemNames = new ArrayList<String>();
			String  size;
			int selected = 0;
			for( int i=0; resolutions.size()>i;i++) {
				size = resolutionToString(resolutions.get(i));
				itemNames.add(size);
				if(size.equals(mCurrentResolution))
					selected = i;
			}
			listFieldadApter=new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, itemNames);
			listField.setAdapter(listFieldadApter);
			listField.setSelection(selected);
			listField.setOnItemSelectedListener(spinnerOnItemSelected);
			/*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
				listField.setOnScrollChangeListener(new View.OnScrollChangeListener() {
					@Override
					public void onScrollChange(View view, int i, int i1, int i2, int i3) {
						engageOnScreenControls();
					}
				});
			}*/
			listField.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View view, MotionEvent motionEvent) {
					engageOnScreenControls();
					return false;
				}
			});
		}
	}
	private AdapterView.OnItemSelectedListener spinnerOnItemSelected = new AdapterView.OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
			if(mOpenCvCameraView != null) {
				String selected = (String) adapterView.getItemAtPosition(position);
				if (!selected.equals(mCurrentResolution)) {
					//mOpenCvCameraView.setResolution(mCurrentResolution = selected);
					mOpenCvCameraView.disableView();
					resetLastThresholdWindowContent();
					mOpenCvCameraView.enableView();
				}
			}
			engageOnScreenControls();
		}

		@Override
		public void onNothingSelected(AdapterView<?> adapterView) {

		}
	};

	/**
	 * Called when a view has been clicked and held.
	 *
	 * @param v The view that was clicked and held.
	 * @return true if the callback consumed the long click, false otherwise.
	 */
	@SuppressLint("SimpleDateFormat")
	@Override
	public boolean onLongClick(View v) {/*
		VideoCapture camera = new VideoCapture(JavaCameraView.CAMERA_ID_BACK);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		String currentDateandTime = sdf.format(new Date());
		String fileName = getAvailableForStoreDirectory().getPath() +
				"/LuckySpots/picture_" + currentDateandTime + ".jpg";
		camera.set(org.Highgui.CV_CAP_PROP_FRAME_WIDTH, 960);//takePicture(fileName);
		Toast.makeText(this, fileName + " saved", Toast.LENGTH_SHORT).show();*/
		return false;

	}

	public void closeApplication(View view) {
		finish();
	}

	//////////////Video Recording////////
	//public static class FramesRecording {
    /* The number of seconds in the continuous record loop (or 0 to disable loop). */
	//final int RECORD_LENGTH = 10;
	private int mRecordedVideoFramesCounter = 0;
	/*
	private void startVideoRecorder(){}
	private void stopVideoRecording(){}
	private void recordVideoFrame(Mat rgba){}
	*
	* */
	private volatile FFmpegFrameRecorder recorder;

	private int sampleAudioRateInHz = 44100;
	private int frameRate = 30;

	private Thread audioThread;
	volatile boolean runAudioThread = false;
	private AudioRecord audioRecord;
	private AudioRecordRunnable audioRecordRunnable;

	private String mFFmpegUri;

	private long mStartRecordingTime = 0;
	private int depth = org.bytedeco.javacpp.opencv_core.IPL_DEPTH_8U;


	private int mFrameChannels = 4;
	private static final String VIDEO_RECORDING_FORMAT = "mp4";
	private Frame mFrameForRecording = null;

	private void initVideoRecorder() {
		CondLog('w', "initVideoRecorder");
		mRecordedVideoFramesCounter = 0;
		// Recreated after frame size is set in surface change method
		File videoFile = prepareNewFile(mDirectoryPathVideo, "video_", null, VIDEO_RECORDING_FORMAT);
		if(videoFile != null) {
			if(mFrameForRecording == null || mFrameForRecording.imageWidth != lastRGBAWindowWidth || mFrameForRecording.imageHeight != lastRGBAWindowHeight)
				mFrameForRecording = new Frame(lastRGBAWindowWidth, lastRGBAWindowHeight, depth, mFrameChannels);
			mFFmpegUri = videoFile.getAbsolutePath();
			recorder = new FFmpegFrameRecorder(mFFmpegUri, lastRGBAWindowWidth, lastRGBAWindowHeight, 1);
			recorder.setFormat(VIDEO_RECORDING_FORMAT);//recorder.setFormat("flv");
			//recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
			//recorder.setVideoCodecName("libx264");
			recorder.setSampleRate(sampleAudioRateInHz);
			// re-set in the surface changed method as well
			recorder.setFrameRate(frameRate);
			// Create audio recording thread
			audioRecordRunnable = new AudioRecordRunnable();
			audioThread = new Thread(audioRecordRunnable);
			runAudioThread = true;
		}
	}
	public void startVideoRecorder(){
		mVideoRecordingPaused = false;
		if(mVideoRecordingOngoing)
			stopVideoRecording();

		initVideoRecorder();
		try {
			recorder.start();
			mStartRecordingTime = System.currentTimeMillis();
			mVideoRecordingOngoing = true;
			audioThread.start();
		} catch (FFmpegFrameRecorder.Exception e) {
			e.printStackTrace();
		}
	}
	public void stopVideoRecording() {
		// This should stop the audio thread from running
		mVideoRecordingPaused = true;
		mStopVideoRecording = true;
		mVideoRecordingOngoing = false;
		runAudioThread = false;
		if (recorder != null) {
			CondLog('v', "Finishing recording, calling stop and release on recorder");
			try {
				recorder.stop();
				recorder.release();
				MediaScannerConnection.scanFile(getApplicationContext(), new String[]{mFFmpegUri}, null, scanCompleteListener);
			} catch (FFmpegFrameRecorder.Exception e) {
				e.printStackTrace();
			}
			recorder = null;
		}
		mVideoRecordingPaused = false;
	}

	private void recordVideoFrame(Mat rgba){
		if (mVideoRecordingOngoing && !mVideoRecordingPaused) {
			long videoTimestamp = 1000 * (System.currentTimeMillis() - mStartRecordingTime);
			byte[] byteFrame = new byte[(int) (rgba.total() * (mFrameChannels=rgba.channels()))];
			rgba.get(0, 0, byteFrame);
			ByteBuffer frameBufferPointer = (ByteBuffer)mFrameForRecording.image[0].position(0);
			frameBufferPointer.put(byteFrame);//videoImage.getByteBuffer().put(data);
			try {
				recorder.setTimestamp(videoTimestamp);// Get the correct time
				recorder.record(mFrameForRecording);// Record the image into FFmpegFrameRecorder
				mRecordedVideoFramesCounter++;
				CondLog('i', "Wrote Frame: " + mRecordedVideoFramesCounter);
			}catch (FFmpegFrameRecorder.Exception e) {
				CondLog('e',e.getMessage());
				e.printStackTrace();
			}catch(RuntimeException er){
				CondLog('e',er.getMessage());
				er.printStackTrace();
			}
		}

	}
	class AudioRecordRunnable implements Runnable {
		@Override
		public void run() {
			// Set the thread priority
			android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
			// Audio
			int bufferReadResult;

			int bufferSize = AudioRecord.getMinBufferSize(sampleAudioRateInHz,
					AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
			audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleAudioRateInHz,
					AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);

			ShortBuffer audioData = ShortBuffer.allocate(bufferSize);

			CondLog('d',  "audioRecord.startRecording()");
			audioRecord.startRecording();

			//ffmpeg_audio encoding loop
			while (runAudioThread) {
				//Log.v(LOG_TAG,"recording? " + recording);
				bufferReadResult = audioRecord.read(audioData.array(), 0, audioData.capacity());
				audioData.limit(bufferReadResult);
				if (bufferReadResult > 0) {
					CondLog('v', "bufferReadResult: " + bufferReadResult);
					if (mVideoRecordingOngoing && !mVideoRecordingPaused) {
						try {// Until "recording" is true when this thread starts, it will not record
							recorder.recordSamples(audioData);
							//Log.v(LOG_TAG,"recording " + 1024*i + " to " + 1024*i+1024);
						} catch (FFmpegFrameRecorder.Exception e) {
							CondLog('e', e.getMessage());
							e.printStackTrace();
						}
					}
				}
			}
			CondLog('v', "AudioThread Finished");

			//encoding finish, release recorder
			if (audioRecord != null) {
				audioRecord.stop();
				audioRecord.release();
				audioRecord = null;
				CondLog('v', "audioRecord released");
			}
		}
	}
	/*
	public void changeResolution(View v){
		List<Camera.Size> cameraResolutionList = cameraView.getResolutionList();
		resolutionIndex++;
		if(resolutionIndex >= cameraResolutionList.size()){
			resolutionIndex = 0;
		}

		Camera.Size resolution = cameraResolutionList.get(resolutionIndex);
		cameraView.setResolution(resolution.width, resolution.height);
		resolution = cameraView.getResolution();
		String caption = Integer.valueOf(resolution.width).toString() + "x" + Integer.valueOf(resolution.height).toString();
		Toast.makeText(this, caption, Toast.LENGTH_SHORT).show();

		imageWidth = resolution.width;
		imageHeight = resolution.height;

		frameRate = cameraView.getFrameRate();

		initVideoRecorder();
	}
	*/
}
//}
