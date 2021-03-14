package vp.metagram.utils;


import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Point;
import android.opengl.GLES20;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;



import static android.content.Context.ACTIVITY_SERVICE;
import static javax.microedition.khronos.egl.EGL10.EGL_ALPHA_SIZE;
import static javax.microedition.khronos.egl.EGL10.EGL_BLUE_SIZE;
import static javax.microedition.khronos.egl.EGL10.EGL_DEFAULT_DISPLAY;
import static javax.microedition.khronos.egl.EGL10.EGL_DEPTH_SIZE;
import static javax.microedition.khronos.egl.EGL10.EGL_GREEN_SIZE;
import static javax.microedition.khronos.egl.EGL10.EGL_HEIGHT;
import static javax.microedition.khronos.egl.EGL10.EGL_NONE;
import static javax.microedition.khronos.egl.EGL10.EGL_NO_CONTEXT;
import static javax.microedition.khronos.egl.EGL10.EGL_RED_SIZE;
import static javax.microedition.khronos.egl.EGL10.EGL_STENCIL_SIZE;
import static javax.microedition.khronos.egl.EGL10.EGL_WIDTH;
import static vp.metagram.general.variables.appContext;
import static vp.metagram.general.variables.logger;


public class deviceInfo
{

    public static JSONObject collect()
    {
        JSONObject result = new JSONObject();

        try
        {
            result.put("Software", collectSoftwareInfo());
        }
        catch (Exception e)
        {
        }

        try
        {
            result.put("Hardware", collectHardwareInfo());
        }
        catch (Exception e)
        {
        }

        return result;
    }


    public static JSONObject collectSoftwareInfo() throws JSONException
    {
        JSONObject softwareResult = new JSONObject();

        softwareResult.put("linuxVersion", System.getProperty("os.version"));
        softwareResult.put("APIVersion", android.os.Build.VERSION.SDK_INT);
        softwareResult.put("ReleaseVersion", android.os.Build.VERSION.RELEASE);
        softwareResult.put("deviceType", android.os.Build.DEVICE);
        softwareResult.put("deviceModel", android.os.Build.MODEL);
        softwareResult.put("deviceBrand", android.os.Build.BOARD);
        softwareResult.put("deviceID", android.os.Build.ID);
        softwareResult.put("Manufacture", android.os.Build.MANUFACTURER);
        softwareResult.put("Board", android.os.Build.BOARD);

        softwareResult.put("Fingerprint", android.os.Build.FINGERPRINT);
        softwareResult.put("Tags", android.os.Build.TAGS);

        String cpuABI = "";
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            cpuABI = "";
            for (String abi : Build.SUPPORTED_ABIS)
            {
                if ((abi != null) && (!abi.equals("")))
                {
                    cpuABI += ", " + abi;
                }
            }
            cpuABI = cpuABI.substring(1);
        } else
        {
            cpuABI = android.os.Build.CPU_ABI;
            if ((Build.CPU_ABI2 != null) && !Build.CPU_ABI2.equals("") && (!Build.CPU_ABI2.equals("unknown")))
            {
                cpuABI += ", " + Build.CPU_ABI2;
            }
        }

        softwareResult.put("cpuABI", cpuABI);

        WindowManager wm = (WindowManager) appContext.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point outSize = new Point();
        display.getRealSize(outSize);

