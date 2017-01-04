#include <stdio.h>
#include <stdlib.h>
#include <string.h>

char wave[5];
char data[5];
char riff[5];
char fmt[5];

const int NUM_TESTS = 13;

const int SIZE_OF_HEADER = 44;
const int SAMPLE_RATE = 44100;
const int SUBCHUNK_SIZE = 16;
const int BIT_RATE = 16;
const int NUM_CHANNELS = 1;
const int BYTE_SIZE = 8;
//(SAMPLE_RATE * NUM_CHANNELS * BIT_RATE) / BYTE_SIZE == 88200
const int BYTE_RATE = 88200;
//(NUM_CHANNELS * SAMPLE_RATE) / BYTE_SIZE = 2
const int BLOCK_ALIGN = 2;

const int RIFF_LOC = 0;
const int FILE_SIZE_LOC = 4;
const int WAVE_LOC = 8;
const int FMT_LOC = 12;
const int SUBCHUNK_LOC = 16;
const int PCM_LOC = 20;
const int NUM_CHANNELS_LOC = 22;
const int SAMPLE_RATE_LOC = 24;
const int BYTE_RATE_LOC = 28;
const int BLOCK_ALIGN_LOC = 32;
const int BIT_RATE_LOC = 34;
const int DATA_LOC = 36;
const int DATA_SIZE_LOC = 40;


int le_to_d(unsigned char header[], int loc, int n);
long long_le_to_d(unsigned char header[], int loc, int n);
void verify_header(unsigned char header[], long size);
int verify_size(unsigned char header[], long size);
int verify_constants(unsigned char header[]);
int verify_string(unsigned char header[], char string[], int loc);
int compare_header_text(unsigned char header[], char expected[], int loc);
void print_wave_header(unsigned char header[]);

int main(int argc, char* argv[]){

    if(argc < 2){
        printf("ERROR: Filename needed as argument");
        return -1;
    }

    unsigned char header[SIZE_OF_HEADER];
    FILE * wavfile;
    long size;
    wavfile = fopen(argv[1], "r");

    strcpy(wave, "WAVE");
    strcpy(data, "data");
    strcpy(riff, "RIFF");
    strcpy(fmt, "fmt ");

    for(int i = 0; i < SIZE_OF_HEADER; i++){
        fscanf(wavfile, "%c", &header[i]);
    }

    fseek(wavfile, 0, SEEK_END);
    size = ftell(wavfile);
    fclose(wavfile);

    verify_header(header, size);

    return 0;
}

void verify_header(unsigned char header[], long size){
    int pass = 0;
    pass += verify_string(header, riff, RIFF_LOC);
    pass += verify_string(header, wave, WAVE_LOC);
    pass += verify_string(header, fmt, FMT_LOC);
    pass += verify_string(header, data, DATA_LOC);
    pass += verify_constants(header);
    pass += verify_size(header, size);
    printf("PASS TOTAL: %d\n", pass);
    if(pass == NUM_TESTS){
        printf("PASS: ALL TESTS PASS\n");
    } else {
        printf("FAIL: %d TESTS FAILED\n", NUM_TESTS-pass);
    }
}

int verify_size(unsigned char header[], long size){
    long filesize = long_le_to_d(header, FILE_SIZE_LOC, 4);
    long datasize = long_le_to_d(header, DATA_SIZE_LOC, 4);
    int pass = 0;
    if(filesize != (size-8)){
        printf("FAIL: overall file size - header size = %ld, should be %ld\n", filesize, (size-8));
    } else {
        printf("PASS: overall filesize - header size = %ld\n", filesize);
        pass++;
    }

    if(datasize != size-SIZE_OF_HEADER){
        printf("FAIL: data size = %ld, should be %ld\n", datasize, size-SIZE_OF_HEADER);
    } else {
        printf("PASS: data size = %ld\n", datasize);
        pass++;
    }
    return pass;
}

int verify_string(unsigned char header[], char string[], int loc){
    if(compare_header_text(header, string, loc) == 0){
        printf("PASS: %s found\n", string);
        return 1;
    } else {
        printf("FAIL: %s missing\n", string);
        return 0;
    }
}

int compare_header_text(unsigned char header[], char expected[], int loc){
    char actual[5];
    actual[4] = '\0';
    for(int i = 0; i < 4; i++){
        actual[i] = header[i+loc];
    }
    return strcmp(expected, actual);
}

int verify_constants(unsigned char header[]){
    int pass = 0;
    int subchunk = le_to_d(header, SUBCHUNK_LOC, 4);
    if(subchunk != SUBCHUNK_LOC){
        printf("FAIL: subchunk is: %d, should be set to %d\n", subchunk, SUBCHUNK_SIZE);
    } else {
        printf("PASS: subchunk is set to %d\n", SUBCHUNK_SIZE);
        pass++;
    }

    int format = le_to_d(header, PCM_LOC, 2);
    if(format != 1){
        printf("FAIL: format is: %d, should be set to 1 (PCM)\n", format);
    } else {
        printf("PASS: format set to 1 (PCM)\n");
        pass++;
    }

    int channels = le_to_d(header, NUM_CHANNELS_LOC, 2);
    if(channels != 1){
        printf("FAIL: number of channels is: %d, should be set to 1 (mono)\n", channels);
    } else {
        printf("PASS: channels set to 1 (mono)\n");
        pass++;
    }

    int samplerate = le_to_d(header, SAMPLE_RATE_LOC, 4);
    if(samplerate != SAMPLE_RATE){
        printf("FAIL: sample rate is: %d, should be set to %dkhz\n", samplerate, SAMPLE_RATE);
    } else {
        printf("PASS: sample rate set to %dkhz\n", SAMPLE_RATE);
        pass++;
    }

    int byterate = le_to_d(header, BYTE_RATE_LOC, 4);
    if(byterate != BYTE_RATE){
        printf("FAIL: byte rate: %d should be set to %d\n", byterate, BYTE_RATE);
    } else {
        printf("PASS: byte rate equal to %d\n", BYTE_RATE);
        pass++;
    }

    int blockalign = le_to_d(header, BLOCK_ALIGN_LOC, 2);
    if(blockalign != BLOCK_ALIGN){
        printf("FAIL: block align is: %d, should be set to %d\n", blockalign, BLOCK_ALIGN);
    } else {
        printf("PASS: block align equal to %d\n", BLOCK_ALIGN);
        pass++;
    }

    int bitrate = le_to_d(header, BIT_RATE_LOC, 2);
    if(bitrate != BIT_RATE){
        printf("FAIL: bit rate is: %d, should be set to %d\n", bitrate, BIT_RATE);
    } else {
        printf("PASS: bit rate set to %d\n", BIT_RATE);
        pass++;
    }
    return pass;
}

int le_to_d(unsigned char header[], int loc, int n){
    int sum = 0;
    for(int i = 0; i < n; i++){
        //can just shift in without masking because header is unsigned
        sum |= header[loc+i] << (BYTE_SIZE*i);
    }
    return sum;
}

long long_le_to_d(unsigned char header[], int loc, int n){
    long sum = 0;
    for(int i = 0; i < n; i++){
        //can just shift in without masking because header is unsigned
        sum |= header[loc+i] << (BYTE_SIZE*i);
    }
    return sum;
}
