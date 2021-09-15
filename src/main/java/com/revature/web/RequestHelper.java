package com.revature.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revature.dao.EmployeeDao;
import com.revature.models.Employee;
import com.revature.service.EmployeeService;

public class RequestHelper {
	
	private static Logger log = Logger.getLogger(RequestHelper.class);
	
	private static EmployeeService eserv = new EmployeeService(new EmployeeDao());
	
	private static ObjectMapper om = new ObjectMapper();
	
	public static void processEmployees(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		response.setContentType("text/html");
		
		List<Employee> allEmps = eserv.findAll(); // create this in service layer
		
		// turn list to json string
		String json = om.writeValueAsString(allEmps);

		PrintWriter out = response.getWriter();
		out.println(json);
	}
	public static void processLogin(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		
		// we need to capture the user input and split up the username and password
		BufferedReader reader = request.getReader();
		
		StringBuilder s = new StringBuilder();
		
		//transfer everything over from the reader to the builder
		String line = reader.readLine();
		while (line!=null) {
			s.append(line);
			line=reader.readLine(); // username=bob&password=secret
		}
		String body = s.toString();
		String[] sepByAmp = body.split("&");
		
		List<String> values = new ArrayList<String>();
		for (String pair: sepByAmp) {
			values.add(pair.substring(pair.indexOf("=")+1));
		}
		
		// capture the actual username and password values
		String username = values.get(0);
		String password = values.get(1);
		
		log.info("User attempted login with username" + username);
		
		//call the confirmLogin method
		
		Employee e = eserv.confirmLogin(username, password);
		
		//return the user found and show object in browser
		if(e!=null) {
			HttpSession session = request.getSession();
			session.setAttribute("user", e);
			
			//print the logged in user to the screen
			PrintWriter out = response.getWriter();
			response.setContentType("text/html");
			
			//convert the object with the object mapper
			out.println(om.writeValueAsString(e));
			//log it!
			log.info("User "+username+" has logged in.");
		}else {
			response.setStatus(204); // this is a No Content status (successful request, but no user found)
		}
		
		
		
	}
	public static void processError(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		
		//if something goes wrong, redirect the user to a custom 404 error page
		request.getRequestDispatcher("error.html").forward(request, response);
		
	}

}
