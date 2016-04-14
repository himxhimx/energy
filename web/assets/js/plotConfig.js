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
    },
    xaxis: {
        show: false
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
    "Time": {
        label: "Time",
        data: [[]]  
    },
    "Total": {
        label: "Total",
        data: [[]]
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