import java.util.BitSet;


public class BloomFilterDet 
{
	private final BitSet data;
	private final int filterSize;
	private final int bitsPerElement;
	private final int setSize;
	private final int maxNoHashFuntions;
	private int noElements;
	
	private static final long FNV_64_INIT = 0xcbf29ce484222325L;
	private static final long FNV_64_PRIME = 0x100000001b3L;
	private static final long FNV_64_MOD = 0x1999999999999999L;	 

	public BloomFilterDet(int setSize, int bitsPerElement)
	{
		 this.setSize = setSize;
		 this.bitsPerElement = bitsPerElement;
		 this.filterSize = this.setSize * this.bitsPerElement;
		 this.data = new BitSet(this.filterSize);
		 this.maxNoHashFuntions = (int)(Math.log(2.0)*this.bitsPerElement);
		 this.noElements = 0;
		
	}
	
	public long hash64(String s) 
	{
        long rv = FNV_64_INIT;
        final int len = s.length();
        for(int i = 0; i < len; i++) 
        {
        	rv = rv^s.charAt(i);
            rv = (rv*FNV_64_PRIME)%FNV_64_MOD;
        }
        return rv;
    }
	
	public void add(String s)
	{
		long hashValue = hash64(s.toLowerCase());
		int a = (int)(hashValue & 0xFFFFFFFF); // lower hash 
		int b = (int)((hashValue >> 32) & 0xFFFFFFFF); // upper hash
		int x = 0;
		int kHashValue = 0;
		int bitNo = 0;
		
		for(int i=0; i<this.maxNoHashFuntions; i++)
		{
			kHashValue = (b + a*i);
			bitNo =  kHashValue%this.filterSize;
			this.data.set(Math.abs(bitNo));
		}
		this.noElements++;
		
	}
	
	public boolean appears(String s)
	{
		long hashValue = hash64(s.toLowerCase());
		int a = (int)(hashValue & 0xFFFFFFFF); // lower hash 
		int b = (int)((hashValue >> 32) & 0xFFFFFFFF); // upper hash
		int x = 0;
		int kHashValue = 0;
		int bitNo = 0;
		boolean found = true;
		
		for(int i=0; i<this.maxNoHashFuntions; i++)
		{
			kHashValue = (b + a*i);
			bitNo =  kHashValue%this.filterSize;
			found = found && this.data.get(Math.abs(bitNo));
		}
		
		return found;
	}
	
	public int filterSize()
	{
		return this.filterSize;
	}
	
	public int dataSize()
	{
		return this.noElements;
	}
	
	public int numHashes()
	{
		return this.maxNoHashFuntions;
	}
}
