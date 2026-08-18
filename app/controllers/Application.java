package controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.io.TemplateLoader;
import com.google.inject.Inject;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import play.Logger.ALogger;
import play.api.Configuration;
import play.Logger;
import play.api.Environment;
import play.api.http.MediaRange;
import play.i18n.Lang;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Http.Request;
import play.mvc.Result;
import services.LayoutProvider;
import services.VocabProvider;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ArrayList;

/**
 * Created by fo on 16.11.15.
 */
public class Application extends Controller {

  public static final String JSON_LD = "JSON-LD";
  public static final String ERROR_PAGE_UNSUPPORTED_PARAM = "/en/unsupportedParam.html";
  public static final String ERROR_PAGE_UNSUPPORTED_DATE = "/en/unsupportedDate.html";
  public static final String ERROR_PAGE_UNSUPPORTED_RELATED_URL = "/en/unsupportedRelatedURL.html";
  public static final String ERROR_PAGE_UNSUPPORTED_LANGUAGE = "/en/unsupportedLanguage.html";
  public static final String MIME_TYPE_TEXT_HTML = "text/html";
  public static final String REL_DERIVEDFROM = ">; rel=derivedfrom";
  private static final Map<String, Object> mimeTypeParserMap = generateParserMap();
  private final ALogger logger =Logger.of(this.getClass());

  private static Map<String, Object> generateParserMap() {
    Map<String, Object> mimeTypeParserMap =  new HashMap<>();
    mimeTypeParserMap.put("text/turtle","TURTLE");
    mimeTypeParserMap.put("application/json",JSON_LD);
    mimeTypeParserMap.put("application/ld+json",JSON_LD);
    mimeTypeParserMap.put("*/*", JSON_LD);
    return mimeTypeParserMap;
  }

  private static final  Map<String, Object> mimeTypeExtMap =  generateExtentionsMap();

  private static Map<String, Object> generateExtentionsMap() {
    Map<String, Object> extensionsMap =  new HashMap<>();
    extensionsMap.put("text/turtle" ,"ttl");
    extensionsMap.put("application/json" ,"json");
    extensionsMap.put("application/ld+json" ,"jsonld");
    extensionsMap.put("*/*" ,"json");
    return extensionsMap;
  }

  private static final Map<String, Object> defaults =generateValueMap( ConfigFactory.load().getConfig("default"));

  private static final Map<String, Object> validParameters = generateValueMap(ConfigFactory.load().getConfig("params"));

  private static final Map<String, Object> sparqlQueries = generateValueMap(ConfigFactory.load().getConfig("queries"));

  private static final List<String> languages = generateAvailableLanguageList(ConfigFactory.load().getString("languages.available"));

  protected static final Locale[] availableLocals = languages.stream().map(Locale::forLanguageTag).toArray(Locale[]::new);

  private static final List<Pattern> blackListedUrlsPatterns = getUrlPatterns(ConfigFactory.load().getStringList("blacklist.relatedURL"));

  private final VocabProvider vocabProvider;

  private final LayoutProvider layoutProvider;

  private final Configuration configuration;

  private final Environment env;

  @Inject
  public Application(VocabProvider vocabProvider, LayoutProvider layoutProvider, Configuration configuration,Environment env) {
    this.vocabProvider = vocabProvider;
    this.layoutProvider = layoutProvider;
    this.configuration = configuration;
    this.env = env;
  }

  public Result redirectToGetCollectionData(String id, String version) {
    String targetUrl = controllers.routes.Application.getCollectionData(id, version, null).url();
    // Return a permanent redirect (301) appending the slash
    return movedPermanently(targetUrl);
  }


  public Result getVocab(String version, Http.Request request) {
    logger.info("Getting vocab for version: {}", version);
    if (request.accepts(MIME_TYPE_TEXT_HTML)) {
      Locale locale = getLocale(request, null);
      logger.info("Vocab language : {}",locale.getLanguage());
      return redirect(routes.Application.getVocabPage(version, locale.getLanguage()).url());
    } else {
      logger.info(routes.Application.getVocabData(version, null).url());
      return redirect(routes.Application.getVocabData(version, null).url());
    }
  }
  public Result redirectToGetVocab(String version) {
    String targetUrl = controllers.routes.Application.getVocab(version).url();
    // Return a permanent redirect (301) appending the slash
    return movedPermanently(targetUrl);
  }

