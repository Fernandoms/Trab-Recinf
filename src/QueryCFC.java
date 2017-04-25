import java.util.ArrayList;
import java.util.List;

public class QueryCFC {
	public int query_number;
	public String query;
	public List<Integer> relevant_docs;

	public QueryCFC() {
		this.relevant_docs = new ArrayList<>();
	}
}
