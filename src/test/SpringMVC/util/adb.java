package test.SpringMVC.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Created by Himx on 30/3/2016.
 *
 */
public class adb {
    private Thread logcatThread = null;
    private Process process = null;

    public String logcatInfo = "";
    private boolean isLogcatExecuted = false;

    @Override
    protected void finalize() {
        try {
            super.finalize();
        } catch (java.lang.Throwable e) {
            e.printStackTrace();
        } finally {
            if (process != null) process.destroy();
            if (logcatThread != null) logcatThread.interrupt();
        }
    }

    public void logcat() {
        if (isLogcatExecuted) return;

        EnergyModelUtils.init();
        isLogcatExecuted = true;
        logcatThread = new Thread(new Runnable() {
            @Override
            public void run() {
                logcatThread();
            }
        });
        logcatThread.start();
    }

    private void logcatThread() {
        try {
            process = Runtime.getRuntime().exec("adb logcat -c"); //clear logcat
            process.waitFor();
            process = Runtime.getRuntime().exec("adb logcat -s EnergyMonitor");

            /*
                "Time\tBatteryLevel\tTotalCPUUsage\tScreenBrightness\t" +
                "TotalWifiSpeed\tTotalMobileSpeed\tProcessSpeed\tBluetoothState\tGPSState\t" +
                "WifiState\tNetState\tVolume\tSignalStrength\t" + processCPUTimes.toString() +"\n";
            */
            String line;
            BufferedReader mReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while((line = mReader.readLine()) != null){
                if (line.length() == 0 || !line.startsWith("V/")) {
                    continue;
                }
                String subline = line.substring(line.indexOf(": ") + 2);
                String infos[] = subline.split("\\s+");
                EnergyCPU.add(EnergyModelUtils.getCPUEnergy(Double.parseDouble(infos[3])));
                EnergyScreen.add(EnergyModelUtils.getScreenEnergy(Double.parseDouble(infos[4])));
                EnergyWiFi.add(EnergyModelUtils.getWiFiEnergy(Double.parseDouble(infos[5])));
                Energy3G.add(EnergyModelUtils.get3GEnergy(Double.parseDouble(infos[6])));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String devices() {
        String res = "";
        Process process = null;
        try {
            process= Runtime.getRuntime().exec("adb devices");
            process.waitFor();
            InputStreamReader isr= new InputStreamReader(process.getInputStream());
            Scanner sc=new Scanner(isr);
            while(sc.hasNext()){
                res = res.concat(" ");
                res = res.concat(sc.next());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(process != null) process.destroy();
        }
        return res;
    }

    public String getDeviceInfo() {
        String res = devices();
        int lastSpace = res.lastIndexOf(" ");
        String tmp = " List of devices attached";
        String deviceName = "";
        String status = "";
        if (!tmp.equalsIgnoreCase(res)) {
            deviceName = res.substring(tmp.length() + 1, lastSpace);
            status = res.substring(lastSpace + 1);
        }
        Map<String, String> map= new HashMap<>();
        map.put("status", status);
        map.put("deviceName", deviceName);
        return JSON.toJSONstr(map);
    }

    private static LinkedList<Double> EnergyCPU = new LinkedList<>();
    private static LinkedList<Double> EnergyScreen = new LinkedList<>();
    private static LinkedList<Double> Energy3G = new LinkedList<>();
    private static LinkedList<Double> EnergyWiFi = new LinkedList<>();

    private int index = 0;
    public String getEnergyInfo() {
        logcat();
        System.out.println(index);
        Map<String, String> map = new HashMap<>();
        if (index >= EnergyCPU.size()) {
            map.put("status", "-1");
            if (index == 0) {
                map.put("CPU", "0");
                map.put("Screen", "0");
                map.put("3G", "0");
                map.put("Wifi", "0");
                return JSON.toJSONstr(map);
            }
            index = EnergyCPU.size() - 1;
        }
        else {
            map.put("status", "0");
        }
        map.put("CPU", "" + EnergyCPU.get(index));
        map.put("Screen", "" + EnergyScreen.get(index));
        map.put("3G", "" + Energy3G.get(index));
        map.put("Wifi", "" + EnergyWiFi.get(index));
        index ++;
        return JSON.toJSONstr(map);
    }
}
