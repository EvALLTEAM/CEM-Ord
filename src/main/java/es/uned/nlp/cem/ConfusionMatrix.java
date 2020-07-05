package es.uned.nlp.cem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeSet;

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
 * <p>This class generates the confusion matrix used by all classification measures in EvALL (most of them are out of scope of CEM-Ord). The distribution of the confusion matrix is:<br>
 * 
 * 
 * 			<table border="1" summary="Confusion Matrix">
 * 				<tr>
 * 					<td></td><td>Predicted A</td><td>Predicted B</td><td>Predicted C</td>
 * 				</tr>
 * 				<tr>	
 * 					<td>Gold A</td><td></td><td></td><td></td>
 *  			</tr>
 * 				<tr>
 * 					<td>Gold B</td><td></td><td></td><td></td>
 * 				</tr>
 * 				<tr> 
 * 					<td>Gold C</td><td></td><td></td><td></td>
 * 				</tr>
 * 			</table>
 * 
 *
 * <p><strong>Author</strong>: Jorge Carrillo-de-Albornoz<br>
 * <strong>Evaluation Service EvALL</strong>: <a href="http://www.evall.uned.es">www.evall.uned.es</a><br>
 * <strong>EvALL source code</strong>: <a href="https://github.com/EvALLTEAM/EvALLToolkit">GitHub Repository</a><br>
 * <strong>Copyright (c) 2020 </strong>- Permission is granted for use and modification of this file for research, non-commercial purposes.<br></p> 
 */

public class ConfusionMatrix 
{
	private HashMap<String, int[][]> confusionMatrix = new HashMap<String, int[][]>();
	private HashMap<String, HashMap<String, Integer>> indexClass = new HashMap<String, HashMap<String, Integer>>();	
	private HashMap<String, HashMap<String, Integer>> frecuencyClassesInGoldPerTopic = new HashMap<String, HashMap<String,Integer>>();
	private HashMap<String, HashMap<String, Integer>> frecuencyClassesInOutputPerTopic = new HashMap<String, HashMap<String,Integer>>();	
	
	public void generateConfusionMatrix(OrdinalClassificationFormat output, OrdinalClassificationFormat gold)
	{
		for (Map.Entry<String, HashMap<String, String>> entry : gold.getTableOfTopics().entrySet()) 
		{ 
			String topic = entry.getKey();
			HashMap<String, String> goldValues = entry.getValue();
			HashMap<String, String> outputValues = output.getTableOfTopics().get(topic);
			
			this.indexClass.put(topic, new HashMap<String, Integer>());
			this.frecuencyClassesInGoldPerTopic.put(topic, new HashMap<String, Integer>());
			this.frecuencyClassesInOutputPerTopic.put(topic, new HashMap<String, Integer>());
			
			parseConfusionMatrixForTopic(topic, goldValues, outputValues);
		}
	}
	
	private void parseConfusionMatrixForTopic(String topic, HashMap<String, String> goldValues, HashMap<String, String> outputValues)
	{
		int[][] confMat = identifyGoldClassesAndCalculateTheirFrequency(topic, goldValues);
		identifyOutputClassesAndCalculateTheirFrequency(topic, outputValues);
		
		if(outputValues!=null)
		{
			for (Map.Entry<String, String> entry : goldValues.entrySet()) 
			{
				String id = entry.getKey();
				String goldValue = entry.getValue();	
				//If the output does not contains the id we ignore it for the confusion matrix.
				if(outputValues.get(id)!=null)
				{
					String outputValue = outputValues.get(id);
					
					int posGold = this.indexClass.get(topic).get(goldValue);
					//If the output value does not exist in the gold we ignore it for the confusion matrix.
					if(this.indexClass.get(topic).get(outputValue)!=null)
					{
						int posOutput = this.indexClass.get(topic).get(outputValue);
						int occurrences = confMat[posGold][posOutput] +1;
						confMat[posGold][posOutput]= occurrences;
					}
				}
			}
		}
		this.confusionMatrix.put(topic, confMat);
	}
	
	private int[][] identifyGoldClassesAndCalculateTheirFrequency(String topic, HashMap<String, String> goldValues)
	{
		int numClassesInGold = 0;
		for (Map.Entry<String, String> entry : goldValues.entrySet()) 
		{
			String goldValue = entry.getValue();
			//Checks if the class exist in index of classes for the given topic
			if(this.indexClass.get(topic).get(goldValue)==null)
			{
				int size = this.indexClass.get(topic).size();
				this.indexClass.get(topic).put(goldValue, size);
				numClassesInGold++;
			}	
			if(this.frecuencyClassesInGoldPerTopic.get(topic).get(goldValue)==null)
			{
				this.frecuencyClassesInGoldPerTopic.get(topic).put(goldValue, new Integer(1));
			}
			else
			{
				int occurences = this.frecuencyClassesInGoldPerTopic.get(topic).get(goldValue) + 1;
				this.frecuencyClassesInGoldPerTopic.get(topic).put(goldValue, occurences);
			}
		}
		return new int[numClassesInGold][numClassesInGold];
	}	
	
