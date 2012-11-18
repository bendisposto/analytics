
var tl;
function onLoad() {
  var tl_el = document.getElementById("tl");
  var eventSource1 = new Timeline.DefaultEventSource();
  
  var theme1 = Timeline.ClassicTheme.create();
  theme1.autoWidth = true; // Set the Timeline's "width" automatically.
  // Set autoWidth on the Timeline's first band's theme,
  // will affect all bands.
  theme1.timeline_start = new Date(Date.UTC(2000, 0, 1));
  theme1.timeline_stop  = new Date(Date.UTC(2100, 0, 1));
  
  var d = Timeline.DateTime.parseGregorianDateTime("2012")
  var bandInfos = [
    Timeline.createBandInfo({
      width:          45, // set to a minimum, autoWidth will then adjust
      intervalUnit:   Timeline.DateTime.DECADE, 
      intervalPixels: 600,
      eventSource:    eventSource1,
      date:           d,
      theme:          theme1,
      layout:         'original'  // original, overview, detailed
    })
  ];
  
  // create the Timeline
  tl = Timeline.create(tl_el, bandInfos, Timeline.HORIZONTAL);
  
  var url = '.'; // The base url for image, icon and background image
  // references in the data

 var timeline_data = {}
 
  $.ajax({
    type: 'GET', // Or any other HTTP Verb (Method)
    url: 'http://localhost:9333/commits',
    async: false, // async * false = !async = sync
    success: function(r){
      timeline_data = $.parseJSON(r);        
    },
    error: function(e){
      alert('failed');
    }
});

  eventSource1.loadJSON(timeline_data, url); // The data was stored into the 
  // timeline_data variable.
  
  var oldFillInfoBubble = Timeline.DefaultEventSource.Event.prototype.fillInfoBubble;
  Timeline.DefaultEventSource.Event.prototype.fillInfoBubble = function(elmt, theme, labeller) {
    oldFillInfoBubble.call(this, elmt, theme, labeller);
    var eventObject = this;
    var div = document.createElement("div");
    div.innerHTML = "<a href=\"javascript:alert('click')\">"+eventObject._text+"</a>";
    elmt.appendChild(div);
  }
  
  
  tl.layout(); // display the Timeline
}

var resizeTimerID = null;
function onResize() {
  if (resizeTimerID == null) {
    resizeTimerID = window.setTimeout(function() {
      resizeTimerID = null;
      tl.layout();
    }, 500);
  }
}
