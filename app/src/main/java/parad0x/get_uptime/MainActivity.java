package parad0x.get_uptime;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;


public class MainActivity extends ActionBarActivity {
    public final String TAG = this.getClass().getSimpleName();

    DatabaseHandler db = null;
    
    private String s_startDate, s_best[] = null;
    private long startDate;

    private final int[] conv = {31536000, 2592000, 604800, 86400, 3600, 60, 0};
    private String[] name = null, names = null, names2 = null;

    volatile boolean stopped = false;
    
    @Override
    public void onPause(){
        super.onPause();
        stopped = true;
    }

    @Override
    public void onResume(){
        super.onResume();
        
        updateRecord();
        updateBest();

        stopped = false;
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    while (!stopped)
                    {
                        Thread.sleep(90);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                TextView textViewUptimeTime = (TextView) findViewById(R.id.TextViewUptime);
                                String text = "<font color='#e1e1e1'>" + getString(R.string.msg_sup) +
                                        ":</font><br/><font color='#4CAF50'>" +
                                        s_startDate + "</font><br/><br/>" +
                                        "<font color='#e1e1e1'>" + getString(R.string.msg_up) +
                                        ":</font><br/><font color='#4CAF50'>" +
                                        getUptime(true) + "</font><br/>" +
                                        "<font color='#e1e1e1'>" + getString(R.string.msg_best) +
                                        ":</font><br/><small color='#4CAF50'>" +
                                        getBestUptime() + "</small>";
                                textViewUptimeTime.setText(Html.fromHtml(text),
                                        TextView.BufferType.SPANNABLE);
                            }
                        });
                    }
                }
                catch (InterruptedException e)
                {
                    stopped = true;
                    Log.d(TAG, "thread interrupted");
                }
                catch (Exception e)
                {
                    Log.d(TAG, "error: " + e.getMessage());
                }
            }
        };

        t.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);

        name = new String[] {
                getString(R.string.time_y), getString(R.string.time_M),
                getString(R.string.time_w), getString(R.string.time_d),
                getString(R.string.time_h), getString(R.string.time_m),
                getString(R.string.time_s)};
        names = new String[] {
                getString(R.string.timep_y), getString(R.string.timep_M),
                getString(R.string.timep_w), getString(R.string.timep_d),
                getString(R.string.timep_h), getString(R.string.timep_m),
                getString(R.string.timep_s)};
        names2 = new String[] {"y", "m", "w", "d", "H", "M", "s" };

        try {
            db = new DatabaseHandler(getApplicationContext());
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

    private void updateBest() {
        try {
            s_best = db.getBest();
            Log.d(TAG, "BEST: " + s_best[0] + ": " + s_best[1] );
        } catch (Exception e) {}
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_share) {
            shareIntent(getUptime(false));
            return true;
        } else if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_about) {
            showAbout();
            return true;
        } else if (id == R.id.action_exit) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void shareIntent(String text) {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "This is my uptime!"); // device name
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }

    protected void showAbout() {
        View messageView = getLayoutInflater().inflate(R.layout.about_dialog, null, false);

        TextView textView = (TextView) messageView.findViewById(R.id.about_credits);
        int defaultColor = textView.getTextColors().getDefaultColor();
        textView.setTextColor(defaultColor);

        String version = "?";
        try {
            PackageInfo manager = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = manager.versionName;
        }
        catch (Exception e) { }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setTitle(getString(R.string.app_name) + "-" + version);
        builder.setView(messageView);
        builder.create();
        builder.show();
    }
    
    private String getBestUptime() {
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
                return getString(R.string.msg_this);
            }
            //String tobest = time2human(Math.abs(sub), false);
            
            return best_time + "<br/>" + run + "<br/>"; // -" + tobest;
        }
        catch (Exception e) {
            return "?";
        }
    }
    
    private long getUnixTime() {
        return  System.currentTimeMillis() / 1000L;
    }

    private String getTimeFromUnix(long time) {
        time = time * 1000L;
        Date date = new Date(time);
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
        return sdf.format(date);
    }
    
    private String getUptime(boolean html) {
        return time2human(getRawUptime(), html);
    }

    private Double getRawUptime() {
        String uptime = null;

        try {
            uptime = new Scanner(new FileInputStream("/proc/uptime")).next();
        }
        catch (Exception e) {
            uptime = "0";
        }

        return Double.parseDouble(uptime);
    }

    private String time2human(double sec, boolean html)
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
