package za.co.victoriatuition.booking;

import android.app.Activity;
import org.json.JSONObject;
import java.io.*;
import java.net.*;

public class VtaApi {
    public interface Callback { void done(JSONObject result, Exception error); }
    private static final String URL="https://xrmljvdyxqyegclkazei.supabase.co/functions/v1/vta-api";
    private final Activity activity;
    private String token="";

    public VtaApi(Activity activity){this.activity=activity;}
    public void setToken(String token){this.token=token==null?"":token;}
    public String getToken(){return token;}

    public void post(JSONObject payload, boolean auth, Callback cb){
        new Thread(()->{
            HttpURLConnection c=null;
            try{
                c=(HttpURLConnection)new URL(URL).openConnection();
                c.setRequestMethod("POST");
                c.setDoOutput(true);
                c.setConnectTimeout(15000);
                c.setReadTimeout(30000);
                c.setRequestProperty("Content-Type","application/json");
                if(auth&&!token.isEmpty()) c.setRequestProperty("Authorization","Bearer "+token);
                try(OutputStream os=c.getOutputStream()){os.write(payload.toString().getBytes("UTF-8"));}
                int code=c.getResponseCode();
                InputStream is=code>=200&&code<300?c.getInputStream():c.getErrorStream();
                String text=readAll(is);
                JSONObject result=text.isEmpty()?new JSONObject():new JSONObject(text);
                if(code<200||code>=300)throw new IOException(result.optString("error","Request failed"));
                activity.runOnUiThread(()->cb.done(result,null));
            }catch(Exception e){
                activity.runOnUiThread(()->cb.done(null,e));
            }finally{if(c!=null)c.disconnect();}
        }).start();
    }

    private static String readAll(InputStream is)throws Exception{
        if(is==null)return "";
        BufferedReader br=new BufferedReader(new InputStreamReader(is,"UTF-8"));
        StringBuilder sb=new StringBuilder(); String line;
        while((line=br.readLine())!=null)sb.append(line);
        return sb.toString();
    }
}
