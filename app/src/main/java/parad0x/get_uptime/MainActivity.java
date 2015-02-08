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


public class MainActivity extends ActionBarActivity {
    public final String TAG = this.getClass().getSimpleName();

    UptimeUtils up = null;
    
    volatile boolean stopped = false;
    
    @Override
    public void onPause() {
        super.onPause();
        stopped = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        
        up.updateRecord();
        up.updateBest();

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
                                        up.getStartDate() + "</font><br/><br/>" +
                                        "<font color='#e1e1e1'>" + getString(R.string.msg_up) +
                                        ":</font><br/><font color='#4CAF50'>" +
                                        up.getUptime(true) + "</font><br/>" +
                                        "<font color='#e1e1e1'>" + getString(R.string.msg_best) +
                                        ":</font><br/><small color='#4CAF50'>" +
                                        up.getBestUptime() + "</small>";
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

        up = new UptimeUtils(getBaseContext());
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
            shareIntent(up.getUptime(false));
            return true;
        } else if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_top) {
            Intent intent = new Intent(this, TopActivity.class);
            startActivity(intent);
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
}
