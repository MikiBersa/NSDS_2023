/*
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the Institute nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE INSTITUTE AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE INSTITUTE OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 * This file is part of the Contiki operating system.
 *
 */

#include "contiki.h"
#include "net/routing/routing.h"
#include "net/netstack.h"
#include "net/ipv6/simple-udp.h"

#include "sys/log.h"
#include "random.h"
#include <stdio.h>
#include <stdlib.h>

#define LOG_MODULE "App"
#define LOG_LEVEL LOG_LEVEL_INFO

#define UDP_CLIENT_PORT	8765
#define UDP_SERVER_PORT	5678
#define MAX_RECEIVERS 10
#define MAX_READINGS 10

#define ALERT_THRESHOLD 22


static struct simple_udp_connection udp_conn;

static unsigned readings[MAX_READINGS];
static unsigned next_reading=0;
static uip_ipaddr_t receivers[MAX_RECEIVERS];
static int current_rec = 0;
static unsigned mex = 0;


PROCESS(udp_server_process, "UDP server");
AUTOSTART_PROCESSES(&udp_server_process);
/*---------------------------------------------------------------------------*/
static int able(const uip_ipaddr_t *sender_addr){
    if(current_rec == MAX_RECEIVERS){
        LOG_INFO("Checking if the receiver is in the list\n");
        for(int j=0;j<current_rec;j++){
            if(uip_ipaddr_cmp(sender_addr,&(receivers[j])) == 1){
                return 1;
             }
         }
        return 0;
    }
    else{
    //LOG_INFO("we can add a new receiver\n");
    for(int j=0;j<current_rec;j++){
      if(uip_ipaddr_cmp(sender_addr,&(receivers[j])) == 1){
         //LOG_INFO("already seen this receiver\n");
         return 1;
       }}
      LOG_INFO("Adding the new receiver\n");
      uip_ipaddr_copy(&receivers[current_rec],sender_addr);
     current_rec++;
   return 1;
   }
}
/*---------------------------------------------------------------------------*/
static void
udp_rx_callback(struct simple_udp_connection *c,
         const uip_ipaddr_t *sender_addr,
         uint16_t sender_port,
         const uip_ipaddr_t *receiver_addr,
         uint16_t receiver_port,
         const uint8_t *data,
         uint16_t datalen)
{
  unsigned reading = *(unsigned*) data;
  if(able(sender_addr) == 1){
  /* Add reading */
  readings[next_reading++] = reading;
  if (next_reading == MAX_READINGS) {
    next_reading = 0;
  }

  /* Compute average */
  float average;
  unsigned sum = 0;
  unsigned no = 0;  
  for (int i=0; i<MAX_READINGS; i++) {
    if (readings[i]!=0){
      sum = sum+readings[i];
      no++;
    }
  }
  average = ((float)sum)/no;
  LOG_INFO("Current average is %f \n",average);

  }
  else{ 
    //LOG_INFO("Too many receivers \n");
    mex = 1;
    simple_udp_sendto(&udp_conn, &mex, sizeof(mex), sender_addr);
  }
}
/*---------------------------------------------------------------------------*/
PROCESS_THREAD(udp_server_process, ev, data)
{
  
  PROCESS_BEGIN();

  static uint8_t i=0;
  /* Initialize temperature buffer */
  for (i=0; i<next_reading; i++) {
    readings[i] = 0;
  }  
  
  
  /* Initialize DAG root */
  NETSTACK_ROUTING.root_start();

  /* Initialize UDP connection */
  simple_udp_register(&udp_conn, UDP_SERVER_PORT, NULL,
                      UDP_CLIENT_PORT, udp_rx_callback);
  
  PROCESS_END();
  
}
/*---------------------------------------------------------------------------*/
