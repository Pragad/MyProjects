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


#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <math.h>

#include "api.h"
#include "partition.h"
#include "network_ip.h"
#include "ipv6.h"
#include "routing_cs218.h"
#include "buffer.h"
#include "external_socket.h"

#define  CS218_DEBUG 1
#define  CS218_DEBUG_INIT 1
#define  CS218_DEBUG_ROUTE_TABLE 1
#define  CS218_DEBUG_CS218_TRACE 1
#define  CS218_DEBUG_HELLO 1

#define CS218_PC_ERAND(cs218JitterSeed) (RANDOM_nrand(cs218JitterSeed)\
    % CS218_BROADCAST_JITTER)

#define CS218_MAX_ROUTE_LENGTH	(9)
#define CS218_VEHICLE_START_NO (26)	

//#define CS218_TEST_LQ_X	(99999)
//#define CS218_TEST_LQ_Y	(100)
//#define CS218_TEST_LU_X	(0)
//#define CS218_TEST_LU_Y	(0)
#define DEFAULT_ONE_HOP_DISTANCE (100)
#define DEFAULT_CENTER_X	(300)
#define DEFAULT_CENTER_Y	(300)


// EVENTS
#define EVENT_CLEAR	(100)
#define EVENT_VEH_PREPARE_LU	(101)
#define EVENT_VEH_PROCESS_LU	(102)
#define EVENT_VEH_REGISTER		(103)
#define EVENT_VEH_CHECK_POS		(104)

static FILE *cs218_trace_fp;
static FILE *cs218_stat_fp;
static FILE *cs218_info_fp;

static bool trace_init = false;
static bool stat_init = false;
static bool info_init = false;

// four neighbor of center points
static void Cs218RetrieveCPInfo(int idx, int* x, int* y) {
	switch(idx) {
	case 0:
		*x = 200; *y = 200;
		break;
	case 1:
		*x = 400; *y = 200;
		break;
	case 2:
		*x = 200; *y = 400;
		break;
	case 3:
		*x = 400; *y = 400;
		break;
	}
}

inline static float distance( Cs218Coord *a, Cs218Coord *b ) {
	return sqrt((float)((a->x-b->x)*(a->x-b->x) + (a->y-b->y)*(a->y-b->y)));
}

	
static void Cs218InitializeHelloPacket(Node* node, Cs218Data* cs218);
static void Cs218InitializeReplyHelloPacket(Node* node, Cs218Data* cs218, Cs218AddrSeqInfo *dest);
static void Cs218InitializeLocationQueryPacket(Node* node, Cs218Data* cs218, NodeAddress interestingAddress);
static void Cs218SetTimer(Node* node, int eventType, clocktype delay);

static void Cs218PrintRoutingTable(Node* node, Cs218RoutingTable* routeTable);
static void Cs218PrintVehicleTable(Node* node, Cs218VehicleTable* vehicleTable);
static void Cs218PrintLocationDirectory(Node* node, Cs218LocationDirectory* locationDirectory);


inline static void Cs218NodeAddressToString(NodeAddress *nodeAddr, char* str) {
	Address address;
	SetIPv4AddressInfo(&address, *nodeAddr);
	IO_ConvertIpAddressToString( &address, str );
}
	
inline static bool Cs218ValidRange(Cs218AddrSeqInfo *dst, Cs218AddrSeqInfo *src, int oneHopDistance) {
	if( distance( &(dst->coord), &(src->coord) ) > oneHopDistance )
		return false;
	return true;
}

inline static Cs218Data* getCs218Data(Node *node) {
	return (Cs218Data *) NetworkIpGetRoutingProtocol(node,
							ROUTING_PROTOCOL_CS218,
							NETWORK_IPV4);
}
	

inline static void printLog(Node *node, Message *msg, Cs218Packet *pkt, char *fmt, ...) {
	char buffer[MAX_STRING_LENGTH] = {0,};
    char time[MAX_STRING_LENGTH] = {0,};
	va_list args;
	va_start(args, fmt);
	vsprintf(buffer, fmt, args);
	va_end(args);
	
	clocktype curTime = TIME_getSimTime(node);
	TIME_PrintClockInSecond(curTime, time);

	UInt32 floodingId = 0;
	if( pkt ) floodingId = pkt->floodingId;
	UInt32 packetType = 0;
	if( pkt ) packetType = pkt->packetType;
	
	char log[MAX_STRING_LENGTH] = {0,};
	if( trace_init ) {
		cs218_trace_fp = fopen("cs218.trace", "a");
	} else {
		cs218_trace_fp = fopen("cs218.trace", "w");
		sprintf(log, "nodeId, floodingId, time, packetType, msg\n");
		fprintf(cs218_trace_fp, log);
		
	}
	trace_init = true;
	
	sprintf(log, "%3u,%u,%-8s,%-3d,%s \n", node->nodeId, floodingId, time,  packetType, buffer);
	printf( log );
	
	
	fprintf( cs218_trace_fp, log );
	fclose( cs218_trace_fp );
	
}	


void D_Cs218Print::ExecuteAsString(const std::string& in, std::string& out)
{
    Cs218RoutingTable* routeTable = &cs218->routeTable;
    Cs218RouteEntry* rtEntry = NULL;
    int i = 0;
    EXTERNAL_VarArray v;
    char str[MAX_STRING_LENGTH];

    EXTERNAL_VarArrayInit(&v, 400);

    EXTERNAL_VarArrayConcatString(
        &v,
        "The Routing Table is:\n"
        " Type       Node            Seq   Position         Time     \n"
        "------------------------------------------------------------\n");
    for (rtEntry = routeTable->head; rtEntry != NULL;
        rtEntry = rtEntry->next)
    {
        char node[MAX_STRING_LENGTH];
		char time[MAX_STRING_LENGTH];

		Cs218NodeAddressToString( &(rtEntry->node.address), node );

        TIME_PrintClockInSecond(rtEntry->timestamp, time);

        sprintf(str, "%10s %15s  %5u   %5d,%5d,%5d    %9s  ", 
			((rtEntry->node.nodeType==1)?"SENSOR":"VEHICLE"),
			node,
            rtEntry->node.seqNum,
			rtEntry->node.coord.x,
			rtEntry->node.coord.y,
			rtEntry->node.coord.z,
            time);

        EXTERNAL_VarArrayConcatString(&v, str);

    }
    EXTERNAL_VarArrayConcatString(
        &v,
        
        "------------------------------------------------------------\n");

    out = v.data;
    EXTERNAL_VarArrayFree(&v);
}

Cs218RouteEntry*
Cs218FindEntryInDirection(Cs218Data *cs218, Cs218RoutingTable *routeTable, int direction) {
	Cs218RouteEntry *each = routeTable->head;

	int target = -1;
	int nodeNum = cs218->addrSeq.seqNum;
	switch( direction ) {
		case CS218_XYLS_W:
			if( nodeNum%cs218->gridSizeW != 1 ) {
				target = nodeNum - 1;
			}
			break;
		case CS218_XYLS_E:
			if( nodeNum%cs218->gridSizeW != 0 ) {
				target = nodeNum + 1;
			}
			break;
		case CS218_XYLS_N:
			if( nodeNum - cs218->gridSizeW > 0  ) {
				target = nodeNum - cs218->gridSizeW;
			}
			break;
		case CS218_XYLS_S:
			if( nodeNum + cs218->gridSizeW <= cs218->gridSizeW*cs218->gridSizeH ) {
				target = nodeNum + cs218->gridSizeW;
			}
			break;
	}
	if( target == -1 )
		return NULL;
	while( each ) {
		if( each->node.seqNum == target )
			return each;
		each = each->next;
	}
	return NULL;
}

void
Cs218Init(
    Node* node,
    Cs218Data** cs218Ptr,
    const NodeInput* nodeInput,
    int interfaceIndex,
    NetworkRoutingProtocolType cs218ProtocolType)
{
    NetworkDataIp *ip = (NetworkDataIp *) node->networkData.networkVar;

    Cs218Data* cs218 = (Cs218Data *) MEM_malloc(sizeof(Cs218Data));

    BOOL retVal;
    char buf[MAX_STRING_LENGTH];
    int i = 0;
    NetworkRoutingProtocolType protocolType;

    (*cs218Ptr) = cs218;

    memset(cs218, 0, sizeof(Cs218Data));

    RANDOM_SetSeed(cs218->cs218JitterSeed,
                   node->globalSeed,
                   node->nodeId,
                   cs218ProtocolType);
    // USE-XYLS?
 	IO_ReadString(
        node->nodeId,
        ANY_ADDRESS,
        nodeInput,
        "CS218-XYLS-USE",
        &retVal,
        buf);
	if( retVal == false || strcmp(buf, "NO") == 0 )
		cs218->useXYLS = false;
	else if(strcmp(buf, "YES") == 0)
		cs218->useXYLS = true;
	else
		ERROR_ReportError("Needs YES/NO against CS218-XYLS-USE");
	if( cs218->useXYLS ) {

	 	IO_ReadInt(
	        node->nodeId,
	        ANY_ADDRESS,
	        nodeInput,
	        "CS218-XYLS-GRID-W",
	        &retVal,
	        &(cs218->gridSizeW));
		if( !retVal )
			ERROR_ReportError("To use XYLS provide CS218-XYLS-GRID-W");
	 	IO_ReadInt(
	        node->nodeId,
	        ANY_ADDRESS,
	        nodeInput,
	        "CS218-XYLS-GRID-H",
	        &retVal,
	        &(cs218->gridSizeH));
		if( !retVal )
			ERROR_ReportError("To use XYLS provide CS218-XYLS-GRID-H");
	}

 	IO_ReadInt(
        node->nodeId,
        ANY_ADDRESS,
        nodeInput,
        "CS218-ONE-HOP-DISTANCE",
        &retVal,
        &(cs218->oneHopDistance));
	if( !retVal )
		cs218->oneHopDistance = DEFAULT_ONE_HOP_DISTANCE;

	cs218->centerCoord.x = 100 + (int)(cs218->gridSizeW/2)*cs218->oneHopDistance;
	cs218->centerCoord.y = 100 + (int)(cs218->gridSizeH/2)*cs218->oneHopDistance;




    Coordinates coordinates;

    MOBILITY_ReturnCoordinates(node, &coordinates);

    cs218->addrSeq.address = NetworkIpGetInterfaceAddress(node, interfaceIndex);
    cs218->addrSeq.seqNum = node->nodeId;
    cs218->addrSeq.nodeType = ( node->nodeId < CS218_VEHICLE_START_NO )?1:2;   // TODO sensor and vehicle
    cs218->addrSeq.coord.x = coordinates.common.c1;
    cs218->addrSeq.coord.y = coordinates.common.c2;
    cs218->addrSeq.coord.z = 0;     // SAME PLANE

    cs218->floodingId = 1;

	//printLog( node, NULL, NULL, "Initialized" );

    // Set the router function
    NetworkIpSetRouterFunction(
        node,
        &Cs218RouterFunction,
        interfaceIndex);

    protocolType = ROUTING_PROTOCOL_CS218;

    // Set default Interface Info
    cs218->defaultInterface = interfaceIndex;

    SetIPv4AddressInfo(
        &cs218->defaultInterfaceAddr,
        NetworkIpGetInterfaceAddress(node, interfaceIndex));
    
    if( node->nodeId < CS218_VEHICLE_START_NO ) {
		// Process HELLO
		Cs218InitializeHelloPacket(node, cs218);
	} else {
		// Vehicle Location Update process
		
		Cs218SetTimer( node, EVENT_VEH_PREPARE_LU, CS218_VEHICLE_HELLO_INTERVAL);
		Cs218SetTimer( node, EVENT_VEH_CHECK_POS, CS218_VEHICLE_CHECK_POS_INTERVAL);
	}
	//Cs218SetTimer( node, EVENT_CLEAR, CS218_CLEAR_INTERVAL);
}


