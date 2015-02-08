package parad0x.get_uptime;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


public class TopActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_top);

        TextView mTextViewInfo = (TextView) findViewById(R.id.TextViewLog);

        /* Bundle extras = getIntent().getExtras();
        if(extras != null)
            str = extras.getString("tag"); */
        
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
