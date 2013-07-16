package com.tw.go.plugin.nuget;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.internal.matchers.StringContains.containsString;

public class ProcessRunnerTest {

    public static final String[] CHOICE_Y = new String[]{"choice", "/T", "0", "/D", "Y"};
    public static final String[] CHOICE_INVALID = new String[]{"choice", "/T", "0", "/D", "B"};

    @Test
    public void shouldRunACommand() {
        NuGetCmdOutput output = new ProcessRunner().execute(CHOICE_Y);
        assertThat(output.getStdOut().get(0), is("[Y,N]?Y"));
        assertThat(output.getReturnCode(), is(1));
    }

    @Test
    public void shouldThrowExceptionIfCommandThrowsAnException() {
        try {
            new ProcessRunner().execute(new String[]{"doesNotExist"});
            fail("Should have thrown exception");
        } catch (Exception e) {
            assertThat(e instanceof RuntimeException, is(true));
            assertThat(e.getMessage(), containsString("Cannot run program \"doesNotExist\""));
        }
    }

    @Test
    public void shouldReturnErrorOutputIfCommandFails() {
        NuGetCmdOutput output = new ProcessRunner().execute(CHOICE_INVALID);
        assertThat(output.getErrorDetail(), containsString("Error Message: ERROR: Invalid syntax."));
        assertThat(output.getReturnCode(), is(not(0)));
    }
}
