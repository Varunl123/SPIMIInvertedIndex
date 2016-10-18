import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MyProgram {

	
	public long initialMemory;
	public static void main(String[] args) {

		String corpusPath = System.getProperty("user.dir") + "/corpus/";
		String indexPath  = System.getProperty("user.dir") + "/indexOutput/";
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String s = "";

		SPIMIIndexer queryObject = new SPIMIIndexer(Integer.parseInt(args[0]));
		
		try {

				
				do {
					System.out.println("Please select an option below:");
					System.out.println("1) Create UnCompressed Index");
					System.out.println("2) Create Compressed Index");
					
					System.out.println("3) Load index to memory for query");
					System.out.println("4) Enter Query");
					System.out.println("5) Exit");
					
					s = br.readLine();
					
					switch(s) {
					
					case "1":
					{
						System.out.println("Creating UnCompressed Index....... Please wait");
						
						SPIMIIndexer spimi = new SPIMIIndexer(Integer.parseInt(args[0]));
						spimi.readFile(corpusPath,"uncompressed");
						int[] res = spimi.mergeIndexes(indexPath);
						

						System.out.println("");
						System.out.println("***********************************");
						System.out.println("Total # of Terms: "+res[0]);
						System.out.println("Total # of Posting: "+res[1]);
						System.out.println("***********************************");
						System.out.println("");
						System.out.println("UnCompressed Index Creation Completed.");
						
						break;
					}
					case "2":
					{
						System.out.println("Creating UnCompressed Index....... Please wait");
						
						SPIMIIndexer spimi = new SPIMIIndexer(Integer.parseInt(args[0]));
						spimi.readFile(corpusPath,"compressed");
						int[] res = spimi.mergeIndexes(indexPath);
						

						System.out.println("");
						System.out.println("***********************************");
						System.out.println("Total # of Terms: "+res[0]);
						System.out.println("Total # of Posting: "+res[1]);
						System.out.println("***********************************");
						System.out.println("");
						System.out.println("UnCompressed Index Creation Completed.");
						break;
					}
					case "3":
					{
						System.out.println("Reading Index file....... Please wait");
						
						queryObject.readIndexToMemory("uncompressed");
						System.out.println("");
						System.out.println("Read complete. please proceed to make a query.");
						break;
					}
					case "4":
					{
						System.out.println("");
						System.out.println("Select one of the following:");
						System.out.println("1) AND Query");
						System.out.println("2) OR Query");
						String t = br.readLine();
						switch(t) {
						case "1":
						{
							System.out.println("");
							System.out.println("Enter the search query:");
							String query = br.readLine();
							String response = queryObject.andQuery(query);
							System.out.println("*********************");
							if(!response.equals(""))
							{
								System.out.println("Returned Values: "+response);
							} else {
								System.out.println("No documents found");
							}
							System.out.println("*********************");
							System.out.println("");
							//AND Query
							break;
						}
						case "2":
							//OR Query
							System.out.println("");
							System.out.println("Enter the search query:");
							String query = br.readLine();
							String response = queryObject.orQuery(query);
							System.out.println("*********************");
							if(!response.equals(""))
							{
								System.out.println("Returned Values: "+response);
							} else {
								System.out.println("No documents found");
							}
							System.out.println("*********************");
							System.out.println("");
							break;
						}
						
					}
					}
				} while(!s.equals("5"));
				
				System.out.println("");
				System.out.println("Good Bye!");
				
				
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		

		
		

	}
	
	



}
