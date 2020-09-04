package ch.so.agi.sodata.server;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.document.Field.Store;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import ch.so.agi.sodata.shared.Dataset;


@RestController
public class MainController {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AppConfig config;
    
    Directory memoryIndex;
    StandardAnalyzer analyzer;
    
    @PostConstruct
    public void init() throws IOException {
        log.info("** execute once only");
        
        memoryIndex = new MMapDirectory(Paths.get(System.getProperty("java.io.tmpdir")));
        analyzer = new StandardAnalyzer();
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        IndexWriter writter = new IndexWriter(memoryIndex, indexWriterConfig);
        Document document = new Document();
         
        document.add(new TextField("title", "Ich bin der Titel.", Store.YES));
        document.add(new TextField("body", "Some hello world", Store.YES));
         
        writter.addDocument(document);
        writter.close();
    }
    
    @GetMapping("/ping")
    public ResponseEntity<String> ping() throws ParseException, IOException {
        for (Dataset dataset : config.getDatasets()) {
            log.info(dataset.getId());
        }
        
        Query query = new QueryParser("body", analyzer).parse("world");
        
        IndexReader indexReader = DirectoryReader.open(memoryIndex);
        IndexSearcher searcher = new IndexSearcher(indexReader);
        TopDocs topDocs = searcher.search(query, 10);
        List<Document> documents = new ArrayList<>();
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            documents.add(searcher.doc(scoreDoc.doc));
        }

        log.info("******"+documents.get(0).toString());
        
        log.info("sodata");
        return new ResponseEntity<String>("sodata", HttpStatus.OK);
    }
   
    @GetMapping("/datasets")
    public List<Dataset> datasets() {
        return config.getDatasets();
    }    
    
//    @GetMapping("/datasets/grouped")
//    public Map<String, List<Dataset>> groupedDatasets() {
//            Map<String, List<Dataset>> datasetListGrouped = config.getDatasets().stream().collect(Collectors.groupingBy(w -> w.getProvider()));
//            return datasetListGrouped;
//    }    
    
    @GetMapping("/dataset/{id}/format/{format}") 
    public RedirectView datset(@PathVariable String id, @PathVariable String format) {
        log.info(id);
        log.info(format);
        
        RedirectView redirectView = new RedirectView();
        // TODO: Im wahren Leben steckt die URL im Konfig, da sie unterschiedlich sein kann.
        redirectView.setUrl("https://s3.eu-central-1.amazonaws.com/ch.so.agi.geodata/"+id+"_"+format+".zip");
        return redirectView;
    }
}
