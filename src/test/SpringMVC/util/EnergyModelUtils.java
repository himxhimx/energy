package test.SpringMVC.util;

/**
 * Created by shenpeng on 2016/4/12.
 */
public class EnergyModelUtils {
    private static final String CONFIG_FILE_PATH = "C:\\Users\\Himx\\IdeaProjects\\final2\\energy_model.ini";

    private static double CPU_A, CPU_B, SCREEN_A, SCREEN_B, WIFI_A, MOBILE_A, GPU_A, GPU_B, GPS_A, CAMERA_A;

    private static IniReader reader = null;

    public static void init() {
        if (reader != null)
            return;

        reader = new IniReader(CONFIG_FILE_PATH);
        CPU_A = Double.parseDouble(reader.getValue("default", "cpu_a"));
        CPU_B = Double.parseDouble(reader.getValue("default", "cpu_b"));
        SCREEN_A = Double.parseDouble(reader.getValue("default", "screen_a"));
        SCREEN_B = Double.parseDouble(reader.getValue("default", "screen_b"));
        WIFI_A = Double.parseDouble(reader.getValue("default", "wifi_a"));
        MOBILE_A = Double.parseDouble(reader.getValue("default", "mobile_a"));
    }

    public static double getCPUEnergy(double usage) {
        return CPU_A * usage + CPU_B;
    }

    public static double getScreenEnergy(double light) {
        return SCREEN_A * light + SCREEN_B;
    }

    public static double getWiFiEnergy(double speed) {
        return WIFI_A * speed;
    }

    public static double get3GEnergy(double speed) {
        return MOBILE_A * speed;
    }
}
