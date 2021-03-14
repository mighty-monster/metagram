package vp.tools.http;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import vp.metagram.utils.MetaServerException;

import static vp.metagram.general.variables.serverAddress;

public class iMetaProxy implements InvocationHandler
{
    private iMetaCom metaCom;

    public  iMetaProxy(iMetaCom metaCom)
    {
        this.metaCom = metaCom;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {

        Object result = null;

        try
        {
            result = method.invoke(metaCom,args);
        }
        catch (InvocationTargetException e)
        {

            if (e.getTargetException() instanceof MetaServerException )
            {
                throw e.getTargetException();
            }
            else
            {
                serverAddress.nextAddress();
                result = method.invoke(metaCom,args);
            }
        }

        return result;
    }
}
