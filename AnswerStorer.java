package personal.projects;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.io.PrintWriter;
import java.io.File;
import java.util.Scanner;
import javax.swing.JOptionPane;
import java.util.ArrayList;

//TODO: Allow user to delete temporary databases
//TODO: Allow user to save TestNGo mid truth-value filling.
//TODO: Allow Test-N-Go entries to confirm that a duplicate/similar questions don't exist.
//TODO: Allow the user to view the search results, look at a question, then return to the previous search results
//TODO: Allow user to delete an entire database.
public class AnswerStorer {
	static LinkedHashMap<String, HashMap<String, Boolean>> Database = new LinkedHashMap<String, HashMap<String, Boolean>>();
	static LinkedHashMap<String, HashMap<String, Boolean>> TempDatabase = new LinkedHashMap<String, HashMap<String, Boolean>>();
	static ArrayList<String> DatabaseList = new ArrayList<String>();
	static ArrayList<String> tempDatabaseList = new ArrayList<String>();
	static String CurrentDatabaseName = null;
	static String CurrentTempDatabaseName = null;
	static int CurrentTempDatabaseStage = -1;
	static int[] CurrentTempDatabaseInformationArray = null; 
	//contains last filled question, answer choice if stage = 1
	
	static final String baseDirectoryPath = "C:/DrettPrograms/DrettJava/AnswerStorer/Databases";
	static final String version = "1.1";
	static final String databaseDiscriminant = "Database:True | Type:"+version;
	static final String tempDatabaseDiscriminant = "temp"+databaseDiscriminant+" | Phase:";
	public static void main(String[] args) {
		start();
	}
	/**
	 * Starts program by updating database list and running loadDatabase method
	 */
	public static void start() {
		/*while (true) {
			System.out.println(detectBadDatabaseName(JOptionPane.showInputDialog("Database name tester")));
		}*/
		
		printVersion();
		updateDatabaseList();
		loadDatabase();
	}
	
	public static void printVersion() {
		JOptionPane.showMessageDialog(null, "Greetings. Answer-Storer "
				+ "Version: "+version);
	}
	
	static String[] mainOptionsOptions = {"Search By List","Search by text", "Add Question", "TestNGo Mode", "Load TestNGo Temporary Database", "Change Database", "Go back"};
	public static void mainOptions() {
		boolean loop = true;
		String message = "Database "+CurrentDatabaseName+"\n"
				+ "Choose an option.";
		String selection = "";
		while (loop) {
			selection = (String)JOptionPane.showInputDialog(null, message, "Main Options", 1, null, mainOptionsOptions, mainOptionsOptions[0]);
			if (selection == null) {
				JOptionPane.showMessageDialog(null, "No option was selected. If you wish to go back, select the \"Go back\" option.");
			}
			//Search By List
			else if (selection == mainOptionsOptions[0]) {
				searchDatabaseList();
			}
			//Search By Text
			else if (selection == mainOptionsOptions[1]) {
				searchDatabase();
			}
			//Add question
			else if (selection == mainOptionsOptions[2]) {
				addQuestion();
			}
			//Test n Go Mode
			else if (selection == mainOptionsOptions[3]) {
				if (CurrentTempDatabaseName == null) {
					testNGoMode();
				}
				else {
					int temp_input = JOptionPane.showConfirmDialog(null, "You've loaded the temporary database \""+CurrentTempDatabaseName+"\"\n"
							+ "Would you like to continue working with this database?");
					//Yes
					if (temp_input == 0) {
						testNGoMode();
					}
					//No
					else if (temp_input == 1){
						int temp_input_2 = JOptionPane.showConfirmDialog(null, "Discard current Temporary database and continue with a blank one?");
						//Yes, continue with blank temp database
						if (temp_input_2 == 0) {
							JOptionPane.showMessageDialog(null, "Discarded "+CurrentTempDatabaseName+". Continuing with blank temporary database");
							resetTempDatabaseVariables();
							testNGoMode();
						}
						//No or Cancel
						else {
							//Do nothing, go back to main options
						}
					}
					//Cancel
					else if (temp_input == 2){
						//Do nothing, go back to main options
					}
				}
				
			}
			//Load Temp Test n Go Mode
			else if (selection == mainOptionsOptions[4]) {
				updateTempDatabaseList();
				if (tempDatabaseList.size() > 0) {
					String input = (String)JOptionPane.showInputDialog(null, "Tempdatabase list", "", 3, null, tempDatabaseList.toArray(), tempDatabaseList.toArray()[0]);
					if (input != null) {
						loadTempDatabaseVariables(input);
						JOptionPane.showMessageDialog(null, "Loaded "+CurrentTempDatabaseName);
						//resumeTestNGo();
					}
					else {
						break;
					}
				}
				else {
					JOptionPane.showMessageDialog(null, "There are no temporary databases for this database");
				}
			}
			//Change Database
			else if (selection == mainOptionsOptions[5]) {
				loadDatabase();
			}
			//Go Back
			else if (selection == mainOptionsOptions[6]) {
				break;
			}
		}
		
	}
	
	
	/**
	 * Not to be confused with seachDatabaseList()
	 * Allows user to search the database of questions using text
	 */
	public static void searchDatabase() {
		String input = null;
		boolean loop = true;
		while (loop) {
			input = JOptionPane.showInputDialog(null, "Question Search | What is the question?", "");
			if (input == null) {
				loop = false;
			}
			else {
				input = input.toLowerCase().trim();
				String searchChoice = searchForQuestion(input,"Search result for \""+input+"\"");
				if (searchChoice == null || searchChoice.equals("E")) {
					short choice = (short)JOptionPane.showConfirmDialog(null, input+" does not exist.\n"
							+ "Would you like to add this question to the database?");
					if (choice == 0) {
						addQuestion(input);
					}
					else {
						
					}
				}
				else {
					getKeyValues(searchChoice);
				}
				HashMap<String, Boolean> result = Database.get(input);
				if (result == null) {
					
				}
				else {
					JOptionPane.showMessageDialog(null, "Question: "+input+" found.");
					getKeyValues(input);
				}
			}
		}
	}
	
