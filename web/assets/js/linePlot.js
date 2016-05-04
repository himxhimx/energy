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
        $("#currentInfoBoxBody").append("<h4 style='font-weight: 600; word-wrap: break-word;'>" + PackageName[packageInfoBox.selectPid] + "</h4>");
        var updateToCurrentInfoBox = function (key, val, notAddToPie) {
            var j;
            // Find the nearest points, x-wise
            for (j = 0; j < val.data.length; ++j) {
                if (val.data[j][0] > pos.x) {
                    break;
                }
            }
            var y, p1 = val.data[j - 1], p2 = val.data[j];
            if (key == "Time") console.log(j, p1, p2);
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

linePlot.previousPoint = null;

linePlot.allowTooltip = function(){
    $("<div id='tooltip'></div>").css({
        position: "absolute",
        display: "none",
        border: "1px solid #fdd",
        padding: "2px",
        "background-color": "#fee",
        opacity: 0.80
    }).appendTo("body");
};


linePlot.bindHover = function() {
    // 绑定提示事件
    $("#placeholder").bind("plothover", function (event, pos, item) {
        var axes = plot.getAxes();
        if (pos.x < axes.xaxis.min || pos.x > axes.xaxis.max ||
            pos.y < axes.yaxis.min || pos.y > axes.yaxis.max) {
            $("#tooltip").hide();
            return;
        }
        var j = pos.x.toFixed(0);
        var tooltipHTML = "";
        $.each(APIInfoList[j], function(key, val) {
            tooltipHTML += "<h5>" + val + "</h5>"
        });
        $("#tooltip").html(tooltipHTML)
            .css({top: 200, left: 350})
            .fadeIn(200);

        /*
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
        }*/
    });
};

linePlot.init = function() {
    this.bindClick();
    this.bindHover();
    this.allowTooltip();
};

linePlot.plotAccordingToChoices = function() {
    var tmpData = isPlaying? AllEnergyInfo:dataWhenPause;
    var data = [];
    choiceBox.choiceContainer.find("input:checked").each(function () {
        var key = $(this).attr("name");
        if (key && tmpData[key]) {
            var tmpd = [[]];
            if (!tmpData[key].data[pid]) {
                for (var i = totalPoints - timeInterval * 10 * 2; i < 300; i++)
                    tmpd.push([i, 0]);
            } else {
                $.each(tmpData[key].data[pid].slice(totalPoints - timeInterval * 10 * 2), function(key, val) {
                    tmpd.push([totalPoints - timeInterval * 10 * 2 + key, val]);
                });
            }
            var tmptmp = {
                label: tmpData[key].label,
                data: tmpd
            };

            data.push(tmptmp);
        }
    });
    if (data.length > 0) {
        plot = $.plot("#placeholder", data, plotOptions);
    }
};
