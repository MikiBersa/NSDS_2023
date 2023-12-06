#include <mpi.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>

/**
 * Group number:
 *
 * Group members
 * Member 1
 * Member 2
 * Member 3
 *
 **/

// Set DEBUG 1 if you want car movement to be deterministic
#define DEBUG 0

const int num_segments = 256;

const int num_iterations = 1000;
const int count_every = 10;

const double alpha = 0.5;
const int max_in_per_sec = 10;

// Returns the number of car that enter the first segment at a given iteration.
int create_random_input() {
#if DEBUG
  return 1;
#elif
  return rand() % max_in_per_sec;
#endif
}

// Returns 1 if a car needs to move to the next segment at a given iteration, 0 otherwise.
int move_next_segment() {
#if DEBUG
  return 1;
#else
  return rand() < alpha ? 1 : 0;
#endif
}

int main(int argc, char** argv) {
  MPI_Init(NULL, NULL);

  int rank;
  int num_procs;
  MPI_Comm_rank(MPI_COMM_WORLD, &rank);
  MPI_Comm_size(MPI_COMM_WORLD, &num_procs);
  srand(time(NULL) + rank);

  // TODO: define and init variables

  // Simulate for num_iterations iterations
  for (int it = 0; it < num_iterations; ++it) {
    // Move cars across segments
    // New cars may enter in the first segment
    // Cars may exit from the last segment

    // When needed, compute the overall sum
    if (it%count_every == 0) {
      int global_sum = 0;

      // TODO compute global sum

      if (rank == 0) {
	printf("Iteration: %d, sum: %d\n", it, global_sum);
      }
    }

    MPI_Barrier(MPI_COMM_WORLD);
  }

  // TODO deallocate dynamic variables, if needed

  MPI_Finalize();
}
