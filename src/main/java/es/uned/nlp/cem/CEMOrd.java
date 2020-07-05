package es.uned.nlp.cem;

import java.util.HashMap;
import java.util.Map;
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
 *
 * <p><strong>Author</strong>: Jorge Carrillo-de-Albornoz<br>
 * <strong>Evaluation Service EvALL</strong>: <a href="http://www.evall.uned.es">www.evall.uned.es</a><br>
 * <strong>EvALL source code</strong>: <a href="https://github.com/EvALLTEAM/EvALLToolkit">GitHub Repository</a><br>
 * <strong>Copyright (c) 2020 </strong>- Permission is granted for use and modification of this file for research, non-commercial purposes.<br></p> 
 */

public class CEMOrd
{
	private ConfusionMatrix confusionMatrix;
	private OrdinalClassificationFormat goldStandard;
	private OrdinalClassificationFormat output;
	private String name = "CEM-Ord";
	private EvALLResult result = new EvALLResult();
	
	public  CEMOrd(OrdinalClassificationFormat gold, OrdinalClassificationFormat output)
	{
		this.goldStandard = gold;
		this.output = output;
		this.confusionMatrix = new ConfusionMatrix();
		this.confusionMatrix.generateConfusionMatrix(this.output, this.goldStandard);
	}

	/**
	 * Method that evaluates a system output with a gold standard, both in a OrdinalClassificationFormat object, using the CEM-Ord measure
	 */
	public void evaluate() 
	{
		/**
		 * Evaluate according to the test case present in the gold. Calculate each result, and average over them.
		 * */
		for (Map.Entry<String, HashMap<String, String>> entry : this.goldStandard.getTableOfTopics().entrySet()) 
		{ 
			String topic = entry.getKey();
			HashMap<String, String> valuesGold = entry.getValue();
			HashMap<String, String> valuesOutput = this.output.getTableOfTopics().get(topic);
			
			Double cemOrd = 0.0d;
			double sumNumerator = 0.0d;
			double sumDenominator = 0.0d;
			/**
			 * For each test case in the gold check it in output.
			 * */
			if(valuesOutput!=null)
			{
				/**
				 * For each itme calculate the proximity for each element in the gold.
				 * */
				for(Map.Entry<String, String> entry2: valuesGold.entrySet())
				{
					String idGold = entry2.getKey();
					String classGold = entry2.getValue();
					/**
					 * If the item does not exist in the output the proximity is 0. 
					 * */
					if(valuesOutput.get(idGold)!=null)
					{
						String classOutput = valuesOutput.get(idGold);
						sumNumerator+= this.confusionMatrix.proximityCEM(topic, classOutput, classGold);						
					}
					sumDenominator+= this.confusionMatrix.proximityCEM(topic, classGold, classGold);
				}	
				if(sumDenominator!=0.0d)
				{
					cemOrd = sumNumerator/sumDenominator;	
				}
			}
			this.getResult().getResults().put(topic, cemOrd);
		}		
	}
	
	public EvALLResult getResult() 
	{
		return result;
	}

	public String getName() 
	{
		return name;
	}
}
