package in.airveda.sensors.dataview;


public class AQIConversions
{

    public static int MAX_AQI_DEFINED = 500;
    public static int AQI_LEVELS = 6;

    public static String [] AQI_STATUS = {"Good", "Satisfactory", "Moderately Polluted", "Poor", "Very Poor", "Severe"};

    public static int [] AQI_RANGE = {50, 100, 200, 300, 400, 500};
    public static int [] AQI_PM25_RANGE = {30, 60, 90, 120, 250, 500};
    public static int [] AQI_COLOR = {0xff79bc6a, 0xffbbcf4c, 0xffFFCF00, 0xffFF9A00, 0xffff0000, 0xffA52A2A};

    public static String [] AQI_ADVISORY = {"No cautionary action needed. Encourage children to play outside.",
                                            "Extra sensitive people - avoid prolonged/heavy work outside.",
                                            "Children, elderly and people with heart or lung ailments to watch out!",
                                            "Children and elderly should avoid exertion, others watch out!",
                                            "Stay indoors as far as possible. Use masks if available. !",
                                            "Stay indoors as far as possible. Use masks or air filteration if possible."};

    Integer [] colorCodes = new Integer[MAX_AQI_DEFINED];

    public AQIConversions(){
        initColors();
    }

    private void initColors(){
        int start = 0;
        int end = 0;
        for(int i = 0; i < AQI_LEVELS; i++){
            end = AQI_RANGE[i];
            initColorArray(start, end, new Integer(AQI_COLOR[i]));
            start = end;
        }
    }

    private void initColorArray(int start, int end, Integer color) {
        for(int i = start; i < end && i < MAX_AQI_DEFINED; i++){
            colorCodes[i] = color;
        }

    }

    private static int getAQIforPM25(int pm25value){
        int i = 0;
        for(; i < AQI_LEVELS - 1; i++){
            if(pm25value <= AQI_PM25_RANGE[i]) break;
        }
        return (int)((pm25value * AQI_RANGE[i])/AQI_PM25_RANGE[i]);
    }

    public static Integer getColor(int pm25value){
        int i = 0;
        for(; i < AQI_LEVELS - 1; i++){
            if(pm25value <= AQI_PM25_RANGE[i]) break;
        }
        return AQI_COLOR[i];

    }

    public static String getStatus(int pm25value) {
        int i = 0;
        for(; i < AQI_LEVELS - 1; i++){
            if(pm25value <= AQI_PM25_RANGE[i]) break;
        }
        return AQI_STATUS[i];
    }

    public static String getAdvisory(int pm25value) {
        int i = 0;
        for(; i < AQI_LEVELS - 1; i++){
            if(pm25value <= AQI_PM25_RANGE[i]) break;
        }
        return AQI_ADVISORY[i];
    }

}
