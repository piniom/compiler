# ExEval Compiler

A full-featured compiler for the ExEval programming language, written in Kotlin. ExEval is a statically typed, functional-style language that compiles to x86-64 assembly code.

## Features

- **Complete compilation pipeline**: Lexical analysis → Parsing → AST generation → Semantic analysis → Code generation
- **Static type system** with type checking and inference
- **x86-64 code generation** producing optimized assembly
- **Comprehensive error reporting** with detailed diagnostics
- **LR(1) parser** with precompiled parsing tables
- **Register allocation** and liveness analysis
- **Support for nested functions** and closures

## Language Overview

ExEval is a simple yet powerful language with C-style syntax and functional programming features:

### Basic Types
- `Int` - Integer numbers
- `Bool` - Boolean values (`true`/`false`) 
- `Nope` - Unit type (equivalent to `void`, with value `()`)

### Example Program
```exeval
// Function declaration with 'foo' keyword
foo add(a: Int, b: Int) -> Int = {
    a + b
}

// Entry point - must return Int
foo main() -> Int = {
    let x: Int = 42;           // Immutable variable
    let mut counter: Int = 0;   // Mutable variable
    
    // Conditional expressions
    let result: Int = if x > 40 then {
        add(x, 10)
    } else {
        0
    };
    
    result
}
```

### Key Language Features
- **Immutable by default**: Variables declared with `let` are constants
- **Explicit mutability**: Use `let mut` for mutable variables
- **Expression-based**: Every statement is an expression with a value
- **Type annotations**: Required for variable declarations
- **Block expressions**: `{}` blocks return the value of their last expression
- **Named parameters**: Function calls support named arguments

## Requirements

- **Java 17** or later (Java 21+ recommended for full test suite)
- **NASM** (Netwide Assembler) for assembly
- **GCC** for linking (or any compatible linker)

### Installing Dependencies

#### Ubuntu/Debian
```bash
sudo apt update
sudo apt install nasm gcc
```

#### macOS
```bash
brew install nasm gcc
```

#### Windows
Install NASM from [nasm.us](https://www.nasm.us/) and GCC via [MinGW-w64](https://www.mingw-w64.org/).

## Building

Clone the repository and build with Gradle:

```bash
git clone https://github.com/piniom/compiler.git
cd compiler
./gradlew build
```

## Usage

Compile an ExEval program:

```bash
./gradlew run --args="path/to/your/program.exe"
```

This will:
1. Parse and analyze your ExEval source code
2. Generate `program.asm` (x86-64 assembly)
3. Assemble with NASM to create `program.o` (requires NASM)
4. Link with GCC to create executable `program` (requires GCC)

**Note**: The compiler will successfully generate assembly code even if NASM/GCC are not installed. You'll only need them for the final assembly and linking steps.

### Example
```bash
# Compile a sample program
./gradlew run --args="app/src/test/resources/programs/valid/variables/creatingSomeVariables.exe"

# Run the generated executable
./app/program
```

## Testing

Run the test suite:

```bash
./gradlew test          # Unit tests only  
```

**Note**: The integration tests (`cucumberTest`) require Java 21 due to compilation settings, while unit tests work with Java 17+.

## Documentation

- [Language Syntax](docs/syntax.md) - Complete syntax reference
- [Grammar Specification](docs/grammar.md) - Formal grammar definition  
- [Token Reference](docs/tokens.md) - Lexical tokens and regex patterns

## Project Structure

```
├── app/src/main/kotlin/org/exeval/
│   ├── lexer/          # Lexical analysis
│   ├── parser/         # Syntax analysis  
│   ├── ast/            # Abstract syntax tree
│   ├── cfg/            # Control flow graphs
│   ├── instructions/   # Code generation
│   └── utilities/      # Helper classes
├── app/src/test/
│   ├── kotlin/         # Unit tests
│   └── resources/
│       ├── programs/   # Sample ExEval programs
│       └── features/   # Cucumber test specifications
└── docs/               # Language documentation
```

## Architecture

The compiler implements a traditional compilation pipeline:

1. **Lexer**: Tokenizes source code using DFA-based regex matching
2. **Parser**: LR(1) parser builds parse tree from tokens
3. **AST**: Converts parse tree to abstract syntax tree
4. **Name Resolution**: Resolves variable and function references
5. **Type Checker**: Validates types and performs inference
6. **Code Generator**: Produces x86-64 assembly with register allocation

## Contributing

### Development Workflow

When developing a feature:
1. Create an issue (if not already created)
2. Link the issue to the milestone
3. Create a branch with name: `stage{STAGE_NUMBER}/feature-name`
   - `STAGE_NUMBER` is the week number for milestone tracking
4. Create a PR with linked issue
5. Link the PR to the milestone

### Code Standards
- Follow Kotlin coding conventions
- Add tests for new features
- Update documentation as needed
- Ensure all tests pass before submitting PR

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details.

## Authors

Agata Margas, Dominik Chmura, Jakub Siuta, Jakub Sordyl, Kacper Kozak, Leonid Dorochko, Mikołaj Jachowicz, Mateusz Hurałka, Michał Miziołek, Szymon Wojtulewicz, Mateusz Puto
