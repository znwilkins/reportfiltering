
import java.net.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.io.*;
import java.util.*;

import javax.xml.parsers.*;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;

import com.opencsv.CSVReader;

/**
 * This class contains methods and nested classes
 * to read in JSON, XML, and CSV files, and
 * output a CSV file in the specified criteria.
 * <p></p>
 * This solution was created for Mariner as
 * a programming task.
 * <p></p>
 * Dependencies for compilation include
 * Google's GSON (v 2.7) and
 * Glen Smith's OpenCSV (v 3.8). Both of
 * these are used under the Apache 2.0
 * license.
 * <p></p>
 * This software is written in Java,
 * using Java SDK 1.8.
 * <p></p>
 * Thank you for considering this solution!
 * 
 * @author Zachary Wilkins
 */
public class ReportFiltering{
	
	/** This field contains the location of the report files. */
	protected static String loc = "./reports/";
	/** This field stores an initial array size for the report list. */
	private static int arrSize = 900;
	
	/**
	 * The Report class is a class to
	 * store information that is parsed from
	 * JSON, XML and CSV files. It has
	 * setters for all fields, and
	 * pertinent getters for this project.
	 *  
	 * @author Zachary Wilkins
	 */
	public static class Report implements Comparable<Report>{
		
		@SerializedName("max-hole-size")
		private int maxHoleSize;
		@SerializedName("packets-serviced")
		private int packetsServiced;
		@SerializedName("packets-requested")
		private int packetsRequested;
		@SerializedName("client-guid")
		private String clientGUID;
		@SerializedName("client-address")
		private InetAddress clientAddress;
		@SerializedName("request-time")
		private long requestTime;
		@SerializedName("service-guid")
		private String serviceGUID;
		@SerializedName("retries-request")
		private int retriesRequest;
		
		public void setMaxHoleSize(int mhs){
			maxHoleSize = mhs;
		}
		
		public void setPacketsServiced(int ps){
			packetsServiced = ps;
		}
		
		public int getPacketsServiced(){
			return packetsServiced;
		}
		
		public void setPacketsRequested(int pr){
			packetsRequested = pr;
		}
		
		public void setClientGUID(String guid){
			clientGUID = guid;
		}
		
		public void setClientAddress(String ca){
			try{
				clientAddress = InetAddress.getByName(ca);
			}
			catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}
		
		public void setRequestTime(long rt){
			requestTime = rt;
		}
		
		public long getRequestTime(){
			return requestTime;
		}
		
		public void setServiceGUID(String guid){
			serviceGUID = guid;
		}
		
		public String getServiceGUID(){
			return serviceGUID;
		}
		
		public void setRetriesRequest(int rr){
			retriesRequest = rr;
		}
		
		/** The default constructor. */
		public Report(){
		}
		
		/**
		 * This method converts a long integer
		 * representing time since epoch into
		 * a human readable String.
		 * @return a formatted String
		 */
		public String timeToString(){
			Date date = new Date(requestTime);
			// Example: 2016-06-29 07:22:30 ADT	
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
			sdf.setTimeZone(TimeZone.getTimeZone("America/Halifax"));
			
			return sdf.format(date);
		}
		
		/**
		 * This overridden method returns a
		 * String representation of the report
		 * in a CSV friendly format.
		 */
		public String toString(){
			StringBuilder sb = new StringBuilder();
			sb.append(
					clientAddress.getHostAddress() + "," +
					clientGUID + "," +
					timeToString() + "," +
					serviceGUID + "," +
					retriesRequest + "," +
					packetsRequested + "," +
					packetsServiced + "," +
					maxHoleSize + "\n"
					);
			
			return sb.toString();
		}

		/**
		 * This overridden method compares reports
		 * based upon their request time.
		 * The default ordering is that
		 * newer request times are greater.
		 */
		public int compareTo(Report r) {
			if(r.getRequestTime() > this.getRequestTime())
				return -1;
			else if(r.getRequestTime() < this.getRequestTime())
				return 1;
			else
				return 0;
		}
	}
	
	/**
	 * This extended class is equipped to parse XML
	 * reports and create Java object Reports.
	 * @author Zachary Wilkins
	 */
	private static class XMLHandler extends DefaultHandler{
		
		/** The report being assembled. */
		private Report tempRep;
		/** The completed reports. */
		private LinkedList<Report> reports;
		/** A temporary storage space for integers. */
		private int tempInt;
		/** A temporary storage space for longs. */
		private long tempLong;
		/** A temporary storage space for Strings. */
		private String tempString;
		/** A flag to indicate the respective data type. */
		private boolean intFlag, longFlag, stringFlag;
		
