import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Scanner;

public class QueryProcessor 
{ 
	public static void main(String[] args)throws FileNotFoundException 
	{
	
		Scanner in = new Scanner(System.in);
		Scanner in2 = new Scanner(System.in);
		Scanner in3 = new Scanner(System.in);
		Scanner in4 = new Scanner(System.in);
		
		String folderName="";
		System.out.print("Name of a folder: ");
		folderName = in.next();
		
		IndexBuilder index_build = new IndexBuilder(folderName);
		
		/*
		 * retrieve a set S consisting of top 2k documents (along with cosine similarities with q) 
		 * that matches the query using vector space model scoring
		 * 
		 */
		
		// 1. Document collection
		System.out.println("1. Document collection");
		String[] file_names = index_build.getFileNames();
		int N = file_names.length;
		
		BiWordDocumentFilter[] bwFilter = new BiWordDocumentFilter[N];
		LinkedHashMap<String, Integer> fileIndexMap = new LinkedHashMap<String, Integer>();
		
		
		for(int i=0; i<N; i++)
		{
			bwFilter[i] = new BiWordDocumentFilter(8,file_names[i],folderName);
			bwFilter[i].addDocument();
			fileIndexMap.put(file_names[i],i);
		}
		
		// 2. Pre-processing Step1: Build the inverted index
		System.out.println("2. Pre-processing Step1: Build the inverted index");
		
		index_build.buildIndex();
		
		// 3. Pre-processing Step2: Initialize Length[i]to ||v(di)|| for 1 <= i <= N.
		System.out.println("3. Pre-processing Step2: Initialize Length[i]to ||v(di)|| for 1 <= i <= N.");
		
		double[] Length =  new double[N];
		
		for(int i=0; i<N; i++)
		{
			Length[i] = vectorLength(index_build.getDocWeightVector(file_names[i]));
		}
		
		System.out.print("Enter number of queries willing to check: ");
		int queryCount = in4.nextInt();
		
		for(int qc=0; qc<queryCount; qc++)
		{
			// 4. Input: Query q
			System.out.println("4. Input: Query"+qc);
			
			String q = "";
			int k = 0;
			
			System.out.print("Enter query "+qc+":");
			q = in2.nextLine();
			String[] rawQTerms = q.toLowerCase().split("\\s+");
			ArrayList<String> qTerms = new ArrayList<String>();
			
			for(String t: rawQTerms)
			{
				if((t.length()>=3) && !(t.equalsIgnoreCase("the")))
				{
					t = removePunctuation(t);
					
					qTerms.add(t);
				}
			}
			
			System.out.print("Enter an integer: ");
			k = in3.nextInt();
			
			// 5. Initialize scores[i] to 0 for for 1 <= i <= N
			System.out.println("5. Initialize scores[i] to 0 for for 1 <= i <= N");
			
			double[] scores = new double[N];
			
			for(int d=0; d<N; d++)
			{
				scores[d] = 0.0;
			}
			
			// 6. For each term t in q
			// 		(a) Fetch postings list postings(t) for t.
			//		(b) For each tuple <d,TFtd> in postings(t)
			//				 Compute weight(t; d)
			//				 scores[d] = scores[d] + weight(t; d)
			System.out.println("6. For each term t in q set scores for each document");
			
			
			double w =0.0;
			for(String t : qTerms)
			{
				for(int d=0; d<N; d++)
				{
					w = index_build.weight(t, file_names[d]);
					scores[d] += w;
				}
				
			}
			
			// 7. Compute ||v(q)||.
			System.out.println("7. Compute ||v(q)||");
			
			double vq = vectorLength(index_build.getQueryWeightVector(q));
			
			// 8. For 1 <= d <= N DO
			//		scores[d] = scores[d]/ (Length[d]*||v(q)||)
			
			System.out.println("8. Update scores for each document");
			
			for(int d=0; d<N; d++)
			{
				if((Length[d]*vq) != 0.0)
				{
					scores[d] = scores[d]/(Length[d]*vq);
				}
				else
				{
					scores[d] = 0.0;
				}
				
			}
			
			// 9. Top k components of scores array both doc names and cosine similarity.		
			ArrayList<Tuple> docsScore =  new ArrayList<Tuple>();
			int x = 0;
			for(String tk: file_names)
			{
				Tuple t = new Tuple(tk,scores[x]);
				docsScore.add(t);
				x++;
			}
			
			System.out.println("9. Retrieve a set S consisting of top 2k documents (along with cosine similarities with q) that matches the query using vector space model scoring.");
			
			ArrayList<Tuple> top2KDocsVSMS = getTopKDocs_VectorSpaceModelScoring(docsScore, k*2);
			
			// 10. Find B(q)set of all bi-words of query
			System.out.println("10. Find B(q)set of all bi-words of query");
			
			ArrayList<String> Bq = getBiWords(q);
			
			// 11.  s(d) = number of bi-words from B(q) that appear in d
			// 		For every document d in S, Compute s(d) using BiWordDocumentFilter
			System.out.println("11. For every document d in S, Compute s(d) using BiWordDocumentFilter");
			
	
			ArrayList<Integer> s =new ArrayList<Integer>();
			for(int i=0; i<k*2; i++)
			{
				s.add(0);
			}
			
			for(int d=0; d<k*2; d++)
			{
				int indexD = fileIndexMap.get(top2KDocsVSMS.get(d).doc);
				for(String b: Bq)
				{
					if(bwFilter[indexD].appears(b))
					{
						int temp = s.get(d)+1;
						s.set(d, temp);
					}
				}
			}
					
			// 12. Top k documents along with cosine similarities with q based on the criteria 
			//	   given in the assignment
			System.out.println("12. Top k documents along with cosine similarities with q");
			
			ArrayList<Tuple> topKRankedDocs  = getTopKRankedDocs(top2KDocsVSMS,s,k);
			
			
			for(Tuple tk: topKRankedDocs)
			{
				System.out.println("TopKRanked"+tk.doc + "\t" + tk.cosine_sim);
			}
			
			//Writing data to files
			System.out.println("********************************************Writing data to files");
			String fileNamePK = "MyTopPageRankQuery"+Integer.toString(qc)+"Output.txt";
			try {
				PrintWriter fileOut= new PrintWriter(fileNamePK);
				
				for(Tuple tk: topKRankedDocs)
				{
					fileOut.println("TopKRanked"+tk.doc + "\t" + tk.cosine_sim);
				}
			
				
			fileOut.close();
			}
			catch (FileNotFoundException e) {
				
			}
			
			System.out.println("********************************");
			System.out.println("********************************");
			System.out.println("********************************");
			System.out.println("********************************");
			System.out.println("********************************");
		}		
	}
		
