#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <mpi.h>

const int num_iter_per_proc = 10 * 1000 * 1000 * 10;

int isInside(double x, double y){
  return (x*x + y*y) <= 1;
}

int main() {
  MPI_Init(NULL, NULL);

  int rank;
  int num_procs;
  int sum;

  int sum_parz;

  MPI_Comm_rank(MPI_COMM_WORLD, &rank);
  MPI_Comm_size(MPI_COMM_WORLD, &num_procs);

  srand(time(NULL) + rank);

  // DA NOTARE UTILIZZARE UNA VARIABILE LOCALE PER OGNI SUM INTERNA
  for(int i = 0; i < num_iter_per_proc; i++){
    double x = (double) rand() / RAND_MAX;
    double y = (double) rand() / RAND_MAX;
    // MANDARE LA SOMMA AL MASTER
    sum_parz += isInside(x,y);
  }

  // QUI FACCIO UNA RIDUZIONE SU UN  ARRAY DI GRNADEZZA 1
  // TUTTI DEVONO FARE LA REDUCE ANCHE
  MPI_Reduce(&sum_parz, &sum, 1, MPI_INT, MPI_SUM, 0, MPI_COMM_WORLD);


  if (rank == 0) {
    double pi = (4.0*sum) / (num_iter_per_proc*num_procs);
    printf("Pi = %f\n", pi);
  }

  MPI_Finalize();
  return 0;
}