		/** The default constructor. */
		public XMLHandler(){
			reports = new LinkedList<>();
		}
		
		/**
		 * This overridden method determines if a new report
		 * is beginning, or if an attribute is being read.
		 */
		public void startElement(String uri, String localName,
									String qName, Attributes attributes)
											throws SAXException
		{
			// Reset the holders
			tempInt = 0;
			tempLong = 0L;
			tempString = null;
			
			/* This series of statements flips
			the appropriate flag when a particular
			data type is to be read in. */
			if(qName.equals("packets-serviced"))
				intFlag = true;
			else if(qName.equals("client-guid"))
				stringFlag = true;
			else if(qName.equals("packets-requested"))
				intFlag = true;
			else if(qName.equals("service-guid"))
				stringFlag = true;
			else if(qName.equals("retries-request"))
				intFlag = true;
			else if(qName.equals("request-time"))
				longFlag = true;
			else if(qName.equals("client-address"))
				stringFlag = true;
			else if(qName.equals("max-hole-size"))
				intFlag = true;
			else if(qName.equals("report"))
				tempRep = new Report();
		}

		/**
		 * This overridden method stores the presently used
		 * characters in an appropriate data type.
		 */
		public void characters(char ch[], int start, int length)
	    		throws SAXException
	    {
			String word = new String(ch, start, length);
			if(intFlag){
				tempInt = Integer.valueOf(word);
				intFlag = false;
			}
			else if(longFlag){
				try{
					tempLong = parseDate(word);
					longFlag = false;
				}
				catch(ParseException e){
					e.printStackTrace();
				}
			}
			else if(stringFlag){
				tempString = word;
				stringFlag = false;
			}
	    }
		
		/**
		 * This overridden method looks for XML
		 * properties, and either sets values or
		 * saves the report to a list.
		 */
		public void endElement(String uri, String localName, String qName)
				throws SAXException
		{
			if(qName.equals("packets-serviced"))
				tempRep.setPacketsServiced(tempInt);
			else if(qName.equals("client-guid"))
				tempRep.setClientGUID(tempString);
			else if(qName.equals("packets-requested"))
				tempRep.setPacketsRequested(tempInt);
			else if(qName.equals("service-guid"))
				tempRep.setServiceGUID(tempString);
			else if(qName.equals("retries-request"))
				tempRep.setRetriesRequest(tempInt);
			else if(qName.equals("request-time"))
				tempRep.setRequestTime(tempLong);
			else if(qName.equals("client-address"))
				tempRep.setClientAddress(tempString);
			else if(qName.equals("max-hole-size"))
				tempRep.setMaxHoleSize(tempInt);
			else if(qName.equals("report"))
				reports.add(tempRep);
		}
		
		public List<Report> getReports(){
			return reports;
		}
	}
	
	/**
	 * The main method, where the program runs from.
	 * @param args arguments, not used
	 */
	public static void main(String[] args){
		ArrayList<Report> reports = new ArrayList<>(arrSize);
		
		try{
			reports.addAll(Arrays.asList(parseJSON()));
			reports.addAll(parseCSV());
			reports.addAll(parseXML());
		}
		catch (Exception e){
			e.printStackTrace();
		}
		
		// Remove records with packet request == 0
		pruneList(reports);
		
		// Order by time
		Collections.sort(reports);
		
		// Create file
		writeCSV(reports);
		
		// Print out summary
		printSummary(countGUID(reports));

	}
	
	/**
	 * This method uses GSON to set the values
	 * of each report using reflection.
	 * @return a Report array, containing all reports from the JSON file
	 * @throws Exception if the file pathname is null, or if the file
	 * is not found by the scanner
	 */
	private static Report[] parseJSON() throws Exception{
		//TODO Need to do a relative file location
		Gson gson = new Gson();
		File file = new File(loc + "reports.json");
		StringBuilder sb = new StringBuilder();
		Scanner input = new Scanner(file);
		
		// Parse entire file
		while(input.hasNext())
			sb.append(input.next());
		
		input.close();
		return gson.fromJson(sb.toString(), Report[].class);
	}
	
