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

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StoredField;
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

import ch.so.agi.sodata.dto.DataproductDTO;

@Repository
public class LuceneDataproductRepository {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private Directory fsIndex;
    private StandardAnalyzer analyzer;
    private QueryParser queryParser;
    private IndexWriter writer;

    // Index-Initialisierung muss vor dem Parsen der Konfig-Datei(en) 
    // gemacht werden. Diese wird im CommandLineRunner geparsed.
    @PostConstruct
    public void init() throws IOException {
        log.info("Prepare index...");
        
        Path tempDirWithPrefix = Files.createTempDirectory(Paths.get(System.getProperty("java.io.tmpdir")), "sodata_idx_dataproduct");
        log.info("Index folder: " + tempDirWithPrefix);
        
        fsIndex = new NIOFSDirectory(tempDirWithPrefix);
        analyzer = new StandardAnalyzer();
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        writer = new IndexWriter(fsIndex, indexWriterConfig);
    }
    
    public void saveAll(List<DataproductDTO> dataproductList) throws IOException {
        for (DataproductDTO dataproduct : dataproductList) {
            Document document = new Document();
            document.add(new TextField("ident", dataproduct.getIdent(), Store.YES));
            document.add(new TextField("title", dataproduct.getTitle(), Store.YES));
            document.add(new TextField("abstract", dataproduct.getTheAbstract(), Store.YES));
            document.add(new StoredField("visibility", Boolean.toString(dataproduct.isVisibility())));
            document.add(new StoredField("opacity", String.valueOf(dataproduct.getOpacity())));
            if (dataproduct.getThemeTitle() != null) document.add(new TextField("themetitle", dataproduct.getThemeTitle(), Store.YES));

            writer.addDocument(document);

            if (dataproduct.getSublayers() != null) {
                for (DataproductDTO sublayer : dataproduct.getSublayers()) {
                    Document sublayerDocument = new Document();
                    sublayerDocument.add(new TextField("ident", sublayer.getIdent(), Store.YES));
                    sublayerDocument.add(new TextField("title", sublayer.getTitle(), Store.YES));
                    sublayerDocument.add(new TextField("abstract", sublayer.getTheAbstract(), Store.YES));
                    sublayerDocument.add(new StoredField("visibility", Boolean.toString(sublayer.isVisibility())));
                    sublayerDocument.add(new StoredField("opacity", String.valueOf(sublayer.getOpacity())));
                    sublayerDocument.add(new StoredField("parentIdent", sublayer.getParentIdent()));
                    sublayerDocument.add(new StoredField("parentTitle", sublayer.getParentTitle()));
                    if (sublayer.getThemeTitle() != null) sublayerDocument.add(new TextField("themetitle", sublayer.getThemeTitle(), Store.YES));
                    writer.addDocument(sublayerDocument);
                }
            }
        }

        IndexWriter.DocStats docStats = writer.getDocStats();
        writer.close();
        
        log.info("{} files indexed.", docStats.numDocs);
    }

    /**
     * Search Lucene Index for records matching querystring
     * @param querystring - human written query string from e.g. a search form
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
                luceneQueryString += "("
                        + "ident:*" + token + "*^100 OR "
                        + "title:*" + token + "*^10 OR "
                        + "themetitle:*" + token + "*^10 OR "
                        + "abstract:*" + token + "*"
                        + ")"; 
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
