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
    private HashMap<String, Integer> ListIndex = new HashMap<>();

    private HashMap<String, Integer> CurrentPkgList = new HashMap<>();
    private HashMap<String, Integer> TempPkgList = new HashMap<>();
    private HashMap<String, Integer> CreatePkgList = new HashMap<>();
    private HashMap<String, Integer> DestroyPkgList = new HashMap<>();

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

    public String getDeviceInfo(boolean first) {
        String res = devices();
        int lastSpace = res.lastIndexOf(" ");
        String tmp = " List of devices attached";
        String deviceName = "";
        String status = "";
        if (!tmp.equalsIgnoreCase(res)) {
            deviceName = res.substring(tmp.length() + 1, lastSpace);
            status = res.substring(lastSpace + 1);
        }

        //logcat();
        Map map= new HashMap<>();
        map.put("status", status);
        map.put("deviceName", deviceName);
        if (first) map.put("packageList", getPackagesInfo());

        String ress = JSONObject.fromObject(map).toString();
        System.out.println("GetDevice " + ress.length() + " " + ress);
        return JSONObject.fromObject(map).toString();
    }

    public String getEnergyInfo(int pid) {
        logcat();

        Map infoMap = new HashMap(); //store result(Status, Energy, ProcessChange)

        boolean hasData = false;

        Iterator it;
        it = EnergyInfo.entrySet().iterator();
        List energyList = new LinkedList();
        int _index = 0;
        while (it.hasNext()) {
            Map.Entry entry = ((Map.Entry)it.next());
            String pkgName = (String)entry.getKey();
            LinkedList<Package> list = (LinkedList<Package>)entry.getValue();

            if (!ListIndex.containsKey(pkgName)) {
                System.err.println("index not found: " + pkgName);
                continue;
            }
            int index = ListIndex.get(pkgName);
            _index = index;
            if (index == 0 && list.size() == 0) {
                Map map = new HashMap();
                map.put("Pid", TempPkgList.get(pkgName));
                map.put("CPU", 0);
                map.put("Screen", 0);
                map.put("3G", 0);
                map.put("Wifi", 0);
                energyList.add(map);
                continue;
            } else if (index >= list.size()) {
                Package pkg = list.get(list.size() - 1);
                Map map = new HashMap();
                map.put("Pid", pkg.getPid());
                map.put("CPU", pkg.getCPU());
                map.put("Screen", pkg.getScreen());
                map.put("3G", pkg.get3G());
                map.put("Wifi", pkg.getWifi());
                energyList.add(map);
                continue;
            }

            hasData = true;

            Package pkg = list.get(index);
            Map map = new HashMap();
            map.put("Pid", pkg.getPid());
            map.put("CPU", pkg.getCPU());
            map.put("Screen", pkg.getScreen());
            map.put("3G", pkg.get3G());
            map.put("Wifi", pkg.getWifi());
            energyList.add(map);
            ListIndex.put(pkgName, index + 1);
        }
        infoMap.put("Energy", energyList);

        if (hasData) {
            infoMap.put("Status", 0);
        } else {
            infoMap.put("Status", -1);
        }

        JSONObject processChange = getPackagesChange();
        if (processChange != null) infoMap.put("ProcessChange", processChange);
        String res = JSONObject.fromObject(infoMap).toString();
        System.out.println(res.length() + " " + _index + " " + res);
        return res;
    }

    private void updatePackageList() {
        findDifferentAndAddToMap(CurrentPkgList, TempPkgList, CreatePkgList);
        findDifferentAndAddToMap(TempPkgList, CurrentPkgList, DestroyPkgList);

        for (Object o: CreatePkgList.entrySet()) {
            Map.Entry entry = ((Map.Entry) o);
            String pkgName = (String) entry.getKey();
            ListIndex.put(pkgName, 0);
        }

        for (Object o: DestroyPkgList.entrySet()) {
            Map.Entry entry = ((Map.Entry) o);
            String pkgName = (String) entry.getKey();
            ListIndex.remove(pkgName);
        }

        TempPkgList.clear();
        TempPkgList.putAll(CurrentPkgList);
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

        CreatePkgList.clear();
        DestroyPkgList.clear();
        return JSONObject.fromObject(changeMap);
    }

    private JSONArray getPackagesInfo() {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("adb shell ps");
            BufferedReader mReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            line = mReader.readLine();
            if (line == null)
                return JSONArray.fromObject(new ArrayList<>());

            TempPkgList.clear();
            ListIndex.clear();

            String[] arrs;
            while((line = mReader.readLine()) != null){
                arrs = line.split("\\s+");
                if (arrs[0].startsWith("u")) {
                    int pid = Integer.parseInt(arrs[1]);
                    String pkgName = arrs[8];
                    TempPkgList.put(pkgName, pid);
                    ListIndex.put(pkgName, 0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(process != null) {
                process.destroy();
            }
        }

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
