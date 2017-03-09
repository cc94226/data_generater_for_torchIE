package TestExperiments;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class BioJsonProcesser {

	/**
	 * 
	 * @param args[0] input:the json file
	 * @param args[1] generate the text sequence file 
	 * @throws ParseException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws ParseException, IOException {
		// TODO Auto-generated method stub
		String inputfile = args[0];
		String output = args[1];
		String entityListFile = args[2];
		String dependencyFile = args[3];
		String postagFile = args[4];
		String lseq = args[5];
		String rseq = args[6];
		String text_toks = args[7];
		String lPos = args[8];
		String rPos = args[9];

		
		File jsonfile = new File(inputfile);
		
		Scanner scanner = new Scanner(jsonfile);
		JSONParser parser = new JSONParser();
		
		File outjson = new File(output);
		
		PrintWriter outputWriter = new PrintWriter(new BufferedWriter(
				new FileWriter(output)));
		PrintWriter entityWriter = new PrintWriter(new BufferedWriter(
				new FileWriter(entityListFile)));
		PrintWriter dependencyWriter = new PrintWriter(new BufferedWriter(
				new FileWriter(dependencyFile)));
		PrintWriter posWriter = new PrintWriter(new BufferedWriter(
				new FileWriter(postagFile)));
		PrintWriter lseqWriter = new PrintWriter(new BufferedWriter(
				new FileWriter(lseq)));
		PrintWriter rseqWriter = new PrintWriter(new BufferedWriter(
				new FileWriter(rseq)));
		PrintWriter textWriter = new PrintWriter(new BufferedWriter(
				new FileWriter(text_toks)));
		PrintWriter lPosWriter = new PrintWriter(new BufferedWriter(
				new FileWriter(lPos)));
		PrintWriter rPosWriter = new PrintWriter(new BufferedWriter(
				new FileWriter(rPos)));
		
		int timer = 0;
		int em = 0;
		boolean skipCurrentSens = false;
		
		//entitylist 
		Set<String> set = new HashSet<>();
		
		while (scanner.hasNextLine()) {
			skipCurrentSens = false;
			//System.out.println(timer++);
			//timer++;
			String lineString = scanner.nextLine();
			JSONObject jsonObject = (JSONObject) parser.parse(lineString);
			JSONArray jsonArrayEntityPairList = (JSONArray) jsonObject.get("entityPairList");
			JSONArray jsonArrayDependency = (JSONArray)jsonObject.get("dependency");
			JSONArray jsonPosTag = (JSONArray) jsonObject.get("posTag");
			JSONArray jsonArrayEntityList = (JSONArray)jsonObject.get("entityList");
			JSONArray jsonText = (JSONArray)jsonObject.get("text");
			
			int e1_start = ( (Long)((JSONObject) ((JSONObject)jsonArrayEntityList.get(0)).get("span")).get("begin") ).intValue();
			int e1_end = ((Long) ((JSONObject) ((JSONObject) jsonArrayEntityList
					.get(0)).get("span")).get("end"))
					.intValue();
			String e1_mid = ( ( (JSONObject)jsonArrayEntityList.get(0) ).get("mid") ).toString();
			int e1_head = ((Long) ((JSONObject)jsonArrayEntityList.get(0)).get("headIndex") ).intValue();
			
			int e2_start = ( (Long)((JSONObject) ((JSONObject)jsonArrayEntityList.get(1)).get("span")).get("begin") ).intValue();
			int e2_end = ((Long) ((JSONObject) ((JSONObject) jsonArrayEntityList
					.get(1)).get("span")).get("end"))
					.intValue();
			String e2_mid = ( ( (JSONObject)jsonArrayEntityList.get(1) ).get("mid") ).toString();
			
			if(e1_start > e2_start){
				int tempstart = e2_start;
				e2_start = e1_start;
				e1_start = tempstart;
				int tempend = e2_end;
				e2_end = e1_end;
				e1_end = tempend;
				String tempmid = e2_mid;
				e2_mid = e1_mid;
				e1_mid = tempmid;
			}
			if(e1_end >= e2_start || e2_start <= e1_end){
				//System.out.println("em"+ em++ + " "+ timer);
				//outputWriter.println("em");
				continue;
			}
			else{
				//compress depdency for entity2
				int newgov = -1;
				String newDepType = "";
				boolean flag = false;
				int dropflag = 0;
				
				for(int i = e2_start; i <= e2_end; i++){
					//System.out.println("i: " + i);
					if( i >= e2_start && i <= e2_end){
						JSONObject tempobj = (JSONObject)jsonArrayDependency.get(i);
						if(tempobj == null){
							//newDepList.add(null);
							//System.out.print("check: "+ (timer));
							continue;
						}
						int tempgov = ( (Long)( ((JSONObject)jsonArrayDependency.get(i)).get("governorIndex")) ).intValue();;
						String tempdepType = ( ((JSONObject)jsonArrayDependency.get(i)).get("dependencyType")) .toString();
						if( (tempgov <= e2_start || tempgov >= e2_end) && !flag){
							newgov = tempgov;
							newDepType = tempdepType;
							//System.out.println(i + " " + newDepType);
							if(!flag)
								flag = true;
						}
						else if( (tempgov < e2_start ||  tempgov > e2_end) ){
							//System.out.print("drop1");
							dropflag++;
						}
						if(dropflag > 1 ){
							if(tempgov != newgov){
								System.out.println(newgov + " "+tempgov+ "drop1 "  +timer + " "+ tempdepType);
								skipCurrentSens = true;
							}
							
						}
					}
					
				}
				
				if(skipCurrentSens)
					continue;
				
				if(newgov == -1 || newDepType == ""){
					//System.out.println(newgov + "can not find right gov or type" + newDepType);
					//return;
				}
				int offset = e2_end - e2_start;
				JSONArray newDepList = new JSONArray();
				for(int i = 0 ; i < jsonArrayDependency.size();i++){
					if(i < e2_start){
						JSONObject tempobj = (JSONObject)jsonArrayDependency.get(i);
						if(tempobj == null){
							newDepList.add(null);
							continue;
						}
						else{
							int tempgov = ( (Long)tempobj.get("governorIndex") ).intValue();
							String denpType = ( ((JSONObject)jsonArrayDependency.get(i)).get("dependencyType")) .toString();
							//System.out.println(denpType);
							if( tempgov >= e2_start && tempgov <= e2_end ){	
								
								JSONObject newobj = new JSONObject();
								newobj.put("dependencyType", denpType);
								newobj.put("governorIndex", new Long(e2_start));
								newDepList.add(newobj);
							}
							else if(tempgov > e2_end){
								JSONObject newobj = new JSONObject();
								newobj.put("dependencyType", denpType);
								newobj.put("governorIndex", new Long(tempgov - offset));
								newDepList.add(newobj);
							}
							else{
								newDepList.add(tempobj);
							}
						}
							
					}
					if(i == e2_start){
						if(newgov != -1){
							JSONObject newobj = new JSONObject();
							//System.out.println(newDepType);
							newobj.put("dependencyType", newDepType);
							if(newgov > e2_end)
								newobj.put("governorIndex", new Long(newgov - offset));
							else
								newobj.put("governorIndex", new Long(newgov));
										//dependencyType
							newDepList.add(newobj);
						}
						else
							newDepList.add(null);
					}
					if(i > e2_end){
						JSONObject tempobj = (JSONObject)jsonArrayDependency.get(i);
						if(tempobj == null){
							newDepList.add(null);
							continue;
						}
						else{
							int tempgov = ( (Long)tempobj.get("governorIndex") ).intValue();
							String denpType = ( ((JSONObject)jsonArrayDependency.get(i)).get("dependencyType")) .toString();
							//System.out.println(denpType);
							if( tempgov >= e2_start && tempgov <= e2_end ){						
								
								JSONObject newobj = new JSONObject();
								newobj.put("dependencyType", denpType);
								newobj.put("governorIndex", new Long(e2_start));
								newDepList.add(newobj);
							}
							else if(tempgov > e2_end){
								JSONObject newobj = new JSONObject();
								newobj.put("dependencyType", denpType);
								newobj.put("governorIndex", new Long(tempgov - offset));
								newDepList.add(newobj);
							}
							else{
								newDepList.add(tempobj);
							}
						}
						
					}
				}
				
				//System.out.println(newDepList.size());
				for(int i =0;i < newDepList.size();i++){
					//System.out.println( ( ((JSONObject)newDepList.get(i)).get("dependencyType")) .toString() + " ?");
				}
				
				/*String testType = ( ((JSONObject)newDepList.get(22)).get("dependencyType")) .toString();
				System.out.println(testType);
				testType = ( ((JSONObject)newDepList.get(23)).get("dependencyType")) .toString();
				System.out.println(testType);
				testType = ( ((JSONObject)newDepList.get(24)).get("dependencyType")) .toString();
				System.out.println(testType);
				*/
				//decomposs dependency entity1
				newgov = -1;
				newDepType = "";
				flag = false;
				dropflag = 0;
				//System.out.println(e1_start);
				for(int i = e1_start; i <= e1_end; i++){
					//System.out.println(i);
					if( i >= e1_start && i <= e1_end){
						JSONObject tempobj = (JSONObject)newDepList.get(i);
						if(tempobj == null){
							//newDepList.add(null);
							//System.out.println("check: "+ (timer));
							continue;
						}
						//System.out.println(i + tempobj.toString());
						int tempgov = ( (Long)( ((JSONObject)newDepList.get(i)).get("governorIndex")) ).intValue();;
						String tempdepType = ( ((JSONObject)newDepList.get(i)).get("dependencyType")) .toString();
						if((tempgov <= e1_start || tempgov > e1_end) && !flag){
							newgov = tempgov;
							newDepType = tempdepType;
							if(!flag)
								flag = true;
						} 
						else if((tempgov <= e1_start || tempgov > e1_end)){
							
							dropflag ++;
						}
						if(dropflag == 2){
							if(tempgov != newgov){
								System.out.println(newgov + " "+tempgov+ "drop2 "  +timer + " "+ tempdepType);
								skipCurrentSens = true;
								
							}
						}
					}
					
				}
				
				if(skipCurrentSens)
					continue;
				
				if(newgov == -1 || newDepType == ""){
					//System.out.println(newgov + "can not find right gov or type2" + newDepType);
					//return;
				}
				
				
				
				JSONArray newDepList2 = new JSONArray();
				offset = e1_end - e1_start; 
				for(int i = 0 ; i < newDepList.size();i++){
					//System.out.println(i + " "+ e1_start);
					if(i < e1_start){
						JSONObject tempobj = (JSONObject)newDepList.get(i);
						if(tempobj == null){
							newDepList2.add(null);
							continue;
						}
						else{
							int tempgov = ( (Long)tempobj.get("governorIndex") ).intValue();
							String denpType = ( ((JSONObject)newDepList.get(i)).get("dependencyType")) .toString();
							if( tempgov >= e1_start && tempgov <= e1_end ){
								
								
								
								JSONObject newobj = new JSONObject();
								newobj.put("dependencyType", denpType);
								newobj.put("governorIndex", new Long(e1_start));
								newDepList2.add(newobj);
							}
							else if(tempgov > e1_end){
								JSONObject newobj = new JSONObject();
								newobj.put("denpendencyType", denpType);
								newobj.put("governorIndex", new Long(tempgov - offset));
								newDepList2.add(newobj);
							}
							else{
								newDepList2.add(tempobj);
							}
						}
							
					}
					if(i == e1_start){
						if(newgov != -1){
							JSONObject newobj = new JSONObject();
							newobj.put("dependencyType", newDepType);
							if(newgov > e1_end)
								newobj.put("governorIndex", new Long(newgov - offset));
							else
								newobj.put("governorIndex", new Long(newgov));
							newDepList2.add(newobj);
						}
						else
							newDepList2.add(null);
					}
					if(i > e1_end){
						//System.out.println(i);
						JSONObject tempobj = (JSONObject)newDepList.get(i);
						//System.out.println(i + tempobj.toString());
						if(tempobj == null){
							newDepList2.add(null);
							continue;
						}
						else{
							//System.out.println(tempobj.toString());
							int tempgov = ( (Long)tempobj.get("governorIndex") ).intValue();
							//System.out.println(tempgov);
							String denpType = ( ((JSONObject)newDepList.get(i)).get("dependencyType")) .toString();
							if( tempgov >= e1_start && tempgov <= e1_end ){
								
								
								
								JSONObject newobj = new JSONObject();
								newobj.put("dependencyType", denpType);
								newobj.put("governorIndex", new Long(e1_start));
								newDepList2.add(newobj);
							}
							else if(tempgov < e1_start){
								newDepList2.add(tempobj);
							}
							else if(tempgov > e1_end){
								//String denpType = ( ((JSONObject)newDepList.get(i)).get("dependencyType")) .toString();
								
								JSONObject newobj = new JSONObject();
								newobj.put("dependencyType", denpType);
								newobj.put("governorIndex", new Long(tempgov - offset));
								newDepList2.add(newobj);
							}
							//System.out.println("end");
						}
						
					}
				}
				
				//deal with postag
				JSONArray newPosTagList = new JSONArray();
				int count = 0;
				for(int i = 0; i < jsonPosTag.size();i++){
					if(i < e1_start || i > e2_end){
						String temppos = ( jsonPosTag.get(i) ) .toString();
						newPosTagList.add(temppos);
					}
					if(i == e1_start || i == e2_start){
						
						newPosTagList.add("NN");
					}
					if(i > e1_end && i < e2_start){
						String temppos = ( jsonPosTag.get(i) ) .toString();
						newPosTagList.add(temppos);
					}
					
				}
				
				//deal with text
				JSONArray newText = new JSONArray();
				count = 0;
				for(int i = 0; i < jsonText.size();i++){
					if(i < e1_start || i > e2_end){
						String temptext = convertToken( ( jsonText.get(i) ) .toString().toLowerCase() );
						newText.add(temptext);
					}
					if(i == e1_start){
						
						newText.add(e1_mid);
					}
					if(i == e2_start){
						newText.add(e2_mid);
					}
					if(i > e1_end && i < e2_start){
						String temptext = convertToken( ( jsonText.get(i) ) .toString().toLowerCase() );
						newText.add(temptext);
					}
					
				}
				//jsonArrayEntityList.set(0, )
				
				e2_start = e2_start - (e1_end - e1_start);
				e2_end = e2_start;
				e1_end = e1_start;
				
				JSONArray newEntityList = new JSONArray();
				
				JSONObject newEntity1 = new JSONObject();
				//newEntity1.escape("0");
				newEntity1.put("mentionText", e1_mid);
				newEntity1.put("entityType", null);
				newEntity1.put("entityNotableType", null);
				newEntity1.put("mid", e1_mid);
				JSONObject e1span = new JSONObject();
				e1span.put("end", e1_start);
				e1span.put("begin", e1_start);
				newEntity1.put("span", e1span);
				newEntity1.put("headIndex", e1_start);
				
				JSONObject newEntity2 = new JSONObject();
				//newEntity1.escape("0");
				newEntity2.put("mentionText", e2_mid);
				newEntity2.put("entityType", null);
				newEntity2.put("entityNotableType", null);
				newEntity2.put("mid", e2_mid);
				JSONObject e2span = new JSONObject();
				e2span.put("end", e2_start);
				e2span.put("begin", e2_start);
				newEntity2.put("span", e2span);
				newEntity2.put("headIndex", e2_start);
				
				newEntityList.add(newEntity1);
				newEntityList.add(newEntity2);
				
				//create entitylist file
				set.add(e1_mid);
				set.add(e2_mid);
				
				//lseq rseq
				if(e1_end != 0 || e2_end != 0 ){
					System.out.println(e1_start  + " " + e2_start);
					String templseq = "";
					for(int i = e1_start; i < e2_start; i++){
						if(i < e2_start-1)
							templseq = templseq + i + " ";
						else
							templseq = templseq + i;
					}
					lseqWriter.println(templseq);
					
					String temprseq = "";
					for(int i = e2_start; i > e1_end; i--){
						if(i > e1_end+1)
							temprseq = temprseq + i + " ";
						else
							temprseq = temprseq + i;
					}
					rseqWriter.println(temprseq);
				}
				
				StringBuffer sb = new StringBuffer();
				for(int i =0; i < newText.size(); i++){
					
					if(i < newText.size() - 1)
						sb.append("L" + (e1_start - i) + " ");
					else
						sb.append("L" + (e1_start - i) );
				}
				lPosWriter.println(sb.toString());
				
				StringBuffer sb2 = new StringBuffer(); 
				for(int i =0; i < newText.size(); i++){
					if(i < newText.size())
						sb2.append("R" + (e2_start - i) + " ");
					else
						sb2.append("R" + (e2_start - i) );
				}
				rPosWriter.println(sb2.toString());
				
				
				//create new json
				JSONObject newJson = new JSONObject();
				newJson.put("entityPairList", jsonArrayEntityPairList);
				newJson.put("dependency", newDepList2);
				newJson.put("posTag", newPosTagList);
				newJson.put("entityList", newEntityList);
				newJson.put("text", newText);
				
				outputWriter.println(newJson.toString());
				dependencyWriter.println(getDependencySequence(newDepList2));
				posWriter.println(getPosTagSequence(newPosTagList));
				textWriter.println(getText(newText));
				timer++;
			}
			
			
			
			
			
			//System.out.println(e1_mid);
			//LinkedList list = new LinkedList();
			//return;
		}
		
		for (String e : set) {
			entityWriter.println(e);
		}
		System.out.println("line "+timer);
		outputWriter.close();
		dependencyWriter.close();
		posWriter.close();
		textWriter.close();
		lseqWriter.close();
		rseqWriter.close();
		lPosWriter.close();
		rPosWriter.close();
	}

	public static String getDependencySequence(JSONArray depList){
		StringBuffer sb = new StringBuffer("");
		for(int i = 0;i < depList.size();i++){
			JSONObject tempobj = (JSONObject)depList.get(i);
			if(tempobj == null){
				if(i < depList.size() -1){
					sb.append("-1 ");
				}
				else
					sb.append("-1");
				continue;
			}
			int tempgov = ( (Long)tempobj.get("governorIndex") ).intValue();
			
			if(i < depList.size() -1)
				sb.append(tempgov + " ");
			else
				sb.append(tempgov);
		}
		return sb.toString();
	}
	
	public static String getPosTagSequence(JSONArray posList){
		StringBuffer sb = new StringBuffer("");
		for(int i = 0;i < posList.size();i++){
			String temppos = posList.get(i).toString();
			//int tempgov = ( (Long)tempobj.get("governorIndex") ).intValue();
			
			if(i < posList.size() -1)
				sb.append(temppos + " ");
			else
				sb.append(temppos);
		}
		return sb.toString();
	}
	
	
	public static String getText(JSONArray textList){
		StringBuffer sb = new StringBuffer("");
		for(int i = 0;i < textList.size();i++){
			String temptext =  textList.get(i).toString() ;
			//int tempgov = ( (Long)tempobj.get("governorIndex") ).intValue();
			
			if(i < textList.size() -1)
				sb.append(temptext + " ");
			else
				sb.append(temptext);
		}
		return sb.toString();
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
