package com.swm.service;

import java.awt.List;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

import weka.associations.Apriori;
import weka.associations.FPGrowth;
import weka.associations.ItemSet;
import weka.associations.AssociationRule;
import weka.associations.AssociationRules;
import weka.core.*;
import weka.core.Capabilities.Capability;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.pmml.jaxbbindings.Itemset;

import java.io.Serializable;
import java.util.*;

import com.swm.model.Parameters;
import com.swm.model.Rule;
import com.swm.model.SingleTransaction;
import com.swm.model.Solution;
import com.swm.model.StoreDataOuput;
import com.swm.model.StoreDataInput;
import com.test.TestAPI;


public class AssociationRuleMiner {
	
	/* 
	 * Transfer transaction data from client to server
	 */
	public StoreDataOuput storeDB(StoreDataInput input){
		ArrayList<SingleTransaction> transactionLst = new ArrayList<SingleTransaction>();
		for(int i = 0; i < input.getTransactionList().length; i++){
			transactionLst.add(input.getTransactionList()[i]);
		}
		convertArrays2ARFF(transactionLst,input.getDbName());
		
		return new StoreDataOuput("OK");
	}
	public void convertArrays2ARFF(ArrayList<SingleTransaction> transactionLst, String databaseName) {
		//Sort the list of transaction
		Collections.sort(transactionLst);
		
		//Calculate sets of distinct items and orders
		Set<String> distinctItemSet = new HashSet<String>();
		Set<String> distinctOrderSet = new HashSet<String>();
		
		for(SingleTransaction st : transactionLst) {
			distinctItemSet.add(st.getItemCode());
			distinctOrderSet.add(st.getOrderCode());
		}
		
		for(SingleTransaction st : transactionLst) {
			st.print();
		}
		
		//Hash table for items
		Hashtable<String, Integer> itemHashtable = 
	              new Hashtable<String, Integer>();
		int counter = 0;
		for(String str : distinctItemSet) {
			itemHashtable.put(str, counter);
			counter++;
		}
		
		//Write to file
		BufferedWriter bw = null;
		FileWriter fw = null;
		try {			

			fw = new FileWriter(TestAPI.ROOT_DIR + "/" + databaseName + ".arff");
			bw = new BufferedWriter(fw);
			
			//First line: @relation supermarket
			bw.write("@relation invetory_" + databaseName + "\n");

			//Attributes
			for(String item : distinctItemSet) {
				bw.write("@attribute '" + item + "' { t} \n");	
			}
			
			//Data
			bw.write("@data \n");
			
			int[] order = new int[distinctItemSet.size()];
			String lastOrderCode = " ";
			String currentOrderCode = " ";
			for(int i = 0; i <= transactionLst.size(); i++) {
				if(i < transactionLst.size()) {
					currentOrderCode = transactionLst.get(i).getOrderCode();	
				}
				
				//check for writing down a transaction
				if(lastOrderCode != " " && currentOrderCode != lastOrderCode || i == transactionLst.size()) {
					//Write more 
					String orderStr = "";
					for(int j = 0; j < order.length; j++) {
						if(order[j] == 1) {
							orderStr += "t,";
						}else {
							orderStr += "?,";
						}
					}
					
					orderStr = orderStr.substring(0, orderStr.length() - 1);
					orderStr += "\n";
					
					bw.write(orderStr);
					
					//Reinitialize 
					for(int j = 0; j < order.length; j++) {
						order[j] = 0;
					}					
				}
				
				//check for continuing build a transaction
				if(i < transactionLst.size()) {
					int index = itemHashtable.get(transactionLst.get(i).getItemCode());
					order[index] = 1;
					
					lastOrderCode = transactionLst.get(i).getOrderCode();
				}
			}
			
			System.out.println("Done");

		} catch (IOException e) {

			e.printStackTrace();

		} finally {

			try {

				if (bw != null)
					bw.close();

				if (fw != null)
					fw.close();

			} catch (IOException ex) {

				ex.printStackTrace();

			}
		}
		
	}

	
	/*
	 * Calculate association rules for an input
	 */
	public Solution getAssociationRule(String databaseName, double minSupport, double minConfidence) throws Exception {
		// Set maximum number of return rules
		int maxNumRule = 10000;
		
		// Load data
		Instances data = new Instances(new BufferedReader(new FileReader(TestAPI.ROOT_DIR + "/" + databaseName + ".arff")));

		// Build model
		Apriori model = new Apriori();		
		model.setLowerBoundMinSupport(minSupport);	
		model.setMinMetric(minConfidence);
		model.setNumRules(maxNumRule);		
		model.buildAssociations(data);
				
		// Save return rules into an object
		int ruleNum = model.getAssociationRules().getRules().size();
		
		ArrayList<Rule> ruleLst = new ArrayList<>();
		for(int i = 0; i < ruleNum; i++) {		
			
			// Premise
			String premise = model.getAssociationRules().getRules().get(i).getPremise().toString();
			premise = premise.replaceAll("=t", "");
			premise = premise.replaceAll("\\[", "");
			premise = premise.replaceAll("\\]", "");				
			
			// Consequence
			String consequence = model.getAssociationRules().getRules().get(i).getConsequence().toString();
			consequence = consequence.replaceAll("=t", "");
			consequence = consequence.replaceAll("\\[", "");
			consequence = consequence.replaceAll("\\]", "");				
			
			// Support				
			double support = (double)model.getAssociationRules().getRules().get(i).getTotalSupport()/(double)data.numInstances();
						
			// Confidence
			double confidence = model.getAssociationRules().getRules().get(i).getPrimaryMetricValue();
			
			// Lift
			double lift =  model.getAssociationRules().getRules().get(i).getMetricValuesForRule()[1];
			
			//Rule
			Rule r = new Rule(consequence, premise, support, confidence, lift);
					
			ruleLst.add(r);
		}
		
		Solution sol = new Solution(ruleNum, minSupport, minConfidence, ruleLst);
		
		return sol;
	}

