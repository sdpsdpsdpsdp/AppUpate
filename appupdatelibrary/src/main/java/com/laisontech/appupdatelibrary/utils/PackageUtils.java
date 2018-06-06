package com.laisontech.appupdatelibrary.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;


import com.laisontech.appupdatelibrary.R;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by SDP on 2017/10/19.
 * 对系统包进行检测以及查看
 */

public class PackageUtils {

    /**
     * 检测一个app是否已经安装
     */
    public static boolean isAppInstalled(Context context, String packageName) {
        final PackageManager pm = context.getPackageManager();
        boolean installed;
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            installed = false;
        }
        return installed;
    }

    /**
     * 卸载一个app
     */
    public static void uninstall(Context context, String packageName) {
        //通过程序的包名创建URI
        Uri packageURI = Uri.parse("package:" + packageName);
        //创建Intent意图
        Intent intent = new Intent(Intent.ACTION_DELETE, packageURI);
        //执行卸载程序
        context.startActivity(intent);
    }

    /**
     * 根据文件路径获取包名
     */
    public static String getPackageName(Context context, String filePath) {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo info = packageManager.getPackageArchiveInfo(filePath, PackageManager.GET_ACTIVITIES);
        if (info != null) {
            ApplicationInfo appInfo = info.applicationInfo;
            return appInfo.packageName;  //得到安装包名称
        }
        return null;
    }

    /**
     * 安装一个apk文件
     */
    public static boolean install(Context context, String filePath) {
        int sdkInt = Build.VERSION.SDK_INT;
        if (sdkInt >= Build.VERSION_CODES.O) {
            PackageManager packageManager = context.getPackageManager();
            if (packageManager == null) return false;
            boolean canRequestPackageInstalls = packageManager.canRequestPackageInstalls();
            if (!canRequestPackageInstalls) {
                Toast.makeText(context, R.string.NeedInstallPerssion, Toast.LENGTH_SHORT).show();
                //该权限不是运行时权限，智能用户自己操作，所以需要用户自己调节
                //注意这个是8.0新API
                Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                context.startActivity(intent);
                return false;
            }
            if (filePath != null && !filePath.isEmpty()) {
                installApp(context, filePath);
            }
            return true;
        } else {
            if (filePath != null && !filePath.isEmpty()) {
                installApp(context, filePath);
            }
            return true;
        }
    }

    private static void installApp(Context context, String filePath) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse("file://" + filePath), "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }


    /**
     * 启动一个app
     */
    public static void launcherApp(Context context, String packagename) {

        // 通过包名获取此APP详细信息，包括Activities、services、versioncode、name等等
        PackageInfo packageinfo = null;
        try {
            packageinfo = context.getPackageManager().getPackageInfo(packagename, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageinfo == null) {
            return;
        }
        // 创建一个类别为CATEGORY_LAUNCHER的该包名的Intent
        Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
        resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resolveIntent.setPackage(packageinfo.packageName);
        // 通过getPackageManager()的queryIntentActivities方法遍历
        List<ResolveInfo> resolveinfoList = context.getPackageManager()
                .queryIntentActivities(resolveIntent, 0);
        ResolveInfo resolveinfo = resolveinfoList.iterator().next();
        if (resolveinfo != null) {
            // packagename = 参数packname
            String packageName = resolveinfo.activityInfo.packageName;
            // 这个就是我们要找的该APP的LAUNCHER的Activity[组织形式：packagename.mainActivityname]
            String className = resolveinfo.activityInfo.name;
            // LAUNCHER Intent
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            // 设置ComponentName参数1:packagename参数2:MainActivity路径
            ComponentName cn = new ComponentName(packageName, className);
            intent.setComponent(cn);
            context.startActivity(intent);
        }
    }

    /**
     * 获取手机中所有app的信息
     */
    public static ArrayList<HashMap<String, Object>> getItems(Context context) {

        PackageManager pckMan = context.getPackageManager();
        ArrayList<HashMap<String, Object>> items = new ArrayList<HashMap<String, Object>>();

        List<PackageInfo> packageInfo = pckMan.getInstalledPackages(0);

        for (PackageInfo pInfo : packageInfo) {

            HashMap<String, Object> item = new HashMap<String, Object>();

            item.put("appimage", pInfo.applicationInfo.loadIcon(pckMan));
            item.put("packageName", pInfo.packageName);
            item.put("versionCode", pInfo.versionCode);
            item.put("versionName", pInfo.versionName);
            item.put("appName", pInfo.applicationInfo.loadLabel(pckMan).toString());

            items.add(item);
        }
        return items;
    }

    /**
     * 获取某个包名所对应的app信息
     */
    public static PackageInfo getAppInoFromPackageName(Context context, String packageName) {
        //包管理操作管理类
        PackageManager pm = context.getPackageManager();
        PackageInfo packinfo = null;
        try {
            packinfo = pm.getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return packinfo;
    }

    /**
     * 获取应用程序名称
     */
    public static String getAppName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            int labelRes = packageInfo.applicationInfo.labelRes;
            return context.getResources().getString(labelRes);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
    * 获取程序的名字
    */
    public static String getAppName(PackageManager pm, String packname) {
        try {
            ApplicationInfo info = pm.getApplicationInfo(packname, 0);
            return info.loadLabel(pm).toString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /*
       *获取程序的版本号
       */
    public static int getAppVersionCode(PackageManager pm, String packname) {
        try {
            PackageInfo packinfo = pm.getPackageInfo(packname, 0);
            return packinfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /*
       *获取版本号名
       */
    public static String getAppVersionName(PackageManager pm, String packname) {

        try {
            PackageInfo packinfo = pm.getPackageInfo(packname, 0);
            return packinfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /*
       *获取版本号名
       */
    public static String getAppPackageName(PackageManager pm, String packname) {

        try {
            PackageInfo packinfo = pm.getPackageInfo(packname, 0);
            if (packname.equals(packinfo.packageName)) {
                return packname;
            }
            return null;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取当前应用程序的包名
     *
     * @param context 上下文对象
     * @return 返回包名
     */
    public static String getAppProcessName(Context context) {
        //当前应用pid
        int pid = android.os.Process.myPid();
        //任务管理类
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        //遍历所有应用
        assert manager != null;
        List<ActivityManager.RunningAppProcessInfo> infos = manager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo info : infos) {
            if (info.pid == pid)//得到当前应用
                return info.processName;//返回包名
        }
        return "";
    }

    //获取app的包名
    public static String getAppPackageName(Context context) {
        String packageName;
        packageName = context.getPackageName();
        if (packageName == null || packageName.isEmpty()) {
            try {
                PackageManager manager = context.getPackageManager();
                PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
                if (info != null) {
                    packageName = info.packageName;
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return packageName;
    }

    //获取app的versionCode
    public static int getAppVersionCode(Context context) {
        int versionCode = 0;
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo info = packageManager.getPackageInfo(context.getPackageName(), 0);
            if (info != null) {
                versionCode = info.versionCode;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    /**
     * 获取程序 图标
     *
     * @param context
     * @param packname 应用包名
     * @return
     */
    public static Drawable getAppIcon(Context context, String packname) {
        try {
            //包管理操作管理类
            PackageManager pm = context.getPackageManager();
            //获取到应用信息
            ApplicationInfo info = pm.getApplicationInfo(packname, 0);
            return info.loadIcon(pm);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Drawable getAppIcon(PackageManager pm, String packname) {
        try {
            //获取到应用信息
            ApplicationInfo info = pm.getApplicationInfo(packname, 0);
            return info.loadIcon(pm);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取当前展示 的Activity名称
     */
    private static String getCurrentActivityName(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        String runningActivity;
        runningActivity = activityManager.getRunningTasks(1).get(0).topActivity.getClassName();
        return runningActivity;
    }

    //通过反射获取app的大小 M
    public static double getPkgSize(final ApplicationInfo appInfo) {
        String dir = appInfo.publicSourceDir;
        int size = Integer.valueOf((int) new File(dir).length());
        BigDecimal apkSize = parseApkSize(size);
        return apkSize.doubleValue();
    }

    //单位kb
    // 1m = 1024kb  1kb = 1024b
    private static BigDecimal parseApkSize(int size) {
        BigDecimal bd = new BigDecimal((double) size / (1024 * 1024));
        BigDecimal setScale = bd.setScale(2, BigDecimal.ROUND_DOWN);
        return setScale;
    }
}