	public static double vectorLength(double[] v)
	{
		double length = 0.0;
		double sqSum = 0.0;
		
		for(int i=0; i<v.length; i++)
		{
			sqSum += (v[i]*v[i]);
		}
		
		length = Math.pow(sqSum, 0.5);
		
		return length;
	}
	
	public static ArrayList<Tuple> getTopKDocs_VectorSpaceModelScoring(ArrayList<Tuple> docsScore, int k2)
	{
		ArrayList<Tuple> topKDocs =  new ArrayList<Tuple>();
		int index = 0;
		
		double max = docsScore.get(0).cosine_sim;
		int n = 0;
		
		
		while((docsScore.size()>0)&& (n<k2))
		{
			
			for(int i=0; i<docsScore.size(); i++)
			{
				
				if(max<=docsScore.get(i).cosine_sim)
				{
					max = docsScore.get(i).cosine_sim;
					index = i;
				}
			}
		
			topKDocs.add(docsScore.get(index));
			docsScore.remove(index);
			if(docsScore.size()>0)
			{
				max = docsScore.get(0).cosine_sim;
				index = 0;
			}		
			n++;
		}	
		return topKDocs;
	}
	
	public static ArrayList<String> getBiWords(String s)
	{
		ArrayList<String> biWords = new ArrayList<String>();
		
		String[] arrS = s.toLowerCase().split("\\s+");
		
		
		String prev_w ="";
		for(String w: arrS)
		{

			if((w.length()>=3) && !(w.equalsIgnoreCase("the")))
			{
				w = removePunctuation(w);
				if(!w.equals(""))
				{
					if(!prev_w.equals(""))
					{
						biWords.add(prev_w + " " +w);
					}
					prev_w = w;
				}							
			}		
		}		
		return biWords;
	}
	
