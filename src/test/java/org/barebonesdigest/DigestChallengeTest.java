package org.barebonesdigest;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

public class DigestChallengeTest {
  @Test
  public void testMinimalChallenge() throws Exception {
    String CHALLENGE = "Digest " +
        "realm=\"testrealm@host.com\", " +
        "nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\"";

    DigestChallenge header = DigestChallenge.parse(CHALLENGE);

    assertNotNull(header);
    assertEquals("\"testrealm@host.com\"", header.getQuotedRealm());
    assertEquals("\"dcd98b7102dd2f0e8b11d0f600bfb0c093\"", header.getQuotedNonce());
  }

  @Test
  public void testMalformedChallengeMissingRealm() throws Exception {
    // The example below is from Section 3.5 of RC 2617,
    // https://tools.ietf.org/html/rfc2617#section-3.5
    String CHALLENGE = "Digest " +
        "nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\"";

    assertNull(DigestChallenge.parse(CHALLENGE));
  }

  @Test
  public void testMalformedChallengeMissingNonce() throws Exception {
    // The example below is from Section 3.5 of RC 2617,
    // https://tools.ietf.org/html/rfc2617#section-3.5
    String CHALLENGE = "Digest " +
        "realm=\"testrealm@host.com\"";

    assertNull(DigestChallenge.parse(CHALLENGE));
  }

  @Test
  public void testMd5Algorithm() throws Exception {
    // The example below is from Section 3.5 of RC 2617,
    // https://tools.ietf.org/html/rfc2617#section-3.5
    String CHALLENGE = "Digest " +
        "realm=\"testrealm@host.com\", " +
        "nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\", " +
        "algorithm=MD5";

    DigestChallenge header = DigestChallenge.parse(CHALLENGE);

    assertNotNull(header);
    assertEquals("MD5", header.getAlgorithm());
  }

  @Test
  public void testMd5SessAlgorithm() throws Exception {
    // The example below is from Section 3.5 of RC 2617,
    // https://tools.ietf.org/html/rfc2617#section-3.5
    String CHALLENGE = "Digest " +
        "realm=\"testrealm@host.com\", " +
        "nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\", " +
        "algorithm=MD5-sess";

    DigestChallenge header = DigestChallenge.parse(CHALLENGE);

    assertNotNull(header);
    assertEquals("MD5-sess", header.getAlgorithm());
  }

  @Test
  public void testMissingAlgorithm() throws Exception {
    // The example below is from Section 3.5 of RC 2617,
    // https://tools.ietf.org/html/rfc2617#section-3.5
    String CHALLENGE = "Digest " +
        "realm=\"testrealm@host.com\", " +
        "nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\"";

    DigestChallenge header = DigestChallenge.parse(CHALLENGE);

    assertNotNull(header);
    assertEquals("MD5", header.getAlgorithm());
  }

  @Test
  public void testExampleFromRfc2617() throws Exception {
    // The example below is from Section 3.5 of RC 2617,
    // https://tools.ietf.org/html/rfc2617#section-3.5
    String EXAMPLE = "Digest " +
        "realm=\"testrealm@host.com\", " +
        "qop=\"auth,auth-int\", " +
        "nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\", " +
        "opaque=\"5ccc069c403ebaf9f0171e9517f40e41\"";

    DigestChallenge header = DigestChallenge.parse(EXAMPLE);

    assertNotNull(header);
    assertEquals("\"testrealm@host.com\"", header.getQuotedRealm());
    assertEquals("\"dcd98b7102dd2f0e8b11d0f600bfb0c093\"", header.getQuotedNonce());
    assertEquals("\"5ccc069c403ebaf9f0171e9517f40e41\"", header.getQuotedOpaque());
    assertEquals("MD5", header.getAlgorithm());
  }

  @Test
  public void testUnquotingRealm() {
    String CHALLENGE = "Digest " +
        "realm=\"testrealm@host.com\", " +
        "nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\"";

    DigestChallenge header = DigestChallenge.parse(CHALLENGE);

    assertNotNull(header);
    assertEquals("testrealm@host.com", header.getRealm());
  }

  @Test
  public void testUnquotingNonce() {
    String CHALLENGE = "Digest " +
        "realm=\"testrealm@host.com\", " +
        "nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\"";

    DigestChallenge header = DigestChallenge.parse(CHALLENGE);

    assertNotNull(header);
    assertEquals("dcd98b7102dd2f0e8b11d0f600bfb0c093", header.getNonce());
  }

  @Test
  public void testUnquotingOpaque() {
    String CHALLENGE = "Digest " +
        "realm=\"testrealm@host.com\", " +
        "qop=\"auth,auth-int\", " +
        "nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\", " +
        "opaque=\"5ccc069c403ebaf9f0171e9517f40e41\"";

    DigestChallenge header = DigestChallenge.parse(CHALLENGE);

    assertNotNull(header);
    assertEquals("5ccc069c403ebaf9f0171e9517f40e41", header.getOpaque());
  }

  @Test
  public void testThatDigestLiteralIsCaseInsensitive() throws Exception {
    String CHALLENGE = "DIGEST " +
        "realm=\"\", " +
        "qop=\"auth\", " +
        "nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\"";

    DigestChallenge header = DigestChallenge.parse(CHALLENGE);

    assertNotNull(header);
  }
}