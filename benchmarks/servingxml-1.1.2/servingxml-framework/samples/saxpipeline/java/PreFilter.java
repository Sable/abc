import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

public class PreFilter extends XMLFilterImpl {

  public void startElement (String uri, String localName, String qname,
  Attributes atts) throws SAXException {
    
    String newLocalName = localName.toLowerCase();
    String newlQname = qname.toUpperCase();
    AttributesImpl newAtts =
      (atts.getLength()>0 ?
       new AttributesImpl(atts) :
       new AttributesImpl());
    newAtts.addAttribute("", "old-local-name", "old-local-name", "CDATA", localName);
    newAtts.addAttribute("", "old-qname", "old-qname", "CDATA", qname);
    super.startElement(uri, newLocalName, newlQname, newAtts);
  }

  public void endElement (String uri, String localName, String qname)
  throws SAXException {
    String newLocalName = localName.toLowerCase();
    String newlQname = qname.toUpperCase();
    super.endElement(uri, newLocalName, newlQname);
  }
}

