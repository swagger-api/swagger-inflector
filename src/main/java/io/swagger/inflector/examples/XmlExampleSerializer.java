package io.swagger.inflector.examples;

import java.io.ByteArrayOutputStream;

import io.swagger.inflector.examples.models.ArrayExample;
import io.swagger.inflector.examples.models.Example;
import io.swagger.inflector.examples.models.ObjectExample;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class XmlExampleSerializer {
  public String serialize(Example o) {
    XMLStreamWriter writer = null;
    try {
      XMLOutputFactory f = XMLOutputFactory.newFactory();
      ByteArrayOutputStream out = new ByteArrayOutputStream();

      writer = f.createXMLStreamWriter(out);

      writer.writeStartDocument("UTF-8", "1.1");
      writeTo(writer, o);
      writer.close();
      return out.toString();
    }
    catch(XMLStreamException e) {
      e.printStackTrace();
      return null;
    }
  }

  public void writeTo(XMLStreamWriter writer, Example o) throws XMLStreamException {
    if(o instanceof ObjectExample) {
      ObjectExample or = (ObjectExample) o;

      writer.writeStartElement(o.getPrefix(), or.getName(), o.getNamespace());
      for(String key : or.keySet()) {
        Object obj = or.get(key);
        if(obj instanceof Example) {
          writeTo(writer, (Example) obj);
        }
      }
      writer.writeEndElement();
    }
    else if(o instanceof ArrayExample) {
      ArrayExample ar = (ArrayExample) o;
      if(o.getWrapped() != null && o.getWrapped()) {
        if(o.getWrappedName() != null) {
          writer.writeStartElement(o.getPrefix(), o.getWrappedName(), o.getNamespace());
        }
        else {
          writer.writeStartElement(o.getPrefix(), o.getName() + "s", o.getNamespace());
        }
      }
      for (Example item : ar.getItems()) {
        if(item.getName() == null) {
          writer.writeStartElement(o.getPrefix(), o.getName(), o.getNamespace());
        }
        writeTo(writer, item);
        if(item.getName() == null) {
          writer.writeEndElement();
        }
      }
      if(o.getWrapped() != null && o.getWrapped()) {
        writer.writeEndElement();
      }
    }
    else if(o.getAttribute() != null && o.getAttribute()) {
      writer.writeAttribute(o.getPrefix(), o.getName(), o.getNamespace(), o.asString());
    }
    else if(o.getName() == null) {
      writer.writeCharacters(o.asString());
    }
    else {
      writer.writeStartElement(o.getPrefix(), o.getName(), o.getNamespace());
      writer.writeCharacters(o.asString());
      writer.writeEndElement();
    }
  }
}
