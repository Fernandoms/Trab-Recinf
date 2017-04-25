import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;

public class DocumentIndexer {
	
	private IndexWriter indexWriter;
	private static Filters f;

	public DocumentIndexer(IndexWriter indexWriter) {
		super();
		this.indexWriter = indexWriter;
		String path_yan = "./cfc/";
		// String path_fernandinho= "/home/fernandoms/Downloads/cfc/";
		readFile(indexWriter, path_yan + "cf74");
		readFile(indexWriter, path_yan + "cf75");
		readFile(indexWriter, path_yan + "cf76");
		readFile(indexWriter, path_yan + "cf77");
		readFile(indexWriter, path_yan + "cf78");
		readFile(indexWriter, path_yan + "cf79");
	}

	public static final String[] DOCUMENT_INFO_TYPE = { "RN", "TI", "MJ", "MN", "AB/EX" };

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
				/*
				 * for (int j = 0; j < 5; j++) { if (index[j] != null) {
				 * System.out.print(type[j]); System.out.println(index[j]); } }
				 */
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
					q.query = sCurrentLine.substring(3, sCurrentLine.length());
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

	private static void addDoc(IndexWriter w, String[] document) throws IOException {
		f = new Filters();
		Document doc = new Document();
		// A text field will be tokenized
		for (int j = 0; j < 5; j++) {
			if (document[j] != null) {
				TextField tf = new TextField(DOCUMENT_INFO_TYPE[j], f.currentTokenizerStringFunction(document[j]), Field.Store.YES);
				switch (j) {
				case 2:
					tf.setBoost(.1f);
					break;
				case 3:
					tf.setBoost(.0f);
					break;
				case 4:
					tf.setBoost(.8f);
					break;
				default:
					break;
				}
				doc.add(tf);
				// TODO: setBoost no TextField
			}
		}
		w.addDocument(doc);
	}

	public IndexWriter getIndexWriter() {
		return indexWriter;
	}

}
