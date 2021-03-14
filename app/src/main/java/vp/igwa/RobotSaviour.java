package vp.igwa;

import java.io.IOException;

import vp.metagram.utils.instagram.InstagramAgent;
import vp.igpapi.IGWAStorage;

public class RobotSaviour implements IGWAStorage
{
    InstagramAgent agent;

    public RobotSaviour(InstagramAgent agent)
    {
        this.agent = agent;
    }

    @Override
    public void save(String s, String s1) throws IOException
    {

    }

    @Override
    public String load(String s) throws IOException
    {
        return null;
    }
}
