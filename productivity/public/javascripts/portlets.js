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
    // TODO: store user layout changes with AJAX post
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
