#include <stdio.h>
#include <stdlib.h>

typedef struct {
    int page_number;
    int frame_number;
} TLBEntry;

#define TLB_MAX_SIZE 16 // Define the maximum size of the TLB
#define BUFFER_SIZE 10  // Number of characters being read
#define OFFSET_MASK 0xFF // Mask to get the offset (8 bits)
#define PAGES 256        // Total number of pages
#define FRAMES 128       // Total number of frames in physical memory
#define OFFSET_BITS 8    // Number of bits for the offset

int TLB_start = 0; // Circular array start index
int TLB_count = 0; // Number of entries in the TLB

// Function prototypes
TLBEntry search_TLB(int page_number, TLBEntry TLB[], int TLB_size);
void TLB_add(int page_number, int frame_number, TLBEntry TLB[]);
void TLB_update(int page_number, int frame_number, TLBEntry TLB[]);

int main() {
    FILE *fptr = fopen("addresses.txt", "r"); // Open the file

    char buff[BUFFER_SIZE]; // Buffer to store characters
    int logical_address = 0; // Variable to store the logical address

    // Page table and physical memory initialization
    int page_table[PAGES];
    int physical_memory[FRAMES];
    int next_free_frame = 0; // Tracks the next free frame in physical memory
    int i;
    for (i = 0; i < PAGES; i++) {
        page_table[i] = -1; // Initialize all page table entries to -1
    }

    TLBEntry TLB[TLB_MAX_SIZE]; // Initialize the TLB
    for (i = 0; i < TLB_MAX_SIZE; i++) {
        TLB[i].page_number = -1;
        TLB[i].frame_number = -1;
    }

    // Read logical addresses from the file
    while (fgets(buff, BUFFER_SIZE, fptr) != NULL) {
        logical_address = atoi(buff); // Convert the string to an integer

        // Extract page number and offset
        int page_number = (logical_address >> OFFSET_BITS); // Right shift to get the page number
        int page_offset = (logical_address & OFFSET_MASK);  // Mask to get the offset

        // Look up the TLB
        TLBEntry tlb_result = search_TLB(page_number, TLB, TLB_count);
        int frame_number;

        if (tlb_result.page_number != -1) {
            // TLB hit
            frame_number = tlb_result.frame_number;
        } else {
            if (page_table[page_number] != -1) {
                // Page table hit
                frame_number = page_table[page_number];
            } else {
                // Page fault

                if (next_free_frame < FRAMES) {
                    // Allocate a new frame
                    frame_number = next_free_frame;
                    page_table[page_number] = frame_number;
                    next_free_frame++;
                } else {
                    fclose(fptr);
                    return 1;
                }
            }

            // Update the TLB
            TLB_add(page_number, frame_number, TLB);
        }

        // Compute the physical address
        int physical_address = (frame_number << OFFSET_BITS) | page_offset;
        printf("Logical Address: %d -> Physical Address: %d\n", logical_address, physical_address);
    }

    fclose(fptr); // Close the file
    return 0;
}

// Search the TLB for an entry corresponding to a page number
TLBEntry search_TLB(int page_number, TLBEntry TLB[], int TLB_size) {
    int i;
    for (i = 0; i < TLB_size; i++) {
        int index = (TLB_start + i) % TLB_MAX_SIZE;
        if (TLB[index].page_number == page_number) {
            return TLB[index];
        }
    }
    TLBEntry not_found = {-1, -1};
    return not_found;
}

// Add an entry to the TLB using FIFO replacement policy
void TLB_add(int page_number, int frame_number, TLBEntry TLB[]) {
    TLBEntry new_entry = {page_number, frame_number};

    if (TLB_count < TLB_MAX_SIZE) {
        // Add to the next available slot
        TLB[(TLB_start + TLB_count) % TLB_MAX_SIZE] = new_entry;
        TLB_count++;
    } else {
        // Replace the oldest entry (FIFO)
        TLB[TLB_start] = new_entry;
        TLB_start = (TLB_start + 1) % TLB_MAX_SIZE; // Move the start index
    }
}

// Update the TLB when a page is replaced in physical memory
void TLB_update(int page_number, int frame_number, TLBEntry TLB[]) {
    int i;
    for (i = 0; i < TLB_count; i++) {
        int index = (TLB_start + i) % TLB_MAX_SIZE;
        if (TLB[index].page_number == page_number) {
            // Update the entry in place
            TLB[index].frame_number = frame_number;
            return;
        }
    }
    // If not found, add the new entry
    TLB_add(page_number, frame_number, TLB);
}

