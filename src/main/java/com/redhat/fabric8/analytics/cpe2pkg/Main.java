package com.redhat.fabric8.analytics.cpe2pkg;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;

public class Main {

    public static void main(String[] args) throws IOException, ParseException {

        if (args.length != 1) {
            System.err.println("missing query string");
            System.exit(1);
        }

        String pkgFile = "packages";
        if (System.getProperty("pkgFile") != null) {
            pkgFile = System.getProperty("pkgFile");
        }

        Analyzer analyzer = createSearchingAnalyzer();
        Directory index = new RAMDirectory();

        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter w = new IndexWriter(index, config);

        List<String> packages = Files.readAllLines(Paths.get(pkgFile));
        for (String pkg : packages) {
            String[] gav = pkg.split(",");
            if (gav.length < 3) {
                // weird one, skipping...
                continue;
            }
            addDoc(w, gav[0], gav[1]);
        }

        w.commit();
        w.close();

        String querystr = args[0];

        Query q = new QueryParser("title", analyzer).parse(querystr);

        IndexReader reader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);
        TopDocs docs = searcher.search(q, 10);
        ScoreDoc[] hits = docs.scoreDocs;

        System.out.println("Found " + hits.length + " hits.");
        for (int i = 0; i < hits.length; ++i) {
            int docId = hits[i].doc;
            Document d = searcher.doc(docId);
            System.out.println((hits[i].score) + " " + d.get("vendor") + ":" + d.get("product"));
        }
    }

    private static void addDoc(IndexWriter w, String vendor, String product) throws IOException {
        Document doc = new Document();
        doc.add(new TextField("vendor", vendor, Field.Store.YES));
        doc.add(new TextField("product", product, Field.Store.YES));
        w.addDocument(doc);
    }

    private static Analyzer createSearchingAnalyzer() {
        final Map<String, Analyzer> fieldAnalyzers = new HashMap<>();
        fieldAnalyzers.put("id", new KeywordAnalyzer());
        final SearchFieldAnalyzer productFieldAnalyzer = new SearchFieldAnalyzer(Version.LUCENE_7_0_1);
        final SearchFieldAnalyzer vendorFieldAnalyzer = new SearchFieldAnalyzer(Version.LUCENE_7_0_1);
        fieldAnalyzers.put("product", productFieldAnalyzer);
        fieldAnalyzers.put("vendor", vendorFieldAnalyzer);

        return new PerFieldAnalyzerWrapper(new KeywordAnalyzer(), fieldAnalyzers);
    }
}
