package ballpark;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

public class ParallelTranslator {

	SAXParserFactory factory;
	SAXParser saxParser; 
	
	private String SWE;
	private String EN;
	
	HashMap<String,Translation> memoization;
	
	List<Translation> usedNgrams = new ArrayList<Translation>();
	
	
	public OverlapRes getBestNGram(List<String> tokens, int n){


		Translation bestTranslation = null;
		int bestIndex = -1;
		for(int i = 0; i < tokens.size() - n + 1; i++){
			List<String> subl = tokens.subList(i, i+n);
			String[] ngram = subl.toArray(new String[subl.size()]);
			Translation t = translateNGram(ngram);
			if(t != null){
				if(bestTranslation == null || t.evaluation() > bestTranslation.evaluation()){
					bestTranslation = t;
					bestIndex = i;
				}
			}
				
		}
		
		OverlapRes ret= new OverlapRes();
		ret.t = bestTranslation;
		ret.startIndex = bestIndex;
		return ret;
	}
	
	public void translateOverlap(List<String> tokens, int n, DirectTranslator backup, boolean showBackup){
		
		int pointer = 0; 
		while(pointer < tokens.size()){
			int limit = Math.min(2*n-1, tokens.size() - pointer);
			List<String> sublist = tokens.subList(pointer, pointer+limit);
			OverlapRes or = getBestNGram(sublist, n);
			
			
			if(or.t == null || or.t.best == null){
				for(String word : sublist)
					System.out.print((showBackup?"[":"")+(backup.translateWord(word))+(showBackup?"]":""));
				
				pointer += limit;

			}
			else{
				//direct translation of preceding
				for(int i = 0; i < or.startIndex;i++){
					System.out.print((showBackup?"[":"")+backup.translateWord(tokens.get(pointer + i))+(showBackup?"]":""));
				}
				pointer += or.startIndex;

				//print ngram
				System.out.print(or.t.best +" ");
				pointer += n;
				usedNgrams.add(or.t);


			}

			
		

		}
		
		
	}
	
	public void printUsed(){
		for(Translation t : usedNgrams)
			t.printStats();
	}
	
	
	public void translateSimple(List<String> tokens, int n, DirectTranslator backup, boolean showBackup){

		String print = "";
		
		for(int i = 0; i < tokens.size(); i+=n){
			
			int size = Math.min(tokens.size() - i, n);
			String[] ngram = new String[size];
			
			for(int j = i; j < i + size; j++){
				
				ngram[j-i] = tokens.get(j);

			}
			Translation translation = translateNGram(ngram);
			
			if(translation == null || translation.best == null){//fail
				for(String w : ngram){
					String toPrint = (showBackup?"[":"")+backup.translateWord(w)+(showBackup?"]":"");
					System.out.print(toPrint);
					print += toPrint;
					if(print.length() > 150){
						System.out.println();
						print = "";
					}
				}
			}
			else{
				usedNgrams.add(translation);
				String toPrint = translation.best +" ";	
				System.out.print(toPrint);
				print += toPrint;
				if(print.length() > 150){
					System.out.println();
					print = "";
				}
				
			}
		}
	}
	
	public Translation translateNGram(String[] words){
		
		//try memoization first
		String key = Arrays.toString(words);
		Translation mem = memoization.get(key);
		if(mem != null)
			return mem;

		
		List<NGramSourceHandler.Res> res = new ArrayList<NGramSourceHandler.Res>();
		NGramSourceHandler sweHandler = new NGramSourceHandler(words, res);
	    try {
			saxParser.parse(SWE, sweHandler);
		
			List<String> ngramTrans = new ArrayList<String>();
		    NGramDestHandler engHandler = new NGramDestHandler(res, ngramTrans);
		    saxParser.parse(EN, engHandler);
		    
		    Translation t = new Translation(key,ngramTrans);
		    
		    memoization.put(key, t);
		    
		    return t;
		    
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	    
	    return null;

	}
	
	
	public ParallelTranslator(String swe, String en, String memoizationPath){

		SWE = swe;
		EN = en;
	
		factory = SAXParserFactory.newInstance();
		try {
			saxParser = factory.newSAXParser();
			
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		loadMemoization(memoizationPath);
	}
	
	private void loadMemoization(String memoizationPath){
				memoization = new HashMap<String,Translation>();
				try {
					BufferedReader bf =  new BufferedReader(new InputStreamReader(new FileInputStream(memoizationPath)));
					
					String line  = null;
					
					while((line = bf.readLine()) != null){
						String key = line;
						int size = Integer.parseInt(bf.readLine());
						List<String> ngrams = new ArrayList<String>(size);
						for(int i = 0; i < size; i++){
							ngrams.add(bf.readLine());
						}
						Translation t = new Translation(key,ngrams);
						memoization.put(key, t);
						
					}
					
				} catch (FileNotFoundException e) {
				} catch (IOException e) {
				}
	}
	
	public void saveMemoization(String memoizationPath){
		
		try {
			PrintWriter pw = new PrintWriter(memoizationPath);
			
			for(Entry<String, Translation> entr : memoization.entrySet()){
				pw.println(entr.getKey());
				pw.println(entr.getValue().originalFrequency);
				for(String s : entr.getValue().ngramInstances){
					pw.println(s);
				}
			}
			
			pw.flush();
			pw.close();
		} catch (FileNotFoundException e) {

		}
	}
	
	public class OverlapRes{
		public Translation t;
		public int startIndex;
	}

	
	public class Translation{
		public String orig;
		public String best;
		public int originalFrequency;
		public HashMap<String,Float> stats;
		public List<String> ngramInstances;
		
		public Translation(String orig,List<String> ngramInstances){
			this.orig = orig;
			this.originalFrequency = ngramInstances.size();
			this.ngramInstances = ngramInstances;
			this.stats = new HashMap<String,Float>();
			
			for(String s : ngramInstances){
				Float f = stats.get(s);
				
				if(f == null){
					stats.put(s, 1f);
				}
				else{
					stats.put(s, f + 1f);
				}
			}
			float bestF = -1;
			for(String s : stats.keySet()){
				Float f = stats.get(s);
				if(f > bestF){
					bestF = f;
					best = s;
				}
				stats.put(s, f/((float)originalFrequency));
			}	
		}
		
		public void printStats(){
			System.err.println("\n"+orig+":");
			System.err.println("-------------");
			
			
			for(Entry<String,Float> e : stats.entrySet()){
				System.err.println(e.getKey() +": "+e.getValue());
			}
			System.err.println("-------------\nBest: " +best +" " +stats.get(best));
		}
		
		public float evaluation(){
			float bestValue = 0;
			if(stats.size() > 0 && best != null)
				bestValue = stats.get(best);
			return Math.min(bestValue,0.95f) * originalFrequency;
			
		}
		
	}
	
}


	

