import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

public class LuceneTest {

	public static void main(String[] args) {
		try {
			// Specify the analyzer for tokenizing text.
			// The same analyzer should be used for indexing and searching
			StandardAnalyzer analyzer = new StandardAnalyzer();

			// Code to create the index
			Directory index = new RAMDirectory();

			IndexWriterConfig config = new IndexWriterConfig(analyzer);

			IndexWriter w = new IndexWriter(index, config);

			DocumentIndexer indexer = new DocumentIndexer(w);

			w = indexer.getIndexWriter();
			w.close();

			// Text to search
			String querystr = "Is CF mucus abnormal?";

			// The \"title\" arg specifies the default field to use when no
			// field is explicitly specified in the query
			Query q = new MultiFieldQueryParser(DocumentIndexer.DOCUMENT_INFO_TYPE, analyzer).parse(querystr);
			// Query q = new QueryParser("TI", analyzer).parse(querystr);

			// Searching code
			int hitsPerPage = 1500;
			IndexReader reader = DirectoryReader.open(index);
			IndexSearcher searcher = new IndexSearcher(reader);
			TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage);
			searcher.search(q, collector);
			ScoreDoc[] hits = collector.topDocs().scoreDocs;

			// Code to display the results of search
			System.out.println("Found " + hits.length + " hits.");
			for (int i = 0; i < hits.length; ++i) {
				int docId = hits[i].doc;
				Document d = searcher.doc(docId);
				System.out.println((i + 1) + ". " + d.get("RN") + "\t" + d.get("TI"));
			}

			// reader can only be closed when there is no need to access the
			// documents any more
			reader.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

}