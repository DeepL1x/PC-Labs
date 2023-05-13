import random
import socket
import struct

# Define the IP address and port number of the server
IP_ADDRESS = 'localhost'
PORT = 12345

# Define default size of the array (dimensions of matrix)
m = 1000
n = 1000
t = 4
# Define the array of integers to send
array = []

def receive_data(s):
        len = s.recv(2)
        data = s.recv(int.from_bytes(len, byteorder ='big'))
        print(data.decode('utf-8'))

def send_data(s, message):
    encoded_message = message.encode('utf-8')
    s.sendall(len(encoded_message).to_bytes(2, byteorder='big'))
    s.sendall(encoded_message)

def send_array(s, array, chunk_size):
    array_len = len(array)
    print("Array length:", array_len)
    s.sendall(struct.pack('!i', array_len))

    for i in range(0, array_len, chunk_size):
        chunk = array[i:i+chunk_size]
        data = struct.pack('!%di' % len(chunk), *chunk)
        s.sendall(data)
    print("Min Max check:", min(array), " ", max(array))

# Create a socket and connect to the server
with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
    s.connect((IP_ADDRESS, PORT))

    try:
        receive_data(s)

        while True:
            choice = input("Enter your option number:")
            send_data(s, choice)
            receive_data(s)
            if choice == '1':
                m = int(input("m:"))
                n = int(input("n:"))
                t = int(input("t:"))
                # Send dims
                send_data(s, str(m) + ' ' + str(n) + ' ' + str(t))
                receive_data(s)
            elif choice == '2':
                array = [random.randint(0, 100) for _ in range(m*n)]

                # Send the array
                print("Sending array...")
                send_array(s, array, int(len(array)/(n)))
                receive_data(s)
            elif choice == '5' or choice == '6':
                break
    finally:
        # Close the socket
        s.close()