package aidev.cocis.makerere.org.whiteflycounter;


import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_nonfree;
import org.bytedeco.javacpp.opencv_objdetect;
import org.bytedeco.javacv.AndroidFrameConverter;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameRecorder;
import org.bytedeco.javacv.OpenCVFrameConverter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_8U;
import static org.bytedeco.javacpp.opencv_core.cvCircle;
import static org.bytedeco.javacpp.opencv_core.cvClearMemStorage;
import static org.bytedeco.javacpp.opencv_core.cvScalar;
import static org.bytedeco.javacpp.opencv_highgui.cvSaveImage;
import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.cvCvtColor;

/**
 * Created by User on 7/8/2015.
 */


public class FFmpegRecorderActivity extends Activity implements View.OnClickListener, View.OnTouchListener {

    private static final int[] SCANNER_ALPHA = {0, 64, 128, 192, 255, 192, 128, 64};
    private static final long ANIMATION_DELAY = 80L;
    private static final int CURRENT_POINT_OPACITY = 0xA0;
    private static final int MAX_RESULT_POINTS = 20;
    private static final int POINT_SIZE = 6;

    private  Paint paint;
    private Bitmap resultBitmap;
    private  int maskColor;
    private  int resultColor;
    private  int laserColor;
    private  int resultPointColor;
    private int scannerAlpha;

    ProgressDialog barProgressDialog;
    private opencv_objdetect.CascadeClassifier classifier;
    private opencv_core.CvMemStorage storage;
    private opencv_core.Rect white_flies = new opencv_core.Rect();
    int tt = 0;
    File mFolder = new File(Environment.getExternalStorageDirectory() + File.separator+"whiteflycounter");

    private final static String CLASS_LABEL = "RecordActivity";

    private PowerManager.WakeLock mWakeLock;
    private String strVideoPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "rec_video.mp4";
    private String strFinalPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "rec_final.mp4";
    private File fileVideoPath = null;
    private File tempFolderPath = null;
    private Uri uriVideoPath = null;
    private boolean rec = false;
    boolean recording = false, isRecordingStarted = false;

    boolean isFlashOn = false;
    TextView txtTimer;
    ImageView recorderIcon = null;
    ImageView flashIcon = null, switchCameraIcon = null, resolutionIcon = null;
    private volatile FFmpegFrameRecorder videoRecorder;
    private boolean isPreviewOn = false;
    private int currentResolution = CONSTANTS.RESOLUTION_MEDIUM_VALUE;
    private Camera mCamera;

    private int previewWidth = 320, screenWidth = 320;
    private int previewHeight = 240, screenHeight = 240;
    private int sampleRate = 44100;


    /* video data getting thread */
    private Camera cameraDevice;
    private CameraView cameraView;
    Camera.Parameters cameraParameters = null;
    private Frame yuvImage = null;
    Frame[] images;
    int defaultCameraId = -1, defaultScreenResolution = -1 , cameraSelection = 0;
	/* layout setting */


    private Handler mHandler = new Handler();

    private 	Dialog dialog = null;
    RelativeLayout topLayout = null;
    Dialog	selectResolutionDialog = null;
    RelativeLayout previewLayout = null;

    long firstTime = 0;
    long startPauseTime = 0;
    long totalPauseTime = 0;
    long pausedTime = 0;
    long stopPauseTime = 0;
    long totalTime = 0;
    private int frameRate = 30;
    private int recordingTime = 6000000;
    private int recordingMinimumTime = 5000;
    boolean recordFinish = false;
    private Dialog creatingProgress;

    private final int[] mVideoRecordLock = new int[0];
    private long frameTime = 0L;
    private SavedFrames lastSavedframe = new SavedFrames(null,0L);
    private long mVideoTimestamp = 0L;
    BroadcastReceiver mReceiver = null;
    private boolean isRecordingSaved = false;
    private boolean isFinalizing = false;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Loader.load(opencv_nonfree.class);

