package vp.igpapi;

/**
 * For better error handling, this exception class is used by library
 */
public class IGWAException extends Exception
{
    final static public int not_logged_in = -1;
    final static public int wrong_username = -2;
    final static public int wrong_password = -3;
    final static public int not_authorized = -4;


    final static public int username_password_needed = -11;
    final static public int could_not_find_session_data = -12;

    final static public int could_not_find_csrftoken = -101;

    final static public int invalid_comment_length = -1001;
    final static public int invalid_comment_uppercase = -1002;
    final static public int media_not_found = -1003;

    final static public int http_to_many_request = 429;
    final static public int http_not_found = 404;

    public IGWAException(int code, String message)
    {
        this.code = code;
        this.message = message;

    }

    public IGWAException(int code, String message, String respond)
    {
        this.code = code;
        this.message = message;
        this.respond = respond;

    }

    public int code;
    public String message;
    public  String respond;

}
