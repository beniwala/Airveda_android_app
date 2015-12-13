package in.airveda.sensors.data;

import com.orm.SugarRecord;

import org.json.JSONObject;

/**
 * Created by siddhartha on 26/11/15.
 */
public class Location extends SugarRecord<Location>{
    public int server_id;
    public String name;

    public float latitude;
    public float longitude;

    public String address;

    public Location(){

    }

    public Location(String name, float lat, float lon, String address){
        this.name = name;
        this.latitude = lat;
        this.longitude = lon;
        this.address = address;
    }

    public JSONObject getJSON(){
        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("name", name);
            jsonParams.put("lat", latitude);
            jsonParams.put("lon", longitude);
            jsonParams.put("address", address);
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
            address = o.getString("address");
            latitude = (float)o.getDouble("lat");
            longitude = (float)o.getDouble("lon");
        }catch(Exception e){
            e.printStackTrace();;
        }
    }

}

