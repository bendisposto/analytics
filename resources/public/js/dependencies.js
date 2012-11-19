

function display_graph(module,sha) {
  var json = "null";
  $('svg').remove();
  $.ajax({
    type: 'GET', // Or any other HTTP Verb (Method)
    url: 'http://localhost:9333/dependencies?m='+module+"&sha="+sha,
    async: false, // async * false = !async = sync
    success: function(r){
      json = r;        
    },
    error: function(e){
      alert('failed');
    }});
  
  var g = new Graph(); 
  g.edgeFactory.template.style.directed = true; 
  g.addNode(json.name);
  var a = json.relying;
  if (a != null) {
    for (var i = 0; i < a.length; i++) {
      g.addEdge(json.name,a[i], { stroke: "#333", fill: "#333"});
    }}
  a = json.imported;
  if (a != null) {  
    for (var i = 0; i < a.length; i++) {
      g.addEdge(a[i],json.name);
    }}



  var layouter = new Graph.Layout.Spring(g); layouter.layout();
  var renderer = new Graph.Renderer.Raphael('canvas', g, 800, 600);
  renderer.draw();}

