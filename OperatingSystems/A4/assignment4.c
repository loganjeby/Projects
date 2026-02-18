#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <limits.h>
 
#define CYLINDERS 300
#define NUM_REQUESTS 20

int compare(const void *a, const void *b);
void fcfs(int start, int requests[], int n);
void sstf(int start, int requests[], int n);
void scan(int start, int sorted[], int n, char *direction);
void cscan(int start, int sorted[], int n, char *direction);
void look(int start, int sorted[], int n, char *direction);
void clook(int start, int sorted[], int n, char *direction);

int main(int argc, char *argv[]) {
    if (argc != 3) {
        fprintf(stderr, "Usage: %s <initial_head_position> <LEFT|RIGHT>\n", argv[0]);
        exit(EXIT_FAILURE);
    }
    
    int head = atoi(argv[1]);
    char direction[6];
    strcpy(direction, argv[2]);
        
    // Read disk requests from the binary file "requests.bin"
    FILE *fp = fopen("request.bin", "rb");
    
    int requests[NUM_REQUESTS];
    size_t itemsRead = fread(requests, sizeof(int), NUM_REQUESTS, fp);
    fclose(fp);
    
    // Create a sorted copy of the requests for algorithms that require sorting
    int sorted[NUM_REQUESTS];
    int i;
    for (i = 0; i < NUM_REQUESTS; i++) {
        sorted[i] = requests[i];
    }
    qsort(sorted, NUM_REQUESTS, sizeof(int), compare);
    // Quick Sort from stdlib.h
    
    // Display initial configuration
    printf("Total Requests = %d\n", NUM_REQUESTS);
    printf("Initial Head Position: %d\n", head);
    printf("Direction of Head: %s\n\n", direction);
    
    // Run and output the algorithms
    fcfs(head, requests, NUM_REQUESTS);
    sstf(head, requests, NUM_REQUESTS);
    scan(head, sorted, NUM_REQUESTS, direction);
    cscan(head, sorted, NUM_REQUESTS, direction);
    look(head, sorted, NUM_REQUESTS, direction);
    clook(head, sorted, NUM_REQUESTS, direction);
    
    return 0;
}

/* Compare function for qsort */
int compare(const void *a, const void *b) {
    return (*(int*)a - *(int*)b);
}
 
/* FCFS Scheduling */
void fcfs(int start, int requests[], int n) {
    int current = start, total = 0;
    printf("FCFS DISK SCHEDULING ALGORITHM:\n\n");
    int i;
    for (i = 0; i < n; i++) {
        printf("%d, ", requests[i]);
        total += abs(requests[i] - current);
        current = requests[i];
    }
    printf("\n\nFCFS - Total head movements = %d\n\n", total);
}

/* SSTF Scheduling */
void sstf(int start, int requests[], int n) {
    int visited[NUM_REQUESTS] = {0};
    int current = start;
    int total = 0;
    printf("SSTF DISK SCHEDULING ALGORITHM\n\n");
    int i;
    int j;
    for (i = 0; i < n; i++) {
        int minDist = INT_MAX, index = -1;
        for (j = 0; j < n; j++) {
            if (!visited[j]) {
                int dist = abs(requests[j] - current);
                if (dist < minDist) {
                    minDist = dist;
                    index = j;
                }
            }
        }
        visited[index] = 1;
        total += minDist;
        current = requests[index];
        printf("%d, ", current);
    }
    printf("\n\nSSTF - Total head movements = %d\n\n", total);
}

/* SCAN Scheduling */
void scan(int start, int sorted[], int n, char *direction) {
    int total = 0;
    int current = start;
    printf("SCAN DISK SCHEDULING ALGORITHM \n\n");
    if (strcmp(direction, "LEFT") == 0) {
        // Find the last index with request <= start
        int idx = -1;
        int i;
        for (i = 0; i < n; i++) {
            if (sorted[i] <= start)
                idx = i;
        }
        // Service left side (in descending order)
        for (i = idx; i >= 0; i--) {
            printf("%d ", sorted[i]);
            total += abs(current - sorted[i]);
            current = sorted[i];
        }
        // Move head to cylinder 0 if not already there
        if (current != 0) {
            total += current; // since current is >0
            current = 0;
        }
        // Service the right side (in ascending order)
        for (i = idx + 1; i < n; i++) {
            printf("%d ", sorted[i]);
            total += abs(current - sorted[i]);
            current = sorted[i];
        }
    } else {  // direction RIGHT
        // Find first index with request >= start
        int idx = -1;
        int i;
        for (i = 0; i < n; i++) {
            if (sorted[i] >= start) {
                idx = i;
                break;
            }
        }
        // Service right side (in ascending order)
        for (i = idx; i < n; i++) {
            printf("%d ", sorted[i]);
            total += abs(sorted[i] - current);
            current = sorted[i];
        }
        // Move head to the last cylinder if not already there
        if (current != CYLINDERS - 1) {
            total += (CYLINDERS - 1 - current);
            current = CYLINDERS - 1;
        }
        // Service left side (in descending order)
        for (i = idx - 1; i >= 0; i--) {
            printf("%d ", sorted[i]);
            total += abs(current - sorted[i]);
            current = sorted[i];
        }
    }
    printf("\n\nSCAN - Total head movements = %d\n\n", total);
}