  public Result getVocabData(String version, String extension, Http.Request request) {
    Model vocab = getVocabModel(version);
    if (vocab.isEmpty()) {
      return notFoundPage(request);
    }
    String mimeType =getMimeType(request, extension);
    String mime = routes.Application.getVocabData(version,mimeTypeExtMap.getOrDefault(mimeType,
        defaults.get("mime").toString()).toString()).url();
    String link = "<".concat(routes.Application.getVocabData(version, null)
        .url()).concat(REL_DERIVEDFROM);
    return getData(vocab, mimeType).withHeaders(HttpHeaders.CONTENT_LOCATION, mime,"Link",    link);

  }

  public Result redirectToGetVocabData(String version) {
    String targetUrl = controllers.routes.Application.getVocabData(version, null).url();
    // Return a permanent redirect (301) appending the slash
    return movedPermanently(targetUrl);
  }

  public Result getVocabPage(String version, String language,Http.Request request) throws IOException {

    String validationResult = validateLanguageParam(language);
    if(validationResult != null) {
      return validationErrorPage(request,validationResult);
    }

    Model vocab = getVocabModel(version);
    Locale locale = getLocale(request, language);
    if (vocab.isEmpty()) {
      return notFoundPage(request);
    }
    String linkValue = "<".concat(routes.Application.getVocabPage(version, null).url()).concat(
        REL_DERIVEDFROM);
    String page = getPage(vocab, "/".concat(locale.toLanguageTag()).concat("/statements/vocab.html"), locale.getLanguage(), null,request);
    return status(OK, page).withHeaders(HttpHeaders.CONTENT_LANGUAGE, locale.getLanguage(),"Link",linkValue).as(MIME_TYPE_TEXT_HTML);
  }

  public Result redirectToVocabPage(String version, String language) {
    String targetUrl = controllers.routes.Application.getVocabPage(version, language).url();
    // Return a permanent redirect (301) appending the slash
    return movedPermanently(targetUrl);
  }

  public Result getStatement(String id, String version,Http.Request req) {
    if (!req.queryString().isEmpty()) {
      return notAcceptablePage(req).withHeaders("Alternates", setAlternates(req, id, version, true));
    } else  if (req.accepts(MIME_TYPE_TEXT_HTML)) {
      Locale locale = getLocale(req, null);
      return redirect(routes.Application.getStatementPage(id, version, locale.getLanguage()).url());
    } else {
      return redirect(routes.Application.getStatementData(id, version, null).url());
    }
  }

  public Result redirectToGetStatement(String id, String version) {
    String targetUrl = controllers.routes.Application.getStatement(id, version).url();
    // Return a permanent redirect (301) appending the slash
    return movedPermanently(targetUrl);
  }

  public Result getStatementData(String id, String version, String extension, Http.Request req) {
    if (!req.queryString().isEmpty()) {
      return notAcceptablePage(req).withHeaders("Alternates",setAlternates(req, id, version, false));
    }
    Model rightsStatement = getStatementModel(id, version);
    if (rightsStatement.isEmpty()) {
      return notFoundPage(req);
    }
    String  mimeType = getMimeType(req, extension);
    String location = routes.Application.getStatementData(id, version,
            mimeTypeExtMap.getOrDefault(mimeType, defaults.get("mime").toString())
                .toString()).url();
    String link = "<".concat(routes.Application.getStatementData(id, version, null)
        .url()).concat(REL_DERIVEDFROM);
    return getData(rightsStatement, mimeType).withHeaders("Content-Location", location,"Link", link);
  }

  public Result getStatementPage(String id, String version, String language,Http.Request req) throws IOException {

    HashMap<String, String> parameters = getValidParameterValueMap(req, id);
    String validationResult = validateParameters(parameters,req,language);
    if(validationResult != null) {
      return validationErrorPage(req,validationResult);
    }

    Model rightsStatement = getStatementModel(id, version);
    Locale locale = getLocale(req, language);

    if (rightsStatement.isEmpty()) {
      return notFoundPage(req);
    }
     String page = getPage(rightsStatement, "/en/statement.hbs", locale.getLanguage(),
        parameters, req);

    return status(OK,page).withHeaders("Content-Language", locale.getLanguage(),"Link", "<".concat(routes.Application.getStatementPage(id, version, null)
        .url()).concat(REL_DERIVEDFROM)).as(MIME_TYPE_TEXT_HTML);
  }