	public static String removePunctuation(String t)
	{
		t = t.replace(".", "");
		t = t.replace(",", "");
		t = t.replace(":", "");
		t = t.replace(";", "");
		t = t.replace("'", "");
		return t;
	}
	
	public static ArrayList<Tuple> getTopKRankedDocs(ArrayList<Tuple> top2k, ArrayList<Integer> s, int k)
	{
		ArrayList<Tuple> topKRankDocs =  new ArrayList<Tuple>();
		ArrayList<Integer> sameRankIndex =  new ArrayList<Integer>();
		int max = s.get(0);
		int n= 0;
		
		while(n<k)
		{
			max = s.get(0);
			sameRankIndex.clear();
			for(int i=0; i<s.size(); i++)
			{
				if(max < s.get(i))
				{
					sameRankIndex.clear();
					sameRankIndex.add(i);
					max = s.get(i);
				}
				
				if(max == s.get(i))
				{
					if(!sameRankIndex.contains(i))
					{
						sameRankIndex.add(i);
						max = s.get(i);
					}
				}
			}
			
			if(sameRankIndex.size()==1)
			{
				int index = sameRankIndex.get(0);
				if(n<k)
				{
					topKRankDocs.add(top2k.get(index));
					top2k.remove(index);
					s.remove(index);
					n++;
				}			
				sameRankIndex.clear();
			}
			
			if(sameRankIndex.size()> 1)
			{
				int prevIndex;
				int shif = 0;
				ArrayList<Tuple> tArr = new ArrayList<Tuple>();
				for(int index : sameRankIndex)
				{
					tArr.add(top2k.get(index));
				}
				sameRankIndex = selectionSortInt(sameRankIndex);
				Collections.reverse(sameRankIndex);
				for(int index : sameRankIndex)
				{
					top2k.remove(index);
					s.remove(index);
				}
				
				tArr = selectionSortTuples(tArr);
				for(int p=tArr.size()-1; p>=0; p--)
				{
					if(n<k)
					{
						topKRankDocs.add(tArr.get(p));
						n++;
					}
					
				}
				sameRankIndex.clear();			
			}			
		}		
		return topKRankDocs;
	}
	
	public static ArrayList<Tuple> selectionSortTuples(ArrayList<Tuple> arrTuple)
	{
		
		int i,j;
		int iMin;
		int n = arrTuple.size();
		 
	
		for (j = 0; j < n-1; j++) {
		  
		    iMin = j;
		  
		    for ( i = j+1; i < n; i++) {
		       
		        if (arrTuple.get(i).cosine_sim < arrTuple.get(iMin).cosine_sim) {
		           
		            iMin = i;
		        }
		    }
		 
		    if(iMin != j) {
		       
		    	Tuple tj= arrTuple.get(j);
		    	Tuple tiMin= arrTuple.get(iMin);
		    	arrTuple.add(j, tiMin);
		    	arrTuple.remove(j+1);
		    	
		    	arrTuple.add(iMin, tj);
		    	arrTuple.remove(iMin+1);
		    }		 
		}		
		return arrTuple;
	}
	
	public static ArrayList<Integer> selectionSortInt(ArrayList<Integer> arr)
	{
	    
		int i,j;
		int iMin;
		int n = arr.size();
		 	
		for (j = 0; j < n-1; j++) 
		{	  
		    iMin = j;
		  
		    for ( i = j+1; i < n; i++) 
		    {
		       
		        if (arr.get(i) < arr.get(iMin))   
		            iMin = i;      
		    }
		 
		    if(iMin != j) {
		       
		    	int tj= arr.get(j);
		    	int tiMin= arr.get(iMin);
		    	arr.add(j, tiMin);
		    	arr.remove(j+1);
		    	
		    	arr.add(iMin, tj);
		    	arr.remove(iMin+1);
		    	
		    }	 
		}	
		return arr;
	}
}

class Tuple
{
	String doc;
	double cosine_sim;
	
	public Tuple(String doc, double cosine_sim)
	{
		this.doc = doc;
		this.cosine_sim = cosine_sim;
	}	
}