	/**
	 * Not to be confused with seachDatabase()
	 * Allows user to search the database of questions, but in list format
	 */
	public static void searchDatabaseList() {
		boolean loop = true;
		String selection = null;
		while(loop) {
			if(Database.size()==0) {
				JOptionPane.showMessageDialog(null, "Database is empty. Add something first.");
				break;
			}
			selection = (String)JOptionPane.showInputDialog(null, "Choose a question from the existing list of questions", "Select a question", 1, null, Database.keySet().toArray(), Database.keySet().toArray()[0]);
			if (selection == null) {
				loop = false;
			}
			else {
				getKeyValues(selection);
			}
		}
	}
	
	
	/**
	 * 
	 */
	static String[] getKeyValuesOptions = 
		{
			"Add Answer Choice",
			"Change Answer True or False Value",
			"Delete Answer Choice",
			"Delete Current Question"
				};
	public static void getKeyValues(String key) {
		//Creates the string that will contain all the possible choices for the current question
		//Plus whether they were true or not
		HashMap<String, Boolean> answerInfo = Database.get(key);
		String message = "";
		////////
		boolean loop = true;
		String option = null;
		while (loop) {
			message = getAnswerChoicesString(key);
			message+="\nSelect an answer choice.";
			option = (String)JOptionPane.showInputDialog(null, message, "Select an option", 1, null, getKeyValuesOptions, getKeyValuesOptions[0]);
			if (option == null) {
				loop = false;
			}
			else {
				if (option == getKeyValuesOptions[0]) {
					addQuestion(key);
				}
				else if (option == getKeyValuesOptions[1]) {
					String selection = selectAChoice(answerInfo.keySet().toArray(),message);
					if (selection == null) {
					}
					else {
						flipValue(answerInfo, selection);
					}
				}
				else if (option == getKeyValuesOptions[2]) {
					String selection = selectAChoice(answerInfo.keySet().toArray(),message);
					if (selection == null) {
						
					}
					else {
						deleteValue(answerInfo, selection);
						message = "";
						for(Object k:answerInfo.keySet().toArray()) {
							message += k+": "+answerInfo.get(k)+"\n";
						}
					}
				}
				else if (option == getKeyValuesOptions[3]) {
					//Confirms if user wants to delete current question
					if (JOptionPane.showConfirmDialog(null, "Sure you want to delete the question \n\""+key+"\"?") == 0) {
						Database.remove(key);
						JOptionPane.showMessageDialog(null, "Successfully removed the question \""+key+"");
						break;
					}
					
				}
			}
		}
	}
	
	/**
	 * Receives a question (key) that is present in the database.
	 * Allows user to modify question located in the database using a JOptionPane menu.
	 * @param key the question (more exactly the key) that is present in the database passed
	 * @param database contains question and answer-choice:truth-value pairs
	 */
	public static void getKeyValuesUniversal(String key, LinkedHashMap<String, HashMap<String, Boolean>> database) {
		
		HashMap<String, Boolean> answerInfo = database.get(key);
		
		//Creates the string that will contain all the possible choices for the current question
		//Plus whether they were true or not
		String message = "";
		
		boolean loop = true;
		String option = null;
		
		
		//Finish method to allow user to modify questions within the temporary database (Test-n-go)
		while (loop) {
			//Contains a string which will used as the message portion of the below JOptionPane input menu
			message = key+"\n"
					+ "-----------\n"
					+ getAnswerChoicesStringUniversal(key, database)+"\n"
					+ "Select an answer choice";
			option = (String)JOptionPane.showInputDialog(null, message, "Select an option", 1, null, getKeyValuesOptions, getKeyValuesOptions[0]);
			if (option == null) {
				loop = false;
			}
			else {
				//Adds question answer choices
				if (option == getKeyValuesOptions[0]) {
					addQuestionUniversal(key, database);
				}
				//Change answer truth value
				else if (option == getKeyValuesOptions[1]) {
					String selection = selectAChoice(answerInfo.keySet().toArray(),message);
					if (selection == null) {
					}
					else {
						flipValue(answerInfo, selection);
					}
				}
				else if (option == getKeyValuesOptions[2]) {
					String selection = selectAChoice(answerInfo.keySet().toArray(),message);
					if (selection == null) {
						
					}
					else {
						deleteValue(answerInfo, selection);
						message = "";
						for(Object k:answerInfo.keySet().toArray()) {
							message += k+": "+answerInfo.get(k)+"\n";
						}
					}
				}
				else if (option == getKeyValuesOptions[3]) {
					//Confirms if user wants to delete current question
					if (JOptionPane.showConfirmDialog(null, "Sure you want to delete the question \n\""+key+"\"?") == 0) {
						database.remove(key);
						JOptionPane.showMessageDialog(null, "Successfully removed the question \""+key+"");
						break;
					}
					
				}
			}
		}
	}
	
	/**
	 * Calls addQuestion without having the question defined yet.
	 */
	public static void addQuestion() {
		addQuestion(null);
	}
	
