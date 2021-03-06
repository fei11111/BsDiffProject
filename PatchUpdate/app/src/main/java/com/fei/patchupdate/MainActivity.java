package com.fei.patchupdate;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private String patch = Environment.getExternalStorageDirectory().getAbsolutePath()
            + File.separator + "version.patch";
    private String newApkPath = Environment.getExternalStorageDirectory().getAbsolutePath()
            + File.separator + "version2.apk";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    //合并
    public void combine(View view) {
        if (new File(patch).exists()) {
            int combine = PatchUtil.combine(getPackageResourcePath(), newApkPath, patch);

            if (combine == 0) {
                Log.e(TAG, "合并成功");
                //检验签名
                try {
                    //获取现在apk的签名
                    String originalSign = SignUtil.getApkSign(getPackageResourcePath());
                    //获取合并后apk的签名
                    String targetSign = SignUtil.getApkSign(newApkPath);
                    Log.e(TAG, "orginalSign = " + originalSign + " targetSign = " + targetSign);
                    //相同则安装
                    if (originalSign.equals(targetSign)) {
                        // 通过Intent安装APK文件
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        Uri data;
                        // 判断版本大于等于7.0
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            // ""即是在清单文件中配置的authorities
                            data = FileProvider.getUriForFile(this, getPackageName() + ".fileProvider", new File(newApkPath));
                            // 给目标应用一个临时授权
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        } else {
                            data = Uri.fromFile(new File(newApkPath));
                        }
                        intent.setDataAndType(data, "application/vnd.android.package-archive");
                        startActivity(intent);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //拆分
    public void diff(View view) {
        if (new File(newApkPath).exists()) {
            PatchUtil.diff(getPackageResourcePath(), newApkPath, patch);
        }

    }
}
