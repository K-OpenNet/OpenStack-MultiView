/**
 * ***************************************************************
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2000 CSELT S.p.A.
 * 
 * GNU Lesser General Public License
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * **************************************************************
 */
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import jade.core.Agent;

/**
   This example shows a minimal agent that just prints "Hallo World!" 
   and then terminates.
   @author Giovanni Caire - TILAB
 */
public class Resource extends Agent {

  protected void setup() {

  	System.out.println("Hello test shell! My name is "+getLocalName());

	while(true)
	{
	String command ="bash SmartX_Agent_Compute.sh";
	try{
	//System.out.println("I will run Resource Visibility Agent!");
	shellCmd(command);
	}
	catch(Exception e)
	{
	}//shellCmd(command);  	
  	// Make this agent terminate

	try{
	Thread.sleep(2000);
	}
	catch(Exception e)
	{
	}
	}
  	//doDelete();
  } 


 public static void shellCmd(String command)throws Exception
  {


        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec(command);
        InputStream is = process.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String line;

        while((line = br.readLine()) != null)
        {
                System.out.println(line);

        }
  }
}


