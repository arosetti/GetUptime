package parad0x.get_uptime;

import android.app.Activity;
import android.os.Bundle;
import android.app.ActionBar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


public class TopActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        setContentView(R.layout.activity_top);
        TextView mTextViewInfo = findViewById(R.id.TextViewLog);

        String top = "";
        
        try {
            UptimeUtils up = new UptimeUtils(getBaseContext());
            String data[][] = up.db.getAll();
            int i = 1;

            for (String[] s : data) {
                top += "<h3>" + i + "°)  <font color='#4CAF50'>" + up.getTimeFromUnix(Long.valueOf(s[0])) +
                        " : </font></h3>" + up.time2human(Double.valueOf(s[1]), false) + "<br/><br/>";
                i++;
            }
        } catch (Exception e) {
            top = "<h3>error loading best results!</h3>";
        }
        
        mTextViewInfo.setText(Html.fromHtml(top));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
