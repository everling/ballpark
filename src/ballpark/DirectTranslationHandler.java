package ballpark;

import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class DirectTranslationHandler extends DefaultHandler{
	
	boolean correctWordValue;
	String wordValue;
	
	List<String> translations;
	
	
	public DirectTranslationHandler(String word,List<String> translations){
		this.wordValue = word;
		this.translations = translations;
	}

	public void startElement(String uri, String localName,String qName, Attributes attributes) throws SAXException {


		if (qName.equalsIgnoreCase("word")) {
			correctWordValue = (wordValue.equals(attributes.getValue("value")));
		}
		else if(correctWordValue && qName.equalsIgnoreCase("translation")){
			String translationString = attributes.getValue("value");
			String[] defs = translationString.split("[,; ]");
			for(String s : defs)
				translations.add(s);
		}
	

	}

	public void endElement(String uri, String localName,String qName) throws SAXException {
		
	}
	
	public void characters(char ch[], int start, int length) throws SAXException {

	}


}
