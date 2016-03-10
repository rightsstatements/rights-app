package services;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import play.Logger;
import play.Play;

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

  public DirectoryVocabProvider() {

    String sourceDir = Play.application().configuration().getString("source.dir");
    Path sourcePath = Paths.get(ClassLoader.getSystemResource(sourceDir).getPath());

    try (DirectoryStream<Path> files = Files.newDirectoryStream(sourcePath, "*.ttl")) {
      for (Path file : files) {
        vocab.read(Files.newInputStream(file), null, Lang.TURTLE.getName());
      }
    } catch (IOException e) {
      Logger.error(e.toString());
    }

  }

  @Override
  public Model getVocab() {
    return vocab;
  }

}
