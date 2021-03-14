package vp.igpapi;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * This library does not parse data received from Instagram API and it is the programmer`s job to
 * parse the data, I have chose this method, because it make the library independent of the
 * changes in structure of received data.
 *
 * IGWADigest can be implemented by classes which act as API`s entity`s data types
 * For parsing, a subset of IGWA class can be inherited that it`s method accept IGWADigest as one
 * of the parameters in addition to the original parameters and will call the digest
 * method after receiving raw data from the API,
 * digest method that has been implemented with respect to data type and received json,
 * will fill the data type return the filled version of IGWADigest instance that has been passed
 * to the method in the first place
 *
 * It make pagination much easier, also the library will be independent of the json structure
 *
 */
public interface IGWADigest
{
    <T> T digest(JSONObject input) throws JSONException;
}
