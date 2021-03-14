package vp.igpapi;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * This interface is used to provide dynamic proxy for IGWABase class
 *
 * This dynamic proxy is meant to provide further control over http calls,
 * Currently it is used to save the internal state, every @counterLimit calls
 */
public interface IGWABaseInterface
{
    String _make_request(String _url, Map<String, String> _params, Map<String, String> _headers, Map<String, String> _query, String _methodType) throws IGWAException, IOException, NoSuchAlgorithmException;
}