  public Result redirectToStatementPage(String id, String version, String language) {
    String targetUrl = controllers.routes.Application.getStatementPage(id, version, language).url();
    // Return a permanent redirect (301) appending the slash
    return movedPermanently(targetUrl);
  }

  private String validateParameters(HashMap<String, String> parameters, Request req,String language) {
    for ( String  e :  req.queryString().keySet()) {
      String value = req.getQueryString(e);
      //language parameter is later validated based on available languages
      if (!"language".equals(e) && !parameters.containsKey(e)) {
        return ERROR_PAGE_UNSUPPORTED_PARAM;
      }
      if ("relatedURL".equals(e) && !isValidRelatedURL(value)) {
        return ERROR_PAGE_UNSUPPORTED_RELATED_URL;
      }
      if ("date".equals(e) && !isValidDate(value)) {
        return ERROR_PAGE_UNSUPPORTED_DATE;
      }
    }
    return validateLanguageParam(language);
  }

  private String validateLanguageParam(String value) {
    //validate language only if parameter is provide
    if(!StringUtils.isBlank(value) && !languages.contains(value)) {
      return ERROR_PAGE_UNSUPPORTED_LANGUAGE;
    }
    return null;
  }

  public Result getCollection(String id, String version,Http.Request req) {
    if (req.accepts(MIME_TYPE_TEXT_HTML)) {
      Locale locale = getLocale(req, null);
      return redirect(routes.Application.getCollectionPage(id, version, locale.getLanguage()).url());
    } else {
      return redirect(routes.Application.getCollectionData(id, version, null).url());
    }
  }

  public  Result redirectToGetCollection(String id, String version) {
    String targetUrl = controllers.routes.Application.getCollection(id, version).url();
    // Return a permanent redirect (301) appending the slash
    return movedPermanently(targetUrl);
  }

  public Result getCollectionData(String id, String version, String extension,Http.Request req) {
    Model collection = getCollectionModel(id, version);
    if (collection.isEmpty()) {
      return notFoundPage(req);
    }
    String mimeType = getMimeType(req, extension);
    String mime = routes.Application.getCollectionData(id, version,
            mimeTypeExtMap.getOrDefault(mimeType, defaults.get("mime").toString()).toString()).url();
    String link = "<".concat(routes.Application.getCollectionData(id, version, null)
        .url()).concat(REL_DERIVEDFROM);
    return getData(collection, mimeType).withHeaders("Content-Location", mime,"Link", link);
  }

  public Result redirectToGetStatementData(String id, String version) {
    String targetUrl = controllers.routes.Application.getCollectionPage(id, version, null).url();
    // Return a permanent redirect (301) appending the slash
    return movedPermanently(targetUrl);
  }

  public Result getCollectionPage(String id, String version, String language,Http.Request req) throws IOException {
    String validationResult = validateLanguageParam(language);
    if(validationResult != null) {
      return validationErrorPage(req,validationResult);
    }

    Model collection = getVocabModel(version);
    Locale locale = getLocale(req, language);

    if (collection.isEmpty()) {
      return notFoundPage(req);
    }
    String concat = "<".concat(routes.Application.getCollectionPage(id, version, null)
        .url()).concat(REL_DERIVEDFROM);

    String page = getPage(collection,
        locale.toLanguageTag().concat("/statements/collection-").concat(id).concat(".html"),
        locale.getLanguage(), null, req);

    return status(OK,page).withHeaders("Link", concat, HttpHeaders.CONTENT_LANGUAGE, locale.getLanguage()).as(MIME_TYPE_TEXT_HTML);
  }

  public Result redirectToCollectionPage(String id, String version, String language) {
    String targetUrl = controllers.routes.Application.getCollectionPage(id, version, language).url();
    // Return a permanent redirect (301) appending the slash
    return movedPermanently(targetUrl);
  }

  private Result validationErrorPage(Request req,String pageLocation) throws IOException {
    TemplateLoader loader = layoutProvider.getTemplateLoader();
    loader.setPrefix(getDeployUrl(req));
    return badRequest(loader.sourceAt(pageLocation).content()).as(MIME_TYPE_TEXT_HTML);
  }

  private Result notFoundPage(Request request) {
    TemplateLoader loader = layoutProvider.getTemplateLoader();
    loader.setPrefix(getDeployUrl(request));
    try {
      return notFound(loader.sourceAt("/en/404.html").content()).as(MIME_TYPE_TEXT_HTML);
    } catch (IOException e) {
      Logger.error(e.toString());
      return notFound("Not Found");
    }
  }

