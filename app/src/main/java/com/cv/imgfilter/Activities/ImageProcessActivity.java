package com.cv.imgfilter.Activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.annotation.Nullable;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import androidx.core.content.ContextCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.cv.Entity.ColorLevelsEntity;
import com.cv.Entity.FilterModel;
import com.cv.ViewAdapter.ColorListViewAdapter;
import com.cv.ViewAdapter.FilterListAdapter;
import com.cv.effect.NativeFilter;
import com.cv.imgfilter.R;
import com.cv.utils.ImageUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ja.burhanrashid52.photoeditor.OnSaveBitmap;
import ja.burhanrashid52.photoeditor.PhotoEditor;
import ja.burhanrashid52.photoeditor.PhotoEditorView;
import ja.burhanrashid52.photoeditor.PhotoFilter;

public class ImageProcessActivity extends AppCompatActivity implements View.OnClickListener
        , AdapterView.OnItemSelectedListener, SeekBar.OnSeekBarChangeListener {

    private PhotoEditor photoEditor;
    private Spinner colorLevelsChannelSpinner;
    private boolean filterListIsShow;
    private View brushOptionsPopupView;
    private View colorLevelsTweakPopupView;
    private final int CAMERA_REQUEST_CODE = 1;
    private final int TAKE_REQUEST_CODE = 2;
    private PopupWindow brushOptionsPopupWindow;
    private PopupWindow colorLevelsTweakPopupWindow;

    private TextView promptTextView;
    private PhotoEditorView photoEditorView;

    private boolean imageIsChanged = false;
    private int brushSizeValue = 10;
    private int brushOpacityValue = 100;
    private int eraserSizeValue = 10;
    private LinearLayout linearLayoutBtnGroup;
    private LinearLayout linearLayoutRecy;
    private List<ColorLevelsEntity> colorLevelsEntityList;//色阶对应的seekBar调整
    private Bitmap srcBitmap;
    private Bitmap srcBackUpBitmap;
    private NativeFilter nativeFilter;


    //色阶调整对应的SeekBar
    private SeekBar colorLevelsTweakShadowSeekBar;
    private SeekBar colorLevelsTweakMidtonesSeekBar;
    private SeekBar colorLevelsTweakHighlightSeekBar;
    private SeekBar colorLevelsTweakOutputShadowSeekBar;
    private SeekBar colorLevelsTweakOutputHighlightSeekBar;

    private int colorChannel = 0;//默认RGB通道


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_process);

        Toolbar toolbar = findViewById(R.id.single_process_image_toolbar);
        Drawable drawable = ContextCompat.getDrawable(ImageProcessActivity.this, R.drawable.ic_more);
        toolbar.setOverflowIcon(drawable);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        nativeFilter = new NativeFilter();//使用C++处理
        //画刷弹窗
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        brushOptionsPopupView = inflater.inflate(R.layout.popup_beautify_brush_chose, null);
        //色阶弹窗
        LayoutInflater inflater1 = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        colorLevelsTweakPopupView = inflater1.inflate(R.layout.popup_colorl_levels_tweak, null);
        colorLevelsEntityList = new ArrayList<>();//存储每个颜色通道的黑白点值

        initBrushAdapter(this);
        initFilterAdapter(this);
        initColorLevelsPopupWindow();//色阶弹窗初始化


        colorLevelsTweakShadowSeekBar = colorLevelsTweakPopupView.findViewById(R.id.color_levels_shadow_seekbar);
        colorLevelsTweakShadowSeekBar.setMax(255);//0-255
        colorLevelsTweakShadowSeekBar.setOnSeekBarChangeListener(this);

        colorLevelsTweakHighlightSeekBar = colorLevelsTweakPopupView.findViewById(R.id.color_levels_highlight_seekbar);
        colorLevelsTweakHighlightSeekBar.setMax(255);
        colorLevelsTweakHighlightSeekBar.setOnSeekBarChangeListener(this);

        colorLevelsTweakMidtonesSeekBar = colorLevelsTweakPopupView.findViewById(R.id.color_levels_midtones_seekbar);
        colorLevelsTweakMidtonesSeekBar.setMax(200);
        colorLevelsTweakMidtonesSeekBar.setOnSeekBarChangeListener(this);
        ;

        colorLevelsTweakOutputShadowSeekBar = colorLevelsTweakPopupView.findViewById(R.id.color_levels_output_shadow_seekbar);
        colorLevelsTweakOutputShadowSeekBar.setMax(255);
        colorLevelsTweakOutputShadowSeekBar.setOnSeekBarChangeListener(this);

        colorLevelsTweakOutputHighlightSeekBar = colorLevelsTweakPopupView.findViewById(R.id.color_levels_output_highlight_seekbar);
        colorLevelsTweakOutputHighlightSeekBar.setMax(255);
        colorLevelsTweakOutputHighlightSeekBar.setOnSeekBarChangeListener(this);

        //色阶弹窗

        colorLevelsChannelSpinner = colorLevelsTweakPopupView.findViewById(R.id.color_levels_channel_spinner);
        colorLevelsChannelSpinner.setOnItemSelectedListener(this);

        filterListIsShow = false;

        Intent intent = getIntent();//获取发过来的Intent

        String imgUrl = intent.getStringExtra("ImagePath");//get Image Real Path

        Uri imgUri = Uri.parse(intent.getStringExtra("ImagePath"));

        //imgUri -> srcBitmap
        srcBitmap = BitmapFactory.decodeFile(imgUrl);
        //srcBitmap = ImageUtil.getBitmapFromUri(this,imgUri);
        srcBackUpBitmap = srcBitmap;

        photoEditorView = findViewById(R.id.photoEditorView);

        photoEditorView.getSource().setImageURI(imgUri);
        photoEditor = new PhotoEditor.Builder(this, photoEditorView)
                .setPinchTextScalable(true)
                .build();

        Button brushBtn = findViewById(R.id.beautify_brush_btn);
        brushBtn.setOnClickListener(this);

        Button clearAllBtn = findViewById(R.id.beautify_clear_all_btn);
        clearAllBtn.setOnClickListener(this);

        Button filterBtn = findViewById(R.id.beautify_filter_btn);
        filterBtn.setOnClickListener(this);

        Button textFontBtn = findViewById(R.id.beautify_font_btn);
        textFontBtn.setOnClickListener(this);

        Button undoBtn = findViewById(R.id.beautify_undo_btn);
        undoBtn.setOnClickListener(this);

        Button redoBtn = findViewById(R.id.beautify_redo_btn);
        redoBtn.setOnClickListener(this);

        Button takePhotoBtn = findViewById(R.id.beautify_take_photo_btn);
        takePhotoBtn.setOnClickListener(this);

        Button openAlbumBtn = findViewById(R.id.beautify_open_album_btn);
        openAlbumBtn.setOnClickListener(this);

        Button saveBtn = findViewById(R.id.beautify_save_btn);
        saveBtn.setOnClickListener(this);

        Button exitBtn = findViewById(R.id.exit_beautify_or_filter_btn);
        exitBtn.setOnClickListener(this);

        Button shareBtn = findViewById(R.id.beautify_share_btn);
        shareBtn.setOnClickListener(this);

        TextView restoreBtn = findViewById(R.id.image_process_restore_btn);
        restoreBtn.setOnClickListener(this);


        promptTextView = findViewById(R.id.beautify_propmt_textview);
        promptTextView.setText("");

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.beautify_brush_btn:
                openBrushPopupWindow(v);
                break;

            case R.id.beautify_font_btn:
                addTextToImage();
                break;

            case R.id.beautify_clear_all_btn:
                photoEditor.clearAllViews();
                break;

            case R.id.beautify_filter_btn:
                openFilterList();
                break;
