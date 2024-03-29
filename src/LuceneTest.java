import java.io.FileWriter;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

public class LuceneTest {

	public static void main(String[] args) {
		try {
			boolean dev = true;
			if(args.length < 2 && !dev)
				throw new Exception("Invalid args count");
			
			List<String> instances = new ArrayList<>();
			List<String> validInstances = new ArrayList<>();
			
			validInstances = Arrays.asList("clean", "porter", "similarity_clean", "similarity_porter", 
					"top_10_similarity_porter", "top_50_similarity_porter", "top_100_similarity_porter", "top_150_similarity_porter");
			
			if(dev){
				instances = validInstances;
			} else {
				for(String arg : args){
					if(arg.equals("all")){
						instances = validInstances;
						break;
					}
					for(String validInstance: validInstances) {
					    if(validInstance.trim().contains(arg.trim()))
					       instances.add(arg);
					    else
					    	throw new Exception("Invalid arg: " + arg);
					}
				}
			}
			
			for(String instance : instances){
				System.out.println("Running instance " + instance);
				int currentFunction;
				if(instance.equals("clean")){
					currentFunction = 1;
				} else if(instance.equals("porter")) {
					currentFunction = 2;
				} else if(instance.equals("similarity_clean")){
					currentFunction = 3;
				} else if(instance.equals("similarity_porter")){
					currentFunction = 4;
				} else if(instance.equals("top_10_similarity_porter")){
					currentFunction = 5;
				} else if(instance.equals("top_50_similarity_porter")){
					currentFunction = 6;
				} else if(instance.equals("top_100_similarity_porter")){
					currentFunction = 7;
				} else if(instance.equals("top_150_similarity_porter")){
					currentFunction = 8;
				}
				
				else {
					currentFunction = -1;
				}
				DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
				Date date = new Date();

				// Specify the analyzer for tokenizing text.
				// The same analyzer should be used for indexing and searching
				StandardAnalyzer analyzer = new StandardAnalyzer();
				Filters f = new Filters(currentFunction);
				// Code to create the index
				Directory index = new RAMDirectory();

				IndexWriterConfig config = new IndexWriterConfig(analyzer);

				IndexWriter w = new IndexWriter(index, config);

				DocumentIndexer indexer = new DocumentIndexer(w);
				
				String csvFile = "./output_" + instance + "_" + dateFormat.format(date) + ".csv";
				CSVUtils CSVUtils = new CSVUtils();
				FileWriter writer = new FileWriter(csvFile);

				w = indexer.getIndexWriter();
				w.close();

				List<QueryCFC> queries = DocumentIndexer.readQueryFile();

				CSVUtils.writeLine(writer, Arrays.asList("Query", "Precision", "Recall", "F-Measure", "Total Results"),
						';');

				for (QueryCFC qcfc : queries) {
					String querystr = qcfc.query;

					// The \"title\" arg specifies the default field to use when no
					// field is explicitly specified in the query
					Query q = new MultiFieldQueryParser(DocumentIndexer.DOCUMENT_INFO_TYPE, analyzer)
							.parse(f.currentTokenizerStringFunction(querystr));

					// Searching code
					int hitsPerPage = 200;
					if(currentFunction == 5)
						hitsPerPage = 10;
					else if(currentFunction == 6)
						hitsPerPage = 50;
					else if(currentFunction == 7)
						hitsPerPage = 100;
					else if(currentFunction == 8)
						hitsPerPage = 150;
					IndexReader reader = DirectoryReader.open(index);
					IndexSearcher searcher = new IndexSearcher(reader);
					if(currentFunction < 3){
						searcher.setSimilarity(new ClassicSimilarity());
					} else {
						searcher.setSimilarity(new BM25Similarity());
					}
					TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage);
					searcher.search(q, collector);
					ScoreDoc[] hits = collector.topDocs().scoreDocs;

					// Code to display the results of search
					double relevant_hits = 0;

					for (int i = 0; i < hits.length; ++i) {
						for (int relevant_doc : qcfc.relevant_docs) {
							if (searcher.doc(hits[i].doc).get("RN").equals(String.format("%05d", relevant_doc)))
								relevant_hits++;
						}
					}

					double precision = (double) relevant_hits / hits.length;
					double recall = (double) relevant_hits / qcfc.relevant_docs.size();
					double fmeasure = (double) 2 / ((1 / precision) + (1 / recall));
					DecimalFormat formatter = new DecimalFormat("#.####");

					CSVUtils.writeLine(writer,
							Arrays.asList(String.format("%d", qcfc.query_number), formatter.format(precision).replace('.', ','),
									formatter.format(recall).replace('.', ','), formatter.format(fmeasure).replace('.', ','), 
									String.valueOf(hits.length)), ';');

					// reader can only be closed when there is no need to access the
					// documents any more
					reader.close();
				}
				CSVUtils.writeLine(writer,
						Arrays.asList("AVG", "=AVERAGE(B2:B101)", "=AVERAGE(C2:C101)", "=AVERAGE(D2:D101)"), ';');
				CSVUtils.writeLine(writer, Arrays.asList("STDEV", "=STDEV(B2:B101)", "=STDEV(C2:C101)", "=STDEV(D2:D101)"),
						';');
				writer.flush();
				writer.close();
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

}