  private Result notAcceptablePage(Request req) {
    TemplateLoader loader = layoutProvider.getTemplateLoader();
    loader.setPrefix(getDeployUrl(req));
    try {
      Logger.error("Request not acceptable : {} {} ",req.method(), req.uri());
      return status(406, loader.sourceAt("/en/406.html").content()).as(MIME_TYPE_TEXT_HTML);
    } catch (IOException e) {
      Logger.error(e.toString());
      return status(406, "Not Acceptable");
    }
  }

  private Result getData(Model model, String mimeType) {
    OutputStream result = new ByteArrayOutputStream();
    model.write(result, mimeTypeParserMap.getOrDefault(mimeType, defaults.get("parser"))
        .toString());
    String contentTypeValue = mimeType.equals("*/*") ? defaults.get("mime").toString() : mimeType;
    if(StringUtils.isNotEmpty(contentTypeValue) )
      return ok(result.toString()).as(contentTypeValue + ";charset=utf-8");
    else
       return ok(result.toString());
  }

  private String getPage(Model model, String templateFile, String language, HashMap<String, String> parameters,
      Request req)
      throws IOException {

    Model localized = ModelFactory.createDefaultModel();
    try(QueryExecution queryExecution = QueryExecutionFactory.create(
        QueryFactory.create(String.format(sparqlQueries.get("localize").toString(), language)),
        model)) {
      queryExecution.execConstruct(localized);
    }

    Map<String, Object> scope = new HashMap<>();
    scope.put("parameters", parameters);
    scope.put("language", language);

    OutputStream boas = new ByteArrayOutputStream();
    localized.write(boas, JSON_LD);

    String output = boas.toString();
    scope.put("data", new ObjectMapper().readValue(output, HashMap.class));
    TemplateLoader loader = layoutProvider.getTemplateLoader();
    loader.setPrefix(getDeployUrl(req));
    Handlebars handlebars = new Handlebars(loader);

    try {
      handlebars.registerHelpers("helpers.js",env.classLoader()
          .getResourceAsStream("public/js/helpers.js"));
    } catch (Exception e) {
      Logger.error(e.toString());
    }
    return handlebars.compile(templateFile).apply(scope);

  }

  private Model getVocabModel(String version) {
    Model vocab = ModelFactory.createDefaultModel();
    try(QueryExecution queryExecution = QueryExecutionFactory.create(
        QueryFactory.create(String.format(sparqlQueries.get("vocab").toString(), version)),
        vocabProvider.getVocab())) {
      queryExecution.execConstruct(vocab);
    }
    return vocab;
  }

  private Model getStatementModel(String id, String version) {
    Model statement = ModelFactory.createDefaultModel();
    try(QueryExecution queryExecution = QueryExecutionFactory.create(
        QueryFactory.create(String.format(sparqlQueries.get("statement").toString(), version,
            id)), vocabProvider.getVocab())) {
      queryExecution.execConstruct(statement);
    }
    return statement;
  }

  private Model getCollectionModel(String id, String version) {
    Model collection = ModelFactory.createDefaultModel();
    try(QueryExecution queryExecution = QueryExecutionFactory.create(
        QueryFactory.create(String.format(sparqlQueries.get("collection").toString(), id,
            version)), vocabProvider.getVocab())) {
      queryExecution.execConstruct(collection);
    }
    return collection;
  }

  private String getMimeType(Http.Request request, String extension) {
    if (extension != null) {
      return getMimeTypeByExtension(extension);
    } else {
      return getMimeTypeFromRequest(request);
    }
  }

  private static String getMimeTypeFromRequest(Http.Request request) {
    String mimeType = "*/*";
    List<MediaRange> acceptedTypes = request.acceptedTypes();
    if (!acceptedTypes.isEmpty()) {
      mimeType = request.acceptedTypes().get(0).toString();
    }
    return mimeType;
  }

  private static String getMimeTypeByExtension(String extension) {
    for (Map.Entry<String, Object> entry : mimeTypeExtMap.entrySet()) {
      if (entry.getValue().equals(extension)) {
          return  entry.getKey();
      }
    }
    return "*/*";
  }