	/*
	 * Suggest parameter
	 */
	public void parametersSuggest(String databaseName) throws Exception{
		
		// Set maximum number of return rules
		int maxNumRule = 10000;
		
		// Load data
		Instances data = new Instances(new BufferedReader(new FileReader(databaseName + ".arff")));

		// Build model
		Apriori model = new Apriori();		

		model.setNumRules(maxNumRule);		
		model.buildAssociations(data);
				
		// Save return rules into file
		BufferedWriter bw = null;
		FileWriter fw = null;
		try {			

			fw = new FileWriter(databaseName + ".txt");
			bw = new BufferedWriter(fw);
			
			int ruleNum = model.getAssociationRules().getRules().size();
			System.out.println("XX" + ruleNum);
			for(int i = 0; i < ruleNum; i++) {		
				
				// Premise
				String premise = model.getAssociationRules().getRules().get(i).getPremise().toString();
				premise = premise.replaceAll("=t", "");
				premise = premise.replaceAll("\\[", "");
				premise = premise.replaceAll("\\]", "");				
				
				// Consequence
				String consequence = model.getAssociationRules().getRules().get(i).getConsequence().toString();
				consequence = consequence.replaceAll("=t", "");
				consequence = consequence.replaceAll("\\[", "");
				consequence = consequence.replaceAll("\\]", "");				
				
				// Support				
				double support = (double)model.getAssociationRules().getRules().get(i).getTotalSupport()/(double)data.numInstances();
							
				// Confidence
				double confidence = model.getAssociationRules().getRules().get(i).getPrimaryMetricValue();
				
				double lift =  model.getAssociationRules().getRules().get(i).getMetricValuesForRule()[1];
				
								
				// Write to file
				bw.write(premise + "; " + consequence + "; " + support + "; " + confidence + "; " + lift + " \n");
				
			}
		
		} catch (IOException e) {

			e.printStackTrace();

		} finally {

			try {

				if (bw != null)
					bw.close();

				if (fw != null)
					fw.close();

			} catch (IOException ex) {

				ex.printStackTrace();

			}
		}		
	}
		
	
	public ArrayList<Parameters> parameterSuggest(String databaseName) throws Exception{
		
		// Set maximum number of return rules
		int maxNumRule = 10000;
		
		// Load data
		Instances data = new Instances(new BufferedReader(new FileReader(databaseName + ".arff")));

		// Build model
		Apriori model = new Apriori();		
		model.setNumRules(maxNumRule);		
		model.buildAssociations(data);
					
		ArrayList<Double> supportLst = new ArrayList<Double>();
		ArrayList<Double> confidencetLst = new ArrayList<Double>();
					
		int ruleNum = model.getAssociationRules().getRules().size();		
		for(int i = 0; i < ruleNum; i++) {			
			// Support				
			double support = (double)model.getAssociationRules().getRules().get(i).getTotalSupport()/(double)data.numInstances();
			supportLst.add(support);
			
			// Confidence
			double confidence = model.getAssociationRules().getRules().get(i).getPrimaryMetricValue();
			confidencetLst.add(confidence);				
		}
			
		
		
		return null;
	}
		
}
