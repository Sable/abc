import java.util.Stack;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

public class PostFilter extends XMLFilterImpl {

  public Stack<String> stack;

  public void startDocument() throws SAXException {
    stack = new Stack<String>();
    super.startDocument();
  }

  public void startElement (String uri, String localName, String qname,
                            Attributes atts)
  throws SAXException {

    String originalLocalName = localName;
    String originalQname = qname;
    AttributesImpl newAtts = new AttributesImpl();
    for (int i=0; i<atts.getLength(); i++) {
      String name = atts.getQName(i);
      String val = atts.getValue(i);
      if (name.equals("old-local-name")) {
        originalLocalName = val;
      } else if (name.equals("old-qname")) {
        originalQname = val;
      } else {
        newAtts.addAttribute(atts.getURI(i),
          atts.getLocalName(i),
          name,
          atts.getType(i),
          val);
      }
    }
    super.startElement(uri, originalLocalName, originalQname, newAtts);
    stack.push(originalLocalName);
    stack.push(originalQname);
  }

  public void endElement (String uri, String localName, String qname)
  throws SAXException {
    String originalQname = stack.pop();
    String originalLocalName = stack.pop();
    super.endElement(uri, originalLocalName, originalQname);
  }
}

