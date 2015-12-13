package in.airveda.sensors.data;

import com.orm.SugarRecord;

import org.json.JSONObject;

public class Tag extends SugarRecord<Tag>{
    public int server_id;
    public String label;

    public Tag(){

    }

    public Tag(String label){
        this.label = label;
    }

    public JSONObject getJSON(){
        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("name", label);
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
            label = o.getString("name");
        }catch(Exception e){
            e.printStackTrace();;
        }
    }

}
