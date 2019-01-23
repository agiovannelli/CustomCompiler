; ModuleID = 'hello.c'


; source ~
;	int tester=0; 
;	for (int kai=0;kai<10;kai++)
; 		tester++;

; Function Attrs: nounwind uwtable
define i32 @main() {
  
  ; Allocating the memory for the variables
  ; 	tester = incremented value within loop
  ; 	kai = loop variable
  %tester = alloca i32, align 4
  %kai = alloca i32, align 4
  
  ; Initializing both variables with 0
  store i32 0, i32* %tester, align 4
  store i32 0, i32* %kai, align 4
  
  ; Start the loop
  br label %1

; <label>:1                                       ; the conditional check, switches the flow
  ; %2 = kai
  %2 = load i32, i32* %kai, align 4
  
  ; %3 = %2 < 10 ~ kai < 10
  %3 = icmp slt i32 %2, 10
  ; icmp = compares things
  ; %3 = the result ~ flag

  ; if %3 == true, goto <label>:4; else goto <label>:10
  br i1 %3, label %4, label %10

; <label>:4                                       ; functionality within the loop
  ; %5 = tester
  %5 = load i32, i32* %tester, align 4
  
  ; %6 = %5 + 1 ~ tester++, nsw unnessecary ?
  %6 = add nsw i32 %5, 1
  
  ; %tester = %6
  store i32 %6, i32* %tester, align 4
  
  ; goto <label>:7
  br label %7

; <label>:7                                       ; loop incrementer
  ; %8 = %kai
  %8 = load i32, i32* %kai, align 4
  
  ; %9 = %8 + 1 ~ kai++
  %9 = add nsw i32 %8, 1
  
  ; %kai = %9
  store i32 %9, i32* %kai, align 4
  
  ; goto <label>:1, the start of the loop
  br label %1

; <label>:10                                     ; after the loop
  ret i32 0
}
