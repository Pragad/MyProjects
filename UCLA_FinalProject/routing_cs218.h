// Copyright (c) 2001-2008, Scalable Network Technologies, Inc.  All Rights Reserved.
//                          6701 Center Drive West
//                          Suite 520
//                          Los Angeles, CA 90045
//                          sales@scalable-networks.com
//
// This source code is licensed, not sold, and is subject to a written
// license agreement.  Among other things, no portion of this source
// code may be copied, transmitted, disclosed, displayed, distributed,
// translated, used as the basis for a derivative work, or used, in
// whole or in part, for any program or purpose other than its intended
// use in compliance with the license agreement as part of the QualNet
// software.  This source code and certain of the algorithms contained
// within it are confidential trade secrets of Scalable Network
// Technologies, Inc. and may not be used as the basis for any other
// software, hardware, product or service.

// Rounting protocol for VANET with sensors and vehicles
// This is for the project of CS218 class of 2012 fall of UCLA
// Hyunsoo Choe / Pragadheesh


#ifndef CS218_H
#define CS218_H

typedef struct struct_network_cs218_str Cs218Data;

class D_Cs218Print : public D_Command
{
    private:
        Cs218Data *cs218;

    public:
        D_Cs218Print(Cs218Data *newCs218) { cs218 = newCs218; }

        virtual void ExecuteAsString(const std::string& in, std::string& out);
};

// Cs218 default timer and constant values ref:
// draft-ietf-manet-cs218-08.txt section: 12

// These default values are user configurable. If not configured by the
// user the protocol will run with these specified default values.

#define CS218_DEFAULT_ACTIVE_ROUTE_TIMEOUT      (10000 * MILLI_SECOND)

#define CS218_DEFAULT_HELLO_TIMEOUT             (1000 * MILLI_SECOND)

#define CS218_DEFAULT_NODE_TRAVERSAL_TIME       (40 * MILLI_SECOND)

#define CS218_DEFAULT_MESSAGE_BUFFER_IN_PKT     (100)

#define CS218_ACTIVE_ROUTE_TIMEOUT              (cs218->activeRouteTimeout)

#define CS218_DEFAULT_MY_ROUTE_TIMEOUT          (2 * CS218_ACTIVE_ROUTE_TIMEOUT)

#define CS218_HELLO_TIMEOUT                     (cs218->helloTimeout)

#define CS218_NODE_TRAVERSAL_TIME               (cs218->nodeTraversalTime)

#define CS218_MY_ROUTE_TIMEOUT                  (cs218->myRouteTimeout)

#define CS218_NEXT_HOP_WAIT                     (CS218_NODE_TRAVERSAL_TIME + 10)

#define CS218_INFINITY                          (-1)

#define CS218_BROADCAST_JITTER          (20 * MILLI_SECOND)

#define CS218_LU_HELLO_WAIT						(1000 * MILLI_SECOND)
#define CS218_LU_INTERVAL						(910000 * MILLI_SECOND)
#define CS218_VEHICLE_HELLO_INTERVAL			(1000 * MILLI_SECOND)
#define CS218_CLEAR_INTERVAL					(91000 * MILLI_SECOND)
#define CS218_VEHICLE_CHECK_POS_INTERVAL		(1000 * MILLI_SECOND)


// Cs218 Packet Types
#define CS218_HELLO     1   // HELLO Packet of sensor
#define CS218_R_HELLO   2   // reply to HELLO
#define CS218_LOC_UPD_C 3   // location update to center
#define CS218_LOC_UPD_O 4   // location update from center
#define CS218_LOC_QRY   5   // location query
#define CS218_R_LOC_QRY 6   // reply to location query
#define CS218_DD        7   // message
#define CS218_REG       8   // register vehicle to a sensor
#define CS218_RELAY_REG 9	// notice register information

#define CS218_XYLS_N    0
#define CS218_XYLS_E    1
#define CS218_XYLS_S    2
#define CS218_XYLS_W    3

typedef struct 
{
	UInt32 x, y, z;
} Cs218Coord;

// /**
// STRUCT         ::    Cs218AddrSeqInfo
// DESCRIPTION    ::    Address and sequence number for CS218 for IPv4
//                      to be used in Packets.
// **/
typedef struct
{
    NodeAddress address;
    UInt32 seqNum;
    UInt8 nodeType;     // 1: sensors / 2 : vehicles
	Cs218Coord coord;
} Cs218AddrSeqInfo;

// /**
// STRUCT         ::    Cs218HelloPacket
// **/
typedef struct
{
    UInt8 packetType;
    Cs218AddrSeqInfo srcAddrSeq;  // sender
    Cs218AddrSeqInfo destAddrSeq; // destination
    Cs218AddrSeqInfo vehAddrSeq;  // if packetType is R_LOC_QRY
	UInt32 floodingId;            // flooding id of src node
    Cs218AddrSeqInfo prevHop;     // previous Hop
    Cs218AddrSeqInfo nextHop;     // next Hop
    clocktype lifetime;           // lifetime

    int direction;  // used in XYLS 1 : N / 2 : E / 3 : S / 4 : W


} Cs218Packet;

// /**
// STRUCT         ::    Cs218RouteEntry
// Each node stores its neighbor node information by transmitting HELLO/HELLO_REPLY
// **/
typedef struct route_cs218_table_row
{
	// neighbor node 
	Cs218AddrSeqInfo node;

    // added time
    clocktype timestamp;

    // Pointer to next/previous route entry.
    struct route_cs218_table_row* next;
    struct route_cs218_table_row* prev;
} Cs218RouteEntry;

