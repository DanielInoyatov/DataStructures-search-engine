package edu.yu.cs.com1320.project.stage6.impl;

import edu.yu.cs.com1320.project.impl.BTreeImpl;
import edu.yu.cs.com1320.project.impl.MinHeapImpl;
import edu.yu.cs.com1320.project.impl.StackImpl;
import edu.yu.cs.com1320.project.impl.TrieImpl;
import edu.yu.cs.com1320.project.stage6.Document;
import edu.yu.cs.com1320.project.stage6.DocumentStore;
import edu.yu.cs.com1320.project.undo.CommandSet;
import edu.yu.cs.com1320.project.undo.GenericCommand;
import edu.yu.cs.com1320.project.undo.Undoable;

import javax.print.Doc;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;

public class DocumentStoreImpl implements DocumentStore {
    private class URIHolder implements Comparable<URIHolder>{
        URI uri;
        long lastUseTime = System.nanoTime();
        private URIHolder(URI uri)
        {
            this.uri = uri;
        }
        private URI getKey()
        {
            return this.uri;
        }
        private long getLastUseTime()
        {
            return lastUseTime;
        }
        private void setLastUseTime(long useTime)
        {
            this.lastUseTime=useTime;
        }
        @Override
        public int compareTo(URIHolder o) {
            if(this.lastUseTime<o.getLastUseTime())
                return -1;
            else if (this.lastUseTime>o.getLastUseTime())
                return 1;
            else
                return 0;
        }
        @Override
        public boolean equals(Object o)
        {
            if (o == null ||!(o instanceof URIHolder))
            {
                return false;
            }
            URIHolder equals = (URIHolder) o;
            if(this.uri.equals(equals.getKey()))
                return true;
            else
                return false;
        }
        @Override
        public int hashCode() {
            return this.uri.hashCode();
        }

    }

    private HashMap<URI,URIHolder> uriHoldersInMemory = new HashMap<>();
    private BTreeImpl<URI, Document> store;
    private StackImpl<Undoable> commandStack = new StackImpl<>();
    private TrieImpl<URIHolder> docsTrie = new TrieImpl<>();
    private TrieImpl<URIHolder> docTrieMetadata = new TrieImpl<>();
    private MinHeapImpl<URIHolder> memory = new MinHeapImpl<>();
    private int docLimit = Integer.MAX_VALUE;
    private int byteLimit =Integer.MAX_VALUE;


