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
    private static final String TAG_LOGCAT = "EnergyMonitor";
    private static final String SAMPLE_SPLIT = "+T-23==53-X7-+YuRG";

    private Thread logcatThread = null;
    private Process process = null;

    private HashMap<String, LinkedList<Package>> EnergyInfo = new HashMap<>();

    private HashMap<String, Integer> CurrentPkgList = new HashMap<>();
    private HashMap<String, Integer> TempPkgList = new HashMap<>();
    private HashMap<String, Integer> CreatePkgList = new HashMap<>();
    private HashMap<String, Integer> DestroyPkgList = new HashMap<>();

    private int index = 0;
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
            process = Runtime.getRuntime().exec("adb logcat -s " + TAG_LOGCAT);

            /*
              "PkgName Pid ProcessCPUUsage ScreenBrightness ProcessNetworkSpeed"
              "BluetoothState GPSState WifiState NetState Volume SignalStrength BatteryLevel"
            */
            String line;
            BufferedReader mReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while((line = mReader.readLine()) != null){
                if (line.length() == 0 || !line.startsWith("V/")) {
                    continue;
                }
                String subline = line.substring(line.indexOf(": ") + 2);
                if (subline.equals(SAMPLE_SPLIT)) {
                    updatePackageList();
                    continue;
                }

                String infos[] = subline.split("\\s+");
                String pkgName = infos[0];
                int pid = Integer.parseInt(infos[1]);
                double cpu = EnergyModelUtils.getCPUEnergy(Double.parseDouble(infos[2]));
                double screen = EnergyModelUtils.getScreenEnergy(Double.parseDouble(infos[3]));
                double netSpeed = Double.parseDouble(infos[4]);
                double wifi = 0, mobileNet = 0;
                if (infos[7].equals("On")) { //Wifi On
                    wifi = EnergyModelUtils.getWiFiEnergy(netSpeed);
                } else if (infos[8].equals("On")) { //3G on
                    mobileNet = EnergyModelUtils.get3GEnergy(netSpeed);
                }
                if (EnergyInfo.containsKey(pkgName)) {
                    LinkedList<Package> list = EnergyInfo.get(pkgName);
                    list.add(new Package(pid, cpu, screen, wifi, mobileNet));
                } else {
                    LinkedList<Package> list = new LinkedList<>();
                    list.add(new Package(pid, cpu, screen, wifi, mobileNet));
                    EnergyInfo.put(pkgName, list);
                }
                CurrentPkgList.put(pkgName, pid);
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

        logcat();
        Map map= new HashMap<>();
        map.put("status", status);
        map.put("deviceName", deviceName);
        map.put("packageList", getPackagesInfo());

        List tmp3 = new ArrayList();
        System.out.print(JSONArray.fromObject(tmp3).toString());
        return JSONObject.fromObject(map).toString();
    }

    public String getEnergyInfo(int pid) {
        logcat();

        boolean hasNoData = false;

        Iterator it = EnergyInfo.entrySet().iterator();
        if (!it.hasNext()) {
            hasNoData = true;
            index = -1;
        } else {
            Map.Entry entry = ((Map.Entry)it.next());
            LinkedList<Package> list = (LinkedList<Package>)entry.getValue();
            if (index >= list.size()) {
                hasNoData = true;
                index = list.size() - 1;
            }
        }

        Map infoMap = new HashMap(); //store result(Status, Energy, ProcessChange)
        if (hasNoData) {
            infoMap.put("Status", -1);
            if (index == -1) {
                index = 0;
                Map map = new HashMap();
                map.put("Pid", 0);
                map.put("CPU", 0);
                map.put("Screen", 0);
                map.put("3G", 0);
                map.put("Wifi", 0);

                List list = new LinkedList();
                list.add(map);
                infoMap.put("Energy", list);
                JSONObject json = JSONObject.fromObject(infoMap);
                return json.toString();
            }
        }
        else {
            infoMap.put("Status", 0);
        }

        it = EnergyInfo.entrySet().iterator();
        List energyList = new LinkedList();
        while (it.hasNext()) {
            Map.Entry entry = ((Map.Entry)it.next());
            LinkedList<Package> list = (LinkedList<Package>)entry.getValue();
            Package pkg = list.get(index);

            Map map = new HashMap();
            map.put("Pid", pkg.getPid());
            map.put("CPU", pkg.getCPU());
            map.put("Screen", pkg.getScreen());
            map.put("3G", pkg.get3G());
            map.put("Wifi", pkg.getWifi());

            energyList.add(map);
        }
        infoMap.put("Energy", energyList);

        JSONObject processChange = getPackagesChange();
        if (processChange != null) infoMap.put("ProcessChange", processChange);
        index ++;
        String res = JSONObject.fromObject(infoMap).toString();
        System.out.println(res.length());
        return res;
    }

    private void updatePackageList() {
        findDifferentAndAddToMap(CurrentPkgList, TempPkgList, CreatePkgList);
        findDifferentAndAddToMap(TempPkgList, CurrentPkgList, DestroyPkgList);

        TempPkgList = CurrentPkgList;
        CurrentPkgList.clear();
    }

    private JSONObject getPackagesChange() {
        if (CreatePkgList.size() == 0 && DestroyPkgList.size() == 0) {
            return null;
        }

        Map changeMap = new HashMap();
        List createList = new ArrayList();
        List destroyList = new ArrayList();

        addPkgInfoToList(CreatePkgList, createList);
        changeMap.put("ProcessCreate", JSONArray.fromObject(createList));

        addPkgInfoToList(DestroyPkgList, destroyList);
        changeMap.put("ProcessDestroy", JSONArray.fromObject(destroyList));

        createList.clear();
        destroyList.clear();
        return JSONObject.fromObject(changeMap);
    }

    private JSONArray getPackagesInfo() {
        List list = new ArrayList<>();

        addPkgInfoToList(TempPkgList, list);
        return JSONArray.fromObject(list);
    }

    private void addPkgInfoToList(Map srcMap, List dstList) {
        Iterator it = srcMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = ((Map.Entry)it.next());
            String pkgName = (String)entry.getKey();
            Integer pid = (Integer)entry.getValue();

            Map map = new HashMap();
            map.put("Name", pkgName);
            map.put("Pid", pid);
            dstList.add(JSONObject.fromObject(map));
        }
    }

    private void findDifferentAndAddToMap(Map map1, Map map2, Map dstMap) {
        Iterator it = map1.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = ((Map.Entry)it.next());
            String pkgName = (String)entry.getKey();
            Integer pid = (Integer)entry.getValue();
            if (!map2.containsKey(pkgName)) {
                dstMap.put(pkgName, pid);
            }
        }
    }

    private class Package {
        public Package(int pid, double cpu, double screen, double wifi, double mobilenet) {
            Pid = pid;
            CPU = cpu;
            Screen = screen;
            Wifi = wifi;
            MobileNet = mobilenet;
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

        public double get3G() {
            return MobileNet;
        }
        public void set3G(double mobilenet) {
            MobileNet = mobilenet;
        }

        private int Pid;
        private double CPU;
        private double Screen;
        private double Wifi;
        private double MobileNet;
    }
}
