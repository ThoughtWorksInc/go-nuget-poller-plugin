package com.tw.go.plugin.nuget;

import org.junit.Test;

import java.net.UnknownHostException;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class HttpConnectionCheckerTest {
    @Test
    public void shouldNotThrowExceptionIfCheckConnectionToTheRepoPasses() {
        new HttpConnectionChecker().checkConnection("http://google.com", new Credentials(null, null));
    }

    @Test
    public void shouldFailCheckConnectionToTheRepoWhenUrlIsNotReachable() {
        try {
            new HttpConnectionChecker().checkConnection("http://nonexistentfqdngibberish.com", new Credentials(null, null));
            fail("should fail");
        } catch (RuntimeException e) {
            assertTrue(e.getCause() instanceof UnknownHostException);
        }
    }
}
