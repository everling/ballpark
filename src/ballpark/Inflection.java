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
	
	public String inflectAdjective(String adjectiveLemma, String inflection){
		
		String toRet = "";
		WordElement word = lexicon.getWord(adjectiveLemma, LexicalCategory.ADJECTIVE);

		if(inflection.contains("kom")){
			toRet = word.getFeatureAsString(LexicalFeature.COMPARATIVE);
			if (toRet != null)
				return toRet;
			else
				return "more " +adjectiveLemma;
		}
		return 	adjectiveLemma;
	}
	
	
	public String inflectNoun(String nounLemma, String inflection){
		
		String toRet = "";
		WordElement word = lexicon.getWord(nounLemma, LexicalCategory.NOUN);

		
		if(inflection.contains("def")){
			
			if(inflection.contains("plu")){
				InflectedWordElement pluralWord = new InflectedWordElement(word);
				pluralWord.setPlural(true);
				toRet = realiser.realise(pluralWord).toString();
			}
			if(toRet != null)
				return "the " + toRet;
		}
		return 	nounLemma;
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
		if(inflection.startsWith("jj"))
			return inflectAdjective(lemma,inflection);
		if(inflection.startsWith("nn"))
			return inflectNoun(lemma,inflection);
		return lemma;
		
	 
	}
}
