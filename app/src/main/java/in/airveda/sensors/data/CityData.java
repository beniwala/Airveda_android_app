package in.airveda.sensors.data;

import com.orm.SugarRecord;

import java.util.Date;

/**
 * Created by siddhartha on 03/12/15.
 */
public class CityData extends SugarRecord<CityData>{
    public String city;
    public int pm25value;
    public Date date;

    public CityData(){

    }
}
