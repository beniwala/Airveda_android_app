package in.airveda.sensors.dataview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.FillFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.LineDataProvider;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

import cz.msebera.android.httpclient.Header;
import in.airveda.sensors.airveda.R;
import in.airveda.sensors.data.DataReadings;
import in.airveda.sensors.devicecommunication.ConfigurationCommandMode;
import in.airveda.sensors.utils.AirVedaBackendClient;

public class CurrentDataViewActivity extends AppCompatActivity implements OnChartValueSelectedListener {

    View mProgressView;
    View mDisplayArea;
    DataSyncTask mDataSyncTask;

    private PieChart mPieChart;
    private LineChart mLineChart;
    private Typeface tf;

    private AQIConversions mAQIConverter;

    private TextView mAdvisoryTitle;
    private TextView mAdvisoryText;

    Handler handler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_data_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle extras = getIntent().getExtras();
        String feedname = null;
        if(extras !=null) {
            feedname = extras.getString("feedname");
        }else{
            feedname = "airveda";
        }

        mAQIConverter = new AQIConversions();

        mProgressView = findViewById(R.id.fetch_progress);
        mDisplayArea =  findViewById(R.id.graphDisplayArea);
        mDisplayArea.setVisibility(View.GONE);

        mAdvisoryTitle = (TextView)findViewById(R.id.title_advisory);
        mAdvisoryText = (TextView)findViewById(R.id.text_advisory);
        mAdvisoryText.setSelected(true);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_help);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(getBaseContext(), AQIReferenceActivity.class);
