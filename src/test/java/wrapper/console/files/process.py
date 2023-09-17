import subprocess

# Replace 'your_program.exe' with the path to your .exe file
exe_path = 'print.exe'

try:
    result = subprocess.run(exe_path, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)

    # The return code is stored in the 'returncode' attribute
    status_code = result.returncode

    # Standard output and error output are stored in 'stdout' and 'stderr' attributes respectively
    stdout_output = result.stdout
    stderr_output = result.stderr

    print(f"Status Code: {status_code}")
    print(f"Standard Output:\n{stdout_output}")
    print(f"Standard Error:\n{stderr_output}")

except FileNotFoundError:
    print(f"Error: The file '{exe_path}' was not found.")
except Exception as e:
    print(f"An error occurred: {e}")
