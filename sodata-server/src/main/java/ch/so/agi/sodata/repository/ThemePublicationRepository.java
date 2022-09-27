package ch.so.agi.sodata.repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import ch.so.agi.sodata.dto.ThemePublicationDTO;

@Repository
public class ThemePublicationRepository {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private Directory fsIndex;
    private StandardAnalyzer analyzer;
    private QueryParser queryParser;
    private IndexWriter writer;
    //private IndexWriterConfig indexWriterConfig;

    // Muss dem Parsen der Xml-Config passieren, was im CommandLineRunner
    // gemacht wird.
    @PostConstruct
    public void init() throws IOException {
        log.info("Prepare index...");
        
        Path tempDirWithPrefix = Files.createTempDirectory(Paths.get(System.getProperty("java.io.tmpdir")), "sodata_idx");
        log.info("Index folder: " + tempDirWithPrefix);
        
        fsIndex = new NIOFSDirectory(tempDirWithPrefix);
        analyzer = new StandardAnalyzer();
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        //IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        writer = new IndexWriter(fsIndex, indexWriterConfig);
    }
    
    // Öffnen/schliessen müsste eh anders gelöst werden.
    @Deprecated
    public void save(ThemePublicationDTO themePublication) throws IOException {
        log.info("save: " + themePublication.getIdentifier());
        
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(fsIndex, indexWriterConfig);
        
        Document document = new Document();
        document.add(new TextField("id", themePublication.getIdentifier(), Store.YES));
        document.add(new TextField("model", themePublication.getModel(), Store.YES));
        document.add(new TextField("title", themePublication.getTitle(), Store.YES));
        document.add(new TextField("shortdescription", themePublication.getShortDescription(), Store.YES));
        String ownerLuceneString = themePublication.getOwner().getAgencyName();
        if (themePublication.getOwner().getAbbreviation() != null) ownerLuceneString += ", " + themePublication.getOwner().getAbbreviation();
        document.add(new TextField("owner", ownerLuceneString, Store.YES));
        document.add(new TextField("keywords", String.join(", ", themePublication.getKeywords()), Store.YES));
        document.add(new TextField("synonyms", String.join(", ", themePublication.getSynonyms()), Store.YES));
        
        writer.addDocument(document);
        writer.close();
    }
    
    public void saveAll(List<ThemePublicationDTO> themePublicationList) throws IOException {
        for (ThemePublicationDTO themePublication : themePublicationList) {
            Document document = new Document();
            document.add(new TextField("id", themePublication.getIdentifier(), Store.YES));
            document.add(new TextField("model", themePublication.getModel(), Store.YES));
            document.add(new TextField("title", themePublication.getTitle(), Store.YES));
            document.add(new TextField("shortdescription", themePublication.getShortDescription(), Store.YES));
            String ownerLuceneString = themePublication.getOwner().getAgencyName();
            if (themePublication.getOwner().getAbbreviation() != null) ownerLuceneString += ", " + themePublication.getOwner().getAbbreviation();
            document.add(new TextField("owner", ownerLuceneString, Store.YES));
            if (themePublication.getKeywords() != null) document.add(new TextField("keywords", String.join(", ", themePublication.getKeywords()), Store.YES));
            if (themePublication.getSynonyms() != null) document.add(new TextField("synonyms", String.join(", ", themePublication.getSynonyms()), Store.YES));
            
            writer.addDocument(document);
        }
        IndexWriter.DocStats docStats = writer.getDocStats();
        writer.close();
        
        log.info("{} files indexed.", docStats.numDocs);
    }
    
    public List<ThemePublicationDTO> findByQuery(String searchTerms) {
        return null;
    }
}
