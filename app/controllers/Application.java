package controllers;

import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import play.Logger;
import play.Play;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by fo on 16.11.15.
 */
public class Application extends Controller {

  private static Map<String, String> mimeTypeExtensionMap = new HashMap<>();
  static {
    mimeTypeExtensionMap.put("text/turtle", "ttl");
    mimeTypeExtensionMap.put("application/ld+json", "jsonld");
    mimeTypeExtensionMap.put("application/json", "jsonld");
  }

  private static Model rightsStatements = ModelFactory.createDefaultModel().read(Play.application().classloader()
      .getResourceAsStream(Play.application().configuration().getString("rs.source.ttl")), null, "TURTLE");

  public static Result getVocab(String version) {

    if (request().accepts("text/html")) {
      return redirect(routes.Application.getVocabPage(version).absoluteURL(request()));
    } else {
      return redirect(routes.Application.getVocabData(version, null).absoluteURL(request()));
    }

  }

  public static Result getVocabData(String version, String ext) {

    MimeType mimeType = getMimeType(request(), ext);
    response().setHeader("Content-Location", routes.Application.getVocabData(version,
        mimeTypeExtensionMap.get(mimeType.toString())).absoluteURL(request()));
    response().setHeader("Link", "<".concat(routes.Application.getVocabData(version, null)
        .absoluteURL(request())).concat(">; rel=derivedfrom"));

    return getData(rightsStatements, mimeType);

  }

  public static Result getVocabPage(String version) {
    return ok();
  }

  public static Result getStatement(String id, String version) {

    if (request().accepts("text/html")) {
      return redirect(routes.Application.getStatementPage(id, version).absoluteURL(request()));
    } else {
      return redirect(routes.Application.getStatementData(id, version, null).absoluteURL(request()));
    }

  }

  public static Result getStatementData(String id, String version, String ext) throws IOException {

    String statementURI = "http://rightsstatements.org/vocab/%s/%s/";
    String constructStatement = "CONSTRUCT {<%1$s> ?p ?o} WHERE {<%1$s> ?p ?o}";
    Model rightsStatement = ModelFactory.createDefaultModel();
    QueryExecutionFactory.create(QueryFactory.create(String.format(constructStatement,
        String.format(statementURI, id, version))), rightsStatements).execConstruct(rightsStatement);

    if (rightsStatement.isEmpty()) {
      return notFound();
    }

    MimeType mimeType = getMimeType(request(), ext);
    response().setHeader("Content-Location", routes.Application.getStatementData(id, version,
        mimeTypeExtensionMap.get(mimeType.toString())).absoluteURL(request()));
    response().setHeader("Link", "<".concat(routes.Application.getStatementData(id, version, null)
        .absoluteURL(request())).concat(">; rel=derivedfrom"));

    return getData(rightsStatement, mimeType);

  }

  public static Result getStatementPage(String id, String version) {
    return ok();
  }

  private static Result getData(Model model, MimeType mimeType) {

    OutputStream result = new ByteArrayOutputStream();

    switch (mimeType.toString()) {
      case "text/turtle":
        model.write(result, "TURTLE");
        break;
      case "application/ld+json":
      case "application/json":
        model.write(result, "JSON-LD");
        break;
      default:
        return status(406);
    }

    return ok(result.toString()).as(mimeType.toString());

  }

  private static MimeType getMimeType(Http.Request request, String ext) {

    MimeType mimeType;

    try {
      if (ext != null) {
        switch (ext) {
          case "ttl":
            mimeType = new MimeType("text/turtle");
            break;
          case "jsonld":
            mimeType = new MimeType("application/ld+json");
            break;
          default:
            mimeType = new MimeType("text/html");
            break;
        }
      } else {
        mimeType = new MimeType(request.acceptedTypes().get(0).toString());
      }
    } catch (MimeTypeParseException e) {
      Logger.error(e.toString());
      mimeType = new MimeType();
    }

    return mimeType;

  }

}