void
Cs218Finalize(Node* node, int i, NetworkType networkType)
{
	cs218_info_fp = fopen("cs218.info", info_init?"a":"w");
	info_init = true;
	Cs218PrintRoutingTable( node, &(getCs218Data( node ) -> routeTable) );
	Cs218PrintVehicleTable( node, &(getCs218Data( node ) -> vehicleTable) );
	Cs218PrintLocationDirectory( node, &(getCs218Data( node ) -> locationDirectory) );
	fclose(cs218_info_fp);
	
	char log[MAX_STRING_LENGTH] = {0,};
	if( stat_init ) {
		cs218_stat_fp = fopen("cs218.stat", "a");
	} else {
		cs218_stat_fp = fopen("cs218.stat", "w");
		sprintf(log, "nodeId,SHello,SRHello,SLUC,SLUO,SLQ,SRLQ,SMsg,SReg,SRReg,RHello,RRHello,RLUC,RLUO,RLQ,RRLQ,RMsg,RReg,RRReg,FRouting,DataRecev,DataForward,DataSent\n");
		printf( log );
		fprintf( cs218_stat_fp, log );
	}
	stat_init= true;
	Cs218Data* cs218 = getCs218Data(node);
	
	sprintf( log, 
		"%d,"
		"%d,%d,%d,%d,%d,"
		"%d,%d,%d,%d,%d,"
		"%d,%d,%d,%d,%d,"
		"%d,%d,%d,%d,%d,"
		"%d,%d\n",
		node->nodeId,
		cs218->stats.numHelloPacketSent, cs218->stats.numRHelloPacketSent, cs218->stats.numLUCPacketSent, cs218->stats.numLUOPacketSent, cs218->stats.numLQPacketSent,
		cs218->stats.numRLQPacketSent, cs218->stats.numMsgPacketSent, cs218->stats.numRegisterPacketSent, cs218->stats.numRelayRegisterPacketSent, cs218->stats.numHelloPacketReceived,
		cs218->stats.numRHelloPacketReceived,cs218->stats.numLUCPacketReceived, cs218->stats.numLUOPacketReceived, cs218->stats.numLQPacketReceived, cs218->stats.numRLQPacketReceived,
		cs218->stats.numMsgPacketReceived, cs218->stats.numRegisterPacketReceived, cs218->stats.numRelayRegisterPacketReceived, cs218->stats.numRoutingFailed, cs218->stats.numDataRecved,
		cs218->stats.numDataForwarded, cs218->stats.numDataSent);
	printf( log );
	fprintf( cs218_stat_fp, log );
	fclose( cs218_stat_fp );
	
}

static
unsigned int Cs218GetFloodingId(Cs218Data* cs218)
{
    unsigned int bcast;
    bcast = cs218->floodingId;
    cs218->floodingId++;
    return bcast;
}


// /**
// FUNCTION : Cs218Set
// LAYER    : NETWORK
// PURPOSE  : Set timers for protocol events
// PARAMETERS:
// +node:Node*:Pointer to node which is scheduling an event
// +eventType:int:The event type of the message
// +destAddr:Address:Destination for which the event has been sent (if
//                      necessary)
// +delay:clocktype:Time after which the event will expire
//RETURN    ::void:NULL
// **/

static
void Cs218SetTimer(
         Node* node,
         int eventType,
         clocktype delay)
{
    Message* newMsg = NULL;
    NetworkRoutingProtocolType protocolType;

    protocolType = ROUTING_PROTOCOL_CS218;
    
    // Allocate message for the timer
    newMsg = MESSAGE_Alloc(
                 node,
                 NETWORK_LAYER,
                 protocolType,
                 eventType);

    // Schedule the timer after the specified delay
    MESSAGE_Send(node, newMsg, delay);
	
	
	// log
    char clockStr[MAX_STRING_LENGTH];
	TIME_PrintClockInSecond((getSimTime(node) + delay), clockStr);
	if( eventType != EVENT_VEH_CHECK_POS )
	printLog( node, newMsg, NULL, "SetTimer %d on %s",eventType, clockStr);
}

static
void Cs218InsertEntryToRoutingTable(Node* node, Cs218AddrSeqInfo *neighbor) 
{
	Cs218RouteEntry *entry = (Cs218RouteEntry *)MEM_malloc( sizeof( Cs218RouteEntry ) );
	entry->prev = NULL;
	entry->next = NULL;
	memcpy( &(entry->node), neighbor, sizeof( Cs218AddrSeqInfo ) );
	entry->timestamp = getSimTime( node );

	Cs218Data *cs218 = getCs218Data(node);
	Cs218RoutingTable *routeTable = &(cs218->routeTable);
	
	if( routeTable->size == 0 ) {
		routeTable->head = entry;
		routeTable->tail = entry;
	} else {
		
		Cs218RouteEntry *each = routeTable->head;
		while( each ) {
			// if it has already the same node, change timestamp
			if( each->node.seqNum == neighbor->seqNum ) {
				each->timestamp = entry->timestamp;
				MEM_free( entry );
				return;
			}
			each = each->next;
		}
	
		Cs218RouteEntry *tail = routeTable->tail;
		tail->next = entry;
		entry->prev = tail;
		routeTable->tail = entry;
	}
	
	routeTable->size++;
}


static
void Cs218DeleteEntryFromRoutingTable(Node* node, Cs218AddrSeqInfo *entry) 
{

	Cs218Data *cs218 = getCs218Data(node);
	Cs218RoutingTable *routeTable = &(cs218->routeTable);
	Cs218RouteEntry *each = routeTable->head;
	
	while(each) {
		if( each->node.seqNum == entry->seqNum ) {
			if( each->prev == NULL ) {
				routeTable->head = each->next;
			} else {
				each->prev->next = each->next;
			}
			if( each->next == NULL ) {
				routeTable->tail = each->prev;
			} else {
				each->next->prev = each->prev;				
			}			
			MEM_free( each );
			routeTable->size--;
			return;
		}
		each = each->next;
	}
}

static
void Cs218DeleteAllRoutingTable(Node *node) {
	Cs218Data *cs218 = getCs218Data(node);
	Cs218RoutingTable *routeTable = &(cs218->routeTable);
	Cs218RouteEntry *each = routeTable->head;
	
	while(each) {
		Cs218RouteEntry *next = each -> next;
		MEM_free(each);
		each = next;
	}
	routeTable->head = NULL;
	routeTable->tail = NULL;
	routeTable->size = 0;
}

static
Cs218AddrSeqInfo *Cs218RetrieveClosestSensor(Node *node, Cs218Data *cs218) {

	Cs218RoutingTable *routeTable = &(cs218->routeTable);
	Cs218RouteEntry *each = routeTable->head;
	
	int minDist = 0x7fffffff;
	Cs218RouteEntry *closest = NULL;
	while(each) {
		int diff = distance( &(cs218->addrSeq.coord), &(each->node.coord) );
		if( diff < minDist ) {
			closest = each;
			minDist = diff;
		}
		each = each->next;
	}
	
	if( closest )
		return &(closest->node);
	
	return NULL;
}

// for vehicle Routing Table

static
void Cs218InsertEntryToVehicleTable(Node* node, Cs218AddrSeqInfo *neighbor) 
{
	Cs218VehicleEntry *entry = (Cs218VehicleEntry *)MEM_malloc( sizeof( Cs218VehicleEntry ) );

	entry->next = NULL;
	memcpy( &(entry->node), neighbor, sizeof( Cs218AddrSeqInfo ) );
	entry->lifetime = getSimTime( node ) + CS218_DEFAULT_ACTIVE_ROUTE_TIMEOUT;
	entry->relayNode.seqNum = 0;
	
	
	Cs218Data *cs218 = getCs218Data(node);
	Cs218VehicleTable *vehicleTable = &(cs218->vehicleTable);
	
	if( vehicleTable->head == NULL ) {
		vehicleTable->head = entry;
	} else {
		
		Cs218VehicleEntry *each = vehicleTable->head;
		Cs218VehicleEntry *previous = NULL;
		while( each ) {
			// if it has already the same node, change timestamp
			if( each->node.seqNum == neighbor->seqNum ) {
				each->lifetime = entry->lifetime;
				memcpy( &(each->relayNode), &(entry->relayNode), sizeof(Cs218AddrSeqInfo) );
				MEM_free( entry );
				return;
			}
			previous = each;
			each = each->next;
		}
		previous->next = entry;
	}
}

static
void Cs218UpdateVehicleTable(Node *node, Cs218AddrSeqInfo *vehAddress, Cs218AddrSeqInfo *relayNode) {
	
	Cs218Data *cs218 = getCs218Data(node);
	Cs218VehicleTable *vehicleTable = &(cs218->vehicleTable);
	
	Cs218VehicleEntry *each = vehicleTable->head;
		
	while( each ) {
		if( each->node.seqNum == vehAddress->seqNum ) {
			memcpy(&(each->relayNode), relayNode, sizeof(Cs218AddrSeqInfo));
		}
		each = each->next;
	}
}

static
void Cs218ClearOldVehicleTable(Node *node, Cs218Data *cs218) {

	Cs218VehicleTable *vehicleTable = &(cs218->vehicleTable);
	
	Cs218VehicleEntry *each = vehicleTable->head;
	Cs218VehicleEntry *previous = NULL;
	clocktype curTime = getSimTime(node);
	
	while(each) {
		Cs218VehicleEntry *del = NULL;
		if( each->lifetime < curTime ) {
			del = each;
			if( previous ) {
				previous->next = each->next;
			} else {
				vehicleTable->head = each->next;
			}
		} else {
			previous = each;
		}
		each = each->next;
		if( del )
			MEM_free(del);
	}
}

static
Cs218VehicleEntry* Cs218RetrieveVehicleTable(Node *node, NodeAddress interestingAddress) {
	
	Cs218Data *cs218 = getCs218Data(node);
	Cs218VehicleTable *vehicleTable = &(cs218->vehicleTable);
	
	Cs218VehicleEntry *each = vehicleTable->head;
		
	while( each ) {
		if( each->node.address == interestingAddress ) {
			return each;
		}
		each = each->next;
	}
	return NULL;
}

static 
Cs218RouteEntry* Cs218RetrieveLocationDirectory(Node *node, Cs218Data *cs218, NodeAddress destAddr) {
	Cs218RouteEntry *each = cs218->locationDirectory.head;
	clocktype curTime = getSimTime(node);
	while( each ) {
		if( each->node.address == destAddr && curTime < each->timestamp )
			return each;
		each = each->next;
	}
	return NULL;
}

static 
Cs218RouteEntry* Cs218RetrieveLocationDirectoryXYLS(Node *node, Cs218Data *cs218, Cs218AddrSeqInfo *vehAddr) {
	Cs218RouteEntry *each = cs218->locationDirectory.head;
	clocktype curTime = getSimTime(node);
	while( each ) {
		if( each->node.address == vehAddr->address &&
			each->node.coord.x == vehAddr->coord.x &&
			each->node.coord.y == vehAddr->coord.y  )
			return each;
		each = each->next;
	}
	return NULL;
}

