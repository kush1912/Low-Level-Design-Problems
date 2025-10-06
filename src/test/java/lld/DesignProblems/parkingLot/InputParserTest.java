package lld.DesignProblems.parkingLot;

import lld.DesignProblems.parkingLot.service.CommandService;
import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class InputParserTest {
    CommandService commandService = new CommandService();
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @Before
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
    }

    @After
    public void cleanUpStreams() {
        System.setOut(null);
    }

    @Test
    public void parseTextInput() throws Exception {
        File file = new File("inputTest.txt");
        assertTrue(file.exists());
    }
}
