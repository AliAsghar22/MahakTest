package ir.hw;


public class Main {

    public static void main(String[] args) {
        Indexer indexer = new Indexer();
        indexer.cleanIndex();
        indexer.makeIndex("index", "data/documents");
        indexer.makeRelevantsHashMap();
        indexer.compute();


    }
}
