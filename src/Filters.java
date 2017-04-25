import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.util.AttributeFactory;

public class Filters {
	private Set<String> s;
	StandardTokenizer stk;
	public static int currentFunction;
	
	public Filters() {
		s = new HashSet<String>();
		AttributeFactory factory = AttributeFactory.DEFAULT_ATTRIBUTE_FACTORY;
		stk = new StandardTokenizer(factory);
		try {
			BufferedReader br = new BufferedReader(new FileReader("./StopWords/stopword.txt"));
			while (br.ready())
				s.add(br.readLine());
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
		}	
	}

	public Filters(int currentFunction) {
		Filters.currentFunction = currentFunction;
		
		s = new HashSet<String>();
		AttributeFactory factory = AttributeFactory.DEFAULT_ATTRIBUTE_FACTORY;
		stk = new StandardTokenizer(factory);
		try {
			BufferedReader br = new BufferedReader(new FileReader("./StopWords/stopword.txt"));
			while (br.ready())
				s.add(br.readLine());
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
		}	
	}
	
	public String currentTokenizerStringFunction(String input) throws IOException{
		switch(currentFunction){
		case 1:
			return tokenizeStopStem(input);
		default:
			throw new IOException("Invalid args");
		}
	}

	public String tokenizeStopStem(String input) throws IOException {
		stk.setReader(new StringReader(input));
		TokenStream tokenStream = stk;
		tokenStream = new LowerCaseFilter(tokenStream);
		tokenStream = new StopFilter(tokenStream, CharArraySet.copy(s));
		tokenStream = new PorterStemFilter(tokenStream);
		tokenStream.reset();
		StringBuilder sb = new StringBuilder();
		tokenStream.addAttribute(OffsetAttribute.class);
		CharTermAttribute charTermAttr = tokenStream.getAttribute(CharTermAttribute.class);
		try {
			while (tokenStream.incrementToken()) {
				if (sb.length() > 0)
					sb.append(" ");
				sb.append(charTermAttr.toString());
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		tokenStream.close();
		return sb.toString();
	}
}
