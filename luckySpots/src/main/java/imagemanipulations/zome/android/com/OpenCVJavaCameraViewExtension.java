package imagemanipulations.zome.android.com;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.AttributeSet;

import org.opencv.android.JavaCameraView;

import java.util.ArrayList;
import java.util.List;
/**
 * Created by Sasha on 6/22/2016.
 */

public class OpenCVJavaCameraViewExtension extends JavaCameraView{
	public OpenCVJavaCameraViewExtension(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public List<String> getEffectList() {
		return mCamera.getParameters().getSupportedColorEffects();
	}

	public boolean isEffectSupported() {
		return (mCamera.getParameters().getColorEffect() != null);
	}

	public String getEffect() {
		return mCamera.getParameters().getColorEffect();
	}

	public void setEffect(String effect) {
		Camera.Parameters params = mCamera.getParameters();
		params.setColorEffect(effect);
		mCamera.setParameters(params);
	}

	public List<Size> getResolutionList() {
		return mCamera.getParameters().getSupportedPreviewSizes();
	}
	public ArrayList<Integer> convertResolutionToIntArray(String resolution){
		String[] strResolution = resolution.split("x");
		ArrayList<Integer> intResolution = new ArrayList<Integer>(2)				;
		if(strResolution.length ==2) {
			intResolution.add(Integer.parseInt(strResolution[0]));
			intResolution.add(Integer.parseInt(strResolution[1]));
		}
		return intResolution;

	}
	public void setResolution(String resolution) {
		ArrayList<Integer> intResolution = convertResolutionToIntArray(resolution);
		if(intResolution.size() == 2) {
			setResolution(intResolution.get(0), intResolution.get(1));
		}else
			throw new RuntimeException("Expected resolution format is WxH while supplied:"+ resolution);
	}
	public void setResolution(Size resolution) {
		setResolution(resolution.width, resolution.height);
	}
	public void setResolution(int width, int height) {
		Size prevResolution = this.getResolution();
		disconnectCamera();
		mMaxHeight = height;
		mMaxWidth = width;
		if(!connectCamera(width, height/*getWidth(), getHeight()*/))
			connectCamera(prevResolution.width, prevResolution.height);
	}

	public Size getResolution() {
		return mCamera.getParameters().getPreviewSize();
	}

	public Camera getCamera(){
		return mCamera;
	}
/*
	public void takePicture(final String fileName) {
		Log.i(TAG, "Taking picture");
		this.mPictureFileName = fileName;
		// Postview and jpeg are sent in the same buffers if the queue is not empty when performing a capture.
		// Clear up buffers to avoid mCamera.takePicture to be stuck because of a memory issue
		mCamera.setPreviewCallback(null);

		// PictureCallback is implemented by the current class
		mCamera.takePicture(null, null, this);
	}

	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		Log.i(TAG, "Saving a bitmap to file");
		// The camera preview was automatically stopped. Start it again.
		mCamera.startPreview();
		mCamera.setPreviewCallback(this);

		// Write the image in a file (in jpeg format)
		try {
			FileOutputStream fos = new FileOutputStream(mPictureFileName);

			fos.write(data);
			fos.close();

		} catch (java.io.IOException e) {
			Log.e("Picture", "Exception in photoCallback", e);
		}

	}*/
}