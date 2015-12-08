Handlebars.registerHelper('json', function (obj, options) {
  return new Handlebars.SafeString(JSON.stringify(obj, null, 2));
});

Handlebars.registerHelper('a', function (href, options) {
  return new Handlebars.SafeString("<a href=\"" + href + "\">" + href + "</a>");
});