	/**
	 * Asks question if necessary, plus gets answer choices for question.
	 * @param question | if the question is already defined, it will skip asking for the question
	 */
	public static void addQuestion(String question) {
		boolean addQ = true;
		if(question == null) {
			question = JOptionPane.showInputDialog("What is the question?");
			if (question == null) { addQ = false; }
		}
		if (addQ) {
			question = question.trim();
			HashMap<String, Boolean> answerChoices = null;
			
			//assigns the value of answerChoices
			if(Database.get(question) == null) { answerChoices = new HashMap<String, Boolean>(); }
			else { answerChoices = Database.get(question); }
			
			boolean loop = true;
			String input = "";
			Database.put(question, answerChoices);
			while (loop) {

				input = JOptionPane.showInputDialog(null, "Question: "+question+"\n"+getAnswerChoicesString(question)+"\nWhat is the answer choice?");
				if (input == null) {
					loop = false;
				}
				else {
					addValue(answerChoices, input.trim());
				}
			}
		}
		else {
			JOptionPane.showMessageDialog(null, "Question not added");
		}
	}
	
	
	/**
	 * Use this method to add question and answer choices to a database of your choice. Database must heed to database format.
	 * One large quirk to note is the fact that it will not ask whether or not the answer is true, it will just enter it as false.
	 * This is because it is used in the testNGoMode() where it doesn't ask you the value till the end.
	 * @param question is the key in the database that points to a answer choice HashMap.
	 * @param database a LinkedHashMap whose type is (String, HashMap[String, Boolean]) (Database format)
	 */
	public static void addQuestionUniversal(String question, LinkedHashMap<String, HashMap<String, Boolean>> database) {
		boolean addQ = true;
		if(question == null) {
			question = JOptionPane.showInputDialog("What is the question?");
			if (question == null) { addQ = false; }
		}
		if (addQ) {
			question = question.trim();
			HashMap<String, Boolean> answerChoices = null;
			
			//assigns the value of answerChoices
			if(database.get(question) == null) { answerChoices = new HashMap<String, Boolean>(); }
			else { answerChoices = database.get(question); }
			
			boolean loop = true;
			String input = "";
			database.put(question, answerChoices);
			while (loop) {

				input = JOptionPane.showInputDialog(null, "Question: "+question+"\n"+getAnswerChoicesStringUniversal(question, database)+"\nWhat is the answer choice?");
				if (input == null) {
					loop = false;
				}
				else {
					//unlike the regular version, it enters the value as false without asking the user
					answerChoices.put(input, false);
				}
			}
			if (answerChoices.size()>0) {
				database.put(input, answerChoices);
			}
			else {
				JOptionPane.showMessageDialog(null, question+" does not have any answer choices. Discarding current question.");
			}
		}
		else {
			JOptionPane.showMessageDialog(null, "Question not added");
		}
	}
	
	public static String selectAChoice(Object[] options, String message) {
		return (String)JOptionPane.showInputDialog(null, message, "Select a choice", 1, null, options, options[0]);
	}
	
	static Boolean[] addValueOptions = {true, false};
	/**
	 * 
	 * @param map | the answer choice set of the current question.
	 * @param key | the current answer choice in the answer choice set we want to add the value of.
	 * @return true if value was added, false if value was not.
	 */
	public static boolean addValue(HashMap<String, Boolean> map, String key) {
		 Object input = JOptionPane.showInputDialog(null, key+"\nWas the answer choice true or false?", "Sekect answer choice value", 1, null, addValueOptions, addValueOptions[0]);
		 if (input == null) {
			 return false;
		 }
		 boolean value = (Boolean)input;
		 map.put(key, value);
		 JOptionPane.showMessageDialog(null, "Added Answer choice "+key+": "+value);
		 saveDatabase();
		 return true;
	}
	
	/**
	 * 
	 * @param map
	 * @param key
	 * @param Question
	 * @return true if value was added, false if value was not.
	 */
	public static boolean addValue(HashMap<String, Boolean> map, String key, String Question) {
		Object input = JOptionPane.showInputDialog(null, Question+": "+key+"\nWas the answer choice true or false?", "Sekect answer choice value", 1, null, addValueOptions, addValueOptions[0]);
		 if (input == null) {
			 return false;
		 }
		 boolean value = (Boolean)input;
		 map.put(key, value);
		 JOptionPane.showMessageDialog(null, "Added Answer choice "+key+": "+value);
		 saveDatabase();
		 return true;
	}
	
	public static void flipValue(HashMap<String, Boolean> map, String key) {
		map.put(key, !map.get(key));
		JOptionPane.showMessageDialog(null, "Changed "+key+" from "+!map.get(key)+" to "+map.get(key));
		saveDatabase();
	}
	
	public static void deleteValue(HashMap<String, Boolean> map, String key) {
		map.remove(key);
		JOptionPane.showMessageDialog(null, "Removed the answer choice \""+key+"\"");
		saveDatabase();
	}
	
	public static String getAnswerChoicesString(String key) {
		String message = "";
		HashMap<String, Boolean> answerInfo = Database.get(key);
		for(Object k:answerInfo.keySet().toArray()) {
			message += k+": "+answerInfo.get(k)+"\n";
		}
		return message;
	}
	
	/**
	 * Differs from the non-universal getAnswerChoicesString by allowing the programmer to send
	 * a database-formatted LinkedHashMap as a parameter. The method then grabs the HashMap that corresponds with the
	 * current question (key) and compiles all the answer-choice:true-value sets into one string.
	 * The string is then returned
	 * @param key the current question in the database
	 * @param database contains question:answer-choices pairs.
	 * @return a string containing all answer-choice:true-value corresponding with current key, separated by a new line.
	 */
	public static String getAnswerChoicesStringUniversal(String key, LinkedHashMap<String, HashMap<String, Boolean>> database) {
		String message = "";
		HashMap<String, Boolean> answerInfo = database.get(key);
		for(Object k:answerInfo.keySet().toArray()) {
			message += k+": "+answerInfo.get(k)+"\n";
		}
		return message;
	}
	
	static String[] loadDatabaseOptions = null;
	public static void loadDatabase() {
		setLoadDatabaseOptions();
		String selection = null;
		boolean loop = true;
		
		while (loop) {
			selection = (String)JOptionPane.showInputDialog(null, "Select a database", "Select Database", 0, null, loadDatabaseOptions, loadDatabaseOptions[0]);
			if(selection == null) {
				loop = false;
			}
			else {
				//third last option == [Update Database List]
				if (selection == loadDatabaseOptions[loadDatabaseOptions.length-3]) {
					updateDatabaseList();
					setLoadDatabaseOptions();
				}
				//second last option == [Create new Database]
				else if (selection == loadDatabaseOptions[loadDatabaseOptions.length-2]) {
					createNewDatabase();
					updateDatabaseList();
					setLoadDatabaseOptions();
				}
				else if (selection == loadDatabaseOptions[loadDatabaseOptions.length-1]) {
					exit();
				}
				else {
					loadDatabaseVariables(selection);
				}
			}
		}
	}
	
