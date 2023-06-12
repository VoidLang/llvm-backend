import subprocess

# Replace "program.exe" with the actual path to your .exe file
exe_path = "output.exe"

# Run the .exe file using subprocess
process = subprocess.Popen(exe_path, stdout=subprocess.PIPE, stderr=subprocess.PIPE)

# Wait for the process to finish and get the exit code
exit_code = process.wait()

# Print the exit code
print("Exit code:", exit_code)