static 
void Cs218InsertLocationDirectory(Node *node, Cs218Data *cs218, Cs218AddrSeqInfo *vehicleInfo, clocktype lifetime) {
	Cs218RouteEntry *each = cs218->locationDirectory.head;
	Cs218RouteEntry *previous = NULL;
	clocktype curTime = getSimTime(node);
	while( each ) {
		// if has the same
		if( each->node.address == vehicleInfo->address ) {
			each->timestamp = getSimTime(node) + lifetime;
			each->node.coord.x = vehicleInfo->coord.x;
			each->node.coord.y = vehicleInfo->coord.y;
			return;
		}
		previous = each;
		each = each->next;
	}
	cs218->locationDirectory.size++;
	Cs218RouteEntry *entry = (Cs218RouteEntry *)MEM_malloc( sizeof( Cs218RouteEntry ) );
	entry->timestamp = getSimTime(node) + lifetime;
	memcpy( &(entry->node), vehicleInfo, sizeof(Cs218AddrSeqInfo));
	entry->prev = NULL;
	entry->next = NULL;
	if( previous == NULL ) {
		cs218->locationDirectory.head = entry;
		cs218->locationDirectory.tail = entry;
	} else {
		previous->next = entry;
		entry->prev = previous;
		cs218->locationDirectory.tail = entry;
	}
}

static 
void Cs218ClearOldLocationDirectory(Node *node, Cs218Data* cs218) {
	Cs218RouteEntry* current = cs218->locationDirectory.head;
	
	clocktype curTime = getSimTime(node);
	while( current ) {
		Cs218RouteEntry* del = NULL;
		if( curTime > current->timestamp ) {
			del = current;
			if( current->prev ) {
				current->prev->next = current->next;
			} else {
				cs218->locationDirectory.head = current->next;
			}
			if( current->next ) {
				current->next->prev = current->prev;
			} else {
				cs218->locationDirectory.tail = current->prev;
			}
			cs218->locationDirectory.size--;
		}
		current = current->next;
		if( del ) 
			MEM_free(del);
	}
	
}

static 
Cs218PacketEntry* Cs218RetrieveLocationQuery(Node *node, Cs218Data *cs218, NodeAddress destAddr) {
	Cs218PacketEntry *each = cs218->lqList.head;
	clocktype curTime = getSimTime(node);
	while( each ) {
		if( each->packet.vehAddrSeq.address == destAddr && curTime < each->timestamp )
			return each;
		each = each->next;
	}
	return NULL;
}

static 
Cs218PacketEntry* Cs218DeleteLocationQuery(Node *node, Cs218Data *cs218, Cs218PacketEntry* current) {
	
	Cs218PacketEntry* del = NULL;
	
	del = current;
	if( current->prev ) {
		current->prev->next = current->next;
	} else {
		cs218->lqList.head = current->next;
	}
	if( current->next ) {
		current->next->prev = current->prev;
	} else {
		cs218->lqList.tail = current->prev;
	}
	cs218->lqList.size--;
	
	if( del ) 
		MEM_free(del);
}



static 
void Cs218InsertLocationQueries(Node *node, Cs218Data *cs218, Cs218Packet* pkt, clocktype lifetime) {
	Cs218PacketEntry *each = cs218->lqList.head;
	Cs218PacketEntry *previous = NULL;
	clocktype curTime = getSimTime(node);
	while( each ) {
		// if has the same
		if( each->packet.srcAddrSeq.address == pkt->srcAddrSeq.address &&
			each->packet.vehAddrSeq.address == pkt->vehAddrSeq.address ) {
			each->timestamp = getSimTime(node) + lifetime;
			return;
		}
		previous = each;
		each = each->next;
	}
	cs218->lqList.size++;
	Cs218PacketEntry *entry = (Cs218PacketEntry *)MEM_malloc( sizeof( Cs218PacketEntry ) );
	entry->timestamp = getSimTime(node) + lifetime;
	memcpy( &(entry->packet), pkt, sizeof(Cs218Packet));
	entry->prev = NULL;
	entry->next = NULL;
	if( previous == NULL ) {
		cs218->lqList.head = entry;
		cs218->lqList.tail = entry;
	} else {
		previous->next = entry;
		entry->prev = previous;
		cs218->lqList.tail = entry;
	}
}



static 
Cs218PacketEntry* Cs218RetrieveLocationQueryXYLS(Node *node, Cs218Data* cs218, Cs218AddrSeqInfo *srcAddrSeq, Cs218AddrSeqInfo *vehAddrSeq) {
	Cs218PacketEntry* current = cs218->lqList.head;
	
	while( current ) {
		if( current->packet.srcAddrSeq.address == srcAddrSeq->address &&
			current->packet.vehAddrSeq.address == vehAddrSeq->address )
			return current;

		current = current->next;
	}
	return NULL;
	
}

static 
void Cs218ClearOldLocationQueries(Node *node, Cs218Data* cs218) {
	Cs218PacketEntry* current = cs218->lqList.head;
	
	clocktype curTime = getSimTime(node);
	while( current ) {
		Cs218PacketEntry* del = NULL;
		if( curTime > current->timestamp ) {
			del = current;
			if( current->prev ) {
				current->prev->next = current->next;
			} else {
				cs218->lqList.head = current->next;
			}
			if( current->next ) {
				current->next->prev = current->prev;
			} else {
				cs218->lqList.tail = current->prev;
			}
			cs218->lqList.size--;
		}
		current = current->next;
		if( del ) 
			MEM_free(del);
	}
	
}



static
void Cs218PrintRoutingTable(
         Node* node,
         Cs218RoutingTable* routeTable)
{
	char log[MAX_STRING_LENGTH] = {0,};
    Cs218RouteEntry* rtEntry = NULL;
    int i = 0;
    sprintf(log, "The Routing Table of Node %u is:\n"
        "The Routing Table is:\n"
        " Type       Node            Seq   Position         Time     \n"
        "------------------------------------------------------------\n", node->nodeId);
    printf(log);
	fprintf(cs218_info_fp, log);
	for (rtEntry = routeTable->head; rtEntry != NULL;
        rtEntry = rtEntry->next)
    {
		char node[MAX_STRING_LENGTH];
        char time[MAX_STRING_LENGTH];

		Address address;
		SetIPv4AddressInfo(&address, rtEntry->node.address);
		
        IO_ConvertIpAddressToString(
            &address,
            node);
        

        TIME_PrintClockInSecond(rtEntry->timestamp, time);

        sprintf(log, " %-10s %-15s  %-5u   (%5d,%5d)    %s  \n", 
            ((rtEntry->node.nodeType==1)?"SENSOR":"VEHICLE"),
            node,
            rtEntry->node.seqNum,
            rtEntry->node.coord.x,
            rtEntry->node.coord.y,
            time);
		printf(log);
		fprintf(cs218_info_fp, log);
    }
    sprintf(log, "------------------------------------------------------------\n\n");
	printf(log);
	fprintf(cs218_info_fp, log);
}


static
void Cs218PrintVehicleTable(
         Node* node,
         Cs218VehicleTable* vehicleTable)
{
    char log[MAX_STRING_LENGTH] = {0,};
    Cs218VehicleEntry* rtEntry = NULL;
    int i = 0;
    sprintf(log, "The Vehicle Table of Node %u is:\n"
        "The Vehicle Table is:\n"
        " Type       Node            Seq       Position      RelayNode    Time     \n"
        "------------------------------------------------------------\n", node->nodeId);
    printf(log);
	fprintf(cs218_info_fp, log);
	for (rtEntry = vehicleTable->head; rtEntry != NULL;
        rtEntry = rtEntry->next)
    {
		char node[MAX_STRING_LENGTH];
        char time[MAX_STRING_LENGTH];
		char relayNode[MAX_STRING_LENGTH] = {0,};

		Address address;
		SetIPv4AddressInfo(&address, rtEntry->node.address);
		
        IO_ConvertIpAddressToString(
            &address,
            node);
		if( rtEntry->relayNode.seqNum > 0 ) {
			SetIPv4AddressInfo(&address, rtEntry->relayNode.address);
			IO_ConvertIpAddressToString( &address, relayNode);
		}
        

        TIME_PrintClockInSecond(rtEntry->lifetime, time);

        sprintf(log, " %-10s %-15s  %-5u   (%5d,%5d)    %d(%s)        %s  \n", 
            ((rtEntry->node.nodeType==1)?"SENSOR":"VEHICLE"),
            node,
            rtEntry->node.seqNum,
            rtEntry->node.coord.x,
            rtEntry->node.coord.y,
			rtEntry->relayNode.seqNum,
			relayNode,
            time);
		printf(log);
		fprintf(cs218_info_fp, log);
		
    }
    sprintf(log, "------------------------------------------------------------\n\n");
	printf(log);
	fprintf(cs218_info_fp, log);
}

static
void Cs218PrintLocationDirectory(
         Node* node,
         Cs218LocationDirectory* locationDirectory)
{
    char log[MAX_STRING_LENGTH] = {0,};

    Cs218RouteEntry* rtEntry = NULL;
    int i = 0;
    sprintf(log, "The Location Directory of Node %u is:\n"
        "The Location Directory is:\n"
        " Type       Node            Seq   Position         Time     \n"
        "------------------------------------------------------------\n", node->nodeId);
	printf(log);
	fprintf(cs218_info_fp, log);
	for (rtEntry = locationDirectory->head; rtEntry != NULL;
        rtEntry = rtEntry->next)
    {
		char node[MAX_STRING_LENGTH];
        char time[MAX_STRING_LENGTH];

		Address address;
		SetIPv4AddressInfo(&address, rtEntry->node.address);
		
        IO_ConvertIpAddressToString(
            &address,
            node);
        

        TIME_PrintClockInSecond(rtEntry->timestamp, time);

        sprintf(log, " %-10s %-15s  %-5u   (%5d,%5d)    %s  \n", 
            ((rtEntry->node.nodeType==1)?"SENSOR":"VEHICLE"),
            node,
            rtEntry->node.seqNum,
            rtEntry->node.coord.x,
            rtEntry->node.coord.y,
            time);
		printf(log);
		fprintf(cs218_info_fp, log);
    }
    sprintf(log, "------------------------------------------------------------\n\n");
	printf(log);
	fprintf(cs218_info_fp, log);
}


//.....................................................


static
void Cs218InsertBuffer(
         Node* node,
         Message* msg,
         NodeAddress destAddr,
         NodeAddress previousHop,
         Cs218MessageBuffer* buffer)
{
    Cs218Data* cs218 = NULL;
	
	cs218 = (Cs218Data*) NetworkIpGetRoutingProtocol(
								 node,
								 ROUTING_PROTOCOL_CS218,
								 NETWORK_IPV4);

    // Find Insertion point.  Insert after all address matches.
    // This is to maintain a sorted list in ascending order of the
    // destination address
    Cs218BufferNode *previous = NULL;
    Cs218BufferNode *current = buffer->head;

	while( current ) {
		previous = current;
		current = current->next;
	}
	
    Cs218BufferNode *newNode = (Cs218BufferNode*) MEM_malloc(sizeof(Cs218BufferNode));
    // Store the allocate message along with the destination number and
    // the time at which the packet has been inserted

	newNode->destAddr = destAddr;
	newNode->previousHop = previousHop;
	
    newNode->msg = msg;
    newNode->timestamp = getSimTime(node) + CS218_DEFAULT_ACTIVE_ROUTE_TIMEOUT;
    newNode->next = current;
	
    // Increase the size of the buffer
    ++(buffer->size);

    // Got the insertion point
    if (previous == NULL)
    {
        // The is the first message in the buffer or to be
        // inserted in the first
        buffer->head = newNode;
    }
    else
    {
        // This is an intermediate node in the list
        previous->next = newNode;
    }
}