	public static void setLoadDatabaseOptions() {
		int size = DatabaseList.size() + 3;
		loadDatabaseOptions = new String[size];
		for (int index = 0; index<loadDatabaseOptions.length-3; index++) {
			loadDatabaseOptions[index] = DatabaseList.get(index);
		}
		loadDatabaseOptions[size-3] = "[Update Database List]";
		loadDatabaseOptions[size-2] = "[Create new Database]";
		loadDatabaseOptions[size-1] = "[Exit]";
		
	}
	/**
	 * Used to check for valid databases within the baseDirectoryPath
	 */
	public static void updateDatabaseList() {
		DatabaseList.clear();
		File baseDir = new File(baseDirectoryPath);
		if (baseDir.exists()) {
			File[] potentialDatabases = baseDir.listFiles();
			for (File f: potentialDatabases) {
				String fileName = f.getName().substring(0,f.getName().lastIndexOf('.'));
				String fileExtention = f.getName().substring(f.getName().lastIndexOf('.')+1);
				if (detectBadDatabaseName(fileName)) {
					System.out.println(f.getName()+" Database name contains an illegal character");
					continue;
				}
				if (fileExtention.equals("txt")) {
					try {
						Scanner lineReader = new Scanner(f);
						if (lineReader.hasNext()) {
							String discriminantChunk = lineReader.nextLine();
							if (discriminantChunk.equals(databaseDiscriminant)) {
								DatabaseList.add(f.getName());
							}
							else {
								//txt file did not contain the discriminant
								System.out.println(f.getName()+" is not empty, but doesn't contain"
										+ "Discriminant");
							}
						}
						else {
							//Empty txt file
							System.out.println(f.getName()+" is empty");
						}
						lineReader.close();
					}
					catch(Exception e) {
						System.out.println(e);
					}
				}
				else {
					System.out.println(f.getName()+" is not a valid file name");
				}
			}
		}
	}
	
	
	public static void updateTempDatabaseList() {
		tempDatabaseList.clear();
		File baseDir = new File(baseDirectoryPath);
		if (baseDir.exists()) {
			File[] potentialDatabases = baseDir.listFiles();
			for (File f: potentialDatabases) {
				//everything before fileExtension
				String fileName = f.getName().substring(0,f.getName().lastIndexOf('.'));
				String databaseName = null;
				try {
					databaseName = fileName.substring(0,fileName.lastIndexOf("-"));
				}
				catch(Exception e) {
					System.out.println(e);
				}
				if(databaseName == null) {
					System.out.println(f.getName()+" is not a temporary database");
				}
				else {
					if(databaseName.equals(CurrentDatabaseName)) {
						String temporaryDatabaseName = fileName.substring(fileName.lastIndexOf("-")+1);
						if(temporaryDatabaseName.isBlank()) {
							System.out.println(f.getName()+" is a temp database, but doesn't belong to the "+CurrentDatabaseName+" database");
						}
						else {
							//everything after fileExtention
							String fileExtention = f.getName().substring(f.getName().lastIndexOf('.')+1);
							if (fileExtention.equals("txt")) {
								try {
									Scanner lineReader = new Scanner(f);
									if (lineReader.hasNext()) {
										String discriminantChunk = lineReader.nextLine();
										if (discriminantChunk.length() == tempDatabaseDiscriminant.length()+1) {
											if (discriminantChunk.contains(tempDatabaseDiscriminant)) {
												tempDatabaseList.add(temporaryDatabaseName);
											}
											else {
												//txt file did not contain the discriminant
												System.out.println(fileName+" is not empty, but doesn't contain"
														+ " the correct Discriminant");
											}
										}
										else {
											System.out.println(fileName+" is not empty, but doesn't contain"
													+ " the correct Discriminant");
										}
									}
									else {
										//Empty txt file
										System.out.println("File is empty");
									}
									lineReader.close();
								}
								catch(Exception e) {
									System.out.println(e);
								}
							}
							else {
								System.out.println(f.getName()+" is not a valid file name");
							}
						}
					}
					else {
						System.out.println(f.getName()+" is a temp database, but doesn't belong to the "+CurrentDatabaseName+" database");
					}
				}
			}
		}
	}
	
	
	//Format for database code
		//Discriminant = Database:True | Type:1.1
		//Question:|:AnswerChoice#?true/false/|/AnswerChoice2#?true/false...
		//Question2:|:AnswerChoice#?true/false/|/AnswerChoice2:true#?false...
		//...
	/**
	 * Loading method for version 1.1
	 * Discriminant = Database:True | Type:1.1
	 * @param fileName
	 */
	public static void loadDatabaseVariables(String fileName) {
		Database = new LinkedHashMap<String, HashMap<String, Boolean>>();
		CurrentDatabaseName = fileName.substring(0,fileName.lastIndexOf("."));//takes the first part of the name
		File currentFile = new File(baseDirectoryPath+"/"+fileName);
		try {
			Scanner reader = new Scanner(currentFile);
			reader.nextLine(); //skips the discriminant line
			String currentLine = null;
			while (reader.hasNext()) {
				currentLine = reader.nextLine();
				String[] split1 = currentLine.split("[:][|][:]");
				String question = split1[0];
				String[] split2 = split1[1].split("[/][|][/]"); //Answer Choices and values
				HashMap<String, Boolean> AnswerSet = new HashMap<String, Boolean>();
				for(String s:split2) {
					String[] answerSplit = s.split("[#][?]");
					AnswerSet.put(answerSplit[0], Boolean.parseBoolean(answerSplit[1]));
				}
				Database.put(question, AnswerSet);
			}
			reader.close();
		}
		catch(Exception e) {
			System.out.println("Something went very wrong in loadDatabaseVariables()\n"
					+ e.getMessage());
			System.exit(1);
		}
		mainOptions();
		//System.out.println("Database "+fileName+" was successfully loaded");
	}
	
