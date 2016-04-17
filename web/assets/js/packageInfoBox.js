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

var packageInfoBox = {};

packageInfoBox.container = $("#packageList");

packageInfoBox.init = function() {
    this.container.empty();
    this.container.append("" +
        "<tr class='packageListItem' id='packageListItem0'> " +
        "<td style='width: 90%'> All </td> <td> " +
        "<span class='glyphicon glyphicon-eye-open packageListItemSpan'> </span> </td> " +
        "</tr>");
    for (var i = 1; i <= 25; i++)
        this.container.append("" +
            "<tr class='packageListItem' id='packageListItem" + i + "'>" +
            "<td style='width: 90%'> com.example.himx.package1 </td> " +
            "<td> <span class='glyphicon glyphicon-eye-open packageListItemSpan' style='opacity:0;' id='" + i+ "'> </span> </td> " +
            "</tr>");
    $(".packageListItem").click(function(){
        var idx = this.id;
        $(".packageListItemSpan").css("opacity", 0);
        var span = "#" + idx + ">td>.packageListItemSpan";
        $(span).css("opacity", 1);
        pid = $(span).attr("id");
        console.log("pid", pid);
    });
};

