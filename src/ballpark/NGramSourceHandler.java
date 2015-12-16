package ballpark;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class NGramSourceHandler extends DefaultHandler{
	
	String[] words;
	int accepted = 0;
	String link = "";
	boolean wordB = false;
	
	List<Res> results;
	
	//indexes
	int first = 1000; 
	int last = -1;
	String wlinkAtt;
	
	
	public NGramSourceHandler(String[] words, List<Res> results){
		this.words = words;
		this.results = results;
	}

	public void startElement(String uri, String localName,String qName, Attributes attributes) throws SAXException {


		if (qName.equalsIgnoreCase("LINK")) {
			link = 	attributes.getValue("id");

		}
		if (qName.equals("w")) {
			wordB = true;
			wlinkAtt = attributes.getValue("wordlink-en");
		}

	}

	public void endElement(String uri, String localName,String qName) throws SAXException {

	}
	
	public List<Integer> wordIndexes(){
		String[] split = wlinkAtt.split("\\|");
		List<Integer> ret = new ArrayList<Integer>();
		
		for(String s : split){
			if(s.length() > 0){
				try{
					Integer i = Integer.parseInt(s);
					ret.add(i);
				}
					catch(NumberFormatException e){
					}
				}
			}
		return ret;

	}
		
		
	

	public void acceptWord(){
		accepted++;
		
		for(Integer i : wordIndexes()){
			if(i > last)
				last = i;
			if(i < first)
				first = i;
		}
		
		if(accepted == words.length){
			if(last > -1 && first < 1000){
				Res r = new Res(link,first,last);
				results.add(r);
			}
			reset();
		}
	}
	
	public void reset(){
		accepted = 0;
		first = 1000;
		last = -1;
	}
	
	public void characters(char ch[], int start, int length) throws SAXException {

		if (wordB) {
			String w = new String(ch, start, length);
			if(w.equalsIgnoreCase(words[accepted])){
				acceptWord();
			}
			else{
				reset();
				if(w.equalsIgnoreCase(words[accepted])){
					acceptWord();
				}

			}
			wordB = false;
		}
	}

	public class Res{
		
		String linkID;
		int firstWord;
		int lastWord;
		
		public Res(String linkID, int firstWord, int lastWord){
			this.linkID = linkID;
			this.firstWord = firstWord;
			this.lastWord = lastWord;
		}
		
	}
}
