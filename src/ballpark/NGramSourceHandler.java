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
	String wlinkAtt;
	List<Integer> indexes = new ArrayList<Integer>();
	
	
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
			if(!indexes.contains(i))
				indexes.add(i);
		}
		
		if(accepted == words.length){
			if(indexes.size() > 0){
				Res r = new Res(link,indexes);
				results.add(r);
			}
			reset();
		}
	}
	
	public void reset(){
		accepted = 0;
		indexes = new ArrayList<Integer>();
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
		//int firstWord;
		int lastWord;
		List<Integer> indexes;
		
		public Res(String linkID, List<Integer> indexes){
			this.linkID = linkID;
			//this.firstWord = firstWord;
			//this.lastWord = lastWord;
			this.indexes = indexes;
			
			lastWord = 0;
			for(Integer i : indexes){
				if(i > lastWord)
					lastWord = i;
			}
			
		}
		
	}
}
