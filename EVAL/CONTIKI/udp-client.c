#include "contiki.h"
#include "net/routing/routing.h"
#include "random.h"
#include "net/netstack.h"
#include "net/ipv6/simple-udp.h"

#include "sys/log.h"
#include  <math.h>
#define LOG_MODULE "App"
#define LOG_LEVEL LOG_LEVEL_INFO

#define WITH_SERVER_REPLY  1
#define UDP_CLIENT_PORT	8765
#define UDP_SERVER_PORT	5678
#define MAX_READINGS 10

static struct simple_udp_connection udp_conn;

#define SEND_INTERVAL  60*CLOCK_SECOND
#define FAKE_TEMPS 5

static struct simple_udp_connection udp_conn;
static unsigned mex;
static unsigned reading[MAX_READINGS];
/*---------------------------------------------------------------------------*/
PROCESS(udp_client_process, "UDP client");
AUTOSTART_PROCESSES(&udp_client_process);
/*---------------------------------------------------------------------------*/
static float average(){
  float average;
  unsigned sum = 0;
  unsigned no = 0;  
  for (int i=0; i<MAX_READINGS; i++) {
    if (reading[i]!=0){
      sum = sum+reading[i];
      no++;
      reading[i]=0;
    }
  }
  average = ((float)sum)/no;
  return average;
}
//---------------------------------------------------------------------------*/
static unsigned
get_temperature()
{
  static unsigned fake_temps [FAKE_TEMPS] = {30, 25, 20, 15, 10};
  return fake_temps[random_rand() % FAKE_TEMPS];
  
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
  mex = *(unsigned*) data;
  if(mex == 1){
    LOG_INFO("too many receivers \n");
  }
}
/*---------------------------------------------------------------------------*/
PROCESS_THREAD(udp_client_process, ev, data)
{
  static struct etimer periodic_timer;
  static unsigned t; 
  static int avg;
  static unsigned index = 0;
  uip_ipaddr_t dest_ipaddr;  
  static int i;
  PROCESS_BEGIN();
  for (i=0; i<MAX_READINGS; i++) {
    reading[i] = 0;
  } 
  avg=0;
  /* Initialize UDP connection */
  simple_udp_register(&udp_conn, UDP_CLIENT_PORT, NULL,
                      UDP_SERVER_PORT, udp_rx_callback);

  etimer_set(&periodic_timer,SEND_INTERVAL);
  while(1) {
    PROCESS_WAIT_EVENT_UNTIL(etimer_expired(&periodic_timer));

    if(NETSTACK_ROUTING.node_is_reachable() && NETSTACK_ROUTING.get_root_ipaddr(&dest_ipaddr)) {
     // printf("Reconnection\n");
      if(avg == 0){
       //Send to DAG root */
      t = get_temperature();
      LOG_INFO("Sending reading %u to the server", t);
      LOG_INFO_6ADDR(&dest_ipaddr);
      LOG_INFO("\n");
      simple_udp_sendto(&udp_conn, &t, sizeof(t), &dest_ipaddr);
      }else{
         //sending average
      float a = average();
      avg = 0;
      t = (unsigned) round(a);
      LOG_INFO("Sending average as reading %f to the server,rounded to %u", a,t);
     // LOG_INFO("rounding %u to the server", t);
      LOG_INFO_6ADDR(&dest_ipaddr);
      LOG_INFO("\n");
      simple_udp_sendto(&udp_conn, &t, sizeof(t), &dest_ipaddr);
      index = 0;
      }
    } else {
      LOG_INFO("Not reachable yet\n"); 
      avg = 1;
      t = get_temperature();
      LOG_INFO("Storing reading %u locally", t);
      LOG_INFO("\n");
      reading[index++]=t;
      if (index == MAX_READINGS) {
      index = 0;
      }
    }

    etimer_set(&periodic_timer, SEND_INTERVAL);
  }

  PROCESS_END();
}
/*---------------------------------------------------------------------------*/
