import java.util.ArrayList;

class Dictionary
{
	String term;
	int df;
	Postings post;
	
	public Dictionary()
	{
		this.term = "";
		this.df = 0;
		this.post = new Postings();
	}
	
	public void setDF()
	{
		this.df = this.post.doc.size();
	}

}

class Postings
{
	ArrayList<String> doc;
	ArrayList<Double> TF;
	
	
	public Postings()
	{
		this.doc = new ArrayList<String>();
		this.TF = new ArrayList<Double>();
	}
	
	public void addDocument(String d, double f)
	{
		this.doc.add(d);
		this.TF.add(f);
	}	
}


public class InvertedIndex 
{
	
	Dictionary dictionary;
	
	public InvertedIndex()
	{
		dictionary = new Dictionary();
	}
}
