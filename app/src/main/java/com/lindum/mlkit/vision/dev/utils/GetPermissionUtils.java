package com.lindum.mlkit.vision.dev.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * @author MarkGosling
 * @date 2022/3/1 14:49
 */

public class GetPermissionUtils {
    private static final int PERMISSION_REQUESTS = 1;

    /**
     * 获取需要的权限
     * @param context 上下文
     * @return 获取所需权限的权限数组
     */
    public static String[] getRequiredPermissions(Context context) {
        try {
            PackageInfo info =
                    context.getPackageManager()
                            .getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS);
            String[] ps = info.requestedPermissions;
            if (ps != null && ps.length > 0) {
                return ps;
            } else {
                return new String[0];
            }
        } catch (Exception e) {
            return new String[0];
        }
    }

    /**
     * 权限是否授权
     * @param context 上下文
     * @param permission 权限的名称
     * @return 是否授权
     */
    public static boolean isPermissionGranted(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED) {
            Logger.i("Permission granted: " + permission);
            return true;
        }
        Logger.i("Permission NOT granted: " + permission);
        return false;
    }

    /**
     * 所有权限是否已经授权
     * @param context 上下文
     * @return 是否授权
     */
    public static boolean allPermissionsGranted(Context context) {
        for (String permission : getRequiredPermissions(context)) {
            if (!isPermissionGranted(context, permission)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 获取运行时需要的权限
     * @param context 尚贤文
     */
    public static void getRuntimePermissions(Context context) {
        List<String> allNeededPermissions = new ArrayList<>();
        for (String permission : getRequiredPermissions(context)) {
            if (!isPermissionGranted(context, permission)) {
                allNeededPermissions.add(permission);
            }
        }
        if (!allNeededPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                    (Activity) context, allNeededPermissions.toArray(new String[0]), PERMISSION_REQUESTS);
        }
        // 判断是否获取外置存储写入权限
        if (Build.VERSION.SDK_INT >= 29) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    Logger.e("已获取外置存储写入权限");
                } else {
                    requestExternalStorage(context);
                }
            }
        }
    }

    /**
     * 请求访问外置内存
     *
     * @param context 上下文场景
     */
    public static void requestExternalStorage(Context context) {
        Intent intent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
        }
        assert intent != null;
        intent.setData(Uri.parse("package:" + context.getPackageName()));
        context.startActivity(intent);
    }
}