//
//            case R.id.beautify_emjoi_btn:
//
//
//                break;
            case R.id.beautify_share_btn:
                photoEditor.saveAsBitmap(new OnSaveBitmap() {

                    @Override
                    public void onBitmapReady(Bitmap saveBitmap) {
                        File file = ImageUtil.getImageBitmapFile(ImageProcessActivity.this, saveBitmap);
                        Uri shareUri = ImageUtil.getImageContentUri(ImageProcessActivity.this, file);
                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("image/*");
                        shareIntent.putExtra(Intent.EXTRA_STREAM, shareUri);
                        startActivity(shareIntent);
                    }

                    @Override
                    public void onFailure(Exception e) {

                    }

                });
                break;
            case R.id.image_process_restore_btn:
                photoEditorView.getSource().setImageBitmap(srcBackUpBitmap);
                break;
            case R.id.beautify_undo_btn:
                photoEditor.undo();
                break;
            case R.id.beautify_redo_btn:
                photoEditor.redo();
                break;
            case R.id.beautify_take_photo_btn:
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
                break;
            case R.id.beautify_open_album_btn:
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), TAKE_REQUEST_CODE);
                break;
            case R.id.beautify_save_btn:
                photoEditor.saveAsBitmap(new OnSaveBitmap() {
                    @Override
                    public void onBitmapReady(Bitmap saveBitmap) {
                        ImageUtil.saveBitmapToImage(ImageProcessActivity.this, saveBitmap, null);
                        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), getResources().getString(R.string.snack_save_prompt), Snackbar.LENGTH_LONG);
                        View mSnackBar = snackbar.getView();
                        mSnackBar.setBackground(ImageProcessActivity.this.getDrawable(R.color.half_white_color));
                        TextView textView = mSnackBar.findViewById(com.google.android.material.R.id.snackbar_text);
                        textView.setTextColor(getColor(R.color.half_black_color));
                        snackbar.show();
                    }

                    @Override
                    public void onFailure(Exception e) {

                    }
                });

                break;

            case R.id.exit_beautify_or_filter_btn:
                if (filterListIsShow) {
                    filterListIsShow = false;
                    linearLayoutBtnGroup.setVisibility(View.VISIBLE);
                    linearLayoutRecy.setVisibility(View.INVISIBLE);
                    promptTextView.setText("");
                } else {
                    android.app.AlertDialog.Builder builder;
                    final android.app.AlertDialog dialog;
                    builder = new android.app.AlertDialog.Builder(this);
                    builder.setCancelable(true);
                    builder.setTitle("确认？");
                    builder.setMessage("确定要退出吗？");
                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
                    dialog = builder.create();
                    final Window window = dialog.getWindow();
                    assert window != null;
                    window.setBackgroundDrawable(ImageProcessActivity.this.getDrawable(R.drawable.half_white_background));
                    dialog.show();
                }
                break;
            default:
                break;
        }
    }//onClick


    //色阶处理

    private void initColorLevelsPopupWindow() {
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        colorLevelsTweakPopupWindow = new PopupWindow(colorLevelsTweakPopupView, width, height, false);
        colorLevelsTweakPopupWindow.setOutsideTouchable(true);
        //初始化通道数据
        for (int i = 0; i < 4; i++) {
            ColorLevelsEntity entity = new ColorLevelsEntity(i, 0, 100, 255, 0, 255);
            colorLevelsEntityList.add(entity);
        }

    }//initColorLevelsPopupWindow

    private void openColorLevelsPopupWindow(final View view) {
        colorLevelsTweakPopupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);

        colorLevelsTweakPopupView.setOnTouchListener((v, event) -> true);


    }//openColorLevelsPopupWindow


    //色阶调整


    //Brush
    private void openBrushPopupWindow(final View view) {

        //设置可以画刷
        photoEditor.setBrushDrawingMode(true);
        brushOptionsPopupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);
        // dismiss the popup window when touched
