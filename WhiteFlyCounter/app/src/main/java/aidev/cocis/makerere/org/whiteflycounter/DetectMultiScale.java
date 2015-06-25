package aidev.cocis.makerere.org.whiteflycounter;

import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_8U;
import static org.bytedeco.javacpp.opencv_core.cvClearMemStorage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;


import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_objdetect;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

/**
 * Created by Acellam Guy on 6/12/2015.
 */
public class DetectMultiScale extends Activity {
    private FrameLayout layout;
    private DetectMultiScaleView whiteflyView;
    private Preview mPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Hide the window title.
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Create our Preview view and set it as the content of our activity.
        try {
            layout = new FrameLayout(this);
            whiteflyView = new DetectMultiScaleView(this);
            mPreview = new Preview(this, whiteflyView);
            layout.addView(mPreview);
            layout.addView(whiteflyView);
            setContentView(layout);
        } catch (IOException e) {
            e.printStackTrace();
            new AlertDialog.Builder(this).setMessage(e.getMessage()).create().show();
        }
    }
}

// ----------------------------------------------------------------------

class DetectMultiScaleView extends View implements Camera.PreviewCallback {
    public static final int SUBSAMPLING_FACTOR = 4;

    private opencv_core.IplImage grayImage;
    private opencv_objdetect.CascadeClassifier classifier;
    private opencv_core.CvMemStorage storage;
    private opencv_core.Rect white_flies = new opencv_core.Rect();
    int tt = 0;

    public DetectMultiScaleView(DetectMultiScale context) throws IOException {
        super(context);

        // Load the classifier file from Java resources.

        InputStream is = getResources().openRawResource(R.raw.wf_cascade);
        File classifierFile = new File(context.getCacheDir(), "wf_cascade.xml");
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
        storage = opencv_core.CvMemStorage.create();
    }

    public void onPreviewFrame(final byte[] data, final Camera camera) {
        try {
            Camera.Size size = camera.getParameters().getPreviewSize();
            processImage(data, size.width, size.height);
            camera.addCallbackBuffer(data);
        } catch (RuntimeException e) {
            // The camera has probably just been released, ignore.
        }
    }

    protected void processImage(byte[] data, int width, int height) {
        // First, downsample our image and convert it into a grayscale IplImage
        int f = SUBSAMPLING_FACTOR;
        if (grayImage == null || grayImage.width() != width / f || grayImage.height() != height / f) {
            grayImage = opencv_core.IplImage.create(width / f, height / f, IPL_DEPTH_8U, 1);
        }

        opencv_core.IplImage tmpImage = grayImage;

        int imageWidth = grayImage.width();
        int imageHeight = grayImage.height();
        int dataStride = f * width;
        int imageStride = grayImage.widthStep();
        ByteBuffer imageBuffer = grayImage.getByteBuffer();
        for (int y = 0; y < imageHeight; y++) {
            int dataLine = y * dataStride;
            int imageLine = y * imageStride;
            for (int x = 0; x < imageWidth; x++) {
                imageBuffer.put(imageLine + x, data[dataLine + f * x]);
            }
        }


        opencv_core.Mat imgMat = new opencv_core.Mat(tmpImage, true); //true - required to copy data

        classifier.detectMultiScale(imgMat, white_flies, 1.1, 1, 0, new opencv_core.Size(10, 10), new opencv_core.Size(25, 25));

        postInvalidate();
        cvClearMemStorage(storage);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setTextSize(20);

        String s = tt + "White Flies Detected";
        ;
        float textWidth = paint.measureText(s);
        canvas.drawText(s, (getWidth() - textWidth) / 2, 20, paint);

        if (white_flies != null) {
            paint.setStrokeWidth(2);
            paint.setStyle(Paint.Style.STROKE);

            opencv_core.CvPoint center = new opencv_core.CvPoint();
            center.x(white_flies.x() + (white_flies.width() / 2));
            center.y(white_flies.y() + (white_flies.height() / 2));

            if (grayImage != null) {

                float scaleX = (float) getWidth() / grayImage.width();
                float scaleY = (float) getHeight() / grayImage.height();

                tt = white_flies.capacity();

                for (int j = 0; j < white_flies.capacity(); j++) {
                    opencv_core.CvRect r = new opencv_core.CvRect(white_flies.position(j));
                    int x = r.x(), y = r.y(), w = r.width(), h = r.height();
                    canvas.drawRect(x * scaleX, y * scaleY, (x + w) * scaleX, (y + h) * scaleY, paint);
                }
            }
        }
    }
}

// ----------------------------------------------------------------------

class ImagePreview extends SurfaceView implements SurfaceHolder.Callback {
    SurfaceHolder mHolder;
    Camera mCamera;
    Camera.PreviewCallback previewCallback;
    Context context;
    boolean isPreviewRunning = false;

    ImagePreview(Context context, Camera.PreviewCallback previewCallback) {
        super(context);
        this.context = context;
        if (Build.VERSION.SDK_INT >= 8) this.mCamera.setDisplayOrientation(90);
        this.previewCallback = previewCallback;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, acquire the camera and tell it where
        // to draw.
        mCamera = Camera.open();
        try {

            if (Build.VERSION.SDK_INT >= 8) mCamera.setDisplayOrientation(90);

            mCamera.setPreviewDisplay(holder);
        } catch (IOException exception) {
            mCamera.release();
            mCamera = null;
            // TODO: add more exception handling logic here
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface will be destroyed when we return, so stop the preview.
        // Because the CameraDevice object is not a shared resource, it's very
        // important to release it when the activity is paused.
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }


    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.05;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {


        Camera.Parameters parameters = mCamera.getParameters();

        // Now that the size is known, set up the camera parameters and begin
        // the preview.

        List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
        Camera.Size optimalSize = getOptimalPreviewSize(sizes, w, h);
        parameters.setPreviewSize(optimalSize.width, optimalSize.height);

        mCamera.setParameters(parameters);
        if (previewCallback != null) {
            mCamera.setPreviewCallbackWithBuffer(previewCallback);
            Camera.Size size = parameters.getPreviewSize();
            byte[] data = new byte[size.width * size.height *
                    ImageFormat.getBitsPerPixel(parameters.getPreviewFormat()) / 8];
            mCamera.addCallbackBuffer(data);
        }
        mCamera.startPreview();
    }
}
