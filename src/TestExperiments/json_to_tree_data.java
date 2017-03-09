package TestExperiments;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

public class json_to_tree_data {
	
	/**
	 * 
	 * @param args[0] input:text sequence file/ text.toks
	 * @param args[1] generate text.toks
	 * @param args[2] generate lSeq.ids
	 * @param args[3] generate rSeq.ids
	 * @param args[4] input: entity list
	 * @param args[5] input: pos tag file
	 * @param args[6] generate pos.toks
         * @param args[7] input: dependency file
	 * 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String fileString = args[0];
		
		String textFileString = args[1];
		
		String lSeqFileString = args[2];
		String rSeqFileString = args[3];
		String updateListString = args[4];
		String posTagFile = args[5];
		String postTagoutfile = args[6];
		String dependencyFile = args[7];
		Scanner dependencyScanner = new Scanner(new File(dependencyFile));
		PrintWriter textFileWriter = new PrintWriter(new BufferedWriter(
				new FileWriter(textFileString, true)));
		PrintWriter posFileWriter = new PrintWriter(new BufferedWriter(
				new FileWriter(postTagoutfile, true)));
		PrintWriter lSeqFileWriter = new PrintWriter(new BufferedWriter(
				new FileWriter(lSeqFileString, true)));
		PrintWriter rSeqFileWriter = new PrintWriter(new BufferedWriter(
				new FileWriter(rSeqFileString, true)));
		Scanner scFile = new Scanner(new File(updateListString));
		Scanner scPosFile = new Scanner(new File(posTagFile));
		HashSet<String> setLine = new HashSet<>();
		while (scFile.hasNextLine()) {
			String line = scFile.nextLine();
			setLine.add(line);
		}
		scFile.close();
		Scanner sc = new Scanner(new File(fileString));
	
		int count = 0;
		int c = 0;
		while (sc.hasNextLine()) {
			System.out.println("Line:"+c);
			c++;
			String lineString = sc.nextLine();
			String denpencyLine = dependencyScanner.nextLine();
			String line[] = lineString.split(" ");
			String deps[] = denpencyLine.split(" ");
			String posLine = scPosFile.nextLine();
			List<Integer> mid = new ArrayList<Integer>();
			for (int i = 0; i < line.length; i++) {
				if (setLine.contains(line[i])) {
					mid.add(i);
				}
			}
			if (mid.size()>2){
				System.out.println(lineString);
				System.out.println(mid);
			}
			for (int i = 0; i < mid.size(); i++) {
				for (int j = i + 1; j < mid.size(); j++) {

					int leftBegin = mid.get(i);
					int rightBegin = mid.get(j);
					if(leftBegin ==rightBegin){
						continue;
						}
					System.out.println("Break point");
					PathTuple pathTuple = findPath(leftBegin,rightBegin,deps,c);
					String seqL = "";
					String seqR = "";
					for (Integer lidx:pathTuple.pathLeft) {
						seqL+=String.valueOf(lidx)+" ";
					}
					for (Integer ridx:pathTuple.pathRight) {
						seqR+=String.valueOf(ridx)+" ";
					}
					
					posFileWriter.println(posLine);
					textFileWriter.println(lineString);
					lSeqFileWriter.println(seqL.substring(0, seqL.length() - 1));
					rSeqFileWriter.println(seqR.substring(0, seqR.length() - 1));
					count++;
				}
			}
		}
		System.out.println(count);
		System.out.println(c);
		scPosFile.close();
		posFileWriter.close();
		textFileWriter.close();
		lSeqFileWriter.close();
		rSeqFileWriter.close();
		dependencyScanner.close();
		sc.close();
	}
	private static PathTuple findPath(int leftBegin, int rightBegin,
			String[] deps, int c) {
		PathTuple  pathTuple =new PathTuple();
		HashSet<Integer> traceLeft = new HashSet<>();
		int leftCurs = leftBegin;
		while (leftCurs!=rightBegin&&leftCurs!=-1) {
			traceLeft.add(leftCurs);
			leftCurs = Integer.valueOf(deps[leftCurs]);
			
		}
		if(leftCurs==rightBegin){
			int leftDep = leftBegin;
			while (leftDep!=rightBegin) {
				pathTuple.pathLeft.add(leftDep);
				leftDep = Integer.valueOf(deps[leftDep]);
			}
			pathTuple.pathLeft.add(rightBegin);
			pathTuple.pathRight.add(rightBegin);
			return pathTuple;
		}else{
			traceLeft.add(-1);
			int rightCurs = rightBegin;
			while (!traceLeft.contains(rightCurs)) {
				pathTuple.pathRight.add(rightCurs);
				System.out.println("Right"+rightCurs);
				System.out.println("Line:"+c);
				rightCurs = Integer.valueOf(deps[rightCurs]);
			}
			pathTuple.pathRight.add(rightCurs);
			int leftDep = leftBegin;
			while (leftDep!=rightCurs) {

				pathTuple.pathLeft.add(leftDep);
				System.out.println("Left"+leftDep);
				leftDep = Integer.valueOf(deps[leftDep]);
			
			}
			pathTuple.pathLeft.add(rightCurs);
			return pathTuple;
		}
		
		
		
		
		
		
	}
	
}

class PathTuple{
	List<Integer> pathLeft = new ArrayList<>();
	List<Integer> pathRight = new ArrayList<>();
}
