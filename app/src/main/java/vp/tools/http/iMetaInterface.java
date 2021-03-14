package vp.tools.http;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.GeneralSecurityException;

import vp.metagram.utils.MetaServerException;

public interface iMetaInterface
{
    public JSONObject postMessage(String message) throws MetaServerException, GeneralSecurityException, JSONException, IOException;
}
