/**
 * Created by Himx on 14/4/2016.
 * 
 * <div id="controlBoxes" style="margin-right: 3%;text-align:center">
 *  <button id="controlConnect" class="button raised blue controlBox">
 *      <span class="glyphicon glyphicon-off" aria-hidden="true" id="connectSpan"></span>
 *  </button>
 *  <button id="controlPlay" class="button raised blue controlBox" disabled>
 *       <span class="glyphicon glyphicon-play" aria-hidden="true" id="playSpan"></span>
 *  </button>
 *  <button id="download" class="button raised blue dropdown-toggle controlBox" title="Download the chart"
 *      data-toggle="dropdown" aria-haspopup="true" aria-expanded="false" disabled>
 *      <span class="glyphicon glyphicon-save" aria-hidden="true"></span>
 *  </button>
 *  <ul class="dropdown-menu" style="top: inherit; left: 63%;">
 *      <li><a id="getCanvas" class="downloadMenuItem">Download chart</a></li>
 *      <li><a id="getPie" class="downloadMenuItem">Download pie-chart</a></li>
 *      <li><a id="getData" class="downloadMenuItem">Download data</a></li>
 *  </ul>
 * </div>
 */

controlBox.connectHandler = function() {
    console.log("connect");
    initEnergyTime();
    $.ajax({
        type: "GET",
        url: "/getDevices",
        success: function (data) {
            var jData = JSON.parse(data);
            if (jData["deviceName"] === "") {
                $("#deviceName").text("No Device Connected");
                $(".deviceInfoBriefVal").text("Unknown");
                $("#alertBar").show();
            } else {
                $("#deviceName").text(jData["deviceName"]);
                $("#deviceInfoBriefStatus").text(jData.status === "device" ? "Online" : "Offline");
                connected = true;
                isPlaying = true;
                pid = 0;
                $("#controlConnect").unbind("click", controlBox.connectHandler);
                $("#controlConnect").click(controlBox.disconnectHandler);
                $("#connectSpan").css("color", "white");
                $("#controlPlay").removeAttr("disabled");
                $("#download").removeAttr("disabled");
                $("#playSpan").attr("class", "glyphicon glyphicon-pause");
                $("#plotChoices").show();
                $(".infoChoose").removeAttr("disabled");
                console.log(AllEnergyInfo);
                timeXaisBar.init();
                PackageName = [];
                initEnergyInfo(0);
                PackageName[0] = "All";
                $.each(jData["packageList"], function(key, val) {
                    PackageName[val["Pid"]] = val["Name"];
                    initEnergyInfo(val["Pid"]);
                });

                console.log(AllEnergyInfo);
                packageInfoBox.init();
                update();
            }
        },
        error: function (err) {
            console.error(err);
        }
    });
};

controlBox.disconnectHandler = function () {
    console.log("disconnect");
    connected = false;
    isPlaying = false;
    clearTimeout(timer);
    packageInfoBox.clear();
    $("#controlConnect").unbind("click", controlBox.disconnectHandler);
    $("#controlConnect").click(controlBox.connectHandler);
    $("#deviceName").text("No Device Connected");
    $(".deviceInfoBriefVal").text("Unknown");
    $("#connectSpan").css("color", "#9d9d9d");
    $("#controlPlay").attr("disabled", true);
    $("#playSpan").attr("class", "glyphicon glyphicon-play");
    $("#download").attr("disabled", true);
    $(".infoChoose").attr("disabled", true);
};

controlBox.playHandler = function() {
    console.log("play");
    isPlaying = true;
    packageInfoBox.draw();
    $("#controlPlay").unbind("click", controlBox.playHandler);
    $("#controlPlay").click(controlBox.stopHandler);
    $("#playSpan").attr("class", "glyphicon glyphicon-pause");
};

controlBox.stopHandler = function () {
    console.log("stop");
    isPlaying = false;
    dataWhenPause = {};
    $.each(AllEnergyInfo, function(key, val) {
        if (key === "Time") return;
        dataWhenPause[key] = {
            label: val.label,
            data: [[]]
        };
        $.each(val.data, function(key2, val2) {
            if (!val2) return;
            dataWhenPause[key].data[key2] = val2.slice(0);
        })
    });
    dataWhenPause["Time"] = {
        label: "Time",
        data: [[]]
    };

    $.each(AllEnergyInfo["Time"].data, function(key, val) {
        dataWhenPause["Time"].data.push([val[0], val[1]]);
    });

    APIInfoListWhenStop = [];
    $.each(APIInfoList, function(key, val) {
        if (val["Pid"]) {
            APIInfoListWhenStop[key] = {
                Pid: val["Pid"],
                APIList: val["APIList"].slice(0)
            }
        } else {
            APIInfoListWhenStop[key] = {};
        }
    });

    $("#controlPlay").unbind("click", controlBox.stopHandler);
    $("#controlPlay").click(controlBox.playHandler);
    $("#playSpan").attr("class", "glyphicon glyphicon-play");
};

controlBox.downloadLinePlot = function() {
    if (!plot) return;
    var myCanvas = plot.getCanvas();
    document.location.href= myCanvas.toDataURL("image/png").replace("image/png", "image/octet-stream;Content-Disposition:attachment;filename=foobar.png");
};

controlBox.downloadPiePlot = function() {
    if (!piePlot) return;
    var myCanvas = piePlot.getCanvas();
    document.location.href= myCanvas.toDataURL("image/png").replace("image/png", "image/octet-stream");
};

controlBox.downloadData = function() {
    document.location.href = AllEnergyInfo.toString();
};

controlBox.init = function() {
    $("#controlPlay").click(this.stopHandler);
    $("#controlConnect").click(this.connectHandler);

    $("#getCanvas").click(this.downloadLinePlot);
    $("#getPie").click(this.downloadPiePlot);
    $("#getData").click(this.downloadData);
};