package com.cv.utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.annotation.Nullable;

import android.widget.ImageView;
import android.widget.Toast;

import com.cv.imgfilter.R;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.UUID;

public class ImageUtil {

    public static Uri getImageContentUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Images.Media._ID }, MediaStore.Images.Media.DATA + "=? ",
                new String[] { filePath }, null);
        Uri uri = null;

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
                Uri baseUri = Uri.parse("content://media/external/images/media");
                uri = Uri.withAppendedPath(baseUri, "" + id);
            }

            cursor.close();
        }

        if (uri == null) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DATA, filePath);
            uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        }

        return uri;
    }


    //save a bitmap object to local image
    public static void saveBitmapToImage(Context context,Bitmap bitmap, @Nullable String cusFileName){
        String savedPath = context.getResources().getString(R.string.save_path);
        String dir = Environment.getExternalStorageDirectory().getAbsolutePath()+savedPath;
        String state = Environment.getExternalStorageState();

        Calendar now = new GregorianCalendar();
        SimpleDateFormat simpleDate = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        String fileRealName = null;
        if(cusFileName==null){
            String fileName = simpleDate.format(now.getTime());
            String fileUUID = UUID.randomUUID().toString();
            fileRealName = fileName+fileUUID;
        }else{
            fileRealName = cusFileName;
        }

        if(state.equals(Environment.MEDIA_MOUNTED)){
            File folder = new File(dir);
            if(!folder.exists()){
                folder.mkdirs();
            }

            try{
                //dir => ../ToMagic/Album
                File newFile = new File(dir+"/"+fileRealName+".jpg");
                FileOutputStream fileOutputStream = new FileOutputStream(newFile);
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
                bitmap.compress(Bitmap.CompressFormat.JPEG,100,bufferedOutputStream);
                bufferedOutputStream.flush();
                bufferedOutputStream.close();
                //Send BroadCast to system
                Uri uri = Uri.fromFile(newFile);
                context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,uri));

            }catch (Exception e){
                e.printStackTrace();
            }
        }else{
            Toast.makeText(context,"The storage is Not Ready!",Toast.LENGTH_SHORT).show();
            return;
        }
    }


    public static File getImageBitmapFile(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        //File outputDir = ApplicationContextSingleton.getContext().getCacheDir(); // context being the Activity pointer
        String savedPath = inContext.getResources().getString(R.string.save_path);

        String dir = Environment.getExternalStorageDirectory().getAbsolutePath()+savedPath;
        String tempPath = dir+"/Temp";
        File folder = new File(tempPath);

        if(!folder.exists()){
            folder.mkdirs();
        }

        String fileName=null;
        String path=null;
        try {
            Calendar now = new GregorianCalendar();
            SimpleDateFormat simpleDate = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
            fileName = simpleDate.format(now.getTime());
            path = tempPath+"/"+fileName+"_Temp_.jpg";
            File newFile = new File(path);
            FileOutputStream fileOutputStream = new FileOutputStream(newFile);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
            inImage.compress(Bitmap.CompressFormat.JPEG,100,bufferedOutputStream);
            bufferedOutputStream.flush();
            bufferedOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        File file = new File(path);
        // String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return file;
        //return Uri.fromFile(file);
    }//temp file


    public  static Bitmap getBitmapFromUri(Context context,Uri uri) {
        InputStream inputStream = null;
        try {
            inputStream = context.getContentResolver().openInputStream(uri);
            Bitmap dstBitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
            return dstBitmap;
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            return null;
        }

    }//getBitmapFromUri



}
