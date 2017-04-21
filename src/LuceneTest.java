import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.Weight;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

public class LuceneTest {

	private static final String[] type = { "RN", "TI", "MJ", "MN", "AB/EX" };

	public static void main(String[] args) {
		try {
			// Specify the analyzer for tokenizing text.
			// The same analyzer should be used for indexing and searching
			StandardAnalyzer analyzer = new StandardAnalyzer();

			// Code to create the index
			Directory index = new RAMDirectory();

			IndexWriterConfig config = new IndexWriterConfig(analyzer);

			IndexWriter w = new IndexWriter(index, config);

			readFile(w, "/home/fernandoms/Downloads/cfc/cf74");
			readFile(w, "/home/fernandoms/Downloads/cfc/cf75");
			readFile(w, "/home/fernandoms/Downloads/cfc/cf76");
			readFile(w, "/home/fernandoms/Downloads/cfc/cf77");
			readFile(w, "/home/fernandoms/Downloads/cfc/cf78");
			readFile(w, "/home/fernandoms/Downloads/cfc/cf79");
			w.close();

			// Text to search
			String querystr = "Is CF mucus abnormal?";

			// The \"title\" arg specifies the default field to use when no
			// field is explicitly specified in the query
			Query q = new QueryParser("TI", analyzer).parse(querystr);			

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

	private static void addDoc(IndexWriter w, String[] document) throws IOException {

		Document doc = new Document();
		// A text field will be tokenized
		for (int j = 0; j < 5; j++) {
			if (document[j] != null) {
				doc.add(new TextField(type[j], document[j], Field.Store.YES));
				// TODO: setBoost no TextField
			}
		}
		w.addDocument(doc);
	}

	private static void readFile(IndexWriter w, String fileName) {
		BufferedReader br = null;
		List<String[]> docs = new ArrayList<>();
		FileReader fr = null;
		String[] doc;
		try {
			fr = new FileReader(fileName);
			br = new BufferedReader(fr);
			String sCurrentLine;
			doc = new String[5];
			int save = -1;
			while ((sCurrentLine = br.readLine()) != null) {
				save = isSaveFields(sCurrentLine, save);
				if (save != -1) {
					if (sCurrentLine.length() > 2 && !sCurrentLine.startsWith(" ")) {
						if (save == 0) {
							docs.add(doc);
							doc = new String[5];
						}
						doc[save] = sCurrentLine.substring(2, sCurrentLine.length());
					} else {
						doc[save] = doc[save] + sCurrentLine.replace("  ", "");
					}
				}
			}
			docs.add(doc);
			for (String[] index : docs) {
				/*for (int j = 0; j < 5; j++) {
					if (index[j] != null) {
						System.out.print(type[j]);
						System.out.println(index[j]);
					}
				}*/
				addDoc(w, index);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static int isSaveFields(String line, int isToSave) {
		if (line != null && line.length() > 2) {
			String code = line.substring(0, 2);
			switch (code) {
			case "RN":
				return 0;
			case "TI":
				return 1;
			case "MJ":
				return 2;
			case "MN":
				return 3;
			case "AB":
			case "EX":
				return 4;
			case "PN":
			case "AN":
			case "AU":
			case "SO":
			case "RF":
			case "CT":
				return -1;
			default:
				return isToSave;
			}
		}
		return -1;
	}
}