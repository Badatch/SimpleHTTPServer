import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;
import java.io.File;
import java.io.FileOutputStream;

class RunThread extends Thread 
{
    //*** TASK 10 *** 
	//(Whole program extends thread and this class 
	//handles the clients request as a thread)
    Socket conn=null;
    public RunThread(Socket c) 
    { 
        conn = c;
    }

    public void run()
    {
        try
        {
            Scanner scanin = new Scanner(conn.getInputStream());
            String line=null;
            int nlines=0;
            String arl [] =new String[32];

            while (scanin.hasNextLine()) 
            {
                line = scanin.nextLine();
                if(line.length()==0) break;
                arl[nlines] = line;
                nlines = nlines + 1;

            }

            for(int cnt = 0; cnt <= nlines; cnt++)
            {
                System.out.println(arl[cnt]);
            }

            String reqline = arl[0];
            Scanner scan = new Scanner (reqline);
            String command = scan.next();
            String resource = "www" + scan.next();

            // *** TASK 9 ***
            File checkitsadir = new File(resource);
            if(checkitsadir.isDirectory())
            {
                File checkdirhtml = new File(resource + "/index.html");	//File check for index.html
                File checkdirhtm = new File(resource + "/index.htm");	//File check for index.htm
                if(checkdirhtml.isFile())
                {
                    resource += "/index.html";	//check for index.html
                }
                else if(checkdirhtm.isFile())
                {
                    resource += "/index.htm";	//else check for index.htm
                }
            }

			
            //Check if file exists
            String filename = resource;
            File chkfile = new File(filename);
            if(!chkfile.exists())
            {
                System.out.println("File does not exist");
            }
            else
            {
                String sizestr = "Bytes in file: " + chkfile.length();
                System.out.println(sizestr);
            }

			//*** TASK 8 ***
            //Date and time
            String resourceName = resource.substring(3);
            OutputStream outs = conn.getOutputStream();
            Date date = new Date();
            SimpleDateFormat ft = new SimpleDateFormat ("EEE, d MMM yyyy H:m:s z"); //date format as in RFC2616
            String theDate = ft.format(date);

			
            //*** TASK 7***
            String getExt = chkfile.getName();
            int fcheck = getExt.lastIndexOf(".");
            fcheck = fcheck + 1;
            String extension = "plain";
            String checkexte = getExt.substring(fcheck);
            String dir = "text/";
            if(checkexte.equals("html") || checkexte.equals("htm")) //check if extension is .htm or .html 
            {
                extension = "html";
            }
            if(checkexte.equals("jpg") || checkexte.equals("jpeg")) //check if extension is .jpg or .jpeg
            {
                extension = "jpeg";
            }
            if(extension.equals("jpg") || extension.equals("jpeg")) //if extension is .jpg or .jpg set the first half of the extension to image
            {
                dir = "image/";
            }

			
            //Request responses
            String resno = "";
            Boolean sendFile = false;
			
			// *** TASK 1 ***
			// *** TASK 2 (Connection: close) in all replies ***
			// *** TASK 8 (DATE) in all replies ***
            if(!resourceName.startsWith("/"))		//400 response no "/" in request
            {
                String badreply = "HTTP/1.0 400 Bad request\r\n" +
                    "Connection: close\r\n" +
                    "Content-Type: " + dir + extension + "\r\n" +
                    "Date: " + theDate + "\r\n" + "\r\n";
                    
                outs.write(badreply.getBytes());
                resno = " 400";
            }
			// *** TASK 3 ***
            else if(command.equals("PUT") || command.equals("DELETE") || command.equals("TRACE")) //405 response using either PUT, DELETE or TRACE
            {
                String notallowedreply = "HTTP/1.0 405 Method Not Allowed\r\n" +
                    "Connection: close\r\n" +
                    "Content-Type: " + dir + extension + "\r\n" + 
                    "Date: " + theDate + "\r\n" +
		    "Allow: GET,HEAD\r\n" + "\r\n";
                outs.write(notallowedreply.getBytes());
                resno = " 405";
            }
            else  if(!chkfile.exists())	//404 no file found
            {
                String reply="HTTP/1.0 404 Not Found\r\n" +
                    "Connection: close\r\n" +
                    "Content-Type: " + dir + extension + "\r\n"  +
                    "Date: " + theDate + "\r\n"+ "\r\n" +
                    "<h1>Sorry, work in progress</h1>\r\n";
                outs.write(reply.getBytes());
                resno = " 404";

            }
			// *** TASK 5 ***
            else if(command.equals("HEAD"))	//handle HEAD request 200
            {	
                String headreply = "HTTP/1.0 200 OK\r\n" +
                    "Connection: close \r\n" +
                    "Content-Length: " + chkfile.length() + "\r\n" +
                    "Content-Type: " + dir + extension + "\r\n" +
                    "Date: " + theDate + "\r\n" + "\r\n";
                outs.write(headreply.getBytes());
                resno = " 200";
            }		
            else if (command.equals("GET"))	//handle GET request 200
            {
                String correply = "HTTP/1.0 200 OK\r\n" +              
                    "Connection: close \r\n" +
                    "Content-Length: " + chkfile.length() + "\r\n" +
                    "Content-Type: " + dir + extension + "\r\n" +
                    "Date: " + theDate + "\r\n" + "\r\n";
                outs.write(correply.getBytes());
                resno = " 200";
                sendFile = true;	//set fileoutput to true so file is only output if this response is sent
            }
			// *** TASK 4 ***
            else if(!command.equals("GET") || !command.equals("HEAD")) //handles unknown request
            {
                String notimpreply = "HTTP/1.0 501 Not Implemented\r\n" + 
                    "Connection: close\r\n" +
                    "Content-Type: " + dir + extension + "\r\n" +
                    "Date : " + theDate + "\r\n" + "\r\n";
                outs.write(notimpreply.getBytes());
                resno = " 501";
            }

            //Output request file if it is a GET request
            if(chkfile.isFile() && sendFile == true)
            {
                InputStream fins = new FileInputStream(chkfile);  
                byte buf[]=new byte[1024];
                while (true)      
                {
                    int rc = fins.read(buf, 0, 1024);
                    if(rc<=0) break;
                    outs.write(buf,0,rc);
                }
            }

            // *** TASK 6 ***
            File logout = new File("log.txt");
            FileOutputStream writelog = new FileOutputStream (logout, true);
            PrintStream pout = new PrintStream(writelog);
            pout.println(conn.getInetAddress() + "  " +  command + "  " + ft.format(date) + "  " +  resourceName + resno);
            //Create a log if it doesn't exist
            if(!logout.exists())
            {
                logout.createNewFile();
            }
            conn.close();
        }
        catch (IOException e) {
            System.err.println("Unexpected Error");
            System.exit(1);
        }
        catch (NullPointerException e)
        {
            System.err.println("Bad Request");
        }
    }
}

public class HTTPServer extends Thread	//*** TASK 10 ***
{
    public static void main(String args[]) throws Exception //This is only called once when the server is run 
    {
        //Create a server socket
        try{
            int port = Integer.parseInt(args[0]);
            ServerSocket serverSock=new ServerSocket(port);

            while(true) 
            {
                //Handle the client request in the other class
                Socket connSocket = serverSock.accept();
                RunThread server = new RunThread(connSocket);	//creating the handle part of the server 
                server.start();									//running the handler
            }
        }
        catch(IOException e)
        {
            System.err.println("Concurrent Server: Error on socket");
            System.exit(1);
        }

    }
}
