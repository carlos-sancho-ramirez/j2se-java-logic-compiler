#include "output.h"
#include <stdio.h>
#include <string.h>

static unsigned int passedCount = 0;
static unsigned int failureCount = 0;

#define DISASM_INSTRUCTION_RESULT_VALUES_LENGTH 255

#define TEST_FUNC_FIRST(l) \
    struct Result result; \
    struct String string; \
    char stringValues[DISASM_INSTRUCTION_RESULT_VALUES_LENGTH + 1]; \
    string.values = stringValues; \
    \
    unsigned char *codeValues[l];

#define TEST_FUNC_LAST(l) \
    struct Array code; \
    code.length = l; \
    code.values = (void **) codeValues; \
    \
    disasmInstruction(&result, &string, &code); \
    \
    result.message->values[result.message->length] = '\0'; \
    if (result.valid) { \
        if (strcmp(result.message->values, expected)) { \
            printf("  * Failure: Expected '%s' but it was '%s'\n", expected, result.message->values); \
            failureCount++; \
        } \
        else { \
            printf("  * Ok: %s\n", result.message->values); \
            passedCount++; \
        } \
    } \
    else { \
        printf("  * Failure: Expected '%s' but returned error with text '%s'\n", expected, result.message->values); \
        failureCount++; \
    }

void testOk1(unsigned char ch0, const char *expected) {
    TEST_FUNC_FIRST(1)
    codeValues[0] = &ch0;
    TEST_FUNC_LAST(1)
}

void testOk2(unsigned char ch0, unsigned char ch1, const char *expected) {
    TEST_FUNC_FIRST(2)
    codeValues[0] = &ch0;
    codeValues[1] = &ch1;
    TEST_FUNC_LAST(2)
}

void testOk3(unsigned char ch0, unsigned char ch1, unsigned char ch2, const char *expected) {
    TEST_FUNC_FIRST(3)
    codeValues[0] = &ch0;
    codeValues[1] = &ch1;
    codeValues[2] = &ch2;
    TEST_FUNC_LAST(3)
}

void testOk4(unsigned char ch0, unsigned char ch1, unsigned char ch2, unsigned char ch3, const char *expected) {
    TEST_FUNC_FIRST(4)
    codeValues[0] = &ch0;
    codeValues[1] = &ch1;
    codeValues[2] = &ch2;
    codeValues[3] = &ch3;
    TEST_FUNC_LAST(4)
}

