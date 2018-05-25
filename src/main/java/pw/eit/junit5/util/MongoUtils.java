package pw.eit.junit5.util;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.awaitility.Awaitility;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class MongoUtils
{
    private MongoDatabase mongoDatabase;
    private long attemptDuration;

    /**
     * Only to be used with unit testing.  Uses an embedded mongo instance located at
     * localhost:${com.gci.junit5.mongo.port}.  The port property is populated using RandomPortInitailizer.
     *
     * @param databaseName
     */
    public MongoUtils(String databaseName)
    {
        this(10000, "localhost", RandomPortInitailizer.mongoPort, databaseName);
    }

    public MongoUtils(long attemptDuration, String host, Integer port, String databaseName)
    {
        this(attemptDuration, host, port, databaseName, MongoClientOptions.builder()
                                                                          .build());
    }

    public MongoUtils(long attemptDuration, String host, Integer port, String databaseName, MongoClientOptions mongoClientOptions)
    {
        this.attemptDuration = attemptDuration;
        this.mongoDatabase = getMongoDatabase(host, port, databaseName, mongoClientOptions);
    }

    private MongoDatabase getMongoDatabase(String host, Integer port, String databaseName, MongoClientOptions mongoClientOptions)
    {
        ServerAddress serverAddress = new ServerAddress(host, port);
        MongoClient mongoClient = new MongoClient(serverAddress, mongoClientOptions);
        MongoDatabase mongoDatabase = mongoClient.getDatabase(databaseName);
        return mongoDatabase;
    }

    private Long countMongoDocuments(String collectionName, Bson bson)
    {
        Long count = mongoDatabase.getCollection(collectionName)
                                  .count(bson);
        return count;
    }

    private Document getOneDocumentFromMongo(String collectionName, Bson bson)
    {
        Document document = mongoDatabase.getCollection(collectionName)
                                         .find(bson)
                                         .first();
        return document;
    }

    private FindIterable<Document> getMongoDocuments(String collectionName, Bson bson)
    {
        FindIterable<Document> documents = mongoDatabase.getCollection(collectionName)
                                                        .find(bson);
        return documents;
    }

    public Boolean wereNDocumentsAddedInMongo(String collectionName, Bson bson, long numberAdded)
    {
        Callable<Boolean> wereNDocumentsAddedInMongo = () -> countMongoDocuments(collectionName, bson) == numberAdded;
        return performVerification(attemptDuration, wereNDocumentsAddedInMongo);
    }

    private Boolean performVerification(Long attemptDuration, Callable<Boolean> verification)
    {
        try
        {
            Awaitility.await()
                      .atMost(attemptDuration, TimeUnit.MILLISECONDS)
                      .until(verification);
        } catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void write(String collectionName, JSONObject jsonObject)
    {
        Boolean collectionExists = mongoDatabase.listCollectionNames()
                                                .into(new HashSet<>())
                                                .contains(collectionName);
        if (!collectionExists)
        {
            mongoDatabase.createCollection(collectionName);
        }
        MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);
        Document document = Document.parse(jsonObject.toString());
        collection.insertOne(document);
    }
}
