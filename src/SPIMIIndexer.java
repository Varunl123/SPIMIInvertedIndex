import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.htmlparser.Parser;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.jsoup.Jsoup;

public class SPIMIIndexer {


	private int BLOCK_SIZE = 26000;
	public long initialMemory;
	String typeOfIndex = "uncompressed";

	private static Map<String,ArrayList<String>> dictionary  = new TreeMap<String,ArrayList<String>>();
	private static final Pattern UNDESIRABLES = Pattern.compile("[\\W]"); //unused pre-processing text cleaner regex. remove all non word words.
	private boolean isSortedAndWrittern=false;
	private int outputCounter=0;



	public SPIMIIndexer(int blockSize) {
		this.BLOCK_SIZE = blockSize;
	}

	private String removeNumbers (String content)
	{
		return content.replaceAll("[0-9]","");
	}

	private String caseFold (String content)
	{
		return content.toLowerCase();
	}

	private String removeStopWords (String[] stopWords, String content)
	{
		String newContent = content;

		for (String stopword : stopWords) {
			if(content.equalsIgnoreCase(stopword))
				newContent = "";
			//newContent = content.replaceAll(stopword.toLowerCase(), "");
		}
		return newContent;
	}




	public long readFile(String path, String paramTypeOfIndex) {
		
		//Actual SPIMI Algorithm Implementation
		
		TagNameFilter filter = new TagNameFilter("Body"); //the tag to read from corpus
		this.typeOfIndex = paramTypeOfIndex; //type of index we can to create

		File folder = new File(path);
		FileReader reader = null;
		File[] listOfFiles = folder.listFiles();

		long totalPostings = 0;

		try {
			for (int i = 0; i < listOfFiles.length; i++) {
				if (listOfFiles[i].isFile()) {
					if(listOfFiles[i].getName().startsWith("reut2"))
					{
						reader = new FileReader(listOfFiles[i]);
						char[] chars = new char[(int) listOfFiles[i].length()];
						reader.read(chars);
						reader.close();

						String content = new String(chars);
						Parser p = new Parser(content);

						NodeList list = p.extractAllNodesThatMatch(filter); //extract all <BODY> nodes from the file.

						for(int k=0;k<list.size();k++)
						{

							String t = Jsoup.parse(list.elementAt(k).toPlainTextString()).text();
							StringTokenizer tokens = new StringTokenizer(t," "); //create tokens based on whitespace
							while(tokens.hasMoreTokens())
							{
								if(isMemoryAvailable()) //check for memory constrained as defined in blocksize
								{
									//Preprocessing of the tokens.
									String str = tokens.nextToken(); 
									String newStr =  "";
									//String 


									if(this.typeOfIndex.equalsIgnoreCase("compressed"))
									{
										//newStr = UNDESIRABLES.matcher(str).replaceAll("");
										newStr = str.replaceAll("\\$,\\.;:'\"!\\^*()& /+", "").trim(); //trim and remove all special characters from the token
										newStr = removeNumbers(newStr); //remove Numbers from the token
										newStr = caseFold(newStr); //case fold the token to lowercase

										//String[] stopwords = WordLists.getStopWordsFromFile("englishStopWords.txt");
										String[] stopwords = WordLists.getStopWordsFromFile("longStopWords.txt");
										newStr = removeStopWords(stopwords, newStr);
									} else {
										newStr = str.replaceAll("\\$,\\.;:'\"!\\^*()& /+", "").trim();
										//newStr = str.replaceAll("[$,.;:'\"!^*()& /+]", "").trim();
									}

									if(newStr.equalsIgnoreCase("")) //ignore if return is empty after cleaning
										continue;

									if(!dictionary.containsKey(newStr)){
										dictionary.put(newStr, new ArrayList<String>());
									} 
									if(!dictionary.get(newStr).contains(listOfFiles[i].getName()))
									{
										dictionary.get(newStr).add(listOfFiles[i].getName()); //add to dictionary.
										totalPostings++;
									}

								} else {
									//Sort & Write to Disk.

									writeToFile("tempIndex");
								}
							}
							if(!isSortedAndWrittern)
							{
								//sortDictionary();
							}
							t = null;
						}
						//CleanUp the memory
						content = null;
						p=null;
						list = null;

						java.lang.Runtime.getRuntime().gc();
						java.lang.Runtime.getRuntime().gc();
					}
				} 
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if(reader !=null){
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}}
		}
		return totalPostings;
	}


	private void sortDictionary()
	{

		//sort the values of dictionary. (postingslist)
		for(Map.Entry<String, ArrayList<String>> element : dictionary.entrySet()) {

			Collections.sort(element.getValue(), new Comparator<String>() {
				@Override
				public int compare(String s1, String s2) {
					return s1.compareTo(s2);
				}
			});
		}
	}