int main(int argc, char *argv[]) {
    testOk2(0x00, 0x00, "add [bx + si], al");
    testOk2(0x00, 0x01, "add [bx + di], al");
    testOk2(0x00, 0x02, "add [bp + si], al");
    testOk2(0x00, 0x03, "add [bp + di], al");
    testOk2(0x00, 0x04, "add [si], al");
    testOk2(0x00, 0x05, "add [di], al");
    testOk4(0x00, 0x06, 0x34, 0x12, "add [0x1234], al");
    testOk2(0x00, 0x07, "add [bx], al");
    testOk2(0x00, 0x08, "add [bx + si], cl");
    testOk2(0x00, 0x09, "add [bx + di], cl");
    testOk4(0x00, 0x0E, 0x34, 0x12, "add [0x1234], cl");
    testOk2(0x00, 0x10, "add [bx + si], dl");
    testOk2(0x00, 0x18, "add [bx + si], bl");
    testOk2(0x00, 0x20, "add [bx + si], ah");
    testOk2(0x00, 0x28, "add [bx + si], ch");
    testOk2(0x00, 0x30, "add [bx + si], dh");
    testOk2(0x00, 0x38, "add [bx + si], bh");
    testOk3(0x00, 0x40, 0x67, "add [bx + si + 0x67], al");
    testOk3(0x00, 0x40, 0x80, "add [bx + si - 0x80], al");
    testOk3(0x00, 0x40, 0xFC, "add [bx + si - 0x04], al");
    testOk3(0x00, 0x41, 0x7F, "add [bx + di + 0x7F], al");
    testOk3(0x00, 0x42, 0x83, "add [bp + si - 0x7D], al");
    testOk3(0x00, 0x46, 0xFA, "add [bp - 0x06], al");
    testOk3(0x00, 0x4E, 0x04, "add [bp + 0x04], cl");
    testOk4(0x00, 0x80, 0x67, 0x45, "add [bx + si + 0x4567], al");
    testOk4(0x00, 0x85, 0xFE, 0xFE, "add [di - 0x0102], al");
    testOk4(0x00, 0xAE, 0x34, 0x12, "add [bp + 0x1234], ch");
    testOk2(0x00, 0xC0, "add al, al");
    testOk2(0x00, 0xC1, "add cl, al");
    testOk2(0x00, 0xC7, "add bh, al");
    testOk2(0x00, 0xC8, "add al, cl");
    testOk2(0x00, 0xEB, "add bl, ch");
    testOk2(0x01, 0x00, "add [bx + si], ax");
    testOk2(0x01, 0x01, "add [bx + di], ax");
    testOk4(0x01, 0x06, 0x35, 0x02, "add [0x0235], ax");
    testOk4(0x01, 0x0E, 0x34, 0x12, "add [0x1234], cx");
    testOk2(0x01, 0x0F, "add [bx], cx");
    testOk2(0x01, 0x14, "add [si], dx");
    testOk2(0x01, 0x1D, "add [di], bx");
    testOk2(0x01, 0x23, "add [bp + di], sp");
    testOk2(0x01, 0x2F, "add [bx], bp");
    testOk4(0x01, 0x36, 0x00, 0x01, "add [0x0100], si");
    testOk2(0x01, 0x38, "add [bx + si], di");
    testOk3(0x01, 0x40, 0x67, "add [bx + si + 0x67], ax");
    testOk3(0x01, 0x40, 0x80, "add [bx + si - 0x80], ax");
    testOk3(0x01, 0x49, 0x7F, "add [bx + di + 0x7F], cx");
    testOk3(0x01, 0x56, 0xFA, "add [bp - 0x06], dx");
    testOk3(0x01, 0x76, 0x04, "add [bp + 0x04], si");
    testOk4(0x01, 0x80, 0x67, 0x45, "add [bx + si + 0x4567], ax");
    testOk4(0x01, 0xAE, 0x34, 0x12, "add [bp + 0x1234], bp");
    testOk2(0x01, 0xC0, "add ax, ax");
    testOk2(0x01, 0xC1, "add cx, ax");
    testOk2(0x01, 0xC7, "add di, ax");
    testOk2(0x01, 0xC8, "add ax, cx");
    testOk2(0x01, 0xEB, "add bx, bp");
    testOk2(0x02, 0x00, "add al, [bx + si]");
    testOk2(0x02, 0x14, "add dl, [si]");
    testOk4(0x02, 0x26, 0xEF, 0xC0, "add ah, [0xC0EF]");
    testOk3(0x02, 0x7C, 0x02, "add bh, [si + 0x02]");
    testOk4(0x02, 0x95, 0x38, 0x01, "add dl, [di + 0x0138]");
    testOk2(0x02, 0xCC, "add cl, ah");
    testOk3(0x03, 0x77, 0x56, "add si, [bx + 0x56]");
    testOk2(0x04, 0x55, "add al, 0x55");
    testOk3(0x05, 0xA0, 0x9E, "add ax, 0x9EA0");
    testOk1(0x06, "push es");
    testOk1(0x07, "pop es");
    testOk2(0x08, 0x00, "or [bx + si], al");
    testOk2(0x08, 0x01, "or [bx + di], al");
    testOk2(0x08, 0x02, "or [bp + si], al");
    testOk2(0x08, 0x05, "or [di], al");
    testOk4(0x08, 0x06, 0x23, 0xBC, "or [0xBC23], al");
    testOk2(0x08, 0x0B, "or [bp + di], cl");
    testOk3(0x08, 0x64, 0x8A, "or [si - 0x76], ah");
    testOk4(0x08, 0x9D, 0x44, 0x23, "or [di + 0x2344], bl");
    testOk2(0x08, 0xC1, "or cl, al");
    testOk2(0x09, 0x00, "or [bx + si], ax");
    testOk4(0x09, 0x0E, 0x35, 0x02, "or [0x0235], cx");
    testOk3(0x09, 0x56, 0xFA, "or [bp - 0x06], dx");
    testOk2(0x09, 0xC7, "or di, ax");
    testOk2(0x0A, 0x00, "or al, [bx + si]");
    testOk4(0x0A, 0x26, 0xEF, 0xC0, "or ah, [0xC0EF]");
    testOk2(0x0A, 0xCC, "or cl, ah");
    testOk3(0x0B, 0x77, 0x56, "or si, [bx + 0x56]");
    testOk2(0x0C, 0x95, "or al, 0x95");
    testOk3(0x0D, 0xA0, 0x9E, "or ax, 0x9EA0");
    testOk1(0x0E, "push cs");
    testOk2(0x10, 0x00, "adc [bx + si], al");
    testOk4(0x10, 0x16, 0x23, 0xBC, "adc [0xBC23], dl");
    testOk3(0x10, 0x64, 0x8A, "adc [si - 0x76], ah");
    testOk4(0x10, 0x9D, 0x44, 0x23, "adc [di + 0x2344], bl");
    testOk2(0x10, 0xC1, "adc cl, al");
    testOk2(0x11, 0x00, "adc [bx + si], ax");
    testOk4(0x11, 0x0E, 0x35, 0x02, "adc [0x0235], cx");
    testOk2(0x11, 0xC7, "adc di, ax");
    testOk2(0x12, 0x00, "adc al, [bx + si]");
    testOk3(0x13, 0x77, 0x56, "adc si, [bx + 0x56]");
    testOk2(0x14, 0x95, "adc al, 0x95");
    testOk3(0x15, 0xA0, 0x9E, "adc ax, 0x9EA0");
    testOk1(0x16, "push ss");
    testOk1(0x17, "pop ss");
    testOk2(0x18, 0x00, "sbb [bx + si], al");
    testOk4(0x18, 0x16, 0x23, 0xBC, "sbb [0xBC23], dl");
    testOk3(0x18, 0x64, 0x8A, "sbb [si - 0x76], ah");
    testOk4(0x18, 0x9D, 0x44, 0x23, "sbb [di + 0x2344], bl");
    testOk2(0x18, 0xC1, "sbb cl, al");
    testOk2(0x19, 0x00, "sbb [bx + si], ax");
    testOk4(0x19, 0x0E, 0x35, 0x02, "sbb [0x0235], cx");
    testOk2(0x19, 0xC7, "sbb di, ax");
    testOk2(0x1A, 0x00, "sbb al, [bx + si]");
    testOk3(0x1B, 0x77, 0x56, "sbb si, [bx + 0x56]");
    testOk2(0x1C, 0x95, "sbb al, 0x95");
    testOk3(0x1D, 0xA0, 0x9E, "sbb ax, 0x9EA0");
    testOk1(0x1E, "push ds");
    testOk1(0x1F, "pop ds");
    testOk2(0x20, 0x00, "and [bx + si], al");
    testOk4(0x20, 0x16, 0x23, 0xBC, "and [0xBC23], dl");
    testOk3(0x20, 0x64, 0x8A, "and [si - 0x76], ah");
    testOk4(0x20, 0x9D, 0x44, 0x23, "and [di + 0x2344], bl");
    testOk2(0x20, 0xC1, "and cl, al");
    testOk2(0x21, 0x00, "and [bx + si], ax");
    testOk4(0x21, 0x0E, 0x35, 0x02, "and [0x0235], cx");
    testOk2(0x21, 0xC7, "and di, ax");
    testOk2(0x22, 0x00, "and al, [bx + si]");
    testOk3(0x23, 0x77, 0x56, "and si, [bx + 0x56]");
    testOk2(0x24, 0x95, "and al, 0x95");
    testOk3(0x25, 0xA0, 0x9E, "and ax, 0x9EA0");
    // TODO: Test 0x26 -> es:
    // TODO: Test 0x27 -> daa
    testOk2(0x28, 0x00, "sub [bx + si], al");
    testOk4(0x28, 0x16, 0x23, 0xBC, "sub [0xBC23], dl");
    testOk3(0x28, 0x64, 0x8A, "sub [si - 0x76], ah");
    testOk4(0x28, 0x9D, 0x44, 0x23, "sub [di + 0x2344], bl");
    testOk2(0x28, 0xC1, "sub cl, al");
    testOk2(0x29, 0x00, "sub [bx + si], ax");
    testOk4(0x29, 0x0E, 0x35, 0x02, "sub [0x0235], cx");
    testOk2(0x29, 0xC7, "sub di, ax");
    testOk2(0x2A, 0x00, "sub al, [bx + si]");
    testOk3(0x2B, 0x77, 0x56, "sub si, [bx + 0x56]");
    testOk2(0x2C, 0x95, "sub al, 0x95");
    testOk3(0x2D, 0xA0, 0x9E, "sub ax, 0x9EA0");
    // TODO: Test 0x2E -> cs:
    // TODO: Test 0x2F -> das
    testOk2(0x30, 0x00, "xor [bx + si], al");
    testOk4(0x30, 0x16, 0x23, 0xBC, "xor [0xBC23], dl");
    testOk3(0x30, 0x64, 0x8A, "xor [si - 0x76], ah");
    testOk4(0x30, 0x9D, 0x44, 0x23, "xor [di + 0x2344], bl");
    testOk2(0x30, 0xC1, "xor cl, al");
    testOk2(0x31, 0x00, "xor [bx + si], ax");
    testOk4(0x31, 0x0E, 0x35, 0x02, "xor [0x0235], cx");
    testOk2(0x31, 0xC7, "xor di, ax");
    testOk2(0x32, 0x00, "xor al, [bx + si]");
    testOk3(0x33, 0x77, 0x56, "xor si, [bx + 0x56]");
    testOk2(0x34, 0x95, "xor al, 0x95");
    testOk3(0x35, 0xA0, 0x9E, "xor ax, 0x9EA0");
    // TODO: Test 0x36 -> ss:
    // TODO: Test 0x37 -> aaa
    testOk2(0x38, 0x00, "cmp [bx + si], al");
    testOk4(0x38, 0x16, 0x23, 0xBC, "cmp [0xBC23], dl");
    testOk3(0x38, 0x64, 0x8A, "cmp [si - 0x76], ah");
    testOk4(0x38, 0x9D, 0x44, 0x23, "cmp [di + 0x2344], bl");
    testOk2(0x38, 0xC1, "cmp cl, al");
    testOk2(0x39, 0x00, "cmp [bx + si], ax");
    testOk4(0x39, 0x0E, 0x35, 0x02, "cmp [0x0235], cx");
    testOk2(0x39, 0xC7, "cmp di, ax");
    testOk2(0x3A, 0x00, "cmp al, [bx + si]");
    testOk3(0x3B, 0x77, 0x56, "cmp si, [bx + 0x56]");
    testOk2(0x3C, 0x95, "cmp al, 0x95");
    testOk3(0x3D, 0xA0, 0x9E, "cmp ax, 0x9EA0");
    // TODO: Test 0x3E -> ds:
    // TODO: Test 0x3F -> aas

    printf("%d tests found. %d passed, %d failed\n", passedCount + failureCount, passedCount, failureCount);
    return failureCount? 1 : 0;
}
