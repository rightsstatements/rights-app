package services;

import com.google.inject.Singleton;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter;
import play.Configuration;
import play.Logger;
import play.Play;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;


/**
 * Created by fo on 25.11.15.
 */

@Singleton
public class GitVocabProvider implements VocabProvider {

  private Model vocab = ModelFactory.createDefaultModel();

  public GitVocabProvider() {

    Configuration gitSource = Play.application().configuration().getConfig("source.git");

    try {

      File localPath = File.createTempFile(gitSource.getString("local"), "");
      localPath.delete();

      try (Git git = Git.cloneRepository().setURI(gitSource.getString("remote")).setDirectory(localPath).call()) {

        Repository repository = git.getRepository();
        ObjectId lastCommitId = repository.resolve(Constants.HEAD);

        try (RevWalk revWalk = new RevWalk(repository)) {

          RevCommit commit = revWalk.parseCommit(lastCommitId);
          RevTree tree = commit.getTree();

          try (TreeWalk treeWalk = new TreeWalk(repository)) {

            treeWalk.addTree(tree);
            treeWalk.setRecursive(true);
            treeWalk.setFilter(PathSuffixFilter.create(".ttl"));

            while (treeWalk.next()) {
              ObjectId objectId = treeWalk.getObjectId(0);
              ObjectLoader loader = repository.open(objectId);
              ByteArrayOutputStream baos = new ByteArrayOutputStream();
              loader.copyTo(baos);
              ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
              vocab.read(bais, null, "TURTLE");
            }

          }

          revWalk.dispose();

        }

        git.close();

      }

    } catch (IOException | GitAPIException e) {

      Logger.error(e.toString());

    }

  }

  @Override
  public Model getVocab() {

    return vocab;

  }

}
