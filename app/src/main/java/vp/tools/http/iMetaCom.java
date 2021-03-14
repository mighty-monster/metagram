package vp.tools.http;

import android.content.Context;
import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.security.GeneralSecurityException;


import vp.metagram.utils.MetaServerException;
import vp.tools.cipher.iAESCipher;

import static vp.metagram.general.functions.getIV;
import static vp.metagram.general.variables.deviceSettings;
import static vp.metagram.general.variables.executionMode;


public class iMetaCom implements iMetaInterface
{
    iHttpClient httpClient;

    iEndPoints endPoint;
    iServerAddress serverAddress;

    public iMetaInterface proxy;
    public iMetaProxy dynamicProxy;

    public iMetaCom(Context context, iServerAddress serverAddress, iEndPoints endPoint) throws  GeneralSecurityException,  IOException
    {
        ClassLoader classLoader = getClass().getClassLoader();
        dynamicProxy = new iMetaProxy(this);
        proxy = (iMetaInterface) Proxy.newProxyInstance(classLoader, new Class[]{iMetaInterface.class}, dynamicProxy);
        httpClient = new iHttpClient(context);
        this.serverAddress = serverAddress;
        this.endPoint = endPoint;
    }

    public JSONObject postMessage(String message) throws MetaServerException, GeneralSecurityException, JSONException, IOException
    {
        if (deviceSettings.DeviceUUI == null || deviceSettings.DeviceUUI.equals("")) {return null;}

        String IV = getIV();
        iAESCipher AESIAESCipher = new iAESCipher(Base64.decode(deviceSettings.comAES, Base64.NO_WRAP), IV.getBytes());

        String cmd = AESIAESCipher.encryptToHex(message.toString().getBytes());

        JSONObject cmdObj = new JSONObject();

        cmdObj.put("Node1", deviceSettings.DeviceUUI);
        cmdObj.put("Node2", cmd);
        cmdObj.put("Node3", IV);

        cmd = cmdObj.toString();

        String answer = "";
        if ( executionMode.equals("debug") )
        {
            answer = httpClient.httpPost(serverAddress.getServerAddress(endPoint), cmd);
        }
        else
        {
            answer = httpClient.httpsPost(serverAddress.getServerAddress(endPoint), cmd);
        }

        answer = AESIAESCipher.decryptFromHexToString(answer);

        JSONObject resultObj = new JSONObject(answer);

        if(!resultObj.getString("result").equals("ok"))
        {
            throw new MetaServerException(resultObj.getString("message"));
        }

        return resultObj;
    }

}
