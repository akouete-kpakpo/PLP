package cs.bigdata.Lab2;

/**
 * tf-idf 3rd reducer
 * 
 * WordsInCorpusTFIDFReducer calculates the number of documents d in corpus that a given key occurs and the TF-IDF computation.
 * The total number of D is acquired from the job name the driver.
 * OUTPUT : (word@docname , [d/D, n/N, TF-IDF])
 * 
 * @author Kpakpo Akouete, Amine Belhaj, Darnel Hossie
 */

import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

	
public class WordsInCorpusTFIDFReducer extends Reducer<Text, Text, Text, Text> {
	 
    private static final DecimalFormat DF = new DecimalFormat("###.########");
 
    /**
     * @param key is the key of the mapper
     * @param values are all the values aggregated during the mapping phase
     * @param context contains the context of the job run
     *
     *  PRECONDITION: receive a list of <word, ["doc1=n1/N1", "doc2=n2/N2"]>
     *  POSTCONDITION: <"word@doc1,  [d/D, n/N, TF-IDF]">
     */
    
    @Override
    protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
        // get the number of documents indirectly from the file-system (stored in the job name on purpose)
        int numberOfDocumentsInCorpus = Integer.parseInt(context.getJobName());
        // total frequency of this word
        int numberOfDocumentsInCorpusWhereKeyAppears = 0;
        Map<String, String> tempFrequencies = new HashMap<String, String>();
       
        for (Text val : values) {
            String[] documentAndFrequencies = val.toString().split("=");
            numberOfDocumentsInCorpusWhereKeyAppears++;
            tempFrequencies.put(documentAndFrequencies[0], documentAndFrequencies[1]);
        }
        
        for (String document : tempFrequencies.keySet()) {
            String[] wordFrequenceAndTotalWords = tempFrequencies.get(document).split("/");
 
            //Term frequency is the quocient of the number of terms in document and the total number of terms in doc
            double tf = Double.valueOf(Double.valueOf(wordFrequenceAndTotalWords[0])
                    / Double.valueOf(wordFrequenceAndTotalWords[1]));
 
            //interse document frequency quocient between the number of docs in corpus and number of docs the term appears
            double idf = (double) numberOfDocumentsInCorpus / (double) numberOfDocumentsInCorpusWhereKeyAppears;
 
            //given that log(10) = 0, just consider the term frequency in documents
            double tfIdf = numberOfDocumentsInCorpus == numberOfDocumentsInCorpusWhereKeyAppears ?
                    tf : tf * Math.log10(idf);
 
            //context.write(new Text(key + "@" + document), new Text("[" + numberOfDocumentsInCorpusWhereKeyAppears + "/"
            //        + numberOfDocumentsInCorpus + " , " + wordFrequenceAndTotalWords[0] + "/"
            //        + wordFrequenceAndTotalWords[1] + " , " + DF.format(tfIdf) + "]"));
            context.write(new Text(key + "@" + document), new Text(DF.format(tfIdf)));
        }
    }
}

