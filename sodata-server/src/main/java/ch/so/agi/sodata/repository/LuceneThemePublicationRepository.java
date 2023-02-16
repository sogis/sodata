package ch.so.agi.sodata.repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import ch.so.agi.sodata.dto.ThemePublicationDTO;

@Repository
public class LuceneThemePublicationRepository {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private Directory fsIndex;
    //private StandardAnalyzer analyzer;
    // Debugging: https://util.unicode.org/UnicodeJsps/breaks.jsp
    // https://www.baeldung.com/lucene-analyzers
    // Auch die Synonyme scheinen zu greifen.
    private WhitespaceAnalyzer analyzer;
    private QueryParser queryParser;
    private IndexWriter writer;
    //private IndexWriterConfig indexWriterConfig;

    // Index-Initialisierung muss vor dem Parsen der XML-Konfig-Datei 
    // gemacht werden. Diese wird im CommandLineRunner geparsed.
    @PostConstruct
    public void init() throws IOException {
        log.info("Prepare index...");
        
        Path tempDirWithPrefix = Files.createTempDirectory(Paths.get(System.getProperty("java.io.tmpdir")), "sodata_idx");
        log.info("Index folder: " + tempDirWithPrefix);
        
        fsIndex = new NIOFSDirectory(tempDirWithPrefix);
        //analyzer = new StandardAnalyzer();
        analyzer = new WhitespaceAnalyzer();
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        //IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        writer = new IndexWriter(fsIndex, indexWriterConfig);
    }
    
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
            log.debug("Write Lucene index: {}", themePublication.getIdentifier());
            Document document = new Document();
            document.add(new TextField("id", themePublication.getIdentifier().toLowerCase(), Store.YES));
            if(themePublication.getModel() != null) document.add(new TextField("model", themePublication.getModel().toLowerCase(), Store.YES));
            document.add(new TextField("title", themePublication.getTitle().toLowerCase(), Store.YES));
            document.add(new TextField("shortdescription", themePublication.getShortDescription().toLowerCase(), Store.YES));
            String ownerLuceneString = themePublication.getOwner().getAgencyName().toLowerCase();
            if (themePublication.getOwner().getAbbreviation() != null) ownerLuceneString += ", " + themePublication.getOwner().getAbbreviation().toLowerCase();
            document.add(new TextField("owner", ownerLuceneString, Store.YES));
            if (themePublication.getKeywords() != null) document.add(new TextField("keywords", String.join(", ", themePublication.getKeywords()).toLowerCase(), Store.YES));
            if (themePublication.getSynonyms() != null) document.add(new TextField("synonyms", String.join(", ", themePublication.getSynonyms()).toLowerCase(), Store.YES));
            
            writer.addDocument(document);
        }
        IndexWriter.DocStats docStats = writer.getDocStats();
        writer.close();
        
        log.info("{} files indexed.", docStats.numDocs);
    }
    
    /**
     * Search Lucene Index for records matching querystring
     * @param querystring - human written query string from e.g. a search form. Must be lower case.
     * @param numRecords - number of requested records 
     * @return Lucene query results as a List of Maps object
     * @throws LuceneSearcherException 
     * @throws InvalidLuceneQueryException 
     */
    public List<Map<String, String>> findByQuery(String searchTerms, int numRecords) throws LuceneSearcherException, InvalidLuceneQueryException {
        IndexReader reader = null;
        IndexSearcher indexSearcher = null;
        Query query;
        TopDocs documents;
        
        try {
            reader = DirectoryReader.open(fsIndex);
            indexSearcher = new IndexSearcher(reader);
            queryParser = new QueryParser("title", analyzer); // 'title' is default field if we don't prefix search string
            queryParser.setAllowLeadingWildcard(true); 

            String luceneQueryString = "";
            String[] splitedQuery = searchTerms.split("\\s+");
            for (int i=0; i<splitedQuery.length; i++) {
                String token = QueryParser.escape(splitedQuery[i]);
                // Das Feld, welches bestimmend sein soll (also in der Suche zuoberst gelistet), bekommt
                // einen sehr hohen Boost. Wobei wir im GUI wieder alphabetisch sortieren. Es sorgt aber
                // auch dafür, dass Objekte gefunden werden, die wir für passender halten.                
                luceneQueryString += "("
                        + "id:*" + token + "*^100 OR "
                        + "model:*" + token + "* OR "
                        + "title:*" + token + "*^10 OR "
                        + "shortdescription:*" + token + "* OR "
                        + "owner:*" + token + "* OR "
                        + "keywords:*" + token + "* OR "
                        + "synonyms:*" + token + "*";
                luceneQueryString += ")";
                if (i<splitedQuery.length-1) {
                    luceneQueryString += " AND ";
                }
            }
            
            query = queryParser.parse(luceneQueryString);
            log.debug("'" + luceneQueryString + "' ==> '" + query.toString() + "'");
            
            documents = indexSearcher.search(query, numRecords);
            List<Map<String, String>> mapList = new LinkedList<Map<String, String>>();
            for (ScoreDoc scoreDoc : documents.scoreDocs) {
                Document document = indexSearcher.doc(scoreDoc.doc);
                Map<String, String> docMap = new HashMap<String, String>();
                List<IndexableField> fields = document.getFields();
                for (IndexableField field : fields) {
                    docMap.put(field.name(), field.stringValue());
                }
                mapList.add(docMap);
            }
            
            return mapList;
            
        } catch (ParseException e) {
            e.printStackTrace();
            throw new InvalidLuceneQueryException(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            throw new LuceneSearcherException(e.getMessage());
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ioe) {
                log.warn("Could not close IndexReader: " + ioe.getMessage());
            }
        }
    }
}
