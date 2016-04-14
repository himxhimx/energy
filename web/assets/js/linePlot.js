/**
 * Created by Himx on 14/4/2016.
 *
 */

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

var linePlot = {};
linePlot.bindClick = function() {
    $("#placeholder").bind("plotclick", function (event, pos, item) {
        $("#currentInfoBoxBody").empty();
        var axes = plot.getAxes();
        if (pos.x < axes.xaxis.min || pos.x > axes.xaxis.max ||
            pos.y < axes.yaxis.min || pos.y > axes.yaxis.max) {
            return;
        }
        var dataset = plot.getData();
        var piePlotData = [];
        var updateToCurrentInfoBox = function (key, val, notAddToPie) {
            var j;
            // Find the nearest points, x-wise
            for (j = 0; j < val.data.length; ++j) {
                if (val.data[j][0] > pos.x) {
                    break;
                }
            }
            var y, p1 = val.data[j - 1], p2 = val.data[j];
            if (p1 == null) {
                y = p2[1];
            } else if (p2 == null) {
                y = p1[1];
            } else {
                y = parseFloat(p1[1]) + parseFloat((p2[1] - p1[1]) * (pos.x - p1[0]) / (p2[0] - p1[0]));
            }
            $("#currentInfoBoxBody").append("<h6>" + val.label + ":" + y.toFixed(2) + "</h6>");
            if (!notAddToPie) piePlotData.push({label: val.label, data: y});
        };
        updateToCurrentInfoBox("Time", isPlaying?AllEnergyInfo.Time:dataWhenPause.Time, true);
        $.each(dataset, updateToCurrentInfoBox);
        piePlot = $.plot("#pie-placeholder", piePlotData, pieplotOption);
    });
};

var previousPoint = null;
linePlot.bindHover = function() {
    // 绑定提示事件
    $("#placeholder").bind("plothover", function (event, pos, item) {
        if (item) {
            if (previousPoint != item.dataIndex) {
                previousPoint = item.dataIndex;
                $("#tooltip").remove();
                var tip = "展现量：";
                showTooltip(item.pageX, item.pageY,tip+item.datapoint[1]);
            }
        }
        else {
            $("#tooltip").remove();
            previousPoint = null;
        }
    });
};

linePlot.init = function() {
    this.bindClick();
    this.bindHover();
}
