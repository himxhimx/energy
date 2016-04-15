/**
 * Created by Himx on 14/4/2016.
 *
 * <div id="plotxais" style="position: absolute;top:440px;left:50px"></div>
 */
const timeXaisItemInitPos = [0, 55, 114, 174, 233, 292, 351, 410, 470, 529, 588];
const ntimeXais = 10;
const movePerTick = [1, 5.5, 5.9, 6, 5.9, 5.9, 5.9, 5.9, 6, 5.9, 5.9];
//var timeXais = [];
var nowPosXaisItem = [];
var timeXaisBar = {};

timeXaisBar.endTimeVal = 0;
timeXaisBar.timeXaisInterval = 5;

timeXaisBar.initTimeXais = function() {
    nowPosXaisItem.push(-9);
    nowPosXaisItem = nowPosXaisItem.concat(timeXaisItemInitPos.slice(0, -1));
};

timeXaisBar.init = function() {
    $("#plotxais").empty();
    this.initTimeXais();

    for (var i = 0; i < ntimeXais + 1; i++) {
        $("#plotxais").append("<label class='plotXaisItem' id='plotXaisItem" + i
            + "' style='opacity:0;left:"+ nowPosXaisItem[i] + "'>" + -1 + "</label>");
    }
};


timeXaisBar.update = function(updateTimeInfo) {
    var i;
    if (updateTimeInfo) {
        for (i = 0; i < ntimeXais + 1; i++) {
            nowPosXaisItem[i] -= movePerTick[i] * (this.timeXaisInterval / timeInterval);
        }
        if (nowPosXaisItem[0] <= -10) {
            nowPosXaisItem = timeXaisItemInitPos.slice(0);
            timeXaisBar.endTimeVal = AllEnergyInfo["Time"].data[totalPoints - 1][1];
        }
    }
};

timeXaisBar.draw = function() {
    var timeVal = timeXaisBar.endTimeVal - ntimeXais * timeInterval;
    for (var i = 0; i < ntimeXais + 1; i++) {
        var id = "#plotXaisItem" + i;
        var showable = nowPosXaisItem[i] >= 0 && nowPosXaisItem[i] < timeXaisItemInitPos[ntimeXais] && timeVal >= 0;
        if (isPlaying) {
            $(id).css("left", nowPosXaisItem[i]);
            $(id).css("opacity", showable?1:0);
        }
        if (isPlaying) $(id).text(timeVal);
        timeVal = timeVal + timeInterval;
    }
};