#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <mpi.h>
#include <math.h>

/*
 * Group number: 13
 *
 * Group members
 *  - Manuela Marenghi
 *  - Michele Bersani
 *  - Paolo Dalle Rive
 */

const float min = 0;
const float max = 1000;
const float len = max - min;
const int num_ants = 8 * 1000 * 1000;
const int num_food_sources = 10;
const int num_iterations = 500;

float random_position() {
  return (float) rand() / (float)(RAND_MAX/(max-min)) + min;
}

/*
 * Process 0 invokes this function to initialize food sources.
 */

void init_food_sources(float* food_sources) {
  for (int i=0; i<num_food_sources; i++) {
    food_sources[i] = random_position();
  }
}

/*
 * Process 0 invokes this function to initialize the position of ants.
 */
void init_ants(float* ants) {
  for (int i=0; i<num_ants; i++) {
    ants[i] = random_position();
  }
}

/*
 * CALCOLO DELLA MEDIA
 */
float average_center(float* ants, int dim){
    float somma = 0;
    float center = 0;

    for(int i = 0; i < dim ; i++){
        somma += ants[i];
    }

    center = somma / dim;

    return center;
}


int main() {
  MPI_Init(NULL, NULL);
    
  int rank;
  int num_procs;
  MPI_Comm_rank(MPI_COMM_WORLD, &rank);
  MPI_Comm_size(MPI_COMM_WORLD, &num_procs);

  //srand((unsigned int)time(NULL));
  srand(rank);

  // Allocate space in each process for food sources and ants
    float* food_sources = (float*) malloc(sizeof(float )* num_food_sources );
    int ant_per_proc = num_ants /num_procs;
    float* ants = NULL;
    float* ants_process = (float *) calloc(ant_per_proc,sizeof(float ));

  // Process 0 initializes food sources and ants

  // printf("init array allocati 81\n");
  if( rank == 0){
      ants = (float *) calloc(num_ants,sizeof(float ));
      init_food_sources(food_sources);
      init_ants(ants);

  }

    // printf("init fatti 87\n");
    // Process 0 distributed food sources and ants
    MPI_Bcast(food_sources, num_food_sources, MPI_FLOAT, 0, MPI_COMM_WORLD);
    // printf("broadcast fatti 90\n");
    MPI_Scatter(ants, ant_per_proc, MPI_FLOAT, ants_process, ant_per_proc, MPI_FLOAT, 0, MPI_COMM_WORLD);
    // printf("scatter fatt0 92\n");

  // Iterative simulation
  float center = 0;

  // centro iniziale
  if(rank == 0){
      center = average_center(ants, num_ants);
      // printf("AVG %f\n", center);
  }

  MPI_Bcast(&center, 1, MPI_FLOAT, 0, MPI_COMM_WORLD);

  for (int iter=0; iter<num_iterations; iter++) {
      float center_partial = 0;

      for (int ant = 0; ant < ant_per_proc; ant++) {

          float min_food_distance = max; // Initialize to a large value
          int nearest_food_index;

          for (int j = 0; j < num_food_sources; j++) {
              float dist_to_food = fabs(ants_process[ant] - food_sources[j]);
              if (dist_to_food < min_food_distance) {
                  min_food_distance = dist_to_food;
                  nearest_food_index = j;
              }
          }

          float dist_to_center = fabs(ants_process[ant] - center);

          // Compute forces F1 and F2
          float force_f1 = 0.01 * min_food_distance * ((ants_process[ant] < food_sources[nearest_food_index]) ? 1 : -1);
          float force_f2 = 0.012 * dist_to_center * ((ants_process[ant] < center) ? 1 : -1);

          // new position della formica
          ants_process[ant] += force_f1 + force_f2;
          if(ants_process[ant] > max) ants_process[ant] = max;
          if(ants_process[ant] < min) ants_process[ant] = min;
      }

      // calcolo della media
      center_partial = average_center(ants_process, ant_per_proc);


      float *gather_buffer = NULL;
      if (rank == 0) {
          gather_buffer = (float *) malloc(sizeof(float) * num_procs);
      }
      // invio della media al P0
      MPI_Gather(&center_partial, 1, MPI_FLOAT, gather_buffer, 1, MPI_FLOAT, 0, MPI_COMM_WORLD);

    // Compute and print the center of the colony (average position)
    if (rank == 0) {
         // for(int i = 0; i < num_procs; i++) printf("Media %f del processo %d\n", gather_buffer[i], i);
         center = average_center(gather_buffer, num_procs);
      printf("Iteration: %d - Average position: %f\n", iter, center);
      free(gather_buffer);
    }

    //invio del centro nuovo -> fa anche da barrier
    MPI_Bcast(&center, 1, MPI_FLOAT, 0, MPI_COMM_WORLD);
  }

  // Free memory
  free(food_sources);
  if(rank == 0){
      free(ants);
  }
  free(ants_process);
  
  MPI_Finalize();
  return 0;
}