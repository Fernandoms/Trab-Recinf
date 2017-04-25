import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

public class LuceneTest {

	public static class QueryCFC {
		public int query_number;
		public String query;
		public List<Integer> relevant_docs;

		public QueryCFC() {
			this.relevant_docs = new ArrayList<>();
		}
	}

	public static List<QueryCFC> readQueryFile() {

		BufferedReader br = null;
		List<QueryCFC> queries = new ArrayList<>();
		FileReader fr = null;
		try {
			fr = new FileReader("./cfc/cfquery");
			br = new BufferedReader(fr);
			String sCurrentLine;
			QueryCFC q = new QueryCFC();
			while ((sCurrentLine = br.readLine()) != null) {
				if (sCurrentLine.startsWith("QN")) {
					if (q.query_number > 0)
						queries.add(q);

					q = new QueryCFC();
					q.query_number = Integer.parseInt(sCurrentLine.substring(3, sCurrentLine.length()).trim());
				} else if (sCurrentLine.startsWith("QU")) {
					q.query = sCurrentLine.substring(3, sCurrentLine.length()).trim();
					while ((sCurrentLine = br.readLine()).startsWith("   "))
						q.query += sCurrentLine.substring(4, sCurrentLine.length()).trim();

				} else if (sCurrentLine.startsWith("RD")) {
					String[] split = sCurrentLine.substring(2, sCurrentLine.length()).trim().split("\\s+");
					for (int i = 0; i < split.length; i += 2)
						q.relevant_docs.add(Integer.parseInt(split[i]));
				} else if (!sCurrentLine.trim().isEmpty()) {
					String[] split = sCurrentLine.trim().split("\\s+");
					for (int i = 0; i < split.length; i += 2)
						q.relevant_docs.add(Integer.parseInt(split[i]));
				}
			}
			queries.add(q);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return queries;
	}

	public static void main(String[] args) {
		try {
			// Specify the analyzer for tokenizing text.
			// The same analyzer should be used for indexing and searching
			StandardAnalyzer analyzer = new StandardAnalyzer();
			Filters f = new Filters();
			// Code to create the index
			Directory index = new RAMDirectory();

			IndexWriterConfig config = new IndexWriterConfig(analyzer);

			IndexWriter w = new IndexWriter(index, config);

			DocumentIndexer indexer = new DocumentIndexer(w);

			String csvFile = "./output_" + ".csv";
			CSVUtils CSVUtils = new CSVUtils();
			FileWriter writer = new FileWriter(csvFile);

			w = indexer.getIndexWriter();
			w.close();

			List<QueryCFC> queries = readQueryFile();

			CSVUtils.writeLine(writer, Arrays.asList("Query", "Precision", "Recall", "F-Measure", "Total Results"),
					';');

			for (QueryCFC qcfc : queries) {
				/*
				 * if (qcfc.query_number > 15) continue;
				 */
				String querystr = qcfc.query;
				System.out.println(querystr);

				// The \"title\" arg specifies the default field to use when no
				// field is explicitly specified in the query
				Query q = new MultiFieldQueryParser(DocumentIndexer.DOCUMENT_INFO_TYPE, analyzer)
						.parse(f.tokenizeStopStem(querystr));

				// Searching code
				int hitsPerPage = 1000;
				IndexReader reader = DirectoryReader.open(index);
				IndexSearcher searcher = new IndexSearcher(reader);
				searcher.setSimilarity(new BM25Similarity());
				TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage);
				searcher.search(q, collector);
				ScoreDoc[] hits = collector.topDocs().scoreDocs;

				// Code to display the results of search
				double relevant_hits = 0;

				for (int i = 0; i < hits.length; ++i) {
					for (int relevant_doc : qcfc.relevant_docs) {
						if (hits[i].doc == relevant_doc)
							relevant_hits++;
					}
				}

				double precision = (double) relevant_hits / hits.length;
				double recall = (double) relevant_hits / qcfc.relevant_docs.size();
				double fmeasure = (double) relevant_hits / hits.length;
				DecimalFormat formatter = new DecimalFormat("#.####");

				CSVUtils.writeLine(writer,
						Arrays.asList(String.format("%d", qcfc.query_number), formatter.format(precision),
								formatter.format(recall), formatter.format(fmeasure), String.valueOf(hits.length)),
						';');

				System.out
						.println("Query: " + qcfc.query_number + " - Precision: " + precision + " - Recall: " + recall);

				// reader can only be closed when there is no need to access the
				// documents any more
				reader.close();
			}
			writer.flush();
			writer.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

}