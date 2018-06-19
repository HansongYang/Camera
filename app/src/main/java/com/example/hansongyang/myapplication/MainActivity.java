package com.example.hansongyang.myapplication;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import android.net.Uri;
import android.graphics.Matrix;
import android.database.Cursor;


public class MainActivity extends AppCompatActivity {

    Uri imgUri;
    ImageView imv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        imv = (ImageView)findViewById(R.id.imageView);
    }

    public void onGet(View v){
        String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        String fname = "p" + System.currentTimeMillis() + ".jpg";
        imgUri = Uri.parse("file://" + dir + "/" + fname);

        Intent it = new Intent("android.media.action.IMAGE_CAPTURE");
        it.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);
        startActivityForResult(it,100);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Activity.RESULT_OK){
            switch (requestCode){
                case 100:
                    Intent it = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, imgUri);
                    sendBroadcast(it);
                    break;
                case 101:
                    imgUri = convertUri(data.getData());
                    break;
            }
            showImg();
        }
        else{
            Toast.makeText(this, "No Image", Toast.LENGTH_LONG).show();
        }
    }

    public Uri convertUri(Uri uri){
        if(uri.toString().substring(0,7).equals("content")){
            String[] colName = {MediaStore.MediaColumns.DATA};
            Cursor cursor = getContentResolver().query(uri, colName, null, null, null);
            cursor.moveToFirst();
            uri = Uri.parse("file://" + cursor.getString(0));
            cursor.close();
        }
        return uri;
    }

    public void onPick(View v){
        Intent it = new Intent (Intent.ACTION_GET_CONTENT);
        it.setType("image/*");
        startActivityForResult(it,101);
    }

    public void showImg(){
        int iw, ih, vw, vh;
        boolean needRotate;

        BitmapFactory.Options option = new BitmapFactory.Options();
        option.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imgUri.getPath(), option);

        iw = option.outWidth;
        ih = option.outHeight;
        vw = imv.getWidth();
        vh = imv.getHeight();

        int scaleFactor = Math.min(iw/vw, ih/vh);
        if(iw < ih){
            needRotate = false;
            scaleFactor = Math.min(iw/vw,ih/vh);
        } else{
            needRotate = true;
            scaleFactor = Math.min(ih/vw, iw/vh);
        }

        option.inJustDecodeBounds = false;
        option.inSampleSize = scaleFactor;

        Bitmap bmp = BitmapFactory.decodeFile(imgUri.getPath(),option);

        if(needRotate){
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            bmp = Bitmap.createBitmap(bmp, 0,0,bmp.getWidth(), bmp.getHeight(), matrix, true);
        }

        imv.setImageBitmap(bmp);
    }

    public void onShare(View view) {
        if(imgUri != null){
            Intent it = new Intent(Intent.ACTION_SEND);
            it.setType("image/*");
            it.putExtra(Intent.EXTRA_STREAM,imgUri);
            startActivity(it);
        }
    }
}

