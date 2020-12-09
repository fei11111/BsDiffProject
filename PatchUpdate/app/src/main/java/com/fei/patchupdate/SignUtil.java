package com.fei.patchupdate;

import android.content.pm.Signature;
import android.os.Build;
import android.util.DisplayMetrics;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @ClassName: SignUtil
 * @Description: 获取apk签名
 * @Author: Fei
 * @CreateDate: 2020/12/9 15:20
 * @UpdateUser: Fei
 * @UpdateDate: 2020/12/9 15:20
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class SignUtil {

    //系统版本
    private static int sysVersion = Build.VERSION.SDK_INT;

    /**
     * @param apkPath apk路径
     */
    public static String getApkSign(String apkPath) throws Exception {

        //1.反射获取PackageParser对象
        Object packageParser = getPackageParser(apkPath);
        //2.反射调用parsePackage方法获取Package
        Object packageObj = getPackageObject(apkPath, packageParser);
        //3.反射调用collectCertificates，赋值mSignatures
        collectCertificates(packageParser, packageObj);
        //4.反射获取mSignatures[0]
        return getSignatures(packageObj);
    }

    /**
     * 获取mSignatures
     */
    private static String getSignatures(Object packageObj) throws NoSuchFieldException, IllegalAccessException {
        if (sysVersion < Build.VERSION_CODES.P) {
            //小于28，直接通过package获取mSignatures
            Field mSignaturesField = packageObj.getClass().getDeclaredField("mSignatures");
            if (!mSignaturesField.isAccessible()) {
                mSignaturesField.setAccessible(true);
            }
            Signature[] signatures = (Signature[]) mSignaturesField.get(packageObj);
            return signatures[0].toCharsString();
        } else {
            //大于等于28,需要通过Package获取SignDetails，在通过SignDetails获取mSignatures
            Field mSigningDetailsField = packageObj.getClass().getDeclaredField("mSigningDetails");
            if (!mSigningDetailsField.isAccessible()) {
                mSigningDetailsField.setAccessible(true);
            }
            Object signDetail = mSigningDetailsField.get(packageObj);
            Field signaturesField = signDetail.getClass().getDeclaredField("signatures");
            if (!signaturesField.isAccessible()) {
                signaturesField.setAccessible(true);
            }
            Signature[] signatures = (Signature[]) signaturesField.get(signDetail);
            return signatures[0].toCharsString();
        }
    }

    /**
     * 反射调用collectCertificates方法，赋值mSignatures
     *
     * @param packageParser 用来获取collectCertificates方法
     * @param packageObj    collectCertificates里的参数
     */
    private static void collectCertificates(Object packageParser, Object packageObj) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        if (sysVersion < Build.VERSION_CODES.P) {
            //小于28，参数是pkg,int
            Class[] clazz = new Class[2];
            clazz[0] = packageObj.getClass();
            clazz[1] = int.class;
            Method collectCertificatesMethod = packageParser.getClass().getDeclaredMethod("collectCertificates",clazz);
            if (!collectCertificatesMethod.isAccessible()) {
                collectCertificatesMethod.setAccessible(true);
            }
            //小于24，是public方法
            if (sysVersion < Build.VERSION_CODES.N) {
                collectCertificatesMethod.invoke(packageParser, packageObj, 0);
            } else {
                //大于等于24是静态方法
                collectCertificatesMethod.invoke(null, packageObj, 0);
            }
        } else {
            //大于等于28，参数是pkg，boolean，是静态方法
            Class[] clazz = new Class[2];
            clazz[0] = packageObj.getClass();
            clazz[1] = boolean.class;
            Method collectCertificatesMethod = packageParser.getClass().getDeclaredMethod("collectCertificates",clazz);
            if (!collectCertificatesMethod.isAccessible()) {
                collectCertificatesMethod.setAccessible(true);
            }
            collectCertificatesMethod.invoke(null, packageObj, false);
        }
    }

    /**
     * 获取Package
     *
     * @param apkPath       apk路径
     * @param packageParser packageParser对象
     */
    private static Object getPackageObject(String apkPath, Object packageParser) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (sysVersion < Build.VERSION_CODES.LOLLIPOP) {
            //小于21，有四个参数 File sourceFile, String destCodePath,
            //            DisplayMetrics metrics, int flags
            Class[] clazz = new Class[4];
            clazz[0] = File.class;
            clazz[1] = String.class;
            clazz[2] = DisplayMetrics.class;
            clazz[3] = int.class;
            Method parsePackageMethod = packageParser.getClass().getDeclaredMethod("parsePackage", clazz);
            Object[] params = new Object[4];
            params[0] = new File(apkPath);
            params[1] = apkPath;
            DisplayMetrics metrics = new DisplayMetrics();
            metrics.setToDefaults();
            params[2] = metrics;
            params[3] = 0;
            if (!parsePackageMethod.isAccessible()) {
                parsePackageMethod.setAccessible(true);
            }
            return parsePackageMethod.invoke(packageParser, params);
        } else {
            //大于等于21，有两个参数 File packageFile, int flags
            Class[] clazz = new Class[2];
            clazz[0] = File.class;
            clazz[1] = int.class;
            Method parsePackageMethod = packageParser.getClass().getDeclaredMethod("parsePackage", clazz);
            Object[] params = new Object[2];
            params[0] = new File(apkPath);
            params[1] = 0;
            if (!parsePackageMethod.isAccessible()) {
                parsePackageMethod.setAccessible(true);
            }
            return parsePackageMethod.invoke(packageParser, params);
        }
    }

    /**
     * 通过构造函数获取PackageParser
     *
     * @param apkPath apk路径
     */
    private static Object getPackageParser(String apkPath) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Class<?> clazz = Class.forName("android.content.pm.PackageParser");
        if (sysVersion < Build.VERSION_CODES.LOLLIPOP) {
            //小于21,有参数
            Constructor<?> constructor = clazz.getDeclaredConstructor(String.class);
            return constructor.newInstance(apkPath);
        } else {
            //大于等于21，无参数
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            return constructor.newInstance();
        }
    }

}
