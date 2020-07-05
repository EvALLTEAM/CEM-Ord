# Closeness Evaluation Measure for Ordinal Classification (CEM-Ord)
This source implements the metric CEM-Ord presented in the paper:

     An Effectiveness Metric for Ordinal Classification: Formal Properties and Experimental Results
     Enrique Amigó, Julio Gonzalo, Stefano Mizzaro, Jorge Carrillo-de-Albornoz. In proceedings of ACL'20.

If you use this resource please cite it.

This package is also included in the Evaluation Service EvALL, along with extended features: pdf and latex reports, other ordinal metrics, statistical significance test, etc. The code of EvALL project will be released by the end of 2020 (https://github.com/EvALLTEAM/EvALLToolkit).

This source is available to evaluate a pair of goldstandard/output, and generates as output an EvALL tsv report. The input format for both files is described in the OrdinalClassificationFormat class.

The package must be invoked with 2 parameter: pathGoldStandard pathSystemOutput

     Example: java -jar CEM-Ord_EvALL-0.1.0.jar test/resources/GOLD.tsv test/resources/SYS.tsv 

		
# ORDINAL CLASSIFICATION FORMAT 

The Ordinal Classification task uses as input a 3 column tsv format without headers, where the first column represents the TEST CASE, the second column represents the ID of the item and the third column represents the ORDINAL VALUE assigned to the item. Notice that the ORDINAL VALUES should be represented as a numeric value. Your can find an example in the test/resources folder.

Notice that, in the Ordinal Classification input, duplicate ids of items at TEST CASE level are not allowed. Similarly, empty values or different number of columns are not permitted. These restrictions will produce warnings when parsing the output file (the evaluation can continue but might not be reliable). These same restrictions will produce errors when parsing the goldstandard (the process will stop until errors are solved).