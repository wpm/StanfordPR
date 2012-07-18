package wpmcn;

import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.util.Triple;
import gate.AnnotationSet;
import gate.Factory;
import gate.Resource;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.util.InvalidOffsetException;

import java.io.IOException;
import java.util.List;

@CreoleResource(name = "Stanford NER", comment = "Stanford named entity recognizer")
public class StanfordNERPR extends AbstractLanguageAnalyser {
   private CRFClassifier namedEntityRecognizer;

   private String model;

   @Override
   public Resource init() throws ResourceInstantiationException {
      try {
         namedEntityRecognizer = CRFClassifier.getClassifier(getModel());
      } catch (IOException e) {
         throw new ResourceInstantiationException(e);
      } catch (ClassNotFoundException e) {
         throw new ResourceInstantiationException(e);
      }
      return this;
   }

   @Override
   public void execute() throws ExecutionException {
      AnnotationSet outputAnnotationSet = document.getAnnotations();
      String text = document.getContent().toString();
      @SuppressWarnings({"unchecked"})
      List<Triple<String, Integer, Integer>> annotations = namedEntityRecognizer.classifyToCharacterOffsets(text);
      for (Triple<String, Integer, Integer> annotation : annotations) {
         String type = annotation.first();
         long beginIndex = annotation.second();
         long endIndex = annotation.third();
         try {
            outputAnnotationSet.add(beginIndex, endIndex, type, Factory.newFeatureMap());
         } catch (InvalidOffsetException e) {
            throw new ExecutionException(e);
         }
      }
   }

   public String getModel() {
      return model;
   }

   @CreoleParameter(comment = "NER model", defaultValue = "english.all.3class.distsim.crf.ser.gz")
   public void setModel(String model) {
      this.model = model;
   }

   static public void main(String[] args) throws ResourceInstantiationException {
      StanfordNERPR ner = new StanfordNERPR();
      ner.setModel(args[0]);
      ner.init();
   }
}
