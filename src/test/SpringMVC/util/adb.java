package test.SpringMVC.util;

import java.io.*;
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

    //set path
    private static final String API_FILE_PATH = "C:\\Users\\Himx\\IdeaProjects\\final2\\API_result.txt";

    private Thread logcatThread = null;
    private Process process = null;

    private HashMap<String, LinkedList<Package>> EnergyInfo = new HashMap<>();
    private HashMap<String, Integer> ListIndex = new HashMap<>();

    private HashMap<String, Integer> CurrentPkgList = new HashMap<>();
    private HashMap<String, Integer> TempPkgList = new HashMap<>();
    private HashMap<String, Integer> CreatePkgList = new HashMap<>();
    private HashMap<String, Integer> DestroyPkgList = new HashMap<>();
//    private LinkedList<String> APIInfoList = null;
    private HashMap<Long, LinkedList<String>> APIInfoList = new HashMap<>();

    private HashMap<String, LinkedList<String>> APIInfo = new HashMap<>();

    private Stack<Event> EventStack = new Stack<>();
    private LinkedList<EventEnergy> EventEnergyList = new LinkedList<>();

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

    public void killProcess(int Pid) {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("adb shell \"su -c kill " + Pid + "\"");
            process.waitFor();
            /*
            BufferedWriter mWriter = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            mWriter.write("su");
            mWriter.newLine();
            mWriter.write("kill " + Pid);
            mWriter.newLine();
            Thread.sleep(100);

            mWriter.write("exit");
            mWriter.newLine();
            mWriter.write("exit");
            process.waitFor();
            */
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (process != null) process.destroy();
        }
    }

    public void logcat() {
        if (isLogcatExecuted) return;

        EnergyModelUtils.init();
        loadAPIInfo();
        isLogcatExecuted = true;
        logcatThread = new Thread(new Runnable() {
            @Override
            public void run() {
                logcatThread();
            }
        });
        logcatThread.start();
    }

    private void loadAPIInfo() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(API_FILE_PATH));

            String line;
            String event = "";
            LinkedList<String> current = null;
            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.length() == 0) {
                    continue;
                }

                if (line.startsWith("##")) {
                    event = line.substring(line.indexOf("##") + 3);
                    if (APIInfo.containsKey(event)) {
                        current = APIInfo.get(event);
                    } else {
                        current = new LinkedList<>();
                        APIInfo.put(event, current);
                    }
                } else {
                    String[] infos = line.split(" ");
                    if (current == null) {
                        current = new LinkedList<>();
                    }
                    current.add(infos[0]);
                    APIInfo.put(event, current);
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void logcatThread() {
        try {
            process = Runtime.getRuntime().exec("adb logcat -c"); //clear logcat
            process.waitFor();
            process = Runtime.getRuntime().exec("adb logcat -s " + TAG_LOGCAT);

            /*
              "Time PkgName Pid ProcessCPUUsage ScreenBrightness ProcessNetworkSpeed"
              "BluetoothState GPSState WifiState NetState Volume SignalStrength BatteryLevel"
            */
            String line;
            BufferedReader mReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while((line = mReader.readLine()) != null){
                if (line.length() == 0 || !line.startsWith("V/")) {
                    continue;
                }

                // 具体事件信息
                String subline = line.substring(line.indexOf(": ") + 2);
                if (subline.contains(" - ")) {
                    int idx = subline.indexOf(" - ");
                    Long time = Long.parseLong(subline.substring(0, idx));
                    String event = subline.substring(idx + 3);
                    if (!EventStack.empty()) {
                        Event e = EventStack.pop();
                        float energy = getEventEnergy(e.time, time, "com.metek.zqWeather");
                        EventEnergyList.add(new EventEnergy(e.time, time, e.event, energy));
                        //System.out.println("begin:" + e.time + ", end:" + time + ", event:" + e.event + ", energy:" + energy);
                        int apicount = 0;
                        if (APIInfo.containsKey(e.event)) {
                            apicount = APIInfo.get(e.event).size();
                        }
                        System.out.println("event:" + e.event + ", energy:" + energy + ", api:" + apicount);
                    }
                    continue;
                }

                if (subline.contains(" + ")) {
                    int idx = subline.indexOf(" + ");
                    Long time = Long.parseLong(subline.substring(0, idx));
                    String event = subline.substring(idx + 3);
                    LinkedList<String> info = new LinkedList<>();
                    info.add(event);

                    EventStack.push(new Event(time, event));

                    if (APIInfo.containsKey(event)) {
                        info.addAll(APIInfo.get(event));
                    }
                    APIInfoList.put(time, info);
                    continue;
                }

                // 手机端一次采样结束标志，更新进程列表
                if (subline.equals(SAMPLE_SPLIT)) {
                    updatePackageList();
                    continue;
                }

                // 硬件资源使用信息，根据模型计算能耗
                String infos[] = subline.split("\\s+");
                Long time = Long.parseLong(infos[0]);
                String pkgName = infos[1];
                int pid = Integer.parseInt(infos[2]);
                double cpu = EnergyModelUtils.getCPUEnergy(Double.parseDouble(infos[3]));
                double screen = EnergyModelUtils.getScreenEnergy(Double.parseDouble(infos[4]));
                double netSpeed = Double.parseDouble(infos[5]);
                double wifi = 0, mobileNet = 0;
                if (infos[8].equals("On")) { //Wifi On
                    wifi = EnergyModelUtils.getWiFiEnergy(netSpeed);
                } else if (infos[9].equals("On")) { //3G on
                    mobileNet = EnergyModelUtils.get3GEnergy(netSpeed);
                }
                Package pkg = new Package(time, pid, cpu, screen, wifi, mobileNet);
                if (EnergyInfo.containsKey(pkgName)) {
                    LinkedList<Package> list = EnergyInfo.get(pkgName);
                    list.add(pkg);
                } else {
                    LinkedList<Package> list = new LinkedList<>();
                    list.add(pkg);
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
        //System.out.println("GetDevice " + ress.length() + " " + ress);
        return JSONObject.fromObject(map).toString();
    }

    synchronized public String getEnergyInfo() {
        logcat();
        //System.out.println("getEnergyInfo");
        Map infoMap = new HashMap(); //store result(Status, Energy, ProcessChange)

        boolean hasData = false;
        Long time = 0L;

        Iterator it;
        it = EnergyInfo.entrySet().iterator();
        List energyList = new LinkedList();
        while (it.hasNext()) {
            Map.Entry entry = ((Map.Entry)it.next());
            String pkgName = (String)entry.getKey();
            LinkedList<Package> list = (LinkedList<Package>)entry.getValue();

            if (!ListIndex.containsKey(pkgName)) {
                //System.err.println("index not found: " + pkgName);
                continue;
            }
            int index = ListIndex.get(pkgName);
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
            time = pkg.getTime();

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
        if (hasData && APIInfoList.size() > 0) {
            JSONArray apis = getAPIList(time);
            if (apis != null) {
                infoMap.put("APIInfoList", apis);
            }
        }
        String res = JSONObject.fromObject(infoMap).toString();
        return res;
    }

    private JSONArray getAPIList(long time) {
        LinkedList<String> list = new LinkedList<>();
        LinkedList<Long> removeList = new LinkedList<>();
        for (Object o: APIInfoList.entrySet()) {
            Map.Entry entry = ((Map.Entry) o);
            Long t = (Long)entry.getKey();
            if (Math.abs(time - t) < 500) {
                removeList.add(t);
            }
        }
        if (removeList.size() > 0) {
            Collections.sort(removeList);
            for (Long t: removeList) {
                list.addAll(APIInfoList.get(t));
                APIInfoList.remove(t);
            }
            return JSONArray.fromObject(list);
        } else {
            return null;
        }
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
            EnergyInfo.remove(pkgName);
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

    private float getEventEnergy(Long begin, Long end, String pkgName) {
        if (!EnergyInfo.containsKey(pkgName)) {
            return 0f;
        }
        LinkedList<Package> list = EnergyInfo.get(pkgName);
        float energy = 0f;
        for (Package p : list) {
            //System.out.println("" + p.getTime() + " " + begin + " " + Math.abs(p.getTime() - begin));
            if (Math.abs(p.getTime() - begin) < 1000) {
                double current = p.getCPU() + p.getScreen() + p.get3G() + p.getWifi();
                Long time = end - begin;
                if (time <= 0) time = 1L;
                energy = (float)(current * 0.001 * 4.2 * time * 0.001);
                return energy;
            }
        }
        return energy;
    }

    private class Package {
        public Package(Long time, int pid, double cpu, double screen, double wifi, double mobilenet) {
            Time = time;
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

        public long getTime() {
            return Time;
        }
        public void setTime(long time) {
            Time = time;
        }

        private int Pid;
        private double CPU;
        private double Screen;
        private double Wifi;
        private double MobileNet;
        private long Time;
    }

    private class Event {
        public Event(Long time, String event) {
            this.time = time;
            this.event = event;
        }

        public Long time;
        public String event;
    }

    private class EventEnergy implements Comparable{
        public EventEnergy (Long begin, Long end, String event, float energy) {
            this.begin = begin;
            this.end = end;
            this.event = event;
            this.energy = energy;
        }

        @Override
        public int compareTo(Object o) {
            EventEnergy e = (EventEnergy) o;
            return this.begin.compareTo(e.begin);
        }

        public Long begin, end;
        public String event;
        public float energy;
    }
}
