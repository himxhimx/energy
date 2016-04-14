/**
 * Created by Himx on 14/4/2016.
 * 
 */

var getEnergyInfo = function () {
    $.ajax({
        type: "GET",
        url: "/getEnergyInfo.do",
        success: function(data) {
            if (!data) return;
            data = JSON.parse(data);
            timeXaisBar.update();
            var totalEnergy = 0;

            if (data.status == -1) {
                $.ajax({
                    method: 'GET',
                    url: 'getDevices.do',
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

            $.each(data, function(key){
                if (key === 'status') return;
                AllEnergyInfo[key].data = AllEnergyInfo[key].data.slice(1);
                totalEnergy += data[key];
                $.each(AllEnergyInfo[key].data, function(key2){
                    AllEnergyInfo[key].data[key2][0]--;
                });
                AllEnergyInfo[key].data.push([totalPoints - 1, data[key]]);
            });
            //Calculate the total energy
            AllEnergyInfo.Total.data = AllEnergyInfo.Total.data.slice(1);
            $.each(AllEnergyInfo.Total.data, function(key2){
                AllEnergyInfo.Total.data[key2][0]--;
            });
            AllEnergyInfo.Total.data.push([totalPoints - 1, totalEnergy]);

            //update the time sequence
            AllEnergyInfo.Time.data = AllEnergyInfo.Time.data.slice(1);
            $.each(AllEnergyInfo.Time.data, function(key2){
                AllEnergyInfo.Time.data[key2][0]--;
            });
            AllEnergyInfo.Time.data.push([totalPoints - 1, AllEnergyInfo.Time.data[AllEnergyInfo.Time.data.length - 1][1] + updateInterval / 1000.0]);

            choiceBox.plotAccordingToChoices();
        },
        error: function (err) {
            console.error(err);
        }
    })
};

var update = function() {
    if (!connected) return;
    getEnergyInfo();
    timer = setTimeout(update, updateInterval);
};

choiceBox.init();
linePlot.init();
controlBox.init();