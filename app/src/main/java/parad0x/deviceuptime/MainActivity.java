package parad0x.deviceuptime;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Scanner;


public class MainActivity extends ActionBarActivity {
    public final String TAG = this.getClass().getSimpleName();
    private String startDate;

    private final int[] conv = {31536000, 2592000, 86400, 3600, 60, 0};
    private String[] name = null;
    private String[] names = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);

        name = new String[] {getString(R.string.time_y), getString(R.string.time_M),
                getString(R.string.time_d), getString(R.string.time_h),
                getString(R.string.time_m), getString(R.string.time_s)};
        names = new String[] {getString(R.string.timep_y), getString(R.string.timep_M),
                getString(R.string.timep_d), getString(R.string.timep_h),
                getString(R.string.timep_m), getString(R.string.timep_s)};

        startDate = getStartUptime();

        DatabaseHandler db = new DatabaseHandler(getApplicationContext());

        Thread t = new Thread() {

            @Override
            public void run() {
                try {
                    while (!isInterrupted())
                    {
                        Thread.sleep(80);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                TextView textViewUptimeTime = (TextView) findViewById(R.id.TextViewUptime);
                                String text = "<font color='#e1e1e1'>" + getString(R.string.msg_sup) +
                                              ":</font><br/><font color='#4CAF50'>" +
                                               startDate + "</font><br/><br/><br/>" +
                                               "<font color='#e1e1e1'>" + getString(R.string.msg_up) +
                                               ":</font><br/><font color='#4CAF50'>" +
                                               getUptime() + "</font>";
                                textViewUptimeTime.setText(Html.fromHtml(text), TextView.BufferType.SPANNABLE);
                            }
                        });
                    }
                }
                catch (InterruptedException e)
                {
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_exit) {
            //android.os.Process.killProcess(android.os.Process.myPid());
            //System.exit(0);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private String getStartUptime() {
        String uptime = getRawUptime();

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND, - (int) Float.parseFloat(uptime));

        return sdf.format(cal.getTime());
    }

    private String getUptime() {
        return time2human(Double.parseDouble(getRawUptime()));
    }

    private String getRawUptime() {
        String uptime = null;

        try {
            uptime = new Scanner(new FileInputStream("/proc/uptime")).next();
        }
        catch (Exception e) {
            uptime = "0";
        }

        return uptime;
    }

    private String time2human(double sec)
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

                time += "<br/>";
                sec -= conv[i] * val;
            }
        }

        return time;
    }
}
