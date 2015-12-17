package controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.TemplateLoader;
import com.google.inject.Inject;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import helpers.ResourceTemplateLoader;
import play.Logger;
import play.Play;
import play.api.http.MediaRange;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import services.VocabProvider;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.annotation.Nonnull;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ArrayList;

/**
 * Created by fo on 16.11.15.
 */
public class Application extends Controller {

  private static Map<String, Object> mimeTypeParserMap = Play.application().configuration().getConfig("parser").asMap();

  private static Map<String, Object> mimeTypeExtMap = Play.application().configuration().getConfig("extension").asMap();

  private static Map<String, Object> defaults = Play.application().configuration().getConfig("default").asMap();

  private static Map<String, Object> validParameters = Play.application().configuration().getConfig("params").asMap();

  private final VocabProvider vocabProvider;

  @Inject
  public Application(VocabProvider vocabProvider) {
    this.vocabProvider = vocabProvider;
  }

  public Result getVocab(String version) {

    if (request().accepts("text/html")) {
      Locale locale = getLocale(request(), null);
      return redirect(routes.Application.getVocabPage(version, locale.getLanguage()).absoluteURL(request()));
    } else {
      return redirect(routes.Application.getVocabData(version, null).absoluteURL(request()));
    }

  }

  public Result getVocabData(String version, String extension) {

    Model vocab = getVocabModel(version);

    if (vocab.isEmpty()) {
      return notFound();
    }

    MimeType mimeType = getMimeType(request(), extension);
    response().setHeader("Content-Location", routes.Application.getVocabData(version,
        mimeTypeExtMap.get(mimeType.toString()).toString()).absoluteURL(request()));
    response().setHeader("Link", "<".concat(routes.Application.getVocabData(version, null)
        .absoluteURL(request())).concat(">; rel=derivedfrom"));

    return getData(vocab, mimeType);

  }

  public Result getVocabPage(String version, String language) throws IOException {

    Model vocab = getVocabModel(version);
    Locale locale = getLocale(request(), language);

    if (vocab.isEmpty()) {
      return notFound();
    }

    response().setHeader("Link", "<".concat(routes.Application.getVocabPage(version, null)
        .absoluteURL(request())).concat(">; rel=derivedfrom"));
    response().setHeader("Content-Language", locale.getLanguage());

    return getPage(vocab, "vocab.hbs", locale.getLanguage());

  }

  public Result getStatement(String id, String version) {

    if (!request().queryString().isEmpty()) {
      setAlternates(request(), id, version);
      return status(406);
    } else  if (request().accepts("text/html")) {
      Locale locale = getLocale(request(), null);
      return redirect(routes.Application.getStatementPage(id, version, locale.getLanguage()).absoluteURL(request()));
    } else {
      return redirect(routes.Application.getStatementData(id, version, null).absoluteURL(request()));
    }

  }

  public Result getStatementData(String id, String version, String extension) throws IOException {

    if (!request().queryString().isEmpty()) {
      setAlternates(request(), id, version);
      return status(406);
    }

    Model rightsStatement = getStatementModel(id, version);

    if (rightsStatement.isEmpty()) {
      return notFound();
    }

    MimeType mimeType = getMimeType(request(), extension);
    response().setHeader("Content-Location", routes.Application.getStatementData(id, version,
        mimeTypeExtMap.get(mimeType.toString()).toString()).absoluteURL(request()));
    response().setHeader("Link", "<".concat(routes.Application.getStatementData(id, version, null)
        .absoluteURL(request())).concat(">; rel=derivedfrom"));

    return getData(rightsStatement, mimeType);

  }

  public Result getStatementPage(String id, String version, String language) throws IOException {

    Model rightsStatement = getStatementModel(id, version);
    Locale locale = getLocale(request(), language);

    if (rightsStatement.isEmpty()) {
      return notFound();
    }

    response().setHeader("Link", "<".concat(routes.Application.getStatementPage(id, version, null)
        .absoluteURL(request())).concat(">; rel=derivedfrom"));
    response().setHeader("Content-Language", locale.getLanguage());

    return getPage(rightsStatement, "statement.hbs", locale.getLanguage());

  }

  private Result getData(Model model, MimeType mimeType) {

    OutputStream result = new ByteArrayOutputStream();
    model.write(result, mimeTypeParserMap.get(mimeType.toString()).toString());
    return ok(result.toString()).as(
        mimeType.toString().equals("*/*") ? defaults.get("mime").toString() : mimeType.toString());

  }

