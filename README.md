# CompSh – A Shell-Like Compiled Language

**CompSh** is a compiled programming language designed to handle the **execution and testing of external programs**, combining features of traditional shell languages with support for **numeric and textual data types** and standard arithmetic operations.  

Developed as part of the **Compilers course** at the University of Aveiro (April 2025), this project implements a minimal compiler and interpreter for the CompSh language and its interpreted counterpart **ISh**.

---

## Project Overview

The CompSh language aims to:
- Provide a shell-like syntax for program execution using pipes.  
- Support data types such as `text`, `integer`, `real`, and `program`.  
- Allow arithmetic and text operations similar to traditional programming languages.  
- Manage multiple communication channels (expression `$`, stdout `|`, stderr `&`, and exit `?`) within programs.  
- Compile CompSh source files (`.csh`) into equivalent programs in a target language (e.g., Java, Python).  
- Support ISh (`.ish`) interpreted programs as well.

### Example (Shell Concept)
```bash
ls | sort   # Bash example (not CompSh)
```
### Example (CompSh Equivalent)
```lua
! "ls" ! | stdout
```

## Language Features

### Minimum Features

* Execution of external programs: !"ls"!

* Text, integer, real, and program data types

* Arithmetic expressions with type checking

* Pipe operator for chaining operations

* Standard output & error writing instructions

* Input reading from standard input

* Type conversion (e.g., integer("10"), text(10))

* Basic semantic type checking in pipes

### Desirable / Advanced (if implemented)
* Boolean expressions and conditional statements

* Loops

* Channel selection and redirection (|, &, ?, $, *, -)

* List type and literal values

* Text filters and prefix/suffix operators (like grep or sed)

* Functions and local variables

* Full semantic analysis of pipe operations

## Running the Project

### 1️ Build the Compiler
```bash
./build
```
### 2️ Compile a Source File
Use one of the example programs provided in the examples/ directory:
```bash
./compile ../examples/min01.csh
# or
./compile ../examples/min02.csh
./compile ../examples/min03.csh
```

## 3️ Run the Compiled Program
```bash
./run
```

## Project Structure
```python
.
├── build             # Build script for the compiler
├── compile           # Compiler executable
├── run               # Program execution script
├── src/              # Source code of the compiler/interpreter
├── examples/         # Example .csh and .ish programs
└── README.md
```

## What I Learned
* Implementing a domain-specific language (DSL) using compilation techniques

* Designing a lexer, parser, and semantic analyzer for a shell-like language

* Handling multiple I/O channels in a compiler context

* Using pipes, type systems, and program execution in a controlled environment

* Building a minimal interpreter for an embedded language (ISh)

## Team Contributions

This project was developed collaboratively as part of the Compilers course.  
The table below shows the contribution of each team member:

|  Name                                 | Contribution |
|---------------------------------------|--------------|
| Arthur Melo e Vale Monetto            |     23.3%    | 
| João Vitor Batista Domingues Ferreira |     23.3%    |
| Luís Carlos Ferreira dos Santos       |     30.1%    |
| Pedro Miguel Tavares Rodrigues        |     23.3%    |
