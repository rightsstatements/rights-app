package services;

import com.github.jknack.handlebars.io.URLTemplateLoader;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import play.Configuration;
import play.Logger;

import java.io.IOException;
import java.net.URL;

@Singleton
public class HttpLayoutProvider implements LayoutProvider {

  private String httpSource;

  public URLTemplateLoader getTemplateLoader() {
    URLTemplateLoader urlTemplateLoader = new HttpLayoutProvider.ResourceTemplateLoader();
    urlTemplateLoader.setPrefix("/");
    urlTemplateLoader.setSuffix("");
    return urlTemplateLoader;
  }

  public class ResourceTemplateLoader extends URLTemplateLoader {

    protected URL getResource(final String location) throws IOException {

      Logger.debug("Fetching " + httpSource.concat(location));
      // TODO: cache templates, see e.g. https://stackoverflow.com/a/45439170
      return new URL(httpSource.concat(location));

    }

  }

  @Inject
  public HttpLayoutProvider(Configuration configuration) {

    httpSource = configuration.getString("source.site.http");

  }

}