  private Result getPage(Model model, String templateFile, String language) throws IOException {

    String constructLocalizedModel = "CONSTRUCT {?s ?p ?o}"
        .concat("WHERE {?s ?p ?o . FILTER(!isLiteral(?o) || lang(?o) = \"\" || langMatches(lang(?o), \"")
        .concat(language).concat("\"))}");
    Model localized = ModelFactory.createDefaultModel();
    QueryExecutionFactory.create(QueryFactory.create(constructLocalizedModel),
        model).execConstruct(localized);

    OutputStream boas = new ByteArrayOutputStream();
    localized.write(boas, "JSON-LD");
    Map<?,?> scope = new ObjectMapper().readValue(boas.toString(), HashMap.class);

    TemplateLoader loader = new ResourceTemplateLoader();
    loader.setPrefix("public/handlebars");
    loader.setSuffix("");
    Handlebars handlebars = new Handlebars(loader);

    try {
      handlebars.registerHelpers(new File("public/js/helpers.js"));
    } catch (Exception e) {
      Logger.error(e.toString());
    }

    Template pageTemplate = handlebars.compile(templateFile);
    Map<String, Object> main = new HashMap<>();
    main.put("content", pageTemplate.apply(scope));
    Template template = handlebars.compile("main.hbs");

    return ok(template.apply(main)).as("text/html");

  }

  private Model getVocabModel(String version) {

    String constructStatement = "CONSTRUCT WHERE {?s <http://www.w3.org/2002/07/owl#versionInfo> \"%1$s\" . ?s ?p ?o}";
    Model vocab = ModelFactory.createDefaultModel();
    QueryExecutionFactory.create(QueryFactory.create(String.format(constructStatement, version)),
        vocabProvider.getVocab()).execConstruct(vocab);

    return vocab;

  }

  private Model getStatementModel(String id, String version) {

    String constructStatement = "CONSTRUCT WHERE {?s <http://www.w3.org/2002/07/owl#versionInfo> \"%1$s\" ."
        .concat("?s <http://purl.org/dc/elements/1.1/identifier> \"%2$s\" . ?s ?p ?o}");
    Model statement = ModelFactory.createDefaultModel();
    QueryExecutionFactory.create(QueryFactory.create(String.format(constructStatement, version, id)),
        vocabProvider.getVocab()).execConstruct(statement);

    return statement;

  }

  private MimeType getMimeType(Http.Request request, String extension) {

    if (extension != null) {
      return getMimeTypeByExtension(extension);
    } else {
      return getMimeTypeFromRequest(request);
    }

  }

  private static MimeType getMimeTypeFromRequest(Http.Request request) {

    MimeType mimeType;
    List<MediaRange> acceptedTypes = request.acceptedTypes();

    try {
      if (! acceptedTypes.isEmpty()) {
        mimeType = new MimeType(request.acceptedTypes().get(0).toString());
      } else {
        mimeType = new MimeType("*/*");
      }
    } catch (MimeTypeParseException e) {
      Logger.error(e.toString());
      mimeType = new MimeType();
    }

    return mimeType;

  }

  private static MimeType getMimeTypeByExtension(@Nonnull String extension) {

    for (Map.Entry<String, Object> entry : mimeTypeExtMap.entrySet()) {
      if (entry.getValue().equals(extension)) {
        try {
          return new MimeType(entry.getKey());
        } catch (MimeTypeParseException e) {
          Logger.error(e.toString());
        }
      }
    }

    return new MimeType();

  }

  private Locale getLocale(Http.Request request, String language) {

    if (language != null) {
      return getLocaleByCode(language);
    } else {
      return getLocaleFromRequest(request);
    }

  }

  private Locale getLocaleFromRequest(Http.Request request) {

    String code;

    if (!request.acceptLanguages().isEmpty()) {
      code = request.acceptLanguages().get(0).language();
    } else {
      code = defaults.get("language").toString();
    }

    return new Locale(code);

  }

  private Locale getLocaleByCode(String code) {

    return new Locale(code);

  }

  private void setAlternates(Http.Request request, String id, String version) {

    Map<String, String[]> parameters = request.queryString();
    String validParameters = (String) Application.validParameters.get(id);

    if (parameters.size() > 0 && validParameters != null) {

      List<String> recoveryParameters = new ArrayList<>();

      for (String validParameter : validParameters.split(" ")) {
        String suppliedParameter = request.getQueryString(validParameter);
        if (suppliedParameter != null) {
          recoveryParameters.add(validParameter.concat("=").concat(suppliedParameter));
        }
      }

      if (!recoveryParameters.isEmpty()) {

        String vocabUrl = routes.Application.getStatement(id, version).url();
        String pageUrl = routes.Application.getStatementPage(id, version, null).url().concat("?")
            .concat(String.join("&", recoveryParameters));
        String dataUrl = routes.Application.getStatementData(id, version, null).url();

        List<String> alternates = new ArrayList<>();
        alternates.add(String.format("{\"%s\" 0.9}", vocabUrl));
        alternates.add(String.format("{\"%s\" 0.9 {text/html}}", pageUrl));
        for (Map.Entry<String, Object> entry : mimeTypeExtMap.entrySet()) {
          if (entry.getKey().equals("*/*")) {
            continue;
          }
          alternates.add(String.format("{\"%s\" 0.9 {".concat(entry.getKey()).concat("}}"), dataUrl));
        }

        response().setHeader("Alternates", String.join(",", alternates));

      }

    }

  }

}
