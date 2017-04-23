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

	public DocumentIndexer(IndexWriter indexWriter) {
		super();
		this.indexWriter = indexWriter;
		String path_yan = "./cfc/";
		//String path_fernandinho= "/home/fernandoms/Downloads/cfc/";
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

	private static void addDoc(IndexWriter w, String[] document) throws IOException {

		Document doc = new Document();
		// A text field will be tokenized
		for (int j = 0; j < 5; j++) {
			if (document[j] != null) {
				doc.add(new TextField(DOCUMENT_INFO_TYPE[j], document[j], Field.Store.YES));
				// TODO: setBoost no TextField
			}
		}
		w.addDocument(doc);
	}

	public IndexWriter getIndexWriter() {
		return indexWriter;
	}

}
