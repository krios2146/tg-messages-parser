# tg-messages-parser 

This project parses Telegram chat messages data and outputs the top 50 users by their message count in the chat

## Local run

Before running the project locally, ensure that you have the following installed:

- **Java Development Kit (JDK)**: [Download here](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html).
- **Scala**: [Download here](https://www.scala-lang.org/download/).
- **SBT (Scala Build Tool)**: [Download here](https://www.scala-sbt.org/download.html).

1. Clone the repository:
```bash
git clone git@github.com:krios2146/tg-messages-parser.git
```
2. Navigate to the project directory:
```bash
cd tg-messages-parser
```

3. Download the Telegram chat data as a .json file.
  - The file should be placed in the `src/main/resources` directory of the project.
  - Name the file `result.json`, or modify the code in `Parser.scala` to reference a different file name.

4. Update project dependencies:
```bash
sbt update
```

5. Compile the project:
```bash
sbt compile
```

6. Run the project:
```bash
sbt run
```

The output is the top 50 chat members by messages count