    public DocumentStoreImpl(File baseDir) {
        this.store = new BTreeImpl<>();
        this.store.setPersistenceManager(new DocumentPersistenceManager(baseDir));
    }
    public DocumentStoreImpl() {
        this.store = new BTreeImpl<>();
        this.store.setPersistenceManager(new DocumentPersistenceManager(null));
    }
    private int numBytes() {
        int output = 0;
        for(URI val: uriHoldersInMemory.keySet()) {
            Document cur;
            try {
                cur = store.get(val);
            }
            catch (RuntimeException e) {
                return 0;
            }
            if(cur.getDocumentBinaryData()!=null)
                output+=cur.getDocumentBinaryData().length;
            else
                output+=cur.getDocumentTxt().getBytes().length;

        }
        return output;
    }
    private void setMax(URI uri) //max memory?
    {
        if(uriHoldersInMemory.get(uri)==null) {
            uriHoldersInMemory.put(uri, new URIHolder(uri));
        }
        uriHoldersInMemory.get(uri).setLastUseTime(System.nanoTime());
        try{
            memory.reHeapify(uriHoldersInMemory.get(uri));
        }
        catch (NoSuchElementException e){
            memory.insert(uriHoldersInMemory.get(uri));
            this.setMaxDocumentCount(this.docLimit);
            this.setMaxDocumentBytes(this.byteLimit);
        }
    }
    private void insertHolderIntoMemory(URIHolder holder) {
        memory.insert(holder);
        this.setMaxDocumentCount(this.docLimit);
        this.setMaxDocumentBytes(this.byteLimit);
        this.addToUriHoldersInMemory(holder);
    }
    private void fillTempStack(StackImpl<Document> temp, Document oldVal)
    {
        while(memory.peek()!=null && !store.get(memory.peek().getKey()).equals(oldVal))
        {
            URIHolder top =  memory.remove();
            temp.push(store.get(top.getKey()));
            uriHoldersInMemory.remove(top.getKey());
        }
    }
    private URIHolder addToMemory(Document doc) {
        doc.setLastUseTime(System.nanoTime());
        URIHolder tempHolder;
        if (uriHoldersInMemory.get(doc.getKey())==null)
            tempHolder =(new URIHolder(doc.getKey()));
        else
            tempHolder =uriHoldersInMemory.get(doc.getKey());
        memory.insert(tempHolder);
        return tempHolder;
    }
    private void addValueToStore(URIHolder holder,URI key, Document value) {
        this.setMaxDocumentCount(this.docLimit);
        this.setMaxDocumentBytes(this.byteLimit);
        uriHoldersInMemory.putIfAbsent(holder.getKey(), holder);
        this.store.put(key, value);
        uriHoldersInMemory.get(key).setLastUseTime(System.nanoTime());
        memory.reHeapify(uriHoldersInMemory.get(key));
        this.setMaxDocumentBytes(byteLimit);
        this.setMaxDocumentCount(docLimit);
    }
    private void addToUriHoldersInMemory(URIHolder holder) {
        uriHoldersInMemory.putIfAbsent(holder.getKey(), holder);
        holder.setLastUseTime(System.nanoTime());
        try{
            memory.reHeapify(holder);
        }
        catch (NoSuchElementException e){
            memory.insert(holder);
            this.setMaxDocumentCount(this.docLimit);
            this.setMaxDocumentBytes(this.byteLimit);
        }
    }
    private void addHolderToTempStack(StackImpl<Document> temp) {
        URIHolder holder;
        if(uriHoldersInMemory.get(temp.peek().getKey())==null)
            holder=(new URIHolder(temp.pop().getKey()));
        else
            holder=(uriHoldersInMemory.get(temp.pop().getKey()));

        memory.insert(holder);
        this.setMaxDocumentCount(this.docLimit);
        this.setMaxDocumentBytes(this.byteLimit);
        uriHoldersInMemory.putIfAbsent(holder.getKey(), holder);
    }
    private Document getWithoutMemory(URI url) {
        try{
            Document output = this.store.get(url);
            if(output!=null)
                uriHoldersInMemory.putIfAbsent(output.getKey(), new URIHolder(output.getKey()));
            return output;
        }
        catch (RuntimeException e) {
            return null;
        }
    }
    private void createUriHolder(Document doc)
    {
        URIHolder holder;
        if(uriHoldersInMemory.get(doc.getKey())==null)
        {
            holder=(new URIHolder(doc.getKey()));
        }
        else
        {
            holder= uriHoldersInMemory.get(doc.getKey());
        }
        insertHolderIntoMemory(holder);
        holder.setLastUseTime(System.nanoTime());
        memory.reHeapify(holder);
    }
    private String stripPunctuation(String str) {
        String newStr = "";
        char[] keyArr = str.toCharArray();
        for (int i = 0; i < keyArr.length; i++) {
            if (Character.isDigit(keyArr[i]) || Character.isLetter(keyArr[i])) {
                newStr += keyArr[i];
            }
        }
        return newStr;
    }
    private List<URIHolder> getSortedUriHolderListForKeywordSearch(String keyword)
    {
        return docsTrie.getSorted(keyword, (URIHolder o1, URIHolder o2) -> {
            if (store.get(o1.getKey()).wordCount(keyword) < store.get(o2.getKey()).wordCount(keyword))
                return 1;
            else if (store.get(o1.getKey()).wordCount(keyword) > store.get(o2.getKey()).wordCount(keyword))
                return -1;
            else
                return 0;
        });
    }
    private List<Document> searchWithoutMemory(String keyword) {
        List<URIHolder> uriHolders = getSortedUriHolderListForKeywordSearch(keyword);
        List<Document> output = new ArrayList<>();
        for(URIHolder d: uriHolders) {
            output.add(store.get(d.getKey()));
        }
        return output;
    }

