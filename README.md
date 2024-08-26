# Public-Message-Board
How to compile and run program:
- Client:
Open the terminal with working directory be where client.py is located
Type "python client.py" in command line to compile and run the client program
- Server:
Make sure that the folder WebServer is located in the same directory as WebServer.java
Open the terminal with working directory be where WebServer.java is located
Type "javac WebServer.java" in command line to compile the server program
Type "java WebServer" in command line to run the server program

Required libraries:
Python: socket, threading, time, ipaddress
Java: java.io.*, java.net.*, java.util.*, java.time.*

How to use the client program:
The program will first prompt the user for the IP address and port of the webserver. Currently, the webserver is hosted locally so the Ip address should be "127.0.0.1" and the port should be 6789. Once the client is connected, the client can type "help" then enter for a list of possible commands that the user can use. The user will first enter 1 word so that the program can understand the type of command. Using each of the command may ask the user to prompt the user for more input such as the group ID or message ID. For example, if the user type "groupjoin", the program will prompt the user for the group ID.

Major Issues:
- When the webserver was writing multiple lines to the input streams, it is possible for the client program to only have partial read of the messages sent. To resolve this, we make sure th webserver to only send multiple lines as 1 line deliminted by ";;" for each response and the client will parse this to output the correct message.
