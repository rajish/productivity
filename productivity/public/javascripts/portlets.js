/*** ndigits - field width */
Number.prototype.pad = function(ndigits) {
    var n = this;
    var p = isNaN(ndigits) ? 2 : ndigits;
    var zeros = $.map(new Array(p - 1), function () { return "0"; }).join("");
    if( p > 0)
        return (zeros + n).substring(n.toString().length - p + 1);
    else
        return n;
};

var portletConfig = null;

function storeConfig() {
    portletConfig = $(".column").map(function(index, column) {
        var widgets =  $(column).find(".portlet").map(function(index, widget) {
            return {
                state: $(widget).attr("data-state"),
                name:  $(widget).attr("name"),
                tmpl:  $(widget).attr("data-tmpl")
            };
        });
        widgets.push(null);
        return widgets;
    });

    try {
        // the window.storeConfigAction variable should be defined inline in the template.
        var serializedCfg = "";
        var t = 0;
        for(var i = 0; i < portletConfig.length; i++) {
            var column = portletConfig[i];
            for(var j = 0; j < column.length; j++) {
                var widget = column[j];
                if(widget != null) {
                    serializedCfg += "widgets[" + t + "].state=" + widget['state'] + "&";
                    serializedCfg += "widgets[" + t + "].name=" + widget['name'] + "&";
                    serializedCfg += "widgets[" + t + "].tmpl=" + widget['tmpl'] + "&";
                } else {
                    serializedCfg += "widgets[" + t + "]=null&";
                }
                t++;
            }
        }
        $.ajax({
            type: "POST",
            url: window.storeConfigAction,
            data: serializedCfg
        });
    } catch (ex) {
        alert("Unable to store user preferences.\n" + ex);
    }
}

$(function () {
    //
    // Portlets layout handling
    //
    $( ".column" ).sortable({
        connectWith: ".column",
        forcePlaceholderSize: true,
        opacity: 0.5,
        //axis: "y",
        start: function(event, ui) {
            //$(".ui-sortable-placeholder").css("height", ui.item.css("height"));
            portletConfig = new Array();
        },
        update: function(event, ui) {
            storeConfig();
        }
    });

    $( ".portlet" ).addClass( "ui-widget ui-widget-content ui-helper-clearfix ui-corner-all" )
        .find( ".portlet-header" )
            .addClass( "ui-widget-header ui-corner-all" )
            .end()
        .find(".portlet-header[data-state='unfolded']")
            .prepend( "<span class='ui-icon ui-icon-minusthick'></span>")
            .end()
        .find(".portlet-header[data-state='folded']")
            .prepend( "<span class='ui-icon ui-icon-plusthick'></span>")
            .end();
    $( ".portlet[data-state='folded'] .portlet-content" ).hide();

    $( ".portlet-header .ui-icon" ).click(function() {
        $( this ).toggleClass( "ui-icon-minusthick" ).toggleClass( "ui-icon-plusthick" );
        if($(this).attr("data-state") == "unfolded") {
            $( this ).attr("data-state", "folded");
            $( this ).parents( ".portlet:first" ).attr("data-state", "folded");
        } else {
            $( this ).attr("data-state", "unfolded");
            $( this ).parents( ".portlet:first" ).attr("data-state", "unfolded");
        }
        $( this ).parents( ".portlet:first" ).find( ".portlet-content" ).toggle();
        storeConfig();
    });

    $( ".column" ).disableSelection();
});
