; ModuleID = 'hello.c'

; source
; void simp()
; {
;	return;
; }

; int main(void)
; {
; 	simp();
; }

; Defining function simp
define void @simp() {
  ret void
}

; Function Attrs: nounwind uwtable
define i32 @main() {

  ; calling function simple with void return type
  call void @simp()

  ret i32 0
}
