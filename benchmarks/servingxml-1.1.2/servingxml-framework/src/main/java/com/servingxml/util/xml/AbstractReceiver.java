package com.servingxml.util.xml;

import com.servingxml.util.Name;

public abstract class AbstractReceiver implements Receiver {

  private final Receiver parent;
  private final Name name;
  private int selfSym;
  private Receiver[] receivers = new Receiver[0];
  private int selfLevel;
  private int childLevel;
  private final Name[] attributeNames;
  private String[] attributeValues;

  public AbstractReceiver(Name name, Receiver parent) {
    this.name = name;
    this.selfSym = Receiver.UNDEFINED_SYMBOL;
    this.selfLevel = -1;
    this.childLevel = -1;
    this.attributeNames = new Name[0];
    this.attributeValues = new String[0];
    this.parent = parent;
  }

  public AbstractReceiver(Name name, Name[] attributeNames, Receiver parent) {
    this.name = name;
    this.selfSym = Receiver.UNDEFINED_SYMBOL;
    this.selfLevel = -1;
    this.childLevel = -1;
    this.attributeNames = attributeNames;
    this.attributeValues = new String[attributeNames.length];
    this.parent = parent;
  }                                                             

  public int getSymbol() {
    return selfSym;
  }

  public void bind(ReceiverContext context, Receiver[] receivers) {

    this.selfSym = context.getSymbol(name);

    this.receivers = receivers;
  }

  public void startElement(ReceiverContext context) {
    //System.out.println(getClass().getName()+".startElement element="+context.getCurrentElementName()
    //  + ", level = " + context.getLevel() + ", selfLevel = " + selfLevel + ", parent = " + context.getParentElementName()
    //  + ", selfSym=" + selfSym + ", currentSym="+context.getCurrentElementSymbol());
    if (selfLevel == -1 && selfSym == context.getCurrentElementSymbol()) {
      //System.out.println("self level");
      selfLevel = context.getLevel();
      childLevel = selfLevel+1;
      for (int i = 0; i < attributeNames.length; ++i) {
       //System.out.println(getClass().getName()+".startElement " + attributeNames[i]);
        attributeValues[i] = context.getCurrentElementAttributes().getValue(attributeNames[i].getNamespaceUri(),
                  attributeNames[i].getLocalName());
      }
      for (int i = 0; i < receivers.length; ++i) {
        Receiver receiver = receivers[i];
        receiver.bind(context);
      }
    } else {
      for (int i = 0; i < receivers.length; ++i) {
        Receiver receiver = receivers[i];
        receiver.startElement(context);
      }
    }
  }

  public void characters(ReceiverContext context, char[] ch, int start, int length) {
    if (selfLevel == context.getLevel()) {
    } else if (childLevel == context.getLevel()) {
      for (int i = 0; i < receivers.length; ++i) {
        Receiver receiver = receivers[i];
        receiver.characters(context,ch,start,length);
      }
    }
  }

  public void endElement(ReceiverContext context) {
    //System.out.println(getClass().getName()+".endElement element="+context.getCurrentElementName()
    //  + ", level = " + context.getLevel() + ", selfLevel = " + selfLevel + ", parent = " + context.getParentElementName()
    //  + ", selfSym=" + selfSym + ", currentSym="+context.getCurrentElementSymbol());
    if (selfLevel == context.getLevel() && selfSym == context.getCurrentElementSymbol()) {
      //System.out.println("self level");
      selfReceived(context);
      if (parent != null) {
        parent.childReceived(selfSym);
      }
      selfLevel = -1;
      childLevel = -1;
      this.attributeValues = new String[attributeNames.length];
    } else {
      //System.out.println("child level");
      for (int i = 0; i < receivers.length; ++i) {
        Receiver receiver = receivers[i];
        receiver.endElement(context);
      }
    }
  }

  public Name getName() {
    return name;
  }

  public void childReceived(int symbol) {
  }

  protected void selfReceived(ReceiverContext context) {
  }

  public String getAttributeValue(int i) {
    return attributeValues[i];
  }
}
