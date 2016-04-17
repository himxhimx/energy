package test.SpringMVC.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

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
        Map map= new HashMap<>();
        map.put("status", status);
        map.put("deviceName", deviceName);
        map.put("packageList", getPackagesInfo());

        List tmp3 = new ArrayList();
        System.out.print(JSONArray.fromObject(tmp3).toString());
        return JSONObject.fromObject(map).toString();
    }

    private static LinkedList<Double> EnergyCPU = new LinkedList<>();
    private static LinkedList<Double> EnergyScreen = new LinkedList<>();
    private static LinkedList<Double> Energy3G = new LinkedList<>();
    private static LinkedList<Double> EnergyWiFi = new LinkedList<>();

    private static LinkedList<Package> PackageInfo = new LinkedList<>();
    private static LinkedList<Package> PackageNameInfo = new LinkedList<>();

    private int index = 0;
    public String getEnergyInfo(int pid) {
        logcat();
        Map map = new HashMap<>();
        Map energyPercent = getEnergyPercent(pid);
        if (index >= EnergyCPU.size()) {
            map.put("Status", -1);
            if (index == 0) {
                map.put("CPU", 0);
                map.put("Screen", 0);
                map.put("3G", 0);
                map.put("Wifi", 0);
                map.put("Percent", energyPercent);
                JSONObject json = JSONObject.fromObject(map);
                return json.toString();
            }
            index = EnergyCPU.size() - 1;
        }
        else {
            map.put("Status", 0);
        }
        map.put("CPU", EnergyCPU.get(index));
        map.put("Screen", EnergyScreen.get(index));
        map.put("3G", Energy3G.get(index));
        map.put("Wifi", EnergyWiFi.get(index));
        map.put("Percent", energyPercent);
        //map.put("ProcessChange", getPackagesChange());
        index ++;
        return JSONObject.fromObject(map).toString();
    }

    private static int testT = 11;

    private JSONObject getPackagesChange() {
        Map map = new HashMap();
        List ProcessCreate = new ArrayList();
        List ProcessDestroy = new ArrayList();
        Map tmp1 = new HashMap();
        tmp1.put("Pid", 123 + testT);
        tmp1.put("Name", "com.example.himx.package" + testT);
        ProcessCreate.add(tmp1);
        Map tmp2 = new HashMap();
        tmp2.put("Pid", 123 + testT - 10);
        tmp2.put("Name", "com.example.himx.package" + (testT - 10));
        ProcessDestroy.add(tmp2);
        map.put("ProcessCreate", JSONArray.fromObject(ProcessCreate));
        map.put("ProcessDestroy", JSONArray.fromObject(ProcessDestroy));
        testT++;
        return JSONObject.fromObject(map);
    }

    private JSONObject getEnergyPercent(int pid) {
        Map map = new HashMap<>();
        pid++;
        Double tmp = 1.0 / pid;
        map.put("CPU", tmp);
        map.put("Screen", tmp);
        map.put("3G", tmp);
        map.put("Wifi", tmp);
        return JSONObject.fromObject(map);
    }

    private JSONArray getPackagesInfo() {
        List list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Map map = new HashMap();
            map.put("Pid", i + 123);
            map.put("Name", "com.example.himx.package" + i);
            list.add(JSONObject.fromObject(map));
        }
        return JSONArray.fromObject(list);
    }

    private class Package {
        public Package(int pid, double CPU, double screen, double wifi) {
            Pid = pid;
            this.CPU = CPU;
            Screen = screen;
            Wifi = wifi;
        }

        public int getPid() {
            return Pid;
        }

        public void setPid(int pid) {
            Pid = pid;
        }

        public double getCPU() {
            return CPU;
        }

        public void setCPU(double CPU) {
            this.CPU = CPU;
        }

        public double getScreen() {
            return Screen;
        }

        public void setScreen(double screen) {
            Screen = screen;
        }

        public double getWifi() {
            return Wifi;
        }

        public void setWifi(double wifi) {
            Wifi = wifi;
        }
        private int Pid;
        private double CPU;
        private double Screen;
        private double Wifi;
    }

    private class PackageName {
        private int Pid;
        private String pname;
        public PackageName(int pid, String name) {
            this.Pid = pid;
            this.pname = name;
        }

        public int getPid() {
            return Pid;
        }

        public String getPname() {
            return pname;
        }
    }
}
