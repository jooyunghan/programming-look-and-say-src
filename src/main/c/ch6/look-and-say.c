#include <assert.h>
#include <stdlib.h>
#include <stdio.h>

typedef struct proc proc;
struct proc {
  int (*proc)(proc*); // 코루틴 함수 포인터
  char ptr;           // 재진입 위치
  char next;          // read 로 읽은 값
  char prev;          // 이미 읽은 값 (갯수를 세는 값)
  char count;         // 반복된 글자 갯수
};

int init(proc *s) {
  switch (s->ptr) {
    case 0:
      s->ptr++;
      return 1;
    default:
      return 0;
  }
}

int next(proc *s) {
  int prev;
  switch (s->ptr) {
    case 0:
      s->ptr = 1;
      return -1;
    case 1:
      s->prev = s->next;
      s->count = 1;
      s->ptr = 2;
      return -1;
    case 2:                            // 루프 시작점 역할을 한다
      if (s->next == 0) {
        s->ptr = 5;    // 루프 탈출
        return s->count;
      } else if (s->prev == s->next) {
        s->count++;
        return -1;
      } else {
        s->ptr = 3;
        return s->count;
      }
    case 3:
      prev = s->prev;
      s->prev = s->next;
      s->count = 1;
      s->ptr = 4;
      return prev;
    case 4:
      s->ptr = 2;
      return -1;                       // 루프 반복
    case 5:
      s->ptr = 6;
      return s->prev;
    case 6:
      return 0;                        // 코루틴 종료
    default:
      assert("Unreachable");
      return 0;
  }
}

int main(int argc, char** argv) {
  int n = argc == 2 ? atoi(argv[1]) : 10;

  // 코루틴 준비
  proc* procs = (proc*)calloc(n + 1, sizeof(proc));
  procs[0].proc = &init;
  for (int i = 1; i < n + 1; i++) {
    procs[i].proc = &next;
  }

  // 디스패치 루프 시작
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