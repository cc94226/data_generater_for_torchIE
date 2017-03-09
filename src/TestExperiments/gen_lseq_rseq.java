package TestExperiments;

import java.io.BufferedWriter;
import java.io.File;
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

public class gen_lseq_rseq {

	public static void main(String[] args) throws ParseException, IOException {
		// TODO Auto-generated method stub
		String dirString = args[0];//input:the json file
		String lseq = args[1];
		String rseq = args[2];
		
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
					
				}
				
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
			lseqFile.close();
			rseqFile.close();
	}

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
}
