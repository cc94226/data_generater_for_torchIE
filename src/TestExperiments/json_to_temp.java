package TestExperiments;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class json_to_temp {

	public static void main(String[] args) throws IOException, ParseException {
		// TODO Auto-generated method stub
		String jsonFile = args[0];
		String entityListFile = args[1];
		String dependencyFile = args[2];
		String postagFile = args[3];
		String lseq = args[4];
		String rseq = args[5];
		String text_toks = args[6];
		
		File json = new File(jsonFile);
		
		Scanner scanner = new Scanner(json);
		JSONParser parser = new JSONParser();
		
		//File outjson = new File(output);
		
		//PrintWriter outputWriter = new PrintWriter(new BufferedWriter(
			//	new FileWriter(output)));
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
		
		Set<String> set = new HashSet<>();
		
		while (scanner.hasNextLine()) {
			String lineString = scanner.nextLine();
			JSONObject jsonObject = (JSONObject) parser.parse(lineString);
		}
	}

}
