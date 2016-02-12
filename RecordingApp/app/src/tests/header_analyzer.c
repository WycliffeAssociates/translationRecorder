#include <stdio.h>
#include <stdlib.h>

int main(){
    unsigned char header[44];
    FILE * wavfile;
    wavfile = fopen("test.wav", "r");
    for(int i = 0; i < 44; i++){
        fscanf(wavfile, "%c", &header[i]);
    }
    fclose(wavfile);
    for(int i = 0; i < 44; i++){
        printf("%x\n", header[i]);
    }
    return 0;
}