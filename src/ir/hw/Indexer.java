package ir.hw;

import org.apache.lucene.analysis.fa.PersianAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;

/**
 * Created by Taghizadeh on 11/25/2015.
 */
public class Indexer {

    public void cleanIndex() {
        new File("index").delete();
    }

    public void makeIndex(String indexPath, String documentsPath) {
        PersianAnalyzer analyzer = new PersianAnalyzer();
        IndexWriterConfig cfg = new IndexWriterConfig(analyzer);
        cfg.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        IndexWriter writer = null;
        Directory indexDir;
        try {
            indexDir = FSDirectory.open(new File(indexPath).toPath());
            writer = new IndexWriter(indexDir, cfg);
            for(File f : new File(documentsPath).listFiles()){
                Document doc = new Document();
                String main = new String(Files.readAllBytes(f.toPath()));
                doc.add(new TextField("text", main, org.apache.lucene.document.Field.Store.YES));
                doc.add(new StringField("name", f.getName().replace("out", "").replace(".txt", ""), Field.Store.YES));
                writer.addDocument(doc);
            }

            writer.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<String> search(String q, int count){
        try {
            ArrayList<String> names = new ArrayList<>();
            IndexReader rdr = DirectoryReader.open(FSDirectory.open(new File("index").toPath()));
            IndexSearcher is = new IndexSearcher(rdr);
            QueryParser parser = new QueryParser("text", new PersianAnalyzer());
            Query query = parser.parse(q);
            TopDocs hits = is.search(query, count);
            for (ScoreDoc scoreDoc : hits.scoreDocs) {
                names.add(is.doc(scoreDoc.doc).get("name"));
            }
            return names;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }




}
