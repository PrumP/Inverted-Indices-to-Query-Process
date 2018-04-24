import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class BiWordDocumentFilter 
{
	
	private final String fileName;
	private final String pathName;
	private final int bitsPerBiWord;
	private final ArrayList<String> biWords;
	private final BloomFilterDet BFD;
		
	public BiWordDocumentFilter(int bitsPerBiWord, String fileName, String pathName)throws FileNotFoundException
	{
		this.bitsPerBiWord  = bitsPerBiWord;
		this.fileName = fileName;
		this.pathName = pathName;
		this.biWords = new ArrayList<String>();
		
		String filePath = this.pathName+"/"+this.fileName;
		
		File inputFile = new File(filePath);

		Scanner fileIn= new Scanner(inputFile);
		
		String w = "";
		String prev_w ="";
		while(fileIn.hasNext())
		{
			w = fileIn.next();
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

		this.BFD = new BloomFilterDet(this.biWords.size(),this.bitsPerBiWord);
		
		fileIn.close();
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
	
	public void addDocument()
	{
		for(int i=0; i<this.biWords.size();i++)
		{
			this.BFD.add(biWords.get(i));
		}
	}
	
	public boolean appears(String s)
	{
		return this.BFD.appears(s);
	}
	
	public String getDocument()
	{
		return this.pathName+"/"+this.fileName;
	}
	
	public String getFileName()
	{
		return this.fileName;
	}
}