  private Locale getLocale(Http.Request request, String language) {
    Locale[] requestedLocales = getRequestedLocales(request, language);
      for (Locale requestedLocale : requestedLocales) {
        if (Arrays.asList(availableLocals).contains(requestedLocale)) {
          return requestedLocale;
        }
      }
    return availableLocals[0];
  }

  private Locale[] getRequestedLocales(Request request, String language) {
    if (language != null) {
      return getLocalesByCode(language);
     }
     return getLocalesFromRequest(request);
  }

  private Locale[] getLocalesFromRequest(Http.Request request) {
    if (!request.acceptLanguages().isEmpty()) {
      return request.acceptLanguages().stream().map(Lang::toLocale).toArray(Locale[]::new);
    }
    return new Locale[0];
  }

  private Locale[] getLocalesByCode(String code) {
    return new Locale[]{Locale.forLanguageTag(code)};
  }

  private String setAlternates(Request request, String id, String version, boolean includeVocab) {

    List<String> alternates = new ArrayList<>();
    if (!request.queryString().isEmpty()) {
      List<String> recoveryParameters = new ArrayList<>();
      for (Map.Entry<String, String> parameter : getValidParameterValueMap(request, id).entrySet()) {
        recoveryParameters.add(parameter.getKey().concat("=").concat(parameter.getValue()));
      }
      if (!recoveryParameters.isEmpty()) {
        String vocabUrl = routes.Application.getStatement(id, version).url();
        String pageUrl = routes.Application.getStatementPage(id, version, null).url().concat("?")
            .concat(String.join("&", recoveryParameters));
        String dataUrl = routes.Application.getStatementData(id, version, null).url();
        if (includeVocab) {
          alternates.add(String.format("{\"%s\" 0.9}", vocabUrl));
        }
        alternates.add(String.format("{\"%s\" 0.9 {text/html}}", pageUrl));
        for ( Map.Entry<String, Object> entry : mimeTypeExtMap.entrySet()) {
          if (entry.getKey().equals("*/*")) {
            continue;
          }
          alternates.add(String.format("{\"%s\" 0.9 {".concat(entry.getKey()).concat("}}"), dataUrl));
        }
      }
    }
    return String.join(",", alternates);
  }

  private HashMap<String, String> getValidParameterValueMap(Http.Request request, String id) {
    List<String> validParams = getConfiguredParameterForId(id);
    HashMap<String, String> parameters = new HashMap<>();
    if (validParams != null) {
      for (String validParameter : validParams) {
        String suppliedParameter = request.getQueryString(validParameter);
        if (suppliedParameter != null) {
            parameters.put(validParameter, StringEscapeUtils.escapeHtml4(request.queryString(validParameter).orElse(null)));
        }
      }
    }
    return parameters;
  }

  private static List<String> getConfiguredParameterForId(String id) {
    return Application.validParameters.get(id) != null ? List.of(
        Application.validParameters.get(id).toString().split(" ")) : new ArrayList<>();
  }

  /**
   * parses a date without an offset, such as '2011-12-03'
   * @param value of the date
   * @return true if the date is valid
   */
  private boolean isValidDate(String value) {
    DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
    try{
      formatter.parse(value);
      return true;
    } catch (DateTimeParseException e){
      return false;
    }
  }

  private boolean isValidRelatedURL(String value) {
     return blackListedUrlsPatterns.stream().noneMatch(p -> p.matcher(value).matches());
  }

  private static List<Pattern> getUrlPatterns(List<String> blackListedURLPatterns) {
     List<Pattern> patternList = new ArrayList<>();
    for(String s : blackListedURLPatterns){
        Pattern urlPattern = Pattern.compile(s, Pattern.DOTALL);
        patternList.add(urlPattern);
    }
    return patternList;
  }

  private static List<String> generateAvailableLanguageList(String languages) {
    return languages!=null ? Arrays.stream(languages.split(" +")).toList() : Collections.emptyList();
  }

  private String getDeployUrl(Http.Request req) {
    if (configuration.underlying().getString("source.site.http") != null) {
      return configuration.underlying().getString("source.site.http");
    }
    return req.hasHeader("X-Deploy-Url")
      ? req.header("X-Deploy-Url").get()
      : "/";

  }

  private static Map<String, Object> generateValueMap(Config queries)  {
    Map<String, Object> result = new HashMap<>();
    for(String key : queries.root().keySet()){
      result.put(key,queries.getAnyRef(key));
    }
    return result;
  }

  public Result index() {
    return ok(views.html.index.render());
  }
}
