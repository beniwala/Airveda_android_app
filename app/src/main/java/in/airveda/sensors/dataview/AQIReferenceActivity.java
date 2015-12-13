package in.airveda.sensors.dataview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.webkit.WebView;

import in.airveda.sensors.airveda.R;


public class AQIReferenceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aqireference);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        WebView wv;
        wv = (WebView) findViewById(R.id.aqi_web_view);
        wv.loadUrl("file:///android_asset/AQITableSingapore.html");

    }

}
