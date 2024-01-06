# Interpreter
Tokenizes, parses, and interprets the AWK programming language utilizing Java. Implements most AWK functionality with global functions such as gsub, sub, match, getline, next, and global variables like ORS, FS, NF, NR, FNR, etc.. The parser uses recursive descent to construct the abstract symbol tree (AST).

The process of interpreting goes as follows: 

Lexer (tokenizes the provided text file)  ->  Parser (constructs the AST according to AWK syntax)  ->  Interpreter (assings functionality to the AST)

# Usage
Compile using the following command in the terminal: "javac -d .\bin Main.java", and run with "java -cp .\bin Main code.awk text.txt". The repo comes with a file named code.awk and text.txt, but feel free to edit/use other files.

# Acknowledgments
I'd like to give a big thank you to my professor, Michael Phipps, who gave direction and guidance for this project. I'm also very grateful for the efforts of my peers who contributed on the parser and the implementation of the global functions.