        softwareResult.put("ActualX", outSize.x);
        softwareResult.put("ActualY", outSize.y);

        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);

        softwareResult.put("density", metrics.density);
        softwareResult.put("densityDpi", metrics.densityDpi);
        softwareResult.put("scaledDensity", metrics.scaledDensity);
        softwareResult.put("xdpi", metrics.xdpi);
        softwareResult.put("ydpi", metrics.ydpi);
        softwareResult.put("widthPixels", metrics.widthPixels);
        softwareResult.put("heightPixels", metrics.heightPixels);



        return softwareResult;

    }

    public static JSONObject collectHardwareInfo()
    {
        JSONObject HardwareResult = new JSONObject();


        try
        {
            HardwareResult.put("CPUInfo", getCPUInfo());

            HardwareResult.put("noOfCpu", Runtime.getRuntime().availableProcessors());
        }
        catch (Exception e)
        {
            logger.logError("collectHardwareInfo:getCPUInfo", e.getMessage());
        }

        try
        {
            HardwareResult.put("MemoryInfo", getMemoryInfo());
        }
        catch (Exception e)
        {
            logger.logError("collectHardwareInfo:getMemoryInfo", e.getMessage());
        }
        try
        {
            HardwareResult.put("GPUInfo", getGpuInfo());
        }
        catch (Exception e)
        {
            logger.logError("collectHardwareInfo:getCPUInfo", e.getMessage());
        }


        return HardwareResult;

    }

    static JSONObject getCPUInfo() throws Exception
    {
        JSONObject tmpObj = new JSONObject();
        Map<String, String> tempCpuInfo = getCPUInfoFromProc();
        Iterator iterator = tempCpuInfo.entrySet().iterator();
        while (iterator.hasNext())
        {
            Map.Entry pair = (Map.Entry) iterator.next();
            tmpObj.put(pair.getKey().toString(), pair.getValue().toString());
            iterator.remove();
        }
        return tmpObj;
    }

    static JSONObject getMemoryInfo() throws Exception
    {
        JSONObject tmpObj = new JSONObject();

        ActivityManager activityManager = (ActivityManager) appContext.getSystemService(ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);

        tmpObj.put("systemTotalMem", memoryInfo.totalMem);
        tmpObj.put("systemMemThreshold", memoryInfo.threshold);

        tmpObj.put("javaMaxMem", Runtime.getRuntime().maxMemory());
        tmpObj.put("javaTotalMem", Runtime.getRuntime().totalMemory());

        return tmpObj;

    }


    static JSONObject GPUObj = new JSONObject();

    static JSONObject getGpuInfo() throws JSONException
    {

        class PixelBuffer
        {

            int mWidth, mHeight;

            EGL10 mEGL;
            EGLDisplay mEGLDisplay;
            EGLConfig[] mEGLConfigs;
            EGLConfig mEGLConfig;
            EGLContext mEGLContext;
            EGLSurface mEGLSurface;
            public GL10 mGL;

            public PixelBuffer(int width, int height)
            {
                mWidth = width;
                mHeight = height;

                int[] version = new int[2];
                int[] attribList = new int[] {
                        EGL_WIDTH, mWidth,
                        EGL_HEIGHT, mHeight,
                        EGL_NONE
                };

                mEGL = (EGL10) EGLContext.getEGL();
                mEGLDisplay = mEGL.eglGetDisplay(EGL_DEFAULT_DISPLAY);
                mEGL.eglInitialize(mEGLDisplay, version);
                mEGLConfig = chooseConfig();
                mEGLContext = mEGL.eglCreateContext(mEGLDisplay, mEGLConfig, EGL_NO_CONTEXT, null);
                mEGLSurface = mEGL.eglCreatePbufferSurface(mEGLDisplay, mEGLConfig, attribList);
                mEGL.eglMakeCurrent(mEGLDisplay, mEGLSurface, mEGLSurface, mEGLContext);
                mGL = (GL10) mEGLContext.getGL();

            }

            private EGLConfig chooseConfig()
            {
                int[] attribList = new int[] {
                        EGL_DEPTH_SIZE, 0,
                        EGL_STENCIL_SIZE, 0,
                        EGL_RED_SIZE, 8,
                        EGL_GREEN_SIZE, 8,
                        EGL_BLUE_SIZE, 8,
                        EGL_ALPHA_SIZE, 8,
                        EGL_NONE
                };


                int[] numConfig = new int[1];
                mEGL.eglChooseConfig(mEGLDisplay, attribList, null, 0, numConfig);
                int configSize = numConfig[0];
                mEGLConfigs = new EGLConfig[configSize];
                mEGL.eglChooseConfig(mEGLDisplay, attribList, mEGLConfigs, configSize, numConfig);

                return mEGLConfigs[0];
            }


        }

        PixelBuffer pixelBuffer = new PixelBuffer(10, 10);

        GPUObj.put("gpuRenderer", pixelBuffer.mGL.glGetString(GLES20.GL_RENDERER));
        GPUObj.put("gpuVendor", pixelBuffer.mGL.glGetString(GLES20.GL_VENDOR));

        return GPUObj;

    }


    public static Map<String, String> getCPUInfoFromProc() throws IOException
    {

        Map<String, String> output = new HashMap<>();

        BufferedReader br = new BufferedReader(new FileReader("/proc/cpuinfo"));

        String str;

        while ((str = br.readLine()) != null)
        {

            String[] data = str.split(":");

            if (data.length > 1)
            {

                String key = data[0].trim().replace(" ", "_");
                if (key.equals("model_name")) key = "cpu_model";

                String value = data[1].trim();

                if (key.equals("cpu_model"))
                    value = value.replaceAll("\\s+", " ");

                output.put(key, value);

            }

        }

        br.close();

        return output;

    }
}
