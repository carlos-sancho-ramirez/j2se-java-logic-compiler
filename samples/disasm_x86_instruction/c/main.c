#include "output.h"
#include <stdio.h>

int main(int argc, char *argv[]) {
    struct Result result;
    struct Array array;
    unsigned char *arrayValues[256];
    struct Array code;
    unsigned char rawCode[] = { 0x06, 0x00, 0x00, 0x00 };
    unsigned char *codeValues[sizeof(rawCode)];

    array.values = (void **) arrayValues;
    for (int i = 0; i < sizeof(rawCode); i++) {
        codeValues[i] = rawCode + i;
    }

    code.length = sizeof(rawCode);
    code.values = (void **) codeValues;
    disasmInstruction(&result, &array, &code);

    char rawMessage[1024];
    for (int i = 0; i < result.message->length; i++) {
        rawMessage[i] = *((char *) result.message->values[i]);
    }
    rawMessage[result.message->length] = '\0';

    if (result.valid) {
        printf("Ok: %s\n", rawMessage);
    }
    else {
        printf("Error: %s\n", rawMessage);
    }

    return 0;
}
