package controllers;

import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import play.Play;
import play.mvc.Controller;
import play.mvc.Result;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by fo on 16.11.15.
 */
public class Application extends Controller {

  private static Model rightsStatements = ModelFactory.createDefaultModel().read(Play.application().classloader()
      .getResourceAsStream(Play.application().configuration().getString("rs.source.ttl")), null, "TURTLE");

  private static String statementURI = "http://rightsstatements.org/vocab/%s/%s/";

  private static String constructStatement = "CONSTRUCT {<%1$s> ?p ?o} WHERE {<%1$s> ?p ?o}";

  public static Result getStatement(String id, String version) {
    return redirect(routes.Application.getStatementData(id, version, null).absoluteURL(request()));
  }

  public static Result getStatementData(String id, String version, String ext) throws IOException {

    Model rightsStatement = ModelFactory.createDefaultModel();
    QueryExecutionFactory.create(QueryFactory.create(String.format(constructStatement,
        String.format(statementURI, id, version))), rightsStatements).execConstruct(rightsStatement);

    if (rightsStatement.isEmpty()) {
      return notFound();
    }

    OutputStream result = new ByteArrayOutputStream();
    if ("ttl".equals(ext) || request().accepts("text/turtle")) {
      rightsStatement.write(result, "TURTLE");
      response().setHeader("Content-Location", routes.Application.getStatementData(id, version, "ttl")
          .absoluteURL(request()));
      return ok(result.toString()).as("text/turtle");
    } else {
      return status(406);
    }

  }

  public static Result getVocab(String version) {
    return redirect(routes.Application.getVocabData(version, null).absoluteURL(request()));
  }

  public static Result getVocabData(String version, String ext) {

    OutputStream result = new ByteArrayOutputStream();
    if ("ttl".equals(ext) || request().accepts("text/turtle")) {
      rightsStatements.write(result, "TURTLE");
      response().setHeader("Content-Location", routes.Application.getVocabData(version, "ttl")
          .absoluteURL(request()));
      return ok(result.toString()).as("text/turtle");
    } else if ("jsonld".equals(ext) || request().accepts("application/ld+json")
        || request().accepts("application/json")) {
      rightsStatements.write(result, "JSON-LD");
      response().setHeader("Content-Location", routes.Application.getVocabData(version, "jsonld")
          .absoluteURL(request()));
      return ok(result.toString()).as("application/ld+json");
    } else {
      return status(406);
    }

  }

}
