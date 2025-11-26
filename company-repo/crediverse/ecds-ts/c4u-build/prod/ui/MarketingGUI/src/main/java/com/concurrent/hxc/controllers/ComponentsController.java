package com.concurrent.hxc.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/components")
public class ComponentsController
{

	// Loads the livedemos.jsp page
	@RequestMapping("/livedemos")
	public String livedemos() {
		return "/components/livedemos";
	}
	
	// Downloads the APK file
	@RequestMapping("/download/apk")
	public void downloadApk(HttpServletResponse response)
	{
		try {
			
			// Refer to the android file
	        String filePathToBeServed = "/var/opt/cs/c4u/android/c4u.apk";
	        File fileToDownload = new File(filePathToBeServed);
	        
	         // Create the input stream
	        InputStream inputStream = new FileInputStream(fileToDownload);
	        
	        // Set the header information
	        response.setContentType("application/force-download");
	        response.setHeader("Content-Disposition", "attachment; filename=c4u.apk");
	        
	        // Copy the file to the output stream
	        IOUtils.copy(inputStream, response.getOutputStream());
	        
	        // Flush the buffer and close the input stream
	        response.flushBuffer();
	        inputStream.close();
	        
	    } catch (Exception e){
	        
	    	
	    }
	}
	
}
