package vp.metagram.utils.instagram;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import vp.metagram.utils.instagram.types.ResponseStatus;

import static vp.metagram.general.functions.isNetworkAvailable;
import static vp.metagram.general.variables.appContext;


public class InstagramProxy implements InvocationHandler
{
    private InstagramAgent instagramAgent;

    public InstagramProxy(InstagramAgent instagramAgent)
    {
        this.instagramAgent = instagramAgent;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        Object result = null;

        if (!isNetworkAvailable(appContext))
        { instagramAgent.agentStatus.responseStatus = ResponseStatus.noInternet; }

        try
        {
            long startTime = System.currentTimeMillis();
            result = method.invoke(instagramAgent,args);
            long endTime = System.currentTimeMillis();
            instagramAgent.agentStatus.calculateAverageRequestDuration(endTime - startTime);
            instagramAgent.agentStatus.responseStatus = ResponseStatus.ok;
        }
        catch (InvocationTargetException e)
        {
            if (e.getTargetException() instanceof IOException)
            {
                instagramAgent.agentStatus.responseStatus = ResponseStatus.noInternet;

                throw e;
            }
            else
            {
                try
                {
                    result = method.invoke(instagramAgent,args);
                    instagramAgent.agentStatus.responseStatus = ResponseStatus.ok;
                }
                catch (InvocationTargetException e2)
                {
                    throw e2.getTargetException();
                }

            }
        }

        return result;
    }
}
