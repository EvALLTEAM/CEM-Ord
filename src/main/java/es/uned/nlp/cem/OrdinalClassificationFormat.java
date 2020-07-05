package es.uned.nlp.cem;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.csvreader.CsvReader;
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
 * <p>The Ordinal Classification task uses as input a 3 column tsv format <strong>without headers</strong>, where the first column represents the TEST CASE, the second column represents 
 * the ID of the item and the third column represents the ORDINAL VALUE assigned to the item. Notice that the ORDINAL VALUES should be represented as a numeric value. Your can find
 * an example in the test/resources folder.</p>
 * 
 * <p>Notice that, in the Ordinal Classification input, duplicate ids of items at TEST CASE level are not allowed. Similarly, empty values or different number of columns are not permitted. 
 * These restrictions will produce warnings when parsing the output file (the evaluation can continue but might not be reliable). These same restrictions will produce errors when 
 * parsing the goldstandard (the process will stop until errors are solved).</p>
 *
 * <p><strong>Author</strong>: Jorge Carrillo-de-Albornoz<br>
 * <strong>Evaluation Service EvALL</strong>: <a href="http://www.evall.uned.es">www.evall.uned.es</a><br>
 * <strong>EvALL source code</strong>: <a href="https://github.com/EvALLTEAM/EvALLToolkit">GitHub Repository</a><br>
 * <strong>Copyright (c) 2020 </strong>- Permission is granted for use and modification of this file for research, non-commercial purposes.<br></p> 
 */

public class OrdinalClassificationFormat
{
	private boolean isGold;
	private String pathFile;
	private boolean stop= false;
	private HashMap<String, Integer> frecuencyOfClasses = new HashMap<String,Integer>();

	/**
	 * Contains the list of data for different test cases.  
	 * 				Topic			id				ordinalValue
	 * */
	private HashMap<String, HashMap<String,String>> tableOfTopics = new HashMap<String, HashMap<String,String>>();
		
	public boolean isGold() 
	{
		return isGold;
	}

	public String getPathFile()
	{
		return pathFile;
	}	
	
	public boolean isStop() 
	{
		return stop;
	}

	public void parseFile(boolean isGold, String pathFile)
	{
		this.isGold = isGold;
		this.pathFile = pathFile;
		try 
		{
			InputStream streamOutput = new FileInputStream(pathFile);
			if(streamOutput!=null)
			{	
				CsvReader reader = new CsvReader(new InputStreamReader(streamOutput, Charset.forName(StandardCharsets.UTF_8.displayName())), '\t');		
				parserInternal(reader); 
				reader.close();
			}           
		} 
		catch (FileNotFoundException e1)
		{
			System.out.println("File not found: " + pathFile);
		}		
	}
	
	private void parserInternal(CsvReader reader)
	{
		System.out.println("Parsing file " + this.pathFile);
		reader.setUseTextQualifier(true);
        reader.setTextQualifier('\"');
        int inLine = 0;
        int rowWithNo3Columns = 0;            
        try
        {
            while(reader.readRecord())
            {
            	inLine++;
            	String[] record = reader.getValues();  
            	if(record.length!=3)
            	{
            		if(this.isGold())
            		{
            			System.out.println("Format error: the number of columns must be 3. Line " + inLine);
    	            	rowWithNo3Columns++;
    	            	stop=true;
    	            	continue;
    	            	
            		}
            		else
            		{
	            		System.out.println("Format warning: the number of columns must be 3. Line " + inLine);
	            		rowWithNo3Columns++;
	            		continue;	
            		}
            	}	            	
            	
            	String topic = record[0];
            	String id = record[1];
            	String value = record[2];
            	
            	if(topic.equalsIgnoreCase("") || id.equalsIgnoreCase("") || value.equalsIgnoreCase(""))
            	{
            		if(this.isGold())
            		{
            			System.out.println("Format error: the columns in the rows cannot be empty. Line " + inLine);
            			stop=true;
    	            	continue;
            		}
            		else
            		{
            			System.out.println("Format warning: the columns in the rows cannot be empty. Line " + inLine);
	            		continue;	
            		}
            	}
            	
            	/**
            	 * Check if there are duplicated ids (not allowed in the output, permitted in the gold standard at test case level with different aspects).
            	 * */
            	if((this.getTableOfTopics().containsKey(topic))&&(this.getTableOfTopics().get(topic).containsKey(id)))
            	{
            		if(this.isGold())
            		{
            			System.out.println("Format error: this format does not allow duplicated ids at test case level. Line " + inLine);
            			stop=true;
    	            	continue;
            		}
            		else
            		{
            			System.out.println("Format warning: this format does not allow duplicated ids at test case level, EvALL will only consider the first instance. Line " + inLine);
	            		continue;	
            		}
            	}
            	
            	/**
            	 * If gold standard, check if the values are numerical.
            	 * */
        		if(!isNumeric(value))
        		{
            		if(this.isGold())
            		{
            			System.out.println("Format error: the value is not a valid number. Line " + inLine);
            			stop=true;
		            	continue;
            		}
            		else
            		{
            			System.out.println("Format warning: the value is not a valid number. Line " + inLine);
	            		continue;	
            		}
        		}

            	/**
            	 * Everything is correct and we update the tables.
            	 * */
            	if (this.getTableOfTopics().get(topic)!=null)
            	{
            		
            		HashMap<String,String> processed = this.getTableOfTopics().get(topic);
            		processed.put(id, value);
            		this.getTableOfTopics().put(topic,processed);
            	}
            	else
            	{
            		HashMap<String,String> processed = new HashMap<String,String>();
            		processed.put(id, value);
            		this.getTableOfTopics().put(topic, processed);
            	}
            	if(this.getFrecuencyOfClasses().get(value)==null)
            	{
            		this.getFrecuencyOfClasses().put(value, 1);
            	}
            	else
            	{
            		int occurrences = this.getFrecuencyOfClasses().get(value) +1;
            		this.getFrecuencyOfClasses().put(value, occurrences);
            	}		     
            }
            if(inLine==0 && !reader.readRecord())
            {
            	System.out.println("Format error: The file is empty.");
            	stop=true;
            }   
            else if(rowWithNo3Columns==inLine)
            {
            	System.out.println("Format error: The number of columns must be 3 in all lines.");
            	stop=true;
            }
        }
        catch (IOException e)
        {	        
        	System.out.println("IO error: input file not well formed.");
        	stop=true;
        } 
	}
	
	public boolean isNumeric(String str)
	{
		try 
		{
			Double.parseDouble(str);
		} 
		catch (NumberFormatException nfe) {return false;}
		return true;
	}
	
	/********************************************************************************************************************************************
	 * 
	 * 												EVALUATION
	 * 
	 *********************************************************************************************************************************************/

	public HashMap<String, HashMap<String,String>> getTableOfTopics() 
	{
		return tableOfTopics;
	}
	
	public HashMap<String, Integer> getFrecuencyOfClasses() 
	{
		return frecuencyOfClasses;
	}
	
	public List<String> getElementsClassifiedByClassAtTopicLevel(String topic, String classForSearch)
	{
		HashMap<String, String> topicsValues = this.getTableOfTopics().get(topic);
		List<String> keys = new ArrayList<String>();
	    for (Map.Entry<String, String> entry : topicsValues.entrySet()) 
	    {
	        if (entry.getValue().equalsIgnoreCase(classForSearch)) 
	        {
	            keys.add(entry.getKey());
	        }
	    }
	    return keys;		
	}	
}
