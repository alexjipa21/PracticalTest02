import socket
from datetime import datetime
import time

HOST = "192.168.150.178"  # Bind to all available interfaces
PORT = 12345  # Port to listen on


def run_server():
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as server_socket:
        server_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        server_socket.bind((HOST, PORT))
        server_socket.listen(5)  # Allow up to 5 clients to connect
        print(f"Server started on {HOST}:{PORT}. Waiting for connections...")

        while True:
            client_socket, client_address = server_socket.accept()
            print(f"Connection established with {client_address}")
            with client_socket:
                try:
                    while True:
                        # Send current time to the client
                        current_time = datetime.now().strftime("%H:%M:%S")
                        client_socket.sendall(f"{current_time}\n".encode("utf-8"))
                        time.sleep(1)  # Wait for 1 second
                except (BrokenPipeError, ConnectionResetError):
                    print(f"Connection with {client_address} closed.")
                finally:
                    client_socket.close()


if __name__ == "__main__":
    run_server()
