/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package utils;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 *
 * @author jceatwell
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