/* C-SCAN Scheduling */
void cscan(int start, int sorted[], int n, char *direction) {
    int total = 0;
    int current = start;
    printf("C-SCAN DISK SCHEDULING ALGORITHM\n\n");
    if (strcmp(direction, "LEFT") == 0) {
        // For LEFT, service left side (in descending order)
        int idx = -1;
        int i;
        for (i = 0; i < n; i++) {
            if (sorted[i] <= start)
                idx = i;
        }
        for (i = idx; i >= 0; i--) {
            printf("%d, ", sorted[i]);
            total += abs(current - sorted[i]);
            current = sorted[i];
        }
        // Jump to the highest cylinder
        total += current; // Jump to 0
        current = CYLINDERS - 1; // Move to the highest cylinder
        total += CYLINDERS - 1; // Jump from 0 to CYLINDERS - 1
        // Then service the right side (in descending order)
        for (i = n - 1; i > idx; i--) {
            printf("%d", sorted[i]);
            if (i > idx + 1) printf(", ");
            total += abs(current - sorted[i]);
            current = sorted[i];
        }
    } else {  // direction RIGHT
        // For RIGHT, service right side (in ascending order)
        int idx = -1;
        int i;
        for (i = 0; i < n; i++) {
            if (sorted[i] >= start) {
                idx = i;
                break;
            }
        }
        for (i = idx; i < n; i++) {
            printf("%d, ", sorted[i]);
            total += abs(sorted[i] - current);
            current = sorted[i];
        }
        // Jump to the first cylinder
        total += (CYLINDERS - 1 - current); // Jump to CYLINDERS - 1
        current = 0; // Move to the lowest cylinder
        total += CYLINDERS - 1; // Jump from CYLINDERS - 1 to 0
        // Then service the left side (in ascending order)
        for (i = 0; i < idx; i++) {
            printf("%d", sorted[i]);
            if (i < idx - 1) printf(", ");
            total += abs(current - sorted[i]);
            current = sorted[i];
        }
    }
    printf("\n\nC-SCAN - Total head movements = %d\n\n", total);
}
 
/* LOOK Scheduling */
void look(int start, int sorted[], int n, char *direction) {
    int total = 0;
    int current = start;
    printf("LOOK DISK SCHEDULING ALGORITHM  ");
    if (strcmp(direction, "LEFT") == 0) {
        int idx = -1;
        int i;
        for (i = 0; i < n; i++) {
            if (sorted[i] <= start)
                idx = i;
        }
        // Service left side (only as far as the first request, not going to 0)
        for (i = idx; i >= 0; i--) {
            printf("%d ", sorted[i]);
            total += abs(current - sorted[i]);
            current = sorted[i];
        }
        // Then service right side (in ascending order)
        for (i = idx + 1; i < n; i++) {
            printf("%d ", sorted[i]);
            total += abs(current - sorted[i]);
            current = sorted[i];
        }
    } else {  // direction RIGHT
        int idx = -1;
        int i;
        for (i = 0; i < n; i++) {
            if (sorted[i] >= start) {
                idx = i;
                break;
            }
        }
        // Service right side (only as far as the last request)
        for (i = idx; i < n; i++) {
            printf("%d ", sorted[i]);
            total += abs(sorted[i] - current);
            current = sorted[i];
        }
        // Then service left side (in descending order)
        for (i = idx - 1; i >= 0; i--) {
            printf("%d ", sorted[i]);
            total += abs(current - sorted[i]);
            current = sorted[i];
        }
    }
    printf("\n\nLOOK - Total head movements = %d\n\n", total);
}

/* C-LOOK Scheduling */
void clook(int start, int sorted[], int n, char *direction) {
    int total = 0;
    int current = start;
    printf("C-LOOK DISK SCHEDULING ALGORITHM\n\n");
    if (strcmp(direction, "LEFT") == 0) {
        int idx = -1;
        int i;
        for (i = 0; i < n; i++) {
            if (sorted[i] <= start)
                idx = i;
        }
        // Service left side (in descending order)
        for (i = idx; i >= 0; i--) {
            printf("%d ", sorted[i]);
            total += abs(current - sorted[i]);
            current = sorted[i];
        }
        // Jump to the highest request on the right side (if any)
        if (idx < n - 1) {
            total += abs(current - sorted[n - 1]);
            current = sorted[n - 1];
        }
        // Service the right side (in descending order from the second highest to idx+1)
        for (i = n - 2; i > idx; i--) {
            printf("%d ", sorted[i]);
            total += abs(current - sorted[i]);
            current = sorted[i];
        }
    } else {  // direction RIGHT
        int idx = -1;
        int i;
        for (i = 0; i < n; i++) {
            if (sorted[i] >= start) {
                idx = i;
                break;
            }
        }
        // Service right side (in ascending order)
        for (i = idx; i < n; i++) {
            printf("%d ", sorted[i]);
            total += abs(sorted[i] - current);
            current = sorted[i];
        }
        // Jump to the smallest request on the left side (if any)
        if (idx > 0) {
            total += abs(current - sorted[0]);
            current = sorted[0];
        }
        // Service left side (in ascending order)
        for (i = 1; i < idx; i++) {
            printf("%d ", sorted[i]);
            total += abs(current - sorted[i]);
            current = sorted[i];
        }
    }
    printf("\n\nC-LOOK - Total head movements = %d\n\n", total);
}