	private void identifyOutputClassesAndCalculateTheirFrequency(String topic, HashMap<String, String> outputValues)
	{
		if(outputValues!=null)
		{
			for (Map.Entry<String, String> entry : outputValues.entrySet()) 
			{
				String outputValue = entry.getValue();
				if(this.frecuencyClassesInOutputPerTopic.get(topic).get(outputValue)==null)
				{
					this.frecuencyClassesInOutputPerTopic.get(topic).put(outputValue, new Integer(1));
				}
				else
				{
					int occurences = this.frecuencyClassesInOutputPerTopic.get(topic).get(outputValue) + 1;
					this.frecuencyClassesInOutputPerTopic.get(topic).put(outputValue, occurences);
				}
			}
		}
	}

	public String getClassName(String topic, int index)
	{
		for (Map.Entry<String, Integer> entry : this.indexClass.get(topic).entrySet()) 
		{
			if(entry.getValue().intValue()==index)
			{
				return entry.getKey();
			}
		}
		return null;
	}
	
	public int getIndexByClassNameForTopic(String topic, String className)
	{
		for (Map.Entry<String, Integer> entry : this.indexClass.get(topic).entrySet()) 
		{
			if(entry.getKey().equalsIgnoreCase(className))
			{
				return entry.getValue();
			}
		}
		return -1;
	}
	
	public int getDiagonalForAccuracy(String topic)
	{
		int diagonal = 0;
		int[][] confMat = this.confusionMatrix.get(topic);
		for(int i= 0; i< confMat.length;i++)
		{
			for(int j =0; j<confMat[i].length;j++)
			{
				if(i==j)
				{
					diagonal = diagonal + confMat[i][j];
				}
			}
		}
		return diagonal;
	}
	
	public int getNumberInstancesInGold(String topic)
	{
		HashMap<String, Integer> instancesInGold = this.frecuencyClassesInGoldPerTopic.get(topic);
		int numInstances = 0;
		for (Map.Entry<String, Integer> entry : instancesInGold.entrySet()) 
		{
			numInstances = numInstances + entry.getValue();
		}
		return numInstances;		
	}
	
	public int getDiagonalForClass(String topic, int indexClass)
	{
		return this.confusionMatrix.get(topic)[indexClass][indexClass];
	}
	
	public int getAntiDiagonalForClassInMatrix2x2(String topic, int indexClass)
	{
		int indexOutput = this.confusionMatrix.get(topic).length-1-indexClass; 
		return this.confusionMatrix.get(topic)[indexClass][indexOutput];
	}
	
	public int getNumberInstancesPerClassInGold(String topic, int indexClass)
	{
		String className = this.getClassName(topic, indexClass);
		return this.frecuencyClassesInGoldPerTopic.get(topic).get(className);
	}
	
	public int getNumberInstancesPerClassOutput(String topic, int indexClass)
	{
		String className = this.getClassName(topic, indexClass);
		if(this.frecuencyClassesInOutputPerTopic.get(topic)!=null)
		{
			if(this.frecuencyClassesInOutputPerTopic.get(topic).get(className)!=null)
			{
				return this.frecuencyClassesInOutputPerTopic.get(topic).get(className);
			}
		}
		return 0;
	}
	
	public int getNumberInstancesPerClassOutputInConfusionMatrix(String topic, int indexClass)
	{
		int totalPredictedForPosClass = 0;
		int[][] confMat = this.confusionMatrix.get(topic);
		for(int i=0;i<confMat.length;i++)
		{
			totalPredictedForPosClass = totalPredictedForPosClass + confMat[i][indexClass];
		}		
		return totalPredictedForPosClass;
	}
	
	public int getPosInMatrix(String topic, int indexGold, int indexOutput)
	{
		int[][] confMat = this.confusionMatrix.get(topic);
		return confMat[indexGold][indexOutput];
	}
	
	public int getMajorityClassInGold(String topic)
	{
		HashMap<String, Integer> instancesInGold = this.frecuencyClassesInGoldPerTopic.get(topic);
		int mayorityClass = 0;
		for (Map.Entry<String, Integer> entry : instancesInGold.entrySet()) 
		{
			int instancesClass = entry.getValue();
			if(instancesClass>mayorityClass)
			{
				mayorityClass = instancesClass;
			}
		}
		return mayorityClass;
	}
	
	public int getFailuresForOutputInClassFromConfusionMatrix(String topic, int indexClass)
	{
		int fails = 0;
		int[][] confMat = this.confusionMatrix.get(topic);
		for(int i= 0; i< confMat.length;i++)
		{
			if(i!=indexClass)
			{
				fails = fails + confMat[i][indexClass];
			}
		}
		return fails;
	}
	
