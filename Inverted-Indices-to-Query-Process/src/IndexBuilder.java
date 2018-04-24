import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Scanner;
import java.util.Set;

public class IndexBuilder 
{
	private final String folder;
	private final File folderFiles;
	private final String[] file_names;
	private final int N;
	private final ArrayList<String> termCollection;
	private final int totalNumTerms;
	private final InvertedIndex[] inverted_index;

	
	public IndexBuilder(String folder) throws FileNotFoundException
	{
		System.out.println("Get files from the folder "+folder);
		this.folder = folder;
		this.folderFiles = new File(this.folder);
		this.file_names = folderFiles.list();
		this.N = this.file_names.length;
		System.out.println("Collect all valid terms");
		this.termCollection = new ArrayList<String>();
		findTermCollection();
		this.totalNumTerms = this.termCollection.size();
		System.out.println("Initialize inverted index");
		this.inverted_index = new InvertedIndex[this.totalNumTerms];
		for(int i=0; i<this.totalNumTerms; i++)
		{
			this.inverted_index[i] = new InvertedIndex();
		}
		
	}
	
	public void findTermCollection()throws FileNotFoundException
	{
		int totalNumberOfwords = 0;
		ArrayList<String> termList = new ArrayList<String>();
		HashSet<String> terms = new HashSet<String>();
		
		for(int i=0; i<this.file_names.length; i++)
		{
			termList = findFileTerms(this.file_names[i]);
			terms.addAll(termList);
		}
		
		for(String t : terms)
		{
			this.termCollection.add(t);
		}

	}
	
	public String removePunctuation(String t)
	{
		t = t.replace(".", "");
		t = t.replace(",", "");
		t = t.replace(":", "");
		t = t.replace(";", "");
		t = t.replace("'", "");

		return t;
	}
		
	public ArrayList<String> findFileTerms(String file) throws FileNotFoundException
	{
		ArrayList<String> termsList = new ArrayList<String>();
		
		String filePath = this.folder+"/"+file;
		
		File inputFile = new File(filePath);

		Scanner fileIn= new Scanner(inputFile);
		
		String t = "";
		while(fileIn.hasNext())
		{
			t = fileIn.next();
			t = t.toLowerCase();
			if((t.length()>=3) && !(t.equalsIgnoreCase("the")))
			{
				t = removePunctuation(t);
				termsList.add(t);
			}
		}
		
		return termsList;
	}
	
	
	public double[] findTermFrequency_d(ArrayList<String> doc)
	{
		int[] num = new int[this.totalNumTerms];
		
		for(int i=0;i< this.totalNumTerms; i++)
		{
				String t = this.termCollection.get(i);
				
				for(String doc_term : doc)
				{
						if(doc_term.equalsIgnoreCase(t))
						{
							num[i] +=1;
						}
				}
		}
		
		double[] freq = new double[this.totalNumTerms];
		
		for(int i=0; i<this.totalNumTerms; i++)
		{
			freq[i] = ((double)num[i]/(double)doc.size());
		}
				
		return freq;
	}
	
	public void buildIndex() throws FileNotFoundException
	{
		ArrayList<String> doc_termList =  new ArrayList<String>();
		String doc = "";
		double[] TF = new double[this.totalNumTerms] ;
		
		for(int i=0; i<this.totalNumTerms; i++)
		{
			this.inverted_index[i].dictionary.term = this.termCollection.get(i);
		}

		for(int d=0; d<this.file_names.length; d++)
		{
			doc  = this.file_names[d];
			doc_termList = findFileTerms(doc);
			TF = findTermFrequency_d(doc_termList);
			
			for(int i=0; i<this.totalNumTerms; i++)
			{
				if(TF[i]>0)
				{
					this.inverted_index[i].dictionary.post.doc.add(doc);
					this.inverted_index[i].dictionary.post.TF.add(TF[i]);
				}
				System.out.println("Building inverted index continue......");
			}
		}
		
		for(int i=0; i<this.totalNumTerms; i++)
		{
			this.inverted_index[i].dictionary.setDF();
		}
		
	}
	
	public double weight(String t, String d)
	{
		double w = 0.0;
		double TFtd = 0.0;
		int dft = 0;
		for(int i=0;i<this.totalNumTerms;i++)
		{
			if(this.inverted_index[i].dictionary.term.equalsIgnoreCase(t))
			{
				dft = this.inverted_index[i].dictionary.df;
				int index = this.inverted_index[i].dictionary.post.doc.indexOf(d);
				if(index>=0)
				{
					TFtd = this.inverted_index[i].dictionary.post.TF.get(index);
				}
			}
		}
		
		w = Math.log(1+ TFtd)*Math.log10((double)this.N/(double)dft);

		return w;
	}
	
	public int getTotalNumTerms()
	{
		return this.totalNumTerms;
	}
	
	public double[] getDocWeightVector(String d)
	{
		double[] vd = new double[this.totalNumTerms];
		
		for(int i=0; i<this.totalNumTerms; i++)
		{
			vd[i] = weight(this.termCollection.get(i), d);
		}
		
		System.out.println("Find document weight vector:"+d);
		
		return vd;
	}
	
	public Postings getPostings(String t)
	{
		Postings p = new Postings();
		for(int i=0; i<this.totalNumTerms; i++)
		{
			if(this.inverted_index[i].dictionary.term.equals(t))
			{
				p = this.inverted_index[i].dictionary.post;
				break;
			}
		}
		return p;
	}
	
	public String[] getFileNames()
	{
		return this.file_names;
	}
	
	public double[] findTermFrequency_q(String q)
	{
		int[] num = new int[this.totalNumTerms];
		
		String[] q_terms = q.toLowerCase().split("\\s+");
		
		for(int i=0;i< this.totalNumTerms; i++)
		{
				String t = this.termCollection.get(i);
				
				
				for(String qt : q_terms)
				{
						qt = removePunctuation(qt);
						
						if(qt.equalsIgnoreCase(t))
						{
							
							num[i] +=1;
						}
			
				}
		}
		
		
		double[] freq = new double[this.totalNumTerms];
		
		for(int i=0; i<this.totalNumTerms; i++)
		{
			freq[i] = ((double)num[i]/(double)q_terms.length);
		}
		
		return freq;
	}
	
	
	public int[] FindDocumentFrequency_q()
	{
		int[] df = new int[this.totalNumTerms];
		
		for(int i=0;i< this.totalNumTerms; i++)
		{
				df[i] = this.inverted_index[i].dictionary.df;
			
		}
		
		return df;		
	}
	
	public double[] getQueryWeightVector(String q)
	{
		double[] TFtq= findTermFrequency_q(q);
		int[] df = FindDocumentFrequency_q();
		double[] vq = new double[this.totalNumTerms];
		double weight = 0.0;

		for(int i=0;i<this.totalNumTerms;i++)
		{
			weight = Math.log(1+ TFtq[i])*Math.log10((double)this.N/(double)df[i]);
			vq[i] = weight;		
		}
		System.out.println("Find query weight vector:"+q);
		
		return vq;
	}
}
