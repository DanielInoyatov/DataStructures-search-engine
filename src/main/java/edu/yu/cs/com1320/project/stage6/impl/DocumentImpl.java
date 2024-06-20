package edu.yu.cs.com1320.project.stage6.impl;
import edu.yu.cs.com1320.project.stage6.Document;

import java.net.URI;
import java.util.*;

public class DocumentImpl implements Document {
    private HashMap<String,String> metaData = new HashMap<>();
    private HashMap<String, Integer> words = new HashMap<>();
    private URI uri;
    private String txt;
    private byte[] binaryData;
    private long lastUseTime = System.nanoTime();

    public DocumentImpl(URI uri, String txt, Map<String, Integer> wordCountMap)
    {
        if(uri==null|| uri.toString().isBlank())
        {
            throw new IllegalArgumentException("uri cannot be null or blank");
        }
        else if(txt==null || txt.isEmpty())
        {
            throw new IllegalArgumentException("text cannot be empty or null");
        }

        if(wordCountMap==null)
        {
            String[] textArr = txt.split(" ");
            for(String wrd: textArr)
            {
                if(this.words.get(wrd)==null)
                {
                    this.words.put(wrd, 1);
                }
                else
                {
                    this.words.put(wrd, this.words.get(wrd)+1);
                }
            }
        }
        else {
            this.words=new HashMap<>(wordCountMap);
        }
        this.uri = uri;
        this.txt=txt;
    }
    public DocumentImpl(URI uri, byte[] binaryData)
    {
        if(uri.toString().isBlank() || uri==null)
        {
            throw new IllegalArgumentException("uri cannot be null or blank");
        }
        else if(binaryData.length ==0 || binaryData==null)
        {
            throw new IllegalArgumentException("binaryData cannot be empty or null");
        }

        this.uri = uri;
        this.binaryData=binaryData;
    }
    private String stripPunctuation(String str)
    {
        String newStr = "";
        char[] keyArr = str.toCharArray();
        for(int i =0; i<keyArr.length;i++)
        {
            if(Character.isDigit(keyArr[i]) || Character.isLetter(keyArr[i]))
            {
                newStr+=keyArr[i];
            }
        }
        return newStr;
    }
    @Override
    public String setMetadataValue(String key, String value)
    {
        if(key == null || key.isBlank())
        {
            throw new IllegalArgumentException("key cannot be blank or null");
        }

        return this.metaData.put(key,value);
    }
    @Override
    public String getMetadataValue(String key)
    {
        if(key == null || key.isBlank())
        {
            throw new IllegalArgumentException("key cannot be blank or null");
        }

        return this.metaData.get(key);
    }
    @Override
    public HashMap<String, String> getMetadata()
    {
        HashMap<String, String> output = new HashMap<>();
        Set<String> keySet = this.metaData.keySet();
        for(String key : keySet)
        {
            output.put(key, this.metaData.get(key));
        }
        return output;
    }

    @Override
    public String getDocumentTxt()
    {
        return this.txt;
    }

    @Override
    public byte[] getDocumentBinaryData()
    {
        return this.binaryData;
    }

    @Override
    public URI getKey()
    {
        return this.uri;
    }

    @Override
    public int hashCode()
    {
        int result = uri.hashCode();
        result = 31 * result + (txt != null ? txt.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(binaryData);
        return Math.abs(result);
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == null ||!(o instanceof DocumentImpl))
        {
            return false;
        }
        else if (o==this)
        {
            return true;
        }
        else if(o.hashCode() == this.hashCode())
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    @Override
    public int wordCount(String word)
    {
        word = stripPunctuation(word);
        if(this.binaryData!=null)
        {
            return 0;
        }

        int output=0;
        HashSet<String> allWords= new HashSet<>(getWords());
        int prefLength= word.length();
        for(String wrd: allWords)
        {
            if(wrd.length()==prefLength)
            {
                if(wrd.equals(word))
                {
                    output=output+ this.words.get(wrd);
                }
            }

        }
        return output;
    }

    @Override
    public void setMetadata(HashMap<String, String> metadata)
    {
        this.metaData = metadata;
    }
    @Override
    public Set<String> getWords()
    {
        return words.keySet();
    }

    @Override
    public long getLastUseTime() {
        return this.lastUseTime;
    }

    @Override
    public void setLastUseTime(long timeInNanoseconds) {
        this.lastUseTime= timeInNanoseconds;

    }

    @Override
    public HashMap<String, Integer> getWordMap() {
        return words;
    }

    @Override
    public void setWordMap(HashMap<String, Integer> wordMap) {
        this.words=wordMap;

    }

    @Override
    public int compareTo(Document o) {
        if(this.lastUseTime<o.getLastUseTime())
        {
            return -1;
        } else if (this.lastUseTime>o.getLastUseTime()) {
            return 1;
        }
        else
            return 0;
    }
}
