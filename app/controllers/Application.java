package controllers;

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
    return redirect(routes.Application.getVocabData(version, null).absoluteURL(request()));
  }

  public static Result getVocabData(String version, String ext) {

    MimeType mimeType = getMimeType(request(), ext);
    response().setHeader("Content-Location", routes.Application.getVocabData(version,
        mimeTypeExtensionMap.get(mimeType.toString())).absoluteURL(request()));
    response().setHeader("Link", "<".concat(routes.Application.getVocabData(version, null)
        .absoluteURL(request())).concat(">; rel=derivedfrom"));

    return getData(rightsStatements, mimeType);

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
