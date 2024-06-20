package edu.yu.cs.com1320.project.stage6.impl;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

class DocumentImplTest {

    @Test
    void wordCount() throws URISyntaxException {
        DocumentImpl doc1 = new DocumentImpl(new URI("http://yu.edu"),"text",null);

        assertEquals(1,doc1.wordCount("text"));
    }
}