	public int getFailuresForOutputInClassFromOutputFrecuency(String topic, int indexClass)
	{
		int fails = 0;
		int intersectionTRUE = getDiagonalForClass(topic, indexClass);
		int outputTrue = this.getNumberInstancesPerClassOutput(topic, indexClass);
		fails = Math.abs(outputTrue -intersectionTRUE);
		return fails;
	}
	
	/**
	 * 				Predicted A	Predicted B	Predicted C
	 * Gold A	
	 * Gold B
	 * Gold C
	 * 
	 * That is, precision is the fraction of events where we correctly declared ii out of all instances where the algorithm declared ii. 
	 * **/	
	public Double getPrecisionForClass(String topic, int posClass)
	{
		int[][] confMat = this.confusionMatrix.get(topic);
		double truePositiveForPosClass = confMat[posClass][posClass];
		double totalPredictedForPosClass = this.getNumberInstancesPerClassOutput(topic, posClass);
		if(totalPredictedForPosClass==0)
		{
			return null;
		}
		double result = truePositiveForPosClass/totalPredictedForPosClass;		
		return result;
	}
	
	/**
	 * 				Predicted A	Predicted B	Predicted C
	 * Gold A	
	 * Gold B
	 * Gold C
	 * 
	 * Conversely, recall is the fraction of events where we correctly declared ii out of all of the cases where the true of state of the world is ii.
	 * **/	
	public double getRecallForClass(String topic, int posClass)
	{
		int[][] confMat = this.confusionMatrix.get(topic);
		double truePositiveForPosClass = confMat[posClass][posClass];
		double totalTPForPosClass = this.getNumberInstancesPerClassInGold(topic, posClass);
		if(totalTPForPosClass==0)
		{
			return 0.0d;
		}
		double result = truePositiveForPosClass/totalTPForPosClass;
		return result;
	}	
	
	public int[] getOrderedClassesBetweenTwoClasses(String topic, String ciClass, String cjClass)
	{
		/**
		 * Order classes an get a subset.
		 * */
		NavigableSet<String> classes = new TreeSet<String>(new Comparator<String>()
		{
		    public int compare(String one, String other) 
		    {
		    	Double val1 = Double.parseDouble(one);
		    	Double val2 = Double.parseDouble(other);
	            return val1.compareTo(val2);
		    }

		});
		
		//Add both, gold and output classes, to generate the ordinal index
		classes.addAll(new ArrayList<>(this.indexClass.get(topic).keySet()));
		classes.addAll(new ArrayList<>(this.frecuencyClassesInOutputPerTopic.get(topic).keySet()));
		
		//Check order range and discard first or last element
		String begin = ciClass;
		String end = cjClass;		
		boolean includeBegin = false;
		boolean includeEnd = false;
		if(Double.parseDouble(ciClass)<Double.parseDouble(cjClass))
		{
			includeEnd = true;			
		}
		else
		{
			begin= cjClass;
			end = ciClass;
			includeBegin = true;
		}
		//Get the subset
		NavigableSet<String> subset = classes.subSet(begin, includeBegin, end, includeEnd);
		
		//Get index in the matrix of the subset. If class does not exist in the gold index is -1
		Iterator<String> itr = subset.iterator(); 
		int[] subsetClasses = new  int[subset.size()];
		int i=0;
        while (itr.hasNext()) 
        { 
        	subsetClasses[i]= this.getIndexByClassNameForTopic(topic, itr.next());
           i++;
        } 
        return subsetClasses;
	}
	
	public double proximityCEM(String topic, String ciClass, String cjClass)
	{
		double itemsInGold = this.getNumberInstancesInGold(topic);
		double itemsGoldClassCi= 0.0d;
		//if class exist in gold
		if(this.getIndexByClassNameForTopic(topic, ciClass)!=-1)
		{
			itemsGoldClassCi = this.getNumberInstancesPerClassInGold(topic, this.getIndexByClassNameForTopic(topic, ciClass));
		}

		double sumItemsClasses = 0.0d;
		if(!ciClass.equalsIgnoreCase(cjClass))
		{
			int[] subsetClasses = this.getOrderedClassesBetweenTwoClasses(topic, ciClass, cjClass);
			for(int i=0;i<subsetClasses.length;i++)
			{
				//if class exist in gold, otherwise sum+0
				if(subsetClasses[i]!=-1)
				{
					sumItemsClasses+=  this.getNumberInstancesPerClassInGold(topic, subsetClasses[i]);
				}
			}
		}
		
		double proximity = 0.0d;
		if(itemsInGold!=0.0d)
		{
			proximity = ((itemsGoldClassCi/2) + sumItemsClasses)/itemsInGold;
		}
		if(proximity>0.0d)
		{
			proximity = -1*Math.log10(proximity)/Math.log10(2);
		}
		return proximity;
	}	
}
