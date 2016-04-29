package services;

import com.github.jknack.handlebars.io.URLTemplateLoader;
import com.google.inject.Singleton;
import com.google.inject.Inject;
import org.apache.commons.io.FileUtils;
import org.apache.jena.atlas.RuntimeIOException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import play.Configuration;
import play.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Created by fo on 19.04.16.
 */

@Singleton
public class GitLayoutProvider implements LayoutProvider {

  private Configuration gitSource;

  public URLTemplateLoader getTemplateLoader() {
    URLTemplateLoader urlTemplateLoader = new ResourceTemplateLoader();
    urlTemplateLoader.setPrefix("/");
    urlTemplateLoader.setSuffix("");
    return urlTemplateLoader;
  }

  public class ResourceTemplateLoader extends URLTemplateLoader {

    protected URL getResource(final String location) throws IOException {

      File localPath = new File(gitSource.getString("local"), location.substring(1));
      Logger.debug("Fetching " + localPath.toURI().toURL());
      return localPath.toURI().toURL();

    }

  }

  @Inject
  public GitLayoutProvider(Configuration configuration) {

    gitSource = configuration.getConfig("source.site.git");

    try {
      Logger.debug("Checking out template branch ".concat(gitSource.getString("branch")));
      File localPath = new File(gitSource.getString("local"));
      FileUtils.deleteDirectory(localPath);
      try (Git git = Git.cloneRepository().setURI(gitSource.getString("remote")).setDirectory(localPath).call()) {
        git.checkout().setName(gitSource.getString("branch")).call();
        git.getRepository().close();
      }
    } catch (IOException | GitAPIException e) {
      throw new RuntimeIOException(e);
    }

  }

}
