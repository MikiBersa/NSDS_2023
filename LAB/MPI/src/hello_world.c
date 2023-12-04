#include <mpi.h>
#include <stdio.h>

int main(int argc, char** argv) {
  // Init the MPI environment
  MPI_Init(NULL, NULL);

  // Get the number of processes -> LO RICEVO TRAMITE INDIRIZZAZIONE
  int world_size;
  // MPI_COMM_WORLD -> IL COMMUNICATOR DI TUTTI
  // IL WORLD PER ESEMPIO SI HA QUANDO SI FANNO ESEGUIRE PROGRAMMI IN MODO PARALLELO SU MACCHINE DIFFERENTI
  // QUINDI OGNUNA  HA IL SUO COMUNICATORE = PROCESSO
  MPI_Comm_size(MPI_COMM_WORLD, &world_size);

  // Get the rank of the process -> VIENE ESEGUITO DA TUTTI I PROCESSI
  int world_rank;
  MPI_Comm_rank(MPI_COMM_WORLD, &world_rank);

  // Get the name of the processor -> lo ricevo dentro processor_name
  // NOME DELLA MACCHINA IN CUI SI TROVA / PROCESSO -> che è il comunicatore di quel gruppo di processi
  char processor_name[MPI_MAX_PROCESSOR_NAME];
  int name_len;
  MPI_Get_processor_name(processor_name, &name_len);

  // Print off a hello world message
  printf("Hello world from processor %s %d (rank %d out of %d)\n", processor_name, name_len,world_rank, world_size);

  // Finalize the MPI environment
  MPI_Finalize();
}

/*
COME COMPILARE
mpicc src/helloworld.c -o hello (name del file oggetto)
mpirun -np # ./hello

# con # => numero di core da utilizzare per questa esecuzione

-> IN QUESTO CASO ESEGUE 6 PROCESSI == NUMERO DEI CORE
Hello world from processor MBP-di-Michele.fritz.box (rank 0 out of 6)s


FALLO PARTIRE IN MANIERA DISTRIBUITA
-> STESSA VERSIONE MPI E STESSE ESECUZIONE PER FUZNIONARE IN MODO DISTRIBUTIVO
*/