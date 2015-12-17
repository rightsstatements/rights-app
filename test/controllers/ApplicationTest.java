package controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
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

        Result data = route(fakeRequest(routes.Application.getVocab("1.0"))
            .header("Accept", "text/turtle"));
        assertEquals(303, data.status());
        assertEquals("http://null/data/1.0/", data.redirectLocation());

        Result page = route(fakeRequest(routes.Application.getVocab("1.0"))
            .header("Accept", "text/html").header("Accept-Language", "en"));
        assertEquals(303, page.status());
        assertEquals("http://null/page/1.0/?language=en", page.redirectLocation());

      }
    });

  }

  @Test
  public void testGetStatement() {

    running(fakeApplication(), new Runnable() {
      @Override
      public void run() {

        Result data = route(fakeRequest(routes.Application.getStatement("InC", "1.0"))
            .header("Accept", "text/turtle"));
        assertEquals(303, data.status());
        assertEquals("http://null/data/InC/1.0/", data.redirectLocation());

        Result page = route(fakeRequest(routes.Application.getStatement("InC", "1.0"))
            .header("Accept", "text/html").header("Accept-Language", "en"));
        assertEquals(303, page.status());
        assertEquals("http://null/page/InC/1.0/?language=en", page.redirectLocation());

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
  public void testGetVocabDataWithoutAcceptHeader() {

    running(fakeApplication(), new Runnable() {
      @Override
      public void run() {
        Result result = route(fakeRequest(routes.Application.getVocabData("1.0", null)));
        assertEquals(200, result.status());
        assertEquals("application/json", result.contentType());
        assertEquals("http://null/data/1.0.json", result.header("Content-Location"));
        assertEquals("<http://null/data/1.0/>; rel=derivedfrom", result.header("Link"));
        assertEquals(getResource("data/1.0.jsonld"), contentAsString(result));
      }
    });

  }

  @Test
  public void testGetVocabDataWithWildcardAcceptHeader() {

    running(fakeApplication(), new Runnable() {
      @Override
      public void run() {
        Result result = route(fakeRequest(routes.Application.getVocabData("1.0", null))
            .header("Accept", "*/*"));
        assertEquals(200, result.status());
        assertEquals("application/json", result.contentType());
        assertEquals("http://null/data/1.0.json", result.header("Content-Location"));
        assertEquals("<http://null/data/1.0/>; rel=derivedfrom", result.header("Link"));
        assertEquals(getResource("data/1.0.jsonld"), contentAsString(result));
      }
    });

  }

  @Test
  public void testGetVocabDataAsJson() {

    running(fakeApplication(), new Runnable() {
      @Override
      public void run() {
        Result result = route(fakeRequest(routes.Application.getVocabData("1.0", null))
            .header("Accept", "application/json"));
        assertEquals(200, result.status());
        assertEquals("application/json", result.contentType());
        assertEquals("http://null/data/1.0.json", result.header("Content-Location"));
        assertEquals("<http://null/data/1.0/>; rel=derivedfrom", result.header("Link"));
        assertEquals(getResource("data/1.0.jsonld"), contentAsString(result));
      }
    });

  }

  @Test
  public void testGetVocabDataAsJsonLd() {

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
  public void testGetStatementDataWithoutAcceptHeader() {

    running(fakeApplication(), new Runnable() {
      @Override
      public void run() {
        Result result = route(fakeRequest(routes.Application.getStatementData("InC", "1.0", null)));
        assertEquals(200, result.status());
        assertEquals("application/json", result.contentType());
        assertEquals("http://null/data/InC/1.0.json", result.header("Content-Location"));
        assertEquals("<http://null/data/InC/1.0/>; rel=derivedfrom", result.header("Link"));
        assertEquals(getResource("data/InC/1.0.jsonld"), contentAsString(result));
      }
    });

  }

  @Test
  public void testGetStatementDataWithWildcardAcceptHeader() {

    running(fakeApplication(), new Runnable() {
      @Override
      public void run() {
        Result result = route(fakeRequest(routes.Application.getStatementData("InC", "1.0", null))
            .header("Accept", "*/*"));
        assertEquals(200, result.status());
        assertEquals("application/json", result.contentType());
        assertEquals("http://null/data/InC/1.0.json", result.header("Content-Location"));
        assertEquals("<http://null/data/InC/1.0/>; rel=derivedfrom", result.header("Link"));
        assertEquals(getResource("data/InC/1.0.jsonld"), contentAsString(result));
      }
    });

  }

  @Test
  public void testGetStatementDataAsJson() {

    running(fakeApplication(), new Runnable() {
      @Override
      public void run() {
        Result result = route(fakeRequest(routes.Application.getStatementData("InC", "1.0", null))
            .header("Accept", "application/json"));
        assertEquals(200, result.status());
        assertEquals("application/json", result.contentType());
        assertEquals("http://null/data/InC/1.0.json", result.header("Content-Location"));
        assertEquals("<http://null/data/InC/1.0/>; rel=derivedfrom", result.header("Link"));
        assertEquals(getResource("data/InC/1.0.jsonld"), contentAsString(result));
      }
    });

  }

  @Test
  public void testGetStatementDataAsJsonLd() {

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

  @Test
  public void testGetVocabPage() {

    running(fakeApplication(), new Runnable() {
      @Override
      public void run() {
        Result result = route(fakeRequest(routes.Application.getVocabPage("1.0", "en"))
            .header("Accept", "text/html"));
        assertEquals(200, result.status());
        assertEquals("text/html", result.contentType());
        assertEquals("<http://null/page/1.0/>; rel=derivedfrom", result.header("Link"));
        assertEquals("en", result.header("Content-Language"));
        assertEquals(getResource("page/1.0"), contentAsString(result));
      }
    });

  }

  @Test
  public void testGetStatementPage() {

    running(fakeApplication(), new Runnable() {
      @Override
      public void run() {
        Result result = route(fakeRequest(routes.Application.getStatementPage("InC", "1.0", "en"))
            .header("Accept", "text/html"));
        assertEquals(200, result.status());
        assertEquals("text/html", result.contentType());
        assertEquals("<http://null/page/InC/1.0/>; rel=derivedfrom", result.header("Link"));
        assertEquals("en", result.header("Content-Language"));
        assertEquals(getResource("page/InC/1.0"), contentAsString(result));
      }
    });

  }

  @Test
  public void testStatementAlternates() {

    running(fakeApplication(), new Runnable() {
      @Override
      public void run() {
        Result result = route(fakeRequest("GET", routes.Application.getStatement("InC-OW-EU", "1.0").url()
            .concat("?relatedURL=http://example.org/")));
        assertEquals(406, result.status());
        assertEquals("{\"/vocab/InC-OW-EU/1.0/\" 0.9},{\"/page/InC-OW-EU/1.0/?relatedURL=http://example.org/\" 0.9 " +
            "{text/html}},{\"/data/InC-OW-EU/1.0/\" 0.9 {application/ld+json}},{\"/data/InC-OW-EU/1.0/\" 0.9 " +
            "{application/json}},{\"/data/InC-OW-EU/1.0/\" 0.9 {text/turtle}}", result.header("Alternates"));
      }
    });

  }

  @Test
  public void testStatementDataAlternates() {

    running(fakeApplication(), new Runnable() {
      @Override
      public void run() {
        Result result = route(fakeRequest("GET", routes.Application.getStatementData("InC-OW-EU", "1.0", null).url()
            .concat("?relatedURL=http://example.org/")));
        assertEquals(406, result.status());
        assertEquals("{\"/vocab/InC-OW-EU/1.0/\" 0.9},{\"/page/InC-OW-EU/1.0/?relatedURL=http://example.org/\" 0.9 " +
            "{text/html}},{\"/data/InC-OW-EU/1.0/\" 0.9 {application/ld+json}},{\"/data/InC-OW-EU/1.0/\" 0.9 " +
            "{application/json}},{\"/data/InC-OW-EU/1.0/\" 0.9 {text/turtle}}", result.header("Alternates"));
      }
    });

  }

  @Test
  public void testInvalidStatementParameter() {

    running(fakeApplication(), new Runnable() {
      @Override
      public void run() {
        Result result = route(fakeRequest("GET", routes.Application.getStatement("InC", "1.0").url()
            .concat("?relatedURL=http://example.org/")));
        assertEquals(406, result.status());
        assertNull(result.header("Alternates"));
      }
    });

  }

  @Test
  public void testInvalidStatementDataParameter() {

    running(fakeApplication(), new Runnable() {
      @Override
      public void run() {
        Result result = route(fakeRequest("GET", routes.Application.getStatementData("InC", "1.0", null).url()
            .concat("?relatedURL=http://example.org/")));
        assertEquals(406, result.status());
        assertNull(result.header("Alternates"));
      }
    });

  }

  @Test
  public void testGetCollectionAsTurtle() {

    running(fakeApplication(), new Runnable() {
      @Override
      public void run() {
        Result result = route(fakeRequest(routes.Application.getCollection("ic", "1.0", null, null))
            .header("Accept", "text/turtle"));
        assertEquals(200, result.status());
        assertEquals("text/turtle", result.contentType());
        assertEquals("http://null/collection-ic/1.0.ttl", result.header("Content-Location"));
        assertEquals(getResource("collection/ic/1.0.ttl"), contentAsString(result));
      }
    });

  }

  @Test
  public void testGetCollectionAsJson() {

    running(fakeApplication(), new Runnable() {
      @Override
      public void run() {
        Result result = route(fakeRequest(routes.Application.getCollection("ic", "1.0", null, null))
            .header("Accept", "application/json"));
        assertEquals(200, result.status());
        assertEquals("application/json", result.contentType());
        assertEquals("http://null/collection-ic/1.0.json", result.header("Content-Location"));
        assertEquals(getResource("collection/ic/1.0.jsonld"), contentAsString(result));
      }
    });

  }

  @Test
  public void testGetCollectionAsJsonLd() {

    running(fakeApplication(), new Runnable() {
      @Override
      public void run() {
        Result result = route(fakeRequest(routes.Application.getCollection("ic", "1.0", null, null))
            .header("Accept", "application/ld+json"));
        assertEquals(200, result.status());
        assertEquals("application/ld+json", result.contentType());
        assertEquals("http://null/collection-ic/1.0.jsonld", result.header("Content-Location"));
        assertEquals(getResource("collection/ic/1.0.jsonld"), contentAsString(result));
      }
    });

  }

  @Test
  public void testGetCollectionPage() {

    running(fakeApplication(), new Runnable() {
      @Override
      public void run() {
        Result result = route(fakeRequest(routes.Application.getCollection("ic", "1.0", null, null))
            .header("Accept", "text/html"));
        assertEquals(200, result.status());
        assertEquals("text/html", result.contentType());
        assertEquals(getResource("collection/ic/1.0.html"), contentAsString(result));
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
