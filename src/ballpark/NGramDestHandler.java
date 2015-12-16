package ballpark;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class NGramDestHandler extends DefaultHandler{
	
	List<NGramSourceHandler.Res> links;
	boolean relevantLink = false;
	
	List<String> results;
	String currentString = "";
	
	//indexes
	NGramSourceHandler.Res currentLink;
	int currentWord = 0;
	
	
	public NGramDestHandler(List<NGramSourceHandler.Res> links, List<String> results){
		this.links = links;
		this.results = results;
	}

	public void startElement(String uri, String localName,String qName, Attributes attributes) throws SAXException {


		if (qName.equalsIgnoreCase("LINK")) {
			String link = 	attributes.getValue("id");
			
			matchLink:
			while(true){
				for(NGramSourceHandler.Res l : links){
					if(l.linkID.equals(link)){
						relevantLink = true;
						currentLink = l;
						currentWord = 0;
						break matchLink;
					}
				}
				relevantLink = false;
				currentLink = null;
				break;
			}
				
			
		}
		if(relevantLink && qName.equalsIgnoreCase("w")){
			currentWord++;
		}
	

	}

	public void endElement(String uri, String localName,String qName) throws SAXException {

	}
	


	public void characters(char ch[], int start, int length) throws SAXException {
		
		if (relevantLink && currentWord >= currentLink.firstWord) {
			String w = new String(ch, start, length);
			w = w.trim();
			currentString += w;
			
			if(w.length() > 0){
				if(currentWord < currentLink.lastWord){
					currentString += " ";
				}
				else{ //ngram finished
					results.add(currentString);
					currentString = "";
					relevantLink = false;
					currentLink = null;
					currentWord = 0;
				}
			}
		}
	}




}
