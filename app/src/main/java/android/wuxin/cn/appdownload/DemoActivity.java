package android.wuxin.cn.appdownload;

import android.app.Activity;
import android.app.DownloadManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * 类名：MainActivity  使用请注册广播接收和加上权限
 *
 * @author wuxin<br/>
 */
public class DemoActivity extends Activity implements UpdateDownLoaderUtils.DownloaderCallbackListener {
    private Button mDownloadStart;
    private ProgressBar mDownloadProgress;
    private TextView mDownloadSize;
    private TextView mDownloadPrecent;
    private ImageView mDownloadCancel;
    private UpdateHandler mHandler;
    private UpdateDownLoaderUtils mUpdateDownLoaderUtils;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new UpdateHandler();
        setContentView(R.layout.activity_main);
        mUpdateDownLoaderUtils = UpdateDownLoaderUtils.getInstance(this);
        mUpdateDownLoaderUtils.setDownloaderCallbackListener(DemoActivity.this);
        mUpdateDownLoaderUtils.setDescription("downloading apps ...");
        mUpdateDownLoaderUtils.setDescriptionTitle("downloading apps ...");
        mUpdateDownLoaderUtils.setDownload_filename("wuxin_" + System.currentTimeMillis());
        initView();
        mDownloadStart.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick (View v) {
                mUpdateDownLoaderUtils.openUpdateDownLoader("your app urls");
            }
        });

        mDownloadCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick (View v) {
                mUpdateDownLoaderUtils.cancelCurrentTask(mUpdateDownLoaderUtils.getDownloadId());
            }
        });
    }

    private void initView () {
        mDownloadStart = (Button) findViewById(R.id.download_start);
        mDownloadCancel = (ImageView) findViewById(R.id.download_cancel);
        mDownloadProgress = (ProgressBar) findViewById(R.id.download_progress);
        mDownloadSize = (TextView) findViewById(R.id.download_size);
        mDownloadPrecent = (TextView) findViewById(R.id.download_precent);
    }

    private class UpdateHandler extends Handler {

        @Override
        public void handleMessage (Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case 0:
                    int status = (Integer) msg.obj;
                    if (status != DownloadManager.STATUS_FAILED) {
                        mDownloadProgress.setVisibility(View.VISIBLE);
                        mDownloadProgress.setMax(0);
                        mDownloadProgress.setProgress(0);
                        mDownloadSize.setVisibility(View.VISIBLE);
                        mDownloadPrecent.setVisibility(View.VISIBLE);
                        mDownloadCancel.setVisibility(View.VISIBLE);

                        if (msg.arg2 < 0) {
                            mDownloadProgress.setIndeterminate(true);
                            mDownloadPrecent.setText("当前已下载：" + "0%");
                            mDownloadSize.setText("0M/0M");
                        } else {
                            mDownloadProgress.setIndeterminate(false);
                            mDownloadProgress.setMax(msg.arg2);
                            mDownloadProgress.setProgress(msg.arg1);
                            mDownloadPrecent.setText("当前已下载： " + UpdateDownLoaderUtils.getNotiPercent(msg.arg1, msg.arg2));
                            mDownloadSize.setText(UpdateDownLoaderUtils.getAppSize(msg.arg1) + "/" + UpdateDownLoaderUtils.getAppSize(msg.arg2));
                        }
                    } else {
                        Log.d("TAG", "下载失败,请检查网络!");
                        mDownloadProgress.setProgress(0);
                        mDownloadPrecent.setText("当前已下载： 0%");
                        mDownloadSize.setText("0K/" + UpdateDownLoaderUtils.getAppSize(msg.arg2));
                    }
                    break;
            }
        }
    }

    @Override
    protected void onResume () {
        super.onResume();
    }

    @Override
    public void downloadComplete (String apkFilePath) {
        mUpdateDownLoaderUtils.install(apkFilePath);
    }

    @Override
    public void downloadSizeChange (int progress, int max, int state) {
        mHandler.sendMessage(mHandler.obtainMessage(0, progress, max, state));
    }
}