	//Format for database code
	//Discriminant
	//Question|AnswerChoice:true/false,AnswerChoice2:true/false...
	//Question2|AnswerChoice:true/false,AnswerChoice2:true/false...
	//...
	/**
	 * This was the loading method for version 1.0
	 * Updated from this version because it would prevent users from using commas in question or answer
	 * Discriminant = Database:True | Type:1
	 * @param fileName
	 */
	public static void loadDatabaseVariablesLegacy_1_0(String fileName) {
		Database = new LinkedHashMap<String, HashMap<String, Boolean>>();
		CurrentDatabaseName = fileName.split("[.]")[0]; //takes the first part of the name
		File currentFile = new File(baseDirectoryPath+"/"+fileName);
		try {
			Scanner reader = new Scanner(currentFile);
			reader.nextLine(); //skips the discriminant line
			String currentLine = null;
			while (reader.hasNext()) {
				currentLine = reader.nextLine();
				String[] split1 = currentLine.split("[|]");
				String question = split1[0];
				String[] split2 = split1[1].split("[,]"); //Answer Choices and values
				HashMap<String, Boolean> AnswerSet = new HashMap<String, Boolean>();
				for(String s:split2) {
					String[] answerSplit = s.split("[:]");
					AnswerSet.put(answerSplit[0], Boolean.parseBoolean(answerSplit[1]));
				}
				
				Database.put(question, AnswerSet);
			}
			reader.close();
		}
		catch(Exception e) {
			System.out.println("Something went very wrong in loadDatabaseVariables()\n"
					+ e.getMessage());
			System.exit(1);
		}
		//System.out.println("Database "+fileName+" was successfully loaded");
	}
	
	/**
	 * Loads a temporary database (unfinished database, usually from TestNGoMode) into
	 * TempDatabase and CurrentTempDatabaseName
	 * Note: Only works when a database is loaded.
	 * @param tempDatabaseName
	 */
	public static void loadTempDatabaseVariables(String tempDatabaseName) {
		String tempDatabaseFileName = CurrentDatabaseName+"-"+tempDatabaseName+".txt";
		CurrentTempDatabaseName = tempDatabaseName;
		try {
			File tempDatabaseFile = new File(baseDirectoryPath+"/"+tempDatabaseFileName);
			Scanner reader = new Scanner(tempDatabaseFile);
			String discriminant = reader.nextLine(); //First line is always discriminant
			
			//Last character of discriminant contains the stage of the Temporary Database
			CurrentTempDatabaseStage = Integer.parseInt(discriminant.charAt(discriminant.length()-1)+"");
			
			String currentLine = "";
			while (reader.hasNext()) {
				currentLine = reader.nextLine();
				//the last line containing the current to-be-filled truth value line
				//only present when stage = 1 (truth filling stage)
				if (currentLine.substring(0,3).equals("!#!")) {
					System.out.println("Contains informationArrayLine");
					String[] informationArray = currentLine.substring(3).split("[,]");
					CurrentTempDatabaseInformationArray = new int[2];
					CurrentTempDatabaseInformationArray[0] = Integer.parseInt(informationArray[0]);
					CurrentTempDatabaseInformationArray[1] = Integer.parseInt(informationArray[1]);
				}
				else {
					String[] split1 = currentLine.split("[:][|][:]");
					String question = split1[0];
					String[] split2 = split1[1].split("[/][|][/]"); //Answer Choices and values
					HashMap<String, Boolean> AnswerSet = new HashMap<String, Boolean>();
					for(String s:split2) {
						String[] answerSplit = s.split("[#][?]");
						AnswerSet.put(answerSplit[0], Boolean.parseBoolean(answerSplit[1]));
					}
					TempDatabase.put(question, AnswerSet);
				}
			}
			reader.close();
			System.out.println("Sub temp of: "+CurrentDatabaseName+"\n"
					+ "Temp Name: "+CurrentTempDatabaseName+"\n"
					+ "TempDatabase Stage: "+CurrentTempDatabaseStage+"\n"
					+ "Number of questions: "+TempDatabase.size());
			if (CurrentTempDatabaseInformationArray != null) {
				System.out.println("Last saved question number: "+CurrentTempDatabaseInformationArray[0]+"\n"
						+ "Last saved answer choice in question: "+CurrentTempDatabaseInformationArray[1]+"\n");
			}
		}
		catch(Exception e) {
			
		}
	}
	
	public static void createNewDatabase() {
		String Name = "";
		boolean loop = true;
		while (loop) {
			Name = JOptionPane.showInputDialog("Name the new database\n"
					+ "Do not include any special characters (! , . ? s _ ... etc.)\n"
					+ "Or file extentions (.txt, .apk, ...etc.)\n\n"
					+ "Press Cancel to go back");
			if (Name == null) {loop = false;}
			else {
				
				if (detectBadDatabaseName(Name)) {
					JOptionPane.showMessageDialog(null, Name+" is not a valid database name.\n"
							+ "Illegal characters are / \\ * : , ? \" < > | -");
					
				}
				else {
					if (initializeNewDatabase(Name)) {
						JOptionPane.showMessageDialog(null, "Successfully created "+
								Name+" database");
						break;
					}
					else {
						JOptionPane.showMessageDialog(null, "Database \""+Name+"\"already exists.");
					}
					
				}
			}
			
		}
		
	}
	
	
	
