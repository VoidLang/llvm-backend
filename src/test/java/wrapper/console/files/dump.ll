; ModuleID = 'print_test'
source_filename = "print_test"

@text = global [17 x i8] c"Hello, World! \C3\81b"

declare i32 @GetStdHandle(i32)

declare i32 @GetLastError()

declare i32 @WriteConsoleA(i32, ptr, i32, ptr, i32)

define i32 @main() {
entry:
  %std_handle = call i32 @GetStdHandle(i32 -11)
  %bytesWritten = alloca i32, align 4
  %print = call i32 @WriteConsoleA(i32 %std_handle, ptr @text, i32 18, ptr %bytesWritten, i32 0)
  %written = load i32, ptr %bytesWritten, align 4
  ret i32 %written
}
