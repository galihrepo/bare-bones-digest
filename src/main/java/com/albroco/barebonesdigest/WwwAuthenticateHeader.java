package com.albroco.barebonesdigest;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Class for extracting challenges from <code>WWW-Authenticate</code> headers.
 * <p>
 * The <code>WWW-Authenticate</code> header is described in
 * <a href="https://tools.ietf.org/html/rfc7235#section-4.1">Section 4.1 of RFC 7235</a>. It can
 * contain one or more challenges and it can appear multiple times in each response.
 * <p>
 * Example: The following header:
 * <pre>
 * WWW-Authenticate: Newauth realm="apps", type=1, title="Login to \"apps\"", Basic realm="simple"
 * </pre>
 * contains two challenges, <code>Newauth realm="apps", type=1, title="Login to \"apps\""</code> and
 * <code>Basic realm="simple"</code>.
 * <p>
 * This class is not specific for digest authentication. It returns the challenges as strings and
 * can extract challenges of any type.
 */
public class WwwAuthenticateHeader {
  /**
   * Name of the HTTP response header WWW-Authenticate.
   *
   * @see <a href="https://tools.ietf.org/html/rfc2616#section-14.47">RFC 7235, Section 14.47,
   * WWW-Authenticate</a>
   */
  public static final String HTTP_HEADER_WWW_AUTHENTICATE = "WWW-Authenticate";

  private WwwAuthenticateHeader() {
  }

  /**
   * Extracts challenges from an HTTP response.
   *
   * @param connection the connection the response will be read from
   * @return a list of challenges
   * @throws HttpDigestChallengeParseException if the challenges are malformed and could not be
   * parsed
   */
  public static List<String> extractChallenges(HttpURLConnection connection) throws
      HttpDigestChallengeParseException {
    return extractChallenges(connection.getHeaderFields());
  }

  /**
   * Extracts challenges from a set of HTTP headers.
   * <p>
   * A note about the map representing the headers: header names are case insensitive in HTTP. This
   * means that the <code>WWW-Authenticate</code> header can be represented in multiple
   * ways (<code>WWW-Authenticate</code>, <code>www-authenticate</code>, etc), even in the same
   * HTTP response. This method makes no assumption about the case of the headers, but two keys
   * in the map must not be equal if case is disregarded, that is, all case variations of
   * <code>WWW-Authenticate</code> must be collected with the same key. Incidentally, this is
   * what is returned by {@code HttpURLConnection.getHeaderFields()}.
   *
   * @param headers the headers, as a map where the keys are header names and values are
   *                iterables where each element is a header value string
   * @return a list of challenges
   * @throws HttpDigestChallengeParseException if the challenges are malformed and could not be
   * parsed
   */
  public static <T extends Iterable<String>> List<String> extractChallenges(Map<String, T>
      headers) throws HttpDigestChallengeParseException {
    if (headers.containsKey(HTTP_HEADER_WWW_AUTHENTICATE)) {
      return extractChallenges(headers.get(HTTP_HEADER_WWW_AUTHENTICATE));
    }

    for (String headerName : headers.keySet()) {
      if (HTTP_HEADER_WWW_AUTHENTICATE.equalsIgnoreCase(headerName)) {
        return extractChallenges(headers.get(headerName));
      }
    }

    return Collections.emptyList();
  }

  /**
   * Extracts challenges from a set of <code>WWW-Authenticate</code> HTTP headers.
   *
   * @param wwwAuthenticateHeaders the header values
   * @return a list of challenges
   * @throws HttpDigestChallengeParseException if the challenges are malformed and could not be
   * parsed
   */
  public static List<String> extractChallenges(Iterable<String> wwwAuthenticateHeaders) throws
      HttpDigestChallengeParseException {
    List<String> result = new ArrayList<>();
    for (String header : wwwAuthenticateHeaders) {
      extractChallenges(header, result);
    }
    return result;
  }

  /**
   * Extracts challenges from a <code>WWW-Authenticate</code> header.
   *
   * @param wwwAuthenticateHeader the header value
   * @return a list of challenges
   * @throws HttpDigestChallengeParseException if the challenges are malformed and could not be
   * parsed
   */
  public static List<String> extractChallenges(String wwwAuthenticateHeader) throws
      HttpDigestChallengeParseException {
    List<String> result = new ArrayList<>();
    extractChallenges(wwwAuthenticateHeader, result);
    return result;
  }

  private static void extractChallenges(String header,
      List<String> result) throws HttpDigestChallengeParseException {
    Rfc2616AbnfParser parser = new Rfc2616AbnfParser(header);
    while (parser.hasMoreData()) {
      try {
        int startOfChallenge = parser.getPos();
        consumeChallenge(parser);
        result.add(parser.getInput().substring(startOfChallenge, parser.getPos()));
        parser.consumeWhitespace();
        if (parser.hasMoreData()) {
          parser.consumeLiteral(",").consumeWhitespace();
        }
      } catch (Rfc2616AbnfParser.ParseException e) {
        throw new HttpDigestChallengeParseException(e);
      }
    }
  }

  private static void consumeChallenge(Rfc2616AbnfParser parser) throws Rfc2616AbnfParser
      .ParseException {
    parser.consumeToken().consumeWhitespace(); // auth-scheme

    int savedPos = parser.getPos();
    try {
      consumeToEndOfEmptyOrAuthParamBasedChallenge(parser);
    } catch (Rfc2616AbnfParser.ParseException e) {
      parser.setPos(savedPos);
      consumeToEndOfToken68BasedChallenge(parser);
    }
  }

  private static void consumeToEndOfToken68BasedChallenge(Rfc2616AbnfParser parser) throws
      Rfc2616AbnfParser.ParseException {
    parser.consumeToken68().consumeWhitespace(); // token68
    if (parser.hasMoreData()) {
      int pos = parser.getPos();
      parser.consumeLiteral(",").setPos(pos);
    }
  }

  private static void consumeToEndOfEmptyOrAuthParamBasedChallenge(Rfc2616AbnfParser parser)
      throws Rfc2616AbnfParser.ParseException {
    boolean firstAuthParam = true;
    while (parser.hasMoreData()) {
      int possibleEndOfChallenge = parser.getPos();
      if (!firstAuthParam) {
        parser.consumeLiteral(",").consumeWhitespace();
      }
      parser.consumeToken().consumeWhitespace();
      if (firstAuthParam || parser.isLookingAtLiteral("=")) {
        parser.consumeLiteral("=")
            .consumeWhitespace()
            .consumeQuotedStringOrToken()
            .consumeWhitespace();
      } else {
        parser.setPos(possibleEndOfChallenge);
        return;
      }
      firstAuthParam = false;
    }
  }
}