/**
 * Created by Himx on 8/4/2016.
 * 
 */
const updateInterval = 500;
const totalPoints = 300;

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

var PackageName = [];

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
    "Time": {
        label: "Time",
        data: [[]]  
    }
};

var APIInfoList = [];
var APIInfoListWhenStop = [];

var initEnergyInfo = function(thePid) {
    $.each(AllEnergyInfo, function(key, val) {
        if (key === "Time") return;
        val.data[thePid] = new Array(totalPoints);
        $.each(val.data[thePid], function(idx) {
            val.data[thePid][idx] = 0;
        });
    });
    APIInfoList = new Array(totalPoints);
};

var initEnergyTime = function() {
    AllEnergyInfo.Time.data = [];
    while (AllEnergyInfo.Time.data.length < totalPoints) {
        AllEnergyInfo.Time.data.push([-1, -0.5]);
    }
    $.each(AllEnergyInfo.Time.data, function(idx, val2) {
        val2[0] = idx;
        val2[1] = -0.5;
    });
};

var clearEnergyInfo = function(thePid) {
    $.each(AllEnergyInfo, function(key) {
       if (key === "Time") return;
        AllEnergyInfo[key].data[thePid] = null;
    });
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

var timeInterval = 5;

var dataWhenPause;

var plot = null;
var piePlot = null;

var isPlaying = true;
var connected = false;

var timer;

var pid = 0;

var packageInfoBox = {};
var choiceBox = {};
var controlBox = {};
var linePlot = {};
var timeXaisBar = {};