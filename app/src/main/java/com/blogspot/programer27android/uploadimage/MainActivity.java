package com.blogspot.programer27android.uploadimage;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.kosalgeek.android.photoutil.CameraPhoto;
import com.kosalgeek.android.photoutil.GalleryPhoto;
import com.kosalgeek.android.photoutil.ImageBase64;
import com.kosalgeek.android.photoutil.ImageLoader;
import com.kosalgeek.genasync12.AsyncResponse;
import com.kosalgeek.genasync12.EachExceptionsHandler;
import com.kosalgeek.genasync12.PostResponseAsyncTask;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private final String TAG= this.getClass().getName();
    ImageView kamera,galeri,upload,gambar;
    CameraPhoto camera;
    GalleryPhoto galleryPhoto;
    final int GALERY_REQUEST=33221;
    final int CAMERA_REQUEST=13323;
    String selectedPhoto;
    EditText edtIP;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        edtIP= (EditText) findViewById(R.id.editIP);
        camera = new CameraPhoto(getApplicationContext());
        galleryPhoto=new GalleryPhoto(this);

        kamera= (ImageView) findViewById(R.id.kamera);
        galeri= (ImageView) findViewById(R.id.galery);
        galeri.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              startActivityForResult(galleryPhoto.openGalleryIntent(),GALERY_REQUEST);
            }
        });
        upload= (ImageView) findViewById(R.id.upLoad);
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedPhoto== null || selectedPhoto.equals("") ){
                    Toast.makeText(MainActivity.this, "tidak ada gambar", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    Bitmap b = ImageLoader.init().from(selectedPhoto).requestSize(1024,1024).getBitmap();
                    String encodeImage= ImageBase64.encode(b);
                    Log.d(TAG,encodeImage);
                    HashMap<String,String>postData=new HashMap<String, String>();
                    postData.put("image",encodeImage);
                    PostResponseAsyncTask tsk=new PostResponseAsyncTask(MainActivity.this, postData, new AsyncResponse() {
                        @Override
                        public void processFinish(String s) {
//                            contains cek di upload,php
                            if (s.contains("uploaded_success")){
                                Toast.makeText(MainActivity.this, "Image succes upload", Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(MainActivity.this, "image error upload", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    String ip = edtIP.getText().toString();
                    tsk.execute("https://" +ip + "/file/upload.php/");
                    tsk.setEachExceptionsHandler(new EachExceptionsHandler() {
                        @Override
                        public void handleIOException(IOException e) {
                            Toast.makeText(MainActivity.this, "server mati", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void handleMalformedURLException(MalformedURLException e) {
                            Toast.makeText(MainActivity.this, "Urlnya salah", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void handleProtocolException(ProtocolException e) {
                            Toast.makeText(MainActivity.this, "protocol error", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void handleUnsupportedEncodingException(UnsupportedEncodingException e) {
                            Toast.makeText(MainActivity.this, "Encode Error", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (FileNotFoundException e) {
                    Toast.makeText(MainActivity.this, "encode wrong", Toast.LENGTH_SHORT).show();
                }

            }
        });
        gambar= (ImageView) findViewById(R.id.image);

        //akses kamera
        kamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    startActivityForResult(camera.takePhotoIntent(),CAMERA_REQUEST);
                    camera.addToGallery();
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "something wrong kamera", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
//tampilan image di custom
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK){
            if (requestCode == CAMERA_REQUEST){
                String photoPath=camera.getPhotoPath();
                selectedPhoto=photoPath;
                Bitmap btm=null;
                try {
                    btm= ImageLoader.init().from(photoPath).requestSize(512,512).getBitmap();
                    gambar.setImageBitmap(getrotate(btm, 90));

                } catch (FileNotFoundException e) {
                    Toast.makeText(getApplicationContext(), "something wrong photo", Toast.LENGTH_SHORT).show();
                }
                Log.d(TAG, photoPath);
            }
        }else if (requestCode==GALERY_REQUEST){
            Uri uri=data.getData();
            galleryPhoto.setPhotoUri(uri);
            String photoPathg=galleryPhoto.getPath();
            selectedPhoto=photoPathg;
            try {
                Bitmap bt= ImageLoader.init().from(photoPathg).requestSize(512,512).getBitmap();
                gambar.setImageBitmap(bt);
            } catch (FileNotFoundException e) {
                Toast.makeText(getApplicationContext(), "something wrong galery", Toast.LENGTH_SHORT).show();
            }Log.d(TAG, photoPathg);
        }
    }
    //rotasi
    private Bitmap getrotate(Bitmap source,float engle){
        Matrix mt=new Matrix();
        mt.postRotate(engle);
        Bitmap bibmapl =Bitmap.createBitmap(source, 0, 0, source.getWidth(),source.getHeight(),mt,true);
        return bibmapl;
    }
}
