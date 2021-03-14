package vp.metagram.utils.instagram.executors.statistics.types;

import org.json.JSONException;
import org.json.JSONObject;




public class RankDecisionLimit
{
    public RankDecisionLimit(String values)
    {
        setValues(values);
    }

    public void setValues(String values)
    {
        try
        {
            JSONObject jsonObject = new JSONObject(values);

            try { postsNo_rankAPlusPlusLimit = jsonObject.getDouble("postsNo_rankAPlusPlusLimit"); }catch (Exception ex) {ex.printStackTrace();}
            try { postsNo_rankAPlusLimit = jsonObject.getDouble("postsNo_rankAPlusLimit"); }catch (Exception ex) {ex.printStackTrace();}
            try { postsNo_rankALimit = jsonObject.getDouble("postsNo_rankALimit"); }catch (Exception ex) {ex.printStackTrace();}
            try { postsNo_rankBLimit = jsonObject.getDouble("postsNo_rankBLimit"); }catch (Exception ex) {ex.printStackTrace();}

            try { alpha_rankAPlusPlusLimit = jsonObject.getDouble("alpha_rankAPlusPlusLimit"); }catch (Exception ex) {ex.printStackTrace();}
            try { alpha_rankAPlusLimit = jsonObject.getDouble("alpha_rankAPlusLimit"); }catch (Exception ex) {ex.printStackTrace();}
            try { alpha_rankALimit = jsonObject.getDouble("alpha_rankALimit"); }catch (Exception ex) {ex.printStackTrace();}
            try { alpha_rankBLimit = jsonObject.getDouble("alpha_rankBLimit"); }catch (Exception ex) {ex.printStackTrace();}

            try { meanEngagement_rankAPlusPlusLimit = jsonObject.getDouble("meanEngagement_rankAPlusPlusLimit"); }catch (Exception ex) {ex.printStackTrace();}
            try { meanEngagement_rankAPlusLimit = jsonObject.getDouble("meanEngagement_rankAPlusLimit"); }catch (Exception ex) {ex.printStackTrace();}
            try { meanEngagement_rankALimit = jsonObject.getDouble("meanEngagement_rankALimit"); }catch (Exception ex) {ex.printStackTrace();}
            try { meanEngagement_rankBLimit = jsonObject.getDouble("meanEngagement_rankBLimit"); }catch (Exception ex) {ex.printStackTrace();}
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

    }

    public String getValues()
    {
        JSONObject jsonObject = new JSONObject();

        try
        {
            jsonObject.put("postsNo_rankAPlusPlusLimit", postsNo_rankAPlusPlusLimit);
            jsonObject.put("postsNo_rankAPlusLimit", postsNo_rankAPlusLimit);
            jsonObject.put("postsNo_rankALimit", postsNo_rankALimit);
            jsonObject.put("postsNo_rankBLimit", postsNo_rankBLimit);

            jsonObject.put("alpha_rankAPlusPlusLimit", alpha_rankAPlusPlusLimit);
            jsonObject.put("alpha_rankAPlusLimit", alpha_rankAPlusLimit);
            jsonObject.put("alpha_rankALimit", alpha_rankALimit);
            jsonObject.put("alpha_rankBLimit", alpha_rankBLimit);

            jsonObject.put("meanEngagement_rankAPlusPlusLimit", meanEngagement_rankAPlusPlusLimit);
            jsonObject.put("meanEngagement_rankAPlusLimit", meanEngagement_rankAPlusLimit);
            jsonObject.put("meanEngagement_rankALimit", meanEngagement_rankALimit);
            jsonObject.put("meanEngagement_rankBLimit", meanEngagement_rankBLimit);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return jsonObject.toString().trim();
    }


    public double postsNo_rankAPlusPlusLimit;
    public double postsNo_rankAPlusLimit;
    public double postsNo_rankALimit;
    public double postsNo_rankBLimit;

    public double alpha_rankAPlusPlusLimit;
    public double alpha_rankAPlusLimit;
    public double alpha_rankALimit;
    public double alpha_rankBLimit;

    public double meanEngagement_rankAPlusPlusLimit;
    public double meanEngagement_rankAPlusLimit;
    public double meanEngagement_rankALimit;
    public double meanEngagement_rankBLimit;

}
