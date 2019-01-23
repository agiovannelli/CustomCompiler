; ModuleID = 'hello.c'

; source
; 	int value = 0;
;	if (value==0)
;		value=8;
;	else
;		value=9;

; Function Attrs: nounwind uwtable
define i32 @main() {
  
  ; Allocating memory for value
  %value = alloca i32, align 4
  store i32 0, i32* %value, align 4
  
  ; %1 = value
  %1 = load i32, i32* %value, align 4
  
  ; %2 = %1 == 0 ~ %2 = value == 0
  %2 = icmp eq i32 %1, 0
  
  ; if %2 == true; goto <label>:3; else goto <label>:4
  br i1 %2, label %3, label %4

; <label>:3                                       ; if conditional body
  
  ; value = 8
  store i32 8, i32* %value, align 4
  
  ; goto <label>:5
  br label %5

; <label>:4                                       ; else conditional body

  ; value = 9
  store i32 9, i32* %value, align 4
  
  ; goto <label>:5
  br label %5

; <label>:5                                       ; end of program
  ret i32 0
}

