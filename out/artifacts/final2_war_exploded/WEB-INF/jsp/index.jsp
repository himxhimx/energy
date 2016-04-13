<%--
  Created by IntelliJ IDEA.
  User: Himx
  Date: 30/3/2016
  Time: 21:40
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
  <head>
    <script language="javascript" type="text/javascript" src="assets/js/jquery-2.2.2.min.js"></script>
    <script language="javascript" type="text/javascript" src="assets/js/jquery.flot.js"></script>
    <script language="JavaScript" type="text/javascript" src="assets/js/jquery.flot.pie.js"></script>
    <script language="JavaScript" type="text/javascript" src="assets/js/jquery.flot.crosshair.js"></script>

    <script language="javascript" type="text/javascript" src="assets/js/excanvas.min.js"></script>
    <script language="javascript" type="text/javascript" src="assets/js/bootstrap.min.js"></script>

    <script language="JavaScript" type="text/javascript" src="assets/js/getInfoFromBackend.js"></script>
    <script language="JavaScript" type="text/javascript" src="assets/js/plotConfig.js"></script>

    <link href="assets/css/bootstrap.min.css" rel="stylesheet" type="text/css" />
    <link href="assets/css/bootstrap-theme.min.css" rel="stylesheet" type="text/css" />

    <link href="assets/css/main.css" rel="stylesheet" type="text/css" />
    <title>$Title$</title>
  </head>
  <body>
  <div class="alert alert-danger alert-dismissible" role="alert" hidden id="non-connectBox">
    <button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>
    <strong>Warning!</strong> Better check yourself, you're not looking too good.
  </div>
  <div id="header" class="jumbotron">
    <h1 style="margin-left: 20px">Energy Monitor</h1>
    <p style="margin-left: 22px"> Monitor the energy consumption of your mobile devices </p>
  </div>
  <div id="container" class="container">
    <div class="row">
      <div id="deviceInfo" class="deviceInfo col-md-3">
        <div id="controlBoxes" style="margin-right: 3%;text-align:center">
          <button id="controlConnect" class="button raised blue controlBox">
            <span class="glyphicon glyphicon-off" aria-hidden="true" id="connectSpan"></span>
          </button>
          <button id="controlPlay" class="button raised blue controlBox" disabled>
            <span class="glyphicon glyphicon-play" aria-hidden="true" id="playSpan"></span>
          </button>
          <button id="download" class="button raised blue dropdown-toggle controlBox" title="Download the chart"
                  data-toggle="dropdown" aria-haspopup="true" aria-expanded="false" disabled>
            <span class="glyphicon glyphicon-save" aria-hidden="true"></span>
          </button>
          <ul class="dropdown-menu" style="top: inherit; left: 63%;">
            <li><a id="getCanvas" class="downloadMenuItem">Download chart</a></li>
            <li><a id="getPie" class="downloadMenuItem">Download pie-chart</a></li>
            <li><a id="getData" class="downloadMenuItem">Download data</a></li>
          </ul>
        </div>
        <div id="deviceStatus" class="panel panel-primary deviceStatus">
          <div class="panel-heading" style="font-size: 20px">Mobile Information</div>
          <div class="panel-body">
            <table border="0" class="deviceInfoBrief">
              <tr>
                <td colspan="2" id="deviceName">No Device Connected</td>
              </tr>
              <tr class="deviceInfoBriefLine">
                <td class="deviceInfoBriefKey">Status:</td>
                <td class="deviceInfoBriefVal" id="deviceInfoBriefStatus"> Unknown </td>
              </tr>
              <tr class="deviceInfoBriefLine">
                <td class="deviceInfoBriefKey">Battery:</td>
                <td class="deviceInfoBriefVal" id="deviceInfoBriefBattery"> Unknown </td>
              </tr>
              <tr class="deviceInfoBriefLine">
                <td class="deviceInfoBriefKey">RAM:</td>
                <td class="deviceInfoBriefVal" id="deviceInfoBriefRAM"> Unknown </td>
              </tr>
            </table>
          </div>
        </div>
      </div>
      <div id="p-container" class="p-container col-md-6">
        <div id="placeholder" class="placeholder"></div>
        <div id="plotxais" style="position: absolute;top:440px;left:50px"></div>
      </div>
      <div id="container-right" class="col-md-3">
        <div class="panel panel-primary" style="height: 100px">
          <div class="panel-heading" style="font-size: 20px">Chart selected</div>
          <div class="panel-body">
            <p id="plotChoices"></p>
          </div>
        </div>
        <div id="currentInfoBox" class="panel panel-primary">
          <div class="panel-heading" style="font-size: 20px">Instant Information</div>
          <div class="panel-body" id="currentInfoBoxBody">
          </div>
        </div>
        <div id="pie-container" class="pie-container panel panel-primary ">
          <div id="pie-placeholder"></div>
          <div class="panel-footer" style="width: 100%;text-align: center;font-size: 20px;background-color: #337ab7;color: white;">
            Percentage of Energy Used
          </div>
        </div>
      </div>
    </div>
  </div>
  <div id="footer">
    <h6 style="text-align: center">
      &copy; Copyright 2016 Himx - All rights reserved.
    </h6>
  </div>
  <script type="text/javascript">
    'use strict';
    //generate Choice boxes
    var choiceContainer = $("#plotChoices");
    $.each(AllEnergyInfo, function(key, val){
      if (AllEnergyInfo[key].label !== "Total") {
        choiceContainer.append("<input type='checkbox' class='infoChoose' name='" + key +
                "' checked='checked' id='id" + key + "' disabled />" +
                "<label for='id" + key + "'>"
                + key + "</label>" + "&nbsp;");
      }
      for (let i = 0; i < totalPoints; i++) {
        val.data.push([i, 0]);
      }
    });

    choiceContainer.find("input").click(plotAccordingToChoices);

    function plotAccordingToChoices() {
      let tmpData = isPlaying? AllEnergyInfo:dataWhenPause;
      let data = [];
      choiceContainer.find("input:checked").each(function () {
        let key = $(this).attr("name");
        if (key && tmpData[key]) {
          data.push(tmpData[key]);
        }
      });
      if (data.length > 0) {
        plot = $.plot("#placeholder", data, plotOptions);
      }
    }

    var getEnergyInfo = function () {
      //console.log("getEnergyInfo");
      $.ajax({
        type: "GET",
        url: "/getEnergyInfo.do",
        success: function(data) {
          //console.log(data);
          if (!data) return;
          data = JSON.parse(data);
          let totalEnergy = 0;

          if (data.status == -1) {
            $.ajax({
              method: 'GET',
              url: 'getDevices.do',
              success: function (data) {
                var jData = JSON.parse(data);
                if (jData.deviceName === "") {
                  disconnectHandler();
                }
              },
              error: function(err) {
                console.error(err);
              }
            });
          }

          $.each(data, function(key){
            if (key === 'status') return;
            AllEnergyInfo[key].data = AllEnergyInfo[key].data.slice(1);
            totalEnergy += data[key];
            $.each(AllEnergyInfo[key].data, function(key2){
              AllEnergyInfo[key].data[key2][0]--;
            });
            AllEnergyInfo[key].data.push([totalPoints - 1, data[key]]);
          });

          AllEnergyInfo.Total.data = AllEnergyInfo.Total.data.slice(1);
          $.each(AllEnergyInfo.Total.data, function(key2){
            AllEnergyInfo.Total.data[key2][0]--;
          });
          AllEnergyInfo.Total.data.push([totalPoints - 1, totalEnergy]);
          plotAccordingToChoices();
        },
        error: function (err) {
          console.error(err);
        }
      })
    };

    $("#placeholder").bind("plotclick", function (event, pos, item) {
      $("#currentInfoBoxBody").empty();
      let axes = plot.getAxes();
      if (pos.x < axes.xaxis.min || pos.x > axes.xaxis.max ||
              pos.y < axes.yaxis.min || pos.y > axes.yaxis.max) {
        return;
      }
      let dataset = plot.getData();
      let piePlotData = [];
      $.each(dataset, function(key, val){
        let j;
        // Find the nearest points, x-wise
        for (j = 0; j < val.data.length; ++j) {
          if (val.data[j][0] > pos.x) {
            break;
          }
        }
        let y, p1 = val.data[j - 1], p2 = val.data[j];
        if (p1 == null) {
          y = p2[1];
        } else if (p2 == null) {
          y = p1[1];
        } else {
          y = parseFloat(p1[1]) + parseFloat((p2[1] - p1[1]) * (pos.x - p1[0]) / (p2[0] - p1[0]));
        }
        $("#currentInfoBoxBody").append("<h6>" + val.label + ":" + y.toFixed(2) + "</h6>");
        piePlotData.push({label: val.label, data: y});
      });
      piePlot = $.plot("#pie-placeholder", piePlotData, pieplotOption);
    });

    var previousPoint = null;
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
    function update() {
      if (!connected) return;
      getEnergyInfo();
      updateTimeXaisBar();
      timer = setTimeout(update, updateInterval);
    }

    connectHandler = function() {
      console.log("connect");
      $.each(AllEnergyInfo, function (key, val) {
        for (let i = 0; i < totalPoints; i++) {
          val.data[i][1] = 0;
        }
      });
      $.ajax({
        type: "GET",
        url: "/getDevices.do",
        success: function (data) {
          var jData = JSON.parse(data);
          if (jData.deviceName === "") {
            $("#deviceName").text("No Device Connected");
            $(".deviceInfoBriefVal").text("Unknown");
            $("#non-connectBox").show();
          } else {
            $("#deviceName").text(jData.deviceName);
            $("#deviceInfoBriefStatus").text(jData.status === "device" ? "Online" : "Offline");
            connected = true;
            isPlaying = true;
            $("#controlConnect").unbind("click", connectHandler);
            $("#controlConnect").click(disconnectHandler);
            $("#connectSpan").css("color", "white");
            $("#controlPlay").removeAttr("disabled");
            $("#download").removeAttr("disabled");
            $("#playSpan").attr("class", "glyphicon glyphicon-pause");
            $("#plotChoices").show();
            $(".infoChoose").removeAttr("disabled");
            initTimeXaisBar();
            update();
          }
        },
        error: function (err) {
          console.error(err);
        }
      });
      /*
      connected = true;
      isPlaying = true;
      $(this).click(disconnectHandler);
      $("#connectSpan").css("color", "white");
      $("#controlPlay").removeAttr("disabled");
      $("#download").removeAttr("disabled");
      $("#playSpan").attr("class", "glyphicon glyphicon-pause");
      $("#plotChoices").show();
      $(".infoChoose").removeAttr("disabled");
      update();*/
    };

    disconnectHandler = function () {
      console.log("disconnect");
      connected = false;
      isPlaying = false;
      clearTimeout(timer);
      $("#controlConnect").unbind("click", disconnectHandler);
      $("#controlConnect").click(connectHandler);
      $("#deviceName").text("No Device Connected");
      $(".deviceInfoBriefVal").text("Unknown");
      $("#connectSpan").css("color", "#9d9d9d");
      $("#controlPlay").attr("disabled", true);
      $("#playSpan").attr("class", "glyphicon glyphicon-play");
      $("#download").attr("disabled", true);
      $(".infoChoose").attr("disabled", true);
    };

    playHandler = function() {
      isPlaying = true;
      $(this).click(stopHandler);
      $("#playSpan").attr("class", "glyphicon glyphicon-pause");
    };

    stopHandler = function () {
      isPlaying = false;
      dataWhenPause = {};
      $.each(AllEnergyInfo, function(key, val) {
        dataWhenPause[key] = {
          label: val.label,
          data: [[]]
        };
        $.each(val.data, function(idx, val2) {
          dataWhenPause[key].data.push([val2[0], val2[1]]);
        })
      });
      $(this).click(playHandler);
      $("#playSpan").attr("class", "glyphicon glyphicon-play");
    };

    $("#controlPlay").click(stopHandler);
    $("#controlConnect").click(connectHandler);

    $("#getCanvas").click(function (){
      if (!plot) return;
      var myCanvas = plot.getCanvas();
      document.location.href= myCanvas.toDataURL("image/png").replace("image/png", "image/octet-stream;Content-Disposition:attachment;filename=foobar.png");
    });
    $("#getPie").click(function(){
      if (!piePlot) return;
      var myCanvas = piePlot.getCanvas();
      document.location.href= myCanvas.toDataURL("image/png").replace("image/png", "image/octet-stream");
    });
    $("#getData").click(function () {
      document.location.href = AllEnergyInfo.toString();
    });
    initTimeXaisBar();
    setInterval(updateTimeXaisBar, 500);
  </script>
  </body>
</html>
