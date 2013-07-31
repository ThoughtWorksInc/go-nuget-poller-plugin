package com.tw.go.plugin.util;

import com.tw.go.plugin.util.RepoUrl;
import org.junit.Test;

import java.net.UnknownHostException;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class HttpRepoURLTest {
    @Test
    public void shouldNotThrowExceptionIfCheckConnectionToTheRepoPasses() {
        RepoUrl.create("http://google.com", null, null).checkConnection();
    }

    @Test
    public void shouldFailCheckConnectionToTheRepoWhenUrlIsNotReachable() {
        try {
            RepoUrl.create("http://nonexistentfqdngibberish.com", null, null).checkConnection();
            fail("should fail");
        } catch (RuntimeException e) {
            assertTrue(e.getCause() instanceof UnknownHostException);
        }
    }
}
