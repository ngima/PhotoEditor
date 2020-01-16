package com.burhanrashid52.photoeditor;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.burhanrashid52.photoeditor.base.BaseActivity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import ja.burhanrashid52.photoeditor.PhotoEditor;
import ja.burhanrashid52.photoeditor.PhotoEditorView;
import ja.burhanrashid52.photoeditor.SaveSettings;

import static ja.burhanrashid52.photoeditor.Utils.dpToPx;

public class EditImageNewActivity extends BaseActivity {

    private static final int CAMERA_REQUEST = 52;
    private static final int PICK_REQUEST = 53;
    PhotoEditor mPhotoEditor;
    private PhotoEditorView mPhotoEditorView;

    private ArrayList<String> arrayList = new ArrayList<>();
    private IssueAdapter adapter = new IssueAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        makeFullScreen();
        setContentView(R.layout.activity_edit_image_new);

        initViews();

//        mWonderFont = Typeface.createFromAsset(getAssets(), "beyond_wonderland.ttf");

//        mPropertiesBSFragment = new PropertiesBSFragment();
//        mEmojiBSFragment = new EmojiBSFragment();
//        mStickerBSFragment = new StickerBSFragment();

        mPhotoEditor = new PhotoEditor.Builder(this, mPhotoEditorView)
                .setPinchTextScalable(true) // set flag to make text scalable when pinch
                //.setDefaultTextTypeface(mTextRobotoTf)
                //.setDefaultEmojiTypeface(mEmojiTypeFace)
                .build(); // build photo editor sdk
        final RecyclerView recyclerView = ((RecyclerView) findViewById(R.id.recyclerView));

