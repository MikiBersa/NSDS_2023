#include <mpi.h>
#include <stdio.h>
#include <stdlib.h>

// Send a message in a ring starting from process 0 and increment by one at each hop.
// When the message gets back to process 0, print the number of hops.

/*
Write a ring communication program
– A set of processes exchange a token
  • Process P0 sends the token to P1
  • Process P1 sends the token to P2
  ...
– The program terminates after a given number of iterations over the ring
*/

// 1 -> 2 -> 3 -> ... -> finale ->  1
int main(int argc, char** argv) {
  MPI_Init(NULL, NULL);

  int my_rank, world_size;
  MPI_Comm_rank(MPI_COMM_WORLD, &my_rank);
  MPI_Comm_size(MPI_COMM_WORLD, &world_size);

  int num_hops = 0;

  // TODO

  if (my_rank == 0) {
    printf("Final number of hops in process %d = %d\n", my_rank, num_hops);
  }

  MPI_Finalize();
}
