package controllers;

import static org.junit.Assert.assertEquals;
import static play.test.Helpers.contentAsString;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.fakeRequest;
import static play.test.Helpers.route;
import static play.test.Helpers.running;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import play.Logger;
import play.mvc.Result;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by fo on 16.11.15.
 */
public class ApplicationTest {

  @Test
  public void testGetVocab() {
    running(fakeApplication(), new Runnable() {
      @Override
      public void run() {
        Result result = route(fakeRequest(routes.Application.getVocab("1.0"))
            .header("Accept", "text/turtle"));
        assertEquals(303, result.status());
        assertEquals("http://null/data/1.0/", result.redirectLocation());
      }
    });
  }

  @Test
  public void testGetStatement() {

    running(fakeApplication(), new Runnable() {
      @Override
      public void run() {
        Result result = route(fakeRequest(routes.Application.getStatement("InC", "1.0"))
            .header("Accept", "text/turtle"));
        assertEquals(303, result.status());
        assertEquals("http://null/data/InC/1.0/", result.redirectLocation());
      }
    });

  }

  @Test
  public void testGetVocabDataAsTurtle() {
    running(fakeApplication(), new Runnable() {
      @Override
      public void run() {
        Result result = route(fakeRequest(routes.Application.getVocabData("1.0", null))
            .header("Accept", "text/turtle"));
        assertEquals(200, result.status());
        assertEquals("text/turtle", result.contentType());
        assertEquals("http://null/data/1.0.ttl", result.header("Content-Location"));
        assertEquals("<http://null/data/1.0/>; rel=derivedfrom", result.header("Link"));
        assertEquals(getResource("data/1.0.ttl"), contentAsString(result));
      }
    });
  }

  @Test
  public void testGetStatementDataAsTurtle() {

    running(fakeApplication(), new Runnable() {
      @Override
      public void run() {
        Result result = route(fakeRequest(routes.Application.getStatementData("InC", "1.0", null))
            .header("Accept", "text/turtle"));
        assertEquals(200, result.status());
        assertEquals("text/turtle", result.contentType());
        assertEquals("http://null/data/InC/1.0.ttl", result.header("Content-Location"));
        assertEquals("<http://null/data/InC/1.0/>; rel=derivedfrom", result.header("Link"));
        assertEquals(getResource("data/InC/1.0.ttl"), contentAsString(result));
      }
    });

  }

  @Test
  public void testGetVocabDataAsJson() {
    running(fakeApplication(), new Runnable() {
      @Override
      public void run() {
        Result result = route(fakeRequest(routes.Application.getVocabData("1.0", null))
            .header("Accept", "application/ld+json"));
        assertEquals(200, result.status());
        assertEquals("application/ld+json", result.contentType());
        assertEquals("http://null/data/1.0.jsonld", result.header("Content-Location"));
        assertEquals("<http://null/data/1.0/>; rel=derivedfrom", result.header("Link"));
        assertEquals(getResource("data/1.0.jsonld"), contentAsString(result));
      }
    });
  }

  @Test
  public void testGetStatementDataAsJson() {

    running(fakeApplication(), new Runnable() {
      @Override
      public void run() {
        Result result = route(fakeRequest(routes.Application.getStatementData("InC", "1.0", null))
            .header("Accept", "application/ld+json"));
        assertEquals(200, result.status());
        assertEquals("application/ld+json", result.contentType());
        assertEquals("http://null/data/InC/1.0.jsonld", result.header("Content-Location"));
        assertEquals("<http://null/data/InC/1.0/>; rel=derivedfrom", result.header("Link"));
        assertEquals(getResource("data/InC/1.0.jsonld"), contentAsString(result));
      }
    });

  }

  private String getResource(String file) {

    InputStream in = ClassLoader.getSystemResourceAsStream(file);
    try {
      return IOUtils.toString(in, "UTF-8");
    } catch (IOException e) {
      Logger.error(e.toString());
      return null;
    }

  }

}
