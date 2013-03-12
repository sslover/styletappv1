package com.shilpanbhagat.writetagapplication;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;

/** A basic Camera preview class */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;
    public static final int LARGEST_WIDTH = 6000;
    public static final int LARGEST_HEIGHT= 6000;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        
        
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
    	try {
    		Camera.Parameters parameters = mCamera.getParameters();
    		if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
	    		mCamera.setDisplayOrientation(90);
	    		parameters.setRotation(90);
	    	}
		    mCamera.setParameters(parameters);
		    mCamera.setPreviewDisplay(holder);
		    mCamera.startPreview();
		    	
		    Log.v("SNAPSHOT","Preview Layout Size: " + this.getWidth() + " " + this.getHeight());
		    Log.v("SNAPSHOT","Camera Preview Size: " + mCamera.getParameters().getPreviewSize().width + " " + mCamera.getParameters().getPreviewSize().height);
	    } 
    	catch (IOException exception) {
	    		mCamera.release();
	    		Log.v(VIEW_LOG_TAG,"Error setting camera preview: " + exception.getMessage());
    	}
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null){
          // preview surface does not exist
          return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e){
          // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

        } catch (Exception e){
            Log.d(VIEW_LOG_TAG, "Error starting camera preview: " + e.getMessage());
        }
    }
}