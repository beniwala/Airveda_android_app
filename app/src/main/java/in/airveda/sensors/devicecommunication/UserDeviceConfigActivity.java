package in.airveda.sensors.devicecommunication;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import in.airveda.sensors.airveda.R;


public class UserDeviceConfigActivity extends AppCompatActivity implements TelnetCallBack{

    public static String SHARED_PREFERENCE_NAME = "AirvedaDeviceSimulatorPreferences";

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    SharedPreferences sharedPreferences;

    static TelnetManager tm;
    ArrayList<String> wifiNames;
    ArrayList<String> wifiPasswords;

    BasicConfigurationFragment    bfm;
    ConnectFragment               cfm;
    AdvancedConfigurationFragment afm;

    static Handler handler = new Handler();
    static int msgCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration_command_mode);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        sharedPreferences = getSharedPreferences(SHARED_PREFERENCE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor e = sharedPreferences.edit();
//        e.putString("IP", "192.168.43.96");
//        e.putInt("Port", 6789);
        e.putString("IP", "192.168.4.1");
        e.putInt("Port", 1336);
        e.commit();

        tm = new TelnetManager(this);

        android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(UserDeviceConfigActivity.this);

        alert.setTitle("Configuring Device");
        alert.setMessage("Please ensure your device is in CONFIG MODE!\n\n Connect to wifi \"Airveda\"");

        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });

        alert.show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_configuration_command_mode, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    if(bfm == null) {
                        bfm = new BasicConfigurationFragment();
                        bfm.sharedPref = sharedPreferences;
                        bfm.spEditor = sharedPreferences.edit();
                        return bfm;
                    }
                case 1:
                    if(cfm == null){
                        cfm = new ConnectFragment();
                        cfm.sharedPref = sharedPreferences;
                        cfm.spEditor = sharedPreferences.edit();
                        return cfm;
                    }
                case 2:
                    if(afm == null){
                        afm = new AdvancedConfigurationFragment();
                        return afm;
                    }
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Setup";
                case 1:
                    return "Connect";
                case 2:
                    return "Advanced";
            }
            return null;
        }
    }

    public static class BasicConfigurationFragment extends Fragment implements View.OnClickListener {

        EditText deviceIDEditText;
        public SharedPreferences sharedPref;
        public SharedPreferences.Editor spEditor;

        ListView listWifi;

        EditText wifiSSID;
        EditText wifiPASS;

//        Button addWifi;
//        Button manageWifi;

        Button button_startConfig;
        Button button_saveConfig;

        TextView status_text;
//        Button button_setDeviceID;

        Handler h = new Handler();
        ArrayList<String> wifiNamesCopy;
        ArrayList<String> wifiPasswordsCopy;
        ArrayAdapter<String> wifiDataAdapter;


        public static final int MANUAL = 0;

        public static final int PRE_CONNECT = 1;
        public static final int CONNECTED = 2;
        public static final int READ_DATA_WIFI = 3;
        public static final int READ_DEVICE_ID = 4;
        public static final int WRITING_DATA = 5;

        public int status = MANUAL;


        public int writing_mode = wmWIFI;
        public static final int wmWIFI = 0;
        public static final int wmDEVICE = 2;
        public static final int wmDATETIME = 3;
        public static final int wmALLDONE = 4;



        public BasicConfigurationFragment(){

        }


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_configuration_command_mode_basic, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText("Basic Configuration");

            status_text = (TextView) rootView.findViewById(R.id.text_status);
            deviceIDEditText = (EditText)rootView.findViewById(R.id.device_id_edittext);

//            manageWifi = (Button) rootView.findViewById(R.id.button_wifi);
//            manageWifi.setOnClickListener(this);
//            manageWifi.setVisibility(View.GONE);

//            addWifi = (Button) rootView.findViewById(R.id.button_add_wifi);
//            addWifi.setOnClickListener(this);
//            addWifi.setVisibility(View.GONE);

//            button_setDeviceID = (Button) rootView.findViewById(R.id.device_id_button);
//            button_setDeviceID.setOnClickListener(this);

            button_startConfig = (Button) rootView.findViewById(R.id.button_device_configuration);
            button_startConfig.setOnClickListener(this);

            button_saveConfig = (Button) rootView.findViewById(R.id.button_save_configuration);
            button_saveConfig.setOnClickListener(this);
            button_saveConfig.setVisibility(View.GONE);

            listWifi = (ListView)rootView.findViewById(R.id.wifi_list);
            wifiSSID = (EditText)rootView.findViewById((R.id.wifi_ssid_edittext));
            wifiPASS = (EditText)rootView.findViewById((R.id.wifi_password_edittext));


            return rootView;
        }

        public void onClick(View view){
            switch (view.getId()) {
//                case R.id.device_id_button: {
//                    String deviceid = deviceIDEditText.getText().toString();
//                    String msg = "set_DEVICEID::" + deviceid;
//                    tm.sendMessage(msg);
//                }
//                break;
//                case R.id.button_wifi: {
//                    String msg = "get_WIFILIST";
//                    tm.sendMessage(msg);
//                    listWifi.setVisibility(View.VISIBLE);
//                    addWifi.setVisibility(View.VISIBLE);
//                }
//                break;
//                case R.id.button_add_wifi: {
//                    if(wifiPASS.getVisibility() == View.VISIBLE
//                            && wifiSSID.getVisibility() == View.VISIBLE){
//                        String msg = getWifiListToSend();
//                        tm.sendMessage(msg);
//                    }
//                    wifiSSID.setVisibility(View.VISIBLE);
//                    wifiPASS.setVisibility(View.VISIBLE);
//                }
//                break;
                case R.id.button_device_configuration:{
                    if(status == MANUAL) {
                        status = PRE_CONNECT;
                        configureDevice();
                    }
                }
                break;
                case R.id.button_save_configuration:{
                    if(status != MANUAL) {
                        status = WRITING_DATA;
                        configureDevice();
                    }else{
                        if(button_saveConfig.getText().toString().compareTo("Done") == 0){
                            getActivity().finish();
                        }
                    }
                }
                break;
            }
        }

        private String getWifiListToSend(){
            String msg = "";
            String newWifi = wifiSSID.getText().toString().trim();
            String newPass = wifiPASS.getText().toString().trim();
            int count = 0;
            boolean found = false;
            if(wifiNamesCopy == null){
                updateWifiList(new ArrayList<String>(),new ArrayList<String>());
            }
            if(wifiNamesCopy != null && wifiPasswordsCopy != null){
                for(int i = 0; i < wifiNamesCopy.size(); i++) {
                    msg += "ssid=" + wifiNamesCopy.get(i) + ";";
                    msg += "pass=" + wifiPasswordsCopy.get(i) + ";";
                    count++;
                    if(newWifi.compareTo(wifiNamesCopy.get(i)) == 0){
                        found = true;
                    }
                }
                if(newWifi.compareTo("") != 0 && !found){
                    wifiNamesCopy.add(newWifi);
                    wifiPasswordsCopy.add(newPass);
                    wifiDataAdapter.notifyDataSetChanged();
                }
            }
            for(int i=count; i < 5; i++){
                msg += "ssid=NODEF;";
                msg += "pass=NODEF;";
            }
            return msg;
        }

        public void configureDevice(){
            switch (status){
                case PRE_CONNECT:
//                    String ip = sharedPref.getString("IP","192.168.43.96");
//                    int port = sharedPref.getInt("Port",6789);
                    String ip = sharedPref.getString("IP","192.168.4.1");
                    int port = sharedPref.getInt("Port",1336);
                    tm.connect(ip, port);
                    break;
                case CONNECTED:
                    tm.sendMessage("get_WIFILIST");
                    h.post(new Runnable() {
                        @Override
                        public void run() {
                            listWifi.setVisibility(View.VISIBLE);
                            wifiSSID.setVisibility(View.VISIBLE);
                            wifiPASS.setVisibility(View.VISIBLE);
                        }
                    });
//                    addWifi.setVisibility(View.VISIBLE);
                    break;
                case READ_DATA_WIFI:
                    tm.sendMessage("get_DEVICEID");
                    break;
                case READ_DEVICE_ID:{
                    h.post(new Runnable() {
                        @Override
                        public void run() {
                            button_startConfig.setVisibility(View.GONE);
                            deviceIDEditText.setVisibility(View.VISIBLE);
                            button_saveConfig.setVisibility(View.VISIBLE);
                        }
                    });
                }
                    break;
                case WRITING_DATA:
                    switch (writing_mode){
                    case wmWIFI:
                        msgCount = 0;
                        getWifiListToSend();
                        final Runnable r = new Runnable() {
                            public void run() {
                                if(msgCount == 5) return;
                                String msg = "";
                                String ssidSring = "ssid=NODEF;pass=NODEF;";
                                if(wifiNamesCopy!= null && msgCount < wifiNamesCopy.size()){
                                    ssidSring = "ssid=" + wifiNamesCopy.get(msgCount) + ";";
                                    ssidSring += "pass=" + wifiPasswordsCopy.get(msgCount) + ";";
                                }
                                msg = "set_WIFI" + (msgCount  + 1) + "::"+ ssidSring;
                                tm.sendMessage(msg);
                                msgCount++;
                                handler.postDelayed(this, 500);
                            }
                        };

                        handler.postDelayed(r, 500);
//                        tm.sendMessage("set_WIFILIST::" + getWifiListToSend());
                        break;
                    case wmDEVICE:
                        tm.sendMessage("set_DEVICEID::" + deviceIDEditText.getText().toString().trim() + ";");
                        break;
                    case wmDATETIME:
                        String msg = "";
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        msg += sdf.format(new Date(System.currentTimeMillis()));
                        tm.sendMessage("set_DATETIME::" + msg);
                        break;
                }
                break;
            }
        }

        public void updateWifiList(final ArrayList<String> wifiNames, ArrayList<String> wifiPasswords){
            wifiNamesCopy = wifiNames;
            wifiPasswordsCopy = wifiPasswords;
            wifiDataAdapter = new ArrayAdapter<String>(getContext(),
                    android.R.layout.simple_list_item_1, android.R.id.text1, wifiNamesCopy);
            h.post(new Runnable() {
                @Override
                public void run() {
                    listWifi.setVisibility(View.VISIBLE);
                    listWifi.setAdapter(wifiDataAdapter);
                    listWifi.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                        @Override
                        public void onItemClick(AdapterView<?> parent, View view,
                                                int position, long id) {
                            wifiDataAdapter.remove(wifiNamesCopy.get(position));
                            wifiPasswordsCopy.remove(position);
                            wifiDataAdapter.notifyDataSetChanged();

//                            String msg = "set_WIFILIST::";
//                            if (wifiNamesCopy != null && wifiPasswordsCopy != null) {
//                                for (int i = 0; i < wifiNamesCopy.size(); i++) {
//                                    msg += "ssid=" + wifiNamesCopy.get(i) + ";";
//                                    msg += "pass=" + wifiPasswordsCopy.get(i) + ";";
//                                }
//                            }
//                            tm.sendMessage(msg);

                        }

                    });
                }
            });
        }

        public void updateDeviceID(String d) {
            final String deviceidtext = d;
            if(deviceidtext != null && deviceidtext.compareTo("") != 0) {
                spEditor.putString("DeviceID", deviceidtext.trim());
                spEditor.commit();
            }

            h.post(new Runnable() {
                @Override
                public void run() {
                    deviceIDEditText.setText(deviceidtext);
                }
            });
        }

        public void setStatus(String msg){
            final String message = msg;
            h.post(new Runnable() {
                @Override
                public void run() {
                    status_text.setVisibility(View.VISIBLE);
                    status_text.setText(message);
                    switch (writing_mode) {
                        case wmWIFI:
                            break;
                        case wmDEVICE:
                            listWifi.setVisibility(View.GONE);
                            wifiSSID.setVisibility(View.GONE);
                            wifiPASS.setVisibility(View.GONE);
                            break;
                        case wmDATETIME:
                            deviceIDEditText.setVisibility(View.GONE);
                            break;
                        case wmALLDONE:
                            button_saveConfig.setText("Done");
                            status = MANUAL;
                            break;
                    }
                }
            });

        }

    }

    public static class AdvancedConfigurationFragment extends Fragment implements View.OnClickListener {
        Button button_reset;

        Button button_server_url;
        EditText edit_server_url;

        Button button_update_freq;
        EditText edit_update_freq;
        Button setDateTime;



        public AdvancedConfigurationFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_configuration_command_mode_advance, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText("Advanced Configuration");

            button_reset = (Button) rootView.findViewById(R.id.reset_button);
            button_reset.setOnClickListener(this);

            setDateTime = (Button) rootView.findViewById(R.id.button_date_time);
            setDateTime.setOnClickListener(this);

            edit_server_url = (EditText) rootView.findViewById(R.id.serverurl_edittext);
            button_server_url = (Button) rootView.findViewById(R.id.serverurl_button);
            button_server_url.setOnClickListener(this);

            edit_update_freq = (EditText) rootView.findViewById(R.id.update_freq_edittext);
            button_update_freq = (Button) rootView.findViewById(R.id.button_updateFreq);
            button_update_freq.setOnClickListener(this);

            return rootView;
        }

        public void onClick(View v){
            switch (v.getId()){
                case R.id.button_updateFreq: {
                    String updateFreq = edit_update_freq.getText().toString();
                    tm.sendMessage("set_UPDTFREQ::" + updateFreq);
                    break;
                }
                case R.id.serverurl_button: {
                    String serverUrl = edit_server_url.getText().toString();
                    tm.sendMessage("set_SERVERURL::" + serverUrl);
                    break;
                }
                case R.id.reset_button: {
                    AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                    alert.setTitle("Alert!!");
                    alert.setMessage("Are you sure you want to reset?");
                    alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            tm.sendMessage("reset");
                        }
                    });
                    alert.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            dialog.dismiss();
                        }
                    });

                    alert.show();
                }
                break;
                case R.id.button_date_time: {
                    String msg = "set_DATETIME::";
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    msg += sdf.format(new Date(System.currentTimeMillis()));
                    tm.sendMessage(msg);
                }
                break;

            }
        }
    }

    public static class ConnectFragment extends Fragment implements View.OnClickListener {

        TextView status;
        Button connect_button;
        EditText host_edit_text;
        EditText port_edit_text;

        public SharedPreferences sharedPref;
        public SharedPreferences.Editor spEditor;


        public ConnectFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_configuration_command_mode_connect, container, false);
            status = (TextView) rootView.findViewById(R.id.section_label_connect);
            connect_button = (Button) rootView.findViewById(R.id.button_connect_to_device);
            connect_button.setOnClickListener(this);

            host_edit_text = (EditText) rootView.findViewById(R.id.device_host_ip);
            port_edit_text = (EditText) rootView.findViewById(R.id.device_host_port);

            status = (TextView) rootView.findViewById((R.id.section_label));
            return rootView;
        }

        public void onClick(View view){
            if(view.getId() == R.id.button_connect_to_device){
                Snackbar.make(view, "Connecting ...", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                String ip = host_edit_text.getText().toString();
                int port = new Integer(port_edit_text.getText().toString()).intValue();
                spEditor.putString("IP", ip);
                spEditor.putInt("Port", port);
                spEditor.commit();
                tm.connect(ip, port);
            }
        }


    }


    public void onConnect(){
        if(bfm.status == BasicConfigurationFragment.PRE_CONNECT) {
            bfm.status = BasicConfigurationFragment.CONNECTED;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    bfm.configureDevice();
                }
            });
        }
        Snackbar.make(mViewPager, "Connected to Device", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    public void onMessageReceive(String msg){
        Snackbar.make(mViewPager, msg, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
        msg = msg.trim();
        if(msg.startsWith("WIFILIST") && msg.compareTo("WIFILIST::OK") != 0){
            String list = msg.substring(("WIFILIST::".length()), msg.length());
            if(list.length() > 0) {
                String[] tempNames = list.split(";");
                wifiNames = new ArrayList<String>();
                wifiPasswords = new ArrayList<String>();
                for (int i = 0; i < tempNames.length; i++) {
                    if(tempNames[i].startsWith("ssid=")) {
                        String tempSSID = tempNames[i].substring("ssid=".length(), tempNames[i].length());
                        tempSSID = tempSSID.trim();
                        if(tempSSID.compareTo("NODEF") == 0 || tempSSID.compareTo("") == 0 ) {
                            i++;
                            continue;
                        }
                        wifiNames.add(tempSSID);
                    }
                    if(tempNames[i].startsWith("pass=")) {
                        wifiPasswords.add(tempNames[i].substring("pass=".length(), tempNames[i].length()));
                    }
                }
                bfm.updateWifiList(wifiNames, wifiPasswords);
            }
            if(bfm.status != BasicConfigurationFragment.MANUAL){
                bfm.status = BasicConfigurationFragment.READ_DATA_WIFI;
                bfm.configureDevice();
            }
        }
        if(msg.startsWith("DEVICEID") && msg.compareTo("DEVICEID::OK") != 0){
            String d = msg.substring(("DEVICEID::".length()), msg.length());
            if(d.length() > 0) {
                bfm.updateDeviceID(d);
            }
            if(bfm.status != BasicConfigurationFragment.MANUAL){
                bfm.status = BasicConfigurationFragment.READ_DEVICE_ID;
                bfm.configureDevice();
            }
        }
        if(bfm.status == BasicConfigurationFragment.WRITING_DATA){
            switch (bfm.writing_mode){
                case BasicConfigurationFragment.wmWIFI:
                    if(msg.compareTo("WIFILIST::OK") == 0){
                        bfm.writing_mode = BasicConfigurationFragment.wmDEVICE;
                        bfm.setStatus("Wifi Settings Saved");
                        bfm.configureDevice();
                    }
                    break;
                case BasicConfigurationFragment.wmDEVICE:
                    if(msg.compareTo("DEVICEID::OK") == 0){
                        bfm.writing_mode = BasicConfigurationFragment.wmDATETIME;
                        bfm.setStatus("Device ID Saved");
                        bfm.configureDevice();
                    }
                    break;
                case BasicConfigurationFragment.wmDATETIME:
                    if(msg.compareTo("DATETIME::OK") == 0){
                        bfm.writing_mode = BasicConfigurationFragment.wmALLDONE;
                        bfm.setStatus("Device Config Successful");
                    }
                    break;
            }

        }

    }

    public void onFailure(int error){
        Snackbar.make(mViewPager, "Error Connecting to Device: " + error, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

}
