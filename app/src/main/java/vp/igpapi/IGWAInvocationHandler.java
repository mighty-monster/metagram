package vp.igpapi;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * The InvocationHandler class for dynamic proxy, here we add functionality to every request call
 * Currently it is used to save the internal state, every @counterLimit calls, we might call the API
 * thousands of times and it`s not wise to save the internal state of class in every call
 * till now no problem was faced by loading previous version of internal stat and proceed using
 * the API, even after hundreds of new calls
 */
public class IGWAInvocationHandler implements InvocationHandler
{
    private IGWABase IGWA;

    public IGWAInvocationHandler(IGWABase IGWA)
    {
        this.IGWA = IGWA;
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        Object result = null;

        try
        {
            result = method.invoke(IGWA, args);

            IGWA.call_counter++;

            if (IGWA.call_counter > IGWA.counterLimit)
            {
                IGWA.save();
            }
        }
        catch (InvocationTargetException e)
        {
            if (e.getTargetException() instanceof IGWAException)
            {
                IGWAException targetException = (IGWAException)e.getTargetException();

                //TODO Implement refreshing rollout_hash
                if (targetException.code != 403)
                {

                }

            }

            throw e.getTargetException();
        }
        return result;
    }
}
