package in.airveda.sensors.data;

import com.orm.SugarRecord;

import java.util.Comparator;
import java.util.Date;

/**
 * Created by siddhartha on 28/11/15.
 */
public class DataReadings extends SugarRecord<DataReadings> implements Comparator<DataReadings> {
    public Device device;

    public String pollutant_type;
    public float pollutant_value;
    public String pollutant_unit;
    public int period_of_measure; // In minutes

    public Date capture_date;

    public DataReadings(){

    }

    public int compare(DataReadings d1, DataReadings d2) {
        return d1.capture_date.compareTo(d2.capture_date);
    }
}