	/**
	 * @param value
	 * 0 = from scratch | 1 = loaded incomplete question database | 2 = loaded complete question database with incomplete truth values
	 */
	static String[] testNGoModeExitOptions = {"Keep entering questions","View/Edit Questions","Go to next part", "Save and resume later", "Discard Current TestNGo Session"};
	static String[] testNGoModeExitOptions2 = {"View/Edit Quesitons", "Save and resume later", "Discard Current TestNGo Session"};
	static String tempDatabaseName = null;
	public static void testNGoMode() {
		LinkedHashMap<String, HashMap<String, Boolean>> tempDatabase = null;
		boolean fullExit = false;
		int numQuestions = -1; //this value will be defined in the first line of the while loop below
		
		//initializes TestNGoMode if a database wasn't loaded
		if (CurrentTempDatabaseStage == -1) {
			CurrentTempDatabaseStage = 0;
			numQuestions = 1;
			tempDatabase = new LinkedHashMap<String, HashMap<String, Boolean>>(); 
		}
		else {
			tempDatabase = TempDatabase;
			numQuestions = tempDatabase.size()+1;
		}
		while (true) {
			if (fullExit) { break; }
			if (CurrentTempDatabaseStage == 0) {
				String question = "";
				//this loop will run as long as the user is still adding questions
				while(true) {
					if (fullExit) { break; }
					question = JOptionPane.showInputDialog("Current Temporary Database Name: "+CurrentTempDatabaseName+"\n"+
							"Q"+numQuestions+". What is the question?\n"
							+ "Press Cancel to STOP and either CONTINUE, SAVE, or VIEW QUESTIONS");
					
					if (question != null) {
						if (question.isBlank()) { 
							JOptionPane.showMessageDialog(null, "Blank questions are not accepted. Try again.");
						}
						question = question.trim();
						// != 0 means they selected something else but yes.
						if (JOptionPane.showConfirmDialog(null, "Sure you want to add \""+question+"\"?") != 0) {
							continue;
						}
						else {
							String searchResults = searchForQuestion(question, "Found a close match to your current question already present in this database.\n"
									+ "Press Cancel if none of the questions match");
							if (searchResults == null) {
								//they did not choose an existing question, we will continue with current question
							}
							else if (searchResults.equals("E")) {
								//No matching searches. We continue with current question
							}
							else {
								//they chose an existing question.
								question = searchResults;
							}
							HashMap<String, Boolean> tempAnswerSet = new HashMap<String, Boolean>();
							boolean loop2 = true;
							String answerChoice = "";
							while (loop2) {
								answerChoice = JOptionPane.showInputDialog("Enter answer choices for "+question);
								if (answerChoice == null) {
									if (tempAnswerSet.size() == 0) {
										//number of answer choices added is equal to zero. Discard current question.
										JOptionPane.showMessageDialog(null, "You entered no answer choices for the current question.\n"
												+ "This question will be discarded.");
									}
									else {
										numQuestions++;
									}
									loop2 = false;
								}
								else if (answerChoice.isBlank()) {
									JOptionPane.showMessageDialog(null, "Blank answer choices are not accepted. Try again.");
								}
								else {
									//confirms user wants to add the answer choice
									if (JOptionPane.showConfirmDialog(null, "Sure you want to add \""+answerChoice+"\"?") != 0) {
										continue;
									}
									else {
										tempAnswerSet.put(answerChoice, false);
									}
								}
							}
							//only adds the current question if there are one or more answer choices given
							if (tempAnswerSet.size() > 0) {
								tempDatabase.put(question, tempAnswerSet);
							}
						}
					}
					//if the current question entered is null
					else {
						String response = null;
						while (response == null) {
							if (fullExit) { break; }
							response = (String)JOptionPane.showInputDialog(null, "Would you like to:\nContinue?\nGo to next part?\nSave and Exit?", 
									"Test-N-Go prompt", 2, null, testNGoModeExitOptions, testNGoModeExitOptions[0]);
							if (response == null) {
								break;
							}
							//Keep entering questions
							else if (response.equals(testNGoModeExitOptions[0])) {
								continue;
							}
							//View or Edit questions
							else if (response.equals(testNGoModeExitOptions[1])) {
								viewQuestions(tempDatabase);
							}
							//Go on to next part
							else if (response.equals(testNGoModeExitOptions[2])) {
								fullExit = true;
								CurrentTempDatabaseStage = 1;
							}
							//Save and exit
							else if (response.equals(testNGoModeExitOptions[3])) {
								while (true) {
									if (fullExit) { break; }
									String name = null;
									if (CurrentTempDatabaseName != null) { name = CurrentTempDatabaseName; }
									else {
										name = JOptionPane.showInputDialog("What do you want to name your temporary database?\n"
											+ "none of the following characters\n"
											+ blocked_special_string);
										if(name == null) {
											break;
										}
										if (detectBadDatabaseName(name)) {
											JOptionPane.showMessageDialog(null, name+" contains an illegal character. Try again");
											continue;
										}
										else if (tempDatabaseNameExists(name)) {
											JOptionPane.showMessageDialog(null, "Temporary Database name already exists.\nChoose a different name.");
											continue;
										}
									}
									
									if (name != null) {
											TempDatabase = tempDatabase; //assigns global variable TempDatabase
											CurrentTempDatabaseName = name;
											saveTempDatabase();
											JOptionPane.showMessageDialog(null, "Saved "+CurrentTempDatabaseName+". Exiting TestNGoMode.");
											resetTempDatabaseVariables();
											
											fullExit = true;
									}
									else {
										//goes back out to show testNGoModeExitOptions
										break;
									}
								}
							}
							//Discard current set
							else if (response.equals(testNGoModeExitOptions[4])) {
								int temp_res = JOptionPane.showConfirmDialog(null, "Sure you want to Discard the current TestNGo session?\n"
										+ "Any unsaved data will be lost.");
								if (temp_res != 0) {
									response = null;
								}
								else {
									fullExit = true;
								}
							}
							else {
								// how the hell did this happen?
								// User managed to pick an option index that is outside of the testNGoModeExitOptions length
							}
							
							//exits from this method
							if (fullExit) { break; }
						}
						
					}
					
				}
			}
			//TODO: Give user the option to modify entries and save mid edit.
			if (CurrentTempDatabaseStage == 1) {
				TempDatabase = tempDatabase;
				fullExit = false;
				int QuestionIndex = 0;
				int subQuestionIndex = 0;
				boolean upToSpeed = false;
				for (String q: tempDatabase.keySet()) {
					if(fullExit) { break; }
					if (CurrentTempDatabaseInformationArray != null) {
						//if the loaded TempDatabase has already had portions of the truth values determined
						if (CurrentTempDatabaseInformationArray[0] > QuestionIndex) {
							//skips current question
							QuestionIndex++;
							continue;
						}
					}
					else {
						System.out.println("InformationArray is null");
						upToSpeed = true;
					}
					HashMap<String, Boolean> tempAnswerSet = tempDatabase.get(q);
					
					for (String answerChoice: tempAnswerSet.keySet()) {
						if(fullExit) { break; }
						if (!upToSpeed) {
							if (subQuestionIndex < CurrentTempDatabaseInformationArray[1]) {
								subQuestionIndex++;
								continue;
							}
							else {
								upToSpeed = true;
							}
						}
						if(addValue(tempAnswerSet, answerChoice, q)) {
							
						}
						else {
							String choice = "";
							while (true) {
								choice = (String)JOptionPane.showInputDialog(null, "Select an option for current TestNGo session for "+CurrentDatabaseName+"\n"
										+ "Temporary Database name: "+tempDatabaseName, "", 3, null, testNGoModeExitOptions2, testNGoModeExitOptions2[0]);
								if (choice == null) {
									//it will try to add current answer choice again.
									if(addValue(tempAnswerSet, answerChoice, q)) {
										break;
									}
									else {
										//otherwise, ask user what they want to do again.
									}
								}
								//View current questions and answer choices
								else if (choice == testNGoModeExitOptions2[0]) {
									viewQuestions(tempDatabase);
								}
								//Save current temp database
								else if (choice == testNGoModeExitOptions2[1]) {
									CurrentTempDatabaseInformationArray = new int[2];
									CurrentTempDatabaseInformationArray[0] = QuestionIndex;
									CurrentTempDatabaseInformationArray[1] = subQuestionIndex;
									saveTempDatabase();
									resetTempDatabaseVariables();
									fullExit = true;
									break;
								}
								//Discard current temp database
								else if (choice == testNGoModeExitOptions2[2]) {
									resetTempDatabaseVariables();
									fullExit = true;
									break;
								}
							}
						}
						subQuestionIndex++;
					}
					QuestionIndex++;
				}
				if(fullExit) { /*Do nothing*/ }
				else {
					saveTempDatabase();
					resetTempDatabaseVariables();
					addTempToDatabase(tempDatabase, Database);
					saveDatabase();
					JOptionPane.showMessageDialog(null, "TestNGoMode complete. Questions added to main database.");
					break;
				}
			}
		}
	}
	
