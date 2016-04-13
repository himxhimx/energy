/**
 * Created by Himx on 8/4/2016.
 * 
 */
const updateInterval = 500;
const totalPoints = 100;
const plotOptions = {
    series: {
        shadowSize: 0	// Drawing is faster without shadows
    },
    grid: {
        hoverable: true,
        clickable: true
    },
    lines: {
        fill: true
    },
    crosshair: {
        mode: "x"
    },
    yaxis: {
        min: 0,
        max: 1000
    }
};

const pieplotOption = {
    series: {
        pie: {
            show: true,
            combine: {
                color: "#999",
                threshold: 0.05
            }
        }
    },
    legend: {
        show: false
    }
};

var AllEnergyInfo = {
    "CPU": {
        label: "CPU",
        data: [[]]
    },
    "3G": {
        label: "3G",
        data: [[]]
    },
    "Wifi": {
        label: "Wifi",
        data: [[]]
    },
    "Screen": {
        label: "Screen",
        data: [[]]
    },
    "Total": {
        label: "Total",
        data: [[]]
    }
};

const timeXaisItemInitPos = [0, 55, 114, 174, 233, 292, 351, 410, 470, 529, 588];
const ntimeXais = 10;
const timeXaisInterval = 10;
const movePerTick = [1, 5.5, 5.9, 6, 5.9, 5.9, 5.9, 5.9, 6, 5.9, 5.9];
var timeXais = [];
var nowPosXaisItem = [];

var initTimeXais = function() {
    var tmp = 100;
    for (var i = ntimeXais; i >= 0; i--) {
        timeXais[i] = tmp;
        tmp -= 10;
    }
    nowPosXaisItem = timeXaisItemInitPos.slice(0);
};

var initTimeXaisBar = function() {
    $("#plotxais").empty();
    initTimeXais();
    $.each(timeXais, function(key, val) {
        var showable = (val>=0?1:0);
       $("#plotxais").append("<label class='plotXaisItem' id='plotXaisItem" + key
           + "' style='opacity:" + showable + ";left:"+ timeXaisItemInitPos[key] + "'>" + val + "</label>")
    });
};

var updateTimeXaisBar = function() {
    var i;
    for (i = 0; i < ntimeXais + 1; i++) {
        nowPosXaisItem[i] -= movePerTick[i];
    }
    if (nowPosXaisItem[0] == -10) {
        timeXais = timeXais.slice(1);
        timeXais.push(timeXais[timeXais.length - 1] + timeXaisInterval);
        nowPosXaisItem = timeXaisItemInitPos.slice(0);
    }
    for (i = 0; i < ntimeXais + 1; i++) {
        var id = "#plotXaisItem" + i;
        $(id).css("left", nowPosXaisItem[i]);
        var showable = nowPosXaisItem[i] >= 0 && nowPosXaisItem[i] < timeXaisItemInitPos[ntimeXais];
        $(id).css("opacity", showable?1:0);
        $(id).text(timeXais[i]);
    }
};

// 节点提示
function showTooltip(x, y, contents) {
    /*
    $('<div id="tooltip">' + contents + '</div>').css( {
        position: 'absolute',
        display: 'none',
        top: y + 10,
        left: x + 10,
        border: '1px solid #fdd',
        padding: '2px',
        'background-color': '#dfeffc',
        opacity: 0.80
    }).appendTo("body").fadeIn(200);
    */
    
}

var dataWhenPause;

var plot = null;
var piePlot = null;

var isPlaying = true;
var connected = false;

var connectHandler;
var disconnectHandler;
var playHandler;
var stopHandler;

var timer;