    private int prefixCounter(String pref, Document document) {
        int output = 0;
        String[] allWords = document.getDocumentTxt().split(" ");
        int prefLength = pref.length();
        for (String word : allWords) {
            word = stripPunctuation(word);
            if (word.length() > prefLength && word.substring(0, pref.length()).equals(pref)) {
                output++;
            } else if (word.length() == prefLength) {
                if (word.equals(pref)) {
                    output++;
                }
            }

        }
        return output;
    }
    private void addToSeachOutput(URIHolder d, List<Document> output) {
        output.add(store.get(d.getKey()));
        updateMemoryForSearchedDocument(d);
    }
    private void updateMemoryForSearchedDocument(URIHolder d) {
        d.setLastUseTime(System.nanoTime());
        try{
            memory.reHeapify(d);
        }
        catch (NoSuchElementException e){
            memory.insert(d);
            uriHoldersInMemory.put(d.getKey(),d);
            this.setMaxDocumentCount(this.docLimit);
            this.setMaxDocumentBytes(this.byteLimit);
        }
    }
    private List<URIHolder> getSortedUriHolderListForPrefixSearch(String prefix) {
        return  docsTrie.getAllWithPrefixSorted(prefix, (o1, o2) -> {
            if (prefixCounter(prefix, store.get(o1.getKey())) < prefixCounter(prefix, store.get(o2.getKey())))
                return 1;
            else if (prefixCounter(prefix, store.get(o1.getKey())) > prefixCounter(prefix, store.get(o2.getKey())))
                return -1;
            else
                return 0;
        });
    }
    private List<Document> searchByPrefixWithoutMemory(String keywordPrefix) {
        List<URIHolder> uriHolders = getSortedUriHolderListForPrefixSearch(keywordPrefix);
        List<Document> output = new ArrayList<>();
        for(URIHolder d: uriHolders) {
            output.add(store.get(d.getKey()));
        }
        return output;
    }
    private Set<URI> creatingdDeletedSet(Set<URIHolder> docSet) {
        Set<URI> output = new HashSet<>();
        commandStack.push(new CommandSet<>());
        for (URIHolder doc : docSet) {
            output.add(doc.getKey());
            this.delete(doc.getKey());
        }
        return output;
    }
    private List<Document> searchByMetadataWithoutMemory(Map<String, String> keysValues) {
        Set<URIHolder> output = new HashSet<>();
        Set<String> keys = keysValues.keySet();
        boolean bool = true;
        for (String key : keys) {
            Set<URIHolder> docsThatContainKeyInMetadata = docTrieMetadata.get(key);
            for (URIHolder doc : docsThatContainKeyInMetadata) {
                for (String keyInKeys : keys) {
                    if (!store.get(doc.getKey()).getMetadata().containsKey(keyInKeys) || !(store.get(doc.getKey()).getMetadata().get(keyInKeys).equals(keysValues.get(keyInKeys)))) {
                        bool = false;
                        break;
                    }
                }
                if (bool)
                    output.add(doc);
                bool = true;
            }
            break;
        }
        List<Document> finalOutput = new ArrayList<>();
        for (URIHolder uri: output) {
            finalOutput.add(store.get(uri.getKey()));
        }
        return finalOutput;
    }
    private List<Document> filterMatches(List<Document> listA, List<Document> listB)
    {
        List<Document> output = new ArrayList<>();
        for (Document doc : listA) {
            if (listB.contains(doc)) {
                output.add(doc);
                URIHolder holder = new URIHolder(doc.getKey());
                this.addToUriHoldersInMemory(holder);
                memory.reHeapify(memory.peek());
            }
        }
        return output;
    }
    private Set<URI> getSetOfDeletedURIs(List<Document> whatToDelete, Map<String, String> keysValues) {
        Set<URI> output = new HashSet<>();
        for (String key : keysValues.keySet()) {
            commandStack.push(new CommandSet<>());
            for (Document doc : whatToDelete) {
                docTrieMetadata.delete(key, uriHoldersInMemory.get(doc.getKey()));
                output.add(doc.getKey());
                this.delete(doc.getKey());
            }
        }
        return output;
    }
    public String setMetadata(URI uri, String key, String value)throws IOException {
        if (uri == null) {
            throw new IllegalArgumentException("uri cannot be null or blank");
        } else if (uri.toString().isBlank()) {
            throw new IllegalArgumentException("uri cannot be null or blank");
        } else if (this.store.get(uri) == null) {
            throw new IllegalArgumentException("There is no document stored in that uri");
        } else if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("key cannot be null or blank");
        } else {
            Set<URIHolder> oldDocs = docTrieMetadata.get(key);
            Iterator<URIHolder> itr = oldDocs.iterator();
            URIHolder oldDoc = null;
            while (itr.hasNext()) {
                URIHolder cur = itr.next();
                try{
                    if (this.store.get(cur.getKey()).equals(this.getWithoutMemory(uri))) {
                        oldDoc = cur;
                        this.memory.insert(oldDoc);
                        this.uriHoldersInMemory.put(oldDoc.getKey(), oldDoc);
                    }
                    this.store.moveToDisk(this.store.get(cur.getKey()).getKey());
                }
                catch (NullPointerException e){

                }
            }
            this.setMax(uri);
            String oldVal = this.getMetadata(uri, key);
            docTrieMetadata.put(key,uriHoldersInMemory.get(uri));
            URIHolder finalOldDoc = oldDoc;
            commandStack.push(new GenericCommand<>(uri, (url) -> {
                store.get(url);
                this.getWithoutMemory(url).setMetadataValue(key, oldVal);
                this.setMax(url);
                docTrieMetadata.put(key, finalOldDoc);

            }));

            String output = this.store.get(uri).getMetadataValue(key);
            this.store.get(uri).setMetadataValue(key, value);
            this.setMax(uri);
            return output;
        }
    }

    public String getMetadata(URI uri, String key)  throws IOException{
        if (uri == null) {
            throw new IllegalArgumentException("uri cannot be null or blank");
        } else if (uri.toString().isBlank()) {
            throw new IllegalArgumentException("uri cannot be null or blank");
        } else if (this.store.get(uri) == null) {
            throw new IllegalArgumentException("There is no document stored in that uri");
        } else if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("key cannot be null or blank");
        } else {
            this.setMax(uri);
            return this.store.get(uri).getMetadataValue(key);
        }
    }
    private void deleteDataFromTries(Document doc)
    {
        for (String key : doc.getMetadata().keySet()) {
            docTrieMetadata.delete(key, uriHoldersInMemory.get(doc.getKey()));
        }
        for (String str : doc.getWords()) {
            docsTrie.delete(str, uriHoldersInMemory.get(doc.getKey()));
        }
    }
    private void updateValueInStore(Document oldVal, Document value, URI uri)
    {
        deleteDataFromTries(oldVal);
        StackImpl<Document> temp = new StackImpl<>();
        this.fillTempStack(temp, oldVal);
        if(memory.peek()!=null)
            uriHoldersInMemory.remove(memory.remove().getKey());
        URIHolder holder;
        if(uriHoldersInMemory.get(value.getKey())!=null)
            holder=uriHoldersInMemory.get(value.getKey());
        else
            holder=new URIHolder(value.getKey());

        this.insertHolderIntoMemory(holder);
        while(temp.size()!=0) {
            this.addHolderToTempStack(temp);
        }
        this.store.put(uri, value);
        uriHoldersInMemory.get(uri).setLastUseTime(System.nanoTime());
        memory.reHeapify(uriHoldersInMemory.get(uri));
        this.setMaxDocumentBytes(byteLimit);
        this.setMaxDocumentCount(docLimit);
    }
    private void putIntoTries(Iterable<String> trieWords, Iterable<String> trieMd, Document value)
    {
        for (String word : trieWords) {
            docsTrie.put(word, uriHoldersInMemory.get(value.getKey()));
        }

        for (String key : trieMd) {
            docTrieMetadata.put(key, uriHoldersInMemory.get(value.getKey()));
        }
    }
    private void removeDocumentFromMemory(StackImpl<Document> stack, URI uri) {
        try{
            while(true) {
                URIHolder top = memory.remove();
                stack.push(store.get(top.getKey()));
                uriHoldersInMemory.remove(top.getKey());


            }
        }
        catch (RuntimeException e) {
            while(stack.size()!=0) {
                if(!(stack.peek().getKey().equals(uri)))
                    this.addHolderToTempStack(stack);
                else
                    stack.pop();

            }
        }
    }
    private void putUndoSetup(Document oldVal, Document value, URI uri, URI url) {
        store.get(url);
        this.store.put(uri, oldVal);
        if(oldVal!=null)
            this.putIntoTries(oldVal.getWords(), oldVal.getMetadata().keySet(),oldVal);
        else
            uriHoldersInMemory.remove(url);
        deleteDataFromTries(value);
        StackImpl<Document> delTempStack = new StackImpl<>();
        this.removeDocumentFromMemory(delTempStack, uri);
    }
    public int put(InputStream input, URI uri, DocumentFormat format) throws IOException {
        if (uri == null || uri.toString().isBlank() || format == null)
            throw new IllegalArgumentException("Uri cannot be null or blank, format cannot be null");
        if (input == null && this.getWithoutMemory(uri) == null)
            return 0;
        else if (input == null) {
            int output = this.getWithoutMemory(uri).hashCode();
            this.delete(uri);
            return output;
        }
        int output = this.getWithoutMemory(uri) == null ? 0 : this.getWithoutMemory(uri).hashCode();
        byte[] byteArray = input.readAllBytes();
        ByteArrayInputStream byteArrayInputStr = new ByteArrayInputStream(byteArray);
        if (format == DocumentFormat.TXT) {
            String txtStr = "";
            for (int i = 0; i < byteArray.length; i++) {
                txtStr += (char) byteArrayInputStr.read();
            }
            DocumentImpl value = new DocumentImpl(uri, txtStr, null);
            Document oldVal = this.store.get(uri);
            if(byteArray.length>byteLimit)
                throw new IllegalArgumentException("cannot input have more bytes than lmit");
            if(value.getDocumentTxt()!=null && value.getDocumentTxt().getBytes().length>byteLimit)
                throw new IllegalArgumentException("cannot have more bytes than limit");
            if(oldVal!=null)
                updateValueInStore(oldVal, value, uri);
            else {
                URIHolder tempHolder=this.addToMemory(value);
                this.addValueToStore(tempHolder,uri,value);
            }

            String[] textArr = txtStr.split(" ");
            List<String> text = Arrays.asList(textArr);
            HashMap<String, String> valMetaData = value.getMetadata();
            this.putIntoTries(text,valMetaData.keySet(),value);


            commandStack.push(new GenericCommand<>(uri, (url) -> {
                this.putUndoSetup( oldVal,  value,  uri,  url);
                if(oldVal!=null)
                {
                    URIHolder tempHolder=this.addToMemory(oldVal);
                    this.addToUriHoldersInMemory(tempHolder);
                    this.setMaxDocumentCount(this.docLimit);
                    this.setMaxDocumentBytes(this.byteLimit);
                }
                else
                {
                    uriHoldersInMemory.remove(url);
                }

            }));
        } else if (format == DocumentFormat.BINARY) {
            DocumentImpl value = new DocumentImpl(uri, byteArray);
            HashMap<String, String> valMetaData = value.getMetadata();
            Set<String> keys = valMetaData.keySet();
            for (String key : keys) {
                docTrieMetadata.put(key, uriHoldersInMemory.get(value.getKey()));
            }
            Document oldVal = this.store.get(uri);
            if (value.getDocumentBinaryData().length>byteLimit) {
                throw new IllegalArgumentException("cant insert doc with hgher bytes than limit");
            }
            if(byteArray.length>byteLimit)
                throw new IllegalArgumentException("cannot input more bytes than limit");
            if(oldVal!=null)
            {
                this.updateValueInStore(oldVal,value,uri);
            }
            else
            {
                URIHolder holder = this.addToMemory(value);
                this.addValueToStore(holder,uri,value);
            }
            this.putIntoTries(new HashSet<String>(),valMetaData.keySet(),value );

            commandStack.push(new GenericCommand<>(uri, (url) -> {
                this.putUndoSetup( oldVal,  value,  uri,  url);
                if(oldVal!=null) {
                    oldVal.setLastUseTime(System.nanoTime());
                    URIHolder holder = addToMemory(oldVal);
                    this.addToUriHoldersInMemory(holder);
                    this.setMaxDocumentCount(this.docLimit);
                    this.setMaxDocumentBytes(this.byteLimit);
                }
                else {
                    uriHoldersInMemory.remove(url);
                }
            }));
        }
        return output;
    }




    public Document get(URI url)throws IOException {

        try{
            Document output = this.store.get(url);
            if(output!=null)
            {
                URIHolder holder = new URIHolder(output.getKey());
                this.addToUriHoldersInMemory(holder);
            }

            return output;
        }
        catch (RuntimeException e) {
            return null;
        }
    }
    private void deleteUndoSetUp(URI url, Document val) {
        this.store.put(url, val);
        if(val!=null) {
            val.setLastUseTime(System.nanoTime());
            if(val.getDocumentTxt()!=null && val.getDocumentTxt().getBytes().length>byteLimit)
                throw new IllegalArgumentException("cant insert doc with hgher bytes than limit");
            else if (val.getDocumentBinaryData()!=null &&val.getDocumentBinaryData().length>byteLimit)
                throw new IllegalArgumentException("cant insert doc with hgher bytes than limit");
            this.createUriHolder(val);
            this.setMaxDocumentCount(docLimit);
            this.setMaxDocumentBytes(byteLimit);
            this.putIntoTries(val.getWords() , val.getMetadata().keySet(), val);
        }
    }
    public boolean delete(URI url) {
        if (url == null || this.getWithoutMemory(url) == null) {
            return false;
        } else {
            Document val = this.store.get(url);
            uriHoldersInMemory.get(val.getKey()).setLastUseTime(System.nanoTime());
            memory.insert(uriHoldersInMemory.get(val.getKey()));
            this.deleteDataFromTries(val);
            removeDocFromMemory( new StackImpl<Document>() ,url);
            try {
                CommandSet<URI> cmdSet = (CommandSet<URI>) commandStack.peek();
                if(cmdSet!=null) {
                    cmdSet.addCommand(new GenericCommand<>(url, (uri) -> {
                        this.deleteUndoSetUp(url, val);
                    }));
                }
            } catch (ClassCastException e) {
                commandStack.push(new GenericCommand<>(url, (uri) ->
                {
                    this.deleteUndoSetUp(url, val);
                }));
            }
            this.store.put(url, null);
            return true;
        }
    }

    private void removeDocFromMemory(StackImpl<Document> delTempStack,URI url)
    {
        try{
            while(true) {
                URIHolder top = memory.remove();
                delTempStack.push(store.get(top.getKey()));
                uriHoldersInMemory.remove(top.getKey());
            }
        }
        catch (RuntimeException e) {
            while(delTempStack.size()!=0)
            {
                if(!(delTempStack.peek().getKey().equals(url)))
                {
                    URIHolder holder;
                    if(uriHoldersInMemory.get(delTempStack.peek().getKey())==null)
                        holder=(new URIHolder(delTempStack.pop().getKey()));
                    else
                        holder= uriHoldersInMemory.get(delTempStack.pop().getKey());
                    memory.insert(holder);
                    this.setMaxDocumentCount(this.docLimit);
                    this.setMaxDocumentBytes(this.byteLimit);
                    uriHoldersInMemory.putIfAbsent(holder.getKey(), holder);
                }
                else
                    delTempStack.pop();

            }
        }
    }

    @Override
    public List<Document> search(String keyword) throws IOException{
        List<URIHolder> uriHolders = getSortedUriHolderListForKeywordSearch(keyword);
        List<Document> output = new ArrayList<>();
        for(URIHolder d: uriHolders)
        {
            if(store.get(d.getKey())!=null)
            {
                this.addToSeachOutput(d,output);
            }
        }
        return output;
    }


    @Override
    public List<Document> searchByPrefix(String keywordPrefix) throws IOException{
        List<URIHolder> uriHolders = getSortedUriHolderListForPrefixSearch(keywordPrefix);
        List<Document> output = new ArrayList<>();
        for(URIHolder d: uriHolders)
        {
            this.addToSeachOutput(d, output);
        }
        return output;
    }
    @Override
    public Set<URI> deleteAll(String keyword) {

        Set<URIHolder> docSet = docsTrie.deleteAll(keyword);
        return creatingdDeletedSet(docSet);

    }
    @Override
    public Set<URI> deleteAllWithPrefix(String keywordPrefix) {

        Set<URIHolder> docSet = docsTrie.deleteAllWithPrefix(keywordPrefix);
        return creatingdDeletedSet(docSet);
    }

    @Override
    public List<Document> searchByMetadata(Map<String, String> keysValues)throws IOException {
        Set<URIHolder> output = new HashSet<>();
        Set<String> keys = keysValues.keySet();
        boolean bool = true;
        for (String key : keys) {
            Set<URIHolder> docsThatContainKeyInMetadata = docTrieMetadata.get(key);
            for (URIHolder doc : docsThatContainKeyInMetadata) {
                for (String keyInKeys : keys) {
                    if (!store.get(doc.getKey()).getMetadata().containsKey(keyInKeys) || !(store.get(doc.getKey()).getMetadata().get(keyInKeys).equals(keysValues.get(keyInKeys)))) {
                        bool = false;
                        break;
                    }
                }
                if (bool == true)
                    output.add(doc);
                bool = true;
            }

            break;
        }

        List<Document> finalOutput = new ArrayList<>();
        for (URIHolder uri: output)
        {
            updateMemoryForSearchedDocument(uri);
            finalOutput.add(store.get(uri.getKey()));
        }
        return finalOutput;
    }



    @Override
    public List<Document> searchByKeywordAndMetadata(String keyword, Map<String, String> keysValues) throws IOException {
        List<Document> docsKeywords = this.searchWithoutMemory(keyword);
        List<Document> docsMetaData = this.searchByMetadataWithoutMemory(keysValues);
        return filterMatches(docsKeywords,docsMetaData);
    }

    @Override
    public List<Document> searchByPrefixAndMetadata(String keywordPrefix, Map<String, String> keysValues) throws IOException{
        List<Document> docsKeywords = this.searchByPrefixWithoutMemory(keywordPrefix);
        List<Document> docsMetaData = this.searchByMetadataWithoutMemory(keysValues);
        return filterMatches(docsKeywords,docsMetaData);
    }

    @Override
    public Set<URI> deleteAllWithMetadata(Map<String, String> keysValues)  throws IOException{
        Set<Document> output = new HashSet<>();
        Set<String> keys = keysValues.keySet();
        boolean bool = true;
        for (String key : keys) {
            Set<URIHolder> docsThatContainKeyInMetadata = docTrieMetadata.get(key);
            Set<Document> docsToDelete = new HashSet<>();
            for (URIHolder doc : docsThatContainKeyInMetadata) {
                for (String keyInKeys : keys) {
                    if (!(keysValues.get(keyInKeys).equals(store.get(doc.getKey()).getMetadataValue(keyInKeys)))) {
                        bool = false;
                        break;
                    }
                }
                if (bool == true) {

                    docsToDelete.add(store.get(doc.getKey()));
                    output.add(store.get(doc.getKey()));
                }

            }
            commandStack.push(new CommandSet<>());
            for (Document docToDelete : docsToDelete) {
                for (String keyInKeys : keys) {
                    docTrieMetadata.delete(keyInKeys, uriHoldersInMemory.get(docToDelete.getKey()));
                    this.delete(docToDelete.getKey());
                }
            }
        }
        Set<URI> finalOutput = new HashSet<>();
        for (Document doc : output) {
            finalOutput.add(doc.getKey());
        }
        return finalOutput;
    }
    @Override
    public Set<URI> deleteAllWithKeywordAndMetadata(String keyword, Map<String, String> keysValues)  throws IOException{

        List<Document> whatToDelete = searchByKeywordAndMetadata(keyword, keysValues);
        return getSetOfDeletedURIs(whatToDelete,keysValues);
    }

    @Override
    public Set<URI> deleteAllWithPrefixAndMetadata(String keywordPrefix, Map<String, String> keysValues) throws IOException {

        List<Document> whatToDelete = searchByPrefixAndMetadata(keywordPrefix, keysValues);
        return getSetOfDeletedURIs(whatToDelete,keysValues);
    }

    public void undo() throws IllegalStateException {
        if (commandStack.size() == 0) {
            throw new IllegalStateException("nothing left to be undone");
        }

        try {
            CommandSet<URI> cmdSet = (CommandSet<URI>) commandStack.peek();
            cmdSet = (CommandSet<URI>) commandStack.pop();
            if (cmdSet.size() == 0) {
                cmdSet = (CommandSet<URI>) commandStack.pop();
            }
            cmdSet.undoAll();
        } catch (ClassCastException e) {
            GenericCommand<URI> cmd = (GenericCommand<URI>) commandStack.pop();
            cmd.undo();
        }
    }

    public void undo(URI url) throws IllegalStateException {
        StackImpl<Undoable> tempStack = new StackImpl<>();
        boolean hasUri = false;
        for (int i = this.commandStack.size() - 1; i >= 0 && hasUri == false; i--) {
            Undoable cur = this.commandStack.pop();
            try {
                CommandSet<URI> cmdSet = (CommandSet<URI>) cur;
                if (cmdSet.containsTarget(url)) {
                    cmdSet.undo(url);
                    if (cmdSet.size() != 0) {
                        commandStack.push(cmdSet);
                    }
                    hasUri = true;
                } else {
                    tempStack.push(cmdSet);
                }
            } catch (ClassCastException e) {
                GenericCommand<URI> curCmd = (GenericCommand<URI>) cur;
                if (curCmd.getTarget().equals(url)) {
                    curCmd.undo();
                    hasUri = true;
                } else {
                    tempStack.push(curCmd);
                }

            }
        }
        while (tempStack.size() != 0) {
            this.commandStack.push(tempStack.pop());
        }
        if (!hasUri) {
            throw new IllegalStateException("uri not in command stack");
        }

    }

    @Override
    public void setMaxDocumentCount(int limit) {
        if(limit<1)
            throw new IllegalArgumentException("limit cannot be less than 1");

        this.docLimit=limit;
        while(this.uriHoldersInMemory.size()>limit)
        {
            try
            {
                if (memory.peek()!=null)
                {
                    URIHolder top = memory.remove();
                    this.store.moveToDisk(top.getKey());
                    uriHoldersInMemory.remove(top.getKey());
                }

            }
            catch (IOException e)
            {
                throw new RuntimeException();
            }
        }

    }

    @Override
    public void setMaxDocumentBytes(int limit) {
        if(limit<1)
            throw new IllegalArgumentException("limit cannot be less than 1");
        this.byteLimit=limit;
        while(this.numBytes()>limit &&memory.peek()!=null)
        {
            try
            {
                if (memory.peek()!=null)
                {
                    URIHolder top = memory.remove();
                    this.store.moveToDisk(top.getKey());
                    uriHoldersInMemory.remove(top.getKey());
                }
            }
            catch (IOException e) {
                throw new RuntimeException();
            }
        }
    }
}

