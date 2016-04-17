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
        percent: [[]],
        data: [[]]
    },
    "3G": {
        label: "3G",
        percent: [[]],
        data: [[]]
    },
    "Wifi": {
        label: "Wifi",
        percent: [[]],
        data: [[]]
    },
    "Screen": {
        label: "Screen",
        percent: [[]],
        data: [[]]
    },
    "Time": {
        label: "Time",
        data: [[]]  
    },
    "Total": {
        label: "Total",
        data: [[]]
    }
};

var initEnergyInfo = function() {
    $.each(AllEnergyInfo, function(key, val) {
       while (val.data.length < totalPoints) {
           val.data.push([-1, 0]);
       }
        $.each(val.data, function(idx, val2) {
            val2[0] = idx;
            val2[1] = -0.5;
        });
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