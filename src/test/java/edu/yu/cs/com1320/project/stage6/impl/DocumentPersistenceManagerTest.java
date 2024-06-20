package edu.yu.cs.com1320.project.stage6.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class DocumentPersistenceManagerTest {
    private DocumentPersistenceManager pm = new DocumentPersistenceManager(null);
    DocumentImpl txt;
    DocumentImpl bin;
    URI uri1=  new URI("http://yu.edu/DataStructures/doc1");
    URI uri2=  new URI("http://yu.edu/DataStructures/doc2");
    URI uri3=  new URI("http://yu.edu/DataStructures/doc3");
    URI uri4=  new URI("http://yu.edu/DataStructures/doc4");
    DocumentImpl doc1;
    DocumentImpl doc2;
    DocumentImpl doc3;
    DocumentImpl doc4;

    DocumentPersistenceManagerTest() throws URISyntaxException {
    }


    @BeforeEach
    void setUp() throws URISyntaxException {
        byte[] bytes = {1,2,3};
        txt = new DocumentImpl(uri1, "Hello Hello",null);

        doc1 = new DocumentImpl(uri1,"text",null);
        doc2 = new DocumentImpl(uri2, "texttext", null);
        doc3 = new DocumentImpl(uri3, "text texttext", null);
        doc4 = new DocumentImpl(uri4, "texttexttexttext", null);
        HashMap<String,String> md = new HashMap<>();
        md.put("word", "1");
        md.put("words", "1s");
        //doc1.setMetadata(md);
        bin = new DocumentImpl(uri2, bytes);
    }
    @Test

    void serialize() throws IOException, URISyntaxException {
//        pm.serialize(uri1, doc1);
//        pm.serialize(uri2, doc2);
//        pm.serialize(uri2, pm.deserialize(uri2));
//        pm.serialize(uri3, doc3);

        HashMap<String,String> md1 = new HashMap<>();
        md1.put("k1.1","v1.1");
        md1.put("k1.2","v1.2");
        md1.put("k1.3","v1.3");
        md1.put("k1.4","v1.4");
        doc1.setMetadata(md1);

        HashMap<String,String> md2 = new HashMap<>();
        md2.put("k2.1","v2.1");
        md2.put("k2.2","v2.2");
        md2.put("k2.3","v2.3");
        md2.put("k2.4","v2.4");
        doc2.setMetadata(md2);





        HashMap<String,String> md3 = new HashMap<>();
        md3.put("k3.1","v3.1");
        md3.put("k3.2","v3.2");
        md3.put("k3.3","v3.3");
        md3.put("k3.4","v3.4");
        doc3.setMetadata(md3);

        pm.serialize(uri1, doc1);
        pm.serialize(uri2, doc2);
        pm.serialize(uri3, doc3);

        pm.serialize(uri1, pm.deserialize(uri1));
        pm.serialize(uri2, pm.deserialize(uri2));
        pm.serialize(uri3, pm.deserialize(uri3));


    }

    @Test
    void gradeChange() throws URISyntaxException, IOException {
        pm=new DocumentPersistenceManager(new File("\\tmp\\stage62945358822195770960\\"));
        uri1 = new URI(("http://edu.yu.cs/com1320/project/doc1"));
        pm.serialize(uri1, doc1);
        assertEquals(doc1, pm.deserialize(uri1));


    }

    @Test
    void deserialize() throws IOException, URISyntaxException {
        assertEquals(doc2, pm.deserialize(uri2));
        assertEquals(doc1,pm.deserialize(uri1));
        assertEquals(doc3,pm.deserialize(uri3));
        pm.serialize(uri1, doc1);
        pm.serialize(uri2, doc2);
        pm.serialize(uri3, doc3);
    }

    @Test
    void delete() throws IOException, URISyntaxException {
//        pm.delete(uri1);
//        pm.delete(uri2);
        pm.delete(new URI(uri2.toString().substring(7)+".json"));
        pm.delete(new URI(uri1.toString().substring(7)+".json"));
    }
}