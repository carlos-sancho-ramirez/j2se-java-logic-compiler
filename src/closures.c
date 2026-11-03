#include <stdio.h>

struct Student {
    int id;
    int age;
};

struct Array {
    int length;
    void *values;
};

int filterPredicate(struct Student *student) {
    return student->age >= 12;
}

void filter(struct Array *target, struct Array *source, int (* predicate)(void *)) {
    int count = 0;
    for (int i = 0; i < source->length; i++) {
        struct Student *item = ((struct Student **) source->values)[i];
        if (predicate(item)) {
            ((struct Student **) target->values)[count++] = item;
        }
    }

    target->length = count;
}

struct Closure {
    void *function;
    void *context;
};

struct FilterPredicateContext {
    int minimumAge;
};

int filterDynamicPredicate(struct FilterPredicateContext *context, struct Student *student) {
    return student->age >= context->minimumAge;
}

void filterWithClosure(struct Array *target, struct Array *source, struct Closure *predicateClosure) {
    int count = 0;
    int (*predicate)(void *, void*) = (int (*)(void *, void*)) predicateClosure->function;
    for (int i = 0; i < source->length; i++) {
        struct Student *item = ((struct Student **) source->values)[i];
        if (predicate(predicateClosure->context, item)) {
            ((struct Student **) target->values)[count++] = item;
        }
    }

    target->length = count;
}

int main(int argc, char *argv[]) {
    struct Student students[5];
    students[0].id = 0;
    students[0].age = 12;
    students[1].id = 1;
    students[1].age = 9;
    students[2].id = 2;
    students[2].age = 14;
    students[3].id = 3;
    students[3].age = 11;
    students[4].id = 4;
    students[4].age = 15;

    struct Student *studentsValues[5];
    studentsValues[0] = students;
    studentsValues[1] = students + 1;
    studentsValues[2] = students + 2;
    studentsValues[3] = students + 3;
    studentsValues[4] = students + 4;

    struct Array studentsArray;
    studentsArray.length = 5;
    studentsArray.values = studentsValues;

    printf("Students:\n");
    for (int i = 0; i < studentsArray.length; i++) {
        struct Student *thisStudent = ((struct Student **) studentsArray.values)[i];
        printf("  id = %d, age = %d\n", thisStudent->id, thisStudent->age);
    }

    struct Student *olderStudentsValues[5];
    struct Array olderStudentsArray;
    olderStudentsArray.values = olderStudentsValues;

    filter(&olderStudentsArray, &studentsArray, (int (*)(void *)) filterPredicate);

    printf("Students 12+:\n");
    for (int i = 0; i < olderStudentsArray.length; i++) {
        struct Student *thisStudent = ((struct Student **) olderStudentsArray.values)[i];
        printf("  id = %d, age = %d\n", thisStudent->id, thisStudent->age);
    }

    const int minimumAge = 13;
    struct FilterPredicateContext filterPredicateContext;
    filterPredicateContext.minimumAge = minimumAge;

    struct Closure predicateClosure;
    predicateClosure.function = filterDynamicPredicate;
    predicateClosure.context = &filterPredicateContext;

    filterWithClosure(&olderStudentsArray, &studentsArray, &predicateClosure);
    printf("Students %d+:\n", minimumAge);
    for (int i = 0; i < olderStudentsArray.length; i++) {
        struct Student *thisStudent = ((struct Student **) olderStudentsArray.values)[i];
        printf("  id = %d, age = %d\n", thisStudent->id, thisStudent->age);
    }
    return 0;
}