//        brushOptionsPopupWindow.setBackgroundDrawable(new BitmapDrawable(this.getResources()));
//        brushOptionsPopupWindow.setFocusable(true);
//        brushOptionsPopupWindow.setOutsideTouchable(true);

        brushOptionsPopupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //  brushOptionsPopupWindow.dismiss();
                return true;
            }
        });

        SeekBar brushSizeSeekbar = brushOptionsPopupView.findViewById(R.id.brush_size_seekbar);
        brushSizeSeekbar.setMax(100);
        brushSizeSeekbar.setProgress(brushSizeValue);
        photoEditor.setBrushSize(getConvertedValue(brushSizeValue));
        brushSizeSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                photoEditor.setBrushSize(getConvertedValue(progress));
                brushSizeValue = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        SeekBar brushOpacitySeekbar = brushOptionsPopupView.findViewById(R.id.brush_opacity_seekbar);
        brushOpacitySeekbar.setMax(100);
        brushOpacitySeekbar.setProgress(brushOpacityValue);
        photoEditor.setOpacity(brushOpacityValue);
        brushOpacitySeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                photoEditor.setOpacity(progress);
                brushOpacityValue = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }//openBrushPopupWindow

    private void addTextToImage() {

        final List<Integer> colorList = new LinkedList<>();
        colorList.add(0, Color.WHITE);
        colorList.add(1, Color.rgb(245, 245, 245));
        colorList.add(2, Color.rgb(250, 228, 196));
        colorList.add(3, Color.rgb(255, 218, 185));
        colorList.add(4, Color.rgb(255, 222, 173));
        colorList.add(5, Color.rgb(240, 255, 240));
        colorList.add(6, Color.rgb(12, 52, 99));
        colorList.add(7, Color.rgb(255, 240, 245));
        colorList.add(8, Color.rgb(100, 149, 237));
        colorList.add(9, Color.rgb(72, 61, 139));
        colorList.add(10, Color.rgb(123, 104, 238));
        colorList.add(11, Color.BLUE);
        colorList.add(12, Color.GREEN);
        colorList.add(13, Color.rgb(173, 255, 47));
        colorList.add(14, Color.YELLOW);
        colorList.add(15, Color.rgb(255, 99, 71));
        colorList.add(16, Color.rgb(255, 0, 255));
        colorList.add(17, Color.RED);
        colorList.add(18, Color.rgb(139, 137, 137));
        colorList.add(19, Color.BLACK);

        ColorListViewAdapter addTextColorListViewAdapter = new ColorListViewAdapter(colorList);

        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_beautify_add_text, null);
        final TextInputEditText inputEditText = view.findViewById(R.id.add_text_input);
        final ImageView imageView = view.findViewById(R.id.add_text_color_chosen);
        RecyclerView recyclerView = view.findViewById(R.id.add_text_color_recy);
        LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(addTextColorListViewAdapter);

        final int[] colorChose = new int[1];
        colorChose[0] = Color.BLACK;
        addTextColorListViewAdapter.setOnItemClickListener(new ColorListViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                colorChose[0] = colorList.get(position);
                imageView.setBackgroundColor(colorChose[0]);

            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        AlertDialog dialog;
        builder.setMessage("请输入你要贴图的文字");
        builder.setView(view);
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (inputEditText.getText() != null) {
                    String inputString = inputEditText.getText().toString();
                    if (inputString == null || inputString.equals("")) {
                        //nothing
                    } else {
                        photoEditor.addText(inputString, colorChose[0]);
                    }
                }
            }
        });
        dialog = builder.create();
        final Window window = dialog.getWindow();
        assert window != null;
        window.setBackgroundDrawable(ImageProcessActivity.this.getDrawable(R.drawable.half_white_background));
        dialog.show();
    }

    //filter list show
    private void openFilterList() {
        promptTextView.setText("滤镜");
        filterListIsShow = true;
        linearLayoutBtnGroup.setVisibility(View.INVISIBLE);
        linearLayoutRecy.setVisibility(View.VISIBLE);
    }

    //filter init
    private void initFilterAdapter(final Context context) {

        final List<FilterModel> filterModelList = new LinkedList<>();

        HashMap<String, PhotoFilter> filterHashMap = new LinkedHashMap<>();
        filterHashMap.put("原图", PhotoFilter.NONE);
        filterHashMap.put("自动修复", PhotoFilter.AUTO_FIX);
        filterHashMap.put("粗糙", PhotoFilter.GRAIN);
        filterHashMap.put("黑白调", PhotoFilter.BLACK_WHITE);
        filterHashMap.put("增亮", PhotoFilter.BRIGHTNESS);
        filterHashMap.put("艳丽", PhotoFilter.CONTRAST);
        filterHashMap.put("复古", PhotoFilter.CROSS_PROCESS);
        filterHashMap.put("纪录片", PhotoFilter.DOCUMENTARY);
        filterHashMap.put("反蓝色调", PhotoFilter.DUE_TONE);
        filterHashMap.put("打灯", PhotoFilter.FILL_LIGHT);
        filterHashMap.put("白点", PhotoFilter.FISH_EYE);
        filterHashMap.put("垂直翻转", PhotoFilter.FLIP_VERTICAL);
        filterHashMap.put("水平翻转", PhotoFilter.FLIP_HORIZONTAL);
        filterHashMap.put("灰度化", PhotoFilter.GRAY_SCALE);
        filterHashMap.put("腐蚀", PhotoFilter.LOMISH);
        filterHashMap.put("负片", PhotoFilter.NEGATIVE);
        filterHashMap.put("色调分类", PhotoFilter.POSTERIZE);
        filterHashMap.put("旋转", PhotoFilter.ROTATE);
        filterHashMap.put("浓郁", PhotoFilter.SATURATE);
        filterHashMap.put("深褐", PhotoFilter.SEPIA);
        filterHashMap.put("锐化", PhotoFilter.SHARPEN);
        filterHashMap.put("温馨", PhotoFilter.TEMPERATURE);
        filterHashMap.put("紫纱", PhotoFilter.TINT);
        filterHashMap.put("暗角", PhotoFilter.VIGNETTE);

        AssetManager assetManager = getAssets();

        int i = 0;
        for (final Map.Entry<String, PhotoFilter> entry : filterHashMap.entrySet()) {
            try {
                InputStream stream = assetManager.open(entry.getValue() + ".jpg");
                Bitmap bitmap = BitmapFactory.decodeStream(stream);
                FilterModel model = new FilterModel(entry.getValue(), bitmap, entry.getKey(), entry.getValue().toString());
                filterModelList.add(i, model);
                i++;
            } catch (IOException e) {
                e.printStackTrace();
            }

        }//for

        FilterListAdapter adapter = new FilterListAdapter(filterModelList);

        RecyclerView recyclerView = findViewById(R.id.beautify_filter_effect_recycler);

        linearLayoutBtnGroup = findViewById(R.id.bottom_linearLayout_beautify);
        linearLayoutRecy = findViewById(R.id.bottom_linearLayout_beautify_with_recy);

        filterListIsShow = false;

        LinearLayoutManager manager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new FilterListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                FilterModel model = filterModelList.get(position);
                photoEditor.setFilterEffect(model.getPhotoFilter());
            }
        });//click

    }

    private void initBrushAdapter(Context context) {

        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;
        brushOptionsPopupWindow = new PopupWindow(brushOptionsPopupView, width, height, focusable);
        final List<Integer> colorList = new LinkedList<>();
        colorList.add(0, Color.WHITE);
        colorList.add(1, Color.rgb(245, 245, 245));
        colorList.add(2, Color.rgb(250, 228, 196));
        colorList.add(3, Color.rgb(255, 218, 185));
        colorList.add(4, Color.rgb(255, 222, 173));
        colorList.add(5, Color.rgb(240, 255, 240));
        colorList.add(6, Color.rgb(12, 52, 99));
        colorList.add(7, Color.rgb(255, 240, 245));
        colorList.add(8, Color.rgb(100, 149, 237));
        colorList.add(9, Color.rgb(72, 61, 139));
        colorList.add(10, Color.rgb(123, 104, 238));
        colorList.add(11, Color.BLUE);
        colorList.add(12, Color.GREEN);
        colorList.add(13, Color.rgb(173, 255, 47));
        colorList.add(14, Color.YELLOW);
        colorList.add(15, Color.rgb(255, 99, 71));
        colorList.add(16, Color.rgb(255, 0, 255));
        colorList.add(17, Color.RED);
        colorList.add(18, Color.rgb(139, 137, 137));
        colorList.add(19, Color.BLACK);

        ColorListViewAdapter brushColorListViewAdapter = new ColorListViewAdapter(colorList);

        RecyclerView recyclerView = brushOptionsPopupView.findViewById(R.id.brush_color_recycler);
        LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(brushColorListViewAdapter);

        brushColorListViewAdapter.setOnItemClickListener(new ColorListViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                photoEditor.setBrushColor(colorList.get(position));
                photoEditor.setBrushSize(getConvertedValue(brushSizeValue));
                photoEditor.setOpacity(brushOpacityValue);
                brushOptionsPopupWindow.dismiss();
            }
        });


    }//init

    public float getConvertedValue(int intVal) {
        float floatVal = 0.0f;
        floatVal = .5f * intVal;
        return floatVal;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 0) {
            //nothing
        } else {
            switch (requestCode) {
                case CAMERA_REQUEST_CODE:
                    photoEditor.clearAllViews();
                    Bitmap photo = (Bitmap) data.getExtras().get("data");
                    photoEditorView.getSource().setImageBitmap(photo);
                    break;
                case TAKE_REQUEST_CODE:
                    try {
                        photoEditor.clearAllViews();
                        Uri uri = data.getData();
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                        photoEditorView.getSource().setImageBitmap(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    break;
            }
        }
    }//onActivity

    //Spinner
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //获取position  0=> RGB ,1 => R,2 => G,3 => B
        switch (position) {
            case 0:
                setColorLevelsSeekBarValue(0);
                break;
            case 1:
                setColorLevelsSeekBarValue(1);
                break;
            case 2:
                setColorLevelsSeekBarValue(2);
                break;
            case 3:
                setColorLevelsSeekBarValue(3);
                break;
            default:
                break;
        }//switch

    }//onItemSelected

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
    //Spinner

    private void setColorLevelsSeekBarValue(int colorChannel) {
        this.colorChannel = colorChannel;
        ColorLevelsEntity entity = colorLevelsEntityList.get(colorChannel);
        colorLevelsTweakShadowSeekBar.setProgress(entity.getShadowValue());
        colorLevelsTweakMidtonesSeekBar.setProgress(entity.getMidtonesValus());
        colorLevelsTweakHighlightSeekBar.setProgress(entity.getHighlightValue());
        colorLevelsTweakOutputShadowSeekBar.setProgress(entity.getOutputShadowValue());
        colorLevelsTweakOutputHighlightSeekBar.setProgress(entity.getOutputHighlightValue());
    }//addColorLevelsSeekBarValue


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.more_effect_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }//onCreateOptionsMenu(...)

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.color_levels_tweak_options:
                openColorLevelsPopupWindow(colorLevelsTweakPopupView);
                break;
