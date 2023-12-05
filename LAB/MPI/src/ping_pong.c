#include <mpi.h>
#include <stdio.h>
#include <stdlib.h>

/*
Write a ping-pong program
– Process P0 sends a message to process P1 with a number
– Process P1 replies and increments the number
– Stop when the number overcomes a given threshold
*/
// Assume only two processes
int main(int argc, char** argv) {
  const int tot_msgs = 100;

  MPI_Init(NULL, NULL);

  int my_rank;
  MPI_Comm_rank(MPI_COMM_WORLD, &my_rank);

  int num_msgs = 0;
  int other_rank = 1 - my_rank;

  /*
  while(num_msgs < tot_msgs){
    if(my_rank == 0){ // _> other_rank = 1
      // mando il numero
      MPI_Send(&num_msgs, 1, MPI_INT, other_rank, 0, MPI_COMM_WORLD);
      // RICEVO IL MESSAGGIO
      MPI_Recv(&num_msgs, 1, MPI_INT, other_rank, 0, MPI_COMM_WORLD, MPI_STATUS_IGNORE);

      printf("Messaggio arrivato %d nel processo %d, da %d\n", num_msgs, my_rank, other_rank);

      num_msgs ++;
    }else{ // _> other_rank = 0
      // mando il numero
      MPI_Recv(&num_msgs, 1, MPI_INT, other_rank, 0, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
      // RICEVO IL MESSAGGIO
      MPI_Send(&num_msgs, 1, MPI_INT, other_rank, 0, MPI_COMM_WORLD);

      printf("Messaggio arrivato %d nel processo %d, da %d\n", num_msgs, my_rank, other_rank);
      num_msgs ++;
    }
  }*/


  while (num_msgs < tot_msgs) {
    // GIOCA SUL NUMERO PARI O DISPARI
    if (num_msgs % 2 == my_rank) {
      // INCREMENTA PRIMA
      num_msgs++;
      MPI_Send(&num_msgs, 1, MPI_INT, other_rank, 0, MPI_COMM_WORLD);
      printf("Process %d sent message %d\n", my_rank, num_msgs);
    } else {
      MPI_Recv(&num_msgs, 1, MPI_INT, other_rank, 0, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
      printf("Process %d received message %d\n", my_rank, num_msgs);
    }
  }


  MPI_Finalize();
}
