package hxc.utils.xmlrpc;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class XmlRpcSerializerTest {

    static String invalidXmlTemplate_methodCall = "<?xml version=\"1.0\"?>\n" +
            "<methodCall>\n" +
            "<methodName>__NAME__</methodName>\n" +
            "<params>\n" +
            "<param>\n" +
            "<value>\n" +
            "<string>__STRING__</string>\n" + // Intentional switch of STRING and INT positions
            "</value>\n" +
            "</param>\n" +
            "<param>\n" +
            "<value>\n" +
            "<i4>__INT__</i4>\n" +
            "</value>\n" +
            "</param>\n" +
            "</params>\n" +
            "</methodCall>\n";

    static String xmlTemplate_methodCall = "<?xml version=\"1.0\"?>\n" +
            "<methodCall>\n" +
            "<methodName>__NAME__</methodName>\n" +
            "<params>\n" +
            "<param>\n" +
            "<value>\n" +
            "<i4>__INT__</i4>\n" +
            "</value>\n" +
            "</param>\n" +
            "<param>\n" +
            "<value>\n" +
            "<string>__STRING__</string>\n" +
            "</value>\n" +
            "</param>\n" +
            "</params>\n" +
            "</methodCall>\n";

    static String xmlTemplate_methodResponse = "<?xml version=\"1.0\"?>\n" +
            "<methodResponse>\n" +
            "<params>\n" +
            "<param>\n" +
            "<value>\n" +
            "<i4>__INT__</i4>\n" +
            "</value>\n" +
            "</param>\n" +
            "<param>\n" +
            "<value>\n" +
            "<string>__STRING__</string>\n" +
            "</value>\n" +
            "</param>\n" +
            "</params>\n" +
            "</methodResponse>\n";

    String GetBalanceAndDate_RequestFilePath = "./src/test/resources/GetBalanceAndDate_Request_Example.xml";
    String GetBalanceAndDate_ResponseFilePath = "./src/test/resources/GetBalanceAndDate_Response_Example.xml";

    static String readFile(String path) throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, StandardCharsets.UTF_8);
    }

    @Test
    public void serializeWithoutXmlRpcMethodAnnotation() {
        String xmlString = this.xmlTemplate_methodResponse
                            .replaceAll("__INT__", "5")
                            .replaceAll("__STRING__", "Your Name");

        XmlRpcSerializer xs = new XmlRpcSerializer();
        ToBeSerializedTest tbs = new ToBeSerializedTest();
        String myResponse = xs.serialize(tbs);
        assertEquals(myResponse, xmlString);
    }

    @Test
    public void serializeWithXmlRpcMethodAnnotation() {
        String xmlString = this.xmlTemplate_methodCall
                .replaceAll("__NAME__", "ToBeSerialised_XmlRpcMethod")
                .replaceAll("__INT__", "987000")
                .replaceAll("__STRING__", "0123456789");

        XmlRpcSerializer xs = new XmlRpcSerializer();
        ToBeSerializedXmlRpcMethodTest tbs = new ToBeSerializedXmlRpcMethodTest();
        String myResponse = xs.serialize(tbs);
        assertEquals(myResponse, xmlString);
    }

    @Test
    public void deSerializeWithValidTypes() throws XmlRpcException {
        String result = this.xmlTemplate_methodCall
                .replaceAll("__NAME__", "ToBeSerialised_XmlRpcMethod")
                .replaceAll("__INT__", "987011")
                .replaceAll("__STRING__", "012345");

        InputStream stream = new ByteArrayInputStream(result.getBytes());
        XmlRpcSerializer xs = new XmlRpcSerializer();
        ToBeSerializedXmlRpcMethodTest deSerializedStuff = xs.deSerialize(stream, ToBeSerializedXmlRpcMethodTest.class);
        assertEquals(deSerializedStuff.balance, 987011);
        assertEquals(deSerializedStuff.phone, "012345");
    }

    @Test
    public void deSerializeWithInvalidTypes() {
        String result = this.invalidXmlTemplate_methodCall
                .replaceAll("__NAME__", "ToBeSerialised_XmlRpcMethod")
                .replaceAll("__STRING__", "987011")
                .replaceAll("__INT__", "12345");


        InputStream stream = new ByteArrayInputStream(result.getBytes());
        XmlRpcSerializer xs = new XmlRpcSerializer();
        try {
            ToBeSerializedXmlRpcMethodTest deSerializedStuff = xs.deSerialize(stream, ToBeSerializedXmlRpcMethodTest.class);
        } catch(XmlRpcException e) {
            assertEquals(e.getMessage(), "Can not set java.lang.String field hxc.utils.xmlrpc.ToBeSerializedXmlRpcMethodTest.phone to java.lang.Integer");
        }
    }
}