        // Load the classifier file from Java resources.
        try {
            InputStream is = getResources().openRawResource(R.raw.wf_cascade);
            File classifierFile = new File(getApplicationContext().getCacheDir(), "wf_cascade.xml");
            FileOutputStream os = new FileOutputStream(classifierFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();


            if (classifierFile.length() <= 0) {
                throw new IOException("Could not extract the classifier file.");
            }

            // Pre-load the opencv_objdetect module to work around a known bug.
            Loader.load(opencv_objdetect.class);
            classifier = new opencv_objdetect.CascadeClassifier();
            classifier.load(classifierFile.getAbsolutePath());

            classifierFile.delete();
            if (classifier.isNull()) {
                throw new IOException("Could not load the classifier file.");
            }
        }catch (Exception ex){

            ex.printStackTrace();
        }
        storage = opencv_core.CvMemStorage.create();


        setContentView(R.layout.ffmpeg_recorder);


        barProgressDialog = new ProgressDialog(FFmpegRecorderActivity.this);

        barProgressDialog.setTitle("Analysing Image ...");
        barProgressDialog.setMessage("White Fly Detection in progress ...");
        barProgressDialog.setProgressStyle(barProgressDialog.STYLE_HORIZONTAL);
        barProgressDialog.setProgress(0);
        barProgressDialog.setMax(20);

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, CLASS_LABEL);
        mWakeLock.acquire();

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        //Find screen dimensions
        screenWidth = displaymetrics.widthPixels;
        screenHeight = displaymetrics.heightPixels;

        tempFolderPath = Util.getTempFolderPath();
        if(tempFolderPath != null)
            tempFolderPath.mkdirs();

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Resources resources = getResources();
        maskColor = resources.getColor(R.color.viewfinder_mask);
        resultColor = resources.getColor(R.color.result_view);
        laserColor = resources.getColor(R.color.viewfinder_laser);
        resultPointColor = resources.getColor(R.color.possible_result_points);
        scannerAlpha = 0;

