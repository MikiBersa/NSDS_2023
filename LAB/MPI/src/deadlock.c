#include <mpi.h>
#include <stdio.h>

// Run with two processes.
// Process 0 sends an integer to process 1 and vice-versa.
// Try to run the system: what goes wrong?
int main(int argc, char** argv) {
  MPI_Init(NULL, NULL);

  int my_rank;
  MPI_Comm_rank(MPI_COMM_WORLD, &my_rank);
  int other_rank = 1 - my_rank;

  // MESSAGGIO DA MANDARE -> PASSO IL RIFERIMENTO DI DOVE SI TROVA IL SUO VALORE
  int msg_to_send = 1;
  // BUFFER DOVE METTERE IL MESSAGGIO RICEVUTO
  int msg_to_recv;

  // Ssend => blocking, syncronous
  MPI_Ssend(&msg_to_send, 1, MPI_INT, other_rank, 0, MPI_COMM_WORLD);
  MPI_Recv(&msg_to_recv, 1, MPI_INT, other_rank, MPI_ANY_TAG, MPI_COMM_WORLD, MPI_STATUS_IGNORE);

  // si aspettano tra di loro
  MPI_Finalize();
}

// le soluzioni stanno nel branch solutions
