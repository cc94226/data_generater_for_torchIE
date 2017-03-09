package TestExperiments;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class WordEmbeddingTransfer {
	public static String convertToken(String text) {
		StringBuilder sb = new StringBuilder();
		if (text.matches(".*\\d.*")) {
			text = text.replaceAll("\\d", " ");
			char[] chars = text.toCharArray();
			int count = 0;
			for (char c : chars) {
				if (c == ' ') {
					count++;
				} else {
					if (count == 0) {
						sb.append(c);
					} else {
						sb.append("NUM" + count + c);
						count = 0;
					}
				}
			}
			if (count > 0)
				sb.append("NUM" + count);
			return sb.toString();
		} else {
			return text;
		}

	}
	/**
	 * 
	 * @param args[0] input:the json file
	 * @param args[1] generate the text sequence file 
	 * @param args[2] generate the entity list
	 * @param args[3] generate the post tag file
	 * @param args[4] generate a new json file, filtered out the corrupted data
	 * @param args[5] generate the dependency file
	 * @param args[6] generate the lseq file: the span number from the start of far left entity to the previous of the far right entity
	 * @param args[7] genrrate the rseq file: the span number from the start of the far right to the next of the end of the far left  
	 * 
	 */
	public static void main(String args[]) throws ParseException, IOException {
		String dirString = args[0];//input:the json file
		String ouputFile = args[1];//generate the text sequence file e.g text.toks
		String updateFile = args[2];//generate the entity list
		String postagFile = args[3];//generate the post tag file
		String filterFileString = args[4];//generate a new json file, filtered out the corrupted data
		String dep = args[5];//generate the dependency file
		String lseq = args[6];
		String rseq = args[7];
		
		PrintWriter printWriter = new PrintWriter(new BufferedWriter(
				new FileWriter(ouputFile)));
		PrintWriter printWriterdep = new PrintWriter(new BufferedWriter(
				new FileWriter(dep)));
		PrintWriter printPosTagWriter = new PrintWriter(new BufferedWriter(
				new FileWriter(postagFile)));
		PrintWriter updateWriter = new PrintWriter(new BufferedWriter(
				new FileWriter(updateFile)));
		PrintWriter filterFile = new PrintWriter(new BufferedWriter(
				new FileWriter(filterFileString)));
		PrintWriter lseqFile = new PrintWriter(new BufferedWriter(
				new FileWriter(lseq)));
		PrintWriter rseqFile = new PrintWriter(new BufferedWriter(
				new FileWriter(rseq)));
		
		File dir = new File(dirString);
		Set<String> set = new HashSet<>();
		
			Scanner scanner = new Scanner(dir);
			JSONParser parser = new JSONParser();
			int cc = 0;
			while (scanner.hasNextLine()) {
			//int z =0;
			//while (z++ < 2){
				String lineString = scanner
						.nextLine();
				JSONObject jsonObject = (JSONObject) parser.parse(lineString);
				//System.out.println(lineString);
				JSONArray jsonArrayText = (JSONArray) jsonObject.get("text");
				JSONArray jsonArrayTag = (JSONArray) jsonObject.get("posTag");
				HashMap<Integer,Integer> mapInteger = new HashMap<>();
				mapInteger.put(-1, -1);
				JSONArray jsonArrayEntity = (JSONArray) jsonObject
						.get("entityList");
				int textIdx = 0;
				int entityIdx = 0;
				boolean flag = false;
				String line = "";
				String posString="";
				boolean flag_len = true;
				int entiityCount = 0;
				int offset = 0;
				int mark = 0;
				
				int timer = 0;
				int e1_start = 0; 
				int e1_end = 0;
				int e2_start = 0;
				int e2_end = 0;
				
				for (Object text : jsonArrayText) {
					
					if (entityIdx < jsonArrayEntity.size()
							&& (Long) ((JSONObject) ((JSONObject) jsonArrayEntity
									.get(entityIdx)).get("span")).get("begin") == textIdx) {
						int begin = ((Long) ((JSONObject) ((JSONObject) jsonArrayEntity
								.get(entityIdx)).get("span")).get("begin"))
								.intValue();
						int end = ((Long) ((JSONObject) ((JSONObject) jsonArrayEntity
								.get(entityIdx)).get("span")).get("end"))
								.intValue();
						//lseqFile.println(begin+" "+end + " " + (String) ((JSONObject) jsonArrayEntity.get(entityIdx)).get("mentionText"));
						
						if(timer == 0){
							timer++;
							e1_start = begin;
							e1_end = end;
						}
						else if(timer == 1){
							e2_start = begin;
							e2_end = end;
						}
						
						String mid = (String) ((JSONObject) jsonArrayEntity
								.get(entityIdx)).get("mid");
						if (mid != null) {
							
							flag = true;
							
							line += mid + " ";
							set.add(mid);
							entiityCount++;
							posString+="NER"+" ";
						} else {
							String entity_token = "ENTITY";
							for (int i = begin; i <= end; i++) {
								entity_token += "_"
										+ convertToken(jsonArrayText.get(i)
												.toString().toLowerCase());
							}
							
							line += entity_token + " ";
							if (entity_token.length()>=100) {
								flag_len=false;
							}
							set.add(entity_token);
							flag = true;
							posString+="NER"+" ";
							entiityCount++;
						}
					}

					if (!flag) {
						line += convertToken(((String) text).toLowerCase())
								+ " ";
						posString+=jsonArrayTag.get(textIdx)+" ";
						mapInteger.put(textIdx, textIdx-offset);
					}else{
						if(mark!=0){
							offset++;
						}
						mapInteger.put(textIdx, textIdx-offset);
						
						
						mark++;
					}
					if (entityIdx < jsonArrayEntity.size()
							&& (Long) ((JSONObject) ((JSONObject) jsonArrayEntity
									.get(entityIdx)).get("span")).get("end") == textIdx) {
						flag = false;
						entityIdx++;
						mark=0;
					}
					textIdx++;
				}// end for (Object text : jsonArrayText)
				
				//System.out.println(mark);
				
				/*int timer = 0;
				int e1_start = 0; 
				int e1_end = 0;
				int e2_start = 0;
				int e2_end = 0;
				for (Object text : jsonArrayText) {
					
					if (entityIdx < jsonArrayEntity.size()
							&& (Long) ((JSONObject) ((JSONObject) jsonArrayEntity
									.get(entityIdx)).get("span")).get("begin") == textIdx) {
						if(timer == 0){
							timer++;
							e1_start = ((Long) ((JSONObject) ((JSONObject) jsonArrayEntity
									.get(entityIdx)).get("span")).get("begin"))
									.intValue();
							e1_end = ((Long) ((JSONObject) ((JSONObject) jsonArrayEntity
									.get(entityIdx)).get("span")).get("end"))
									.intValue();
						}
						else if(timer == 1){
							e2_start = ((Long) ((JSONObject) ((JSONObject) jsonArrayEntity
									.get(entityIdx)).get("span")).get("begin"))
									.intValue();
							e2_end = ((Long) ((JSONObject) ((JSONObject) jsonArrayEntity
									.get(entityIdx)).get("span")).get("end"))
									.intValue();
						}
						
					}
				}*/
				
				if(e1_end != 0 || e2_end != 0 ){
				System.out.println(e1_start  + " " + e2_start);
				String templseq = "";
				for(int i = e1_start; i < e2_start; i++){
					if(i < e2_start-1)
						templseq = templseq + i + " ";
					else
						templseq = templseq + i;
				}
				lseqFile.println(templseq);
				
				String temprseq = "";
				for(int i = e2_start; i > e1_end; i--){
					if(i > e1_end+1)
						temprseq = temprseq + i + " ";
					else
						temprseq = temprseq + i;
				}
				rseqFile.println(temprseq);
				}
				
				if (!line.equals("")&&flag_len&&entiityCount>=2){
					int textIdx1 = 0;
					int entityIdx1 = 0;
					boolean flag1 = false;
					String depLine = "";
					int iidx = 0;
					int countminus1 = 0;
					JSONArray depArray = (JSONArray) jsonObject.get("dependency");
					for (Object text : jsonArrayText) {
						if (entityIdx1 < jsonArrayEntity.size()
								&& (Long) ((JSONObject) ((JSONObject) jsonArrayEntity
										.get(entityIdx1)).get("span")).get("begin") == textIdx1) {
							
							int headindex = ((Long) ((JSONObject) jsonArrayEntity
									.get(entityIdx1)).get("headIndex")).intValue();
							int begin = ((Long) ((JSONObject) ((JSONObject) jsonArrayEntity
									.get(entityIdx1)).get("span")).get("begin"))
									.intValue();
							int end = ((Long) ((JSONObject) ((JSONObject) jsonArrayEntity
									.get(entityIdx1)).get("span")).get("end"))
									.intValue();
							int minLength = Integer.MAX_VALUE;
							int maxIndex = 0;
							for (int i = begin; i <= end; i++) {
								int depIdx = i;
								int countLength = 0;
								while(depArray.get(depIdx)!=null){
									
									depIdx = ((Long)((JSONObject)depArray.get(depIdx)).get("governorIndex")).intValue();
									
									countLength++;
								}
								if(countLength<minLength){
									maxIndex = i;
									minLength = countLength;
								}
								
							}
							int countLength = 0;
							int ddepidx = headindex;
							while(depArray.get(ddepidx)!=null){
								
								ddepidx = ((Long)((JSONObject)depArray.get(ddepidx)).get("governorIndex")).intValue();
								
								countLength++;
							}
							if(minLength==countLength){
								maxIndex = headindex;
							}
							int depIdx = -1;
							if(depArray.get(maxIndex)!=null){
								depIdx = ((Long)((JSONObject)depArray.get(maxIndex)).get("governorIndex")).intValue();
							}
							if(depIdx==-1){
								countminus1++;
							}
							int offidx = mapInteger.get(depIdx);
							
							/*if(offidx==iidx){
								//boolean f = false;
								for (int i = begin; i <= end; i++) {
									depIdx = -1;
									if(depArray.get(i)!=null){
										depIdx = ((Long)((JSONObject)depArray.get(i)).get("governorIndex")).intValue();
									}
									offidx = mapInteger.get(depIdx);
									if(offidx!=iidx){
										//f = true;
										break;
									}
								}
								if(offidx==-1){
								countminus1++;
								}
							}*/
							depLine+=offidx+" ";
								flag1 = true;
								iidx++;
						}

						if (!flag1) {
							int depIdx = -1;
							if(depArray.get(textIdx1)!=null){
								depIdx = ((Long)((JSONObject)depArray.get(textIdx1)).get("governorIndex")).intValue();
							}
							if(depIdx==-1){
								countminus1++;
							}
							int offidx = mapInteger.get(depIdx);
							if(offidx==iidx){
								offidx=-1;
								countminus1++;
							}
							depLine+=offidx+" ";
							iidx++;
							
						}
						if (entityIdx1 < jsonArrayEntity.size()
								&& (Long) ((JSONObject) ((JSONObject) jsonArrayEntity
										.get(entityIdx1)).get("span")).get("end") == textIdx1) {
							flag1 = false;
							entityIdx1++;
			
						}
						textIdx1++;
					}
					if(countminus1>1){
						//System.out.println("There are two roots");
						cc++;
					}
					System.out.println(lineString);
					printWriter.println(line.substring(0, line.length() - 1));
					filterFile.println(lineString);
					printPosTagWriter.println(posString.substring(0, posString.length() - 1));
					printWriterdep.println(depLine.substring(0, depLine.length() - 1));
				}//end if (!line.equals("")&&flag_len&&entiityCount>=2)
				
			}//end while
			System.out.println(cc);
			scanner.close();

		
		for (String e : set) {
			updateWriter.println(e);
		}
		filterFile.close();
		printPosTagWriter.close();
		updateWriter.close();
		printWriterdep.close();
		printWriter.close();
		lseqFile.close();
		rseqFile.close();
	}

}
