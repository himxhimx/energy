/**
 * Created by Himx on 16/4/2016.
 * <div id="packageInfoBox" class="panel panel-primary deviceStatus" style="height: 345px;">
 *   <div class="panel-heading" style="font-size: 20px">Packages Running</div>
 *     <div class="panel-body" style="padding: 0; height: 294px; overflow: scroll; overflow-x: hidden;">
 *       <table id="packageList" class="table table-hover table-condensed">
 *
 *       </table>
 * </div>
 * </div>
 */

packageInfoBox.container = $("#packageList");
packageInfoBox.selectPid = 0;

packageInfoBox.clickHandler = function() {
    console.log("click");
    var idx = this.id;
    $(".packageListItemSpan").css("opacity", 0);
    var span = "#" + idx + ">td>.packageListItemSpan";
    $(span).css("opacity", 1);
    packageInfoBox.selectPid = parseInt($(span).attr("id"));
    pid = packageInfoBox.selectPid;
    linePlot.plotAccordingToChoices();
};

packageInfoBox.init = function(){
    packageInfoBox.draw();
};

packageInfoBox.draw = function() {
    this.clear();

    $.each(PackageName, function(key, val) {
        if (!val) return;
        packageInfoBox.container.append("" +
            "<tr class='packageListItem' id='packageListItem" + (key) + "'>" +
            "<td style='width: 90%'>"+ val + "</td> " +
            "<td> <span class='glyphicon glyphicon-eye-open packageListItemSpan' style='opacity:0;' id='" + key + "'> </span> </td> " +
            "</tr>");
    });
    $(".packageListItem").click(packageInfoBox.clickHandler);
    $("#packageListItem" + pid + ">td>span").css("opacity", 1);
};

packageInfoBox.update = function(ProcessChange) {
    if (!ProcessChange) return;
    console.log("PackageInfoBox.update", ProcessChange);
    $.each(ProcessChange["ProcessCreate"], function(key, val) {
        console.log("PackageInfoBox.update-create", val["Pid"]);
        PackageName[val["Pid"]] = val["Name"];
        initEnergyInfo(val["Pid"]);
        if (isPlaying) {
            packageInfoBox.container.append("" +
                "<tr class='packageListItem' id='packageListItem" + val["Pid"] + "'>" +
                "<td style='width: 90%'>"+ val["Name"] + "</td> " +
                "<td> <span class='glyphicon glyphicon-eye-open packageListItemSpan' style='opacity:0;' id='" + val["Pid"] + "'> </span> </td> " +
                "</tr>");
        }
        $("#packageListItem" + val["Pid"]).click(packageInfoBox.clickHandler);
    });
    $.each(ProcessChange["ProcessDestroy"], function(key, val) {
        console.log("PackageInfoBox.update-destroy", val["Pid"]);
        PackageName[val["Pid"]] = null;
        clearEnergyInfo(val["Pid"]);
        if (isPlaying) {
            $("#packageListItem" + val["Pid"]).remove();
        }
    });
};

packageInfoBox.clear = function() {
    this.container.empty();
};