#include <mpi.h>
#include <stdio.h>

// Every process p>0 waits for an interger from process p=0 that never arrives.
// Try to check the use of resources.

// GESTIAMO IL BLOCCO DEL RICEVIMENTO DEL MESSAGGIO
int main(int argc, char** argv) {
  MPI_Init(NULL, NULL);

  int my_rank;
  MPI_Comm_rank(MPI_COMM_WORLD, &my_rank);

  if (my_rank > 0) {
    int buf;
    // BLOCCAGGIO DEL RICEVIMENTO
    // MESSAGGIO DEVE ARRIVARE DA PROCESSO 0 (CHE è IL SENDER)
    // CONSIDERIAMO IL WORLD COMUNICATOR

    // QUANDO ASPETTA UTILIZZA TUTTA LA MIA CPU => BUSY WAIT CONTINUA A CHIEDERE NUOVI BYTE DAL NETWORK
    MPI_Recv(&buf, 1, MPI_INT, 0, MPI_ANY_TAG, MPI_COMM_WORLD, MPI_STATUS_IGNORE);

    // RICORDO OGNI RANK è UN NUMERO IDENTIFICATIVO UNICO TRA I PROCESSI
    // DOMANDA SE METTO # DI PROCESSO CHE NON APPARTIRE A QUEL COMUNICATORE COME FUNZIONA?
  }

  MPI_Finalize();
}
