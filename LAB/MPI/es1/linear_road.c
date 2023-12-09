#include <mpi.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>

#define DEBUG  1

const int num_segments = 256;

const int num_iterations = 100;
const int count_every = 10;

const double alpha = 0.5;
const int max_in_per_sec = 10;

// Returns the number of car that enter the first segment at a given iteration.
int create_random_input() {
#if DEBUG
  return 1;
#else
  return rand() % max_in_per_sec;
#endif
}

// Returns 1 if a car needs to move to the next segment at a given iteration, 0 otherwise.
int move_next_segment() {
#if DEBUG
  return 1;
#else
  return rand() / RAND_MAX < alpha ? 1 : 0;
#endif
}

int main(int argc, char** argv) {
  MPI_Init(NULL, NULL);

  int rank;
  int num_procs;
  MPI_Comm_rank(MPI_COMM_WORLD, &rank);
  MPI_Comm_size(MPI_COMM_WORLD, &num_procs);
  srand(time(NULL) + rank);

  const int default_tag = 0;

  const int segments_per_proc = num_segments / num_procs;

  int *segments = (int*) malloc(segments_per_proc * sizeof(int));
  memset((void *) segments, 0, segments_per_proc * sizeof(int));

  // Within each iteration, we move backward, from the last segment to the first
  for (int it = 1; it <= num_iterations; ++it) {
    // Last segment
    int num_car_exiting = 0;
    for (int car = 0; car < segments[segments_per_proc-1]; car++) {
      if (move_next_segment()) {
	      num_car_exiting++;
      }
    }
    segments[segments_per_proc-1] -= num_car_exiting;
    if (rank < num_procs-1) {
      MPI_Send(&num_car_exiting, 1, MPI_INT, rank+1, default_tag, MPI_COMM_WORLD);
    }

    // Intermediate segments
    for (int seg = segments_per_proc-2; seg >= 0; seg--) {
      // OCCHIO ALL'ORDINE DI COME LI TOLGO
      int num_cars_to_move = 0;
      for (int car = 0; car < segments[seg]; car++) {
	      if (move_next_segment()) {
	        num_cars_to_move++;
	      }
      }
      segments[seg] -= num_cars_to_move;
      segments[seg+1] += num_cars_to_move;
    }

    // Initial segment
    if (rank == 0) {
      segments[0] += create_random_input();
    } else {
      int received = 0;
      MPI_Recv(&received, 1, MPI_INT, rank-1, default_tag, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
      segments[0] += received;
    }

    // When needed, compute the overall sum
    if (it%count_every == 0) {
      int local_sum = 0;
      for (int seg = 0; seg < segments_per_proc; seg++) {
      	local_sum += segments[seg];
      }
      int global_sum = 0;
      MPI_Reduce(&local_sum, &global_sum, 1, MPI_INT, MPI_SUM, 0, MPI_COMM_WORLD);
      if (rank == 0) {
	      printf("Iteration: %d, sum: %d\n", it, global_sum);
      }
    }

    MPI_Barrier(MPI_COMM_WORLD);
  }

  free(segments);
  MPI_Barrier(MPI_COMM_WORLD);
  MPI_Finalize();
}
