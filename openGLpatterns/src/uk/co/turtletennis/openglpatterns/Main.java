package uk.co.turtletennis.openglpatterns;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.MotionEvent;

public class Main extends Activity implements SensorEventListener {
    private GLSurfaceView mGLView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity
        mGLView = new MyGLSurfaceView(this);
        setContentView(mGLView);
    }
    
    
    public void onSensorChanged(SensorEvent event){
    	int i=0;
    	//mGLView.onSensorChanged(event);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // The following call pauses the rendering thread.
        // If your OpenGL application is memory intensive,
        // you should consider de-allocating objects that
        // consume significant memory here.
        mGLView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // The following call resumes a paused rendering thread.
        // If you de-allocated graphic objects for onPause()
        // this is a good place to re-allocate them.
        mGLView.onResume();
    }


	public void onAccuracyChanged(Sensor arg0, int arg1) {
		
		
	}
}

class MyGLSurfaceView extends GLSurfaceView implements SensorEventListener {

    private final GLRenderer mRenderer;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    
    public MyGLSurfaceView(Context context) {
        super(context);

        currentRotationMatrix[0]=1;
        currentRotationMatrix[5]=1;
        currentRotationMatrix[10]=1;
        currentRotationMatrix[15]=1;
        // Create an OpenGL ES 2.0 context.
        setEGLContextClientVersion(2);
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensorlist=mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
        if(sensorlist.size()>0){
        	mAccelerometer = sensorlist.get(0);
        	mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        	
        }
        //mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
        // Set the Renderer for drawing on the GLSurfaceView
        mRenderer = new GLRenderer();
        setRenderer(mRenderer);

        // Render the view only when there is a change in the drawing data
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

 // Create a constant to convert nanoseconds to seconds.
    private static final float NS2S = 1.0f / 1000000000.0f;
    private final float[] deltaRotationVector = new float[4];
    private final float[] currentRotationMatrix = new float[16];
    private float timestamp;
    private final float EPSILON = 0.01f;
    private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
    private final float NOISE = 2.0f;
    private final float alpha = 0.8f;
    private final double twoPi = 2.0 * Math.PI;
    private float mPreviousX;
    private float mPreviousY;
    private float mPreviousXAcc;
    private float mPreviousYAcc;
    private float mPreviousZAcc;
    private double mPreviousTouchAngle;
    private boolean initialised = false;
    private float mTouchMomentum;
    
    public void onSensorChanged(SensorEvent event){
    	
		final float dT = (event.timestamp-timestamp) * NS2S;
		// Axis of the rotation sample, not normalised yet.
	    float x = event.values[0];
	    float y = event.values[1];
	    float z = event.values[2];
	    
	    if(!initialised){
	    	mPreviousXAcc=x;
	    	mPreviousYAcc=y;
	    	mPreviousZAcc=z;
	    	initialised=true;
	    	
	    } else{
	    	float dX=Math.abs( mPreviousXAcc-x);
		    float dY=Math.abs( mPreviousYAcc-y);
		    float dZ=Math.abs( mPreviousZAcc-z);
		    if(dX<NOISE) dX=0.0f;
		    if(dY<NOISE) dY=0.0f;
		    if(dZ<NOISE) dZ=0.0f;
		    mPreviousXAcc=x;
	    	mPreviousYAcc=y;
	    	mPreviousZAcc=z;
	    	mRenderer.mTiltAngleY-=x/4.0;
	    	mRenderer.mTiltAngleX+=y/4.0;
	    	
	    }
	    
	    requestRender();
	
    }
    /*public void onSensorChanged(SensorEvent event){
    	if(timestamp!=0){
    		final float dT = (event.timestamp-timestamp) * NS2S;
    		// Axis of the rotation sample, not normalised yet.
    	    float axisX = event.values[0];
    	    float axisY = event.values[1];
    	    float axisZ = event.values[2];
    	    
    	 // Calculate the angular speed of the sample
    	    float omegaMagnitude = (float) Math.sqrt(axisX*axisX + axisY*axisY + axisZ*axisZ);
    	    
    	    if (omegaMagnitude > EPSILON) {
    	        axisX /= omegaMagnitude;
    	        axisY /= omegaMagnitude;
    	        axisZ /= omegaMagnitude;
    	      }
    	    
    	 // Integrate around this axis with the angular speed by the timestep
    	    // in order to get a delta rotation from this sample over the timestep
    	    // We will convert this axis-angle representation of the delta rotation
    	    // into a quaternion before turning it into the rotation matrix.
    	    
    	    float thetaOverTwo= omegaMagnitude * dT /2.0f;
    	    deltaRotationVector[0] = (float) (axisX * Math.sin(thetaOverTwo));
    	    deltaRotationVector[1] = (float) (axisY * Math.sin(thetaOverTwo));
    	    deltaRotationVector[2] = (float) (axisZ * Math.sin(thetaOverTwo));
    	    deltaRotationVector[3] = (float) (Math.cos(thetaOverTwo));
    	    mRenderer.mTiltAngle+=deltaRotationVector[0]+deltaRotationVector[1];
    	    timestamp=event.timestamp;
    	    float[] deltaRotationMatrix = new float[16];
    	    SensorManager.getRotationMatrixFromVector(deltaRotationMatrix, deltaRotationVector);
    	      // User code should concatenate the delta rotation we computed with the current rotation
    	      // in order to get the updated rotation.
    	      // rotationCurrent = rotationCurrent * deltaRotationMatrix;
    	    float[] newRotation = new float[16];
    	    Matrix.multiplyMM(newRotation, 0,currentRotationMatrix, 0,deltaRotationMatrix,0);
    	    
    	    for(int i=0; i<16; i++){
    	    	currentRotationMatrix[i]=newRotation[i];
    	    }
    	    requestRender();
    	}
    }*/
    
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

        float x = e.getX();
        float y = e.getY();
        int midX = getWidth() / 2;
        int midY = getHeight() / 2;
        double touchAngle = Math.atan2(y-midY, x-midX);
        if(touchAngle<0) touchAngle = twoPi + touchAngle;//change range from *pi to -pi) to (0 to 2pi)
        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:

                float dx = x - mPreviousX;
                float dy = y - mPreviousY;
                double dTheta = touchAngle-mPreviousTouchAngle;
                

                
                // reverse direction of rotation above the mid-line
                if (y > midY) {
                  dx = dx * -1 ;
                  if(mPreviousY <= midY && x > midX){// going over the boundary from 0 to 2pi dTheta will be greater than 2pi
                	  dTheta += twoPi;
                  }
                } else{
                	if(mPreviousY >=midY && x > midX){// going over the boundary from 2pi to 0 dTheta will be as small as -2pi
                		dTheta -= twoPi;
                	}
                }
                
                

                // reverse direction of rotation to left of the mid-line
                if (x < midX) {
                  dy = dy * -1 ;
                }
                mTouchMomentum += (dx + dy) * TOUCH_SCALE_FACTOR * 0.01;
                if(mTouchMomentum > 100) mTouchMomentum=100;
                
                if(mTouchMomentum<-100)mTouchMomentum=-100;
                
                
                
                //mRenderer.mAngle -= (dx + dy) * TOUCH_SCALE_FACTOR;  // = 180.0f / 320
                //mRenderer.mAngleMomentum =mTouchMomentum;
                mRenderer.mAngleMomentum +=dTheta*-10.0;
                if(mRenderer.mAngleMomentum>100) mRenderer.mAngleMomentum =100;
                if(mRenderer.mAngleMomentum<-100) mRenderer.mAngleMomentum =-100;
                requestRender();
        }

        mPreviousX = x;
        mPreviousY = y;
        mPreviousTouchAngle = touchAngle;
        return true;
    }
    
    @Override
    public void onResume(){
    	super.onResume();
    	mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    	mSensorManager.registerListener(this, mAccelerometer,SensorManager.SENSOR_DELAY_GAME);
    	
    }
    
    @Override
    public void onPause(){
    	mSensorManager.unregisterListener(this);
    }

	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}
}
