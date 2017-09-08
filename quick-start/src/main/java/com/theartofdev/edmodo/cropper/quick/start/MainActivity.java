// "Therefore those skilled at the unorthodox
// are infinite as heaven and earth,
// inexhaustible as the great rivers.
// When they come to an end,
// they begin again,
// like the days and months;
// they die and are reborn,
// like the four seasons."
//
// - Sun Tsu,
// "The Art of War"

package com.theartofdev.edmodo.cropper.quick.start;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Hashtable;


public class MainActivity extends AppCompatActivity {

    Button btn,sharpen;
    ImageView imageView;
    Uri uri;
    Mat imageMat;

    TextView barcode,symbology;

    private static final boolean TRASNPARENT_IS_BLACK = false;
    private static final double SPACE_BREAKING_POINT = 13.0/30.0;


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i("OpenCV", "OpenCV loaded successfully");
                    imageMat=new Mat();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn = (Button)findViewById(R.id.button2);
        sharpen = (Button)findViewById(R.id.button3);
        imageView = (ImageView)findViewById(R.id.quick_start_cropped_image);
        barcode = (TextView)findViewById(R.id.textView);
        symbology = (TextView)findViewById(R.id.textView2);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readImage();
            }
        });

        sharpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //process();
                //opencv();
                grayscale();
            }
        });
    }

    public void opencv(){
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
        } catch (FileNotFoundException e){
            e.printStackTrace();
        }

        imageMat = new Mat (bitmap.getHeight(), bitmap.getWidth(),
                CvType.CV_8U, new Scalar(4));
        Bitmap myBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(myBitmap, imageMat);

        // now convert to gray
        Mat grayMat = new Mat ( bitmap.getHeight(), bitmap.getWidth(),
                CvType.CV_8U, new Scalar(1));
        Imgproc.cvtColor(imageMat, grayMat, Imgproc.COLOR_RGB2GRAY, 1);

        // get the thresholded image
        Mat thresholdMat = new Mat ( bitmap.getHeight(), bitmap.getWidth(),
                CvType.CV_8U, new Scalar(1));
        Imgproc.threshold(grayMat, thresholdMat , 128, 255, Imgproc.THRESH_BINARY);

        // convert back to bitmap for displaying
        Bitmap resultBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(),
                Bitmap.Config.ARGB_8888);
        thresholdMat.convertTo(thresholdMat, CvType.CV_8UC1);
        Utils.matToBitmap(thresholdMat, resultBitmap);

        imageView.setImageBitmap(resultBitmap);

        Toast.makeText(this, "success", Toast.LENGTH_LONG).show();

    }

    public void grayscale(){
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
        } catch (FileNotFoundException e){
            e.printStackTrace();
        }

        //Bitmap gray = toGrayscale(bitmap);
        Bitmap gray = toMonochrome(bitmap);

        imageView.setImageBitmap(gray);

        Toast.makeText(this, "success", Toast.LENGTH_LONG).show();
    }

    public Bitmap toGrayscale(Bitmap bmpOriginal)
    {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;

    }

    public Bitmap toMonochrome(Bitmap bitmap){

        Bitmap bmpMonochrome = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmpMonochrome);
        ColorMatrix ma = new ColorMatrix();
        ma.setSaturation(0);
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(ma));
        canvas.drawBitmap(bitmap, 0, 0, paint);

        return bmpMonochrome;
    }

    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }


    public void process(){
        Bitmap bMap = null;
        try {
            bMap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Bitmap binarizedImage = bMap.copy(Bitmap.Config.ARGB_8888, true);
        for(int i=0;i<binarizedImage.getWidth();i++) {
            for(int c=0;c<binarizedImage.getHeight();c++) {
                int pixel = binarizedImage.getPixel(i, c);
                if(shouldBeBlack(pixel))
                    binarizedImage.setPixel(i, c, Color.BLACK);
                else
                    binarizedImage.setPixel(i, c, Color.WHITE);
            }
        }
        imageView.setImageBitmap(binarizedImage);

        Toast.makeText(this, "success", Toast.LENGTH_LONG).show();
    }

    private static boolean shouldBeBlack(int pixel) {
        int alpha = Color.alpha(pixel);
        int redValue = Color.red(pixel);
        int blueValue = Color.blue(pixel);
        int greenValue = Color.green(pixel);
        if(alpha == 0x00) //if this pixel is transparent let me use TRASNPARENT_IS_BLACK
            return TRASNPARENT_IS_BLACK;
        // distance from the white extreme
        double distanceFromWhite = Math.sqrt(Math.pow(0xff - redValue, 2) + Math.pow(0xff - blueValue, 2) + Math.pow(0xff - greenValue, 2));
        // distance from the black extreme //this should not be computed and might be as well a function of distanceFromWhite and the whole distance
        double distanceFromBlack = Math.sqrt(Math.pow(0x00 - redValue, 2) + Math.pow(0x00 - blueValue, 2) + Math.pow(0x00 - greenValue, 2));
        // distance between the extremes //this is a constant that should not be computed :p
        double distance = distanceFromBlack + distanceFromWhite;
        // distance between the extremes
        return ((distanceFromWhite/distance)>SPACE_BREAKING_POINT);
    }

    public static Bitmap convertToMutable(Bitmap imgIn) {
        try {
            //this is the file going to use temporally to save the bytes.
            // This file will not be a image, it will store the raw image data.
            File file = new File(Environment.getExternalStorageDirectory() + File.separator + "temp.tmp");

            //Open an RandomAccessFile
            //Make sure you have added uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
            //into AndroidManifest.xml file
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");

            // get the width and height of the source bitmap.
            int width = imgIn.getWidth();
            int height = imgIn.getHeight();
            Bitmap.Config type = imgIn.getConfig();

            //Copy the byte to the file
            //Assume source bitmap loaded using options.inPreferredConfig = Config.ARGB_8888;
            FileChannel channel = randomAccessFile.getChannel();
            MappedByteBuffer map = channel.map(FileChannel.MapMode.READ_WRITE, 0, imgIn.getRowBytes()*height);
            imgIn.copyPixelsToBuffer(map);
            //recycle the source bitmap, this will be no longer used.
            imgIn.recycle();
            System.gc();// try to force the bytes from the imgIn to be released

            //Create a new bitmap to load the bitmap again. Probably the memory will be available.
            imgIn = Bitmap.createBitmap(width, height, type);
            map.position(0);
            //load it back from temporary
            imgIn.copyPixelsFromBuffer(map);
            //close the temporary file and channel , then delete that also
            channel.close();
            randomAccessFile.close();

            // delete the temp file
            file.delete();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return imgIn;
    }


    /**
     * Start pick image activity with chooser.
     */
    public void onSelectImageClick(View view) {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setMultiTouchEnabled(true)
                .start(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // handle result of CropImageActivity
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                uri = result.getUri();
                imageView.setImageURI(uri);

                //Toast.makeText(this, "Cropping successful, Sample: " + result.getSampleSize(), Toast.LENGTH_LONG).show();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, "Cropping failed: " + result.getError(), Toast.LENGTH_LONG).show();
            }
        }
    }

    public void readImage(){
        try {

            //Bitmap bMap = BitmapFactory.decodeResource(this.getResources(),R.drawable.pic /* ANY DUMMY IMAGE WITH BARCODE */);

            //Bitmap bMap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri),null,bmOptions);
           // Bitmap bMap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
            //Bitmap bMap = bitmap;

            Bitmap bMap=((BitmapDrawable)imageView.getDrawable()).getBitmap();

            int[] intArray = new int[bMap.getWidth() * bMap.getHeight()];
            // copy pixel data from the Bitmap into the 'intArray' array
            bMap.getPixels(intArray, 0, bMap.getWidth(), 0, 0, bMap.getWidth(),
                    bMap.getHeight());

            LuminanceSource source = new RGBLuminanceSource(bMap.getWidth(),
                    bMap.getHeight(), intArray);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            Reader reader = new MultiFormatReader();// use this otherwise


            // ChecksumException
            try {
                Hashtable<DecodeHintType, Object> decodeHints = new Hashtable<DecodeHintType, Object>();
                decodeHints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
                decodeHints.put(DecodeHintType.PURE_BARCODE, Boolean.TRUE);

                Result result = reader.decode(bitmap, decodeHints);
                //Toast.makeText(this, result.getText(), Toast.LENGTH_SHORT).show();
                barcode.setText(result.getText());

                BarcodeFormat format = result.getBarcodeFormat();

                //Toast.makeText(this, format.toString(), Toast.LENGTH_SHORT).show();
                symbology.setText(format.toString());


            } catch (NotFoundException e) {
                e.printStackTrace();
            } catch (ChecksumException e) {
                e.printStackTrace();
            } catch (FormatException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }





}
