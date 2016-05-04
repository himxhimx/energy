package test.SpringMVC.util;

import java.text.DecimalFormat;

/**
 * Created by shenpeng on 2016/4/12.
 */
public class EnergyModelUtils {
    //set path
    private static final String CONFIG_FILE_PATH = "C:\\Users\\shenpeng\\IdeaProjects\\energy\\energy_model.ini";

    private static double CPU_A, CPU_B, CPU_C, SCREEN_A, SCREEN_B, WIFI_A, MOBILE_A, GPU_A, GPU_B, GPS_A, CAMERA_A;

    private static IniReader reader = null;
    private static DecimalFormat df = new DecimalFormat("#.##");

    public static void init() {
        if (reader != null)
            return;

        reader = new IniReader(CONFIG_FILE_PATH);
        CPU_A = Double.parseDouble(reader.getValue("default", "cpu_a"));
        CPU_B = Double.parseDouble(reader.getValue("default", "cpu_b"));
        CPU_C = Double.parseDouble(reader.getValue("default", "cpu_c"));
        SCREEN_A = Double.parseDouble(reader.getValue("default", "screen_a"));
        SCREEN_B = Double.parseDouble(reader.getValue("default", "screen_b"));
        WIFI_A = Double.parseDouble(reader.getValue("default", "wifi_a"));
        MOBILE_A = Double.parseDouble(reader.getValue("default", "mobile_a"));
    }

    public static double getCPUEnergy(double usage) {
        if (usage <= 0) {
            return 0;
        } else {
            double energy = CPU_A * usage * usage + CPU_B * usage + CPU_C;
            return Double.parseDouble(df.format(energy));
        }
    }

    public static double getScreenEnergy(double light) {
        if (light < 0) {
            return 0;
        } else {
            double energy = SCREEN_A * light + SCREEN_B;
            return Double.parseDouble(df.format(energy));
        }
    }

    public static double getWiFiEnergy(double speed) {
        double energy = WIFI_A * speed;
        return Double.parseDouble(df.format(energy > 500 ? 500 : energy));
    }

    public static double get3GEnergy(double speed) {
        double energy = MOBILE_A * speed;
        return Double.parseDouble(df.format(energy > 500 ? 500 : energy));
    }
}
