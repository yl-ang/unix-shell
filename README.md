# CS4218 Codebase
codebase for CS4218, 23/24 Sem2 (Team 04)

## Software
Java Version: Oracle Java SDK 17.0.7 \
Operation System: Ubuntu 22.04 (based on GitHub Runners)

### Running Tests
1. In the root of the repo run
`mvn clean test`
using bash.

### Running Shell
1. In the root of the repo run 
`mvn clean package` 
using bash.
2. Run `java -cp target/classes sg.edu.nus.comp.cs4218.impl.ShellImpl` in bash.

## Tests
This section describe the folders in the test folder `public_tests`
- `unit_tests`: unit test written by our team
- `integration_tests`: integration test written by our team
- `tdd`: test-driven develop test cases written in milestone 1 for unimplemented features in milestone 1
- `external_tests`: provided test-driven develop test cases as part of milestone 2
- `resources`: files use for supporting testing