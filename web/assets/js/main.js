/**
 * Created by Himx on 14/4/2016.
 * 
 */

var getEnergyInfo = function () {
    $.ajax({
        type: "GET",
        url: "/getEnergyInfo",
        success: function(data) {
            if (!data) return;
            data = JSON.parse(data);

            if (data["Status"] == -1) {
                $.ajax({
                    method: 'GET',
                    url: 'getDevices/notfirst',
                    success: function (data) {
                        var jData = JSON.parse(data);
                        if (jData["deviceName"] === "") {
                            controlBox.disconnectHandler();
                        }
                    },
                    error: function(err) {
                        console.error(err);
                    }
                });
            }
            
            packageInfoBox.update(data["ProcessChange"]);
            
            APIInfoList = APIInfoList.slice(1);
            if (data["APIInfoList"]) {
                APIInfoList.push(data["APIInfoList"].slice(0));
            } else {
                APIInfoList.push([]);
            }
            
            var newAll = {
                "CPU": 0,
                "3G": 0,
                "Screen": 0,
                "Wifi": 0
            };

            $.each(data["Energy"], function(idx, pkg){
                var newPid = pkg["Pid"];
                //console.log(newPid, AllEnergyInfo);
                $.each(pkg, function(key, val) {
                    if (!AllEnergyInfo[key]) return;
                    if (!AllEnergyInfo[key].data[newPid]) initEnergyInfo(newPid);
                    AllEnergyInfo[key].data[newPid] = AllEnergyInfo[key].data[newPid].slice(1);
                    AllEnergyInfo[key].data[newPid].push(val);
                    newAll[key] += val;
                });
            });
            
            //console.log(newAll);
            
            $.each(newAll, function(key, val) {
                if (!AllEnergyInfo[key]) return; 
                AllEnergyInfo[key].data[0] = AllEnergyInfo[key].data[0].slice(1);
                AllEnergyInfo[key].data[0].push(val);
            });

            //console.log(AllEnergyInfo);

            //update the time sequence
            AllEnergyInfo.Time.data = AllEnergyInfo.Time.data.slice(1);
            $.each(AllEnergyInfo.Time.data, function(key2){
                AllEnergyInfo.Time.data[key2][0]--;
            });
            AllEnergyInfo.Time.data.push([totalPoints - 1, AllEnergyInfo.Time.data[totalPoints - 2][1] + updateInterval / 1000.0]);
            
            timeXaisBar.update(true);
            if (isPlaying) 
            {
                linePlot.plotAccordingToChoices();
                timeXaisBar.draw();
            }
        },
        error: function (err) {
            console.error(err);
        }
    });
};

var update = function() {
    if (!connected) return;
    getEnergyInfo();
    timer = setTimeout(update, updateInterval);
};

choiceBox.init();
linePlot.init();
controlBox.init();
//initEnergyTime();
//console.log(AllEnergyInfo);