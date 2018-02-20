package file.search.service.search;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PreDestroy;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
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
import org.apache.lucene.search.TotalHitCountCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import file.search.service.rest.exception.FileSearcherException;
import file.search.service.rest.exception.InvalidFileSearchQueryException;

@Repository("FileSearcher")
public class FileSearcher {
	
	private Directory indexDirectory;
	private QueryParser queryParser;
	private IndexWriter writer;

	private final static Logger logger = Logger.getLogger("FileSearcher");
	private static StandardAnalyzer analyzer = new StandardAnalyzer();
	private ArrayList<File> queue = new ArrayList<File>();
     
	/**
	 * Method that starts the Lucene Service and sanity checks the index
	 * 
	 * @throws IOException 
	 */
	public FileSearcher(@Value("${lucene.index.location}") String indexLocation) throws FileSearcherException, IOException {
		
		// The following block creates new index in the directory after deleting the older index during startup.
		
		Path indexFolder = Paths.get(indexLocation + "_Index");
		Files.walk(indexFolder)
	    .sorted(Comparator.reverseOrder())
	    .map(Path::toFile)
	    .forEach(File::delete);
	    FSDirectory dir = FSDirectory.open(indexFolder);
	    IndexWriterConfig config = new IndexWriterConfig(analyzer);
	    writer = new IndexWriter(dir, config);
	    indexFileOrDirectory(indexLocation);
	    writer.close();
	    
	    try {
			Path index = Paths.get(indexLocation);
			indexDirectory = FSDirectory.open(indexFolder);
			CharArraySet stopWordsOverride = new CharArraySet(Collections.emptySet(), true);
			Analyzer analyzer = new StandardAnalyzer(stopWordsOverride);
			queryParser = new QueryParser("dummyfield", analyzer); 
			logger.info("Connected to Index at: " + indexLocation);
			IndexReader reader = DirectoryReader.open(indexDirectory);
			logger.info("Number of docs: "+reader.numDocs());
			if(reader.numDocs()>0){
				logger.info("Getting fields for a sample document in the index. . .");
				reader.document(1).getFields();
				List<IndexableField> fields = reader.document(1).getFields();
				for(int i=0; i<fields.size();i++){
					logger.info(i + 1 + ") " + fields.get(i).name() + ":"+ fields.get(i).stringValue());
				}
			} else {
				logger.warning("Index is empty!!");
			}
			reader.close();
		} catch (IOException ioe) {
			logger.log(Level.SEVERE, "Could not open Lucene Index at: "+indexLocation+ " : "+ioe.getMessage());
			throw new FileSearcherException("Could not open Lucene Index at: "+indexLocation+ " : "+ioe.getMessage());
		}
	}
	
	/**
	 * Closes Lucene resources
	 */
	@PreDestroy
	private void close() {
		try {
			indexDirectory.close();
			logger.info("Lucene Index closed");
		}
		catch (IOException ioe) {
			logger.warning("Issue closing Lucene Index: "+ioe.getMessage());
		}
	}

	/**
	 * Search Lucene Index for records matching querystring
	 * @param querystring - valid Lucene query string
	 * @param numRecords - number of requested records 
	 * @param showAvailable - check for number of matching available records 
	 * @return Top Lucene query results as a Result object
	 * @throws FileSearcherException 
	 * @throws InvalidFileSearchQueryException 
	 */
	public Result searchIndex(String querystring, int numRecords, boolean showAvailable) throws FileSearcherException, InvalidFileSearchQueryException {
		IndexReader reader = null;
		IndexSearcher indexSearcher = null;
		Query query;
		TopDocs documents;
		TotalHitCountCollector collector = null;
		try {
			reader = DirectoryReader.open(indexDirectory);
			indexSearcher = new IndexSearcher(reader);
			query = queryParser.parse(querystring);
			logger.info("'" + querystring + "' ==> '" + query.toString() + "'");
			if(showAvailable){
				collector = new TotalHitCountCollector();
				indexSearcher.search(query, collector);
			}
			documents = indexSearcher.search(query, numRecords);
			List<Map<String,String>> mapList = new LinkedList<Map<String,String>>();
			for (ScoreDoc scoreDoc : documents.scoreDocs) {
				Document document = indexSearcher.doc(scoreDoc.doc);
				Map<String,String> docMap = new HashMap<String,String>();
				List<IndexableField> fields = document.getFields();
				for(IndexableField field : fields){
					docMap.put(field.name(), field.stringValue());
				}
				mapList.add(docMap);
			}
			Result result = new Result(mapList, 
					mapList.size(), 
					collector==null?(mapList.size() < numRecords?mapList.size():-1)
							:collector.getTotalHits());
			return result;
		} catch (ParseException pe) {
			throw new InvalidFileSearchQueryException(pe.getMessage());
		} catch (Exception e) {
			throw new FileSearcherException(e.getMessage());
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			}
			catch (IOException ioe) {
				logger.warning("Could not close IndexReader: "+ioe.getMessage()); 
			}
		}
	}
	
	/**
	   * Indexes a file or directory
	   * @param fileName the name of a text file or a folder we wish to add to the index
	   * @throws java.io.IOException when exception
	   */
	  private void indexFileOrDirectory(String fileName) throws IOException {
	    //===================================================
	    //gets the list of files in a folder (if user has submitted
	    //the name of a folder) or gets a single file name (is user
	    //has submitted only the file name) 
	    //===================================================
	    addFiles(new File(fileName));
	    
	    int originalNumDocs = writer.numDocs();
	    for (File f : queue) {
	      FileReader fr = null;
	      try {
	        Document doc = new Document();

	        //===================================================
	        // add contents of file
	        //===================================================
	        fr = new FileReader(f);
	        doc.add(new TextField("contents", fr));
	        doc.add(new StringField("path", f.getPath(), Field.Store.YES));
	        doc.add(new StringField("filename", f.getName(), Field.Store.YES));

	        writer.addDocument(doc);
	        logger.info("Added: " + f);
	      } catch (Exception e) {
	    	  logger.warning("Could not add: " + f);
	      } finally {
	        fr.close();
	      }
	    }
	    
	    int newNumDocs = writer.numDocs();
	    logger.info("");
	    logger.info("************************");
	    logger.info((newNumDocs - originalNumDocs) + " documents added.");
	    logger.info("************************");
	    queue.clear();
	  }

	  private void addFiles(File file) {
	    if (!file.exists()) {
	    	logger.info(file + " does not exist.");
	    }
	    if (file.isDirectory()) {
	      for (File f : file.listFiles()) {
	        addFiles(f);
	      }
	    } else {
	      String filename = file.getName().toLowerCase();
	      //===================================================
	      // Only index text files
	      //===================================================
	      if (filename.endsWith(".htm") || filename.endsWith(".html") || 
	              filename.endsWith(".xml") || filename.endsWith(".txt")) {
	        queue.add(file);
	      } else {
	    	  logger.info("Skipped " + filename);
	      }
	    }
	  }
	
}
