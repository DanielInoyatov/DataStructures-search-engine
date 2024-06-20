package edu.yu.cs.com1320.project.stage6.impl;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import edu.yu.cs.com1320.project.stage6.Document;
import edu.yu.cs.com1320.project.stage6.PersistenceManager;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

public class DocumentPersistenceManager implements PersistenceManager<URI,Document>{

    private File path;

    public DocumentPersistenceManager(File baseDir)
    {
        if(baseDir==null)
            this.path=new File(System.getProperty("user.dir"));
        else
            this.path=baseDir;
    }
    private class Serializer implements JsonSerializer<Document>
    {
        Gson gson = new Gson();
        @Override
        public JsonElement serialize(Document document, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject result = new JsonObject();

            String md =gson.toJson(document.getMetadata());
            result.add("metaData",  new JsonPrimitive(md));
            String wrds =gson.toJson(document.getWordMap());
            result.add("words", new JsonPrimitive(wrds));
            result.add("uri", new JsonPrimitive(document.getKey().toString()));

            if(document.getDocumentTxt()!=null)
            {
                result.add("txt", new JsonPrimitive(document.getDocumentTxt()));
                result.add("binaryData", null);
            }
            else
            {
                result.add("txt", null);
                byte[] bytes = document.getDocumentBinaryData();
                JsonArray jsonBytes =  new JsonArray(bytes.length);
                for(int i=0;i<bytes.length;i++){
                    jsonBytes.add(bytes[i]);
                }
                result.add("binaryData", jsonBytes);
            }
            return result;
        }
    }

    private class Deserializer implements JsonDeserializer<Document> {
        Gson gson = new Gson();

        @Override
        public Document deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            String md = gson.fromJson(jsonObject.get("metaData"), String.class);

            HashMap<String, String> metaData=gson.fromJson(md, HashMap.class);


            String wordsStr = gson.fromJson(jsonObject.get("words"), String.class);
            Type tx = new TypeToken<HashMap<String, Integer>>(){}.getType();
            HashMap<String, Integer> words = gson.fromJson(wordsStr, tx);

            String uri = jsonObject.get("uri").getAsString();

            String txt;
            try{
                txt =jsonObject.get("txt").getAsString();
            }
            catch (NullPointerException e )
            {
                txt = null;
            }


            JsonArray JSONArray = jsonObject.getAsJsonArray("binaryData");
            byte[] binaryData;
            try
            {
                binaryData= new byte[JSONArray.size()];
                for (int i = 0; i < JSONArray.size(); i++) {
                    binaryData[i] = JSONArray.get(i).getAsByte();
                }
            }
            catch (NullPointerException e)
            {
                binaryData = null;
            }


            if (txt != null) {
                try {
                    DocumentImpl output = new DocumentImpl(new URI(uri), txt, words);
                    output.setMetadata(metaData);
                    return output;
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            } else {
                try {
                    DocumentImpl output = new DocumentImpl(new URI(uri), binaryData);
                    output.setMetadata(metaData);
                    return output;
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Override
    public void serialize(URI key, Document val) throws IOException {
        String[] slashSplit = key.toString().split("/");
        String docName = slashSplit[slashSplit.length-1];
        String dir = key.toString().substring(7,key.toString().length()-docName.length());

        File creatingDir = new File(this.path +File.separator+dir);
        creatingDir.mkdirs();
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeHierarchyAdapter(Document.class, new Serializer());
        Gson gson = gsonBuilder.create();
        String json = gson.toJson(val);

        File file = new File(creatingDir, docName + ".json");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(json);
        }
    }

    @Override
    public Document deserialize(URI key) throws IOException {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeHierarchyAdapter(Document.class, new Deserializer());
        Gson gson = gsonBuilder.create();
        Document output;
        FileReader reader = new FileReader(this.path + File.separator + key.toString().substring(7) + ".json");
        output = gson.fromJson(reader, Document.class);
        reader.close();

        try{
            this.delete(new URI(key.toString().substring(7)+".json"));
        }
        catch (URISyntaxException e)
        {
            throw new RuntimeException();
        }

        return output;
    }

    @Override
    public boolean delete(URI key) throws IOException {
        File fileToDelete =  new File(this.path +"/"+ key.toString());

        boolean output=fileToDelete.delete();
        String[] a1=this.path.toString().split("\\\\");
        String[] a2 = key.toString().split("/");

        String[] dirs = new String[a1.length+a2.length];
        for(int i=0; i< a1.length;i++)
        {
            if(i<a1.length)
                dirs[i]=a1[i];
        }
        for (int i = 0; i < a2.length; i++) {

            dirs[a1.length+ i] = a2[i];
        }
        String currentDir = "";
        for(int i = 0;i< dirs.length-1;i++)
        {
            currentDir+=dirs[i]+"/";
        }
        currentDir=currentDir.substring(0,currentDir.length()-1);
        fileToDelete =  new File(currentDir);
        try{
            while(fileToDelete.list()==null ||fileToDelete.list().length==0)
            {
                fileToDelete.delete();
                dirs=currentDir.split("/");
                currentDir="";
                for(int i = 0;i< dirs.length-1;i++)
                {
                    currentDir+=dirs[i]+"/";
                }
                currentDir=currentDir.substring(0,currentDir.length()-1);
                fileToDelete =  new File(currentDir);
            }
        }
        catch (StringIndexOutOfBoundsException ignored)
        {

        }

        return output;

    }
}
