package edu.yu.cs.com1320.project.stage6.impl;

import edu.yu.cs.com1320.project.stage6.Document;
import edu.yu.cs.com1320.project.stage6.DocumentStore;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.print.Doc;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DocumentStoreImplTest {
    DocumentStoreImpl store;
    URI uri1;
    URI uri2;
    URI uri3;
    URI uri4;
    DocumentImpl doc1;
    DocumentImpl doc2;
    DocumentImpl doc3;
    DocumentImpl doc4;

    String txt1;
    String txt2;
    String txt3;
    String txt4;

    @BeforeEach
    void setUP() throws URISyntaxException, IOException {
        store= new DocumentStoreImpl(null);
        uri1 = new URI("http://yu.edu/DS/stageDocs/doc1");
        uri2 = new URI("http://yu.edu/DS/stageDocs/doc2");
        uri3 = new URI("http://yu.edu/DS/stageDocs/doc3");
        uri4 = new URI("http://yu.edu/DS/stageDocs/doc4");

        doc1 = new DocumentImpl(uri1,"text new",null);
        doc2 = new DocumentImpl(uri2, "texttext", null);
        doc3 = new DocumentImpl(uri3, "texttexttext", null);
        doc4 = new DocumentImpl(uri4, "texttexttexttext", null);

        txt1="text new";
        txt2="texttext";
        txt3="texttexttext";
        txt4="texttexttexttext";


        store.put(new ByteArrayInputStream(this.txt1.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
        store.setMetadata(uri1, "k1", "v1");
//        store.setMetadata(uri1, "k2", "v2");
        store.put(new ByteArrayInputStream(this.txt2.getBytes()),this.uri2, DocumentStore.DocumentFormat.TXT);
        store.put(new ByteArrayInputStream(this.txt3.getBytes()),this.uri3, DocumentStore.DocumentFormat.TXT);
        store.put(new ByteArrayInputStream(this.txt4.getBytes()),this.uri4, DocumentStore.DocumentFormat.TXT);

    }
    @Test
    public void stage6PushToDiskViaMaxDocCountBringBackInViaMetadataSearch() throws IOException
    {
        DocumentStoreImpl ds = new DocumentStoreImpl();
        ds.setMaxDocumentCount(2);
        pushOutToMemoryBringInDocViaMetadatSearch(ds);
    }
    private void pushOutToMemoryBringInDocViaMetadatSearch(DocumentStoreImpl store) throws IOException
    {
        store.put(new ByteArrayInputStream(this.txt1.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
        store.setMetadata(uri1, "k1", "v1");
        store.put(new ByteArrayInputStream(this.txt2.getBytes()),this.uri2, DocumentStore.DocumentFormat.TXT);
        Document doc1= store.get(uri1);
        Document doc2 = store.get(uri2);
        store.put(new ByteArrayInputStream(this.txt3.getBytes()),this.uri3, DocumentStore.DocumentFormat.TXT);
        Document doc3 = store.get(uri3);
        store.setMetadata(uri3, "k1", "v1");
    }

    @Test
    void errorTest() throws IOException {
        store.delete(uri1);
        store.delete(uri2);
        store.delete(uri3);
        store.delete(uri4);
        store.setMaxDocumentCount(2);
        store.put(new ByteArrayInputStream(this.txt1.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
        store.setMetadata(uri1, "k1", "v1");
        store.put(new ByteArrayInputStream(this.txt2.getBytes()),this.uri2, DocumentStore.DocumentFormat.TXT);
        Document doc1= store.get(uri1);
        Document doc2 = store.get(uri2);
        store.put(new ByteArrayInputStream(this.txt3.getBytes()),this.uri3, DocumentStore.DocumentFormat.TXT);


    }

    @Test
    void setMetadata() throws IOException {

        store.setMetadata(uri1, "key1","val1");
        System.out.println("text".getBytes().length+"texttext".getBytes().length+"texttexttext".getBytes().length+"texttexttexttext".getBytes().length);
        //store.setMaxDocumentCount(2);
        store.setMaxDocumentBytes("text".getBytes().length);
        store.setMaxDocumentBytes(Integer.MAX_VALUE);
        store.setMetadata(uri2, "key2","val2");
        assertEquals("val1", store.getMetadata(uri1,"key1"));
        store.setMaxDocumentBytes(4);
        store.setMaxDocumentBytes(Integer.MAX_VALUE);
        store.setMetadata(uri1, "key1.2", "val1.2");
        store.get(uri2);
        store.get(uri4);
        store.get(uri3);
        store.setMaxDocumentBytes(16);
        assertEquals("val1.2", store.getMetadata(uri1, "key1.2"));
        store.setMaxDocumentCount(1);
        store.get(uri4);
        store.setMaxDocumentBytes(20);
        assertEquals(store.get(uri4), doc4);
        store.setMaxDocumentCount(2);
        store.setMetadata(uri1, "key1.3", "val1.3");
        store.get(uri4);
        store.setMaxDocumentBytes(16);
    }

    @Test
    void getMetadata() {

    }

    @Test
    void put() throws IOException {
        store.setMaxDocumentCount(1);
        store.put(new ByteArrayInputStream("new text".getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);

        store.put(new ByteArrayInputStream("new text 2 text".getBytes()),this.uri2, DocumentStore.DocumentFormat.TXT);
        store.put(new ByteArrayInputStream("new text 3 text text 3".getBytes()),this.uri3, DocumentStore.DocumentFormat.TXT);
        store.put(new ByteArrayInputStream(this.txt4.getBytes()),this.uri4, DocumentStore.DocumentFormat.TXT);

    }

    @Test
    void get() {
    }

    @Test
    void delete() throws IOException {
        store.put(new ByteArrayInputStream("new text".getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
        store.setMetadata(uri1,"k1", "v1");
        store.setMaxDocumentCount(1);
        store.delete(uri2);
        store.delete(uri1);
        //tore.undo();
        assertNull(store.get(uri1));
        store.get(uri3);
        //store.delete(uri3);
    }


    @Test
    void grade1() throws IOException, URISyntaxException {

        DocumentStoreImpl store= new DocumentStoreImpl(null);
        URI uri1 = new URI("http://yu.edu/DS/stageDocs/doc1");
        URI uri2 = new URI("http://yu.edu/DS/stageDocs/doc2");
        URI uri3 = new URI("http://yu.edu/DS/stageDocs/doc3");
        URI uri4 = new URI("http://yu.edu/DS/stageDocs/doc4");

        DocumentImpl doc1 = new DocumentImpl(uri1,"text new",null);
        DocumentImpl doc2 = new DocumentImpl(uri2, "texttext", null);
        DocumentImpl doc3 = new DocumentImpl(uri3, "texttexttext", null);
        DocumentImpl doc4 = new DocumentImpl(uri4, "texttexttexttext", null);

        String txt1="text new";
        String txt2="texttext";
        String txt3="texttexttext";
        String txt4="texttexttexttext";


        store.put(new ByteArrayInputStream(this.txt1.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
//        store.setMetadata(uri1, "k1", "v1");
//        store.setMetadata(uri1, "k2", "v2");
        store.put(new ByteArrayInputStream(this.txt2.getBytes()),this.uri2, DocumentStore.DocumentFormat.TXT);
        store.put(new ByteArrayInputStream(this.txt3.getBytes()),this.uri3, DocumentStore.DocumentFormat.TXT);
        store.put(new ByteArrayInputStream(this.txt4.getBytes()),this.uri4, DocumentStore.DocumentFormat.TXT);
        store.setMaxDocumentCount(2);
        store.getMetadata(uri1, "k1");

        store.delete(uri3);
        store.setMetadata(uri3,"k1", "v2");
        assertNull(store.get(uri3));

    }

    @Test
    void stage6PushToDiskViaMaxDocCountBringBackInViaMetadataSearchFinal() throws IOException {
        store.setMaxDocumentCount(2);
        HashMap<String, String> md = new HashMap<>();

        md.put("k1","v1");
        //store.searchByMetadata(md);
    }
    @Test
    void search() throws IOException {
        store.put(new ByteArrayInputStream("new text".getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
        store.put(new ByteArrayInputStream("new new text 2 text".getBytes()),this.uri2, DocumentStore.DocumentFormat.TXT);
        store.put(new ByteArrayInputStream("new text new new 3 text text 3 te".getBytes()),this.uri3, DocumentStore.DocumentFormat.TXT);
        store.put(new ByteArrayInputStream(this.txt4.getBytes()),this.uri4, DocumentStore.DocumentFormat.TXT);

        doc1 = new DocumentImpl(uri1,"new text",null);
        doc2 = new DocumentImpl(uri2, "new new text 2 text", null);
        doc3 = new DocumentImpl(uri3, "new text new new 3 text text 3 te", null);
        doc4 = new DocumentImpl(uri4, "texttexttexttext", null);
        List<Document> compare = new ArrayList<>();
        compare.add(doc3);
        compare.add(doc2);
        compare.add(doc1);
        //compare.add(doc4);
        store.setMaxDocumentCount(1);
        assertEquals(compare, store.search("text"));

        compare.clear();
        compare.add(doc4);
        assertEquals(compare, store.search("texttexttexttext"));

        compare.clear();
        compare.add(doc3);
        compare.add(doc2);
        compare.add(doc1);

        store.setMaxDocumentCount(2);
        assertEquals(compare, store.search("new"));

    }

    @Test
    void searchByPrefix() throws IOException {
        store.put(new ByteArrayInputStream("new text".getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
        store.put(new ByteArrayInputStream("new new text 2 text te".getBytes()),this.uri2, DocumentStore.DocumentFormat.TXT);
        store.put(new ByteArrayInputStream("new text new new 3 text text 3 te".getBytes()),this.uri3, DocumentStore.DocumentFormat.TXT);
        store.put(new ByteArrayInputStream(this.txt4.getBytes()),this.uri4, DocumentStore.DocumentFormat.TXT);

        doc1 = new DocumentImpl(uri1,"new text",null);
        doc2 = new DocumentImpl(uri2, "new new text 2 text te", null);
        doc3 = new DocumentImpl(uri3, "new text new new 3 text text 3 te", null);
        doc4 = new DocumentImpl(uri4, "texttexttexttext", null);
        List<Document> compare = new ArrayList<>();

        compare.add(doc3);
        compare.add(doc2);
        compare.add(doc1);
        compare.add(doc4);

        store.setMaxDocumentCount(1);
        assertEquals(compare, store.searchByPrefix("te"));

    }

    @Test
    void deleteAll() throws IOException {
        store.setMaxDocumentCount(2);
        txt1="text!";
        txt2="text text";
        txt3="text text text";
        txt4="text text text text";


        store.put(new ByteArrayInputStream(this.txt1.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
        store.put(new ByteArrayInputStream(this.txt2.getBytes()),this.uri2, DocumentStore.DocumentFormat.TXT);
        store.put(new ByteArrayInputStream(this.txt3.getBytes()),this.uri3, DocumentStore.DocumentFormat.TXT);
        store.put(new ByteArrayInputStream(this.txt4.getBytes()),this.uri4, DocumentStore.DocumentFormat.TXT);
        store.deleteAll("text");
        assertNull(store.get(uri2));
        assertNull(store.get(uri3));
        assertNull(store.get(uri4));

    }

    @Test
    void deleteAllWithPrefix() throws IOException {
        store.setMaxDocumentCount(2);
        txt1="text!";
        txt2="text text";
        txt3="text text text";
        txt4="text text text text";


        store.put(new ByteArrayInputStream(this.txt1.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
        store.put(new ByteArrayInputStream(this.txt2.getBytes()),this.uri2, DocumentStore.DocumentFormat.TXT);
        store.put(new ByteArrayInputStream(this.txt3.getBytes()),this.uri3, DocumentStore.DocumentFormat.TXT);
        store.put(new ByteArrayInputStream(this.txt4.getBytes()),this.uri4, DocumentStore.DocumentFormat.TXT);
        store.deleteAllWithPrefix("te");
        assertNull(store.get(uri1));
        assertNull(store.get(uri2));
        assertNull(store.get(uri3));
        assertNull(store.get(uri4));
    }

    @Test
    void searchByMetadata() throws IOException {
        HashMap<String, String> md = new HashMap<>();

        md.put("k1", "v1");
        store.setMetadata(uri1, "k1", "v1");
        store.setMetadata(uri1, "k2", "v2");

        List<Document> compare = new ArrayList<>();
        doc1.setMetadata(md);
        compare.add(doc1);
        store.setMaxDocumentCount(1);
        assertEquals(compare, store.searchByMetadata(md));
        assertEquals(compare, store.searchByKeywordAndMetadata("new", md));
    }

    @Test
    void searchByKeywordAndMetadata() {
    }

    @Test
    void searchByPrefixAndMetadata() {
    }

    @Test
    void deleteAllWithMetadata() {
    }

    @Test
    void deleteAllWithKeywordAndMetadata() {
    }

    @Test
    void deleteAllWithPrefixAndMetadata() {
    }

    @Test
    void undoSetMD() throws URISyntaxException, IOException {
        store= new DocumentStoreImpl(null);
        uri1 = new URI("http://yu.edu/DS/stageDocs/doc1");
        uri2 = new URI("http://yu.edu/DS/stageDocs/doc2");
        uri3 = new URI("http://yu.edu/DS/stageDocs/doc3");
        uri4 = new URI("http://yu.edu/DS/stageDocs/doc4");

        doc1 = new DocumentImpl(uri1,"text",null);
        doc2 = new DocumentImpl(uri2, "texttext", null);
        doc3 = new DocumentImpl(uri3, "texttexttext", null);
        doc4 = new DocumentImpl(uri4, "texttexttexttext", null);

        txt1="1";
        txt2="2";
        txt3="3";
        txt4="4";


        store.put(new ByteArrayInputStream(this.txt1.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
        store.put(new ByteArrayInputStream(this.txt2.getBytes()),this.uri2, DocumentStore.DocumentFormat.TXT);
        store.put(new ByteArrayInputStream(this.txt3.getBytes()),this.uri3, DocumentStore.DocumentFormat.TXT);
        store.put(new ByteArrayInputStream(this.txt4.getBytes()),this.uri4, DocumentStore.DocumentFormat.TXT);

        HashMap<String, String> md1 = new HashMap<>();
        HashMap<String, String> md2 = new HashMap<>();
        HashMap<String, String> md3 = new HashMap<>();
        HashMap<String, String> md4 = new HashMap<>();

        md1.put("k1", "1");
        md2.put("k2", "2");
        md3.put("k3", "3");
        md4.put("k4", "4");

        store.setMetadata(uri1, "k1", "1");
        store.setMetadata(uri2, "k2", "2");
        store.setMetadata(uri3, "k3", "3");
        store.setMetadata(uri4, "k4", "4");

        store.setMaxDocumentCount(1);
        store.undo();
        assertNull(store.getMetadata(uri4,"k4"));
        store.undo();
        assertNull(store.getMetadata(uri3,"k3"));

        assertNotNull(store.getMetadata(uri1,"k1"));
        store.undo(uri1);
        assertNull(store.getMetadata(uri1,"k1"));

    }

    @Test
    void undoPut() throws IOException {
        store.put(new ByteArrayInputStream(this.txt4.getBytes()),this.uri4, DocumentStore.DocumentFormat.TXT);
        store.setMetadata(uri4, "4","4");
        store.put(new ByteArrayInputStream(this.txt4.getBytes()),this.uri4, DocumentStore.DocumentFormat.TXT);
        store.undo();
        assertEquals(store.getMetadata(uri4,"4"), "4");

        store.setMaxDocumentCount(1);
        store.undo(uri1);
        store.undo(uri4);
        store.undo(uri4);
        store.undo(uri4);
        store.undo(uri2);
        store.get(uri3);
        store.undo(uri3);

    }

    @Test
    void singleDeleteUndo() throws IOException {

        store.delete(uri1);
        store.delete(uri2);
        store.delete(uri3);
        store.setMaxDocumentCount(1);
        store.setMetadata(uri4, "4","4");
        store.delete(uri4);

        store.undo();
        store.undo(uri3);
        store.undo();
        assertNull(store.getMetadata(uri4, "4"));
        store.undo(uri1);
        store.undo(uri2);
        store.delete(uri1);
        store.undo();


    }
    @Test
    void deletingMultipleTest() throws URISyntaxException, IOException {
        store= new DocumentStoreImpl(null);
        uri1 = new URI("http://yu.edu/DS/stageDocs/doc1");
        uri2 = new URI("http://yu.edu/DS/stageDocs/doc2");
        uri3 = new URI("http://yu.edu/DS/stageDocs/doc3");
        uri4 = new URI("http://yu.edu/DS/stageDocs/doc4");

        doc1 = new DocumentImpl(uri1,"text",null);
        doc2 = new DocumentImpl(uri2, "text text", null);
        doc3 = new DocumentImpl(uri3, "text text text", null);
        doc4 = new DocumentImpl(uri4, "text text text text", null);

        txt1="text";
        txt2="text text";
        txt3="text text text";
        txt4="text text text text";


        store.put(new ByteArrayInputStream(this.txt1.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
        store.put(new ByteArrayInputStream(this.txt2.getBytes()),this.uri2, DocumentStore.DocumentFormat.TXT);
        store.put(new ByteArrayInputStream(this.txt3.getBytes()),this.uri3, DocumentStore.DocumentFormat.TXT);
        store.put(new ByteArrayInputStream(this.txt4.getBytes()),this.uri4, DocumentStore.DocumentFormat.TXT);
        store.setMaxDocumentCount(1);
        store.deleteAll("text");
        store.undo(uri1);
        store.undo(uri4);
        store.undo();

        //test delete w prefix
        store.deleteAllWithPrefix("te");
        store.undo(uri1);
        assertEquals(store.get(uri1), doc1);
//        store.undo(uri4);
        assertNull(store.get(uri2));
        store.undo();
        assertEquals(store.get(uri2), doc2);
        assertEquals(store.get(uri3), doc3);
        assertEquals(store.get(uri4), doc4);



    }

    @Test
    void multipleDeleteUndo() throws URISyntaxException, IOException {
        DocumentStoreImpl documentStore = new DocumentStoreImpl(null);
        uri1 = new URI("http://yu.edu/DS/stageDocs/doc1");
        uri2 = new URI("http://yu.edu/DS/stageDocs/doc2");
        uri3 = new URI("http://yu.edu/DS/stageDocs/doc3");
        uri4 = new URI("http://yu.edu/DS/stageDocs/doc4");

        doc1 = new DocumentImpl(uri1,"text fi",null);
        doc2 = new DocumentImpl(uri2, "texttext fico", null);
        doc3 = new DocumentImpl(uri3, "textte t", null);
        doc4 = new DocumentImpl(uri4, "text text text ", null);

        txt1="text fi";
        txt2="texttext fico";
        txt3="textte t";
        txt4="text text text ";


        documentStore.put(new ByteArrayInputStream(this.txt1.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
        documentStore.setMetadata(uri1, "k1", "v1");
        documentStore.put(new ByteArrayInputStream(this.txt2.getBytes()),this.uri2, DocumentStore.DocumentFormat.TXT);
        documentStore.put(new ByteArrayInputStream(this.txt3.getBytes()),this.uri3, DocumentStore.DocumentFormat.TXT);
        documentStore.put(new ByteArrayInputStream(this.txt4.getBytes()),this.uri4, DocumentStore.DocumentFormat.TXT);
        documentStore.setMetadata(uri4, "k1", "v1");
        documentStore.setMetadata(uri4, "k2", "v2");

        HashMap<String,String> md = new HashMap<>();
        md.put("k1","v1");
        documentStore.setMaxDocumentCount(1);
        documentStore.deleteAllWithPrefixAndMetadata("te", md);
        assertNull(documentStore.get(uri1));
        assertNull(documentStore.get(uri4));
        assertNotNull(documentStore.get(uri2));
        assertNotNull(documentStore.get(uri3));
        documentStore.undo();


    }
    @Test
    void testUndo() {
    }

    @Test
    void setMaxDocumentCount() {
    }

    @Test
    void setMaxDocumentBytes() {
    }
}