/**
 * Created by Himx on 14/4/2016.
 * 
 */

choiceBox.choiceContainer = $("#plotChoices");

choiceBox.init = function() {
    $.each(AllEnergyInfo, function(key){
        if (AllEnergyInfo[key].label !== "Total" && AllEnergyInfo[key].label !== "Time") {
            choiceBox.choiceContainer.append("<input type='checkbox' class='infoChoose' name='" + key +
                "' checked='checked' id='id" + key + "' disabled />" +
                "<label for='id" + key + "'>"
                + key + "</label>" + "&nbsp;");
        }
    });

    var timeIntervalSelectBox = "<select id='timeIntervalSelectBox' class='infoChoose' disabled>";
    for (var i = 1; i <= 10; i++) {
        var selected = (i==5?"selected='selected'":"");
        timeIntervalSelectBox += "<option value='" + i + "'" + selected + ">" + i + "s</option>";
    }
    timeIntervalSelectBox += "</select>";
    this.choiceContainer.append(timeIntervalSelectBox);
    this.choiceContainer.find(".infoChoose>input").click(linePlot.plotAccordingToChoices);
    $("#timeIntervalSelectBox").change(function() {
        var tmp = document.getElementById("timeIntervalSelectBox");
        timeInterval = parseInt(tmp.options[tmp.selectedIndex].value);
        linePlot.plotAccordingToChoices();
        timeXaisBar.draw();
    });
};