static
Cs218BufferNode* Cs218RetrieveBuffer(
					 Node* node,
					 NodeAddress destAddr,
					 Cs218MessageBuffer* buffer)
{
    Cs218Data* cs218 = NULL;
	
	cs218 = (Cs218Data*) NetworkIpGetRoutingProtocol(
								 node,
								 ROUTING_PROTOCOL_CS218,
								 NETWORK_IPV4);

    // Find Insertion point.  Insert after all address matches.
    // This is to maintain a sorted list in ascending order of the
    // destination address
    Cs218BufferNode *previous = NULL;
    Cs218BufferNode *current = buffer->head;

	while( current ) {
		if( current->destAddr == destAddr ) {
			Cs218BufferNode *tmp = current;
			buffer->size--;
			if( previous == NULL ) {
				buffer->head = current->next;
			} else {
				previous->next = current->next;
			}
			return tmp;
		} else {
			previous = current;
			current = current->next;
		}
	}
	return NULL;
}


static 
void Cs218ClearOldMessageBuffer(Node *node, Cs218Data* cs218) {
	Cs218BufferNode* current = cs218->messageBuffer.head;
	Cs218BufferNode* previous = NULL;
	
	clocktype curTime = getSimTime(node);
	while( current ) {
		Cs218BufferNode* del = NULL;
		if( curTime > current->timestamp ) {
			del = current;
			if( previous ) {
				previous->next = current->next;
			} else {
				cs218->messageBuffer.head = current->next;
			}
			cs218->messageBuffer.size--;
		}
		previous = current;
		current = current->next;
		if( del ) 
			MEM_free(del);
	}
	
}

static 
void Cs218Clear(Node *node, Cs218Data* cs218) {
	Cs218ClearOldLocationDirectory(node, cs218);
	Cs218ClearOldMessageBuffer(node, cs218);
	Cs218ClearOldLocationQueries(node, cs218);
	Cs218ClearOldVehicleTable(node, cs218);
	
}

void
Cs218SendPacket(
    Node* node,
    Message* msg,
    int interfaceIndex,
    NodeAddress nextHopAddress,
    clocktype delay,
    BOOL isDelay)
{
	Cs218Data *cs218 = (Cs218Data *) NetworkIpGetRoutingProtocol(node,
											ROUTING_PROTOCOL_CS218,
											NETWORK_IPV4);
    if(isDelay)
    {
        //Trace sending packet
        ActionData acnData;
        acnData.actionType = SEND;
        acnData.actionComment = NO_COMMENT;
        TRACE_PrintTrace(node, msg, TRACE_NETWORK_LAYER,
              PACKET_OUT, &acnData , cs218->defaultInterfaceAddr.networkType);

		NetworkIpSendRawMessageToMacLayerWithDelay(
			node, 
			msg,
			NetworkIpGetInterfaceAddress(node, DEFAULT_INTERFACE),
			nextHopAddress,
			IPTOS_PREC_INTERNETCONTROL,
			IPPROTO_CS218,
			CS218_MAX_ROUTE_LENGTH,
			DEFAULT_INTERFACE,
			nextHopAddress,
			delay);
    }
    else
    {
        //Trace sending packet
        ActionData acnData;
        acnData.actionType = SEND;
        acnData.actionComment = NO_COMMENT;
        TRACE_PrintTrace(node, msg, TRACE_NETWORK_LAYER,
                  PACKET_OUT, &acnData, cs218->defaultInterfaceAddr.networkType);

        NetworkIpSendRawMessageToMacLayer(
			node, 
			msg,
			NetworkIpGetInterfaceAddress(node, DEFAULT_INTERFACE),
			nextHopAddress,
			IPTOS_PREC_INTERNETCONTROL,
			IPPROTO_CS218,
			CS218_MAX_ROUTE_LENGTH,
			DEFAULT_INTERFACE,
			nextHopAddress);
    }
}

static
void Cs218InitializeHelloPacket(Node* node, Cs218Data* cs218)
{
    Message* newMsg = NULL;
    Cs218Packet* pkt = NULL;
    NetworkRoutingProtocolType protocolType = ROUTING_PROTOCOL_CS218;
    char* pktPtr = NULL;
    int pktSize = sizeof(Cs218Packet);
    int i= 0;

    BOOL isDelay = TRUE;


    newMsg = MESSAGE_Alloc(
                 node,
                 NETWORK_LAYER,
                 protocolType,
                 MSG_MAC_FromNetwork);

    MESSAGE_PacketAlloc(
        node,
        newMsg,
        pktSize,
        TRACE_CS218);

    pktPtr = (char *) MESSAGE_ReturnPacket(newMsg);

    memset(pktPtr, 0, pktSize);

    pkt = (Cs218Packet *) pktPtr;
    pkt->packetType = CS218_HELLO;
    memcpy( &(pkt->srcAddrSeq), &(cs218->addrSeq), sizeof(Cs218AddrSeqInfo) );
    memcpy( &(pkt->prevHop), &(cs218->addrSeq), sizeof(Cs218AddrSeqInfo) );
    pkt->floodingId = Cs218GetFloodingId( cs218 );
    pkt->lifetime = (unsigned int) (CS218_DEFAULT_HELLO_TIMEOUT / MILLI_SECOND);

    Address address;
    SetIPv4AddressInfo(&address, cs218->addrSeq.address);
    
    for (i = 0; i < node->numberInterfaces; i++)
    {
        clocktype delay =
            (clocktype) CS218_PC_ERAND(cs218->cs218JitterSeed);


        Cs218SendPacket(
            node,
            MESSAGE_Duplicate(node, newMsg),
            i,
            ANY_DEST,
            delay,
            isDelay);
    }
	
	printLog(node, newMsg, pkt, "INIT_HELLO");

    MESSAGE_Free(node, newMsg);

    cs218->stats.numHelloPacketSent++;

}


static void Cs218InitializeReplyHelloPacket(Node* node, Cs218Data* cs218, Cs218AddrSeqInfo *dest) {
    Message* newMsg = NULL;
    Cs218Packet* pkt = NULL;
    NetworkRoutingProtocolType protocolType = ROUTING_PROTOCOL_CS218;
    char* pktPtr = NULL;
    int pktSize = sizeof(Cs218Packet);
    int i= 0;

    BOOL isDelay = TRUE;

    newMsg = MESSAGE_Alloc(
                 node,
                 NETWORK_LAYER,
                 protocolType,
                 MSG_MAC_FromNetwork);

    MESSAGE_PacketAlloc(
        node,
        newMsg,
        pktSize,
        TRACE_CS218);

    pktPtr = (char *) MESSAGE_ReturnPacket(newMsg);

    memset(pktPtr, 0, pktSize);

    pkt = (Cs218Packet *) pktPtr;
    pkt->packetType = CS218_R_HELLO;
    memcpy( &(pkt->srcAddrSeq), &(cs218->addrSeq), sizeof(Cs218AddrSeqInfo) );
    memcpy( &(pkt->prevHop), &(cs218->addrSeq), sizeof(Cs218AddrSeqInfo) );
    memcpy( &(pkt->destAddrSeq), dest, sizeof(Cs218AddrSeqInfo) );
    memcpy( &(pkt->nextHop), dest, sizeof(Cs218AddrSeqInfo) );
    pkt->floodingId = Cs218GetFloodingId( cs218 );
    pkt->lifetime = (unsigned int) (CS218_DEFAULT_HELLO_TIMEOUT / MILLI_SECOND);

    Address address;
    SetIPv4AddressInfo(&address, cs218->addrSeq.address);
    
    for (i = 0; i < node->numberInterfaces; i++)
    {
        clocktype delay =
            (clocktype) CS218_PC_ERAND(cs218->cs218JitterSeed);


        Cs218SendPacket(
            node,
            MESSAGE_Duplicate(node, newMsg),
            i,
            ANY_DEST,
            delay,
            isDelay);
    }
	
	printLog(node, newMsg, pkt, "INIT_R_HELLO to %u ", dest->seqNum );

    MESSAGE_Free(node, newMsg);

    cs218->stats.numRHelloPacketSent++;
}

static 
void Cs218InitializeLocationQueryPacket(Node* node, Cs218Data* cs218, NodeAddress interestingAddress) {
	Message* newMsg = NULL;
    Cs218Packet* pkt = NULL;
    NetworkRoutingProtocolType protocolType = ROUTING_PROTOCOL_CS218;
    char* pktPtr = NULL;
    int pktSize = sizeof(Cs218Packet);
    int i= 0;

    BOOL isDelay = TRUE;
	Cs218RouteEntry *dest = NULL; 
	Cs218RouteEntry **dests = new Cs218RouteEntry* [2];
	int destSize = 0;
	Cs218Coord fourPtCoord;

	if( cs218->useXYLS == false ) {
		// CS218 ROUTING
		int dist = 0;

		
		// find the farest point
		for(int idx=0; idx<4; ++idx) {
			int xx, yy;
			Cs218RetrieveCPInfo( idx, &xx, &yy);
			Cs218Coord cpCoord;
			cpCoord.x = xx;
			cpCoord.y = yy;
			int diff = distance(&cpCoord, &(cs218->addrSeq.coord));
			if( diff > dist ) {
				dist = diff;
				fourPtCoord = cpCoord;
			}
		}
		Cs218RouteEntry *each = cs218->routeTable.head;
		if( fourPtCoord.x != cs218->addrSeq.coord.x ) {		
			dist = abs(fourPtCoord.x - cs218->addrSeq.coord.x);
			while(each) {
				int diff = abs(fourPtCoord.x - each->node.coord.x);
				if( diff < dist ) {
					dest = each;
					dist = diff;
				}
				each = each->next;
			}
		} else { 
			// when x is the same then y approach...
			dist = abs(fourPtCoord.y - cs218->addrSeq.coord.y);
			while(each) {
				int diff = abs(fourPtCoord.y - each->node.coord.y);
				if( diff < dist ) {
					dest = each;
					dist = diff;
				}
				each = each->next;
			}
		}
		if( dest != NULL ) {
			dests[destSize] = dest;
			destSize++;
		}
	} else {
		// XYLS routing
		// find west and east
		Cs218RouteEntry *wNode = Cs218FindEntryInDirection(cs218, &(cs218->routeTable), CS218_XYLS_W);
		Cs218RouteEntry *eNode = Cs218FindEntryInDirection(cs218, &(cs218->routeTable), CS218_XYLS_E);
		if( !wNode ) {
			wNode = Cs218FindEntryInDirection(cs218, &(cs218->routeTable), CS218_XYLS_N);
		}
		if( !wNode ) {
			eNode = Cs218FindEntryInDirection(cs218, &(cs218->routeTable), CS218_XYLS_S);
		}

		if( wNode ) {
			dests[destSize] = wNode;
			destSize++;
		}		
		if( eNode ) {
			dests[destSize] = eNode;
			destSize++;
		}

	}
	if( destSize == 0 ) {
		Cs218Packet p;
		memset(&p, 0x00, sizeof(Cs218Packet));
		p.packetType = CS218_LOC_QRY;
		printLog(node, newMsg, &p, "INIT_LOCATION_QUERY - NO NEXT HOP");
		return;
	}
	
	for(int idx = 0; idx<destSize; ++idx ) {
		dest = dests[idx];
		newMsg = MESSAGE_Alloc(
                 node,
                 NETWORK_LAYER,
                 protocolType,
                 MSG_MAC_FromNetwork);

	    MESSAGE_PacketAlloc(
	        node,
	        newMsg,
	        pktSize,
	        TRACE_CS218);

	    pktPtr = (char *) MESSAGE_ReturnPacket(newMsg);

	    memset(pktPtr, 0, pktSize);

	    pkt = (Cs218Packet *) pktPtr;
	    pkt->packetType = CS218_LOC_QRY;
	    memcpy( &(pkt->srcAddrSeq), &(cs218->addrSeq), sizeof(Cs218AddrSeqInfo) );
		pkt->destAddrSeq.coord = fourPtCoord;
		
	    memcpy( &(pkt->prevHop), &(cs218->addrSeq), sizeof(Cs218AddrSeqInfo) );	// local address
	    memcpy( &(pkt->nextHop), &(dest->node), sizeof(Cs218AddrSeqInfo) ); 
		pkt->vehAddrSeq.address = interestingAddress;
	    pkt->floodingId = Cs218GetFloodingId( cs218 );
	    pkt->lifetime = (unsigned int) (CS218_DEFAULT_HELLO_TIMEOUT / MILLI_SECOND);

	    Address address;
	    SetIPv4AddressInfo(&address, cs218->addrSeq.address);
	    
	    for (i = 0; i < node->numberInterfaces; i++)
	    {
	        clocktype delay =
	            (clocktype) CS218_PC_ERAND(cs218->cs218JitterSeed);


	        Cs218SendPacket(
	            node,
	            MESSAGE_Duplicate(node, newMsg),
	            i,
	            ANY_DEST,
	            delay,
	            isDelay);
	    }

		char strVehAddress[MAX_STRING_LENGTH] = {0,};
		Cs218NodeAddressToString( &interestingAddress, strVehAddress );
		printLog(node, newMsg, pkt, "INIT_LOCATION_QUERY for %s to %u", strVehAddress, dest->node.seqNum);

	    MESSAGE_Free(node, newMsg);

	    cs218->stats.numLQPacketSent++;
	}
	
    
}

