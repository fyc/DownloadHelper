package com.yaoxiaowen.download.sample;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yaoxiaowen.download.DownloadHelper;
import com.yaoxiaowen.download.DownloadListener;
import com.yaoxiaowen.download.DownloadStatus;
import com.yaoxiaowen.download.FileInfo;
import com.yaoxiaowen.download.sample.utils.Utils_Parse;
import com.yaoxiaowen.download.utils.DebugUtils;
import com.yaoxiaowen.download.utils.LogUtils;

import java.io.File;

/**
 * @author www.yaoxiaowen.com
 * time:  2017/12/20 20:23
 * @since 1.0.0
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = "weny SimpleMainActivity";
    private Handler mainHandler;
    //Todo 同程旅游的下载地址 ：     "http://s.ly.com/tTV79";
    //为什么下载不下来，这个 网页做了什么, 回头要研究

    //豌豆荚 app 下载地址
    private static final String firstUrl = Constanst.WAN_DOU_JIA_URL;
    private File firstFile;
    private String firstName = Constanst.WAN_DOU_JIA_NAME;

    //美团 app 下载地址
    private static final String secondUrl = Constanst.MEI_TUAN_URL;
    private File secondFile;
    private String secondName = Constanst.MEI_TUAN_NAME;

    // 12306 APP 下载地址
    private static final String thirdUrl = Constanst.TRAIN_12306_URL;
    private File thirdFile;
    private String thirdName = Constanst.TRAIN_12306_NAME;

    private DownloadHelper mDownloadHelper;
    private File dir;

    private static final String START = "开始";
    private static final String PAUST = "暂停";


    private static int textColor1 = Color.parseColor("#333333");
    private static int textColor2 = Color.parseColor("#666666");
    private static int textColor3 = Color.parseColor("#999999");
    private static int textColorBlock = Color.parseColor("#000000");
    private static int textColorRandarRed = Color.parseColor("#FF0000");
    private static int textColorGreen = Color.parseColor("#46BCFF");

    private TextView firstTitle;
    private ProgressBar firstProgressBar;
    private Button firstBtn, firstCancleBtn, firstChangeForegroundBtn;

    private TextView secondTitle;
    private ProgressBar secondProgressBar;
    private Button secondBtn, secondCancleBtn, secondChangeForegroundBtn;

    private TextView thirdTitle;
    private ProgressBar thirdProgressBar;
    private Button thirdBtn, thirdCancleBtn, thirdChangeForegroundBtn;

    private Button deleteAllBtn;
    private Button jumpTestActyBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LogUtils.setDebug(true);

        mainHandler = new Handler(getMainLooper());

        initData();
        initView();
        initListener();
    }

    private void initData() {
        firstFile = new File(getDir(), firstName);
        secondFile = new File(getDir(), secondName);
        thirdFile = new File(getDir(), thirdName);

        mDownloadHelper = DownloadHelper.getInstance();
    }

    private void initView() {
        firstTitle = (TextView) findViewById(R.id.firstTitle);
        firstProgressBar = (ProgressBar) findViewById(R.id.firstProgressBar);
        firstBtn = (Button) findViewById(R.id.firstBtn);
        firstCancleBtn = (Button) findViewById(R.id.firstCancleBtn);
        firstChangeForegroundBtn = (Button) findViewById(R.id.firstChangeForegroundBtn);
        firstBtn.setText(START);

        secondTitle = (TextView) findViewById(R.id.secondTitle);
        secondProgressBar = (ProgressBar) findViewById(R.id.secondProgressBar);
        secondBtn = (Button) findViewById(R.id.secondBtn);
        secondCancleBtn = (Button) findViewById(R.id.secondCancleBtn);
        secondChangeForegroundBtn = (Button) findViewById(R.id.secondChangeForegroundBtn);
        secondBtn.setText(START);

        thirdTitle = (TextView) findViewById(R.id.thirdTitle);
        thirdProgressBar = (ProgressBar) findViewById(R.id.thirdProgressBar);
        thirdBtn = (Button) findViewById(R.id.thirdBtn);
        thirdCancleBtn = (Button) findViewById(R.id.thirdCancleBtn);
        thirdChangeForegroundBtn = (Button) findViewById(R.id.thirdChangeForegroundBtn);
        thirdBtn.setText(START);

        deleteAllBtn = (Button) findViewById(R.id.deleteAllBtn);

        jumpTestActyBtn = (Button) findViewById(R.id.jumpTestActyBtn);
    }

    private void initListener() {
        firstBtn.setOnClickListener(this);
        firstCancleBtn.setOnClickListener(this);
        firstChangeForegroundBtn.setOnClickListener(this);

        secondBtn.setOnClickListener(this);
        secondCancleBtn.setOnClickListener(this);
        secondChangeForegroundBtn.setOnClickListener(this);

        thirdBtn.setOnClickListener(this);
        thirdCancleBtn.setOnClickListener(this);
        thirdChangeForegroundBtn.setOnClickListener(this);

        deleteAllBtn.setOnClickListener(this);
        jumpTestActyBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.firstBtn:
                onFirstApkClick();
                break;
            case R.id.firstCancleBtn:
                mDownloadHelper.cancleTask(firstUrl, firstFile).submit(MainActivity.this);
                break;
            case R.id.firstChangeForegroundBtn:
                mDownloadHelper.addTask(firstUrl, firstFile, firstName, "").submitNotify(MainActivity.this);
                break;
            case R.id.secondBtn:
                onSecondApkClick();
                break;
            case R.id.secondCancleBtn:
                mDownloadHelper.cancleTask(secondUrl, secondFile).submit(MainActivity.this);
                break;
            case R.id.secondChangeForegroundBtn:
                mDownloadHelper.addTask(secondUrl, secondFile, secondName, "").submitNotify(MainActivity.this);
                break;
            case R.id.thirdBtn:
                onThirdApkClick();
                break;
            case R.id.thirdCancleBtn:
                mDownloadHelper.cancleTask(thirdUrl, thirdFile).submit(MainActivity.this);
                break;
            case R.id.thirdChangeForegroundBtn:
                mDownloadHelper.addTask(thirdUrl, thirdFile, thirdName, "").submitNotify(MainActivity.this);
                break;
            case R.id.deleteAllBtn:
                deleteAllFile();
                break;
            case R.id.jumpTestActyBtn:
                Intent intent = new Intent(this, TestActivity.class);
                startActivity(intent);
                break;
        }
    }


    private File getDir() {
        if (dir != null && dir.exists()) {
            return dir;
        }

        dir = new File(getExternalCacheDir(), "download");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    private void onFirstApkClick() {
        String firstContent = firstBtn.getText().toString().trim();
        if (TextUtils.equals(firstContent, START)) {
            mDownloadHelper.addTask(firstUrl, firstFile, new FirstDownloadListener()).submit(MainActivity.this);
            firstBtn.setText(PAUST);
            firstBtn.setBackgroundResource(R.drawable.shape_btn_orangle);

        } else {
            mDownloadHelper.pauseTask(firstUrl, firstFile).submit(MainActivity.this);
            firstBtn.setText(START);
            firstBtn.setBackgroundResource(R.drawable.shape_btn_blue);
        }
    }

    private void onSecondApkClick() {
        String secondContent = secondBtn.getText().toString().trim();
        if (TextUtils.equals(secondContent, START)) {
            mDownloadHelper.addTask(secondUrl, secondFile, new SecondDownloadListener()).submit(MainActivity.this);
            secondBtn.setText(PAUST);
            secondBtn.setBackgroundResource(R.drawable.shape_btn_orangle);
        } else {
            mDownloadHelper.pauseTask(secondUrl, secondFile).submit(MainActivity.this);
            secondBtn.setText(START);
            secondBtn.setBackgroundResource(R.drawable.shape_btn_blue);
        }
    }

    private void onThirdApkClick() {
        String thirdContent = thirdBtn.getText().toString().trim();
        if (TextUtils.equals(thirdContent, START)) {
            mDownloadHelper.addTask(thirdUrl, thirdFile, new ThirdDownloadListener()).submit(MainActivity.this);
            thirdBtn.setText(PAUST);
            thirdBtn.setBackgroundResource(R.drawable.shape_btn_orangle);
        } else {
            mDownloadHelper.pauseTask(thirdUrl, thirdFile).submit(MainActivity.this);
            thirdBtn.setText(START);
            thirdBtn.setBackgroundResource(R.drawable.shape_btn_blue);
        }
    }


    @Override
    protected void onDestroy() {
        //解绑
        mDownloadHelper.unbindService(MainActivity.this);
        super.onDestroy();
    }


    private void updateTextview(TextView textView, ProgressBar progressBar, FileInfo fileInfo, String fileName, Button btn) {
        float pro = (float) (fileInfo.getDownloadLocation() * 1.0 / fileInfo.getSize());
        int progress = (int) (pro * 100);
        float downSize = fileInfo.getDownloadLocation() / 1024.0f / 1024;
        float totalSize = fileInfo.getSize() / 1024.0f / 1024;

        // 我们将字体颜色设置的好看一些而已
        int count = 0;
        SpannableStringBuilder sb = new SpannableStringBuilder();
        sb.append(fileName);
        sb.setSpan(new ForegroundColorSpan(textColorBlock), 0, sb.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        count = sb.length();
        sb.append("\t  ( " + progress + "% )" + "\n");
        sb.setSpan(new ForegroundColorSpan(textColor3), count, sb.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        count = sb.length();
        sb.append("状态:");
        sb.setSpan(new ForegroundColorSpan(textColor2), count, sb.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        count = sb.length();

        sb.append(DebugUtils.getStatusDesc(fileInfo.getDownloadStatus()) + " \t \t\t \t\t\t");
        sb.setSpan(new ForegroundColorSpan(textColorGreen), count, sb.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        count = sb.length();

        sb.append(Utils_Parse.getTwoDecimalsStr(downSize) + "M/" + Utils_Parse.getTwoDecimalsStr(totalSize) + "M\n");
        sb.setSpan(new ForegroundColorSpan(textColorRandarRed), count, sb.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);


        textView.setText(sb);


        progressBar.setProgress(progress);

        if (fileInfo.getDownloadStatus() == DownloadStatus.COMPLETE) {
            btn.setText("下载完成");
            btn.setBackgroundColor(0xff5c0d);
        }
    }

    private void deleteAllFile() {
        if (firstFile != null && firstFile.exists()) {
            firstFile.delete();
        }

        if (secondFile != null && secondFile.exists()) {
            secondFile.delete();
        }

        if (thirdFile != null && thirdFile.exists()) {
            thirdFile.delete();
        }
    }


    class FirstDownloadListener implements DownloadListener {


        @Override
        public void onPepare() {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    firstTitle.setText("准备下载");
                }
            });
        }

        @Override
        public void onWait() {

        }

        @Override
        public void onLoading(final FileInfo fileInfo) {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    updateTextview(firstTitle, firstProgressBar, fileInfo, firstName, firstBtn);
                }
            });
        }

        @Override
        public void onFailed() {

        }

        @Override
        public void onPaused() {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    firstTitle.setText("pause");
                    firstTitle.setBackgroundColor(0xff5c0d);
                }
            });
        }

        @Override
        public void onComplete() {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    firstBtn.setText("下载完成");
                    firstBtn.setBackgroundColor(0xff5c0d);
                }
            });
        }

        @Override
        public void onCanceled() {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    firstBtn.setText(START);
                    firstBtn.setBackgroundResource(R.drawable.shape_btn_blue);
                    firstTitle.setText("cancle");
                    firstProgressBar.setProgress(0);
                }
            });
        }
    }

    class SecondDownloadListener implements DownloadListener {


        @Override
        public void onPepare() {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    secondTitle.setText("准备下载");
                }
            });
        }

        @Override
        public void onWait() {

        }

        @Override
        public void onLoading(final FileInfo fileInfo) {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    updateTextview(secondTitle, secondProgressBar, fileInfo, secondName, secondBtn);
                }
            });
        }

        @Override
        public void onFailed() {

        }

        @Override
        public void onPaused() {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    secondTitle.setText("pause");
                    secondTitle.setBackgroundColor(0xff5c0d);
                }
            });
        }

        @Override
        public void onComplete() {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    secondBtn.setText("下载完成");
                    secondBtn.setBackgroundColor(0xff5c0d);
                }
            });
        }

        @Override
        public void onCanceled() {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    secondBtn.setText(START);
                    secondBtn.setBackgroundResource(R.drawable.shape_btn_blue);
                    secondTitle.setText("cancle");
                    secondProgressBar.setProgress(0);
                }
            });
        }
    }

    class ThirdDownloadListener implements DownloadListener {


        @Override
        public void onPepare() {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    thirdTitle.setText("准备下载");
                }
            });
        }

        @Override
        public void onWait() {

        }

        @Override
        public void onLoading(final FileInfo fileInfo) {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    updateTextview(thirdTitle, thirdProgressBar, fileInfo, thirdName, thirdBtn);
                }
            });
        }

        @Override
        public void onFailed() {

        }

        @Override
        public void onPaused() {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    thirdTitle.setText("pause");
                    thirdTitle.setBackgroundColor(0xff5c0d);
                }
            });
        }

        @Override
        public void onComplete() {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    thirdBtn.setText("下载完成");
                    thirdBtn.setBackgroundColor(0xff5c0d);
                }
            });
        }

        @Override
        public void onCanceled() {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    thirdBtn.setText(START);
                    thirdBtn.setBackgroundResource(R.drawable.shape_btn_blue);
                    thirdTitle.setText("cancle");
                    thirdProgressBar.setProgress(0);
                }
            });
        }
    }
}
