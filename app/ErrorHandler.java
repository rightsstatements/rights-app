import com.google.inject.Inject;
import play.Configuration;
import play.Environment;
import play.Logger;
import play.api.OptionalSourceMapper;
import play.http.DefaultHttpErrorHandler;
import play.libs.F;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import play.api.routing.Router;

import services.LayoutProvider;

import javax.inject.Provider;
import java.io.IOException;

/**
 * Created by fo on 29.04.16.
 */
public class ErrorHandler extends DefaultHttpErrorHandler {

  @Inject
  private LayoutProvider layoutProvider;

  @Inject
  public ErrorHandler(Configuration configuration, Environment environment,
                      OptionalSourceMapper sourceMapper, Provider<Router> routes) {
    super(configuration, environment, sourceMapper, routes);
  }

  @Override
  public F.Promise<Result> onNotFound(Http.RequestHeader request, java.lang.String message) {

    Result result;
    try {
      result = Results.notFound(layoutProvider.getTemplateLoader().sourceAt("en/404.html").content()).as("text/html");
    } catch (IOException e) {
      Logger.error(e.toString());
      result = Results.notFound("Not Found");
    }

    return F.Promise.pure(result);

  }

}
