
var tl;
function onLoad() {
  var tl_el = document.getElementById("tl");
  var eventSource1 = new Timeline.DefaultEventSource();
  
  var theme1 = Timeline.ClassicTheme.create();
  theme1.autoWidth = true; // Set the Timeline's "width" automatically.
  // Set autoWidth on the Timeline's first band's theme,
  // will affect all bands.
  theme1.timeline_start = new Date(Date.UTC(2010, 0, 1));
  theme1.timeline_stop  = new Date(Date.UTC(2013, 0, 1));
  
  var timeline_data = {}
 
  $.ajax({
    type: 'GET', // Or any other HTTP Verb (Method)
    url: 'http://localhost:9333/commits',
    async: false, // async * false = !async = sync
    success: function(r){
      timeline_data = r;        
    },
    error: function(e){
      alert('failed');
    }});


  var d = new Date(); 
  var today =  timeline_data.events[0].start;
  if ('date' in timeline_data) d = timeline_data.date;

  var bandInfos = [
    Timeline.createBandInfo({
      width:          45, 
      intervalUnit:   Timeline.DateTime.HOUR, 
      intervalPixels: 60,
      eventSource:    eventSource1,
      date:           d,
      theme:          theme1,
      layout:         'original'  // original, overview, detailed
    }),
    Timeline.createBandInfo({
      width:          45, // set to a minimum, autoWidth will then adjust
      intervalUnit:   Timeline.DateTime.DAY, 
      intervalPixels: 250,
      eventSource:    eventSource1,
      date:           d,
      theme:          theme1,
      layout:         'overview'  // original, overview, detailed
    }),
    Timeline.createBandInfo({
      width:          45, // set to a minimum, autoWidth will then adjust
      intervalUnit:   Timeline.DateTime.MONTH, 
      intervalPixels: 300,
      eventSource:    eventSource1,
      date:           d,
      theme:          theme1,
      layout:         'overview'  // original, overview, detailed
    }),
  Timeline.createBandInfo({
      width:          45, // set to a minimum, autoWidth will then adjust
      intervalUnit:   Timeline.DateTime.YEAR, 
      intervalPixels: 400,
      eventSource:    eventSource1,
      date:           d,
      theme:          theme1,
      layout:         'overview'  // original, overview, detailed
    })
  ];

  bandInfos[1].syncWith = 0;
  bandInfos[2].syncWith = 1;
  bandInfos[3].syncWith = 2;


  bandInfos[1].highlight = true; 
  bandInfos[2].highlight = true;
  bandInfos[3].highlight = true;
 
 
  // create the Timeline
  tl = Timeline.create(tl_el, bandInfos, Timeline.HORIZONTAL);
  
  var url = '.'; // The base url for image, icon and background image
  // references in the data

 

  eventSource1.loadJSON(timeline_data, url); // The data was stored into the 
  // timeline_data variable.
  
  var oldFillInfoBubble = Timeline.DefaultEventSource.Event.prototype.fillInfoBubble;
  Timeline.DefaultEventSource.Event.prototype.fillInfoBubble = function(elmt, theme, labeller) {
//    oldFillInfoBubble.call(this, elmt, theme, labeller);
    var eventObject = this;
    var d_date = document.createElement("div")
    var d_author = document.createElement("div");    
    var d_sha1 = document.createElement("div");
    var d_sha2 = document.createElement("div");
    var d_message = document.createElement("div");
    var sha = eventObject._obj.sha;
    d_date.innerHTML = "<p>"+eventObject._start +"</p>";
    d_author.innerHTML = "<p>"+eventObject._obj.author+"</p>";
    d_sha1.innerHTML = "<a href=\"/?sha=" +sha+ "&date="+eventObject._start+"\">"+sha+"</a>";
    d_sha2.innerHTML = "<a href=\"/?sha=" +sha+ "&date="+eventObject._start+"\">"+sha+"</a>";

    var msg = eventObject._description.replace(/\n/g, '<br />');

    d_message.innerHTML = "<p>" +msg+ "</p>";

    elmt.appendChild(d_sha1)
    elmt.appendChild(d_date);
    elmt.appendChild(d_author);
    elmt.appendChild(d_message);
    elmt.appendChild(d_sha2)
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
