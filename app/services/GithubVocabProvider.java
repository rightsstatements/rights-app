package services;

import com.google.inject.Singleton;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import play.Logger;
import play.Play;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Created by fo on 25.11.15.
 */

@Singleton
public class GithubVocabProvider implements VocabProvider {

  @Override
  public Model getVocab() {

    Model vocab = ModelFactory.createDefaultModel();

    try {
      String path = Play.application().configuration().getString("rs.source.ttl");
      URL pathURL = Play.application().classloader().getResource(path);
      if ((pathURL != null) && pathURL.getProtocol().equals("file")) {
        for (String file : new File(pathURL.toURI()).list()) {
          if (file.endsWith(".ttl")) {
            vocab.read(Play.application().classloader().getResourceAsStream(path.concat(file)), null, "TURTLE");
          }
        }
      }
    } catch (URISyntaxException e) {
      Logger.error(e.toString());
    }

    return vocab;

  }

}
