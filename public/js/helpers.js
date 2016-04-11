Handlebars.registerHelper('json', function (obj, options) {
  return new Handlebars.SafeString(JSON.stringify(obj, null, 2));
});

Handlebars.registerHelper('a', function (href, options) {
  return new Handlebars.SafeString("<a href=\"" + href + "\">" + href + "</a>");
});

Handlebars.registerHelper('id', function(id, graph, options) {

  for (var i = 0; i < graph.length; i++) {
    if (graph[i]['@id'].trim() == id.trim()) {
      return options.fn(graph[i]);
    }
  }

});