	/**
	 * Adds information from one database to another
	 * @param tempDatabase Database to draw information out of
	 * @param database Database to add information to
	 */
	public static void addTempToDatabase(LinkedHashMap <String, HashMap<String, Boolean>> tempDatabase, LinkedHashMap <String, HashMap<String, Boolean>> database) {
		for (String tempKey: tempDatabase.keySet()) {
			
			if(DatabaseHasKey(database, tempKey)) {
				//this will contain all the answer choices from both the database and tempdatabase
				HashMap<String, Boolean> tempAnswerSet = database.get(tempKey);
				
				//this will contain the answer choices from the temp database only. The answer choices will be added
				//to the answer set above.
				HashMap<String, Boolean> transferFromTempSet = tempDatabase.get(tempKey);
				//Gets answer-choice sets from temp database and combines it with the one in database
				for(String tempAnswerChoice:tempDatabase.get(tempKey).keySet()) {
					tempAnswerSet.put(tempAnswerChoice, transferFromTempSet.get(tempAnswerChoice));
				}
			}
			else {
				HashMap<String, Boolean> tempAnswerSet = new HashMap<String, Boolean>();
				Database.put(tempKey, tempAnswerSet);
				HashMap<String, Boolean> transferFromTempSet = tempDatabase.get(tempKey);
				//Gets answer-choice sets from temp database and combines it with the one in database
				for(String tempAnswerChoice:tempDatabase.get(tempKey).keySet()) {
					tempAnswerSet.put(tempAnswerChoice, transferFromTempSet.get(tempAnswerChoice));
				}
			}
		}
	}
	
