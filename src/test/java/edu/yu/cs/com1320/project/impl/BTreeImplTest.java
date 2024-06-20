package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.stage6.Document;
import edu.yu.cs.com1320.project.stage6.impl.DocumentImpl;
import edu.yu.cs.com1320.project.stage6.impl.DocumentPersistenceManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class BTreeImplTest {

    BTreeImpl<URI, Document> tree = new BTreeImpl();
    DocumentImpl txt;
    DocumentImpl txt3;
    DocumentImpl txt4;
    DocumentImpl txt5;
    DocumentImpl txt6;
    DocumentImpl txt7;
    DocumentImpl txt8;
    DocumentImpl txt9;
    DocumentImpl txt10;
    DocumentImpl txt11;
    DocumentImpl txt12;
    DocumentImpl txt13;
    DocumentImpl txt14;
    DocumentImpl txt15;
    DocumentImpl txt16;
    DocumentImpl txt17;
    DocumentImpl bin;
    URI uri1=  new URI("http://yu.edu/DataStructures/doc1");
    URI uri2=  new URI("http://yu.edu/DataStructures/doc2");
    URI uri3=  new URI("http://yu.edu/DataStructures/doc3");
    URI uri4=  new URI("http://yu.edu/DataStructures/doc4");
    URI uri5=  new URI("http://yu.edu/DataStructures/doc5");
    URI uri6=  new URI("http://yu.edu/DataStructures/doc6");
    URI uri7=  new URI("http://yu.edu/DataStructures/doc7");
    URI uri8=  new URI("http://yu.edu/DataStructures/doc8");

    URI uri9=  new URI("http://yu.edu/DataStructures/doc9");
    URI uri10=  new URI("http://yu.edu/DataStructures/doc10");
    URI uri11=  new URI("http://yu.edu/DataStructures/doc11");
    URI uri12=  new URI("http://yu.edu/DataStructures/doc12");
    URI uri13=  new URI("http://yu.edu/DataStructures/doc13");
    URI uri14=  new URI("http://yu.edu/DataStructures/doc14");
    URI uri15=  new URI("http://yu.edu/DataStructures/doc15");
    URI uri16=  new URI("http://yu.edu/DataStructures/doc16");
    URI uri17=  new URI("http://yu.edu/DataStructures/doc17");



    BTreeImplTest() throws URISyntaxException {
    }

    @BeforeEach
    void setup()
    {
        tree.setPersistenceManager(new DocumentPersistenceManager(null));
        byte[] bytes = {1,2,3};
        txt = new DocumentImpl(uri1, "Hello Hello",null);
        HashMap<String,String> md = new HashMap<>();
        md.put("word", "1");
        md.put("words", "1s");
        txt.setMetadata(md);
        bin = new DocumentImpl(uri2, bytes);

        txt3 = new DocumentImpl(uri3, "Hello HI Hello",null);
        txt4 = new DocumentImpl(uri4, "Hello Hello Hello Hello",null);
        txt5 = new DocumentImpl(uri5, "Hello Hello five Hello Hello",null);
        txt6 = new DocumentImpl(uri6, "Hello Hello six Hello Hello",null);
        txt7 = new DocumentImpl(uri7, "Hello Hello hi hi Hello Hello",null);
        txt8 = new DocumentImpl(uri8, "Hello Hello  Hello Hello Hello Hello Hello Hello",null);

        txt9 = new DocumentImpl(uri9, "Hello HI Hello",null);
        txt10 = new DocumentImpl(uri10, "Hello Hello Hello Hello",null);
        txt11 = new DocumentImpl(uri11, "Hello Hello five Hello Hello",null);
        txt12 = new DocumentImpl(uri12, "Hello Hello six Hello Hello",null);
        txt13 = new DocumentImpl(uri13, "Hello Hello hi hi Hello Hello",null);
        txt14 = new DocumentImpl(uri14, "Hello Hello  Hello Hello Hello Hello Hello Hello",null);
        txt15 = new DocumentImpl(uri15, "Hello Hello hi hi Hello Hello",null);
        txt16 = new DocumentImpl(uri16, "Hello Hello  Hello Hello Hello Hello Hello Hello",null);
        txt17 = new DocumentImpl(uri17, "Hello Hello  Hello Hello Hello Hello Hello Hello",null);
    }
    @Test
    void put() {
        assertNull(tree.put(uri1, txt));
        assertNull(tree.put(uri2, bin));
        assertNull(tree.put(uri3, txt3));
        assertNull(tree.put(uri4, txt4));
        tree.put(uri5, txt5);
        tree.put(uri6, txt6);
        tree.put(uri7, txt7);
        tree.put(uri8, txt8);
        tree.put(uri9, txt9);
        tree.put(uri10, txt10);
        tree.put(uri11, txt11);
        tree.put(uri12, txt12);
        tree.put(uri13, txt13);
        tree.put(uri14, txt14);
        tree.put(uri15, txt15);
        tree.put(uri16, txt16);
        tree.put(uri17, txt17);

    }

    @Test
    void get() {
        tree.put(uri17, txt17);
        tree.put(uri1, txt);
        assertEquals(txt17, tree.get(uri17));
        tree.put(uri17, null);
        assertEquals(txt17, tree.get(uri17));
        tree.put(uri1, null);
        tree.get(uri2);

        tree.put(uri5, txt5);
        tree.put(uri5, null);
        tree.get(uri5);

    }

    @Test
    void moveToDisk() throws IOException, URISyntaxException {
        tree.put(uri4, txt4);
        tree.moveToDisk(uri4);
        assertNotNull(tree.get(uri4));
        //DocumentPersistenceManager pm = new DocumentPersistenceManager(null);
        //assertEquals(txt4,pm.deserialize(uri4));
        //pm.delete(new URI(uri4.toString().substring(7)+".json"));


    }

//    @Test
//    void setPersistenceManager() {
//    }
}