static 
void Cs218RelayLocationQueryPacket(Node* node, Cs218Data* cs218, Cs218Packet* origPacket) {
	Message* newMsg = NULL;
    Cs218Packet* pkt = NULL;
    NetworkRoutingProtocolType protocolType = ROUTING_PROTOCOL_CS218;
    char* pktPtr = NULL;
    int pktSize = sizeof(Cs218Packet);
    int i= 0;

    BOOL isDelay = TRUE;

	Cs218AddrSeqInfo* destAddr = &(origPacket->destAddrSeq);
	UInt32 dest_x = destAddr->coord.x;
	UInt32 dest_y = destAddr->coord.y;
	Cs218RouteEntry* dest = NULL;
	int nextDirection = origPacket->direction;;	// use in CS218_XYLS

	if( cs218->useXYLS == false ) {
		// CS218 routing

		if( dest_x == cs218->addrSeq.coord.x && dest_y == cs218->addrSeq.coord.y ) {
			// reached change destination
			dest_x = origPacket->srcAddrSeq.coord.x;
			dest_y = origPacket->srcAddrSeq.coord.y;
		}
		Cs218RouteEntry *each = cs218->routeTable.head;
		int dist;
		if( dest_x != cs218->addrSeq.coord.x ) {
			dist = (dest_x - cs218->addrSeq.coord.x)*(dest_x - cs218->addrSeq.coord.x);
			while(each) {
				int diff = (dest_x - each->node.coord.x)*(dest_x - each->node.coord.x);
				if( diff < dist ) {
					dest = each;
					dist = diff;
				}
				each = each->next;
			}
		} else { 
			// when x is the same then y approach...
			dist = (dest_y - cs218->addrSeq.coord.y)*(dest_y - cs218->addrSeq.coord.y);
			while(each) {
				int diff = (dest_y - each->node.coord.y)*(dest_y - each->node.coord.y);
				if( diff < dist ) {
					dest = each;
					dist = diff;
				}
				each = each->next;
			}
		}
	} else {
		// CS218 XYLS routing
		// find next node in samedirection first
		// if there's no node find node in clock-wise search
		Cs218RouteEntry *next = Cs218FindEntryInDirection(cs218, &(cs218->routeTable), origPacket->direction);
		int oldIdx = origPacket->direction;
		int newIdx = oldIdx;
		while ( next == NULL ) {
			newIdx = (newIdx+1)%4;
			next = Cs218FindEntryInDirection(cs218, &(cs218->routeTable), newIdx);
			nextDirection = newIdx;
			if( newIdx == oldIdx )
				break;
		}
		if( next ) {
			dest = next;
		}
	}

	
	if( dest == NULL ) {
		Cs218Packet p;
		memset(&p, 0x00, sizeof(Cs218Packet));
		p.packetType = CS218_LOC_QRY;
		printLog(node, newMsg, &p, "RELAY_LOCATION_QUERY - NO NEXT HOP");
		return;
	}
	
    newMsg = MESSAGE_Alloc(
                 node,
                 NETWORK_LAYER,
                 protocolType,
                 MSG_MAC_FromNetwork);

    MESSAGE_PacketAlloc(
        node,
        newMsg,
        pktSize,
        TRACE_CS218);

    pktPtr = (char *) MESSAGE_ReturnPacket(newMsg);

    memset(pktPtr, 0, pktSize);

    pkt = (Cs218Packet *) pktPtr;
    pkt->packetType = CS218_LOC_QRY;
    memcpy( &(pkt->srcAddrSeq), &(origPacket->srcAddrSeq), sizeof(Cs218AddrSeqInfo) );
	pkt->destAddrSeq.coord.x = dest_x;
	pkt->destAddrSeq.coord.y = dest_y;
	
    memcpy( &(pkt->prevHop), &(cs218->addrSeq), sizeof(Cs218AddrSeqInfo) );	// local address
    memcpy( &(pkt->nextHop), &(dest->node), sizeof(Cs218AddrSeqInfo) ); 
	pkt->vehAddrSeq.address = origPacket->vehAddrSeq.address;
    pkt->floodingId = Cs218GetFloodingId( cs218 );
    pkt->lifetime = (unsigned int) (CS218_DEFAULT_HELLO_TIMEOUT / MILLI_SECOND);
    pkt->direction = nextDirection;

    Address address;
    SetIPv4AddressInfo(&address, cs218->addrSeq.address);
    
    for (i = 0; i < node->numberInterfaces; i++)
    {
        clocktype delay =
            (clocktype) CS218_PC_ERAND(cs218->cs218JitterSeed);


        Cs218SendPacket(
            node,
            MESSAGE_Duplicate(node, newMsg),
            i,
            ANY_DEST,
            delay,
            isDelay);
    }
	
	char strVehAddress[MAX_STRING_LENGTH] = {0,};
	Cs218NodeAddressToString( &(origPacket->vehAddrSeq.address), strVehAddress );
	printLog(node, newMsg, pkt, "RELAY_LOCATION_QUERY for %s to %u", strVehAddress, dest->node.seqNum);


    MESSAGE_Free(node, newMsg);

    cs218->stats.numLQPacketSent++;
}


static 
void Cs218InitializeReplyLocationQueryPacket(Node* node, Cs218Data* cs218, Cs218Packet* origPacket, Cs218AddrSeqInfo* vehicleNode) {
	Message* newMsg = NULL;
    Cs218Packet* pkt = NULL;
    NetworkRoutingProtocolType protocolType = ROUTING_PROTOCOL_CS218;
    char* pktPtr = NULL;
    int pktSize = sizeof(Cs218Packet);
    int i= 0;

    BOOL isDelay = TRUE;

	Cs218AddrSeqInfo *srcNode = &(origPacket->srcAddrSeq);
	int src_x = srcNode->coord.x;
	int src_y = srcNode->coord.y;
	// find next hop
	// TODO route ... first version it goes to right
	int minDistance = (cs218->addrSeq.coord.x - src_x)*(cs218->addrSeq.coord.x - src_x)+(cs218->addrSeq.coord.y - src_y)*(cs218->addrSeq.coord.y - src_y);
	Cs218RouteEntry *each = cs218->routeTable.head;
	Cs218RouteEntry *dest = NULL; 
	while( each ) {
		int diff = distance( &(each->node.coord), &(srcNode->coord) );
		if( diff < minDistance ) {
			dest = each;
			minDistance = diff;
		}
		each = each->next;
	}
	
	if( dest == NULL ) {
		Cs218Packet p;
		memset(&p, 0x00, sizeof(Cs218Packet));
		p.packetType = CS218_R_LOC_QRY;
		printLog(node, newMsg, &p, "INIT_R_LOCATION_QUERY - NO NEXT HOP");
		return;
	}
	
    newMsg = MESSAGE_Alloc(
                 node,
                 NETWORK_LAYER,
                 protocolType,
                 MSG_MAC_FromNetwork);

    MESSAGE_PacketAlloc(
        node,
        newMsg,
        pktSize,
        TRACE_CS218);

    pktPtr = (char *) MESSAGE_ReturnPacket(newMsg);

    memset(pktPtr, 0, pktSize);

    pkt = (Cs218Packet *) pktPtr;
    pkt->packetType = CS218_R_LOC_QRY;
    memcpy( &(pkt->srcAddrSeq), &(cs218->addrSeq), sizeof(Cs218AddrSeqInfo) );
    memcpy( &(pkt->destAddrSeq), &(origPacket->srcAddrSeq), sizeof(Cs218AddrSeqInfo) );
	
    memcpy( &(pkt->prevHop), &(cs218->addrSeq), sizeof(Cs218AddrSeqInfo) );	// local address
    memcpy( &(pkt->nextHop), &(dest->node), sizeof(Cs218AddrSeqInfo) ); 
	
	memcpy( &(pkt->vehAddrSeq), vehicleNode, sizeof(Cs218AddrSeqInfo) );
	
    pkt->floodingId = Cs218GetFloodingId( cs218 );
    pkt->lifetime = (unsigned int) (CS218_DEFAULT_HELLO_TIMEOUT / MILLI_SECOND);

    Address address;
    SetIPv4AddressInfo(&address, cs218->addrSeq.address);
    
    for (i = 0; i < node->numberInterfaces; i++)
    {
        clocktype delay =
            (clocktype) CS218_PC_ERAND(cs218->cs218JitterSeed);


        Cs218SendPacket(
            node,
            MESSAGE_Duplicate(node, newMsg),
            i,
            ANY_DEST,
            delay,
            isDelay);
    }
	char strVehAddress[MAX_STRING_LENGTH] = {0,};
	Cs218NodeAddressToString( &(pkt->vehAddrSeq.address), strVehAddress );
	printLog(node, newMsg, pkt, "INIT_R_LOCATION_QUERY for %s to %u", strVehAddress, dest->node.seqNum);


    MESSAGE_Free(node, newMsg);

    cs218->stats.numRLQPacketSent++;
}


