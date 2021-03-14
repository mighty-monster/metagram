package vp.igpapi;

import java.io.IOException;

/**
 * This interface have to be implemented and an instance of it has to be passed to IGWA
 * constructor, the class will use these methods to save and load it`s internal state
 * In this library the key will be the username and the value will be json equivalent of
 * internal state
 */

public interface IGWAStorage
{
    public void save(String key, String value) throws IOException;
    public String load(String key) throws IOException;
}
