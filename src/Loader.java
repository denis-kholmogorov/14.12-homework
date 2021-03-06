import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class Loader
{
    private static SimpleDateFormat birthDayFormat = new SimpleDateFormat("yyyy.MM.dd");
    private static SimpleDateFormat visitDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

    private static HashMap<Integer, WorkTime> voteStationWorkTimes = new HashMap<>();
    private static HashMap<Voter, Integer> voterCounts = new HashMap<>();

    public static void main(String[] args) throws Exception
    {
        String fileName = "res/data-18M.xml";
        System.out.println("Начало цикла");
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();
        XMLHandler handler = new XMLHandler();
        XMLHandlerString handlerString = new XMLHandlerString();

        // DOMParser
        parseFile(fileName);

        // Неоптимизированный
        long usage = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        parser.parse(new File(fileName), handler);
        usage = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() - usage)/1024/1024;
        System.out.println(usage + " MB занимает парсер SAXParser до оптимизации \n");
        //handler.DuplicatedVoters();

        // Оптимизтрованный
        long usageS = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        parser.parse(new File(fileName), handlerString);
        usageS = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() - usageS)/1024/1024;
        System.out.println(usageS + " MB занимает парсер SAXParser после оптимизации \n");
        //handlerString.DuplicatedVotersString();



        //Printing results
        System.out.println("Voting station work times: ");
        for(int votingStation : voteStationWorkTimes.keySet())
        {
            WorkTime workTime = voteStationWorkTimes.get(votingStation);
            System.out.println("\t" + votingStation + " - " + workTime);
        }

        System.out.println("Duplicated voters: ");
        for(Voter voter : voterCounts.keySet())
        {
            int count = voterCounts.get(voter);
            if(count > 1) {
                System.out.println("\t" + voter + " - " + count);
            }
        }
    }

    private static void parseFile(String fileName) throws Exception
    {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        long usage = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        Document doc = db.parse(new File(fileName));
        usage = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() - usage)/1024/1024;
        System.out.println("\n" + usage + " MB занимает парсер DOMParser \n");

        //findEqualVoters(doc);
       // fixWorkTimes(doc);
    }

    private static void findEqualVoters(Document doc) throws Exception
    {
        NodeList voters = doc.getElementsByTagName("voter");
        int votersCount = voters.getLength();
        for(int i = 0; i < votersCount; i++)
        {
            Node node = voters.item(i);
            NamedNodeMap attributes = node.getAttributes();

            String name = attributes.getNamedItem("name").getNodeValue();
            Date birthDay = birthDayFormat.parse(attributes.getNamedItem("birthDay").getNodeValue());

            Voter voter = new Voter(name, birthDay);
            Integer count = voterCounts.get(voter);
            voterCounts.put(voter, count == null ? 1 : count + 1);
        }
    }

    private static void fixWorkTimes(Document doc) throws Exception
    {
        NodeList visits = doc.getElementsByTagName("visit");
        int visitCount = visits.getLength();
        for(int i = 0; i < visitCount; i++)
        {
            Node node = visits.item(i);
            NamedNodeMap attributes = node.getAttributes();

            int station = Integer.parseInt(attributes.getNamedItem("station").getNodeValue());
            Date time = visitDateFormat.parse(attributes.getNamedItem("time").getNodeValue());
            WorkTime workTime = voteStationWorkTimes.get(station);
            if(workTime == null)
            {
                workTime = new WorkTime();
                voteStationWorkTimes.put(station, workTime);
            }
            workTime.addVisitTime(time.getTime());
        }
    }
}