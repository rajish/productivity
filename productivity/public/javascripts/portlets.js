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
            $(".ui-sortable-placeholder").css("height", ui.item.css("height"));
        },
        update: function(event, ui) {
            // TODO: (re)store user layout changes
            portletConfig = $( ".column" ).sortable("serialize");
        }
    });

    $( ".portlet" ).addClass( "ui-widget ui-widget-content ui-helper-clearfix ui-corner-all" )
        .find( ".portlet-header" )
            .addClass( "ui-widget-header ui-corner-all" )
            .prepend( "<span class='ui-icon ui-icon-minusthick'></span>")
            .end()
        .find( ".portlet-content" );

    $( ".portlet-header .ui-icon" ).click(function() {
        $( this ).toggleClass( "ui-icon-minusthick" ).toggleClass( "ui-icon-plusthick" );
        $( this ).parents( ".portlet:first" ).find( ".portlet-content" ).toggle();
    });

    $( ".column" ).disableSelection();
});