        mPhotoEditorView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    if (
                            motionEvent.getY() >= dpToPx(18) &&
                                    mPhotoEditorView.getHeight() - motionEvent.getY() >= dpToPx(18) &&
                                    motionEvent.getX() >= dpToPx(18) &&
                                    mPhotoEditorView.getWidth() - motionEvent.getX() >= dpToPx(18)
                    ) {
                        mPhotoEditor.addIssue(arrayList.size() + 1, ((int) motionEvent.getX()), ((int) motionEvent.getY()));
                        arrayList.add("issue");
                        adapter.addItem(new IssueModel("" + (arrayList.size())));
                        recyclerView.scrollToPosition(adapter.getItemCount() - 1);
                    }
                }
                return true;
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void initViews() {

        mPhotoEditorView = findViewById(R.id.photoEditorView);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        mPhotoEditorView.setLayoutParams(new LinearLayout.LayoutParams(width,
                width * 3 / 4));
    }

    @SuppressLint("MissingPermission")
    private void saveImage() {
        if (requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            showLoading("Saving...");
            File file = new File(Environment.getExternalStorageDirectory()
                    + File.separator + ""
                    + System.currentTimeMillis() + ".png");
            try {
                file.createNewFile();

                SaveSettings saveSettings = new SaveSettings.Builder()
                        .setClearViewsEnabled(true)
                        .setTransparencyEnabled(true)
                        .build();

                mPhotoEditor.saveAsFile(file.getAbsolutePath(), saveSettings, new PhotoEditor.OnSaveListener() {
                    @Override
                    public void onSuccess(@NonNull String imagePath) {
                        hideLoading();
                        showSnackbar("Image Saved Successfully");
                        mPhotoEditorView.getSource().setImageURI(Uri.fromFile(new File(imagePath)));
                    }

                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        hideLoading();
                        showSnackbar("Failed to save Image");
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
                hideLoading();
                showSnackbar(e.getMessage());
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CAMERA_REQUEST:
                    mPhotoEditor.clearAllViews();
                    Bitmap photo = (Bitmap) data.getExtras().get("data");
                    mPhotoEditorView.getSource().setImageBitmap(photo);
                    break;
                case PICK_REQUEST:
                    try {
                        mPhotoEditor.clearAllViews();
                        Uri uri = data.getData();
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                        mPhotoEditorView.getSource().setImageBitmap(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }

    @Override
    public void isPermissionGranted(boolean isGranted, String permission) {
        if (isGranted) {
            saveImage();
        }
    }

    private void showSaveDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.msg_save_image));
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                saveImage();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setNeutralButton("Discard", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.create().show();
    }
//    }
//
//    @Override
//    public void onFilterSelected(PhotoFilter photoFilter) {
//        mPhotoEditor.setFilterEffect(photoFilter);
//    }
//
//    @Override
//    public void onToolSelected(ToolType toolType) {
//        switch (toolType) {
//            case BRUSH:
//                mPhotoEditor.setBrushDrawingMode(true);
//                mTxtCurrentTool.setText(R.string.label_brush);
//                mPropertiesBSFragment.show(getSupportFragmentManager(), mPropertiesBSFragment.getTag());
//                break;
//            case TEXT:
//                TextEditorDialogFragment textEditorDialogFragment = TextEditorDialogFragment.show(this);
//                textEditorDialogFragment.setOnTextEditorListener(new TextEditorDialogFragment.TextEditor() {
//                    @Override
//                    public void onDone(String inputText, int colorCode) {
//                        final TextStyleBuilder styleBuilder = new TextStyleBuilder();
//                        styleBuilder.withTextColor(colorCode);
//
//                        mPhotoEditor.addText(inputText, styleBuilder);
//                        mTxtCurrentTool.setText(R.string.label_text);
//                    }
//                });
//                break;
//            case ISSUE:
//                mPhotoEditor.addIssue(1);
//                mTxtCurrentTool.setText(R.string.label_issue);
//                break;
//            case ERASER:
//                mPhotoEditor.brushEraser();
//                mTxtCurrentTool.setText(R.string.label_eraser_mode);
//                break;
//            case FILTER:
//                mTxtCurrentTool.setText(R.string.label_filter);
//                showFilter(true);
//                break;
//            case EMOJI:
//                mEmojiBSFragment.show(getSupportFragmentManager(), mEmojiBSFragment.getTag());
//                break;
//            case STICKER:
//                mStickerBSFragment.show(getSupportFragmentManager(), mStickerBSFragment.getTag());
//                break;
//        }
//    }
//
//
//    void showFilter(boolean isVisible) {
//        mIsFilterVisible = isVisible;
//        mConstraintSet.clone(mRootView);
//
//        if (isVisible) {
//            mConstraintSet.clear(mRvFilters.getId(), ConstraintSet.START);
//            mConstraintSet.connect(mRvFilters.getId(), ConstraintSet.START,
//                    ConstraintSet.PARENT_ID, ConstraintSet.START);
//            mConstraintSet.connect(mRvFilters.getId(), ConstraintSet.END,
//                    ConstraintSet.PARENT_ID, ConstraintSet.END);
//        } else {
//            mConstraintSet.connect(mRvFilters.getId(), ConstraintSet.START,
//                    ConstraintSet.PARENT_ID, ConstraintSet.END);
//            mConstraintSet.clear(mRvFilters.getId(), ConstraintSet.END);
//        }
//
//        ChangeBounds changeBounds = new ChangeBounds();
//        changeBounds.setDuration(350);
//        changeBounds.setInterpolator(new AnticipateOvershootInterpolator(1.0f));
//        TransitionManager.beginDelayedTransition(mRootView, changeBounds);
//
//        mConstraintSet.applyTo(mRootView);
//    }
//
//    @Override
//    public void onBackPressed() {
//        if (mIsFilterVisible) {
//            showFilter(false);
//            mTxtCurrentTool.setText(R.string.app_name);
//        } else if (!mPhotoEditor.isCacheEmpty()) {
//            showSaveDialog();
//        } else {
//            super.onBackPressed();
//        }
//    }
}

