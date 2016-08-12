// server2.cc

// we want to use sockets
// TCP is stream socket
//
// Listen on port
//    allowing connection from clients to this port
//
// 1. create a socket
// 2. bind socket to an address (ip address + port)
// 3. listen on socket
// 4. accept connections from clients

#include <iostream>
#include <sstream>

#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>
#include <arpa/inet.h>

using namespace std;

int main ()
{
  int server = socket ( PF_INET, SOCK_STREAM, 0 );
  if (server == -1)
    {
      cerr << "Too bad\n";
      return 1;
    }

  sockaddr_in serverInfo;
  memset (&serverInfo, 0, sizeof (serverInfo));
  serverInfo.sin_family = AF_INET;
  serverInfo.sin_addr.s_addr = INADDR_ANY; // 0
  serverInfo.sin_port = htons(11111);

  int status = 
    bind (server, (sockaddr*)&serverInfo, sizeof (serverInfo));
  if (status == -1)
    {
      cerr << "Noooooo\n";
      return 1;
    }

  status = listen (server, 10000);
  if (status == -1)
    {
      cerr << "Noooooo\n";
      return 2;
    }

  while (true)
    {
      sockaddr_in clientInfo;
      memset (&clientInfo, 0, sizeof (clientInfo));
      socklen_t addrLength;
      int connection = accept (server, (sockaddr*)&clientInfo, &addrLength);

      status = 
	send (connection, "Hello there!\n", sizeof("Hello there!\n"), 0);
      if (status == -1)
	{
	  cerr << "Hmm...\n";
	  return 3;
	}

      close (connection);
    }
}
