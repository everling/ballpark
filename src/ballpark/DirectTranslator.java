package ballpark;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class DirectTranslator {
	
	XPath xpath;
	InputSource inputSource;
	Inflection infl;

	
	public DirectTranslator(String fileToDictionary){

		xpath = XPathFactory.newInstance().newXPath();
		inputSource = new InputSource(fileToDictionary);	
		infl =  new Inflection();

	}

public String translateWord(String token){
	String[] tokenTreated = Ballpark.separator(token);
	String wordToPrint = tokenTreated[1];
	NodeList nodes = search(xpath,inputSource,tokenTreated[1].toLowerCase());
	List<String> transl = null;
	if(nodes.getLength() > 0){
		transl = getTranslations(nodes);
		if(transl.size() > 0){
			wordToPrint = transl.get(0);
		}
	}
	else{
		String[] lem = Ballpark.lemmatize(tokenTreated[1].toLowerCase());
		nodes = search(xpath,inputSource,lem[2]);
		transl = getTranslations(nodes);
		if(transl.size() > 0){
			wordToPrint = infl.inflect(transl.get(0), lem[1]);
		}
	}
	
	String print = tokenTreated[0] +wordToPrint +tokenTreated[2]+" ";
	
	return print;
	
	
}
	
	
public void translate(List<String> tokens){

		StringBuilder sb = new StringBuilder();
		
		for(String token : tokens){

			String print = translateWord(token);
			
			sb.append(print);
			System.out.print(print);
			if(sb.toString().length() > 150){
				sb = new StringBuilder();
				System.out.println();
			}
		}
	}


	public NodeList search(XPath xpath, InputSource inputSource, String word){
		
		String expression = "/dictionary/word[@value=\""+word+"\"]/translation/@value";
		try {
			return (NodeList) xpath.evaluate(expression, inputSource, XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public List<String> getTranslations(NodeList nodes){
		List<String> ret = new ArrayList<String>();
		for(int i = 0; i < nodes.getLength(); i++){
			String[] defs = nodes.item(i).getNodeValue().split("[,; ]");
			for(String s : defs)
				ret.add(s);
		}
		
		return ret;
	}

}
