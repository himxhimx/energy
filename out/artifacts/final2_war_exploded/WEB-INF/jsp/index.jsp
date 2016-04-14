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
    <link href="assets/css/bootstrap.min.css" rel="stylesheet" type="text/css" />
    <link href="assets/css/bootstrap-theme.min.css" rel="stylesheet" type="text/css" />
    <link href="assets/css/main.css" rel="stylesheet" type="text/css" />
    <link rel="shortcut icon" href="assets/img/favicon.png" />
    <title>$Title$</title>
  </head>
  <body>
    <div class="alert alert-danger alert-dismissible" role="alert" hidden id="alertBar" style="position: absolute; width: 100%;">
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
          <div class="panel panel-primary" style="height: 100px" id="selectChartBox">
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
          <div id="pie-container" class="pie-container panel panel-primary">
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

    <script language="Javascript" type="text/javascript" src="assets/js/jquery-2.2.2.min.js"></script>
    <script language="Javascript" type="text/javascript" src="assets/js/jquery.flot.js"></script>
    <script language="JavaScript" type="text/javascript" src="assets/js/jquery.flot.pie.js"></script>
    <script language="JavaScript" type="text/javascript" src="assets/js/jquery.flot.crosshair.js"></script>
    <script language="javascript" type="text/javascript" src="assets/js/excanvas.min.js"></script>
    <script language="javascript" type="text/javascript" src="assets/js/bootstrap.min.js"></script>

    <script language="JavaScript" type="text/javascript" src="assets/js/getInfoFromBackend.js"></script>
    <script language="JavaScript" type="text/javascript" src="assets/js/plotConfig.js"></script>
    <script language="JavaScript" type="text/javascript" src="assets/js/choiceBox.js"></script>
    <script language="JavaScript" type="text/javascript" src="assets/js/linePlot.js"></script>
    <script language="JavaScript" type="text/javascript" src="assets/js/controlBox.js"></script>
    <script language="JavaScript" type="text/javascript" src="assets/js/timeXaisBar.js"></script>
    <script language="JavaScript" type="text/javascript" src="assets/js/main.js"></script>

  </body>
</html>
