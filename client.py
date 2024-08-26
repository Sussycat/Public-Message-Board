### CS 4065
### AUTHOR: CAT LUONG, HUNG NGUYEN, CHAU NGUYEN
### DATE: 04/16/2024

import socket
import threading 
from time import sleep
import ipaddress

# List of available commands for user to interact with the server
available_commands = ["join", "post", "users", "leave", "message", "exit", "groups", "groupjoin",
                    "grouppost", "groupusers", "groupleave", "groupmessage", "help"]

# Create a lock for synchronization
lock = threading.Lock()

def send_message(http_client, delimiter): # Main thread for sending message
    while True:  
        command = str(input("""Send your command (help for list of commands): """)).lower()

        cmd = ""
        if command not in available_commands: # Check to see if the command is in the list of available commands
            print("INVALID COMMAND.")
            continue
        
        lock.acquire() # Acquire the lock
        try:
            match command:
                # Perform certain action depends on the command
                # e.g. post means that you have to enter your subject line and body message
                # Refers to the project-2.pdf and message board blue print document for more detailed usage of these commands
                case "post": 
                    subject = str(input("Enter your subject line: "))
                    body = str(input("Enter your body message: "))
                    cmd = "%" + command + delimiter + subject + delimiter + body + "\r\n"
                case "message":
                    m_id = str(input("Enter your message id: "))
                    cmd = "%" + command + delimiter + m_id + "\r\n" 
                case "exit":
                    cmd = "%" + command + "\r\n"
                    print(cmd)
                    http_client.sendall(cmd.encode())
                    break
                case "groupjoin":
                    group_name = str(input("What is your desired group ID: "))
                    cmd = "%" + command + delimiter + group_name + "\r\n" 
                case "grouppost":
                    group_name = str(input("What is your desired group ID: "))
                    subject = str(input("Enter your subject line: "))
                    body = str(input("Enter your body message: "))
                    cmd = "%" + command + delimiter + group_name + delimiter + subject + delimiter + body + "\r\n" 
                case "groupusers":
                    group_name = str(input("What is your desired group ID: "))
                    cmd = "%" + command + delimiter + group_name + "\r\n"
                case "groupleave": 
                    group_name = str(input("What is your desired group ID: "))
                    cmd = "%" + command + delimiter + group_name + "\r\n"
                case "groupmessage":
                    group_name = str(input("What is your desired group ID: "))
                    m_id = str(input("Enter your message id: "))
                    cmd = "%" + command + delimiter + group_name + delimiter + m_id + "\r\n"
                case "help":
                    # Show the list of all available commands with help
                    print("""Select among these commands:  
                        - join
                        - post
                        - users
                        - leave
                        - message
                        - exit
                        - groups
                        - groupjoin
                        - grouppost
                        - groupusers
                        - groupleave
                        - groupmessage""")
                case _:
                    cmd = "%" + command + "\r\n"
              
            print("") # Create a new line
            http_client.sendall(cmd.encode()) # Send command over to server to wait for received message
            sleep(0.5)

        finally:
            lock.release()


def receive_message(http_client):
    while True:
        message = http_client.makefile().readline()
        return message
    

def receive_messages_in_real_time(http_client):
    while True:
        # function used by thread to get server output
        message = receive_message(http_client) # Read everything in one go
        messages = message.split(";;")
        with lock: # Locking mechanism to ensure printing of message
            for msg in messages: # Print all messages
                print(
                    "\u001B[s"             # Save current cursor position
                    "\u001B[A"             # Move cursor up one line
                    "\u001B[999D"          # Move cursor to beginning of line
                    "\u001B[S"             # Scroll up/pan window down 1 line
                    "\u001B[L",            # Insert new line
                    end="")     
                print('Received from server: ' + msg, end="")        # Print message line
                print("\u001B[u", end="")  # Move back to the former cursor position
                print("", end="", flush=True)  # Flush message

def http_connect(host, port): 
    # Set up the socket and connnect to server via prompted host and port
    http_client = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    http_client.connect((host, port))
    print("Connected to Server")
    return http_client
   
def main(): 
    try:
        delimiter = ";;"
        connected = False
        while not connected:
            # Establish connection with server by prompting for host and port
            # If host and port not available, prompt again
            print("Connecting to server...")
            host = str(input("Input the webserver IP address: "))
            try:
                ipaddress.ip_address(host)
                port = int(input("Input the message board port number: "))
                if 0 < port < 65536:
                    try:
                        http_client = http_connect(host, port)
                        connected = True
                    except Exception as e:
                        print(f"Error: Unable to connect to the server. Reason: {e}")
                        continue
                else:
                    print("ERROR: Port number must be between 1 and 65535.")
            except ValueError:
                print("Error: Invalid IP address or port number.")

        # Start receiving thread to listen before using sending thread
        receive_thread = threading.Thread(target=receive_messages_in_real_time, args=(http_client, ))
        receive_thread.daemon = True
        receive_thread.start()

        send_message(http_client=http_client, delimiter=delimiter)

    except Exception as e:
        print("Error:", e)
    finally:
        print("Disconnected from server.") # After done, disconnnected from the server

if __name__ == "__main__": 
   main()