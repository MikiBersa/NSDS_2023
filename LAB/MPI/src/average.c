#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <mpi.h>

// Creates an array of random numbers -> MALLOC COSì SI TROVA NELLO HEAP E CI ACCEDO COME UN ARRAY
int *create_random_array(int num_elements, int max_value) {
  int *arr = (int *) malloc(sizeof(int)*num_elements);
  for (int i=0; i<num_elements; i++) {
    arr[i] = (rand() % max_value);
  }
  return arr;
}

// Computes the average value
float compute_average(int *array, int num_elements) {
  int sum = 0;
  for (int i=0; i<num_elements; i++) {
    sum += array[i];
  }
  return ((float) sum) / num_elements;
}

// Computes final average value
float compute_final_average(float *array, int num_elements) {
  float sum = 0.0f;
  for (int i=0; i<num_elements; i++) {
    sum += array[i];
  }
  return  sum/num_elements;
}

int main(int argc, char** argv) {
  // Number of elements for each processor
  int num_elements_per_proc = 1000;
  if (argc > 1) {
    num_elements_per_proc = atoi(argv[1]);
  }

  // Init random number generator
  srand(time(NULL));

  MPI_Init(NULL, NULL); // -> COSì OGNI PROCESSO RICEVE IL PROPRIO RANK

  int my_rank, world_size;
  MPI_Comm_rank(MPI_COMM_WORLD, &my_rank);
  MPI_Comm_size(MPI_COMM_WORLD, &world_size);

  // Process 0 creates the array
  int *global_arr = NULL;
  if (my_rank == 0) {
    // PROCESSO ROOT 0 -> CREA ARRAY DA MANDARE SPLITTATO
    global_arr = create_random_array(num_elements_per_proc * world_size, 100);
  }

  // SOLUZIONE
  // For each process, create a receive buffer -> TUTTI LO FANNO ANCHE IL ROOT
  int *local_arr = (int *) malloc(sizeof(int) * num_elements_per_proc);

  // Scatter the random numbers from process 0 to all processes
  // con 0 dico quale dei processi deve inviare i dati
  // VIENE FATTA SEND CHE MANDA ANCHE A SE STESSO
  // azione bloccante proseguo se ho ricevuto i dati
  MPI_Scatter(global_arr, num_elements_per_proc, MPI_INT, local_arr, num_elements_per_proc, MPI_INT, 0, MPI_COMM_WORLD);
  // TUTTI I PROCESSI INCLUSO ROOT HANNO LA PARTIZIONE DEI DATI ARRAY

  // Compute the average of local array
  float local_average = compute_average(local_arr, num_elements_per_proc);

  // Gather all partial results in process 0
  float *gather_buffer = NULL;
  if (my_rank == 0) {
    // creare il buffer per ricevere i processi
    gather_buffer = (float *) malloc(sizeof(float) * world_size);
  }
  // parametri riferiti sender -> sende one float che è memorizzato in local_average
  // ricevitore -> riceve un float da ogni processo incluso se stesso
  // 0 -> root di ricezione
  // tutti i float creati li mette in gather_buffer => nel root
  MPI_Gather(&local_average, 1, MPI_FLOAT, gather_buffer, 1, MPI_FLOAT, 0, MPI_COMM_WORLD);

  // dopo che ho ricevuto il messaggio se sono ROOT

    // Compute the final average in process 0
    if (my_rank == 0) {
      float result = compute_final_average(gather_buffer, world_size);
      printf("The average is %f\n", result);

      // Sequential code to check correctness
      float sequential_result = compute_average(global_arr, num_elements_per_proc * world_size);
      printf("The average (sequential computation) is %f\n", sequential_result);
    }

    // Clean up
    if (my_rank == 0) {
      free(global_arr);
      free(gather_buffer);
    }
  free(local_arr);

  MPI_Barrier(MPI_COMM_WORLD);
  MPI_Finalize();
}

