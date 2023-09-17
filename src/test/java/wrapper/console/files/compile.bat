clang -c -o test.obj bitcode.bc

:: clang -o print.exe test.obj -lkernel32
clang -o print.exe test.obj -luser32 -lgdi32 -lkernel32