static 
void Cs218RelayReplyLocationQueryPacket(Node* node, Cs218Data* cs218, Cs218Packet* origPacket) {
	Message* newMsg = NULL;
    Cs218Packet* pkt = NULL;
    NetworkRoutingProtocolType protocolType = ROUTING_PROTOCOL_CS218;
    char* pktPtr = NULL;
    int pktSize = sizeof(Cs218Packet);
    int i= 0;

    BOOL isDelay = TRUE;

	Cs218AddrSeqInfo *destNode = &(origPacket->destAddrSeq);
	int dest_x = destNode->coord.x;
	int dest_y = destNode->coord.y;

	//if( cs218->useXYLS == false ) {
		// CS218 routing// find next hop
		// TODO route ... first version it goes to right
		int minDistance = (cs218->addrSeq.coord.x - dest_x)*(cs218->addrSeq.coord.x - dest_x)+(cs218->addrSeq.coord.y - dest_y)*(cs218->addrSeq.coord.y - dest_y);
		Cs218RouteEntry *each = cs218->routeTable.head;
		Cs218RouteEntry *dest = NULL; 
		while( each ) {
			int diff = distance( &(each->node.coord), &(destNode->coord) );
			if( diff < minDistance ) {
				dest = each;
				minDistance = diff;
			}
			each = each->next;
		}
	//} else {
	//	// CS218-XYLS routing
	//}
	
	
	if( dest == NULL ) {
		Cs218Packet p;
		memset(&p, 0x00, sizeof(Cs218Packet));
		p.packetType=CS218_R_LOC_QRY;
		printLog(node, newMsg, &p, "RELAY_R_LOCATION_QUERY - NO NEXT HOP");
		return;
	}
	
    newMsg = MESSAGE_Alloc(
                 node,
                 NETWORK_LAYER,
                 protocolType,
                 MSG_MAC_FromNetwork);

    MESSAGE_PacketAlloc(
        node,
        newMsg,
        pktSize,
        TRACE_CS218);

    pktPtr = (char *) MESSAGE_ReturnPacket(newMsg);

    memset(pktPtr, 0, pktSize);

    pkt = (Cs218Packet *) pktPtr;
    pkt->packetType = CS218_R_LOC_QRY;
    memcpy( &(pkt->srcAddrSeq), &(origPacket->srcAddrSeq), sizeof(Cs218AddrSeqInfo) );
    memcpy( &(pkt->destAddrSeq), &(origPacket->destAddrSeq), sizeof(Cs218AddrSeqInfo) );
	
    memcpy( &(pkt->prevHop), &(cs218->addrSeq), sizeof(Cs218AddrSeqInfo) );	// local address
    memcpy( &(pkt->nextHop), &(dest->node), sizeof(Cs218AddrSeqInfo) ); 
	
	memcpy( &(pkt->vehAddrSeq), &(origPacket->vehAddrSeq), sizeof(Cs218AddrSeqInfo) );
	
    pkt->floodingId = Cs218GetFloodingId( cs218 );
    pkt->lifetime = (unsigned int) (CS218_DEFAULT_HELLO_TIMEOUT / MILLI_SECOND);

    Address address;
    SetIPv4AddressInfo(&address, cs218->addrSeq.address);
    
    for (i = 0; i < node->numberInterfaces; i++)
    {
        clocktype delay =
            (clocktype) CS218_PC_ERAND(cs218->cs218JitterSeed);


        Cs218SendPacket(
            node,
            MESSAGE_Duplicate(node, newMsg),
            i,
            ANY_DEST,
            delay,
            isDelay);
    }
	char strVehAddress[MAX_STRING_LENGTH] = {0,};
	Cs218NodeAddressToString( &(pkt->vehAddrSeq.address), strVehAddress );
	printLog(node, newMsg, pkt, "RELAY_R_LOCATION_QUERY for %s to %u", strVehAddress, dest->node.seqNum);

	
    MESSAGE_Free(node, newMsg);

    cs218->stats.numRLQPacketSent++;
}



static 
void Cs218ProcessLocationUpdatePacket(Node* node, Cs218Data* cs218, Cs218AddrSeqInfo *vehicleNode, int origDirection, bool bCenter, bool isVehicle) {
	Message* newMsg = NULL;
    Cs218Packet* pkt = NULL;
    NetworkRoutingProtocolType protocolType = ROUTING_PROTOCOL_CS218;
    char* pktPtr = NULL;
    int pktSize = sizeof(Cs218Packet);
    int i= 0;

    BOOL isDelay = TRUE;

	Cs218Coord destCoord;
	destCoord.x = cs218->centerCoord.x;
	destCoord.y = cs218->centerCoord.y;
	int dest_x = destCoord.x;
	int dest_y = destCoord.y;


	Cs218RouteEntry *each = cs218->routeTable.head;
	Cs218RouteEntry *dest = NULL; 

	if( cs218->useXYLS == false ) {
		// CS218 routing

		// find next hop
		if( isVehicle ) {
			// send it to registered sensor
			while( each ) {
				if( each->node.seqNum == cs218->currentRegisteredSensor.seqNum ) {
					dest = each;
					break;
				}
				each = each->next;
			}
		} else {
			int nodeDistance = distance( &(cs218->addrSeq.coord), &(destCoord) );
			
			while( each ) {
				int diff = distance( &(each->node.coord), &(destCoord) );
				int nDiff = distance( &(each->node.coord), &(cs218->addrSeq.coord) );
				if( nDiff < cs218->oneHopDistance ) {
					if( bCenter ) {
						// choose closest one
						if( diff < nodeDistance ) {
							dest = each;
							nodeDistance = diff;
						}
					} else {
						// choose farest one
						if( diff > nodeDistance ) {
							dest = each;
							nodeDistance = diff;
						}
					}
				}
				each = each->next;
			}
		}
	} else {
		// CS218-XYLS// find next hop
		if( isVehicle ) {
			// send it to registered sensor
			while( each ) {
				if( each->node.seqNum == cs218->currentRegisteredSensor.seqNum ) {
					dest = each;
					break;
				}
				each = each->next;
			}
		} else {
			int oldIdx = origDirection;
			int newIdx = oldIdx;
			Cs218RouteEntry *next = Cs218FindEntryInDirection(cs218, &(cs218->routeTable), newIdx);
			while( next == NULL ) {
				newIdx = (newIdx+1)%4;
				next = Cs218FindEntryInDirection(cs218, &(cs218->routeTable), newIdx);
				origDirection = newIdx;
				if( newIdx == oldIdx )
					break;
			}
			if( next ) {
				dest = next;
			}
		}

	}
	if( dest == NULL ) {
		Cs218Packet p;
		memset(&p, 0x00, sizeof(Cs218Packet));
		p.packetType=bCenter?CS218_LOC_UPD_C:CS218_LOC_UPD_O;
		printLog(node, newMsg, &p, "INIT_LOCATION_UPDATE_%s - NO NEXT HOP", bCenter?"C":"O");
		return;
	}
	
	
    newMsg = MESSAGE_Alloc(
                 node,
                 NETWORK_LAYER,
                 protocolType,
                 MSG_MAC_FromNetwork);

    MESSAGE_PacketAlloc(
        node,
        newMsg,
        pktSize,
        TRACE_CS218);

    pktPtr = (char *) MESSAGE_ReturnPacket(newMsg);

    memset(pktPtr, 0, pktSize);

    pkt = (Cs218Packet *) pktPtr;
    pkt->packetType = bCenter?CS218_LOC_UPD_C:CS218_LOC_UPD_O;
    memcpy( &(pkt->srcAddrSeq), &(cs218->addrSeq), sizeof(Cs218AddrSeqInfo) );
	
    memcpy( &(pkt->prevHop), &(cs218->addrSeq), sizeof(Cs218AddrSeqInfo) );	// local address
    memcpy( &(pkt->nextHop), &(dest->node), sizeof(Cs218AddrSeqInfo) ); 
	
	memcpy( &(pkt->vehAddrSeq), vehicleNode, sizeof(Cs218AddrSeqInfo) );
	if( isVehicle ) {
		// set vehicle's position as registered sensor's position
		memcpy( &(pkt->vehAddrSeq.coord), &(cs218->currentRegisteredSensor.coord), sizeof( Cs218Coord ) );
	}
	
    pkt->floodingId = Cs218GetFloodingId( cs218 );
    pkt->lifetime = (unsigned int) (CS218_DEFAULT_HELLO_TIMEOUT / MILLI_SECOND);
    pkt->direction = origDirection;

    Address address;
    SetIPv4AddressInfo(&address, cs218->addrSeq.address);
    
    for (i = 0; i < node->numberInterfaces; i++)
    {
        clocktype delay =
            (clocktype) CS218_PC_ERAND(cs218->cs218JitterSeed);


        Cs218SendPacket(
            node,
            MESSAGE_Duplicate(node, newMsg),
            i,
            ANY_DEST,
            delay,
            isDelay);
    }
	printLog(node, newMsg, pkt, "INIT_LOCATION_UPDATE_%s to %u", bCenter?"C":"O", dest->node.seqNum);

    MESSAGE_Free(node, newMsg);

	if( bCenter ) {
		cs218->stats.numLUCPacketSent++;
	} else {
		cs218->stats.numLUOPacketSent++;
	}
}



static 
void Cs218InitializeDataPacket(Node* node, Cs218Data* cs218, Message* msg) {
	
    int i= 0;

    BOOL isDelay = TRUE;
	
	IpHeaderType* ipHeader = (IpHeaderType *) MESSAGE_ReturnPacket(msg);
	
	Cs218Coord destCoord;
	destCoord.x = ipHeader->destination_x;
	destCoord.y = ipHeader->destination_y;
	
	Cs218Packet p;
	memset(&p, 0x00, sizeof(Cs218Packet));
	p.packetType = CS218_DD;
	
	// if I have destination in my vehicleTable
	Cs218VehicleEntry* nodeInVehicleTable = Cs218RetrieveVehicleTable(node, ipHeader->ip_dst);
	Cs218AddrSeqInfo *dest = NULL; 
	if( nodeInVehicleTable != NULL ) {
		if( nodeInVehicleTable->relayNode.seqNum > 0 ) {
			printLog(node, msg, &p, "Relay IP packet to %u", nodeInVehicleTable->relayNode.seqNum );
			dest = &(nodeInVehicleTable->relayNode);
		} else {
			printLog(node, msg, &p, "Forward IP packet to Vehicle %u", nodeInVehicleTable->node.seqNum );
			dest = &(nodeInVehicleTable->node);
		}
	} else {
		
		// find next hop
		int minDistance = distance( &(cs218->addrSeq.coord), &(destCoord) );
		Cs218RouteEntry *each = cs218->routeTable.head;
		while( each ) {
			int diff = distance( &(each->node.coord), &(destCoord));
			if( diff < minDistance ) {
				dest = &(each->node);
				minDistance = diff;
			}
			each = each->next;
		}
	}
	Cs218RouteEntry veh;
	if( dest == NULL ) {
		printLog(node, NULL, &p, "INIT_DATA - NO NEXT HOP");
		return;
	}
	
	printLog(node, msg, &p, "INIT_DATA to %u", dest->seqNum);
		
	NetworkIpSendPacketToMacLayer(
		node,
		msg,
		i,
		dest->address);
}