// /**
// STRUCT         ::    Cs218RoutingTable
// stores routing table(neighbor's info) in double-linked-list
// **/
typedef struct
{
    Cs218RouteEntry* head;
    Cs218RouteEntry* tail;
    int   size;
} Cs218RoutingTable;

// Location Directory saves vehicle's information
typedef struct
{
    Cs218RouteEntry* head;
    Cs218RouteEntry* tail;
    int   size;
} Cs218LocationDirectory;

// sensors saves location querys for its lifetime when it doesn't have vehicle's information
typedef struct loc_qry_row
{
    Cs218Packet packet;
	clocktype timestamp;
    struct loc_qry_row* next;
    struct loc_qry_row* prev;
} Cs218PacketEntry;

typedef struct 
{
    Cs218PacketEntry *head;
    Cs218PacketEntry *tail;
    int size;
} Cs218PacketList;

typedef struct str_cs218_fifo_buffer
{
	// Destination address of the packet
	NodeAddress destAddr;
    // The time when the packet was inserted in the buffer
    clocktype timestamp;
    // The last hop which sent the data
    NodeAddress previousHop;
    // The packet to be sent
    Message *msg;
    // Pointer to the next message.
    struct str_cs218_fifo_buffer *next;
} Cs218BufferNode;
typedef struct
{
    Cs218BufferNode *head;
    int size;
} Cs218MessageBuffer;

typedef struct vehicle_cs218_table_row
{
	Cs218AddrSeqInfo node;
    clocktype lifetime;
	Cs218AddrSeqInfo relayNode;
	
    // Pointer to next/previous route entry.
    struct vehicle_cs218_table_row* next;
} Cs218VehicleEntry;

typedef struct 
{
	Cs218VehicleEntry *head;
} Cs218VehicleTable;

// /**
// STRUCT         ::    Cs218Stats
// DESCRIPTION    ::    CS218 IPv4/IPv6 Structure to store the statistical
//                      informations.
// **/
typedef struct
{
    UInt32 numAllPacketSent;
    UInt32 numHelloPacketSent;
    UInt32 numRHelloPacketSent;
    UInt32 numLUCPacketSent;
    UInt32 numLUOPacketSent;
    UInt32 numLQPacketSent;
    UInt32 numRLQPacketSent;
    UInt32 numMsgPacketSent;
    UInt32 numRegisterPacketSent;
    UInt32 numRelayRegisterPacketSent;
	
	
	
    UInt32 numAllPacketReceived;
    UInt32 numHelloPacketReceived;
    UInt32 numRHelloPacketReceived;
    UInt32 numLUCPacketReceived;
    UInt32 numLUOPacketReceived;
    UInt32 numLQPacketReceived;
    UInt32 numRLQPacketReceived;
    UInt32 numMsgPacketReceived;
    UInt32 numRegisterPacketReceived;
    UInt32 numRelayRegisterPacketReceived;
	
	
	
	UInt32 numRoutingFailed;
	
    UInt32 numDataRecved;
    UInt32 numDataForwarded;	
    UInt32 numDataSent;
} Cs218Stats;

// /**
// STRUCT         ::    Cs218Data
// DESCRIPTION    ::    CS218 IPv4 structure to store all necessary
//                      informations.
// **/
typedef struct struct_network_cs218_str
{

    RandomSeed  cs218JitterSeed;

    Cs218AddrSeqInfo addrSeq;
	int defaultInterface;
	Address defaultInterfaceAddr;

    Cs218RoutingTable routeTable;
    Cs218VehicleTable vehicleTable;
    Cs218LocationDirectory locationDirectory;
    Cs218PacketList lqList;
    //Cs218PacketList luList;
    //Cs218PacketList receivedPackets;
	Cs218MessageBuffer messageBuffer;

    Cs218Stats stats;

    clocktype activeRouteTimeout;
    clocktype helloTimeout;
    clocktype nodeTraversalTime;
    clocktype myRouteTimeout;

    UInt32 floodingId;

    BOOL statsCollected;
    BOOL statsPrinted;
	
	Cs218AddrSeqInfo currentRegisteredSensor;	// vehicle only
	clocktype registeredTime;
	BOOL pendingRegister;

    BOOL useXYLS;
    int gridSizeW;
    int gridSizeH;

    int oneHopDistance;
    Cs218Coord centerCoord;
} Cs218Data;

void Cs218Init(
    Node *node,
    Cs218Data **cs218Ptr,
    const NodeInput *nodeInput,
    int interfaceIndex,
    NetworkRoutingProtocolType cs218ProtocolType);

void Cs218Finalize(Node *node, int i, NetworkType networkType);

void Cs218RouterFunction(
    Node *node,
    Message *msg,
    NodeAddress destAddr,
    NodeAddress previousHopAddress,
    BOOL *packetWasRouted);

void Cs2184RouterFunction(
    Node* node,
    Message* msg,
    NodeAddress destAddr,
    NodeAddress previousHopAddress,
    BOOL* packetWasRouted);


void Cs218HandleProtocolPacket(
    Node *node,
    Message *msg,
    Address srcAddr,
    Address destAddr,
    int ttl,
    int interfaceIndex);

void
Cs218HandleProtocolEvent(
    Node *node,
    Message *msg);

#endif
