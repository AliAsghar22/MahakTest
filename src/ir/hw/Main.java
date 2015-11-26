package ir.hw;


public class Main {

    public static void main(String[] args) {
        Indexer indexer = new Indexer();
        indexer.cleanIndex();
        indexer.makeIndex("index", "data/documents");
//        System.out.println(indexer.search("کمسيون هاي مجلس شوراي اسلامي ايران",30));
        indexer.makeRelevantsHashMap();
        indexer.compute();


    }
}
