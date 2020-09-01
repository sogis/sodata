package ch.so.agi.sodata.server;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.so.agi.sodata.server.AppConfig.CfgDataset;
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
    
    @GetMapping("/cfgdatasets")
    public List<Dataset> cfgDatasets() {
        return config.getDatasets();
    }
    
    @GetMapping("/datasets")
    public List<Dataset> datasets() {
        return config.getDatasets();
    }
    
    
//    @GetMapping("/datasets")
//    public List<Dataset> datasets() {
//        List<Dataset> datasets = new ArrayList<Dataset>();
//        for (CfgDataset cfgDataset : config.getDatasets()) {
//            Dataset dataset = new Dataset();
////            dataset.id = cfgDataset.getId();
//            dataset.setId(cfgDataset.getId());
////            dataset.version = cfgDataset.getVersion();
////            dataset.owner = cfgDataset.getOwner();
////            dataset.epsgCode = cfgDataset.getEpsgCode();
////            dataset.resolutionScope = cfgDataset.getResolutionScope();
////            dataset.publishingDate = cfgDataset.getPublishingDate();
////            dataset.lastEditingDate = cfgDataset.getLastEditingDate();
////            dataset.title = cfgDataset.getTitle();
////            dataset.shortDescription = cfgDataset.getShortDescription();
////            dataset.keywords = cfgDataset.getKeywords();
////            dataset.servicer = cfgDataset.getServicer();
////            dataset.technicalContact = cfgDataset.getTechnicalContact();
////            dataset.furtherInformation = cfgDataset.getFurtherInformation();
////            dataset.furtherMetadata = cfgDataset.getFurtherMetadata();
////            dataset.knownWMS = cfgDataset.getKnownWMS();
//            
//            ArrayList<String> foo = new ArrayList<String>();
//            foo.add("xtf");
//            foo.add("shp");
//            dataset.setFiles(foo.toArray(new String[0])); 
//            
//            datasets.add(dataset);
//        }
//        return datasets;
//    }
    
}
