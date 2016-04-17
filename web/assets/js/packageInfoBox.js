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

    $(".packageListItem").click(function(){
        var idx = this.id;
        $(".packageListItemSpan").css("opacity", 0);
        var span = "#" + idx + ">td>.packageListItemSpan";
        $(span).css("opacity", 1);
        pid = $(span).attr("id");
        packageInfoBox.selectPid = parseInt(pid);
        console.log(packageInfoBox.selectPid);
        linePlot.plotAccordingToChoices();
    });
};

packageInfoBox.update = function(ProcessChange) {
    if (!ProcessChange) return;
    console.log(ProcessChange);
    $.each(ProcessChange["ProcessCreate"], function(key, val) {
        PackageName[val["Pid"]] = val["Name"];
        if (isPlaying) {
            packageInfoBox.container.append("" +
                "<tr class='packageListItem' id='packageListItem" + val["Pid"] + "'>" +
                "<td style='width: 90%'>"+ val["Name"] + "</td> " +
                "<td> <span class='glyphicon glyphicon-eye-open packageListItemSpan' style='opacity:0;' id='" + val["Pid"] + "'> </span> </td> " +
                "</tr>");
        }
    });
    $.each(ProcessChange["ProcessDestroy"], function(key, val) {
        PackageName = PackageName.concat(PackageName.slice(0, val["Pid"]) + PackageName.slice(val["Pid"] + 1));
        if (isPlaying) {
            $("#packageListItem" + val["Pid"]).remove();
        }
    });
};

packageInfoBox.clear = function() {
    this.container.empty();
};