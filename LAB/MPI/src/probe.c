#include <mpi.h>
#include <stdio.h>
#include <stdlib.h>

// The receiver does not know in advance the number of messages it will receive (and the source sending them).
// It dynamically gets the number of messages using a probe.
int main(int argc, char** argv) {
  MPI_Init(NULL, NULL);

  int my_rank;
  MPI_Comm_rank(MPI_COMM_WORLD, &my_rank);
  // FUNZIONA QUANDO HO SOLO 2 CORE
  int other_rank = 1 - my_rank;

  // Sender
  if (my_rank == 0) {
    int num_msgs = 10;
    // CREAZIONE DI UN ARRAY DINAMICO
    int *msgs = (int *) malloc(sizeof(int) * num_msgs);
    for (int i=0; i<num_msgs; i++) {
      msgs[i] = i;
    }
    // INVIO IL BUFFER DEL MESSAGGIO
    MPI_Send(msgs, num_msgs, MPI_INT, other_rank, 0, MPI_COMM_WORLD);
    // viene eseguito quando il messaggio è stato totalmente inviato, NON è DETTO CHE SIA ARRIVATO ALL'ALTRO PROCESSO
    // MA POI POSSO FARCI QUELLO CHE VOGLIO IN QUEL BUFFER
    free(msgs);
  }
  // Receiver
  else {
    // TODO: check the message size and receive

    // USATO PER ESTRARRE INFOMAZIONI:
    // -> SU CHI HA INVIATO IL MESSAGGIO
    // -> SIZE OF THE MESSAGGIO

    // Probe is a blocking operation that checks the number of messages in the receiver buffer
    MPI_Status status;
    // STO ASPETTANDO UN MESSAGGIO => QUANDO C'è UN MESSAGGIO ARRVIATO IL PROB RITORNA E METTE IN STATUS LA RISPOSTA
    // INFORMAZIONI DEL MESSAGGIO
    MPI_Probe(MPI_ANY_SOURCE, MPI_ANY_TAG, MPI_COMM_WORLD, &status);

    int num_msgs;
    // SIZE OF THE MESSAGE PER LEGGERE LO STATUS
    MPI_Get_count(&status, MPI_INT, &num_msgs);
    int source = status.MPI_SOURCE;
    int tag = status.MPI_TAG;
    printf("Process %d received %d messages from source %d with tag %d\n", my_rank, num_msgs, source, tag);

    // sapendo quando è grande il messaggio posso fare un'allocazione
    int *msgs = (int *) malloc(sizeof(int) * num_msgs);
    // ORA POSSO LEGGERE IL MESSAGIO ARRIVATO IN QUEL BUFFER -> ED ESTRAGGO IL MESSAGGIO
    // MA NEL MENTRE DOVE è STATO MESSO IL MESSAGGIO ? => SE NON LìHO ANCORA RITARATO MA MI è ARRIVATA NOTIFICA DI UN NUOVO MESSAGGIO
    MPI_Recv(msgs, num_msgs, MPI_INT, source, tag, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
    for (int i=0; i<num_msgs; i++) {
      printf("%d\t", msgs[i]);
    }
    printf("\n");
    free(msgs);
  }

  MPI_Finalize();
}
