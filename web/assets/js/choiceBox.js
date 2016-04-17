/**
 * Created by Himx on 14/4/2016.
 * 
 */


var choiceBox = {};

choiceBox.choiceContainer = $("#plotChoices");

choiceBox.plotAccordingToChoices = function() {
    var _tmpData = isPlaying? AllEnergyInfo:dataWhenPause;
    var tmpData = {};
    $.each(_tmpData, function(key, val) {
        tmpData[key] = {
            label: val.label,
            percent: val.percent,
            data: [[]]
        };
        $.each(val.data, function(idx, val2) {
            tmpData[key].data.push([val2[0], val2[1]]);
        })
    });
    var data = [];
    this.choiceContainer.find("input:checked").each(function () {
        var key = $(this).attr("name");
        if (key && tmpData[key]) {
            //data.push(tmpData[key]);
            var tmptmp = {
                label: tmpData[key].label,
                data: tmpData[key].data.slice(totalPoints - timeInterval * 10 * 2)
            };

            $.each(tmptmp.data, function(key2){
               tmptmp.data[key2][1] *= tmpData[key].percent;
            });

            data.push(tmptmp);
        }
    });
    if (data.length > 0) {
        plot = $.plot("#placeholder", data, plotOptions);
    }
};

choiceBox.init = function() {
    $.each(AllEnergyInfo, function(key){
        if (AllEnergyInfo[key].label !== "Total" && AllEnergyInfo[key].label !== "Time") {
            choiceBox.choiceContainer.append("<input type='checkbox' class='infoChoose' name='" + key +
                "' checked='checked' id='id" + key + "' disabled />" +
                "<label for='id" + key + "'>"
                + key + "</label>" + "&nbsp;");
        }
    });

    var timeIntervalSelectBox = "<select id='timeIntervalSelectBox'>";
    for (var i = 1; i <= 10; i++) {
        var selected = (i==5?"selected='selected'":"");
        timeIntervalSelectBox += "<option value='" + i + "'" + selected + ">" + i + "s</option>";
    }
    timeIntervalSelectBox += "</select>";
    this.choiceContainer.append(timeIntervalSelectBox);
    this.choiceContainer.find(".infoChoose").click(this.plotAccordingToChoices);
    $("#timeIntervalSelectBox").change(function() {
        var tmp = document.getElementById("timeIntervalSelectBox");
        timeInterval = parseInt(tmp.options[tmp.selectedIndex].value);
        choiceBox.plotAccordingToChoices();
        timeXaisBar.draw();
    });
};


