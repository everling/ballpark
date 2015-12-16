package ballpark;

import simplenlg.framework.*;
import simplenlg.lexicon.*;
import simplenlg.realiser.english.*;
import simplenlg.phrasespec.*;
import simplenlg.features.*;

public class Inflection {
	
	Lexicon lexicon;
	NLGFactory nlgFactory;
	Realiser realiser;
	public Inflection(){
	    lexicon = Lexicon.getDefaultLexicon();
	    nlgFactory = new NLGFactory(lexicon);
	    realiser = new Realiser(lexicon);

	}
	
	
	public String inflectVerb(String verbLemma, String inflection){
		   SPhraseSpec p = nlgFactory.createClause();
		    p.setVerb(verbLemma);
		    
		    //decide tense
		    String[] params = inflection.split("\\.");

		    if(params.length < 2)
		    	return verbLemma;
		    
		    Tense t = Tense.PRESENT;
		    switch(params[1]){
		    	case "prt":
		    		t = Tense.PAST;
		    		break;
		    	case "sup":
		    		t = Tense.PAST;
		    		break;	
		    }
		    
		    p.setFeature(Feature.TENSE,t);
		    String sentence = realiser.realiseSentence(p);
		    return sentence.toLowerCase().substring(0, sentence.length()-1);
	}
	
	public String inflect(String lemma, String inflection){
		if(inflection.startsWith("vb"))
			return inflectVerb(lemma,inflection);
		return lemma;
		
	 
	}
}