	public static boolean DatabaseHasKey(LinkedHashMap <String, HashMap<String, Boolean>> database, String key) {
		for (String keyInDatabase:database.keySet()) {
			if (key.equals(keyInDatabase)) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Receives a database and shows the user a JOptionPane menu to view all the questions in the database
	 * @param database the database whose format is [String, HashMap(String, Boolean)]
	 */
	public static void viewQuestions(LinkedHashMap <String, HashMap<String, Boolean>> database) {
		if (database.size() > 0) {
			String selection = null;
			while (true) {
				selection = (String)JOptionPane.showInputDialog(null, "Select a question", "Question viewer", 3, null, database.keySet().toArray(), database.keySet().toArray()[0]);
				
				//user selected nothing. Pressed cancel/closed prompt
				if (selection == null) {
					break;
				}
				else {
					getKeyValuesUniversal(selection, database);
				}
			}
			
		}
		else {
			JOptionPane.showMessageDialog(null, "Current database contains no entries.");
		}
	}
	
	/**
	 * Allows the user to input a search term
	 * @param question the desired question to be found (Or part of the question)
	 * @param prompt The prompt that is shown to the user when displaying the search results
	 * @return
	 */
	public static String searchForQuestion(String question, String prompt) {
		question = question.toLowerCase();
		String[] searchOptions = StringToSearchOptions(question);
		
		//will contain keys that match search from greatest to least relevance.
		LinkedHashSet<String> itemsFound = new LinkedHashSet<String>();
		for(int index = searchOptions.length -1; index> -1; index--) {
			for(String key:Database.keySet()) {
				String Lkey = key.toLowerCase();
				if (Lkey.contains(searchOptions[index])) {
					itemsFound.add(key);
					//adds key if it finds it not present in the list already, and relevant
				}
				
			}
			
		}
		if (itemsFound.size() == 0) {
			return "E";
		}
		else {
			return (String)JOptionPane.showInputDialog(null, prompt+"\nSearch results for "+question, "Search results", 2, null, itemsFound.toArray(), itemsFound.toArray()[0]);
		}
	}
	
	public static String[] StringToSearchOptions(String search) {
		search = search.toLowerCase();
		String[] split = search.split("[\\W_]");
		String search2 = removeSpecialCharacters(search);
		LinkedHashSet<String> searchList = new LinkedHashSet<String>();
		for(int index = 0, len = 0; index<split.length; index++) {
			len += split[index].length();
			searchList.add(search.substring(0, len+index));
			searchList.add(search2.substring(0, len+index)); //wont add if the previous value is the same.
			//adding substring of original that includes word + special characters that are inbetween words.
		}
		String[] ret = new String[searchList.size()];
		int index = 0;
		for(Object o:searchList.toArray()) {
			ret[index] = (String)o;
			index++;
		}
		return ret;
	}
	
	public static String removeSpecialCharacters(String word) {
		String[] split = word.split("[\\W_]");
		String returnString = "";
		for(String s:split) {
			returnString+=s+" ";
		}
		return returnString.trim();
	}
	
	public static boolean initializeNewDatabase(String fileName) {
		File f = new File(baseDirectoryPath+"/"+fileName+".txt");
		if (f.exists()) {
			return false;
		}
		else {
			//creates text file in main directory with databaseDiscriminant as first line.
			try {
				f.createNewFile();
				PrintWriter writer = new PrintWriter(f);
				writer.println(databaseDiscriminant);
				writer.close();
			}
			catch(Exception e) {
				//something goes wrong
				System.out.println("Something went wrong while trying to create file.\n"
						+ e.toString());
				System.exit(0);
			}
			return true;
		}
	}
	/**
	 * Detects if the given string is a valid database name
	 * Allows everything but what is contained in "blocked_special".
	 * Also does not allow an empty file name
	 * @param Name
	 * @return
	 */
	static final char[] blocked_special = {'/','\\','*',':','?','\"','<','>','|','-'};
	static final String blocked_special_string = "/ \\ * : ? \" > ? | -";
	public static boolean detectBadDatabaseName(String Name) {
		if (Name.isBlank() || Name.isEmpty()) {
			return true;
		}
		for(char c: Name.toCharArray()) {
			if (!Character.isLetterOrDigit(c) && !Character.isWhitespace(c)) { 
				boolean allowed = true;
				for (char blocked_char: blocked_special) {
					if (c == blocked_char) {
						allowed = false;
						break;
					}
				}
				if (!allowed) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * searches tempDatabaseList to find if an existing tempDatabase has a name equal to the one passed.
	 * @param tempDatabaseName Name to verify existence in tempDatabaseList
	 * @return
	 */
	public static boolean tempDatabaseNameExists(String tempDatabaseName) {
		updateTempDatabaseList();
		System.out.println("Looking for name match...");
		for (String name:tempDatabaseList) {
			System.out.println(name);
			if (tempDatabaseName.equals(name)) {
				return true;
			}
		}
		return false;
	}
	
	
	public static void saveDatabase() {
		File currentFile = new File(baseDirectoryPath+"/"+CurrentDatabaseName+".txt");
		try {
			PrintWriter writer = new PrintWriter(currentFile);
			writer.println(databaseDiscriminant);
			for(Object key:Database.keySet().toArray()) {
				String currentLine = ""+(String)key+":|:";
				HashMap<String, Boolean> answerSet = Database.get(key);
				for(Object answerChoice:answerSet.keySet()) {
					currentLine+=(String)answerChoice+"#?"+answerSet.get(answerChoice)+"/|/";
				}
				writer.println(currentLine.substring(0,currentLine.length()-3));
			}
			writer.close();
		}
		catch(Exception e) {
			System.out.println("Something went wrong while saving.");
		}
	}
	
	/**
	 * Saves the current temporaryDatabase being used in TestNGo
	 * @param name The name of the temporary Database
	 */
	public static void saveTempDatabase() {
		File currentFile = new File(baseDirectoryPath+"/"+CurrentDatabaseName+"-"+CurrentTempDatabaseName+".txt");
		System.out.println("Temp file exists?: "+currentFile.exists());
		System.out.println("TempDatabase Size: "+TempDatabase.size());
		try {
			PrintWriter writer = new PrintWriter(currentFile);
			writer.println(tempDatabaseDiscriminant+CurrentTempDatabaseStage);
			for(Object key:TempDatabase.keySet().toArray()) {
				String currentLine = ""+(String)key+":|:";
				HashMap<String, Boolean> answerSet = TempDatabase.get(key);
				for(Object answerChoice:answerSet.keySet()) {
					currentLine+=(String)answerChoice+"#?"+answerSet.get(answerChoice)+"/|/";
				}
				writer.println(currentLine.substring(0,currentLine.length()-3));
			}
			if (CurrentTempDatabaseInformationArray != null) {
				writer.println("!#!"+CurrentTempDatabaseInformationArray[0]+","+CurrentTempDatabaseInformationArray[1]);
			}
			writer.close();
		}
		catch(Exception e) {
			System.out.println("Something went wrong while saving.");
		}
	}
	
	/**
	 * resets all variables related to TempDatabase
	 */
	public static void resetTempDatabaseVariables() {
		TempDatabase = new LinkedHashMap<String, HashMap<String, Boolean>>();
		tempDatabaseList = new ArrayList<String>();
		CurrentTempDatabaseName = null;
		CurrentTempDatabaseStage = -1;
		CurrentTempDatabaseInformationArray = null; 
		
	}
	
	public static void exit() {
		saveDatabase();
		JOptionPane.showMessageDialog(null, "All changes saved. Goodbye");
		System.exit(0);
	}
	
	public static void databaseToString(LinkedHashMap<String, HashMap<String, Boolean>> database) {
		String[] strArr = new String[database.size()];
		int index = 0;
		for(String key:database.keySet()) {
			strArr[index] = key+"|";
			for(String answerChoice: database.get(key).keySet()) {
				strArr[index] += answerChoice+":"+database.get(key).get(answerChoice)+",";
			}
			strArr[index].substring(0, strArr[index].length()-1);
			index++;
		}
		JOptionPane.showInputDialog(null, "", "", 3, null, strArr, strArr[0]);
	}
	
}
