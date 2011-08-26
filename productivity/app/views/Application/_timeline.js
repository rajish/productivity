$(function () {
    var tl;
    var bandInfos = [
        Timeline.createBandInfo({
            width:          "70%",
            intervalUnit:   Timeline.DateTime.MONTH,
            intervalPixels: 100
        }),
        Timeline.createBandInfo({
            width:          "30%",
            intervalUnit:   Timeline.DateTime.YEAR,
            intervalPixels: 200
        })
    ];
    bandInfos[1].syncWith = 0;
    bandInfos[1].highlight = true;
    tl = $(".timeline-main-div").map(function () {
        return Timeline.create(this, bandInfos);
    });

    var resizeTimerID = null;
    $('body').resize(function() {
        if (resizeTimerID == null) {
            resizeTimerID = window.setTimeout(function() {
                resizeTimerID = null;
                tl.map(function() {
                    this.layout();
                });
            }, 500);
        }
    });

    // tl.getBand(0).addOnScrollListener(function(band) {
    //     var minDate = band.getMinDate();
    //     var maxDate = band.getMaxDate();
    //     if (true) {
    //       eventSource.clear();
    //       tl.loadJSON(...);
    //       }
    // });
});