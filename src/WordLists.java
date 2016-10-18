import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class WordLists {


	public static String[] getStopWordsFromFile (String fileName)
	{
		ArrayList<String> list = new ArrayList<String>();

		File file = new File(System.getProperty("user.dir") + "/stopwords/"+fileName);
		FileReader reader = null;

		try {
			reader = new FileReader(file);
		
		char[] chars = new char[(int) file.length()];
		reader.read(chars);
		reader.close();
		
		String content = new String(chars);
		StringTokenizer stopWords = new StringTokenizer(content);
		while(stopWords.hasMoreTokens())
		{
			list.add(stopWords.nextToken());
		}
		
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			return new String[0];
		} catch (IOException e) {
			// TODO Auto-generated catch block
			return new String[0];
		}
		
		return list.toArray(new String[list.size()]);

	}

}