//                startActivity(intent);
//            }
//        });
//
//        TextView details = (TextView) findViewById(R.id.open_detail_graph);
//        details.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(getBaseContext(), DataDetailsViewActivityNew.class);
//                startActivity(intent);
//
//            }
//        });

        final Runnable r = new Runnable() {
            public void run() {
                startDataSyncTask("airveda", false);
                handler.postDelayed(this, 30000);
            }
        };

        handler.postDelayed(r, 30000);
        startDataSyncTask("airveda", true);
    }

    public void startDataSyncTask(String feedname, boolean showProgress){
        if(mDataSyncTask == null) {
            mDataSyncTask = new DataSyncTask(feedname);
            mDataSyncTask.fetchData();
            showProgress(showProgress);
        }
    }
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mDisplayArea.setVisibility(show ? View.GONE : View.VISIBLE);
            mDisplayArea.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mDisplayArea.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mDisplayArea.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void showLineChart(ArrayList<DataReadings> dataList){
        mLineChart = (LineChart) findViewById(R.id.fetchedLineChart);
        mLineChart.setViewPortOffsets(0, 20, 0, 0);
        mLineChart.setBackgroundColor(Color.rgb(104, 241, 175));
//        mLineChart.setBackgroundColor(Color.rgb(0, 0, 0));

        // no description text
        mLineChart.setDescription("");

        // enable touch gestures
        mLineChart.setTouchEnabled(true);

        // enable scaling and dragging
        mLineChart.setDragEnabled(true);
        mLineChart.setScaleEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        mLineChart.setPinchZoom(false);

        mLineChart.setDrawGridBackground(false);

        tf = Typeface.createFromAsset(getAssets(), "OpenSans-Regular.ttf");

        XAxis x = mLineChart.getXAxis();
        x.setEnabled(false);

        YAxis y = mLineChart.getAxisLeft();
        y.setTypeface(tf);
        y.setLabelCount(6, false);
        y.setStartAtZero(true);
//        y.setAxisMaxValue(500);
        y.setTextColor(Color.WHITE);
        y.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        y.setDrawGridLines(false);
        y.setAxisLineColor(Color.WHITE);

        mLineChart.getAxisRight().setEnabled(false);

        // add data
        setLineChartData(dataList);


//        YAxis leftAxis = mLineChart.getAxisLeft();
//
//        for(int i = 0; i < mAQIConverter.AQI_LEVELS; i++) {
//            LimitLine ll = new LimitLine(mAQIConverter.AQI_PM25_RANGE[i], mAQIConverter.AQI_STATUS[i]);
//            ll.setLineColor(mAQIConverter.AQI_COLOR[i]);
//            ll.setLineWidth(2f);
//            ll.setTextColor(mAQIConverter.AQI_COLOR[i]);
//            ll.setTextSize(8f);
//            leftAxis.addLimitLine(ll);
//        }

        mLineChart.getLegend().setEnabled(false);

        mLineChart.animateXY(2000, 2000);
        mLineChart.setOnChartValueSelectedListener(this);

        // dont forget to refresh the drawing
        mLineChart.invalidate();
    }

    private void showPieChart(int pmValue, String dateTime){
        mPieChart = (PieChart) findViewById(R.id.fetchedDataChart);
//        mPieChart.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent i = new Intent(CurrentDataViewActivity.this, DataUnderstandingActivity.class);
//                startActivity(i);
//            }
//        });

        mPieChart.setUsePercentValues(true);
        mPieChart.setDescription("");
        mPieChart.setExtraOffsets(5, 10, 5, 5);

        mPieChart.setDragDecelerationFrictionCoef(0.95f);

        tf = Typeface.createFromAsset(getAssets(), "OpenSans-Regular.ttf");

        mPieChart.setCenterTextTypeface(Typeface.createFromAsset(getAssets(), "OpenSans-Light.ttf"));
        mPieChart.setCenterText(generateCenterSpannableText(pmValue, mAQIConverter.getStatus(pmValue)));

        mPieChart.setDrawHoleEnabled(true);
        mPieChart.setHoleColorTransparent(false);

        mPieChart.setTransparentCircleColor(Color.WHITE);
        mPieChart.setTransparentCircleAlpha(110);

        mPieChart.setHoleRadius(58f);
        mPieChart.setTransparentCircleRadius(61f);

        mPieChart.setDrawCenterText(true);

        mPieChart.setRotationAngle(90);

        mPieChart.setRotationEnabled(false);
        mPieChart.setHighlightPerTapEnabled(false);

        mPieChart.setDrawSliceText(false);
        mPieChart.setTouchEnabled(false);

        setPieChartData(pmValue, dateTime);

        for (DataSet<?> set : mPieChart.getData().getDataSets())
            set.setDrawValues(!set.isDrawValuesEnabled());



        //mPieChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);

    }


    private void setLineChartData(ArrayList<DataReadings> dataList)  {

        SimpleDateFormat sdf = new SimpleDateFormat(("MMM dd, HH:mm"));
        ArrayList<String> xVals = new ArrayList<String>();
        ArrayList<Entry> vals1 = new ArrayList<Entry>();
        ArrayList<Entry> vals2 = new ArrayList<Entry>();
        Random r = new Random(System.currentTimeMillis());
        for (int i = 0; i < dataList.size(); i++) {
            String timeXval = sdf.format(dataList.get(i).capture_date);
            xVals.add(timeXval);
            Entry e = new Entry(dataList.get(i).pollutant_value,i);
            e.setData(timeXval);
            vals1.add(e);
//            vals2.add(new Entry(dataList.get(i).pm_25 + (15 - r.nextInt(30)),i));
        }


        // create a dataset and give it a type
        LineDataSet set1 = new LineDataSet(vals1, "DataSet 1");
        set1.setDrawCubic(true);
        set1.setCubicIntensity(0.2f);
        //set1.setDrawFilled(true);
        set1.setDrawCircles(false);
        set1.setLineWidth(1.8f);
        set1.setCircleSize(4f);
        set1.setCircleColor(Color.WHITE);
        set1.setHighLightColor(Color.rgb(244, 117, 117));
        set1.setColor(Color.WHITE);
        set1.setFillColor(Color.WHITE);
        set1.setFillAlpha(100);
        set1.setDrawHorizontalHighlightIndicator(false);
        set1.setFillFormatter(new FillFormatter() {
            @Override
            public float getFillLinePosition(LineDataSet dataSet, LineDataProvider dataProvider) {
                return -10;
            }
        });

//        LineDataSet set2 = new LineDataSet(vals2, "DataSet 1");
//        set2.setDrawCubic(true);
//        set2.setCubicIntensity(0.2f);
//        //set1.setDrawFilled(true);
//        set2.setDrawCircles(false);
//        set2.setLineWidth(1.8f);
//        set2.setCircleSize(4f);
//        set2.setCircleColor(Color.WHITE);
//        set2.setHighLightColor(Color.rgb(244, 117, 117));
//        set2.setColor(Color.WHITE);
//        set2.setFillColor(Color.WHITE);
//        set2.setFillAlpha(100);
//        set2.setDrawHorizontalHighlightIndicator(false);
//        set2.setFillFormatter(new FillFormatter() {
//            @Override
//            public float getFillLinePosition(LineDataSet dataSet, LineDataProvider dataProvider) {
//                return -10;
//            }
//        });

        ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
        dataSets.add(set1);
//        dataSets.add(set2);

        // create a data object with the datasets
        LineData data = new LineData(xVals, dataSets);
        data.setValueTypeface(tf);
        data.setValueTextSize(9f);
        data.setDrawValues(false);

        // set data
        mLineChart.setData(data);
    }

    private void updateAdvisory(int pmValue){
        mAdvisoryText.setText("                 " + AQIConversions.getAdvisory(pmValue));
        mAdvisoryTitle.setTextColor(AQIConversions.getColor(pmValue));
        mAdvisoryText.setSelected(true);

    }

    private void setPieChartData(int pmValue, String dateTime) {
        ArrayList<Entry> yVals1 = new ArrayList<Entry>();
        yVals1.add(new Entry((float) 90, 0));

        ArrayList<String> xVals = new ArrayList<String>();
//        xVals.add(new String("PM 2.5"));
        xVals.add(dateTime);
        PieDataSet dataSet = new PieDataSet(yVals1, " ->  PM 2.5 (ug/m3)");
        dataSet.setSliceSpace(2f);
        dataSet.setSelectionShift(5f);


        ArrayList<Integer> colors = new ArrayList<Integer>();

//        for (int c : ColorTemplate.LIBERTY_COLORS)
//            colors.add(c);

        colors.add(mAQIConverter.getColor(pmValue));
        mPieChart.setHoleColor(mAQIConverter.getColor(pmValue));

        dataSet.setColors(colors);

        PieData data = new PieData(xVals, dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);
        data.setValueTypeface(tf);
        mPieChart.setData(data);

        // undo all highlights
        mPieChart.highlightValues(null);

        mPieChart.invalidate();
    }

    private SpannableString generateCenterSpannableText(int number, String status) {
        String msg = number + "\n\n" + status;
        if(number == -1){
            msg = "NA" + "\n\n" + status;
        }
        int index = msg.indexOf("\n\n");
        SpannableString s = new SpannableString(msg);
        s.setSpan(new StyleSpan(Typeface.BOLD), 0, index , 0);
        s.setSpan(new RelativeSizeSpan(3.5f), 0, index, 0);
        s.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), index+2, s.length(), 0);
        s.setSpan(new RelativeSizeSpan(2f), index+2, s.length(), 0);
        s.setSpan(new ForegroundColorSpan(Color.BLACK), index+2, s.length(), 0);
        return s;
    }

    public void onValueSelected(Entry e, int dataSetIndex, Highlight h){
        float pm25Value = e.getVal();
        String xValue = (String)e.getData();
        showPieChart((int)pm25Value, xValue);
        updateAdvisory((int) pm25Value);


    }

    public void onNothingSelected(){

    }

    public class DataSyncTask extends AsyncHttpResponseHandler {

        int mLatestPMValue = -1;
        String mLatestPMValueDateTime = "";
        ArrayList<DataReadings> mDataList = null;
        String mFeedName;
        boolean mOldDataType = true;

        DataSyncTask(String feedname) {
            this.mFeedName = feedname;
        }

        public void fetchData(){
            RequestParams r = new RequestParams();
 //           int minSinceMidnight = getMinutesSinceMidnight();
            int minSinceMidnight = getMinutesSinceMidnightGMT();
            r.put("time_gap", "" +  3000);

            switch (mFeedName){
                case "delhi":
                    r.put("device_choice", "delhi_mm");
                    r.put("loc", "Bedroom");
                    r.put("sensor", "PM2.5");
                    AirVedaBackendClient.setAuth("namita", "hello12");

//                    http://52.27.53.140/core/plot_view/?device_choice=1003&time_gap=60&loc=Bedroom&sensor=PM2.5
                    AirVedaBackendClient.getByURL("http://52.27.53.140/core/data_readings/", r, this);
                    mOldDataType = true;
                    break;
                case "airveda":
                    r.put("device_choice","delhi_mm");
                    r.put("loc", "Bedroom");
                    r.put("sensor", "PM2.5");
                    AirVedaBackendClient.setAuth("namita", "hello12");
                    AirVedaBackendClient.getByURL("http://52.27.53.140/core/data_readings_avg/", r, this);
                    mOldDataType = true;
                    break;
                default:
                    AirVedaBackendClient.clearCredentials();
                    AirVedaBackendClient.get("http://sashooj.me:8000/data/cityreadings/", r, this);
                    mOldDataType = false;
                    break;
            }
//            if(mFeedName != null){
//                r.put("city", mFeedName);
//                AirVedaBackendClient.get("/data/cityreadings/", r, this);
//            }else {
//                AirVedaBackendClient.get("/data/devicereadings/", r, this);
//            }
        }

        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            showProgress(false);
            if(mOldDataType){
                if(dataParser_avg(new String(responseBody))) {
                    showPieChart(mLatestPMValue, mLatestPMValueDateTime);
                    showLineChart(mDataList);
                    updateAdvisory(mLatestPMValue);
                }

            }else {
                if (dataParser(new String(responseBody))) {
                    showPieChart(mLatestPMValue, mLatestPMValueDateTime);
                    showLineChart(mDataList);
                    updateAdvisory(mLatestPMValue);
                }
            }
            mDataSyncTask = null;
        }

        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            showProgress(false);
            mDataSyncTask = null;
        }

        private boolean dataParser(String json) {

            DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            ArrayList<DataReadings> listDR_pm25 = new ArrayList<DataReadings>();

            try {
                JSONArray jsonArray = new JSONArray(json);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject o = jsonArray.getJSONObject(i);
                    DataReadings r = new DataReadings();
                    r.pollutant_type = o.getString("pollutant_type");
                    r.pollutant_value = (float)o.getDouble("pollutant_value");
                    r.pollutant_unit = o.getString("pollutant_unit");
                    String datestr = o.getString("capture_datetime");
                    r.capture_date = sdf.parse(datestr);
                    if(r.pollutant_type.compareTo("PM2.5") == 0) {
                        listDR_pm25.add(r);
                    }
                }
                Collections.sort(listDR_pm25, new DataReadings());
                DataReadings latestRecord = listDR_pm25.get(listDR_pm25.size() - 1);
                latestRecord.pollutant_value = latestRecord.pollutant_value + 200;
                mLatestPMValue = (int)latestRecord.pollutant_value;
                SimpleDateFormat sdfTime = new SimpleDateFormat(("MMM dd, HH:mm"));
                mLatestPMValueDateTime = sdfTime.format(latestRecord.capture_date);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            mDataList = listDR_pm25;
            return true;
        }

        private boolean dataParserOld(String json){

            DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            ArrayList<DataReadings> listDR = new ArrayList<DataReadings>();

            try {
                JSONArray jsonArray = new JSONArray(json);
                for(int i = 0; i < jsonArray.length(); i++){
                    JSONObject o = jsonArray.getJSONObject(i);
                    double pm25 = o.getDouble("pm_25");
                    String datestr = o.getString("created_dt");
                    DataReadings dr = new DataReadings();
                    dr.pollutant_value = (int) pm25;
                    dr.capture_date = format.parse(datestr);
                    listDR.add(dr);
                }
                Collections.sort(listDR,new DataReadings());
                DataReadings latestRecord = listDR.get(listDR.size() - 1);
                mLatestPMValue = (int)latestRecord.pollutant_value;
                SimpleDateFormat sdfTime = new SimpleDateFormat(("MMM dd, HH:mm"));
                mLatestPMValueDateTime = sdfTime.format(latestRecord.capture_date);
            }catch (Exception e){
                e.printStackTrace();
                return false;
            }
            mDataList = listDR;
            return true;
        }

        private boolean dataParser_avg(String json){

            DateFormat format = new SimpleDateFormat("yyyyMMddHHmm");
            ArrayList<DataReadings> listDR = new ArrayList<DataReadings>();

            try {
                Gson gson = new GsonBuilder().create();
                Type listType = new TypeToken<ArrayList<TempDataRecord>>() {
                }.getType();
                json = json.substring(1,json.length() - 1).replace("\\","");

                List<TempDataRecord> classList = gson.fromJson(json, listType);
//                TempDataRecord t = gson.fromJson(json, TempDataRecord.class);

                //JSONArray jsonArray = new JSONArray(json);
                for(int i = 0; i < classList.size(); i++){
                    //JSONObject o = jsonArray.getJSONObject(i);
                    TempDataRecord o = classList.get(i);
                    double pm25 = o.a;
                    String datestr = o.t;//getString("t");
                    DataReadings dr = new DataReadings();
                    dr.pollutant_value = (int) pm25;
                    Date date = format.parse(datestr.replace(".",""));
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(date);
                    cal.add(Calendar.HOUR_OF_DAY, 5);
                    cal.add(Calendar.MINUTE, 30);
                    dr.capture_date = cal.getTime();
                    listDR.add(dr);
                }
                Collections.sort(listDR,new DataReadings());
                DataReadings latestRecord = listDR.get(listDR.size() - 1);
                mLatestPMValue = (int)latestRecord.pollutant_value;
                SimpleDateFormat sdfTime = new SimpleDateFormat(("MMM dd, HH:mm"));
                mLatestPMValueDateTime = sdfTime.format(latestRecord.capture_date);
            }catch (Exception e){
                e.printStackTrace();
                return false;
            }
            mDataList = listDR;
            return true;
        }




        private int getMinutesSinceMidnight(){
            Calendar c = Calendar.getInstance();
            long now = c.getTimeInMillis();
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            long passed = now - c.getTimeInMillis();
            return (int)(passed / 60000);
        }

        private int getMinutesSinceMidnightGMT(){
            Calendar c = Calendar.getInstance();
            long now = c.getTimeInMillis();
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            c.add(Calendar.HOUR_OF_DAY, -5);
            c.add(Calendar.MINUTE, -30);
            long passed = now - c.getTimeInMillis();
            return (int)(passed / 60000);
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_current_data_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_aqi_reference) {
            Intent intent = new Intent(getBaseContext(), AQIReferenceActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_device_config) {
            Intent intent = new Intent(getBaseContext(), ConfigurationCommandMode.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    class TempDataRecord{
        double a;
        String t;
    }
}
