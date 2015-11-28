package ir.hw;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.fa.PersianAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
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
import org.apache.lucene.util.Version;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

/**
 * Created by Taghizadeh on 11/25/2015.
 */
public class Indexer {

    public HashMap<String, Map<String, Integer>> relevant;

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
            for (File f : new File(documentsPath).listFiles()) {
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

    public ArrayList<String> search(String q, int count) {
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


    public void makeRelevantsHashMap() {
        //key = queryID
        //value = a hashMap of relevant news id * relevancy
        relevant = new HashMap<>();
        FileInputStream inputStream;
        HSSFWorkbook workbook;
        Sheet sheet;
        Iterator rowIterator;
        Iterator cellIterator;
        Row row = null;

        try {
            inputStream = new FileInputStream("qrels.xls");
            workbook = new HSSFWorkbook(inputStream);
            sheet = workbook.getSheetAt(0);
            rowIterator = sheet.iterator();
            while (rowIterator.hasNext()) {
                row = (Row) rowIterator.next();
                cellIterator = row.cellIterator();
                Cell queryId = (Cell) cellIterator.next();
                Cell newsId = (Cell) cellIterator.next();
                Cell relavancy = (Cell) cellIterator.next();
                String query = String.valueOf((int) queryId.getNumericCellValue());
                String news = String.valueOf((int) newsId.getNumericCellValue());
                int relev = (int) relavancy.getNumericCellValue();
                if (relevant.get(query) == null) {
                    Map<String, Integer> map = new HashMap<>();
                    map.put(news, relev);
                    relevant.put(query, map);
                } else {
                    Map<String, Integer> map = relevant.get(query);
                    map.put(news, relev);
                    relevant.put(query, map);
                }

            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void compute() {
        // the hashMap of relevancy should be created first
        // the queries are rode from exel file and searched one by one and then the results are tested for each query

        FileInputStream inputStream;
        HSSFWorkbook workbook;
        Sheet sheet;
        Iterator rowIterator;
        Iterator cellIterator;
        Row row = null;
        HSSFWorkbook results =new HSSFWorkbook();
        HSSFSheet sheet1=results.createSheet("resultsset");

        try {
            //reading exel file
            inputStream = new FileInputStream("queries.xls");
            workbook = new HSSFWorkbook(inputStream);
            sheet = workbook.getSheetAt(0);
            rowIterator = sheet.iterator();
            int rowcounter=-1;
            Row row1=sheet1.createRow(++rowcounter);
            int column=-1;
            Cell cell=row1.createCell(++column);
            cell.setCellValue("query id");
            cell=row1.createCell(++column);
            cell.setCellValue("p@5");
            cell=row1.createCell(++column);
            cell.setCellValue("p@10");
            cell=row1.createCell(++column);
            cell.setCellValue("p@15");
            cell=row1.createCell(++column);
            cell.setCellValue("Whole Precision");
            cell=row1.createCell(++column);
            cell.setCellValue("recall");
            cell=row1.createCell(++column);
            cell.setCellValue("f measure");
            cell=row1.createCell(++column);
            cell.setCellValue("MAP");
            int qid = 0;
            float whole_f_measure=0;
            float wholeMap=0;
            while (rowIterator.hasNext()) {

                qid++;
                row = (Row) rowIterator.next();
                cellIterator = row.cellIterator();
                Cell main = (Cell) cellIterator.next();//main = query
                String qeryId = main.getStringCellValue();
               // System.out.println("queryID " + qid);
                Map<String, Integer> map = relevant.get(String.valueOf(qid)); // a map of relevant news numbers
              //  System.out.println(map);
                ArrayList<String> names = search(main.getStringCellValue(), 20);// the results of search


                // beginning test of results
                // percision at 5
                ArrayList<String> pat5 = new ArrayList<>(names.subList(0, 5));
                float percsiontat5 = 0;
                for (String s : pat5) {
                    if (map.containsKey(s)) {
                        percsiontat5 += map.get(s);
                    }
                }
                // percision at 10
                ArrayList<String> pat10 = new ArrayList<>(names.subList(0, 10));
                float percsiontat10 = 0;
                for (String s : pat10) {
                    if (map.containsKey(s)) {
                        percsiontat10 += map.get(s);
                    }
                }
                // percision at 15
                float percsiontat15 = 0;
                ArrayList<String> pat15 = new ArrayList<>(names.subList(0, 15));
                for (String s : pat15) {
                    if (map.containsKey(s)) {
                        percsiontat15 += map.get(s);
                    }
                }
                float wholepersc = 0;
                for (String s : names) {
                    if (map.containsKey(s)) {
                        wholepersc += map.get(s);
                    }
                }
                percsiontat10 /= 20;
                percsiontat15 /= 30;
                percsiontat5 /= 10;
                wholepersc /= 40;
//                System.out.println("p@5:"+percsiontat5+"---p@10:"+percsiontat10+"---p@15"+percsiontat15);

                float recall = 0;
                for (String s : names) {
                    if (map.containsKey(s)) {
                        recall += map.get(s);
                    }
                }
                int num2 = 0;
                for (String s : map.keySet())
                    num2 += map.get(s);

                recall /= num2;
                float fmeasure=0;
                if (wholepersc + recall!=0)
                    fmeasure = 2*recall*wholepersc/(wholepersc + recall);

                float makhraj = map.size();

                float ratio = 0;
                float soorat = 0;
                for(int i = 0; i < 20; i++){
                    soorat = 0;
                    ArrayList<String> temp = new ArrayList<>(names.subList(0, i+1));
                    for(String s: temp){
                        if (map.containsKey(s)) {
                            soorat += map.get(s);
                        }
                    }
                    if (i!=0)
                    ratio += soorat/(i*2);
                }
                column=-1;
                float mapMeasure=0;
                if(makhraj!=0)
                    mapMeasure = ratio/makhraj;

                whole_f_measure+=fmeasure;
                wholeMap+=mapMeasure;

               // System.out.println(fmeasure);
                row1=sheet1.createRow(++rowcounter);
                cell=row1.createCell(++column);
                cell.setCellValue(qid);
                cell=row1.createCell(++column);
                cell.setCellValue(percsiontat5);
                cell=row1.createCell(++column);
                cell.setCellValue(percsiontat10);
                cell=row1.createCell(++column);
                cell.setCellValue(percsiontat15);
                cell=row1.createCell(++column);
                cell.setCellValue(wholepersc);
                cell=row1.createCell(++column);
                cell.setCellValue(recall);
                cell=row1.createCell(++column);
                cell.setCellValue(fmeasure);
                cell=row1.createCell(++column);
                cell.setCellValue(mapMeasure);
                try (FileOutputStream outputStream = new FileOutputStream("res.xls")) {
                    results.write(outputStream);
                }

            }
            System.out.println("fMeasure AVG :"+whole_f_measure/216);
            System.out.println("MAP AVG : "+wholeMap/216);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }


}
