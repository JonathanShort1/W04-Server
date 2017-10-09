# W04-Server
Second year practical to deploy a server using java standard libraries.
How to run:
 * The server should be running at <user_name>.host.cs.st-andrews.ac.uk:45689
 * If the server is running the file "running.txt" will exist in the source folder.
 * To compile the Java file use: javac WebServer.java
 * To Start the server use: Java WebServer start <portnumber> <logfile> <serverfolder> 
  *  the three parameters after "start" are optional but have to come in that order 
    * You cannot give a logfile without also giving a portnumber and likewise with the server folder
 * To Stop the server use: Java WebServer stop
 * To run the server in the foreground use: Java WebServer run <portnumber> <logfile> <serverfolder>
    - make sure nothing else is running on this port first
  
  * The Default port number is 45689.
  * The Default server folder is src/Server/.
  * The location of the Default log files is src/Server/logs/.
  
