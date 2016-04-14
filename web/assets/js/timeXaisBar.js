/**
 * Created by Himx on 14/4/2016.
 *
 * <div id="plotxais" style="position: absolute;top:440px;left:50px"></div>
 */
const timeXaisItemInitPos = [0, 55, 114, 174, 233, 292, 351, 410, 470, 529, 588];
const ntimeXais = 10;
const timeXaisInterval = 5;
const movePerTick = [1, 5.5, 5.9, 6, 5.9, 5.9, 5.9, 5.9, 6, 5.9, 5.9];
var timeXais = [];
var nowPosXaisItem = [];
var timeXaisBar = {};

var initTimeXais = function() {
    var tmp = 0;
    for (var i = ntimeXais; i >= 0; i--) {
        timeXais[i] = tmp;
        tmp -= timeXaisInterval;
    }
    nowPosXaisItem = timeXaisItemInitPos.slice(0);
};

timeXaisBar.init = function() {
    $("#plotxais").empty();
    initTimeXais();
    $.each(timeXais, function(key, val) {
        var showable = (val>=0?1:0);
        $("#plotxais").append("<label class='plotXaisItem' id='plotXaisItem" + key
            + "' style='opacity:" + showable + ";left:"+ timeXaisItemInitPos[key] + "'>" + val + "</label>")
    });
};

timeXaisBar.update = function() {
    var i;
    for (i = 0; i < ntimeXais + 1; i++) {
        nowPosXaisItem[i] -= movePerTick[i];
    }
    if (nowPosXaisItem[0] == -10) {
        timeXais = timeXais.slice(1);
        timeXais.push(timeXais[timeXais.length - 1] + timeXaisInterval);
        nowPosXaisItem = timeXaisItemInitPos.slice(0);
    }
    if (!isPlaying) return;
    for (i = 0; i < ntimeXais + 1; i++) {
        var id = "#plotXaisItem" + i;
        $(id).css("left", nowPosXaisItem[i]);
        var showable = nowPosXaisItem[i] >= 0 && nowPosXaisItem[i] < timeXaisItemInitPos[ntimeXais] && timeXais[i] >= 0;
        $(id).css("opacity", showable?1:0);
        $(id).text(timeXais[i]);
    }
};