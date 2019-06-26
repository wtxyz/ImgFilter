package com.cv.imgfilter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.cv.ViewAdapter.AlbumRecyclerViewHolder;
import com.cv.imgfilter.Activities.ImageProcessActivity;
import com.cv.utils.AlbumModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final int PICK_REQUEST_CODE = 4;
    private Uri imageUri;
    private static final int ALBUM_REFRESH_REQUEST_CODE = 5;
    private Handler mHandler;
    private AlbumRecyclerViewHolder albumRecyclerViewHolder;
    private List<AlbumModel> albumModelList = new ArrayList<>();
    private SwipeRefreshLayout swipeRefreshLayout;
    private String temFileName;
    final int NEW_PHOTO_REQUEST_CODE = 1;
    private final int TAKE_REQUEST_TYPE = 3;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        request_permissions();
        initPhotos(MainActivity.this, albumModelList);
        RecyclerView recyclerView = findViewById(R.id.recycler_editor_view);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        recyclerView.setLayoutManager(layoutManager);

        albumRecyclerViewHolder = AlbumRecyclerViewHolder.newInstance(albumModelList);

        recyclerView.setAdapter(albumRecyclerViewHolder);

        //recycler first button listener
        albumRecyclerViewHolder.setOnItemClickListener((view, position) -> {

            if (position == 0) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_REQUEST_CODE);

            } else {
                //int position = viewHolder.getAdapterPosition();
                AlbumModel albumModel = albumModelList.get(position);
                String imgUrl = albumModel.getmImgUrlId();//获取Url去处理
                Intent intent = new Intent(MainActivity.this, ImageProcessActivity.class);
                intent.putExtra("ImagePath", imgUrl);
                startActivity(intent);
            }

        });

        mHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                if (msg.what == ALBUM_REFRESH_REQUEST_CODE) {
                    swipeRefreshLayout.setRefreshing(false);

                }
            }
        };

        swipeRefreshLayout = findViewById(R.id.image_all_swipe_fresh);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.black));

        //Refresh Album Data
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        albumModelList.clear();
                        initPhotos(MainActivity.this, albumModelList);
                        albumRecyclerViewHolder.notifyDataSetChanged();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });

                //refreshAlbum(albumRecyclerViewHolder);
            }
        });

        //主页界面的浮动(新建)按钮
        FloatingActionButton fab = findViewById(R.id.take_photo_fab_btn);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tempPath = getExternalCacheDir() + "/temp";
                //Calendar now = new GregorianCalendar();
                //SimpleDateFormat simpleDate = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
                //temFileName = simpleDate.format(now.getTime());
                temFileName = "NEW_PHOTO_TEMP";
                File newImage = new File(tempPath, temFileName);
                try {
                    File folder = new File(tempPath);
                    if (!folder.exists()) {
                        folder.mkdir();
                    }
                    if (newImage.exists()) {
                        newImage.delete();
                    } else {
                        newImage.createNewFile();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (Build.VERSION.SDK_INT >= 24) {
                    imageUri = FileProvider.getUriForFile(MainActivity.this, "net.cgcity.www.fileprovider", newImage);
                } else {
                    imageUri = Uri.fromFile(newImage);
                }
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, NEW_PHOTO_REQUEST_CODE);
            }
        });


    }//onCreate


    // 请求多个权限
    private void request_permissions() {
        // 创建一个权限列表，把需要使用而没用授权的的权限存放在这里
        List<String> permissionList = new ArrayList<>();

        // 判断权限是否已经授予，没有就把该权限添加到列表中
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.CAMERA);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        // 如果列表为空，就是全部权限都获取了，不用再次获取了。不为空就去申请权限
        if (!permissionList.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permissionList.toArray(new String[permissionList.size()]), 1002);
        }
    }//request_permissions()

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1002:
                // 1002请求码对应的是申请多个权限
                if (grantResults.length > 0) {
                    // 因为是多个权限，所以需要一个循环获取每个权限的获取情况
                    for (int i = 0; i < grantResults.length; i++) {

                        if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                            //refuse
                        }
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View v) {

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //imagePicker.handleActivityResult(resultCode,resultCode,data);

        if (resultCode == 0) {
            //nothing
        } else {
            switch (requestCode) {
                case NEW_PHOTO_REQUEST_CODE:
                    try {
                        String tempPath = getExternalCacheDir() + "/temp";
                        Bitmap bitmap = BitmapFactory.decodeStream(MainActivity.this.getContentResolver().openInputStream(imageUri));
                        Intent intentToStyleTransfer = new Intent(MainActivity.this, ImageProcessActivity.class);
                        String ImagePath = tempPath + "/" + temFileName;
                        intentToStyleTransfer.putExtra("Type", 0);
                        //intentToStyleTransfer.putExtra("imgUrl","NEW_PHOTO");
                        // intentToStyleTransfer.putExtra("imageBitmap",bitmap);
                        intentToStyleTransfer.putExtra("ImagePath", ImagePath);
                        startActivity(intentToStyleTransfer);

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    break;
                case PICK_REQUEST_CODE:
                    Log.d("LOOKUP", "Here!!");
                    Uri uri = data.getData();
                    Intent intentToStyleTransfer = new Intent(MainActivity.this, ImageProcessActivity.class);
                    intentToStyleTransfer.putExtra("Type", TAKE_REQUEST_TYPE);
                    assert uri != null;
                    intentToStyleTransfer.putExtra("ImagePath", uri.toString());
                    startActivity(intentToStyleTransfer);
                    break;
                default:
                    break;
            }//switch
        }//else
    }//onAcRE

    // 获取用户的照片
    public void initPhotos(Context context, List<AlbumModel> albumModelList) {
        AlbumModel albumModel0 = new AlbumModel("FirstBtn", null);
        albumModelList.add(albumModel0);
        Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projImage = {
                MediaStore.Images.Media._ID
                , MediaStore.Images.Media.DATA
                , MediaStore.Images.Media.SIZE
                , MediaStore.Images.Media.DISPLAY_NAME};
        Cursor mCursor = context.getContentResolver().query(mImageUri,
                projImage,
                MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=?",
                new String[]{"image/jpeg", "image/png"},
                MediaStore.Images.Media.DATE_MODIFIED + " desc");
        assert mCursor != null;
        while (mCursor.moveToNext()) {
            String picPath = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DATA));
            String picName = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
            AlbumModel p = new AlbumModel(picName, picPath);//单个图片的信息
            albumModelList.add(p);
        }
        mCursor.close();
    }
}//class