static 
void Cs218InitializeRegisterPacket(Node* node, Cs218Data* cs218, Cs218AddrSeqInfo* destSensor) {
	Message* newMsg = NULL;
    Cs218Packet* pkt = NULL;
    NetworkRoutingProtocolType protocolType = ROUTING_PROTOCOL_CS218;
    char* pktPtr = NULL;
    int pktSize = sizeof(Cs218Packet);
    int i= 0;

    BOOL isDelay = TRUE;
	
    newMsg = MESSAGE_Alloc(
                 node,
                 NETWORK_LAYER,
                 protocolType,
                 MSG_MAC_FromNetwork);

    MESSAGE_PacketAlloc(
        node,
        newMsg,
        pktSize,
        TRACE_CS218);

    pktPtr = (char *) MESSAGE_ReturnPacket(newMsg);

    memset(pktPtr, 0, pktSize);

    pkt = (Cs218Packet *) pktPtr;
    pkt->packetType = CS218_REG;
    memcpy( &(pkt->srcAddrSeq), &(cs218->addrSeq), sizeof(Cs218AddrSeqInfo) );
    memcpy( &(pkt->destAddrSeq), &(cs218->currentRegisteredSensor), sizeof(Cs218AddrSeqInfo) );
	
    memcpy( &(pkt->prevHop), &(cs218->addrSeq), sizeof(Cs218AddrSeqInfo) );	// local address
    memcpy( &(pkt->nextHop), destSensor, sizeof(Cs218AddrSeqInfo) ); 
	
	memcpy( &(pkt->vehAddrSeq), &(cs218->addrSeq), sizeof(Cs218AddrSeqInfo) );
	
    pkt->floodingId = Cs218GetFloodingId( cs218 );
    pkt->lifetime = (unsigned int) (CS218_DEFAULT_HELLO_TIMEOUT / MILLI_SECOND);

    Address address;
    SetIPv4AddressInfo(&address, cs218->addrSeq.address);
    
    for (i = 0; i < node->numberInterfaces; i++)
    {
        clocktype delay =
            (clocktype) CS218_PC_ERAND(cs218->cs218JitterSeed);


        Cs218SendPacket(
            node,
            MESSAGE_Duplicate(node, newMsg),
            i,
            ANY_DEST,
            delay,
            isDelay);
    }
	char strVehAddress[MAX_STRING_LENGTH] = {0,};
	Cs218NodeAddressToString( &(pkt->vehAddrSeq.address), strVehAddress );
	printLog(node, newMsg, pkt, "INIT_REG for %s to %u", strVehAddress, destSensor->seqNum);

	memcpy( &(cs218->currentRegisteredSensor), destSensor, sizeof(Cs218AddrSeqInfo) ); 
	cs218->registeredTime = getSimTime(node);

    MESSAGE_Free(node, newMsg);

    cs218->stats.numRegisterPacketSent++;
}


static 
void Cs218InitializeRelayRegisterPacket(Node* node, Cs218Data* cs218, Cs218Packet* origPacket) {
	Message* newMsg = NULL;
    Cs218Packet* pkt = NULL;
    NetworkRoutingProtocolType protocolType = ROUTING_PROTOCOL_CS218;
    char* pktPtr = NULL;
    int pktSize = sizeof(Cs218Packet);
    int i= 0;

    BOOL isDelay = TRUE;
	
    newMsg = MESSAGE_Alloc(
                 node,
                 NETWORK_LAYER,
                 protocolType,
                 MSG_MAC_FromNetwork);

    MESSAGE_PacketAlloc(
        node,
        newMsg,
        pktSize,
        TRACE_CS218);

    pktPtr = (char *) MESSAGE_ReturnPacket(newMsg);

    memset(pktPtr, 0, pktSize);

    pkt = (Cs218Packet *) pktPtr;
    pkt->packetType = CS218_RELAY_REG;
    memcpy( &(pkt->srcAddrSeq), &(cs218->addrSeq), sizeof(Cs218AddrSeqInfo) );
    memcpy( &(pkt->destAddrSeq), &(origPacket->destAddrSeq), sizeof(Cs218AddrSeqInfo) );
	
    memcpy( &(pkt->prevHop), &(cs218->addrSeq), sizeof(Cs218AddrSeqInfo) );	// local address
    memcpy( &(pkt->nextHop), &(origPacket->destAddrSeq), sizeof(Cs218AddrSeqInfo) ); 
	
	memcpy( &(pkt->vehAddrSeq), &(origPacket->vehAddrSeq), sizeof(Cs218AddrSeqInfo) );
	
    pkt->floodingId = Cs218GetFloodingId( cs218 );
    pkt->lifetime = (unsigned int) (CS218_DEFAULT_HELLO_TIMEOUT / MILLI_SECOND);

    Address address;
    SetIPv4AddressInfo(&address, cs218->addrSeq.address);
    
    for (i = 0; i < node->numberInterfaces; i++)
    {
        clocktype delay =
            (clocktype) CS218_PC_ERAND(cs218->cs218JitterSeed);


        Cs218SendPacket(
            node,
            MESSAGE_Duplicate(node, newMsg),
            i,
            ANY_DEST,
            delay,
            isDelay);
    }
	char strVehAddress[MAX_STRING_LENGTH] = {0,};
	Cs218NodeAddressToString( &(pkt->vehAddrSeq.address), strVehAddress );
	printLog(node, newMsg, pkt, "RELAY_REG of %s to %u", strVehAddress, origPacket->destAddrSeq.seqNum);


    MESSAGE_Free(node, newMsg);

    cs218->stats.numRelayRegisterPacketSent++;
}



static
BOOL Cs218IpIsMyIP(Node* node,Address destAddr)
{
    return(NetworkIpIsMyIP(node,destAddr.interfaceAddr.ipv4));
}

static
void Cs218HandleData(
         Node* node,
         Message* msg,
         NodeAddress destAddr,
         NodeAddress previousHopAddress)
{
    Cs218Data* cs218 =    NULL;
    IpHeaderType* ipHeader = NULL;
    Address sourceAddress;
    Address destAddress;
	
	cs218 = (Cs218Data *) NetworkIpGetRoutingProtocol(
							node,
							ROUTING_PROTOCOL_CS218,
							NETWORK_IPV4);
	ipHeader = (IpHeaderType *) MESSAGE_ReturnPacket(msg);
	SetIPv4AddressInfo(&sourceAddress,
						  ipHeader->ip_src);
	SetIPv4AddressInfo(&destAddress,
						  ipHeader->ip_dst);
	
	Cs218Packet p;
	memset(&p, 0x00, sizeof(Cs218Packet));
	p.packetType = CS218_DD;
			
    // the node is the destination of the route
    if ( cs218->addrSeq.address == destAddr)
    {
        cs218->stats.numDataRecved++;
		char strIpAddress[MAX_STRING_LENGTH];
		IO_ConvertIpAddressToString( &sourceAddress, strIpAddress);
		printLog(node, msg, &p, "Received IP packet from %s ", strIpAddress);
    }
    else
    {
		cs218->stats.numMsgPacketReceived++;

		if( ipHeader->destination_x > 0 && ipHeader->destination_y > 0 ) {
			cs218->stats.numDataForwarded++;
			Cs218InitializeDataPacket(node, cs218, msg);
			char strSrcIpAddress[MAX_STRING_LENGTH];
			IO_ConvertIpAddressToString( &sourceAddress, strSrcIpAddress);
			char strDstIpAddress[MAX_STRING_LENGTH];
			IO_ConvertIpAddressToString( &destAddress, strDstIpAddress);
			
			printLog(node, msg, &p, "Forward IP packet from %s to %s (%d, %d)", 
				strSrcIpAddress, strDstIpAddress, ipHeader->destination_x, ipHeader->destination_y);
		} else {
		}
    }
}


