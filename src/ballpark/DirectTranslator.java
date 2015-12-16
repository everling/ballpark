package ballpark;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import ballpark.ParallelTranslator.Translation;

public class DirectTranslator {
	
	Inflection infl;
	private SAXParserFactory factory;
	private SAXParser saxParser;

	private String dictionaryPath;
	
	
	public DirectTranslator(String fileToDictionary){

		dictionaryPath = fileToDictionary;
		infl =  new Inflection();

		
		
		factory = SAXParserFactory.newInstance();
		try {
			saxParser = factory.newSAXParser();
			
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
		
		
		
		
		
	}
	
	
	private List<String> getWordSAX(String word){
		
		List<String> res = new ArrayList<String>();
		DirectTranslationHandler dHandler = new DirectTranslationHandler(word, res);
		
	    try {
			saxParser.parse(dictionaryPath, dHandler);
			return res;

		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return res;
	}
	


	
	public String translateWord(String token){
		String[] tokenTreated = Ballpark.separator(token);
		String wordToPrint = tokenTreated[1];
		
		
		List<String> transl = getWordSAX(tokenTreated[1].toLowerCase());
	
		if(transl.size() > 0){
			wordToPrint = transl.get(0);
		}
		else{
			String[] lem = Ballpark.lemmatize(tokenTreated[1].toLowerCase());
			transl = getWordSAX(lem[2]);
			if(transl.size() > 0){
				wordToPrint = infl.inflect(transl.get(0), lem[1]);
			}
		}
		
		String print = tokenTreated[0] +wordToPrint +tokenTreated[2]+" ";
		
		return print;
		
		
	}
	
	
	public void translate(List<String> tokens){
	
			for(String token : tokens){
				String print = translateWord(token);
				System.out.print(print);
			}
		}
	
	}
