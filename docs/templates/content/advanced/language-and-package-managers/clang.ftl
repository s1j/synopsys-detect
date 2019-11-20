# C/C++ (CLang) support

C/C++ (CLang) support is limited to Linux systems that support one of the following
package manager commands: APK, DPKG, RPM.

For C/C++ projects on Linux systems that meet these requirements, ${solution_name} can derive
dependency information using information read from a JSON Compilation Database
(compile_commands.json file) and the Linux package manager.

The JSON Compilation Database
must be generated by your project build before ${solution_name} runs.
There are multiple C/C++ build tools that are capable of generating a JSON Compilation Database.
For example, some versions of CMake will generate a JSON Compilation Database
when run with the following option:

    -DCMAKE_EXPORT_COMPILE_COMMANDS=ON

The CLang detector will run when it finds a compile_commands.json file
in the project directory. If the compile_commands.json file resides in a sub-directory,
adjust the [detector search depth](../../properties/Configuration/paths.md#detector-search-depth)
to enable ${solution_name} to find it. If the compile_commands.json file
resides and a normally excluded subdirectory (for example: ./build),
you can turn off the [default detector search exclusions](../../properties/Configuration/paths.md#detector-exclude-default-directories-advanced),
or fine tune the [detector search directory exclusion patterns](../../properties/Configuration/paths.md#detector-directory-patterns-exclusions-advanced).

For each compile command in the compile_commands.json file, the CLang detector
runs a version of the command that has been modified to (a) ensure that it does
not overwrite build artifacts, and (b) generate a list of include files used
(it does this by adding the *-M* and *-MF* compiler options).
It then uses the Linux package manager to identify which installed package owns each
include file. These packages are added as a component to the results.