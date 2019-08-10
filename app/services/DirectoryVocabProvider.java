package services;

import com.google.inject.Inject;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import play.Configuration;
import play.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by fo on 10.03.16.
 */
public class DirectoryVocabProvider implements VocabProvider {

  private Model vocab = ModelFactory.createDefaultModel();

  @Inject
  public DirectoryVocabProvider(Configuration configuration) {

    Configuration source = configuration.getConfig("source.data");
    String sourceDir = source.getString("dir");
    Configuration formats = source.getConfig("formats");
    Path sourcePath = new File(sourceDir).isAbsolute()
        ? Paths.get(new File(sourceDir).getPath())
        : Paths.get(ClassLoader.getSystemResource(sourceDir).getPath());

    for (String format : formats.asMap().keySet()) {
      String ext = formats.getConfig(format).getString("ext");
      String lang = formats.getConfig(format).getString("lang");
      try (DirectoryStream<Path> files = Files.newDirectoryStream(sourcePath, "*".concat(ext))) {
        for (Path file : files) {
          vocab.read(Files.newInputStream(file), null, lang);
        }
      } catch (IOException e) {
        Logger.error(e.toString());
      }
    }

  }

  @Override
  public Model getVocab() {
    return vocab;
  }

}
