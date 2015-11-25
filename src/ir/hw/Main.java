package ir.hw;

import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        Indexer indexer = new Indexer();
        indexer.cleanIndex();
        indexer.makeIndex("index", "data/documents");
        ArrayList<String> names = indexer.search("معترض مجلس ششم", 10);
        System.out.println(names);
    }
}
