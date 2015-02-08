package parad0x.get_uptime;


import android.content.Context;
import android.util.Log;

import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class UptimeUtils {
    public final String TAG = this.getClass().getSimpleName();
    
    Context ctx = null;
    DatabaseHandler db = null;
    
    private static String s_startDate, s_best[] = null;
    private static long startDate;

    private static final int[] conv = {31536000, 2592000, 604800, 86400, 3600, 60, 0};
    private static String[] name = null, names = null, names2 = null;
    
    public UptimeUtils(Context ctx) {
        this.ctx = ctx;
        
        name = new String[] {
                ctx.getString(R.string.time_y), ctx.getString(R.string.time_M),
                ctx.getString(R.string.time_w), ctx.getString(R.string.time_d),
                ctx.getString(R.string.time_h), ctx.getString(R.string.time_m),
                ctx.getString(R.string.time_s)};
        names = new String[] {
                ctx.getString(R.string.timep_y), ctx.getString(R.string.timep_M),
                ctx.getString(R.string.timep_w), ctx.getString(R.string.timep_d),
                ctx.getString(R.string.timep_h), ctx.getString(R.string.timep_m),
                ctx.getString(R.string.timep_s)};
        names2 = new String[] {"y", "m", "w", "d", "H", "M", "s" };

        try {
            db = new DatabaseHandler(ctx);
        } catch (Exception e) {}

        Double c = Math.floor(getUnixTime()) - Math.floor(getRawUptime());
        startDate = c.longValue();
        s_startDate = getTimeFromUnix(startDate);
    }

    public void updateRecord() {
        try {
            Double c = Math.floor(getRawUptime());
            long current = c.longValue();
            boolean sd = db.exists(startDate),
                    sd1 = db.exists(startDate + 1),
                    sd2 = db.exists(startDate - 1);

            if (!sd && !sd1 && !sd2) {
                db.add(startDate, current);
                Log.d(TAG, "ADDING NEW RECORD: " + startDate + ", " + current);
            }
            else {
                if (sd) {
                    db.update(startDate, current);
                    Log.d(TAG, "UPDATING RECORD: " + startDate + ", " + current);
                    db.remove(startDate + 1);
                    db.remove(startDate - 1);
                }
                else if (sd1) {
                    db.update(startDate + 1, current);
                    Log.d(TAG, "UPDATING RECORD: " + (startDate + 1) + ", " + current);
                    db.remove(startDate);
                    db.remove(startDate - 1);
                }
                else if (sd2) {
                    db.update(startDate - 1, current);
                    Log.d(TAG, "UPDATING RECORD: " + (startDate - 1) + ", " + current);
                    db.remove(startDate);
                    db.remove(startDate + 1);
                }
            }
        }
        catch (Exception e)
        {
            Log.e(TAG, "ERROR IN DB ACCESS: " + e.getMessage());
            //e.printStackTrace();
        }
    }

    public void updateBest() {
        try {
            s_best = db.getBest();
            Log.d(TAG, "BEST: " + s_best[0] + ": " + s_best[1] );
        } catch (Exception e) {}
    }
    
    public String getBestUptime() {
        try {
            String best_time = getTimeFromUnix(Integer.valueOf(s_best[0]));
            String run = time2human(Double.valueOf(s_best[1]), false);
            Double sub = getRawUptime() - Double.valueOf(s_best[1]);

            //if (best_time.equals(s_startDate))
            //    return getString(R.string.msg_this);

            if (sub > 0) {
                if (sub > 60) {
                    updateRecord();
                    updateBest();
                }
                return ctx.getString(R.string.msg_this);
            }
            //String tobest = time2human(Math.abs(sub), false);

            return best_time + "<br/>" + run + "<br/>"; // -" + tobest;
        }
        catch (Exception e) {
            return "?";
        }
    }

    public long getUnixTime() {
        return  System.currentTimeMillis() / 1000L;
    }

    public String getTimeFromUnix(long time) {
        time = time * 1000L;
        Date date = new Date(time);
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
        return sdf.format(date);
    }

    public String getUptime(boolean html) {
        return time2human(getRawUptime(), html);
    }
    
    public String getStartDate() { return s_startDate; }

    public Double getRawUptime() {
        String uptime = null;

        try {
            uptime = new Scanner(new FileInputStream("/proc/uptime")).next();
        }
        catch (Exception e) {
            uptime = "0";
        }

        return Double.parseDouble(uptime);
    }

    public String time2human(double sec, boolean html)
    {
        String time = "";
        int val = 0;

        if (sec < 0)
            return "";

        for(int i = 0; i < conv.length ; i++)
        {
            if (conv[i] != 0)
                val = (int) ((sec) / (double)conv[i]);
            if (val > 0 || conv[i] == 0) {
                if (conv[i] != 0) {
                    time += val + " ";
                    if (val > 1)
                        time += names[i];
                    else
                        time += name[i];
                }
                else {
                    time += String.format( "%.2f", sec ) + " ";
                    if (sec != 1)
                        time += names[i];
                    else
                        time += name[i];
                }

                if (html)
                    time += "<br/>";
                else
                    time += " ";
                sec -= conv[i] * val;
            }
        }

        return time;
    }
}
