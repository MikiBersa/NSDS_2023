#include <mpi.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>

int rank;
int num_procs;

const int num_rounds = 10;

const int min_num = 1;
const int max_num = 1000;

// Array, one element per process
// The leader board, instantiated and used in process 0
int *leaderboard = NULL;

// Array, one element per process
// The array of number selected in the current round
int *selected_numbers = NULL;

// The leader for the current round
int leader = 0;

// Allocate dynamic variables
void allocate_vars() {
  // OCCHIO AI SEGMENTI
  leaderboard = (int*) malloc(num_procs * sizeof(int));
  selected_numbers = (int*) malloc(num_procs * sizeof(int));

  for(int  i  = 0; i<num_procs; i++){
    leaderboard[i
    ] = 0;
    selected_numbers[i
    ] = 0;
  }
}
// Deallocate dynamic variables
void free_vars() {
  free(leaderboard);
  free(selected_numbers);
}
// Select a random number between min_num and max_num
int select_number() {
  return min_num + rand() % (max_num - min_num + 1);
}
// Function used to communicate the selected number to the leader
void send_num_to_leader(int num) {
  /*
   MPI_Send(&num,
  1, MPI_INT, leader,
  0, MPI_COMM_WORLD);
  */
  MPI_Gather(&num, 1, MPI_INT, selected_numbers, 1, MPI_INT, leader, MPI_COMM_WORLD);
}
// Compute the winner (-1 if there is no winner)
// Function invoked by the leader only
int compute_winner(int number_to_guess) {
  int prec = -1;
  int min  = 3000;
  int pos_winner  = -1;


  for(int i = 0;  i < num_procs; i++){
    /*
    int num_pos = 0;
    // SAREBBE MEGLIO IL PROB PERCHè COSì NON DEVO ASPETTARE CHE IL PROCESSO SIA PRNTO AD INVIARE
    // MEGLIO GHATER
    MPI_Recv(&num_pos,
    1, MPI_INT, i,
    0, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
    */
    if(i == leader) continue;

    selected_numbers[i] = abs(number_to_guess - selected_numbers[i]);
  }


  for(int i = 0 ; i < num_procs;i++){
    if(i == leader) continue;
    if(prec == selected_numbers[i]) return -1;

    if(selected_numbers[i] < min){
      min = selected_numbers[i];
      pos_winner = i;
    }

    prec = selected_numbers[i
    ];
  }

  return pos_winner;
}
// Function used to communicate the winner to everybody
void send_winner(int *winner) {
  MPI_Bcast(
    winner,
  1,
    MPI_INT,
    leader,
    MPI_COMM_WORLD);
  // printf("Winner %d dal processo  %d\n", *winner, rank);
}
// Update leader
void update_leader(int winner) {
  leader = winner;
}
// Update leaderboard (invoked by process 0 only)
void update_leaderboard(int winner) {
  // AGGIORNO ARRAY DEL VINCITORE
  leaderboard[winner
  ] ++;
}
// Print the leaderboard
void print_leaderboard(int round, int winner) {
  printf("\n* Round %d *\n", round);
  printf("Winner: %d\n", winner);
  printf("Leaderboard\n");
  for (int i=0; i<num_procs; i++) {
    printf("P%d:\t%d\n", i, leaderboard[i
    ]);
  }
}

int main(int argc, char** argv) {
  MPI_Init(NULL, NULL);
  MPI_Comm_rank(MPI_COMM_WORLD, &rank);
  MPI_Comm_size(MPI_COMM_WORLD, &num_procs);
  srand(time(NULL) + rank);

  allocate_vars();

  for (int round=0; round<num_rounds; round++) {
    int selected_number = select_number();
    send_num_to_leader(selected_number);

    int winner;
    if (rank == leader) {
      int num_to_guess = select_number();
      winner = compute_winner(num_to_guess);
    }

    if(winner != -1){
      send_winner(&winner);
      // ce l'ho il winner in winner
      update_leader(winner);
    }

    if (rank == 0) {
      update_leaderboard(winner);
      print_leaderboard(round, winner);
    }
  }

  MPI_Barrier(MPI_COMM_WORLD);
  free_vars();

  MPI_Finalize();
}

/*
GUARDARE LA SOLUZIONE -> rilasciata dal prof
scrivere le metologie di invio e riscezione in broadcast e fare le combinazioni
tra di loro
*/