void
Cs218HandleProtocolPacket(
    Node* node,
    Message* msg,
    Address srcAddr,
    Address destAddr,
    int ttl,
    int interfaceIndex)
{

	Cs218Data *cs218 = (Cs218Data *) NetworkIpGetRoutingProtocol(
						node,
						ROUTING_PROTOCOL_CS218,
						NETWORK_IPV4);

	//trace recd pkt
	ActionData acnData;
	acnData.actionType = RECV;
	acnData.actionComment = NO_COMMENT;
	TRACE_PrintTrace(node, msg, TRACE_NETWORK_LAYER,
	  PACKET_IN, &acnData , srcAddr.networkType);
    
	Cs218Packet* pkt = (Cs218Packet *)MESSAGE_ReturnPacket(msg);
	UInt32 prevHopX, prevHopY;
	prevHopX = pkt->prevHop.coord.x; 
	
	
	
	if( !Cs218ValidRange( &(cs218->addrSeq), &(pkt->prevHop), cs218->oneHopDistance ) ) {
		// Drop out of range
		// I dont know how to modify transmission range, so make it in code.
        MESSAGE_Free(node, msg);
		return;
	}
	
	if( pkt->nextHop.address && pkt->nextHop.address != cs218->addrSeq.address ) {
		// Drop packets which are not broadcast and not assigned 
		MESSAGE_Free(node, msg);
		return;
	}
    
	if( CS218_DEBUG && pkt->packetType != 1 && pkt->packetType != 2) {
		char packetSrcAddr[MAX_STRING_LENGTH];
		char packetDestAddr[MAX_STRING_LENGTH];
		char strSrcAddr[MAX_STRING_LENGTH];
		char strDestAddr[MAX_STRING_LENGTH];
		Cs218NodeAddressToString( &(pkt->srcAddrSeq.address), packetSrcAddr );
		Cs218NodeAddressToString( &(pkt->destAddrSeq.address), packetDestAddr );		
		
		IO_ConvertIpAddressToString( &srcAddr, strSrcAddr );
		IO_ConvertIpAddressToString( &destAddr, strDestAddr );
		
//		printf(".. node [%u(%d,%d)] Cs218HandleProtocolPacket :: %d \n", node->nodeId, cs218->addrSeq.coord.x,cs218->addrSeq.coord.y,pkt->packetType);
//		printf("   flooding Id : %d \n ", pkt->floodingId );
//		printf("   pkt lifetime : %d \n ", pkt->lifetime ); 
//		printf("   from node [%u(%d,%d)] (inPktSrc : %s / src : %s \n", pkt->srcAddrSeq.seqNum, pkt->srcAddrSeq.coord.x,pkt->srcAddrSeq.coord.y, packetSrcAddr, strSrcAddr);
//		printf("   to node [%u(%d,%d)] (inPktSrc : %s / src : %s \n", pkt->destAddrSeq.seqNum, pkt->destAddrSeq.coord.x,pkt->destAddrSeq.coord.y,  packetDestAddr, strDestAddr);
	}
	printLog(node, msg, pkt, "HANDLE_PROTOCOL_PACKET FROM %d", pkt->srcAddrSeq.seqNum);
	
	switch( pkt->packetType ) {
	case CS218_HELLO:
		// send R_HELLO to prevHop
		// add prevHop to routeTable
		
		if( cs218->addrSeq.nodeType == 1 ) {	// node == sensor
			// only sensor reply to hello
			cs218->stats.numHelloPacketReceived++;
			if( pkt->prevHop.nodeType == 1 ) {	
				// sender == sensor
				Cs218InsertEntryToRoutingTable( node, &(pkt->prevHop) );
			} else {
				// sender == vehicle
				//Cs218InsertEntryToVehicleTable(node, &(pkt->prevHop) );
			}
			Cs218InitializeReplyHelloPacket(node, cs218, &(pkt->prevHop));
		}
		
		break;
	case CS218_R_HELLO:
		cs218->stats.numRHelloPacketReceived++;
		Cs218InsertEntryToRoutingTable( node, &(pkt->prevHop) );
		
		break;
	case CS218_LOC_UPD_C:
	{
		cs218->stats.numLUCPacketReceived++;

		Cs218RouteEntry *oldEntry = NULL;
		if( cs218->useXYLS ) 
			if( pkt->prevHop.nodeType == 1 )
				oldEntry = Cs218RetrieveLocationDirectoryXYLS(node, cs218, &(pkt->vehAddrSeq));
		if( oldEntry ) {
			// DO NOTHING
		} else {

			// insert vehicle into location directory
			Cs218InsertLocationDirectory(node, cs218, &(pkt->vehAddrSeq), CS218_DEFAULT_ACTIVE_ROUTE_TIMEOUT);
			// send again 
			Cs218ProcessLocationUpdatePacket(node, cs218, &(pkt->vehAddrSeq), pkt->direction, true, false);
		
			Cs218PacketEntry* entry = Cs218RetrieveLocationQuery(node, cs218, pkt->vehAddrSeq.address );
			while( entry ) {
				Cs218InitializeReplyLocationQueryPacket(node, cs218, &(entry->packet), &(pkt->vehAddrSeq) );

				Cs218DeleteLocationQuery(node, cs218, entry);

				entry = Cs218RetrieveLocationQuery(node, cs218, pkt->vehAddrSeq.address );
			}
		}
	}
		break;
	case CS218_LOC_UPD_O:
	{
		cs218->stats.numLUOPacketReceived++;

		Cs218RouteEntry *oldEntry = NULL;
		if( cs218->useXYLS ) 
			if( pkt->prevHop.nodeType == 1 )
				oldEntry = Cs218RetrieveLocationDirectoryXYLS(node, cs218, &(pkt->vehAddrSeq));
		if( oldEntry ) {
			// DO NOTHING
		} else {

			// insert vehicle into location directory
			Cs218InsertLocationDirectory(node, cs218, &(pkt->vehAddrSeq), CS218_DEFAULT_ACTIVE_ROUTE_TIMEOUT);
			// send again 
			Cs218ProcessLocationUpdatePacket(node, cs218, &(pkt->vehAddrSeq), pkt->direction, false, false);
			
			Cs218PacketEntry* entry = Cs218RetrieveLocationQuery(node, cs218, pkt->vehAddrSeq.address );
			while( entry ) {
				Cs218InitializeReplyLocationQueryPacket(node, cs218, &(entry->packet), &(pkt->vehAddrSeq) );
				Cs218DeleteLocationQuery(node, cs218, entry);
				entry = Cs218RetrieveLocationQuery(node, cs218, pkt->vehAddrSeq.address );
			}
		}
	}
		break;
	case CS218_LOC_QRY:
	{
		cs218->stats.numLQPacketReceived++;
		// check it has ?
		Cs218RouteEntry *vehicleEntry = Cs218RetrieveLocationDirectory(node, cs218, pkt->vehAddrSeq.address);
		Cs218AddrSeqInfo *vehicleAddr = &(vehicleEntry->node);
		if( vehicleAddr ) { // TODO and fresh enough
			Cs218InitializeReplyLocationQueryPacket(node, cs218, pkt, vehicleAddr);
			// TODO : if destination?
		} else if( pkt->srcAddrSeq.address == cs218->addrSeq.address ) {
			// TODO : DO NOTHING?
		} else {
			// already have the same pkt?
			Cs218PacketEntry* oldPkt = NULL;
			if( cs218->useXYLS )
				oldPkt = Cs218RetrieveLocationQueryXYLS(node, cs218, &(pkt->srcAddrSeq), &(pkt->vehAddrSeq));
			if( oldPkt ) {
				// DO NOTHING
				// when packet runs 1 cycle.
			} else {

				Cs218RelayLocationQueryPacket(node, cs218, pkt);
				// add to LQlist
				Cs218InsertLocationQueries( node, cs218, pkt, CS218_DEFAULT_ACTIVE_ROUTE_TIMEOUT );
			}
		}
	}
		break;
	case CS218_R_LOC_QRY:
	{
		// destination?
		cs218->stats.numRLQPacketReceived++;
		if( pkt->destAddrSeq.address == cs218->addrSeq.address ) {
			//printf("..received LQ \n");
			Cs218InsertLocationDirectory(node, cs218, &(pkt->vehAddrSeq), CS218_DEFAULT_ACTIVE_ROUTE_TIMEOUT);
			
			Cs218BufferNode *buffer = Cs218RetrieveBuffer(node, pkt->vehAddrSeq.address, &(cs218->messageBuffer));
			while( buffer ) {
				IpHeaderType* ipHeader = (IpHeaderType *) MESSAGE_ReturnPacket(buffer->msg);
				ipHeader->destination_x = pkt->vehAddrSeq.coord.x;
				ipHeader->destination_y = pkt->vehAddrSeq.coord.y;
				Cs218InitializeDataPacket(node, cs218, buffer->msg);
				MEM_free( buffer );
				buffer = Cs218RetrieveBuffer(node, pkt->vehAddrSeq.address, &(cs218->messageBuffer));
				cs218->stats.numDataSent++;
			}
		} else {
			Cs218RelayReplyLocationQueryPacket(node, cs218, pkt);
		}
	}
		break;
	case CS218_DD:
		break;
	case CS218_REG:
		cs218->stats.numRegisterPacketReceived++;
		// insert into vehicleTable
		Cs218InsertEntryToVehicleTable(node, &(pkt->vehAddrSeq) );
		if( pkt->destAddrSeq.seqNum > 0 &&
			pkt->destAddrSeq.seqNum != cs218->addrSeq.seqNum ) {
			Cs218InitializeRelayRegisterPacket(node, cs218, pkt);
		}
		// if dest.seqNum >0 &&  != me
		// send relay
		break;
	case CS218_RELAY_REG:
		cs218->stats.numRelayRegisterPacketReceived++;
		Cs218UpdateVehicleTable(node, &(pkt->vehAddrSeq), &(pkt->srcAddrSeq) );
		
	}
    MESSAGE_Free(node, msg);
	
}

void
Cs218HandleProtocolEvent(
    Node* node,
    Message* msg)
{
    Cs218Data* cs218 = NULL;

    cs218 = (Cs218Data *) NetworkIpGetRoutingProtocol(
                            node,
                            ROUTING_PROTOCOL_CS218,
                            NETWORK_IPV4);

	int event = MESSAGE_GetEvent(msg);
	if( event != EVENT_VEH_CHECK_POS )
		printLog(node, msg, NULL, "PROCESS_EVENT %d ", event);
    switch (event)
    {
		case CS218_CLEAR_INTERVAL:
			Cs218Clear(node, cs218);
			Cs218SetTimer( node, EVENT_CLEAR, CS218_CLEAR_INTERVAL);
			break;
		case EVENT_VEH_PREPARE_LU:
			// remove All Routing
			Cs218DeleteAllRoutingTable(node);
			Cs218InitializeHelloPacket(node, cs218);
			Cs218SetTimer(node, EVENT_VEH_REGISTER, CS218_LU_HELLO_WAIT);
			Cs218SetTimer(node, EVENT_VEH_PROCESS_LU, CS218_LU_HELLO_WAIT);
			cs218->pendingRegister = true;
			break;
		case EVENT_VEH_PROCESS_LU:
			if( cs218->currentRegisteredSensor.seqNum > 0 ) {
				Cs218ProcessLocationUpdatePacket(node, cs218, &(cs218->addrSeq), CS218_XYLS_N, true, true);
				Cs218ProcessLocationUpdatePacket(node, cs218, &(cs218->addrSeq), CS218_XYLS_S, false, true);
				Cs218SetTimer(node, EVENT_VEH_PREPARE_LU, CS218_LU_INTERVAL - CS218_LU_HELLO_WAIT);
			} else {
				Cs218SetTimer(node, EVENT_VEH_PROCESS_LU, CS218_LU_HELLO_WAIT);
			}
			break;
		case EVENT_VEH_REGISTER:
		{
			// find closest node and register
			Cs218AddrSeqInfo *closest = Cs218RetrieveClosestSensor(node, cs218);
			if( closest ) {
				if( closest->seqNum != cs218->currentRegisteredSensor.seqNum ) {
					Cs218InitializeRegisterPacket(node, cs218, closest);
				}
			}
			cs218->pendingRegister = false;
		}
			break;
		case EVENT_VEH_CHECK_POS:
			Coordinates coordinates;
			MOBILITY_ReturnCoordinates(node, &coordinates);
			cs218->addrSeq.coord.x = coordinates.common.c1;
			cs218->addrSeq.coord.y = coordinates.common.c2;
			cs218->addrSeq.coord.z = 0;
			
			if( cs218->pendingRegister == false ) {
				if( cs218->currentRegisteredSensor.seqNum > 0 ) {
					if( distance(&(cs218->currentRegisteredSensor.coord), &(cs218->addrSeq.coord)) >  (float)cs218->oneHopDistance/2.f ) {
						Cs218DeleteAllRoutingTable(node);
						Cs218InitializeHelloPacket(node, cs218);
						Cs218SetTimer(node, EVENT_VEH_REGISTER, CS218_LU_HELLO_WAIT);
						cs218->pendingRegister = true;
					}
				}
			}
			Cs218SetTimer(node, EVENT_VEH_CHECK_POS, CS218_VEHICLE_CHECK_POS_INTERVAL);
		
		
    }
}

// /**
// FUNCTION: Cs218RouterFunction
// LAYER   : NETWROK
// PURPOSE : Determine the routing action to take for a the given data packet
//          set the PacketWasRouted variable to TRUE if no further handling
//          of this packet by IP is necessary
// PARAMETERS:
// +node:Node *::Pointer to node
// + msg:Message*:The packet to route to the destination
// +destAddr:Address:The destination of the packet
// +previousHopAddress:Address:Last hop of this packet
// +packetWasRouted:BOOL*:set to FALSE if ip is supposed to handle the
//                        routing otherwise TRUE
// RETURN   ::void:NULL
// **/

void
Cs218RouterFunction(
    Node* node,
    Message* msg,
    NodeAddress destAddr,
    NodeAddress previousHopAddress,
    BOOL* packetWasRouted)
{
	if( CS218_DEBUG ) {
		//printf(" Node %u Cs218RouterFunction \n", node->nodeId );
	}
	
    IpHeaderType* ipHeader = (IpHeaderType *) MESSAGE_ReturnPacket(msg);
    Address sourceAddress;
	SetIPv4AddressInfo(&sourceAddress,
						  ipHeader->ip_src);

	
	Cs218Data *cs218 = (Cs218Data *) NetworkIpGetRoutingProtocol(
                            node,
                            ROUTING_PROTOCOL_CS218,
                            NETWORK_IPV4);
	
	if( cs218->addrSeq.address == destAddr ) {
		*packetWasRouted = false;
	} else {
		*packetWasRouted = true;
	}
	
	if( !Cs218IpIsMyIP(node, sourceAddress)) {
		Cs218HandleData(node, msg, destAddr, previousHopAddress);
	} else {
		if( !(*packetWasRouted)) {
			return;
		}
		
		// if ld has route information
		Cs218RouteEntry *rtToDest = NULL;
		rtToDest = Cs218RetrieveLocationDirectory( node, cs218, destAddr );
		
		// sensor knows where the dest veh is
		if( rtToDest ) {
			ipHeader->destination_x = rtToDest->node.coord.x;
			ipHeader->destination_y = rtToDest->node.coord.y;
			cs218->stats.numDataSent++;
			Cs218InitializeDataPacket(node, cs218, msg );
		} else {
			// insert buffer
			// initiate LQ
			
			Cs218InsertBuffer( node, msg, destAddr, previousHopAddress, &cs218->messageBuffer );
			
			Cs218InitializeLocationQueryPacket(node, cs218, destAddr);
		}
		
	}
}

