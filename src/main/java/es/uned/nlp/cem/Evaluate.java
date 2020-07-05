package es.uned.nlp.cem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import com.csvreader.CsvWriter;

/**
 * 
 * <p>This source implements the metric CEM-Ord presented in the paper:<br><br>
 * 			&nbsp;&nbsp;&nbsp;&nbsp;<strong> An Effectiveness Metric for Ordinal Classification: Formal Properties and Experimental Results</strong><br> 	
 * 			&nbsp;&nbsp;&nbsp;&nbsp; Enrique Amigó, Julio Gonzalo, Stefano Mizzaro, Jorge Carrillo-de-Albornoz. In proceedings of ACL'20.</p>
 * 
 * <p>If you use this resource please cite it.</p>
 * 
 * <p>This package is also included in the <strong>Evaluation Service EvALL</strong>, along with extended features: pdf and latex reports,
 * other ordinal metrics, statistical significance test, etc. The code of EvALL project will be released by the end of 2020 (https://github.com/EvALLTEAM/EvALLToolkit).</p>
 * 
 * <p>This source is available to evaluate a pair of goldstandard/output, and generates as output an EvALL tsv report. 
 * The input format for both files is described in the OrdinalClassificationFormat class.</p>
 * 
 * 
 * <p>The package must be invoked with 2 parameter: <i>pathGoldStandard</i> <i>pathSystemOutput</i><br>
 * 
 * 			&nbsp;&nbsp;&nbsp;&nbsp; Example: java -jar CEM-Ord_EvALL-0.1.0.jar test/resources/GOLD.tsv test/resources/SYS.tsv </p>
 *
 * <p><strong>Author</strong>: Jorge Carrillo-de-Albornoz<br>
 * <strong>Evaluation Service EvALL</strong>: <a href="http://www.evall.uned.es">www.evall.uned.es</a><br>
 * <strong>EvALL source code</strong>: <a href="https://github.com/EvALLTEAM/EvALLToolkit">GitHub Repository</a><br>
 * <strong>Copyright (c) 2020 </strong>- Permission is granted for use and modification of this file for research, non-commercial purposes.<br></p> 
 */

