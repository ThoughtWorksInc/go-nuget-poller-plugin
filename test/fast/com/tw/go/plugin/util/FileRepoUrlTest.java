package com.tw.go.plugin.util;

import com.tw.go.plugin.util.FileRepoUrl;
import com.tw.go.plugin.util.RepoUrl;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class FileRepoUrlTest {

    @Test
    public void shouldNotThrowExceptionIfFileExistsPasses() {
        String absolutePath = new File("").getAbsolutePath();
        new FileRepoUrl("file://" + absolutePath).checkConnection();
    }

    @Test
    public void shouldThrowExceptionIfUserNameAndPasswordIsProvided() {
        String absolutePath = new File("").getAbsolutePath();
        try {
            RepoUrl.create("file://" + absolutePath, "user", "passwd").checkConnection();
            fail("should fail");
        } catch (Exception e) {
            assertThat(e.getMessage(), is("File protocol does not support username and/or password."));
        }
    }

    @Test
    public void shouldFailCheckConnectionIfFileDoesNotExist() {
        try {
            new FileRepoUrl("file://foo").checkConnection();
            fail("should fail");
        } catch (Exception e) {
            assertThat(e.getMessage(), is("Invalid file path."));
        }
    }
}
