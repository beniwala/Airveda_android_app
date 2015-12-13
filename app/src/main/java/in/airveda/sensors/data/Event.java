package in.airveda.sensors.data;

import com.orm.SugarRecord;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by siddhartha on 28/11/15.
 */


public class Event extends SugarRecord<Event> {
    public int server_id;
    public String name;
    public Device device;
    public Date startDate;
    public Date endDate;

    public Event(){
    }

    public Event(String name, Device device, Date startDate, Date endDate){
        this.name = name;
        this.device = device;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public JSONObject getJSON(){
        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("name", name);
            if(device != null) {
                jsonParams.put("device_uid", device.device_uid);
            }
            jsonParams.put("start_date", startDate);
            jsonParams.put("end_date", endDate);
        }catch(Exception e){
            e.printStackTrace();;
        }
        return jsonParams;
    }

    public void setFromJSON(String json){
        try {
            //
            // Sample JSON

            //

            JSONObject o = new JSONObject(json);
            server_id = o.getInt("id");
            name = o.getString("name");

            int dev_id = o.getInt("device");
            if(dev_id > 0) {
                device = Device.findById(Device.class, new Long(dev_id));
            }
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            String sdate = o.getString("start_date");

            String edate = o.getString("end_date");

            startDate = format.parse(sdate);
            endDate = format.parse(edate);

        }catch(Exception e){
            e.printStackTrace();;
        }
    }

}
