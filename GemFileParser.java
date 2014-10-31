import java.util.regex.*;
import java.util.*;
import java.io.*;

public class Parser {
	
	public static String patternParse(String file){
		BufferedReader in;
		String line;
		Pattern p;
		Matcher m;
		line = null;
		String results;
		results = null;
		
		
		String regex = "(\\d*)(\\s*?)(@.*?@|INDI|NAME|SEX|BIRT|DEAT|FAMC|FAMS|FAM|MARR|HUSB|WIFE|CHIL|DIV|DATE|TRLR|NOTE)\\s*(.*)";
		p = Pattern.compile(regex);
		
		try {
		    in = new BufferedReader(new FileReader(file));
		    while ((line = in.readLine()) != null) {
			m = p.matcher(line);
			if (m.find()) { 
				if (results != null){
				    results = results + "\n" + "+" + line;
				}
				else {
				    results = "+" + line;
				}
			}
			else {
				if (results != null){
				    results = results + "\n" + "-" + line;
				}
				else {
				    results = "-" + line;
				}
			}
		    }
		}
		catch (IOException e) {
		    e.printStackTrace();
		}
	 //Debug
	 //System.out.print(results);
		return results;
	}
		    

  
    
    public static Map<String, Object> parse(String file) {

    	Map<String, Object> dataMap = new HashMap<String, Object>();
    	Map<String, Individual> Individuals = new HashMap<String, Individual>();
    	Map<String, Family> Families = new HashMap<String, Family>();
    	
    	BufferedReader in;
    	String line;
    	Pattern p;
    	Matcher m;
    	line = null;
    	String id = null;

    	String regex = "(\\d*)(\\s*?)(@.*?@|INDI|NAME|SEX|BIRT|DEAT|FAMC|FAMS|FAM|MARR|HUSB|WIFE|CHIL|DIV|DATE|TRLR|NOTE)\\s*(.*)";
    	p = Pattern.compile(regex);

    	try {
    	    in = new BufferedReader(new FileReader(file));
    	    List<String> recordNotes = new ArrayList<String>();
    	    
    	    while ((line = in.readLine()) != null) {
    	  
    		m = p.matcher(line);
    		if (m.find()) {
    			
    			if (m.group(3).equals("NOTE")) {
    				recordNotes.add(m.group(4));
    			}
    			
    			
    		    if (m.group(1).indexOf("0") != -1 && m.group(3).indexOf('@') != -1) {
    			if (m.group(4).trim().equals("INDI")) {
    			    Individuals.put(m.group(3), new Individual(m.group(3)));
    			    id = m.group(3);
    			    if (!recordNotes.isEmpty()) {
    			    	for (String note : recordNotes){
    			    	Individuals.get(m.group(3)).addNote(note);
    			    	}
    			    	recordNotes.clear();
    			    }
   
    			} else if (m.group(4).equals("FAM")) {
    			    Families.put(m.group(3), new Family(m.group(3)));
    			    id = m.group(3);
    			    if (!recordNotes.isEmpty()) {
    			    	for (String note : recordNotes){
    			    	Families.get(m.group(3)).addNote(note);
    			    	}
    			    	recordNotes.clear();
    			    }
    			}
    		    } else {
    			//IND properties
    			if (m.group(3).equals("NAME"))
    				Individuals.get(id).setName(m.group(4));
    			if (m.group(3).equals("SEX"))
    				Individuals.get(id).setSex(m.group(4));
    			if (m.group(3).equals("BIRT")) {
    			    m = p.matcher(in.readLine());
    			    if (m.find())
    			    Individuals.get(id).setBirth(m.group(4));
    			}
    			if (m.group(3).equals("DEAT")) {
    			    m = p.matcher(in.readLine());
    			    if (m.find())
    			    Individuals.get(id).setDeath(m.group(4));
    			}
    			if (m.group(3).equals("FAMS"))
    				Individuals.get(id).addSpouse(m.group(4));
    			if (m.group(3).equals("FAMC"))
    				Individuals.get(id).addChild(m.group(4));
			
    			//FAM properties
    			if (m.group(3).equals("HUSB")) {
    				Families.get(id).addHusband(m.group(4));
				if (Families.get(id).getHusband().size() > 1)
				    Families.get(id).addNote("Family has multiple husbands");
			}
    			if (m.group(3).equals("WIFE")) {
    				Families.get(id).addWife(m.group(4));
				if (Families.get(id).getWife().size() > 1)
				    Families.get(id).addNote("Family has multiple wives");
			}
    			if (m.group(3).equals("MARR")) {
    			    m = p.matcher(in.readLine());
    			    if (m.find())
    			    	Families.get(id).setMarr(m.group(4));
    			}
    			if (m.group(3).equals("DIV")) {
    			    m = p.matcher(in.readLine());
    			    if (m.find())
    			    Families.get(id).setDiv(m.group(4));
    			}
    			if (m.group(3).equals("CHIL"))
    				Families.get(id).addChild(m.group(4));

    			if (m.group(3).equals("TRLR"))
    			    break;
    		    }
    		}
    	    }
    	} catch (IOException e) {
    	    e.printStackTrace();
    	}
    	
    	dataMap.put("individuals", Individuals);
    	dataMap.put("families", Families);
        
    	
    	return dataMap;	
        }
    
    public static void createReport(Map<String, Object> datamap){
    	BufferedWriter b;
    	Map<String, Individual> Individuals = (Map<String, Individual>) datamap.get("individuals");
    	Map<String, Family> Families = (Map<String, Family>) datamap.get("families");
    	try {
    	    b = new BufferedWriter(new FileWriter("GEDReport.txt"));
        	b.write("******INDIVIDUAL RECORDS******\n");
    	    for (Individual record : Individuals.values()) {
    	    	b.write(record.getIndividualRecordString());
        	}
        	b.write("******FAMILY RECORDS******\n");
    	    for (Family record : Families.values()) {
    	    	b.write(record.getFamilyRecordString());
        	}
 
    	    b.close();
    	} catch (Exception e) {
    	    System.err.println("Error: " + e.getMessage());
    	}
    }


    public static void main(String[] args) {
    	
	//replace "data" with filename
	createReport(parse("Team1TestDataUserStories-Revised.ged"));
    	
    	
    	
    }
}