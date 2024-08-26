# Public Message Board
Overview:
This is a message board implemented as a client-server application using pure unicast sockets. The Client program is written in Python while the server program is written in Java

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
- The program will first prompt the user for the IP address and port of the webserver. Currently, the webserver is hosted locally so the Ip address should be "127.0.0.1" and the port should be 6789.
- Once the client is connected, the client can type "help" then enter for a list of possible commands that the user can use.
- The user will first enter 1 word so that the program can understand the type of command.
- Using each of the command may ask the user to prompt the user for more input such as the group ID or message ID. For example, if the user type "groupjoin", the program will prompt the user for the group ID.

Possible Commands implemented:
- a %connect command followed by the address and port number of a running bulletin board server to
connect to.
– a %join command to join the single message board
– a %post command followed by the message subject and the message content or main body to post a
message to the board.
– a %users command to retrieve a list of users in the same group.
– a %leave command to leave the group.
– a %message command followed by message ID to retrieve the content of the message.
– an %exit command to disconnect from the server and exit the client program.
– a %groups command to retrieve a list of all groups that can be joined.
– a %groupjoin command followed by the group id/name to join a specific group.
– a %grouppost command followed by the group id/name, the message subject, and the message content or
main body to post a message to a message board owned by a specific group.
– a %groupusers command followed by the group id/name to retrieve a list of users in the given group.
– a %groupleave command followed by the group id/name to leave a specific group.
– a %groupmessage command followed by the group id/name and message ID to retrieve the content of the
message posted earlier on a message board owned by a specific group.
