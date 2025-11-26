package hxc.userinterfaces.gui.controller;

import hxc.userinterfaces.gui.data.User;
import hxc.userinterfaces.gui.structs.BaseServlet;
import java.io.IOException;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.context.WebContext;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

@WebServlet(urlPatterns = { "/testdata" }, name = "TestData", asyncSupported = true)
@SuppressWarnings("serial")
public class TestData extends BaseServlet
{

	@Override
	public void defaultHandler(HttpServletRequest request, HttpServletResponse response, HttpSession session, User user, WebContext ctx) throws IOException, ServletException
	{
		String[] data = new String[] { "asd", "Armand", "56045", "srwedf", "some", "thing", "else" };
		Random rnd = new Random(System.currentTimeMillis());
		JsonArray jarr = new JsonArray();

		for (int i = 0; i < 100; i++)
		{
			JsonArray ja = makeElements(i, data[rnd.nextInt(7)], data[rnd.nextInt(7)], data[rnd.nextInt(7)], data[rnd.nextInt(7)], data[rnd.nextInt(7)]);
			jarr.add(ja);
		}

		JsonObject job = new JsonObject();
		job.add("data", jarr);
		String out = job.toString();
		sendResponse(response, out);
	}

	private JsonArray makeElements(int id, String data1, String data2, String data3, String data4, String data5)
	{
		JsonArray jarr = new JsonArray();
		jarr.add(new JsonPrimitive(String.valueOf(id)));
		jarr.add(new JsonPrimitive(data1));
		jarr.add(new JsonPrimitive(data2));
		jarr.add(new JsonPrimitive(data3));
		jarr.add(new JsonPrimitive(data4));
		jarr.add(new JsonPrimitive(data5));
		return jarr;
	}
}