	/**
	 * This method uses OpenCSV to set the values
	 * of each report using the setters.
	 * @return a list of reports from the CSV file
	 * @throws Exception if the file is not found, or if the reader
	 * cannot read the file, or if the date cannot be parsed
	 */
	private static ArrayList<Report> parseCSV() throws Exception{
	     CSVReader reader = new CSVReader(new FileReader(loc + "reports.csv"));
	     List<String[]> csvEntries = reader.readAll();
	     reader.close();
	     
	     // Need to hold the n - 1 entries in Report objects
	     ArrayList<Report> reps = new ArrayList<>(csvEntries.size() - 1);
	     
	     // Work through every entry (except for the first one!)
	     for(int i = 1; i < csvEntries.size(); ++i){
	    	 Report cur = new Report();
	    	 String[] arr = csvEntries.get(i);
	    	 
	    	 // Work through every field
	    	 cur.setClientAddress(arr[0]);
	    	 cur.setClientGUID(arr[1]);
	    	 cur.setRequestTime(parseDate(arr[2]));
	    	 cur.setServiceGUID(arr[3]);
	    	 cur.setRetriesRequest(Integer.valueOf(arr[4]));
	    	 cur.setPacketsRequested(Integer.valueOf(arr[5]));
	    	 cur.setPacketsServiced(Integer.valueOf(arr[6]));
	    	 cur.setMaxHoleSize(Integer.valueOf(arr[7]));
	    	 
	    	 reps.add(cur);
	     }
	     
	     return reps;
	}
	
	/**
	 * This method parses a formatted String to
	 * determine a long representation.
	 * @param fd a formatted String
	 * @return a long representation of the String
	 * @throws ParseException if the String cannot
	 * be parsed
	 */
	private static long parseDate(String fd) throws ParseException{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
		sdf.setTimeZone(TimeZone.getTimeZone("America/Halifax"));
		Date date = sdf.parse(fd);
		
		return date.getTime();
	}
	
	/**
	 * This method use the XML functionality provided by
	 * Java and the XMLHandler to generate a list
	 * of reports from an XML file.
	 * @return a list of Reports
	 * @throws Exception if there is a parser error
	 */
	private static List<Report> parseXML() throws Exception{
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser parser = factory.newSAXParser();
		XMLHandler handler = new XMLHandler();
		File file = new File(loc + "reports.xml");

		parser.parse(file, handler);
		
		return handler.getReports();
	}
	
	/**
	 * This method iterates over a list and
	 * removes any reports that match
	 * the specified criteria.
	 * @param list the reports to evaluate
	 */
	private static void pruneList(ArrayList<Report> list){
		Iterator<Report> it = list.iterator();
		
		while(it.hasNext()){
			Report curRep = it.next();
			if(curRep.getPacketsServiced() == 0)
				it.remove();
		}
	}
	
	/**
	 * This method counts the number of reports
	 * per service GUID using a HashMap.
	 * @param list the reports to analyze
	 * @return a HashMap of GUIDs and occurrences
	 */
	private static HashMap<String,Integer> countGUID(List<Report> list){
		HashMap<String,Integer> hm = new HashMap<>();
		
		for(int i = 0; i < list.size(); ++i){
			String guid = list.get(i).getServiceGUID();
			
			if(hm.containsKey(guid))
				hm.put(guid, hm.get(guid) + 1);
			else
				hm.put(guid, 1);
		}
		
		return hm;
	}
	
	/**
	 * This method prints a summary of each service
	 * GUID and the number of times it has been recorded.
	 * @param hm the HashMap containing the keys and values
	 */
	private static void printSummary(HashMap<String,Integer> hm){
		Set<String> set = hm.keySet();
		Iterator<String> it = set.iterator();
		
		System.out.println("--Summary--");
		while(it.hasNext()){
			String key = it.next();
			System.out.println(key + ": " + String.format("%02d", hm.get(key)) + " records");
		}
	}
	
	/**
	 * This method writes a CSV file out to disk
	 * from a list of Reports.
	 * @param list a collection of reports to write
	 */
	private static void writeCSV(List<Report> list){
		try{
			FileWriter writer = new FileWriter("output.csv");
			Iterator<Report> it = list.iterator();
			String[] reports = new String[list.size() + 1];
			reports[0] =
					"client-address,client-guid,request-time,service-guid,"
					+ "retries-request,packets-requested,packets-serviced,max-hole-size\n";
			int i = 1;
			
			// Iterate through list to create String[]
			while(it.hasNext()){
				reports[i] = it.next().toString();
				++i;
			}
			
			// Write out file, line by line
			for(int j = 0; j < reports.length; ++j){
				writer.write(reports[j]);
			}
			writer.close();
		}
		catch (IOException e){
			e.printStackTrace();
		}
	}
}