//            case R.id.hue_saturation_lightness_tweak_options:
//                Toast.makeText(this,"色相饱和度调整....",Toast.LENGTH_SHORT).show();
//                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }//onOptionsItemSelected(...)


    //SeekBar
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    //停止下来才修改
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

        switch (seekBar.getId()) {
            case R.id.color_levels_shadow_seekbar:
                colorLevelsEntityList.get(colorChannel).setShadowValue(seekBar.getProgress());
                setColorLevelsChannelByAnySeekBar();
                break;
            case R.id.color_levels_midtones_seekbar:
                colorLevelsEntityList.get(colorChannel).setMidtonesValus(seekBar.getProgress());
                setColorLevelsChannelByAnySeekBar();
                break;
            case R.id.color_levels_highlight_seekbar:
                colorLevelsEntityList.get(colorChannel).setHighlightValue(seekBar.getProgress());
                setColorLevelsChannelByAnySeekBar();
                break;
            case R.id.color_levels_output_shadow_seekbar:
                colorLevelsEntityList.get(colorChannel).setOutputShadowValue(seekBar.getProgress());
                setColorLevelsChannelByAnySeekBar();
                break;
            case R.id.color_levels_output_highlight_seekbar:
                colorLevelsEntityList.get(colorChannel).setOutputHighlightValue(seekBar.getProgress());
                setColorLevelsChannelByAnySeekBar();
                break;
            default:
                break;
        }

    }// onStopTrackingTouch

    //colorLevelsTweak(Bitmap bitmap,int colorChannel,int shadowValue,int midtones,int highlight,int outputShadow,int outputHighlight);
    //改变seekBar对应修改图片的色阶
    private void setColorLevelsChannelByAnySeekBar() {
        ColorLevelsEntity entity = colorLevelsEntityList.get(this.colorChannel);
        Bitmap dstBitmap = srcBitmap;
        dstBitmap = nativeFilter.colorLevelsTweak(srcBitmap, colorChannel, entity.getShadowValue(), entity.getMidtonesValus(), entity.getHighlightValue(), entity.getOutputShadowValue(), entity.getOutputHighlightValue());
        photoEditorView.getSource().setImageBitmap(dstBitmap);
    }
    //SeekBar
}
