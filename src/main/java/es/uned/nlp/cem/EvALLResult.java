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

public class EvALLResult 
{
	/**
	 * Contains a result for each test case (1st column EvALLFormats)
	 */
	private HashMap<String,Double> results = new HashMap<String, Double>();	
	private Double aggregatedResult = null;

	public HashMap<String, Double> getResults() 
	{
		return results;
	}

	public void setResults(HashMap<String, Double> results) 
	{
		this.results = results;
	}

	public Double getAggregatedResult()
	{
		return aggregatedResult;
	}

	public void setAggregatedResult(Double aggregatedResult)
	{
		this.aggregatedResult = aggregatedResult;
	}
	
	public void normalizeResult()
	{
		if(results.size()==0)
		{
			this.aggregatedResult = null;
		}
		else
		{
			double total =0;
			double numElems = 0;
			for (Map.Entry<String, Double> entry : results.entrySet()) 
			{ 
				if(entry.getValue()!=null)
				{
					total = total + entry.getValue();
					numElems++;
				}
			}
			if(total == 0)
			{
				this.aggregatedResult = 0.0d;
			}
			else if(numElems!= 0.0d)
			{
				this.aggregatedResult = total/numElems;
			}
		}
	}	
}