	private int[] writeToFile(String typeOfOutput)
	{
		String filename = "";
		int totalTerms = 0;
		int totalPostingList = 0;
		int[] response = new int[2];
		if(typeOfOutput.equalsIgnoreCase(this.typeOfIndex))
		{
			filename = "MasterIndex-"+typeOfOutput;
		} else {

			filename = "tempIndex-"+outputCounter;

			outputCounter++;
		}
		filename+=".txt";
		try {
			String path = System.getProperty("user.dir") + "/indexOutput/";
			File file = new File(path+ filename);
			//file.getParentFile().mkdirs();
			if (!file.exists()) {
				file.createNewFile();
			}
			BufferedWriter bw = new BufferedWriter(new FileWriter(file, true)); // append the result

			for(Map.Entry<String, ArrayList<String>> element : dictionary.entrySet()) {

				String logData =element.getKey().toString()+"\t"+(element.getValue().toString().substring(1, element.getValue().toString().length()-1)).trim().replaceAll(" ", ""); 
				totalPostingList+=element.getValue().size();
				bw.write(logData);
				bw.newLine();
			}

			bw.flush();
			bw.close();
			totalTerms = dictionary.size();
			dictionary.clear();
			java.lang.Runtime.getRuntime().gc();
			java.lang.Runtime.getRuntime().gc();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		response[0] = totalTerms;
		response[1] = totalPostingList;
		return response;


	}

	public int[] mergeIndexes(String path) 
	{
		File folder = new File(path);
		FileReader reader = null;
		File[] listOfFiles = folder.listFiles();
		dictionary.clear();
		java.lang.Runtime.getRuntime().gc();
		java.lang.Runtime.getRuntime().gc();

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				if(listOfFiles[i].getName().startsWith("MasterIndex"))
					continue;

				try {
					reader = new FileReader(listOfFiles[i]);

					char[] chars = new char[(int) listOfFiles[i].length()];
					reader.read(chars);
					reader.close();

					String content = new String(chars);
					String[] items = content.split("\\r?\\n");
					for(int k=0;k<items.length;k++)
					{
						if(items[k].equals("") || items[k].equalsIgnoreCase("\\r?\\n"))
							continue;

						String[] keyVal = items[k].split("\t");

						if (Array.getLength(keyVal) < 2)
							continue;



						if(!dictionary.containsKey(keyVal[0])){
							dictionary.put(keyVal[0], new ArrayList<String>());
						} 
						if(!dictionary.get(keyVal[0]).contains(keyVal[1]))
							dictionary.get(keyVal[0]).add(keyVal[1].trim());


					}

				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException es) {
					es.printStackTrace();
				} catch (ArrayIndexOutOfBoundsException ed) {
					ed.printStackTrace();
				}

			}

			listOfFiles[i].delete();

		}
		sortDictionary();
		return writeToFile(this.typeOfIndex);


	}

	public boolean readIndexToMemory(String typeOfIndex) {

		String filename = "MasterIndex-"+typeOfIndex;
		filename+=".txt";

		FileReader reader = null;

		try {
			String path = System.getProperty("user.dir") + "/indexOutput/";
			File file = new File(path+ filename);
			//file.getParentFile().mkdirs();
			if (!file.exists()) {
				return false;
			}

			reader = new FileReader(file);

			char[] chars = new char[(int) file.length()];
			reader.read(chars);
			reader.close();

			String content = new String(chars);

			String[] items = content.split("\\r?\\n");
			for(int k=0;k<items.length;k++)
			{
				if(items[k].equals("") || items[k].equalsIgnoreCase("\\r?\\n"))
					continue;

				String[] keyVal = items[k].split("\t");

				if (Array.getLength(keyVal) < 2)
					continue;

				if(!dictionary.containsKey(keyVal[0])){
					dictionary.put(keyVal[0], new ArrayList<String>(Arrays.asList(keyVal[1].split(","))));
				} 

				//"(The	reut2-000.sgm, reut2-011.sgm, reut2-016.sgm
			}
			System.out.println("Total of "+dictionary.size()+" terms read from index file.");
			return true;


		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static <T> Set<T> getCommonElements(Collection<? extends Collection<T>> collections) {

		//function to get intersection of passed arrayLists.
		Set<T> common = new LinkedHashSet<T>();
		if (!collections.isEmpty()) {
			Iterator<? extends Collection<T>> iterator = collections.iterator();
			common.addAll(iterator.next());
			while (iterator.hasNext()) {
				common.retainAll(iterator.next());
			}
		}
		return common;
	}

	public String andQuery(String terms)
	{
		String[] splitTerms = terms.split((" "));
		String response = ""; 
		if(splitTerms.length >0 && splitTerms.length < 2)
		{
			if(dictionary.containsKey(splitTerms[0])) //Single word query.
			{
				ArrayList<String> element = dictionary.get(splitTerms[0]);
				response = element.toString().substring(1, element.toString().length()-1);;
			}
		} else {

			ArrayList<List<String>> t = new ArrayList<List<String>>();
			for(int k=0;k<splitTerms.length;k++)
			{
				if(dictionary.containsKey(splitTerms[k]))
				{
					List<String> element = dictionary.get(splitTerms[k]);

					t.add(element);
				}
			}

			response = getCommonElements(t).toString();



		}

		return response;


	}

	public String orQuery(String terms)
	{
		String[] splitTerms = terms.split((" "));
		String response = ""; 
		if(splitTerms.length >0 && splitTerms.length < 2)
		{
			if(dictionary.containsKey(splitTerms[0]))
			{
				ArrayList<String> element = dictionary.get(splitTerms[0]);
				response = element.toString().substring(1, element.toString().length()-1);;
			}
		} else {
			ArrayList<String>  t = new ArrayList<String>();
			
			for(int k=0;k<splitTerms.length;k++)
			{
				if(dictionary.containsKey(splitTerms[k]))
				{
					List<String> element = dictionary.get(splitTerms[k]);

					t.addAll(element);
				}
			}
			
			

			Set<String> hs = new HashSet<>();
			hs.addAll(t);
			t.clear();
			t.addAll(hs);

			
			response = t.toString();
		}


		return response;
	}

	private boolean isMemoryAvailable() {
		int mb = 1024;
		Runtime runtime = Runtime.getRuntime();

		//initialMemory  =java.lang.Runtime.getRuntime().totalMemory();
		//long currentMemory = java.lang.Runtime.getRuntime().freeMemory();
		//long usedMemory = initialMemory - currentMemory/mb;
		long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / mb;
		if (usedMemory > BLOCK_SIZE) {
			return false;
		} else {
			return true;
		}

	}
}
