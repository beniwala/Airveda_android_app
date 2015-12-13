package in.airveda.sensors.data;


import com.orm.SugarRecord;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Device extends SugarRecord<Device>{
    public int server_id;
    public String name;
    public String model_number;
    public String device_uid;

    public Tag tag;
    public Location location;

    public Date activation_date;

    public Device(){
        if(activation_date == null){
            activation_date = new Date(System.currentTimeMillis());
        }
    }

    public Device(String name, Tag tag, Location loc){
        this.name = name;
        this.tag = tag;
        this.location = loc;

        if(activation_date == null){
            activation_date = new Date(System.currentTimeMillis());
        }
    }

    public Device(String name, String model_number, String device_uid,
                  Tag tag, Location loc,
                  Date activation_date){
        this.name = name;
        this.model_number = model_number;
        this.device_uid = device_uid;

        this.tag = tag;
        this.location = loc;

        this.activation_date = activation_date;
    }

    public JSONObject getJSON(){
        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("name", name);
            jsonParams.put("model_name", model_number);
            jsonParams.put("device_uid", device_uid);
            if(tag != null) {
                jsonParams.put("tag", tag.server_id);
            }
            if(location != null){
                jsonParams.put("location", location.server_id);
            }
            //jsonParams.put("activation_date", activation_date);
        }catch(Exception e){
            e.printStackTrace();;
        }
        return jsonParams;
    }

    public void setFromJSON(String json){
        try {
            //
            // Sample JSON
            // {"server_id":2,"name":"dev1",
            // "device_uid":"2222",
            // "model_name":"abc",
            // "location":1,
            // "tag":1,
            // "activation_date":"2015-11-30T10:01:38.438527Z",
            // "status":1}
            //

            JSONObject o = new JSONObject(json);
            server_id = o.getInt("id");
            name = o.getString("name");
            model_number = o.getString("model_number");
            device_uid = o.getString("device_uid");

            int tag_id = o.getInt("tag");
            if(tag_id > 0) {
                tag = Tag.findById(Tag.class, new Long(tag_id));
            }
            int loc_id = o.getInt("location");
            if(loc_id > 0) {
                location = Location.findById(Location.class, new Long(loc_id));
            }
            String date = o.getString("activation_date");
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            activation_date = format.parse(date);
        }catch(Exception e){
            e.printStackTrace();;
        }
    }

}
