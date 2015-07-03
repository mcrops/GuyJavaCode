package aidev.cocis.makerere.org.whiteflycounter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.FrameLayout;

import org.bytedeco.javacpp.*;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_highgui.cvLoadImage;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_highgui.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.graphics.Matrix;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by User on 6/30/2015.
 */
public class MainWhiteFly extends Activity implements SurfaceHolder.Callback {
    TextView testView;

    Camera camera;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    Button snapButton;

    Camera.PictureCallback rawCallback;
    Camera.ShutterCallback shutterCallback;
    Camera.PictureCallback jpegCallback;
    private FrameLayout layout;


    ProgressDialog barProgressDialog;
    Handler updateBarHandler;

    private opencv_core.IplImage grayImage;
    private opencv_objdetect.CascadeClassifier classifier;
    private opencv_core.CvMemStorage storage;
    private opencv_core.Rect white_flies = new opencv_core.Rect();
    int tt = 0;
    File mFolder = new File(Environment.getExternalStorageDirectory() + File.separator+"whiteflycounter");


    protected void processImage(String rgbImagePath) {
        // First, downsample our image and convert it into a grayscale IplImage


        IplImage originalImage = cvLoadImage(rgbImagePath);

        // create a new image of the same size as the original one.
        grayImage = IplImage.create(originalImage.width(),
                originalImage.height(), IPL_DEPTH_8U, 1);

        // We convert the original image to grayscale.
        cvCvtColor(originalImage, grayImage, CV_BGR2GRAY);
        // process

        // the image needs to be a big bigger than video standard to catch
        // white fly features

       /* IplImage grayImageResized = IplImage.create(640, 480, IPL_DEPTH_8U,
                1);

        cvResize(grayImage, grayImageResized);
        grayImage = grayImageResized;*/



        opencv_core.Mat imgMat = new opencv_core.Mat(grayImage, true); //true - required to copy data

        barProgressDialog.incrementProgressBy(2);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                barProgressDialog.setMessage("...Detecting Whiteflies...");
            }
        });


        classifier.detectMultiScale(imgMat, white_flies, 1.1, 1, 0, new opencv_core.Size(10, 10), new opencv_core.Size(25, 25));


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

            CvPoint center =  new CvPoint();

            center.x(r.x()+10); center.y(r.y()+12);

            cvCircle(originalImage, center, 15, cvScalar(0, 0, 255, 0), 2, 8, 0);


        }
        barProgressDialog.incrementProgressBy(4);
        File imgFile = new File( String.format( mFolder.getAbsolutePath()+ File.separator +"%d_%d.jpg", System.currentTimeMillis(),tt));

        cvSaveImage(
                imgFile.getAbsolutePath(),
                originalImage);

        cvClearMemStorage(storage);

        barProgressDialog.dismiss();

        Intent analysisIntent = new Intent(this, ShowAnalysis.class);
        analysisIntent.putExtra("imagePath", imgFile.getAbsoluteFile() + "");
        analysisIntent.putExtra("count",tt+"");
        startActivity(analysisIntent);
    }

    /** Called when the activity is first created. */
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

           Log.e("DetectFrames:Read Casacade file","Problem reading cascade file."+ex.getStackTrace());
       }
        storage = opencv_core.CvMemStorage.create();



        setContentView(R.layout.activity_main);


        updateBarHandler = new Handler();

        barProgressDialog = new ProgressDialog(MainWhiteFly.this);

        barProgressDialog.setTitle("Analysing Image ...");
        barProgressDialog.setMessage("White Fly Detection in progress ...");
        barProgressDialog.setProgressStyle(barProgressDialog.STYLE_HORIZONTAL);
        barProgressDialog.setProgress(0);
        barProgressDialog.setMax(20);

        snapButton = (Button)findViewById(R.id.snapButton);
        snapButton.setVisibility(View.INVISIBLE);

        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        surfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(snapButton.getVisibility()==View.VISIBLE){
                    snapButton.setVisibility(View.INVISIBLE);
                }else {
                    snapButton.setVisibility(View.VISIBLE);
                }
            }
        });

        surfaceHolder = surfaceView.getHolder();

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        surfaceHolder.addCallback(this);

        // deprecated setting, but required on Android versions prior to 3.0
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        jpegCallback = new Camera.PictureCallback() {
            public void onPictureTaken(byte[] data, Camera camera) {

                Log.d("Log", "onPictureTaken - wrote bytes: " + data.length);

                final byte[] picdata = data;

                    barProgressDialog.show();


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


                                FileOutputStream outStream = null;
                                // time consuming task...
                                File imgFile = new File( String.format( mFolder.getAbsolutePath()+ File.separator +"%d.jpg", System.currentTimeMillis()));
                                if (!mFolder.exists()) {
                                    mFolder.mkdir();
                                }
                                outStream = new FileOutputStream(imgFile);
                                outStream.write(picdata);
                                outStream.close();

                                Bitmap bmp = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                                //Fix rotation issues
                                barProgressDialog.incrementProgressBy(2);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                        barProgressDialog.setMessage("...Fixing Rotation.");
                                    }
                                });
                                Matrix matrix = new Matrix();
                                matrix.postRotate(90);
                                bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);

                                FileOutputStream fos2 = new FileOutputStream(imgFile);
                                bmp.compress(Bitmap.CompressFormat.JPEG, 90, fos2);
                                fos2.close();

                                barProgressDialog.incrementProgressBy(2);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                        barProgressDialog.setMessage("Analysis Started..");
                                    }
                                });

                                processImage(imgFile.getAbsolutePath());

                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                            barProgressDialog.dismiss();
                        }
                        }
                    }).start();



                Toast.makeText(getApplicationContext(), "Picture Saved",Toast.LENGTH_SHORT).show();
                refreshCamera();
            }
        };
    }

    public void captureImage(View v) throws IOException {
        //take the picture
        camera.takePicture(null, null, jpegCallback);
    }

    public void refreshCamera() {
        if (surfaceHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            camera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here
        // start preview with new settings
        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (Exception e) {

        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // Now that the size is known, set up the camera parameters and begin
        // the preview.
        refreshCamera();
    }

    public void surfaceCreated(SurfaceHolder holder) {
        try {
            // open the camera
            camera = Camera.open();
            if (Build.VERSION.SDK_INT >= 8) this.camera.setDisplayOrientation(90);

        } catch (RuntimeException e) {
            // check for exceptions
            System.err.println(e);
            return;
        }
        Camera.Parameters param;
        param = camera.getParameters();

        // modify parameter
        param.setPreviewSize(352, 288);
        camera.setParameters(param);
        try {
            // The Surface has been created, now tell the camera where to draw
            // the preview.
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (Exception e) {
            // check for exceptions
            System.err.println(e);
            return;
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // stop preview and release camera
        camera.stopPreview();
        camera.release();
        camera = null;
    }


}