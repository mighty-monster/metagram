package vp.metagram.ui.AccBrowser;

import android.content.Context;
import android.util.AttributeSet;

import de.hdodenhof.circleimageview.CircleImageView;

import vp.metagram.utils.instagram.types.UserFull;

import static vp.metagram.general.variables.metagramAgent;



public class AccImageView extends CircleImageView
{
    Context context;

    public AccImageView(Context context)
    {
        super(context);
        this.context = context;
    }

    public AccImageView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        this.context = context;
    }

    public AccImageView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        this.context = context;
    }

    public UserFull Refresh(String username)
    {

        UserFull userFull = null;
        try
        {
            userFull = metagramAgent.activeAgent.proxy.getUserInfo(username);

            userFull.updatePicURL();

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


        return userFull;
    }
}
