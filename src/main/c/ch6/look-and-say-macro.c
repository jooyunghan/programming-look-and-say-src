#include <assert.h>
#include <stdlib.h>
#include <stdio.h>

#define crBegin    switch(s->ptr) { case 0:
#define crYield(x) do { s->ptr=__LINE__; return x; \
                         case __LINE__:; } while (0)
#define crEnd      } return 0;

typedef struct proc proc;
struct proc {
  int (*proc)(proc*); // 코루틴 함수 포인터
  char ptr;           // 재진입 위치
  char next;          // read 로 읽은 값
  char prev;          // 이미 읽은 값 (갯수를 세는 값)
  char count;         // 반복된 글자 갯수
};

int init(proc *s) {
  crBegin;
  crYield(1);
  crEnd;
}

int next(proc *s) {
  crBegin;
  crYield(-1);
  s->prev = s->next;
  s->count = 1;
  while(1) {
    crYield(-1);
    if (s->next == 0) {
      break;
    } else if (s->prev == s->next) {
      s->count++;
    } else {
      crYield(s->count);
      crYield(s->prev);
      s->prev = s->next;
      s->count = 1;
    }
  }
  crYield(s->count);
  crYield(s->prev);
  crEnd;
}

int main(int argc, char** argv) {
  int n = argc == 2 ? atoi(argv[1]) : 10;

  // 코루틴 준비
  proc* procs = (proc*)calloc(n + 1, sizeof(proc));
  procs[0].proc = &init;
  for (int i = 1; i < n + 1; i++) {
    procs[i].proc = &next;
  }

  // 디스패치 루프
  int cur = n;
  while (cur < n + 1) {
    int result = procs[cur].proc(&procs[cur]);
    if (result == -1) {
      cur--;
    } else if (cur < n) {
      cur++;
      procs[cur].next = result;
    } else if (result != 0) {
      printf("%d", result);
    } else {
      printf("\n");
      break;
    }
  }

  free(procs);
  return 0;
}