/**
 * Created by Himx on 30/3/2016.
 * 
 */
var getLogcat = function () {
    $.ajax({
        type: "GET",
        url: "/getLogcat.do",
        success: function(data) {
            $("#deviceLogger").val(data);
            $("#deviceLogger").scrollTop = $("#deviceLogger").scrollHeight;
        },
        error: function (err) {
            console.error(err);
        }
    });
};

