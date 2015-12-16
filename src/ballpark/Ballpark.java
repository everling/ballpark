package ballpark;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


public class Ballpark {
	
	public static String PARALLEL_SWE = "aspac-corpus/aspacsven-sv.xml";
	public static String PARALLEL_EN = "aspac-corpus/aspacsven-en.xml";
	public static String PARALLEL_MEMO = "memo";
	public static String DIRECT_LEX = "direct-lexicon/folkets_sv_en_public.xml";
	
	Inflection infl;
	ParallelTranslator pc;
	DirectTranslator dt;
	public static void main(String[] args){
		
		if(args.length < 1){
			System.out.println("Usage: [options] fileToTranslate");
			System.out.println("Options:\n-d (only direct translation)\n-n [digit] (size of ngrams (default=2))\n-c (check every ngram possibility)\n-h (highlight direct translation in brackets)\n-p (print ngram probabilities)");
			return;
		}
		
		
		Ballpark m = new Ballpark();
		m.infl = new Inflection();
		m.pc  = new ParallelTranslator(PARALLEL_SWE,PARALLEL_EN,PARALLEL_MEMO);
		m.dt = new DirectTranslator(DIRECT_LEX);
		List<String> tokens = m.tokenize(args[args.length-1]);

		if(tokens.size() == 0){
			System.out.println("Couldn't open file "+args[args.length-1]);
			return;
		}
		
		
		//process options
		
		boolean direct = false;
		boolean overlap = false;
		boolean showBackup = false;
		boolean printProbabilities = false;
		int n = 2;
		
		for(int i = 0; i < args.length-1; i++){
			if(args[i].equals("-d")){
				direct = true;
			}
			else if(args[i].equals("-c")){
				overlap = true;
			}
			else if(args[i].equals("-h")){
					showBackup = true;
			}
			else if(args[i].equals("-p")){
				printProbabilities = true;
			}
			else if(args[i].equals("-n") && ++i < args.length-1){
				try{
					n = Integer.parseInt(args[i]);
					if(n < 1 || n > 6){
						System.out.println("Bad value of n");
						return;
					}
				}
				catch(NumberFormatException ne){
					System.out.println("Invalid number "+args[i]);
					return;
				}
			}
		}
		
		if(direct){
			m.dt.translate(tokens);
		}
		else{
			if(overlap){
				m.pc.translateOverlap(tokens, n, m.dt, showBackup);
			}
			else{
				m.pc.translateSimple(tokens, n, m.dt, showBackup);
			}
			
			if(printProbabilities)
				m.pc.printUsed();
			m.pc.saveMemoization(PARALLEL_MEMO);
		}
	}
	
	/**
	 * Tokenize file into list of strings
	 * @param in
	 * @return
	 */
	public List<String> tokenize(String in){
		
		StringBuilder sb = new StringBuilder();
		
		try {
			BufferedReader bf =  new BufferedReader(new InputStreamReader(new FileInputStream(in),"UTF-8"));

			String line;
			while((line = bf.readLine()) != null)
				sb.append(line);
					
			
		} catch (IOException e) {
		}
		
		List<String> ret = new ArrayList<String>();
		
		StringTokenizer st = new StringTokenizer(sb.toString());
	     while (st.hasMoreTokens()) {
	    	 String tok = st.nextToken();
	         ret.add(tok);
	     }
		return ret;
	}
	
	/**
	 * Identifies preceding and trailing non-alphabetic/numeric characters
	 * @param token
	 * @return array of {preceding,word,trailing}
	 */
	public static String[] separator(String token){
		
		String[] ret = new String[]{"","",""};
		
		for(char c : token.toCharArray()){
			if(Character.isAlphabetic(c) || Character.isDigit(c))
				ret[1] += c;
			else if(ret[1].isEmpty())
				ret[0] += c;
			else
				ret[2] += c;
		}
		return ret;
	}
	
	
	
	/**
	 * 
	 * @param word
	 * @return {original word, inflection, lemma}
	 */
	public static String[] lemmatize(String word){

		Runtime rt = Runtime.getRuntime();
		try {
			OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream("word.txt"),Charset.forName("ISO-8859-15").newEncoder() );
			fw.write(word);
			fw.flush();
			
			
			Process pr = rt.exec("granska-tagger/tagg -WS -l granska-lex word.txt");
			pr.waitFor();
			BufferedReader bf = new BufferedReader(new InputStreamReader(pr.getInputStream(),"ISO-8859-15"));
			String line = null;
			
			while((line = bf.readLine()) != null){			

				if(line.startsWith(word)){
					String[] lem = line.split("\\s");
					return lem;
				}
			}
			
			
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		
		return new String[]{word,"",word};
		
	}
	
	
}
