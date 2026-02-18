#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <fcntl.h>
#include <sys/mman.h>
#include <string.h>

#define PAGE_SIZE 256
#define FRAME_SIZE 256
#define TLB_SIZE 16
#define PAGE_TABLE_SIZE 256
#define PHYSICAL_MEMORY_SIZE 128
#define LOGICAL_MEMORY_SIZE 65536

// Structure for TLB Entry
typedef struct {
    int page;
    int frame;
} TLBEntry;

// Memory structures
TLBEntry tlb[TLB_SIZE];  // TLB table
int page_table[PAGE_TABLE_SIZE];  // Page Table (-1 means not in memory)
signed char physical_memory[PHYSICAL_MEMORY_SIZE][FRAME_SIZE]; // Simulated RAM
int page_queue[PHYSICAL_MEMORY_SIZE]; // FIFO queue for pages
int tlb_index = 0;
int next_frame = 0;
int queue_index = 0;
int page_faults = 0;
int tlb_hits = 0;
char *backing_store;

// Function to search the TLB
int search_TLB(int page_number) {
    for (int i = 0; i < TLB_SIZE; i++) {
        if (tlb[i].page == page_number) {
            return tlb[i].frame; // TLB hit
        }
    }
    return -1; // TLB miss
}

// Function to add an entry to the TLB using FIFO replacement
void TLB_Add(int replaced_page) {
    for (int i = 0; i < TLB_SIZE; i++) {
        if (tlb[i].page == replaced_page) {
            tlb[i].page = -1; // Invalidate the TLB entry
            break;
        }
    }
}

// Function to update the TLB using FIFO replacement
void TLB_Update(int page_number, int frame_number) {
    tlb[tlb_index].page = page_number;
    tlb[tlb_index].frame = frame_number;
    tlb_index = (tlb_index + 1) % TLB_SIZE;
}

// Function to handle page faults
int handle_page_fault(int page_number) {
    page_faults++;
    int frame_number;

    if (next_frame < PHYSICAL_MEMORY_SIZE) {
        frame_number = next_frame++;
    } else {
        int replaced_page = page_queue[queue_index];
        TLB_Add(replaced_page); // Invalidate the TLB entry for the replaced page
        frame_number = page_table[replaced_page];
        page_table[replaced_page] = -1; // Remove old page
    }

    memcpy(physical_memory[frame_number], &backing_store[page_number * PAGE_SIZE], PAGE_SIZE);
    page_table[page_number] = frame_number;
    page_queue[queue_index] = page_number;
    queue_index = (queue_index + 1) % PHYSICAL_MEMORY_SIZE; // Move FIFO queue forward
    return frame_number;
}

int main() {
    FILE *addr_file = fopen("addresses.txt", "r");
    int backing_store_fd = open("BACKING_STORE.bin", O_RDONLY);
    backing_store = mmap(0, LOGICAL_MEMORY_SIZE, PROT_READ, MAP_PRIVATE, backing_store_fd, 0);

    if (!addr_file || backing_store == MAP_FAILED) {
        perror("Error opening files");
        return 1;
    }

    for (int i = 0; i < PAGE_TABLE_SIZE; i++) page_table[i] = -1;
    for (int i = 0; i < TLB_SIZE; i++) tlb[i].page = -1; // Initialize TLB

    char buffer[10];
    while (fgets(buffer, sizeof(buffer), addr_file)) {
        int logical_address = atoi(buffer);
        int page_number = (logical_address >> 8) & 0xFF;
        int offset = logical_address & 0xFF;

        int frame_number = search_TLB(page_number);
        if (frame_number != -1) {
            tlb_hits++;
        } else {
            if (page_table[page_number] == -1) {
                frame_number = handle_page_fault(page_number);
            } else {
                frame_number = page_table[page_number];
            }
            TLB_Update(page_number, frame_number);
        }

        int physical_address = (frame_number << 8) | offset;
        signed char value = physical_memory[frame_number][offset];

        printf("Logical Addr: %d -> Physical Addr: %d -> Value: %d\n", logical_address, physical_address, value);
    }

    printf("Total Page Faults: %d\n", page_faults);
    printf("Total TLB Hits: %d\n", tlb_hits);

    fclose(addr_file);
    close(backing_store_fd);
    munmap(backing_store, LOGICAL_MEMORY_SIZE); // Clean up memory mapping
    return 0;
}
