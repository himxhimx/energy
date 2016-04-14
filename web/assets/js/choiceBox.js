/**
 * Created by Himx on 14/4/2016.
 * 
 */

var choiceBox = {};

choiceBox.choiceContainer = $("#plotChoices");

choiceBox.plotAccordingToChoices = function() {
    var tmpData = isPlaying? AllEnergyInfo:dataWhenPause;
    var data = [];
    this.choiceContainer.find("input:checked").each(function () {
        var key = $(this).attr("name");
        if (key && tmpData[key]) {
            data.push(tmpData[key]);
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
    this.choiceContainer.find("input").click(this.plotAccordingToChoices);
};