        initLayout();
        initVideoRecorder();
        startRecording();

    }

    public void onDraw(Canvas canvas) {


        int width = canvas.getWidth();
        int height = canvas.getHeight();

        // Draw the exterior (i.e. outside the framing rect) darkened
        paint.setColor(resultBitmap != null ? resultColor : maskColor);
        canvas.drawRect(0, 0, width, 100, paint);
        canvas.drawRect(0, 100, 100, 100+ 1, paint);
        canvas.drawRect(100 + 1, 100, width, 100 + 1, paint);
        canvas.drawRect(0, 100 + 1, width, height, paint);

        if (resultBitmap != null) {
            // Draw the opaque result bitmap over the scanning rectangle
            paint.setAlpha(CURRENT_POINT_OPACITY);
            //canvas.drawBitmap(resultBitmap, null, frame, paint);
        } else {

            // Draw a red "laser scanner" line through the middle to show decoding is active
            paint.setColor(laserColor);
            paint.setAlpha(SCANNER_ALPHA[scannerAlpha]);
            scannerAlpha = (scannerAlpha + 1) % SCANNER_ALPHA.length;
            int middle = 100 / 2 + 100;
            canvas.drawRect(100 + 2, middle - 1, 100 - 1, middle + 2, paint);

        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mWakeLock == null) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, CLASS_LABEL);
            mWakeLock.acquire();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(!isFinalizing)
            finish();
        if (mWakeLock != null) {
            mWakeLock.release();
            mWakeLock = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mReceiver != null)
            unregisterReceiver(mReceiver);
        recording = false;
        if (cameraView != null) {
            cameraView.stopPreview();
            if(cameraDevice != null)
                cameraDevice.release();
            cameraDevice = null;
        }
        if (mWakeLock != null) {
            mWakeLock.release();
            mWakeLock = null;
        }
    }
    private void initLayout()
    {
        previewLayout  = (RelativeLayout) (((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.ffmpeg_recorder, null));
        txtTimer = (TextView)  previewLayout.findViewById(R.id.txtTimer);
        recorderIcon = (ImageView) previewLayout.findViewById(R.id.recorderIcon);
        resolutionIcon = (ImageView) previewLayout.findViewById(R.id.resolutionIcon);
        flashIcon = (ImageView) previewLayout.findViewById(R.id.flashIcon);
        switchCameraIcon = (ImageView) previewLayout.findViewById(R.id.switchCameraIcon);

        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            flashIcon.setOnClickListener(this);
            flashIcon.setVisibility(View.VISIBLE);
        }
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
            switchCameraIcon.setOnClickListener(this);
            switchCameraIcon.setVisibility(View.VISIBLE);
        }
        initCameraLayout();
    }

    private void initCameraLayout() {

        if(topLayout != null && topLayout.getChildCount() > 0)
            topLayout.removeAllViews();
        topLayout = new RelativeLayout(this);
        RelativeLayout.LayoutParams layoutParam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

        setCamera();
        handleSurfaceChanged();

        RelativeLayout.LayoutParams layoutParam1 = new RelativeLayout.LayoutParams(screenWidth,screenHeight);
        layoutParam1.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        //int margin = Util.calculateMargin(previewWidth, screenWidth);
        layoutParam1.setMargins(0, 0,0, 0);

        // add the camera preview
        topLayout.addView(cameraView, layoutParam1);
        // add the overlay for buttons and textviews
        topLayout.addView(previewLayout, layoutParam);
        topLayout.setLayoutParams(layoutParam);
        setContentView(topLayout);
        topLayout.setOnTouchListener(this);
    }

    private void setCamera()
    {
        try
        {
            // Find the total number of cameras available
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO)
            {
                int numberOfCameras = Camera.getNumberOfCameras();
                // Find the ID of the default camera
                Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                for (int i = 0; i < numberOfCameras; i++) {
                    Camera.getCameraInfo(i, cameraInfo);
                    if (cameraInfo.facing == cameraSelection) {
                        defaultCameraId = i;
                    }
                }
            }
            stopPreview();
            if(mCamera != null)
                mCamera.release();
            if(defaultCameraId >= 0)
                cameraDevice = Camera.open(defaultCameraId);
            else
                cameraDevice = Camera.open();

            cameraView = new CameraView(this, cameraDevice);

        }
        catch(Exception e)
        {
            finish();
        }
    }
    private void initVideoRecorder() {
        strVideoPath = Util.createTempPath(tempFolderPath);
        RecorderParameters recorderParameters = Util.getRecorderParameter(currentResolution);
        fileVideoPath = new File(strVideoPath);
        videoRecorder = new FFmpegFrameRecorder(strVideoPath, previewWidth, previewHeight, 1);
        videoRecorder.setFormat(recorderParameters.getVideoOutputFormat());
        videoRecorder.setFrameRate(recorderParameters.getVideoFrameRate());
        videoRecorder.setVideoCodec(recorderParameters.getVideoCodec());
        videoRecorder.setVideoQuality(recorderParameters.getVideoQuality());
        videoRecorder.setVideoBitrate(1000000);
    }

    public void startRecording() {

        try {
            videoRecorder.start();

        } catch (FFmpegFrameRecorder.Exception e) {
            e.printStackTrace();
        }
    }

    public class AsyncStopRecording extends AsyncTask<Void,Void,Void>
    {
        @Override
        protected void onPreExecute() {
            isFinalizing = true;
            recordFinish = true;
            creatingProgress= new Dialog(FFmpegRecorderActivity.this);
            creatingProgress.setCanceledOnTouchOutside(false);
            creatingProgress.setTitle("Finalizing");
            creatingProgress.show();
            recorderIcon.setVisibility(View.GONE);
            txtTimer.setVisibility(View.INVISIBLE);
            resolutionIcon.setVisibility(View.VISIBLE);
            mHandler.removeCallbacks(mUpdateTimeTask);
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            isFinalizing = false;
            if (videoRecorder != null && recording) {
                recording = false;
                releaseResources();
                strFinalPath = Util.createFinalPath();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            creatingProgress.dismiss();
            registerVideo();
            returnToCaller(true);
            videoRecorder = null;
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (recording) {
                sendDialog(null);
                return true;
            }
            else
            {
                videoTheEnd(false);
                return false;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    private void sendDialog(String title)
    {
        dialog = new Dialog(FFmpegRecorderActivity.this);
        if(title != null && title.length() > 0)
            dialog.setTitle(title);
        else
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        dialog.setContentView(R.layout.confirmation_dialog);
        dialog.setCanceledOnTouchOutside(true);

        ((Button) dialog.findViewById(R.id.btnDiscard)).setText("Discard");
        ((Button) dialog.findViewById(R.id.btnContinue)).setText("Continue");

        ((Button) dialog.findViewById(R.id.btnDiscard)).setOnClickListener(this);
        ((Button) dialog.findViewById(R.id.btnContinue)).setOnClickListener(this);

        dialog.show();
    }


    //---------------------------------------------
    // camera thread, gets and encodes video data
    //---------------------------------------------
    class CameraView extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {

        private SurfaceHolder mHolder;


        public CameraView(Context context, Camera camera) {
            super(context);
            mCamera = camera;
            cameraParameters = mCamera.getParameters();
            mHolder = getHolder();
            mHolder.addCallback(CameraView.this);
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            mCamera.setPreviewCallback(CameraView.this);
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                stopPreview();
                mCamera.setPreviewDisplay(holder);
            } catch (IOException exception) {
                mCamera.release();
                mCamera = null;
            }
        }

        public void surfaceChanged(SurfaceHolder  holder, int format, int width, int height) {
            if (isPreviewOn)
                mCamera.stopPreview();
            handleSurfaceChanged();
            startPreview();
            try {

                mCamera.autoFocus(null);

            }catch (Exception ex){
                Log.e("Camera app","Failed"+ex.getStackTrace());
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            try {
                mHolder.addCallback(null);
                mCamera.setPreviewCallback(null);
            } catch (RuntimeException e) {
                // The camera has probably just been released, ignore.
            }
        }

        public void startPreview() {
            if (!isPreviewOn && mCamera != null) {
                isPreviewOn = true;
                mCamera.startPreview();
            }
        }

        public void stopPreview() {
            if (isPreviewOn && mCamera != null) {
                isPreviewOn = false;
                mCamera.stopPreview();
            }
        }
        @Override
        public void onPreviewFrame(final byte[] data, Camera camera) {
			/* get video data */
            long frameTimeStamp = 0L;

            synchronized (mVideoRecordLock) {
                if (recording && rec && lastSavedframe != null && lastSavedframe.getFrameBytesData() != null && yuvImage  != null)
                {
                    mVideoTimestamp += frameTime;
                    if(lastSavedframe.getTimeStamp() > mVideoTimestamp)
                        mVideoTimestamp = lastSavedframe.getTimeStamp();
                    try {
                        //videoRecorder.setTimestamp(lastSavedframe.getTimeStamp());
                        //((ByteBuffer)yuvImage.image[0].position(0)).put(lastSavedframe.getFrameBytesData());

                        yuvImage = new AndroidFrameConverter().convert(data,previewWidth,previewHeight);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                barProgressDialog.show();
                            }
                        });


                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    barProgressDialog.incrementProgressBy(2);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {

                                            barProgressDialog.setMessage("Taking Picture...");
                                        }
                                    });


                                    barProgressDialog.incrementProgressBy(2);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {

                                            barProgressDialog.setMessage("Analysis Started..");
                                        }
                                    });

                                    processImage();


                                } catch (Exception e) {
                                    e.printStackTrace();
                                } finally {
                                    barProgressDialog.dismiss();
                                }
                            }
                        }).start();



                        Toast.makeText(getApplicationContext(), "Picture Saved",Toast.LENGTH_SHORT).show();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                lastSavedframe = new SavedFrames(data,frameTimeStamp);
            }
        }
    }


    protected void processImage() {


        OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();
        opencv_core.IplImage grabbedImage = converter.convert(yuvImage);

        opencv_core.IplImage grayIplImage =opencv_core.IplImage.create(grabbedImage.width(), grabbedImage.height(), IPL_DEPTH_8U, 1);

        cvCvtColor(grabbedImage, grayIplImage, CV_BGR2GRAY);

        opencv_core.Mat imgMat = new opencv_core.Mat(grayIplImage, true); //true - required to copy data

        barProgressDialog.incrementProgressBy(2);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                barProgressDialog.setMessage("...Detecting Whiteflies...");
            }
        });


         classifier.detectMultiScale(imgMat, white_flies, 1.1, 2, 2, new opencv_core.Size(10, 10), new opencv_core.Size(25,25));


        tt = white_flies.capacity();
        if(tt>0){
            barProgressDialog.incrementProgressBy(2);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    barProgressDialog.setMessage("Found " + tt + " white files.Preparing to show reults..");
                }
            });
        }
        for (int j = 0; j < tt; j++) {
            opencv_core.CvRect r = new opencv_core.CvRect(white_flies.position(j));

            opencv_core.CvPoint center =  new opencv_core.CvPoint();

            center.x(r.x()+9); center.y(r.y()+11);

            cvCircle(grabbedImage, center, 8, cvScalar(0, 0, 255, 0), 2, 8, 0);


        }
        barProgressDialog.incrementProgressBy(4);
        File imgFile = new File( String.format( mFolder.getAbsolutePath()+ File.separator +"%d_%d.jpg", System.currentTimeMillis(),tt));


        cvSaveImage(
                imgFile.getAbsolutePath(),
                grabbedImage);

        cvClearMemStorage(storage);

        barProgressDialog.dismiss();
        barProgressDialog.incrementProgressBy(-20);

        Intent analysisIntent = new Intent(this, ShowAnalysis.class);
        analysisIntent.putExtra("imagePath", imgFile.getAbsoluteFile() + "");
        analysisIntent.putExtra("count",tt+"");
        startActivity(analysisIntent);
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if(!recordFinish)
        {
            if(totalTime< recordingTime)
            {
                switch (event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        if(!recording)
                            initiateRecording(true);
                        else
                        {
                            stopPauseTime = System.currentTimeMillis();
                            totalPauseTime = stopPauseTime - startPauseTime - ((long) (1.0/(double)frameRate)*1000);
                            pausedTime += totalPauseTime;
                        }
                        rec = true;
                        setTotalVideoTime();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        rec = true;
                        setTotalVideoTime();
                        break;
                    case MotionEvent.ACTION_UP:
                        rec = false;
                        startPauseTime = System.currentTimeMillis();
                        break;
                }
            }
            else
            {
                rec = false;
               // saveRecording();
            }
        }
        return true;
    }
    public void stopPreview() {
        if (isPreviewOn && mCamera != null) {
            isPreviewOn = false;
            mCamera.stopPreview();

        }
    }

    private void handleSurfaceChanged()
    {
        List<Camera.Size> resolutionList = Util.getResolutionList(mCamera);
        resolutionIcon.setVisibility(View.GONE);
        if(resolutionList != null && resolutionList.size() > 0)
        {
            Collections.sort(resolutionList, new Util.ResolutionComparator());
            if(resolutionList.size() > 1 && !recording)
            {
                resolutionIcon.setOnClickListener(FFmpegRecorderActivity.this);
                resolutionIcon.setVisibility(View.VISIBLE);
            }
            Camera.Size previewSize =  null;
            if(defaultScreenResolution == -1)
            {
                int mediumResolution = resolutionList.size()/2;
                if(mediumResolution >= resolutionList.size())
                    mediumResolution = resolutionList.size() - 1;
                previewSize = resolutionList.get(mediumResolution);
            }
            else
            {
                if(defaultScreenResolution >= resolutionList.size())
                    defaultScreenResolution = resolutionList.size() - 1;
                previewSize = resolutionList.get(defaultScreenResolution);
            }
            if(previewSize != null )
            {
                previewWidth = previewSize.width;
                previewHeight = previewSize.height;
                cameraParameters.setPreviewSize(previewWidth, previewHeight);
                if(videoRecorder != null)
                {
                    videoRecorder.setImageWidth(previewWidth);
                    videoRecorder.setImageHeight(previewHeight);
                }

            }
        }
        cameraParameters.setPreviewFpsRange(1000, frameRate*1000);
        yuvImage  = new Frame (previewWidth, previewHeight, Frame.DEPTH_UBYTE, 2);


        if(Build.VERSION.SDK_INT >  Build.VERSION_CODES.FROYO)
        {
            mCamera.setDisplayOrientation(Util.determineDisplayOrientation(FFmpegRecorderActivity.this, defaultCameraId));
            List<String> focusModes = cameraParameters.getSupportedFocusModes();
            if (focusModes != null && focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO))
            {
                cameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            }
        }
        else
            mCamera.setDisplayOrientation(90);
        try {
            mCamera.setParameters(cameraParameters);
        }catch (Exception ex){

        }

    }
    @Override
    public void onClick(View v) {
        List<Camera.Size> resList = Util.getResolutionList(mCamera);

        if(v.getId() == R.id.flashIcon)
        {
            if(isFlashOn)
            {
                flashIcon.setImageDrawable(getResources().getDrawable(R.drawable.cameraflashoff));
                isFlashOn = false;
                cameraParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            }
            else
            {
                flashIcon.setImageDrawable(getResources().getDrawable(R.drawable.cameraflash));
                isFlashOn = true;
                cameraParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            }
            try {
                mCamera.setParameters(cameraParameters);
            }catch (Exception ex){

            }
        }
        else if(v.getId() ==  R.id.resolutionIcon)
        {
            if(!dismissResolutionSelectionDialog())
            {
                selectResolutionDialog = new Dialog(FFmpegRecorderActivity.this, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen)
                {
                    public boolean onTouchEvent(MotionEvent event) {
                        if(event.getAction() == MotionEvent.ACTION_UP){
                            dismissResolutionSelectionDialog();
                        }
                        return false;
                    };
                };
                selectResolutionDialog.setCanceledOnTouchOutside(true);
                selectResolutionDialog.setContentView(R.layout.dialog_resolution_selector);

                RelativeLayout rootLayout = (RelativeLayout) selectResolutionDialog.findViewById(R.id.rootLayout);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                int[] resolutionIconPos = new int[2];
                resolutionIcon.getLocationOnScreen(resolutionIconPos);
                params.gravity = Gravity.TOP| Gravity.RIGHT;
                params.setMargins(0, resolutionIconPos[1],(resolutionIcon.getWidth()*2) - 10, 0);
                rootLayout.setLayoutParams(params);
                TextView txtHighResolution = (TextView) selectResolutionDialog.findViewById(R.id.txtHighResolution);
                TextView txtMediumResolution = (TextView) selectResolutionDialog.findViewById(R.id.txtMediumResolution);
                TextView txtLowResolution = (TextView) selectResolutionDialog.findViewById(R.id.txtLowResolution);

                RadioButton radioHighResolution = (RadioButton) selectResolutionDialog.findViewById(R.id.radioHighResolution);
                RadioButton radioMediumResolution = (RadioButton) selectResolutionDialog.findViewById(R.id.radioMediumResolution);
                RadioButton radioLowResolution = (RadioButton) selectResolutionDialog.findViewById(R.id.radioLowResolution);

                if(currentResolution == CONSTANTS.RESOLUTION_LOW_VALUE)
                {
                    radioHighResolution.setChecked(false);
                    radioMediumResolution.setChecked(false);
                    radioLowResolution.setChecked(true);
                }
                else if(currentResolution == CONSTANTS.RESOLUTION_MEDIUM_VALUE)
                {
                    radioHighResolution.setChecked(false);
                    radioMediumResolution.setChecked(true);
                    radioLowResolution.setChecked(false);
                }
                else if(currentResolution == CONSTANTS.RESOLUTION_HIGH_VALUE)
                {
                    radioHighResolution.setChecked(true);
                    radioMediumResolution.setChecked(false);
                    radioLowResolution.setChecked(false);
                }
                txtHighResolution.setOnClickListener(this);
                txtMediumResolution.setOnClickListener(this);
                txtLowResolution.setOnClickListener(this);

                radioHighResolution.setOnClickListener(this);
                radioMediumResolution.setOnClickListener(this);
                radioLowResolution.setOnClickListener(this);

                if(resList != null && resList.size() == 2)
                    txtMediumResolution.setVisibility(View.GONE);

                Window window = this.getWindow();
                window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
                window.setFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);

                selectResolutionDialog.show();
            }
        }
        else if(v.getId() == R.id.switchCameraIcon)
        {
            cameraSelection = ((cameraSelection == Camera.CameraInfo.CAMERA_FACING_BACK) ? Camera.CameraInfo.CAMERA_FACING_FRONT: Camera.CameraInfo.CAMERA_FACING_BACK);
            initCameraLayout();

            if(cameraSelection == Camera.CameraInfo.CAMERA_FACING_FRONT)
                flashIcon.setVisibility(View.GONE);
            else
            {
                flashIcon.setVisibility(View.VISIBLE);
                if(isFlashOn)
                {
                    cameraParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    mCamera.setParameters(cameraParameters);
                }
            }
        }
        else if(v.getId() == R.id.btnContinue)
        {
            dialog.dismiss();
        }
        else if(v.getId() == R.id.btnDiscard)
        {
            dialog.dismiss();
            videoTheEnd(false);
        }
        else if(v.getId() == R.id.txtHighResolution || v.getId() == R.id.radioHighResolution)
        {
            if(currentResolution != CONSTANTS.RESOLUTION_HIGH_VALUE)
            {
                setHighResolution(resList.size());
            }
        }
        else if(v.getId() == R.id.txtMediumResolution || v.getId() == R.id.radioMediumResolution)
        {
            if(currentResolution != CONSTANTS.RESOLUTION_MEDIUM_VALUE)
            {
                setMediumResolution(resList.size());
            }
        }
        else if(v.getId() == R.id.txtLowResolution || v.getId() == R.id.radioLowResolution)
        {
            if(currentResolution != CONSTANTS.RESOLUTION_LOW_VALUE)
            {
                setLowResolution();
            }
        }
    }
    private boolean dismissResolutionSelectionDialog()
    {
        if(selectResolutionDialog != null)
        {
            selectResolutionDialog.dismiss();
            selectResolutionDialog = null;
            return true;
        }
        return false;
    }

    private void setHighResolution(int size)
    {
        ((RadioButton)(selectResolutionDialog.findViewById(R.id.radioHighResolution))).setChecked(true);
        ((RadioButton)(selectResolutionDialog.findViewById(R.id.radioMediumResolution))).setChecked(false);
        ((RadioButton)(selectResolutionDialog.findViewById(R.id.radioLowResolution))).setChecked(false);
        defaultScreenResolution = ((size/2) + 1);
        currentResolution = CONSTANTS.RESOLUTION_HIGH_VALUE;
        initCameraLayout();
        dismissResolutionSelectionDialog();
    }

    private void setMediumResolution(int size)
    {
        ((RadioButton)(selectResolutionDialog.findViewById(R.id.radioHighResolution))).setChecked(false);
        ((RadioButton)(selectResolutionDialog.findViewById(R.id.radioMediumResolution))).setChecked(true);
        ((RadioButton)(selectResolutionDialog.findViewById(R.id.radioLowResolution))).setChecked(false);
        defaultScreenResolution = (size/2);
        currentResolution = CONSTANTS.RESOLUTION_MEDIUM_VALUE;
        initCameraLayout();
        dismissResolutionSelectionDialog();
    }

    private void setLowResolution()
    {
        ((RadioButton)(selectResolutionDialog.findViewById(R.id.radioHighResolution))).setChecked(false);
        ((RadioButton)(selectResolutionDialog.findViewById(R.id.radioMediumResolution))).setChecked(false);
        ((RadioButton)(selectResolutionDialog.findViewById(R.id.radioLowResolution))).setChecked(true);
        defaultScreenResolution = 0;
        currentResolution = CONSTANTS.RESOLUTION_LOW_VALUE;
        initCameraLayout();
        dismissResolutionSelectionDialog();
    }


    public void videoTheEnd(boolean isSuccess)
    {
        releaseResources();

        if(fileVideoPath != null && fileVideoPath.exists() && !isSuccess)
            fileVideoPath.delete();
        returnToCaller(isSuccess);
    }

    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            if(rec)
                setTotalVideoTime();
            mHandler.postDelayed(this, 500);
        }
    };
    private void returnToCaller(boolean valid)
    {
        try
        {
            setActivityResult(valid);
            finish();
        } catch (Throwable e)
        {
        }
    }

    private void setActivityResult(boolean valid)
    {
        Intent resultIntent = new Intent();
        if(tempFolderPath != null)
            resultIntent.putExtra(CONSTANTS.KEY_DELETE_FOLDER_FROM_SDCARD, tempFolderPath.getAbsolutePath());
        int resultCode;
        if (valid)
        {
            resultCode = RESULT_OK;
            resultIntent.setData(uriVideoPath);
        } else
            resultCode = RESULT_CANCELED;

        setResult(resultCode, resultIntent);
    }


    private void registerVideo()
    {
        Uri videoTable = Uri.parse(CONSTANTS.VIDEO_CONTENT_URI);
        Util.videoContentValues.put(MediaStore.Video.Media.SIZE, new File(strFinalPath).length());
        try
        {
            uriVideoPath = getContentResolver().insert(videoTable, Util.videoContentValues);
        } catch (Throwable e)
        {
            // We failed to insert into the database. This can happen if
            // the SD card is unmounted.
            uriVideoPath = null;
            strFinalPath = null;
            e.printStackTrace();
        } finally
        {}
        Util.videoContentValues = null;
    }


    private void saveRecording()
    {
        if(isRecordingStarted)
        {
            // This will make the executor accept no new threads
            // and finish all existing threads in the queue
            if(!isRecordingSaved)
            {
                //  pool.shutdown();
                // Wait until all threads are finish
                // pool.awaitTermination(firstTime, null);
                isRecordingSaved = true;
                new AsyncStopRecording().execute();
            }
        }
        else
        {
            videoTheEnd(false);
        }
    }


    private synchronized void setTotalVideoTime()
    {
        totalTime = System.currentTimeMillis() - firstTime - pausedTime - ((long) (1.0/(double)frameRate)*1000);
        if(totalTime > 0)
            txtTimer.setText(Util.getRecordingTimeFromMillis(totalTime));
    }

    private void releaseResources()
    {
        isRecordingSaved = true;
        try {
            if(videoRecorder != null)
            {
                videoRecorder.stop();
                videoRecorder.release();
            }
        } catch (FrameRecorder.Exception e) {
            e.printStackTrace();
        }

        yuvImage = null;
        videoRecorder = null;
        lastSavedframe = null;
    }

    private void initiateRecording(boolean isActionDown)
    {
        isRecordingStarted = true;
        firstTime = System.currentTimeMillis();

        recording = true;
        totalPauseTime = 0;
        pausedTime = 0;

        txtTimer.setVisibility(View.VISIBLE);
        // Handler to show recoding duration after recording starts
        mHandler.removeCallbacks(mUpdateTimeTask);
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }
}