public class Evaluate 
{
    /**
     * <p>The package must be invoked with 2 parameter: <i>pathGoldStandard</i> <i>pathSystemOutput</i><br>
     * 
     * 			&nbsp;&nbsp;&nbsp;&nbsp; Example: java -jar CEM-Ord_EvALL-0.1.0.jar test/resources/GOLD.tsv test/resources/SYS.tsv </p>
	 * 
     * @param args Parameters
     */
    public static void main( String[] args )
    {
    	if(args.length!=2)
    	{
    		System.out.println("The number of parameters must be 2: Java CEM-Ord pathGoldStandard pathSystemOutput \n"
    				+ "Example: java -jar CEM-Ord_EvALL-0.1.0.jar test/resources/GOLD.tsv test/resources/SYS.tsv");
    		System.exit(0);
    	}
    	
    	String goldStandardFile = args[0];
    	String outputFile = args[1];
    	
    	if(goldStandardFile==null || goldStandardFile.equalsIgnoreCase(""))
		{
    		System.out.println("The name of the gold standard file cannot be empty");
    		System.exit(0);
		}
	
		if(outputFile==null || outputFile.equalsIgnoreCase(""))
		{
			System.out.println("The name of the system output file cannot be empty");
    		System.exit(0);
		}
    	
    	
		/**
		 * Check the gold standard for errors. Errors stop the analysis.
		 * */
    	OrdinalClassificationFormat gold = new OrdinalClassificationFormat();
		gold.parseFile(true, goldStandardFile);
    	if(gold.isStop())
    	{
    		System.exit(0);
    	}

		
		/**
		 * Check the system output for errors/warnings.
		 * */
		OrdinalClassificationFormat output = new OrdinalClassificationFormat();
    	output.parseFile(false, outputFile);
    	if(output.isStop())
    	{
    		System.exit(0);
    	}
		
		CEMOrd CEMOrd = new CEMOrd(gold, output);
		CEMOrd.evaluate();
		generateSingleTSVFileForOneOutput(output, gold, CEMOrd);
		
    }
    
    
	/**
	 * Method that writes the EvALL tsv report
	 * 
	 * @param output	System output DiversificationFormat object
	 * @param gold		Gold Standard DiversificationFormat object
	 * @param CEMOrd	Measure CEM-Ord object
	 */
	public static void generateSingleTSVFileForOneOutput(OrdinalClassificationFormat output, OrdinalClassificationFormat gold, CEMOrd CEMOrd)
	{
		
		File outputFile = new File("RESULTS.tsv");			
		try
		{

			CsvWriter csvOutput = new CsvWriter(new FileOutputStream(outputFile, false), '\t', Charset.forName(StandardCharsets.UTF_8.displayName()));
			csvOutput.setTextQualifier('\"');
			csvOutput.setUseTextQualifier(true);
			csvOutput.setForceQualifier(true);
			
			/**
			 * Headers for EvALL tsv report
			 * */
				
				csvOutput.writeComment("############################################################################");
				csvOutput.writeComment("\t\t\t\tAUTOMATIC EvALL TSV REPORT\n#\n#\tWe kindly ask you to cite the following work when using EvALL:\n"
						+ "#\t\t\tAn Effectiveness Metric for Ordinal Classification: Formal Properties and Experimental Results\n"
						+ "#\t\t\tEnrique Amigó, Julio Gonzalo, Stefano Mizzaro, Jorge Carrillo-de-Albornoz\n"
						+ "#\t\t\tIn proceedings of ACL'20\n"
						+ "#");

				csvOutput.writeComment("\tThis file contains the results for the output: ");		

				String originalName = output.getPathFile();
				String mix =  MessageFormat.format("\t\t\t\u2022 {0}", originalName);
				csvOutput.writeComment(mix);			
				csvOutput.writeComment("");
				csvOutput.writeComment("\tThe next table contains the results for each test case in this output. \n"
						+ "#\tNotice that first are shown the test cases present in the gold, and after that those not present. \n"
						+ "#\tThose measures that do not satisfy the preconditions are marked with -.");
				csvOutput.writeComment("############################################################################");
				csvOutput.writeComment("The measures included in the table are:");
				csvOutput.writeComment("\t\t- " + CEMOrd.getName());

				csvOutput.writeComment("############################################################################");
				
				
				String title[] = new String[2];
				title[0] = "Test Case";
				title[1] = CEMOrd.getName();
				csvOutput.writeRecord(title);


				/**
				 * First we check the test case of the gold standard.
				 * */
				for (Map.Entry<String, HashMap<String, String>> entry : gold.getTableOfTopics().entrySet()) 
				{ 
					String topic = entry.getKey();
					String record[] = new String[2];
					record[0] = topic;
					if(CEMOrd.getResult().getResults().get(topic)!=null)
					{
						record[1] = String.format("%.4f",CEMOrd.getResult().getResults().get(topic));
					}
					else
					{
						record[1] = "-";
					}
					csvOutput.writeRecord(record);
				}		
				
				/**
				 * Then we check the test case of the output and write those not present in the gold standard.
				 * */
				for (Map.Entry<String, HashMap<String, String>> entry : output.getTableOfTopics().entrySet()) 
				{ 
					String topic = entry.getKey();
					if(gold.getTableOfTopics().get(topic)!=null)
					{
						continue;
					}
					String record[] = new String[2];
					record[0] = topic;
					if(CEMOrd.getResult().getResults().get(topic)!=null)
					{
						record[1] = String.format("%.4f",CEMOrd.getResult().getResults().get(topic));
					}
					else
					{
						record[1] = "-";
					}
					csvOutput.writeRecord(record);
				}			
			csvOutput.close();
		} 
		catch (IOException e) {e.printStackTrace();}
	}
}
