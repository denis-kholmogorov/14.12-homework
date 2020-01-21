import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class XMLHandler extends DefaultHandler
{
    private Voter voter;
    private static SimpleDateFormat birthDayFormat = new SimpleDateFormat("yyyy.MM.dd");
    private HashMap<Voter, Integer> voterCount;


    public XMLHandler(){
        voterCount = new HashMap<>();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
        try
        {
            if(qName.equals("voter") && voter == null)
            {
                Date birthDay = birthDayFormat.parse(attributes.getValue("birthDay"));
                voter = new Voter(attributes.getValue("name"),birthDay);
            }
            else if(qName.equals("visit") && voter != null)
            {
                int count = voterCount.getOrDefault(voter, 0);
                voterCount.put(voter, ++count);
            }
            else{
                throw new RuntimeException();
            }
        }catch (ParseException | RuntimeException e){
            e.getMessage();
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        if(qName.equals("voter"))
        {
            voter = null;
        }
    }

    public void DuplicatedVoters()
    {
        for (Voter voter: voterCount.keySet()){
            int count = voterCount.get(voter);
            if (count > 1){
                System.out.println(voter.toString() + " " + count);
            }


        }
    }
}
