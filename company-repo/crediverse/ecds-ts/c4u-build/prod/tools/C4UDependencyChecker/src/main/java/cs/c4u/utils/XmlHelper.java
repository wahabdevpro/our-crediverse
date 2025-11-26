package cs.c4u.utils;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 * @author johne
 */
public class XmlHelper
{
	public static <T extends Object> T extractClassFromXml(Class<T> clazz, File file) throws JAXBException
	{
		JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		@SuppressWarnings("unchecked")
		T unmarshal = (T) jaxbUnmarshaller.unmarshal(file);
		T result = unmarshal;
		return result;
	}
}